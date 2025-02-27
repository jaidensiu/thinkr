import { Request, Response } from 'express';
import DocumentService from '../services/documentService';
import { Result } from '../interfaces';
import StudyService from '../services/studyService';
import Document from '../db/mongo/models/Document';
import RAGService from '../services/RAGService';

/**
 * Handles document uploads
 *
 */
export const uploadDocuments = async (
    req: Request,
    res: Response
): Promise<void> => {
    const { userId } = req.body;

    if (!userId || !req.file) {
        res.status(400).json({
            message: 'Bad Request, missing userId or files',
        } as Result);
        return;
    }

    try {
        const file = req.file as Express.Multer.File;
        const docs = await DocumentService.uploadDocument(file, userId);

        res.status(200).json({
            data: { docs },
        } as Result);
        generateStudyActivities(docs.documentId, userId);
    } catch (error) {
        console.error('Error uploading documents:', error);

        res.status(500).json({
            message: 'Failed to upload documents',
        } as Result);
    }
};

const generateStudyActivities = async (documentId: string, userId: string) => {
    // textract -> vector db
    const ragService = new RAGService({
        openAIApiKey: process.env.OPENAI_API_KEY!,
        vectorStoreUrl: process.env.VECTOR_STORE_URL!,
    });

    await ragService.ensureVectorStore(userId);
    const text = await DocumentService.extractTextFromFile(
        `${userId}-${documentId}`
    );
    await ragService.insertDocument(userId, text, documentId);

    // generate activities
    await StudyService.createFlashCards(documentId, userId);
    await StudyService.createQuiz(documentId, userId);
    // Create chat
    await Document.findOneAndUpdate(
        { userId: userId, name: documentId },
        { activityGenerationComplete: true }
    );
};

/**
 * Handles deleting documents
 *
 */
export const deleteDocuments = async (
    req: Request,
    res: Response
): Promise<void> => {
    const { userId, documentIds } = req.body;

    if (!userId || !documentIds || !Array.isArray(documentIds)) {
        res.status(400).json({
            message: 'Bad Request, userId and documentIds are required',
        } as Result);
        return;
    }

    try {
        await DocumentService.deleteDocuments(documentIds, userId);

        const ragService = new RAGService({
            openAIApiKey: process.env.OPENAI_API_KEY!,
            vectorStoreUrl: process.env.VECTOR_STORE_URL!,
        });
        await ragService.deleteDocuments(documentIds, userId);

        res.status(200).json();
        return;
    } catch (error) {
        console.error('Error deleting documents:', error);

        res.status(500).json({
            message: 'Failed to delete documents',
        } as Result);
    }
};

/**
 * Handles retrieving documents
 *
 */
export const getDocuments = async (
    req: Request,
    res: Response
): Promise<void> => {
    const { documentIds, userId } = req.body;

    if (!userId || (documentIds && !Array.isArray(documentIds))) {
        res.status(400).json({
            message: 'Bad Request, userId is required',
        } as Result);
        return;
    }

    try {
        const docs = await DocumentService.getDocuments(documentIds, userId);

        const result: Result = {
            data: { docs },
        };
        res.status(200).json(result);
    } catch (error) {
        console.error('Error retrieving document URL:', error);
        const result: Result = {
            message: 'Failed to retrieve document URL',
        };
        res.status(500).json(result);
    }
};
