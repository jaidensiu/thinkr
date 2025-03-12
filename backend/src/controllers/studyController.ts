import { Result } from '../interfaces';
import { Request, Response } from 'express';
import StudyService from '../services/studyService';

/**
 * Handles retrieving flashcards based on documents provided or by userId
 */
export const retrieveFlashcards = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const userId = req.query.userId as string;
        const documentId = req.query.documentId as string;

        if (!userId) {
            res.status(400).json({
                message: 'You must provide a userId identifier',
            });
            return;
        }
        const flashcards = await StudyService.retrieveFlashcards(
            documentId,
            userId
        );

        res.status(200).json({ data: flashcards } as Result);
    } catch (error) {
        console.error('Error generating quiz:', error);
        res.status(500).json({
            message: 'Internal server error',
        });
        return;
    }
};

/**
 * Handles retrieving quizzes based on documents provided or by userId
 */
export const retrieveQuizzes = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const userId = req.query.userId as string;
        const documentId = req.query.documentId as string;

        if (!userId) {
            res.status(400).json({
                message: 'You must provide a userId identifier',
            });
            return;
        }

        const quizzes = await StudyService.retrieveQuizzes(documentId, userId);

        res.status(200).json({ data: quizzes } as Result);
    } catch (error) {
        console.error('Error generating quiz:', error);
        res.status(500).json({
            message: 'Internal server error',
        });
        return;
    }
};

/**
 * Handles retrieving suggested study materials for a user
 */
export const getSuggestedMaterials = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const userId = req.query.userId as string;
        const limit = req.query.limit ? parseInt(req.query.limit as string) : 5;

        if (!userId) {
            res.status(400).json({
                message: 'You must provide a userId identifier',
            } as Result);
            return;
        }

        const suggestedMaterials = await StudyService.getSuggestedMaterials(
            userId,
            limit
        );

        res.status(200).json({
            data: suggestedMaterials,
        } as Result);
    } catch (error) {
        console.error('Error retrieving suggested materials:', error);
        res.status(500).json({
            message: 'Internal server error',
        } as Result);
    }
};
