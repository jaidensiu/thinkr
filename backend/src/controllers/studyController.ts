import { Result } from '../interfaces';
import { Request, Response } from 'express';
import StudyService from '../services/studyService';

export const generateFlashCards = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { documentId, userId } = req.body;

        if (!documentId || !userId) {
            res.status(400).json({
                message:
                    'You must provide a documentId and an userId identifier',
            });
            return;
        }

        const flashcards = await StudyService.createFlashCards(
            documentId,
            userId
        );

        res.status(200).json({ data: flashcards } as Result);
    } catch (error) {
        console.error('Error generating flashcards:', error);
        res.status(500).json({
            message: 'Internal server error',
        });
        return;
    }
};

export const generateQuiz = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { documentId, userId } = req.body;

        if (!documentId || !userId) {
            res.status(400).json({
                message:
                    'You must provide a documentId and an userId identifier',
            });
            return;
        }

        const quiz = await StudyService.createQuiz(documentId, userId);

        res.status(200).json({ data: quiz } as Result);
    } catch (error) {
        console.error('Error generating quiz:', error);
        res.status(500).json({
            message: 'Internal server error',
        });
        return;
    }
};

export const retrieveFlashcards = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { documentIds, userId } = req.body;

        if (!userId || (documentIds && !Array.isArray(documentIds))) {
            res.status(400).json({
                message: 'You must provide a userId identifier',
            });
            return;
        }

        const flashcards = await StudyService.retrieveFlashcards(
            documentIds,
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

export const retrieveQuizzes = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { documentIds, userId } = req.body;

        if (!userId || (documentIds && !Array.isArray(documentIds))) {
            res.status(400).json({
                message: 'You must provide a userId identifier or documentIds is invalid',
            });
            return;
        }

        const quizzes = await StudyService.retrieveQuizzes(documentIds, userId);

        res.status(200).json({ data: quizzes } as Result);
    } catch (error) {
        console.error('Error generating quiz:', error);
        res.status(500).json({
            message: 'Internal server error',
        });
        return;
    }
};
