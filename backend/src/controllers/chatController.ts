import { Request, Response } from 'express';
import ChatService from '../services/ChatService';
import { Result } from '../interfaces';

/**
 * Get or create a chat for a user
 */
export const getUserChat = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const userId = req.query.userId as string;

        if (!userId) {
            res.status(400).json({
                message: 'User ID is required',
            });
            return;
        }

        const chat = await ChatService.getOrCreateUserChat(userId);

        res.status(200).json({
            data: { chat },
        } as Result);
    } catch (error) {
        console.error('Error getting user chat:', error);
        res.status(500).json({
            message: 'Internal server error',
        });
    }
};

/**
 * Send a message to the user's chat
 */
export const sendMessage = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const userId = req.body.userId as string;
        const message = req.body.message as string;

        if (!userId || !message) {
            res.status(400).json({
                message: 'User ID and message are required',
            });
            return;
        }

        const response = await ChatService.sendMessage(userId, message);

        res.status(200).json({
            data: { response },
        } as Result);
    } catch (error) {
        console.error('Error sending message:', error);
        res.status(500).json({
            message: 'Internal server error',
        });
    }
};

/**
 * Clear chat history for a user
 */
export const clearChatHistory = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const userId = req.query.userId as string;

        if (!userId) {
            res.status(400).json({
                message: 'User ID is required',
            });
            return;
        }

        await ChatService.clearChatHistory(userId);

        res.status(200).json({
            message: 'Chat history cleared successfully',
        });
    } catch (error) {
        console.error('Error clearing chat history:', error);
        res.status(500).json({
            message: 'Internal server error',
        });
    }
};
