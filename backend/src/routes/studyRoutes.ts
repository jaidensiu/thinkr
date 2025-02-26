import { Router } from 'express';
import {
    generateFlashCards,
    generateQuiz,
} from '../controllers/studyController';

const router = Router();

router.post('/flashcards', generateFlashCards);
router.post('/quiz', generateQuiz);

export default router;
