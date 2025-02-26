import { Router } from 'express';
import { queryRAG } from '../controllers/ragController';

const router = Router();

router.post('/query', queryRAG);

export default router;
