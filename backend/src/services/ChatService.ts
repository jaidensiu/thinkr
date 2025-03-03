import { v4 as uuidv4 } from 'uuid';
import ChatSession from '../db/mongo/models/Chat';
import { ChatMessage, ChatSessionDTO } from '../interfaces';
import { ChatOpenAI } from '@langchain/openai';
import RAGService from './RAGService';

class ChatService {
    private llm: ChatOpenAI;
    private ragService: RAGService;

    constructor() {
        this.llm = new ChatOpenAI({
            openAIApiKey: process.env.OPENAI_API_KEY,
            temperature: 0.7,
        });
        this.ragService = new RAGService({
            openAIApiKey: process.env.OPENAI_API_KEY!,
            vectorStoreUrl: process.env.VECTOR_STORE_URL!,
        });
    }

    /**
     * Get or create a chat session for a user
     */
    public async getOrCreateUserChat(userId: string): Promise<ChatSessionDTO> {
        // Try to find an existing chat session for the user
        let session = await ChatSession.findOne({ googleId: userId });

        // If no session exists, create a new one
        if (!session) {
            const systemMessage: ChatMessage = {
                role: 'system',
                content: 'You are a helpful assistant that provides accurate information based on the context provided.',
                timestamp: new Date().toISOString(),
            };

            session = await ChatSession.create({
                sessionId: uuidv4(),
                googleId: userId,
                messages: [systemMessage],
                metadata: { type: 'general' }
            });
        }

        return this.formatChatSession(session);
    }

    /**
     * Send a message to the user's chat and get a response
     */
    public async sendMessage(userId: string, message: string): Promise<ChatMessage> {
        // Get the user's chat session
        const session = await this.getOrCreateUserChat(userId);

        // Add the user message to the session
        const userMessage: ChatMessage = {
            role: 'user',
            content: message,
            timestamp: new Date().toISOString(),
        };
        // Get context from user's documents using RAG
        const context = await this.ragService.getRelevantContext(message, userId);

        // Generate response using the context and chat history
        const recentMessages = session.messages.slice(-5); // Use last 5 messages for context
        // Prepare prompt with context
        let prompt = `Based on the following information:\n\n${context}\n\nAnd considering our conversation so far, please respond to: ${message}`;
        
        // Get AI response
        const aiResponse = await this.llm.invoke(prompt);
        
        // Create response message
        const responseMessage: ChatMessage = {
            role: 'assistant',
            content: typeof aiResponse.content === 'string' 
                ? aiResponse.content 
                : JSON.stringify(aiResponse.content),
            timestamp: new Date().toISOString(),
        };

        // Update the session with both messages
        await ChatSession.findOneAndUpdate(
            { googleId: userId },
            { 
                $push: { 
                    messages: { 
                        $each: [userMessage, responseMessage] 
                    } 
                },
                $set: { updatedAt: new Date() }
            }
        );

        return responseMessage;
    }

    /**
     * Clear chat history for a user
     */
    public async clearChatHistory(userId: string): Promise<void> {
        const systemMessage: ChatMessage = {
            role: 'system',
            content: 'You are a helpful assistant that provides accurate information based on the context provided.',
            timestamp: new Date().toISOString(),
        };

        await ChatSession.findOneAndUpdate(
            { googleId: userId },
            { 
                $set: { 
                    messages: [systemMessage],
                    updatedAt: new Date()
                }
            },
            { upsert: true }
        );
    }

    /**
     * Format a chat session for the API response
     */
    private formatChatSession(session: any): ChatSessionDTO {
        return {
            userId: session.userId,
            messages: session.messages.map((msg: any) => ({
                role: msg.role,
                content: msg.content,
                timestamp: msg.timestamp
            })),
            createdAt: session.createdAt,
            updatedAt: session.updatedAt,
            metadata: session.metadata
        };
    }
}

export default new ChatService();
