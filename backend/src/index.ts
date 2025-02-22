import express, { Request, Response } from 'express';
import dotenv from 'dotenv';
import authRouter from './routes/userAuthRoutes';
import connectMongoDB from './db/mongo/connection';
import RAGService from './services/RAGService';

dotenv.config();

const PORT = 3000;
const app = express();

app.use(express.json());
app.use('/auth', authRouter);

// RAG endpoint
app.post('/rag/query', (req: Request, res: Response): Promise<void> => {
    return (async () => {
        try {
            const { query } = req.body;
            if (!query) {
                res.status(400).json({ message: 'Query is required' });
                return;
            }

            const ragService = new RAGService({
                openAIApiKey: process.env.OPENAI_API_KEY!,
                vectorStoreUrl: process.env.VECTOR_STORE_URL!
            });

            const documents = await ragService.fetchRelevantDocuments(query);
            const response = await ragService.queryLLM(query, documents);

            res.json({ response });
        } catch (error) {
            console.error('RAG query error:', error);
            res.status(500).json({ message: 'Error processing query' });
        }
    })();
});


connectMongoDB();

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
