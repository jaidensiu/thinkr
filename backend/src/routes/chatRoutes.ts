import { Router } from 'express';
import {
    createChatSession,
    getChatSession,
    sendChatMessage,
    deleteChatSession,
    getUserChatSessions
} from '../controllers/chatController';

const router = Router();

// Create a new chat session
router.post('/', createChatSession);

// Get a specific chat session
router.get('/:sessionId', getChatSession);

// Get all chat sessions for a user
router.post('/user/sessions', getUserChatSessions);

// Send a message to a chat session
router.post('/:sessionId/message', sendChatMessage);

// Delete a chat session
router.delete('/:sessionId', deleteChatSession);

export default router;
