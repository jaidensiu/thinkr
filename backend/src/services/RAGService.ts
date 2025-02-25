import { OpenAIEmbeddings } from '@langchain/openai';
import { Document } from '@langchain/core/documents';
import { ChatOpenAI } from '@langchain/openai';
import { Chroma } from '@langchain/community/vectorstores/chroma';
import { SystemMessage, HumanMessage } from '@langchain/core/messages';
import { ChromaClient } from 'chromadb';

/**
 * Represents configuration options for RAGService
 */
interface RAGServiceConfig {
    openAIApiKey: string;
    vectorStoreUrl: string;
    maxContextLength?: number;
    temperature?: number;
}

/**
 * Interface for document metadata
 */
interface DocumentMetadata {
    relevanceScore?: number;
    [key: string]: any;
}

/**
 * Custom error class for RAG-related errors
 */
class RAGServiceError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'RAGServiceError';
    }
}

/**
 * Service class for Retrieval Augmented Generation (RAG)
 * Handles document retrieval and LLM querying with context
 */
class RAGService {
    private embeddings: OpenAIEmbeddings;
    private llm: ChatOpenAI;
    private vectorStore: Chroma | null = null;
    private maxContextLength: number;
    private vectorStoreUrl: string;

    constructor(config: RAGServiceConfig) {
        this.embeddings = new OpenAIEmbeddings({
            openAIApiKey: config.openAIApiKey,
        });

        this.llm = new ChatOpenAI({
            openAIApiKey: config.openAIApiKey,
            temperature: config.temperature || 0.7,
        });

        this.maxContextLength = config.maxContextLength || 4000;
        this.vectorStoreUrl = config.vectorStoreUrl;
    }

    /**
     * Initialize connection to vector store
     */
    private async initVectorStore(collectionName: string): Promise<void> {
        try {
            console.log(`Attempting to connect to ChromaDB at ${this.vectorStoreUrl}`);
            this.vectorStore = await Chroma.fromExistingCollection(
                this.embeddings,
                {
                    collectionName: collectionName,
                    url: this.vectorStoreUrl,
                }
            );
            console.log(`Successfully connected to ChromaDB collection: ${collectionName}`);
        } catch (error: unknown) {
            console.error('Vector store initialization error:', error);
            
            try {
                console.log(`Attempting to create new collection: ${collectionName}`);
                this.vectorStore = await Chroma.fromTexts(
                    ['Initial document for testing RAG capabilities.'],
                    { source: 'initialization' },
                    this.embeddings,
                    {
                        collectionName: collectionName,
                        url: this.vectorStoreUrl,
                    }
                );
                console.log(`Successfully created new ChromaDB collection: ${collectionName}`);
            } catch (innerError) {
                console.error('Failed to create ChromaDB collection:', innerError);
                throw new RAGServiceError(`ChromaDB connection failed. Please ensure ChromaDB is running at ${this.vectorStoreUrl}`);
            }
        }
    }

    /**
     * Ensure vector store is initialized before use
     */
    public async ensureVectorStore(collectionName: string): Promise<void> {
        if (!this.vectorStore) {
            await this.initVectorStore(collectionName);
        }
    }

    /**
     * Insert a document into a vector db collection
     */
    public async insertDocument(
        collectionName: string,
        text: string,
        key: string
    ): Promise<string> {
        try {
            const docs = [
                { pageContent: text, metadata: { source: collectionName } },
            ];
            const id = {
                ids: [key],
            };
            await this.vectorStore!.addDocuments(docs, id);

            console.log(
                `Text uploaded to Chroma DB in collection ${collectionName}: ${key}`
            );
            return key;
        } catch (error) {
            console.error('Error uploading text to Chroma DB:', error);
            throw new Error('Failed to upload text to Chroma DB');
        }
    }

    /**
     * Deletes documents in a vector DB collection
     */
    public async deleteDocuments(
        ids: string[],
        collectionName: string
    ): Promise<void> {
        try {
            await this.ensureVectorStore(collectionName);
            await this.vectorStore!.collection!.delete({ ids });

            console.log(
                `Deleted embeddings in Chroma DB collection ${collectionName}: ${ids}`
            );
            return;
        } catch (error) {
            console.error('Error deleting embeddings to Chroma DB:', error);
            throw new Error('Failed to delete embeddings in Chroma DB');
        }
    }

    /**
     * Fetch documents from vectorDB collection based on the embeddingIDs provided
     */
    public async fetchDocumentsFromVectorDB(
        embeddingIds: string[],
        collectionName: string
    ): Promise<string[]> {
        try {
            await this.ensureVectorStore(collectionName);

            const results = await this.vectorStore!.collection!.get({
                ids: embeddingIds,
            });

            const documents = results.documents as string[];
            return documents;
        } catch (error) {
            console.error('Error fetching documents from ChromaDB:', error);
            throw new Error('Failed to fetch documents from ChromaDB');
        }
    }

    /**
     * Fetch relevant documents based on query similarity
     */
    public async fetchRelevantDocumentsFromQuery(
        query: string,
        collectionName: string
    ): Promise<Document<DocumentMetadata>[]> {
        try {
            if (!query.trim()) {
                throw new RAGServiceError('Query cannot be empty');
            }

            await this.ensureVectorStore(collectionName);

            if (!this.vectorStore) {
                throw new RAGServiceError('Vector store not initialized');
            }

            const documents = await this.vectorStore.similaritySearch(query, 5);
            return documents;
        } catch (error: unknown) {
            if (error instanceof Error) {
                throw new RAGServiceError(
                    `Error fetching relevant documents: ${error.message}`
                );
            }
            throw new RAGServiceError(
                'Error fetching relevant documents: Unknown error'
            );
        }
    }

    /**
     * Query LLM with context-aware prompting
     */
    public async queryLLM(
        query: string,
        contextDocuments: Document<DocumentMetadata>[]
    ): Promise<string> {
        try {
            if (!query.trim()) {
                throw new RAGServiceError('Query cannot be empty');
            }

            const context = this.constructContext(contextDocuments);

            // Create messages for the chat model
            const messages = [
                new SystemMessage(
                    'You are a helpful assistant that answers questions based on the provided context.'
                ),
                new HumanMessage(`Context: ${context}\n\nQuestion: ${query}`),
            ];

            // Generate response
            const response = await this.llm.call(messages);

            return response.content as string;
        } catch (error: unknown) {
            if (error instanceof Error) {
                throw new RAGServiceError(
                    `Error querying LLM: ${error.message}`
                );
            }
            throw new RAGServiceError('Error querying LLM: Unknown error');
        }
    }

    /**
     * Construct context string from documents while respecting token limit
     */
    private constructContext(documents: Document<DocumentMetadata>[]): string {
        let context = '';

        for (const doc of documents) {
            // Simple token estimation (can be replaced with more accurate counting)
            const estimatedTokens = doc.pageContent.length / 4;

            if (context.length / 4 + estimatedTokens > this.maxContextLength) {
                break;
            }

            context += `${doc.pageContent}\n\n`;
        }

        return context.trim();
    }

    /**
     * Create composite prompt combining query and context
     */
    private createPrompt(query: string, context: string): string {
        return `
Context information is below:
--------------------
${context}
--------------------

Given the context information and no other information, answer the following query:
Query: ${query}

Answer:`;
    }
}

export default RAGService;
