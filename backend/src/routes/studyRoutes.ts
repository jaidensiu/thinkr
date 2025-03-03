import { Router } from 'express';
import {
    generateFlashCards,
    generateQuiz,
    retrieveFlashcards,
    retrieveQuizzes,
    getSuggestedMaterials
} from '../controllers/studyController';

const router = Router();

router.post('/flashcards', generateFlashCards);
router.get('/flashcards', retrieveFlashcards);

router.post('/quiz', generateQuiz);
router.get('/quiz', retrieveQuizzes);

router.get('/suggestedMaterials', getSuggestedMaterials);

export default router;
