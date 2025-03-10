import { Router } from 'express';
import {
    getUserChat,
    sendMessage,
    clearChatHistory,
} from '../controllers/chatController';

const router = Router();

// Get or create a user's chat
router.get('/', getUserChat);

// Send a message to the user's chat
router.post('/message', sendMessage);

// Clear chat history
router.delete('/history', clearChatHistory);

export default router;
