import { Request, Response } from 'express';
import DocumentService from '../services/documentService';
import { Result } from '../interfaces';

/**
 * Handles document uploads
 *
 */
export const uploadDocuments = async (
    req: Request,
    res: Response
): Promise<void> => {
    const { userId } = req.body;

    if (!userId || !req.files || !Array.isArray(req.files)) {
        res.status(400).json({
            message: 'Bad Request, missing userId or files',
        } as Result);
        return;
    }

    try {
        const files = req.files as Express.Multer.File[];
        const docs = await DocumentService.uploadDocuments(files, userId);

        res.status(200).json({
            data: { docs },
        } as Result);
    } catch (error) {
        console.error('Error uploading documents:', error);

        res.status(500).json({
            message: 'Failed to upload documents',
        } as Result);
    }
};

/**
 * Handles deleting documents
 *
 */
export const deleteDocuments = async (
    req: Request,
    res: Response
): Promise<void> => {
    const { userId, paths } = req.body;

    if (!userId || !paths || !Array.isArray(paths)) {
        res.status(400).json({
            message: 'Bad Request, userId and paths are required',
        } as Result);
        return;
    }

    try {
        await DocumentService.deleteDocuments(paths, userId);
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
    const { paths, userId } = req.body;

    if (!userId || (paths && !Array.isArray(paths))) {
        res.status(400).json({
            message: 'Bad Request, userId is required',
        } as Result);
        return;
    }

    try {
        const docs = await DocumentService.getDocuments(paths, userId);

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
