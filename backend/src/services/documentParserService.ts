import {
    TextractClient,
    AnalyzeDocumentCommand,
    FeatureType,
} from '@aws-sdk/client-textract';
import dotenv from 'dotenv';
dotenv.config();

class DocumentParser {
    private textractClient: TextractClient;
    private bucketName: string;

    constructor() {
        this.bucketName = process.env.S3_BUCKET_NAME!;
        this.textractClient = new TextractClient({
            region: process.env.AWS_REGION,
            credentials: {
                accessKeyId: process.env.AWS_ACCESS_KEY_ID!,
                secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY!,
            },
        });
    }

    /**
     * Extracts text from a file using AWS Textract.
     */
    public async extractTextFromFile(s3FilePath: string): Promise<string> {
        const params = {
            Document: {
                S3Object: {
                    Bucket: this.bucketName,
                    Name: s3FilePath,
                },
            },
            FeatureTypes: [
                FeatureType.TABLES,
                FeatureType.FORMS,
                FeatureType.SIGNATURES,
            ],
        };

        try {
            const command = new AnalyzeDocumentCommand(params);
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

export default new DocumentParser();
