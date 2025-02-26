import {
    S3Client,
    PutObjectCommand,
    DeleteObjectCommand,
    GetObjectCommand,
} from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import dotenv from 'dotenv';
import Document from '../db/mongo/models/Document';
import RAGService from './RAGService';
import { DocumentDTO } from '../interfaces';
import {
    TextractClient,
    DetectDocumentTextCommand,
} from '@aws-sdk/client-textract';

dotenv.config();

class DocumentService {
    private s3Client: S3Client;
    private bucketName: string;
    private date: Date;
    private ragService: RAGService;
    private textractClient: TextractClient;

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
        this.textractClient = new TextractClient({
            region: process.env.AWS_REGION,
            credentials: {
                accessKeyId: process.env.AWS_ACCESS_KEY_ID!,
                secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY!,
            },
        });
    }

    /**
     * Uploads a file to s3 and mongodb
     *
     */
    public async uploadDocument(
        file: Express.Multer.File,
        userId: string
    ): Promise<DocumentDTO> {
        await this.ragService.ensureVectorStore(userId);
        const key = `${userId}-${file.originalname}`;

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
        const text = await this.extractTextFromFile(key);
        const embeddingsId = await this.ragService.insertDocument(
            userId,
            text,
            file.originalname
        );

        // mongodb
        await Document.findOneAndUpdate(
            { s3Path: key },
            {
                name: file.originalname,
                userId,
                s3Path: key,
                uploadDate: dateFormatted,
                embeddingsId: embeddingsId,
                activityGenerationComplete: false
            },
            { upsert: true, new: true }
        );

        return {
            documentId: file.originalname,
            uploadTime: dateFormatted,
            activityGenerationComplete: false
        } as DocumentDTO;
    }

    /**
     * Uploads multiple files to s3 and mongodb
     *
     */
    // public async uploadDocuments(
    //     files: Express.Multer.File[],
    //     userId: string
    // ): Promise<DocumentDTO[]> {
    //     await this.ragService.ensureVectorStore(userId);

    //     const docs = await Promise.all(
    //         files.map((file) => this.uploadDocument(file, userId))
    //     );
    //     return docs as DocumentDTO[];
    // }

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
        await Document.deleteOne({ s3documentId: key });
        return;
    }

    /**
     * Deletes documents on s3 and mongodb
     *
     */

    public async deleteDocuments(
        documentIds: string[],
        userId: string
    ): Promise<void> {
        await Promise.all(
            documentIds.map((documentId) => this.deleteDocument(`${userId}-${documentId}`))
        );
        await this.ragService.deleteDocuments(documentIds, userId);
        return;
    }

    /**
     * Retrieves a document from s3
     *
     */
    public async getDocument(
        key: string,
        userId: string,
    ): Promise<DocumentDTO> {
        const doc = await Document.findOne({ s3Path: `${userId}-${key}` });

        return {
            documentId: key,
            uploadTime: doc?.uploadDate,
            activityGenerationComplete: doc?.activityGenerationComplete
        } as DocumentDTO;
    }

    /**
     * Retrieves multiple documents from s3
     *
     */
    public async getDocuments(
        keys: string[],
        userId: string
    ): Promise<DocumentDTO[]> {
        let documents;
        if (keys) {
            documents = await Promise.all(
                keys.map((key) => this.getDocument(key, userId))
            );
        } else {
            const allKeys = (await Document.find({ userId: userId })).map(
                (doc) => doc.name
            );
            documents = await Promise.all(
                allKeys.map((key) => this.getDocument(key, userId))
            );
        }
        return documents;
    }

    /**
     * Extracts text from a file using AWS Textract.
     */
    private async extractTextFromFile(s3FilePath: string): Promise<string> {
        const params = {
            Document: {
                S3Object: {
                    Bucket: this.bucketName,
                    Name: s3FilePath,
                },
            },
        };

        try {
            const command = new DetectDocumentTextCommand(params);
            const response = await this.textractClient.send(command);

            let extractedText = '';
            if (response.Blocks) {
                for (const block of response.Blocks) {
                    if (block.BlockType === 'LINE' && block.Text) {
                        extractedText += block.Text + '\n';
                    }
                }
            }

            return extractedText.trim();
        } catch (error) {
            console.error('Error extracting text from PDF:', error);
            throw new Error('Failed to extract text from PDF');
        }
    }
}

export default new DocumentService();
