import { Router } from 'express';
import { generateFlashCards, generateQuiz, retrieveFlashcards, retrieveQuizzes } from '../controllers/studyController';

const router = Router();

router.post('/flashcards', generateFlashCards);
router.get('/flashcards', retrieveFlashcards);

router.post('/quiz', generateQuiz);
router.get('/quiz', retrieveQuizzes);
export default router;
