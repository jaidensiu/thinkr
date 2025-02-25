import { Request, Response } from 'express';
import ChatService from '../services/ChatService';
import { Result } from '../interfaces';

/**
 * Creates a new chat session
 */
export const createChatSession = async (req: Request, res: Response): Promise<void> => {
    try {
        const { userId, metadata } = req.body;
        
        const session = await ChatService.createChat(userId, metadata);
        
        res.status(201).json({
            data: { session }
        } as Result);
    } catch (error) {
        console.error('Error creating chat session:', error);
        res.status(500).json({
            message: 'Failed to create chat session'
        } as Result);
    }
};

/**
 * Sends a message to a chat session
 */
export const sendChatMessage = async (req: Request, res: Response): Promise<void> => {
    try {
        const { sessionId } = req.params;
        const { message } = req.body;
        
        if (!message) {
            res.status(400).json({
                message: 'Message is required'
            } as Result);
            return;
        }
        
        await ChatService.sendMessage(sessionId, message);
        
        // Generate and return the AI response
        const response = await ChatService.receiveMessage(sessionId);
        
        res.status(200).json({
            data: { response }
        } as Result);
    } catch (error) {
        console.error('Error processing chat message:', error);
        
        if (error instanceof Error && error.name === 'ChatServiceError' && error.message.includes('not found')) {
            res.status(404).json({
                message: error.message
            } as Result);
            return;
        }
        
        res.status(500).json({
            message: 'Failed to process chat message'
        } as Result);
    }
};

/**
 * Gets chat session details
 */
export const getChatSession = async (req: Request, res: Response): Promise<void> => {
    try {
        const { sessionId } = req.params;
        
        const session = ChatService.getSession(sessionId);
        
        if (!session) {
            res.status(404).json({
                message: `Chat session not found: ${sessionId}`
            } as Result);
            return;
        }
        
        res.status(200).json({
            data: { session }
        } as Result);
    } catch (error) {
        console.error('Error retrieving chat session:', error);
        res.status(500).json({
            message: 'Failed to retrieve chat session'
        } as Result);
    }
};

/**
 * Deletes a chat session
 */
export const deleteChatSession = async (req: Request, res: Response): Promise<void> => {
    try {
        const { sessionId } = req.params;
        
        const deleted = ChatService.deleteSession(sessionId);
        
        if (!deleted) {
            res.status(404).json({
                message: `Chat session not found: ${sessionId}`
            } as Result);
            return;
        }
        
        res.status(200).json({
            message: 'Chat session deleted successfully'
        } as Result);
    } catch (error) {
        console.error('Error deleting chat session:', error);
        res.status(500).json({
            message: 'Failed to delete chat session'
        } as Result);
    }
}; 