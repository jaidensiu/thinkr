import { Router } from 'express';
import { subscribe, unsubscribe } from '../controllers/subscriptionController';

const router = Router();

router.post('/', subscribe);
router.delete('/', unsubscribe);

export default router;
