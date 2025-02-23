import {
    S3Client,
    PutObjectCommand,
    DeleteObjectCommand,
    GetObjectCommand,
} from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import dotenv from 'dotenv';
import Document from '../db/mongo/models/Document';
import DocumentParser from './documentParserService';
import RAGService from './RAGService';
import { DocumentDTO } from '../interfaces';

dotenv.config();

class DocumentService {
    private s3Client: S3Client;
    private bucketName: string;
    private date: Date;
    private ragService: RAGService;

    constructor() {
        this.s3Client = new S3Client({
            region: process.env.region,
            credentials: {
                accessKeyId: process.env.AWS_ACCESS_KEY_ID!,
                secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY!,
            },
        });
        this.bucketName = process.env.S3_BUCKET_NAME!;
        this.date = new Date();
        this.ragService = new RAGService({
            openAIApiKey: process.env.OPENAI_API_KEY!,
            vectorStoreUrl: process.env.VECTOR_STORE_URL!,
        });
    }

    /**
     * Uploads a file to s3 and mongodb
     *
     */
    public async uploadDocument(
        file: Express.Multer.File,
        email: string,
        user: string
    ): Promise<DocumentDTO> {
        const key = `${email}-${file.originalname}`;

        const params = {
            Bucket: this.bucketName,
            Key: key,
            Body: file.buffer,
            ContentType: file.mimetype,
        };

        // s3
        const command = new PutObjectCommand(params);
        await this.s3Client.send(command);
        const dateFormatted = this.date
            .toISOString()
            .replace(/T/, ' ')
            .replace(/\..+/, '');

        // textract -> vector db
        const text = await DocumentParser.extractTextFromFile(key);
        const embeddingsId = await this.ragService.insertDocument(
            user,
            text,
            file.originalname
        );

        // mongodb
        await Document.findOneAndUpdate(
            { s3Path: key },
            {
                name: file.originalname,
                email,
                s3Path: key,
                uploadDate: dateFormatted,
                embeddingsId: embeddingsId,
            },
            { upsert: true, new: true }
        );

        return {
            name: file.originalname,
            uploadTime: dateFormatted
        } as DocumentDTO;
    }

    /**
     * Uploads multiple files to s3 and mongodb
     *
     */
    public async uploadDocuments(
        files: Express.Multer.File[],
        email: string
    ): Promise<DocumentDTO[]> {
        const user = email.replace('@gmail.com', '');
        await this.ragService.ensureVectorStore(user);

        const docs = await Promise.all(
            files.map((file) => this.uploadDocument(file, email, user))
        );
        return docs as DocumentDTO[];
    }

    /**
     * Deletes a document on s3 and mongodb
     *
     */
    public async deleteDocument(key: string): Promise<void> {
        const params = {
            Bucket: this.bucketName,
            Key: key,
        };

        // s3
        const command = new DeleteObjectCommand(params);
        await this.s3Client.send(command);

        // mongodb
        await Document.deleteOne({ s3Path: key });
        return;
    }

    /**
     * Deletes documents on s3 and mongodb
     *
     */

    public async deleteDocuments(
        paths: string[],
        email: string
    ): Promise<void> {
        const user = email.replace('@gmail.com', '');

        await Promise.all(
            paths.map((path) => this.deleteDocument(`${email}-${path}`))
        );
        await this.ragService.deleteDocuments(paths, user);
        return;
    }

    /**
     * Retrieves a document from s3
     *
     */
    public async getDocument(
        key: string,
        email: string,
        expiresIn: number = 3600
    ): Promise<DocumentDTO> {

        let params = {
            Bucket: this.bucketName,
            Key: `${email}-${key}`,
        };

        const command = new GetObjectCommand(params);
        const documentUrl = await getSignedUrl(this.s3Client, command, {
            expiresIn,
        });
        const doc = await Document.findOne({ s3Path: `${email}-${key}`})
          
        return {
            url: documentUrl,
            name: key,
            uploadTime: doc!.uploadDate 
        } as DocumentDTO;
    }

    /**
     * Retrieves multiple documents from s3
     *
     */
    public async getDocuments(keys: string[], email: string): Promise<DocumentDTO[]> {
        let documents;
        if (keys) {
            documents = await Promise.all(
                keys.map((key) => this.getDocument(key, email))
            );
        } else {
            const allKeys = (await Document.find({ email: email })).map(
                (doc) => doc.name
            );
            documents = await Promise.all(
                allKeys.map((key) => this.getDocument(key, email))
            );
        }
        return documents;
    }
}

export default new DocumentService();
