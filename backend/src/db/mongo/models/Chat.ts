import mongoose, { Schema, Document } from 'mongoose';

/**
 * Interface for a chat message document in MongoDB
 * Represents an individual message in a chat session
 */
export interface IMessage extends Document {
    role: 'user' | 'assistant' | 'system';
    content: string;
    timestamp: Date;
}

/**
 * Interface for a chat session document in MongoDB
 * Represents a complete chat session with multiple messages
 */
export interface IChatSession extends Document {
    sessionId: string; // Unique identifier for the chat session
    googleId: string; // User identifier (matches the user's Google ID)
    documentId?: string; // Optional: specific document this chat is about
    messages: IMessage[]; // Array of messages in this chat session
    createdAt: Date; // When the chat session was created
    updatedAt: Date; // When the chat session was last updated
    metadata?: Record<string, any>; // Additional metadata about the chat
}

/**
 * Schema for individual chat messages
 * Embedded within the chat session document
 */
const MessageSchema = new Schema<IMessage>({
    role: {
        type: String,
        enum: ['user', 'assistant', 'system'],
        required: true,
    },
    content: {
        type: String,
        required: true,
    },
    timestamp: {
        type: Date,
        default: Date.now,
    },
});

/**
 * Schema for chat sessions
 * Each session contains multiple messages and is associated with a user
 */
const ChatSessionSchema = new Schema<IChatSession>(
    {
        sessionId: {
            type: String,
            required: true,
            unique: true,
        },
        googleId: {
            type: String,
            required: true,
            index: true, // Index for faster queries by user ID
        },
        documentId: {
            type: String,
            index: true, // Index for faster queries by document ID
        },
        messages: [MessageSchema], // Embedded array of messages
        createdAt: {
            type: Date,
            default: Date.now,
        },
        updatedAt: {
            type: Date,
            default: Date.now,
        },
        metadata: {
            type: Schema.Types.Mixed, // Flexible schema for additional metadata
        },
    },
    {
        // Automatically update the updatedAt field when the document is modified
        timestamps: true,
    }
);

// Create compound index for efficient queries by user and document
ChatSessionSchema.index({ googleId: 1, documentId: 1 });

// Create model from schema
const ChatSession = mongoose.model<IChatSession>(
    'ChatSession',
    ChatSessionSchema
);

export default ChatSession;
