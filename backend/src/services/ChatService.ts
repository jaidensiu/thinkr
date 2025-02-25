import { v4 as uuidv4 } from 'uuid';
import RAGService from './RAGService';
import { ChatOpenAI } from '@langchain/openai';
import {
    HumanMessage,
    AIMessage,
    SystemMessage,
} from '@langchain/core/messages';
import { Document } from '@langchain/core/documents';

/**
 * Represents a chat message
 */
interface ChatMessage {
    role: 'user' | 'assistant' | 'system';
    content: string;
    timestamp: Date;
}

/**
 * Represents a chat session
 */
export interface ChatSession {
    sessionId: string;
    userId?: string;
    messages: ChatMessage[];
    createdAt: Date;
    updatedAt: Date;
    metadata?: Record<string, any>;
}

/**
 * Custom error class for Chat-related errors
 */
class ChatServiceError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'ChatServiceError';
    }
}

/**
 * Service for managing chat sessions and interactions
 */
class ChatService {
    private sessions: Map<string, ChatSession>;
    private llm: ChatOpenAI;
    private ragService: RAGService;

    constructor() {
        this.sessions = new Map<string, ChatSession>();
        this.llm = new ChatOpenAI({
            openAIApiKey: process.env.OPENAI_API_KEY!,
            temperature: 0.7,
            streaming: false,
        });
        this.ragService = new RAGService({
            openAIApiKey: process.env.OPENAI_API_KEY!,
            vectorStoreUrl: process.env.VECTOR_STORE_URL!,
        });
    }

    /**
     * Creates a new chat session
     * @param userId Optional user ID to associate with the session
     * @param metadata Optional metadata for the session
     * @returns The newly created chat session
     */
    public async createChat(
        userId?: string,
        metadata?: Record<string, any>
    ): Promise<ChatSession> {
        try {
            const sessionId = uuidv4();
            const now = new Date();

            // Initialize with a system message
            const systemMessage: ChatMessage = {
                role: 'system',
                content:
                    'You are a helpful assistant that provides accurate information based on the context provided.',
                timestamp: now,
            };

            const session: ChatSession = {
                sessionId,
                userId,
                messages: [systemMessage],
                createdAt: now,
                updatedAt: now,
                metadata,
            };

            this.sessions.set(sessionId, session);
            console.log(`Created new chat session: ${sessionId}`);

            return session;
        } catch (error) {
            console.error('Error creating chat session:', error);
            throw new ChatServiceError('Failed to create chat session');
        }
    }

    /**
     * Sends a user message to the specified chat session
     * @param sessionId The ID of the chat session
     * @param message The user's message
     * @throws ChatServiceError if the session doesn't exist
     */
    public async sendMessage(
        sessionId: string,
        message: string
    ): Promise<void> {
        try {
            const session = this.sessions.get(sessionId);

            if (!session) {
                throw new ChatServiceError(
                    `Chat session not found: ${sessionId}`
                );
            }

            // Add user message to the session
            const userMessage: ChatMessage = {
                role: 'user',
                content: message,
                timestamp: new Date(),
            };

            session.messages.push(userMessage);
            session.updatedAt = new Date();

            console.log(`Added user message to session ${sessionId}`);
        } catch (error) {
            console.error(
                `Error sending message to session ${sessionId}:`,
                error
            );
            if (error instanceof ChatServiceError) {
                throw error;
            }
            throw new ChatServiceError('Failed to send message');
        }
    }

    /**
     * Retrieves a response from the AI for the specified chat session
     * @param sessionId The ID of the chat session
     * @returns The AI's response message
     * @throws ChatServiceError if the session doesn't exist or if there's an error generating a response
     */
    public async receiveMessage(sessionId: string): Promise<string> {
        try {
            const session = this.sessions.get(sessionId);

            if (!session) {
                throw new ChatServiceError(
                    `Chat session not found: ${sessionId}`
                );
            }

            // Get the last user message
            const lastUserMessageIndex = [...session.messages]
                .reverse()
                .findIndex((msg) => msg.role === 'user');

            if (lastUserMessageIndex === -1) {
                throw new ChatServiceError('No user message found in session');
            }

            const lastUserMessage =
                session.messages[
                    session.messages.length - 1 - lastUserMessageIndex
                ];

            // Fetch relevant documents using RAG
            const relevantDocs =
                await this.ragService.fetchRelevantDocumentsFromQuery(
                    lastUserMessage.content,
                    'documents'
                );

            // Generate response using chat history and RAG context
            const response = await this.generateResponse(session, relevantDocs);

            // Add assistant message to the session
            const assistantMessage: ChatMessage = {
                role: 'assistant',
                content: response,
                timestamp: new Date(),
            };

            session.messages.push(assistantMessage);
            session.updatedAt = new Date();

            console.log(`Added assistant response to session ${sessionId}`);

            return response;
        } catch (error) {
            console.error(
                `Error receiving message for session ${sessionId}:`,
                error
            );
            if (error instanceof ChatServiceError) {
                throw error;
            }
            throw new ChatServiceError('Failed to generate response');
        }
    }

    /**
     * Generates a response using the chat history and relevant documents
     * @param session The chat session
     * @param relevantDocs Relevant documents for context
     * @returns The generated response
     */
    private async generateResponse(
        session: ChatSession,
        relevantDocs: Document[]
    ): Promise<string> {
        try {
            // Convert session messages to LangChain message format
            const messages = session.messages.map((msg) => {
                if (msg.role === 'system') {
                    return new SystemMessage(msg.content);
                } else if (msg.role === 'user') {
                    return new HumanMessage(msg.content);
                } else {
                    return new AIMessage(msg.content);
                }
            });

            // If we have relevant documents, add them as context
            if (relevantDocs.length > 0) {
                const context = this.constructContext(relevantDocs);

                // Add context as a system message
                messages.push(
                    new SystemMessage(
                        `Additional context information:\n${context}\n\nUse this information to help answer the user's question.`
                    )
                );
            }

            // Generate response
            const response = await this.llm.call(messages);

            return response.content as string;
        } catch (error) {
            console.error('Error generating response:', error);
            throw new ChatServiceError('Failed to generate response');
        }
    }

    /**
     * Constructs context string from documents
     * @param documents The documents to use as context
     * @returns A formatted context string
     */
    private constructContext(documents: Document[]): string {
        let context = '';

        for (const doc of documents) {
            // Simple token estimation
            const estimatedTokens = doc.pageContent.length / 4;

            if (context.length / 4 + estimatedTokens > 4000) {
                break;
            }

            context += `${doc.pageContent}\n\n`;
        }

        return context.trim();
    }

    /**
     * Gets a chat session by ID
     * @param sessionId The ID of the chat session
     * @returns The chat session or null if not found
     */
    public getSession(sessionId: string): ChatSession | null {
        return this.sessions.get(sessionId) || null;
    }

    /**
     * Gets all messages for a chat session
     * @param sessionId The ID of the chat session
     * @returns Array of chat messages or null if session not found
     */
    public getMessages(sessionId: string): ChatMessage[] | null {
        const session = this.sessions.get(sessionId);
        return session ? session.messages : null;
    }

    /**
     * Deletes a chat session
     * @param sessionId The ID of the chat session to delete
     * @returns True if the session was deleted, false if it wasn't found
     */
    public deleteSession(sessionId: string): boolean {
        return this.sessions.delete(sessionId);
    }
}

export default new ChatService();
