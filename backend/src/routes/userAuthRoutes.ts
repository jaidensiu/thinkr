import { Router } from 'express';
import { userAuthLogin } from '../controllers/userAuthController';

const router = Router();

router.post('/login', userAuthLogin);

export default router;
