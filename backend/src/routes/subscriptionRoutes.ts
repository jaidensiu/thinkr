import { Router } from 'express';
import {
    getSubscriptionStatus,
    subscribe,
    unsubscribe,
} from '../controllers/subscriptionController';

const router = Router();

router.post('/', subscribe);
router.delete('/', unsubscribe);
router.get('/', getSubscriptionStatus);
export default router;
