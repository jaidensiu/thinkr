import { Router } from 'express';
import {
    createChatSession,
    sendChatMessage,
    getChatSession,
    deleteChatSession,
} from '../controllers/chatController';

const router = Router();

router.post('/', createChatSession);

router.get('/:sessionId', getChatSession);

router.post('/:sessionId/message', sendChatMessage);

router.delete('/:sessionId', deleteChatSession);

export default router;
