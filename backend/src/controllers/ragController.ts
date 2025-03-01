import { Request, Response } from 'express';
import RAGService from '../services/RAGService';
import { Result } from '../interfaces';

/**
 * Handles RAG queries by fetching relevant documents and generating responses
 */
export const queryRAG = async (req: Request, res: Response): Promise<void> => {
    try {
        const { query, userId, documentId } = req.body;

        if (!query || !userId) {
            res.status(400).json({
                message: 'Query and userId are required',
            } as Result);
            return;
        }

        const ragService = new RAGService({
            openAIApiKey: process.env.OPENAI_API_KEY!,
            vectorStoreUrl: process.env.VECTOR_STORE_URL!,
        });

        // Fetch relevant documents from the user's collection
        // If documentId is provided, filter to only that document
        const documents = await ragService.fetchRelevantDocumentsFromQuery(
            query,
            userId,
            documentId
        );

        const response = await ragService.queryLLM(query, documents);

        res.status(200).json({
            data: { response },
        } as Result);
    } catch (error) {
        console.error('RAG query error:', error);
        res.status(500).json({
            message: 'Error processing query',
        } as Result);
    }
};
