import { Router } from 'express';
import { generateFlashCards } from '../controllers/studyController';

const router = Router();

router.post('/flashcards', generateFlashCards);

export default router;
