import { FlashCardDTO, QuizDTO } from '../interfaces';
import RAGService from './RAGService';
import FlashcardSet from '../db/mongo/models/FlashcardSet';
import QuizSet from '../db/mongo/models/QuizSet';
import Document from '../db/mongo/models/Document';

class SuggestedMaterialsService {
    private ragService: RAGService;

    constructor() {
        this.ragService = new RAGService({
            openAIApiKey: process.env.OPENAI_API_KEY!,
            vectorStoreUrl: process.env.VECTOR_STORE_URL!,
        });
    }

    /**
     * Get suggested flashcards and quizzes for a user based on their documents
     * @param userId The user ID to get suggestions for
     * @param limit Maximum number of suggestions to return
     */
    public async getSuggestedMaterials(
        userId: string,
        limit: number = 5
    ): Promise<{
        flashcards: FlashCardDTO[];
        quizzes: QuizDTO[];
    }> {
        try {
            // 1. Get all documents for the user
            const userDocuments = await Document.find({ userId });
            
            if (userDocuments.length === 0) {
                return { flashcards: [], quizzes: [] };
            }

            // 2. Find similar documents from other users
            const similarDocumentIds = await this.findSimilarDocuments(
                userId,
                userDocuments.map(doc => doc.documentId),
                limit
            );

            if (similarDocumentIds.length === 0) {
                return { flashcards: [], quizzes: [] };
            }

            // 3. Fetch flashcards and quizzes for similar documents
            const [flashcards, quizzes] = await Promise.all([
                this.fetchFlashcardsForDocuments(similarDocumentIds),
                this.fetchQuizzesForDocuments(similarDocumentIds)
            ]);

            return { flashcards, quizzes };
        } catch (error) {
            console.error('Error getting suggested materials:', error);
            throw new Error('Failed to get suggested materials');
        }
    }

    /**
     * Find documents from other users that are similar to the user's documents
     */
    private async findSimilarDocuments(
        userId: string,
        userDocumentIds: string[],
        limit: number
    ): Promise<Array<{ documentId: string, otherUserId: string }>> {
        // Initialize results array
        const similarityResults: Array<{
            documentId: string;
            otherUserId: string;
            similarityScore: number;
        }> = [];

        // Get all documents from other users
        const otherUsersDocuments = await Document.find({
            userId: { $ne: userId }
        });

        if (otherUsersDocuments.length === 0) {
            return [];
        }

        // For each of the user's documents, find similar documents
        for (const docId of userDocumentIds) {
            try {
                // Get the document text from ChromaDB
                const userDocText = await this.ragService.fetchDocumentsFromVectorDB(
                    docId,
                    `user_${userId}`
                );

                if (userDocText.length === 0) continue;

                // Combine all chunks into one text for comparison
                const combinedUserDocText = userDocText.join(' ');

                // For each document from other users, calculate similarity
                for (const otherDoc of otherUsersDocuments) {
                    try {
                        // Get the other document's text from ChromaDB
                        const otherDocText = await this.ragService.fetchDocumentsFromVectorDB(
                            otherDoc.documentId,
                            `user_${otherDoc.userId}`
                        );

                        if (otherDocText.length === 0) continue;

                        // Combine all chunks into one text
                        const combinedOtherDocText = otherDocText.join(' ');

                        // Use direct embedding comparison instead of ChromaDB for similarity
                        const similarityScore = await this.calculateSimilarity(
                            combinedUserDocText,
                            combinedOtherDocText
                        );

                        similarityResults.push({
                            documentId: otherDoc.documentId,
                            otherUserId: otherDoc.userId,
                            similarityScore
                        });
                    } catch (error) {
                        console.error(`Error processing document ${otherDoc.documentId}:`, error);
                        // Continue with other documents
                    }
                }
            } catch (error) {
                console.error(`Error processing user document ${docId}:`, error);
                // Continue with other user documents
            }
        }

        // Sort by similarity score (highest first) and take the top 'limit' results
        const topSimilarDocuments = similarityResults
            .sort((a, b) => b.similarityScore - a.similarityScore)
            .slice(0, limit)
            .map(result => ({
                documentId: result.documentId,
                otherUserId: result.otherUserId
            }));

        return topSimilarDocuments;
    }

    /**
     * Calculate similarity between two text documents
     * This is a simplified approach using cosine similarity of embeddings
     */
    private async calculateSimilarity(text1: string, text2: string): Promise<number> {
        try {
            // Skip ChromaDB and just use OpenAI embeddings directly
            const embeddings = new (await import('@langchain/openai')).OpenAIEmbeddings({
                openAIApiKey: process.env.OPENAI_API_KEY!,
            });
            
            // Get embeddings for both texts
            const [embedding1, embedding2] = await embeddings.embedDocuments([text1, text2]);
            
            // Calculate cosine similarity
            const dotProduct = embedding1.reduce((sum, val, i) => sum + val * embedding2[i], 0);
            const magnitude1 = Math.sqrt(embedding1.reduce((sum, val) => sum + val * val, 0));
            const magnitude2 = Math.sqrt(embedding2.reduce((sum, val) => sum + val * val, 0));
            
            const similarity = dotProduct / (magnitude1 * magnitude2);
            
            return similarity;
        } catch (error) {
            console.error('Error calculating similarity:', error);
            return 0; // Return 0 similarity on error
        }
    }

    /**
     * Fetch flashcards for a list of documents
     */
    private async fetchFlashcardsForDocuments(
        documents: Array<{ documentId: string, otherUserId: string }>
    ): Promise<FlashCardDTO[]> {
        const flashcardSets: FlashCardDTO[] = [];

        for (const doc of documents) {
            const flashcardSet = await FlashcardSet.findOne({
                documentId: doc.documentId,
                userId: doc.otherUserId
            });

            if (flashcardSet) {
                flashcardSets.push({
                    userId: doc.otherUserId,
                    documentId: doc.documentId,
                    flashcards: flashcardSet.flashcards
                });
            }
        }

        return flashcardSets;
    }

    /**
     * Fetch quizzes for a list of documents
     */
    private async fetchQuizzesForDocuments(
        documents: Array<{ documentId: string, otherUserId: string }>
    ): Promise<QuizDTO[]> {
        const quizSets: QuizDTO[] = [];

        for (const doc of documents) {
            const quizSet = await QuizSet.findOne({
                documentId: doc.documentId,
                userId: doc.otherUserId
            });

            if (quizSet) {
                quizSets.push({
                    userId: doc.otherUserId,
                    documentId: doc.documentId,
                    quiz: quizSet.quiz
                });
            }
        }

        return quizSets;
    }
}

export default new SuggestedMaterialsService(); 