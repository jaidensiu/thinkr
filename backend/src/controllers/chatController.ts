import { Request, Response } from 'express';
import ChatService from '../services/ChatService';
import { Result } from '../interfaces';

/**
 * Creates a new chat session
 */
export const createChatSession = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { userId, metadata } = req.body;

        if (!userId) {
            res.status(400).json({
                message: 'userId is required',
            } as Result);
            return;
        }

        const session = await ChatService.createSession(userId, metadata);

        res.status(200).json({
            data: { session },
        } as Result);
    } catch (error) {
        console.error('Error creating chat session:', error);
        res.status(500).json({
            message: 'Failed to create chat session',
        } as Result);
    }
};

/**
 * Gets a chat session by ID
 */
export const getChatSession = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { sessionId } = req.params;

        if (!sessionId) {
            res.status(400).json({
                message: 'Session ID is required',
            } as Result);
            return;
        }

        const session = await ChatService.getSession(sessionId);

        if (!session) {
            res.status(404).json({
                message: 'Chat session not found',
            } as Result);
            return;
        }

        res.status(200).json({
            data: { session },
        } as Result);
    } catch (error) {
        console.error('Error getting chat session:', error);
        res.status(500).json({
            message: 'Failed to get chat session',
        } as Result);
    }
};

/**
 * Gets all chat sessions for a user
 * Can filter by documentId if provided
 */
export const getUserChatSessions = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { userId, documentId } = req.body;

        if (!userId) {
            res.status(400).json({
                message: 'userId is required',
            } as Result);
            return;
        }

        const sessions = await ChatService.loadUserSessions(userId, documentId);

        res.status(200).json({
            data: { sessions },
        } as Result);
    } catch (error) {
        console.error('Error getting user chat sessions:', error);
        res.status(500).json({
            message: 'Failed to get user chat sessions',
        } as Result);
    }
};

/**
 * Sends a message to a chat session and gets a response
 */
export const sendChatMessage = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { sessionId } = req.params;
        const { message } = req.body;

        if (!sessionId || !message) {
            res.status(400).json({
                message: 'Session ID and message are required',
            } as Result);
            return;
        }

        const response = await ChatService.receiveMessage(sessionId, message);

        res.status(200).json({
            data: { response },
        } as Result);
    } catch (error) {
        console.error('Error sending chat message:', error);
        res.status(500).json({
            message: 'Failed to send chat message',
        } as Result);
    }
};

/**
 * Deletes a chat session
 */
export const deleteChatSession = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { sessionId } = req.params;

        if (!sessionId) {
            res.status(400).json({
                message: 'Session ID is required',
            } as Result);
            return;
        }

        const success = await ChatService.deleteSession(sessionId);

        if (!success) {
            res.status(404).json({
                message: 'Chat session not found',
            } as Result);
            return;
        }

        res.status(200).json({
            message: 'Chat session deleted successfully',
        } as Result);
    } catch (error) {
        console.error('Error deleting chat session:', error);
        res.status(500).json({
            message: 'Failed to delete chat session',
        } as Result);
    }
};
