import { v4 as uuidv4 } from 'uuid';
import RAGService from './RAGService';
import { ChatOpenAI } from '@langchain/openai';
import {
    HumanMessage,
    AIMessage,
    SystemMessage,
} from '@langchain/core/messages';
import { Document } from '@langchain/core/documents';
import ChatSession, { IChatSession } from '../db/mongo/models/Chat';

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
export interface ChatSessionData {
    sessionId: string;
    userId?: string;
    documentId?: string;
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
 * Includes MongoDB persistence for chat history
 */
class ChatService {
    private sessions: Map<string, ChatSessionData>;
    private llm: ChatOpenAI;
    private ragService: RAGService;

    constructor() {
        this.sessions = new Map<string, ChatSessionData>();
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
     * Stores the session in both memory and MongoDB
     */
    public async createSession(
        userId?: string,
        metadata?: Record<string, any>
    ): Promise<ChatSessionData> {
        try {
            const sessionId = uuidv4();
            const now = new Date();
            
            // Extract documentId from metadata if present
            const documentId = metadata?.documentId;

            // Create initial system message
            const systemMessage: ChatMessage = {
                role: 'system',
                content:
                    'You are a helpful assistant that provides accurate information based on the context provided.',
                timestamp: now,
            };

            // Create session object
            const session: ChatSessionData = {
                sessionId,
                userId,
                documentId,
                messages: [systemMessage],
                createdAt: now,
                updatedAt: now,
                metadata,
            };

            // Store in memory
            this.sessions.set(sessionId, session);

            // Store in MongoDB if user ID is provided
            if (userId) {
                await ChatSession.create({
                    sessionId,
                    googleId: userId,
                    documentId,
                    messages: [systemMessage],
                    createdAt: now,
                    updatedAt: now,
                    metadata,
                });
                console.log(`Chat session ${sessionId} created and stored in MongoDB`);
            }

            return session;
        } catch (error) {
            console.error('Error creating chat session:', error);
            throw new ChatServiceError('Failed to create chat session');
        }
    }

    /**
     * Loads chat sessions for a user from MongoDB
     * Can filter by documentId if provided
     */
    public async loadUserSessions(
        userId: string,
        documentId?: string
    ): Promise<ChatSessionData[]> {
        try {
            // Build query based on parameters
            const query: any = { googleId: userId };
            if (documentId) {
                query.documentId = documentId;
            }

            // Fetch sessions from MongoDB
            const sessions = await ChatSession.find(query).sort({ updatedAt: -1 });
            
            // Convert to ChatSessionData format and cache in memory
            sessions.forEach(session => {
                const sessionData: ChatSessionData = {
                    sessionId: session.sessionId,
                    userId: session.googleId,
                    documentId: session.documentId,
                    messages: session.messages.map(msg => ({
                        role: msg.role,
                        content: msg.content,
                        timestamp: msg.timestamp
                    })),
                    createdAt: session.createdAt,
                    updatedAt: session.updatedAt,
                    metadata: session.metadata
                };
                this.sessions.set(session.sessionId, sessionData);
            });

            return sessions.map(session => ({
                sessionId: session.sessionId,
                userId: session.googleId,
                documentId: session.documentId,
                messages: session.messages.map(msg => ({
                    role: msg.role,
                    content: msg.content,
                    timestamp: msg.timestamp
                })),
                createdAt: session.createdAt,
                updatedAt: session.updatedAt,
                metadata: session.metadata
            }));
        } catch (error) {
            console.error('Error loading user chat sessions:', error);
            throw new ChatServiceError('Failed to load chat sessions');
        }
    }

    /**
     * Receives a message from the user and generates a response
     * Updates both in-memory session and MongoDB record
     */
    public async receiveMessage(
        sessionId: string,
        message: string
    ): Promise<string> {
        try {
            const session = this.sessions.get(sessionId);
            if (!session) {
                // Try to load from MongoDB
                const dbSession = await ChatSession.findOne({ sessionId });
                if (dbSession) {
                    // Convert to ChatSessionData and cache
                    this.sessions.set(sessionId, {
                        sessionId: dbSession.sessionId,
                        userId: dbSession.googleId,
                        documentId: dbSession.documentId,
                        messages: dbSession.messages.map(msg => ({
                            role: msg.role,
                            content: msg.content,
                            timestamp: msg.timestamp
                        })),
                        createdAt: dbSession.createdAt,
                        updatedAt: dbSession.updatedAt,
                        metadata: dbSession.metadata
                    });
                } else {
                    throw new ChatServiceError('Chat session not found');
                }
            }

            // Add user message to session
            const userMessage: ChatMessage = {
                role: 'user',
                content: message,
                timestamp: new Date(),
            };
            if (session) {
                session.messages.push(userMessage);
                session.updatedAt = new Date();
            }
            // Get the last user message for context retrieval
            const lastUserMessage = session?.messages
                .filter((msg) => msg.role === 'user')
                .pop();

            if (!lastUserMessage) {
                throw new ChatServiceError('No user messages found in session');
            }

            // Fetch relevant documents using RAG with userId and optional documentId
            const relevantDocs = await this.ragService.fetchRelevantDocumentsFromQuery(
                lastUserMessage.content,
                session?.userId || 'anonymous'
            );

            // Generate response using LLM
            const response = await this.generateResponse(
                session?.messages || [],
                relevantDocs
            );

            // Add assistant response to session
            const assistantMessage: ChatMessage = {
                role: 'assistant',
                content: response,
                timestamp: new Date(),
            };
            
            if (session) {
                session.messages.push(assistantMessage);
                session.updatedAt = new Date();
                
                // Update session in memory
                this.sessions.set(sessionId, session);
            }

            // Update in MongoDB if userId exists
            if (session?.userId) {
                await ChatSession.findOneAndUpdate(
                    { sessionId },
                    { 
                        $push: { 
                            messages: { 
                                $each: [userMessage, assistantMessage] 
                            }
                        },
                        $set: { 
                            updatedAt: session.updatedAt 
                        }
                    }
                );
            }

            return response;
        } catch (error) {
            console.error('Error processing message:', error);
            throw new ChatServiceError('Failed to process message');
        }
    }

    /**
     * Generates a response using the LLM with context from documents
     */
    private async generateResponse(
        messages: ChatMessage[],
        contextDocuments: Document[]
    ): Promise<string> {
        try {
            // Convert chat messages to LangChain format
            const langChainMessages = messages.map((msg) => {
                switch (msg.role) {
                    case 'user':
                        return new HumanMessage(msg.content);
                    case 'assistant':
                        return new AIMessage(msg.content);
                    case 'system':
                        return new SystemMessage(msg.content);
                    default:
                        return new HumanMessage(msg.content);
                }
            });

            // Add context from documents if available
            if (contextDocuments.length > 0) {
                const context = this.constructContext(contextDocuments);
                langChainMessages.push(
                    new SystemMessage(
                        `Additional context information:\n${context}\n\nUse this context to help answer the user's question if relevant.`
                    )
                );
            }

            // Generate response
            const response = await this.llm.call(langChainMessages);

            return response.content as string;
        } catch (error) {
            console.error('Error generating response:', error);
            throw new ChatServiceError('Failed to generate response');
        }
    }

    /**
     * Constructs context string from documents
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
     * Tries memory first, then MongoDB
     */
    public async getSession(sessionId: string): Promise<ChatSessionData | null> {
        // Check in-memory cache first
        const session = this.sessions.get(sessionId);
        if (session) {
            return session;
        }

        // If not in memory, try to load from MongoDB
        try {
            const dbSession = await ChatSession.findOne({ sessionId });
            if (dbSession) {
                // Convert to ChatSessionData and cache in memory
                const sessionData: ChatSessionData = {
                    sessionId: dbSession.sessionId,
                    userId: dbSession.googleId,
                    documentId: dbSession.documentId,
                    messages: dbSession.messages.map(msg => ({
                        role: msg.role,
                        content: msg.content,
                        timestamp: msg.timestamp
                    })),
                    createdAt: dbSession.createdAt,
                    updatedAt: dbSession.updatedAt,
                    metadata: dbSession.metadata
                };
                this.sessions.set(sessionId, sessionData);
                return sessionData;
            }
        } catch (error) {
            console.error('Error fetching chat session from MongoDB:', error);
        }

        return null;
    }

    /**
     * Gets all messages for a chat session
     */
    public async getMessages(sessionId: string): Promise<ChatMessage[] | null> {
        const session = await this.getSession(sessionId);
        return session ? session.messages : null;
    }

    /**
     * Deletes a chat session
     * Removes from both memory and MongoDB
     */
    public async deleteSession(sessionId: string): Promise<boolean> {
        try {
            // Remove from memory
            const memoryResult = this.sessions.delete(sessionId);
            
            // Remove from MongoDB
            const dbResult = await ChatSession.deleteOne({ sessionId });
            
            return memoryResult || dbResult.deletedCount > 0;
        } catch (error) {
            console.error('Error deleting chat session:', error);
            throw new ChatServiceError('Failed to delete chat session');
        }
    }
}

export default new ChatService();
