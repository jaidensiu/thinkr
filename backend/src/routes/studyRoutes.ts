import { Router } from 'express';
import {
    retrieveFlashcards,
    retrieveQuizzes,
    getSuggestedMaterials,
} from '../controllers/studyController';

const router = Router();

router.get('/flashcards', retrieveFlashcards);
router.get('/quiz', retrieveQuizzes);
router.get('/suggestedMaterials', getSuggestedMaterials);

export default router;
