import { Result } from '../interfaces';
import { Request, Response } from 'express';
import StudyService from '../services/studyService';

export const generateFlashCards = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { paths, userId } = req.body;

        if (!Array.isArray(paths) || paths.length === 0 || !userId) {
            res.status(400).json({
                message:
                    'You must provide a paths (array) and an userId identifier',
            });
            return;
        }

        const flashcards = await StudyService.createFlashCards(paths, userId);

        res.status(200).json({ data: flashcards } as Result);
    } catch (error) {
        console.error('Error generating flashcards:', error);
        res.status(500).json({
            message: 'Internal server error, unable to generate flashcards',
        });
        return;
    }
};
