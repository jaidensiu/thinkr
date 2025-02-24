import { Result } from '../interfaces';
import { Request, Response } from 'express';
import StudyService from '../services/studyService';

export const generateFlashCards = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { paths, email } = req.body;

        if (!Array.isArray(paths) || paths.length === 0 || !email) {
            res.status(400).json({
                message:
                    'You must provide an array in paths and an email identifier',
            });
            return;
        }

        const user = email.replace('@gmail.com', '');
        const flashcards = await StudyService.createFlashCards(paths, user);

        res.status(200).json({ data: flashcards } as Result);
    } catch (error) {
        console.error('Error generating flashcards:', error);
        res.status(500).json({
            message: 'Internal server error, unable to generate flashcards',
        });
        return;
    }
};
