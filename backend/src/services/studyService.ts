import { PromptTemplate } from '@langchain/core/prompts';
import { ChatOpenAI } from '@langchain/openai';
import { FlashCard, FlashCardDTO, Quiz, QuizDTO } from '../interfaces';
import RAGService from './RAGService';
import { StructuredOutputParser } from '@langchain/core/output_parsers';
import { z } from 'zod';
import FlashcardSet from '../db/mongo/models/FlashcardSet';
import QuizSet from '../db/mongo/models/QuizSet';
import DocumentService from './documentService';
import Document from '../db/mongo/models/Document';

class StudyService {
    private llm: ChatOpenAI;
    private ragService: RAGService;
    constructor() {
        this.llm = new ChatOpenAI({
            openAIApiKey: process.env.OPENAI_API_KEY,
            temperature: 0.7,
            // model: 'gpt-4'
        });
        this.ragService = new RAGService({
            openAIApiKey: process.env.OPENAI_API_KEY!,
            vectorStoreUrl: process.env.VECTOR_STORE_URL!,
        });
    }

    /**
     * Creates flashcards for a userId and based on the documentId (embeddingId) passed
     */
    public async createFlashCards(
        embeddingId: string,
        collection: string
    ): Promise<FlashCardDTO> {
        const docs = await this.ragService.fetchDocumentsFromVectorDB(
            embeddingId,
            collection
        );

        const flashcardPrompt = new PromptTemplate({
            inputVariables: ['content', 'format_instructions'],
            template: `Generate a list of flashcards from the following content. For each flashcard:
            1. The front should be a **short and clear term or concept**.
            2. The back should be a **concise and accurate definition or explanation** of the term or concept.
            3. Ensure that:
            - Terms are distinct and not repeated.
            - Definitions are precise and avoid unnecessary verbosity.
            - Terms and definitions are directly relevant to the provided content.
            4. Avoid creating flashcards with similar terms or definitions unless they are meaningfully distinct.
            5. You may generate as many flashcards as you'd like in order to cover all important topics in the documents as long as it
            follows the criteria listed above.
            Always generate a valid JSON array of JSON objects as specified in the formatting instructions.

            Formatting Instructions: {format_instructions}
            Content: {content}
            `,
        });

        const flashcardSchema = z.array(
            z.object({
                front: z.string().describe('The term or concept'),
                back: z.string().describe('The definition or explanation'),
            })
        );

        const parser = StructuredOutputParser.fromZodSchema(flashcardSchema);
        const chain = flashcardPrompt.pipe(this.llm).pipe(parser);

        const flashcards: FlashCard[] = await chain.invoke({
            content: docs.join('\n'),
            format_instructions: parser.getFormatInstructions(),
        });

        const existingFlashCard = await FlashcardSet.findOne({
            userId: collection,
            documentId: embeddingId,
        });
        if (existingFlashCard) {
            await FlashcardSet.updateOne(
                { userId: collection, documentId: embeddingId },
                { flashcards }
            );
        } else {
            await FlashcardSet.create({
                userId: collection,
                documentId: embeddingId,
                flashcards,
            });
        }

        return {
            userId: collection,
            documentId: embeddingId,
            flashcards: flashcards,
        } as FlashCardDTO;
    }

    /**
     * Creates a quiz for a userId and based on the documentId (embeddingId) passed
     */
    public async createQuiz(
        embeddingId: string,
        collection: string
    ): Promise<QuizDTO> {
        const docs = await this.ragService.fetchDocumentsFromVectorDB(
            embeddingId,
            collection
        );

        const quizPrompt = new PromptTemplate({
            inputVariables: ['content', 'format_instructions'],
            template: `Generate a list of multiple-choice questions from the following content. 
            For each question, provide:
            1. A clear and concise question.
            2. One correct answer.
            3. Three incorrect but plausible distractors.
            4. Assign letters (A, B, C, D) to each option and include the correct answer's letter in the response.
            5. Ensure the questions are relevant to the content and avoid repetition.
            6. You may generate as many questions as you'd like in order to cover all important topics in the documents as long as it
            follows the criteria listed above.
            Always generate a valid JSON array of JSON objects as specified in the formatting instructions.
            Formatting Instructions: {format_instructions}
            Content: {content}
            `,
        });

        const quizSchema = z.array(
            z.object({
                question: z.string().describe('The multiple-choice question'),
                answer: z
                    .string()
                    .describe('The letter of the correct answer (e.g., "A")'),
                options: z
                    .record(z.string())
                    .describe(
                        'Key-value pairs of options (e.g., { "A": "Option 1", "B": "Option 2" })'
                    ),
            })
        );

        const parser = StructuredOutputParser.fromZodSchema(quizSchema);
        const chain = quizPrompt.pipe(this.llm).pipe(parser);

        const quiz: Quiz[] = await chain.invoke({
            content: docs.join('\n'),
            format_instructions: parser.getFormatInstructions(),
        });

        const existingQuiz = await QuizSet.findOne(
            { userId: collection, documentId: embeddingId },
            {
                quiz,
            }
        );
        if (existingQuiz) {
            await QuizSet.updateOne(
                { userId: collection, documentId: embeddingId },
                { quiz }
            );
        } else {
            await QuizSet.create({
                userId: collection,
                documentId: embeddingId,
                quiz,
            });
        }

        return {
            userId: collection,
            documentId: embeddingId,
            quiz: quiz,
        } as QuizDTO;
    }

    /**
     * Retrieves quizzes for a userId and based on the documentId passed
     * Gets all quizzes for user if documentId is not provided
     */
    public async retrieveQuizzes(
        documentId: string,
        userId: string
    ): Promise<QuizDTO[] | QuizDTO> {
        const quizzes = await QuizSet.find({ userId });

        const filteredQuizzes = documentId
            ? quizzes.filter((q) => q.documentId === documentId)
            : quizzes;

        const mappedQuizzes = filteredQuizzes.map((q) => ({
            userId,
            documentId: q.documentId,
            quiz: q.quiz.map((quiz) => ({
                question: quiz.question,
                answer: quiz.answer,
                options: quiz.options,
            })),
        })) as QuizDTO[];

        return documentId ? mappedQuizzes[0] : mappedQuizzes;
    }

    /**
     * Retrieves flashcards for a userId and based on the documentIds passed
     * Gets all flashcards for user if documentIds are not provided
     */
    public async retrieveFlashcards(
        documentId: string,
        userId: string
    ): Promise<FlashCardDTO[] | FlashCardDTO> {
        const flashCards = await FlashcardSet.find({ userId: userId });
        const filteredFlashcards = documentId
            ? flashCards.filter((f) => documentId === f.documentId)
            : flashCards;

        const mappedFlashcards = filteredFlashcards.map((f) => ({
            userId: userId,
            documentId: f.documentId,
            flashcards: f.flashcards.map((flashcard) => ({
                front: flashcard.front,
                back: flashcard.back,
            })),
        })) as FlashCardDTO[];

        return documentId ? mappedFlashcards[0] : mappedFlashcards;
    }

    /**
     * Generates both quizzes and flashcards for a documentId and userId and adds them to the db
     */
    public async generateStudyActivities(documentId: string, userId: string) {
        const text = await DocumentService.extractTextFromFile(
            `${userId}-${documentId}`
        );
        console.log(userId);
        await this.ragService.initVectorStore(`user_${userId}`);
        await this.ragService.insertDocument(userId, documentId, text);

        // generate activities
        await this.createFlashCards(documentId, userId);
        await this.createQuiz(documentId, userId);

        await Document.findOneAndUpdate(
            { userId: userId, documentId: documentId },
            { activityGenerationComplete: true }
        );
    }

    /**
     * Deletes quizzes and flashcards linked to a documentId from the db
     */
    public async deleteStudyActivities(documentId: string, userId: string) {
        await this.ragService.deleteDocuments([documentId], userId);
        await QuizSet.deleteOne({ documentId: documentId, userId: userId });
        await FlashcardSet.deleteOne({
            documentId: documentId,
            userId: userId,
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
                userDocuments.map((doc) => doc.documentId),
                limit
            );

            if (similarDocumentIds.length === 0) {
                return { flashcards: [], quizzes: [] };
            }

            // 3. Fetch flashcards and quizzes for similar documents
            const [rawFlashcards, rawQuizzes] = await Promise.all([
                this.fetchFlashcardsForDocuments(similarDocumentIds),
                this.fetchQuizzesForDocuments(similarDocumentIds),
            ]);

            // 4. Clean up structure - removive _id
            const flashcards = rawFlashcards.map((f) => ({
                userId: f.userId,
                documentId: f.documentId,
                flashcards: f.flashcards.map((flashcard) => ({
                    front: flashcard.front,
                    back: flashcard.back,
                })),
            })) as FlashCardDTO[];

            const quizzes = rawQuizzes.map((q) => ({
                userId: q.userId,
                documentId: q.documentId,
                quiz: q.quiz.map((quiz) => ({
                    question: quiz.question,
                    answer: quiz.answer,
                    options: quiz.options,
                })),
            })) as QuizDTO[];

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
    ): Promise<Array<{ documentId: string; otherUserId: string }>> {
        // Initialize results array
        const similarityResults: Array<{
            documentId: string;
            otherUserId: string;
            similarityScore: number;
        }> = [];

        // Get all documents from other users
        const otherUsersDocuments = await Document.find({
            userId: { $ne: userId },
        });

        if (otherUsersDocuments.length === 0) {
            return [];
        }

        // For each of the user's documents, find similar documents
        for (const docId of userDocumentIds) {
            try {
                // Get the document text from ChromaDB
                const userDocText =
                    await this.ragService.fetchDocumentsFromVectorDB(
                        docId,
                        userId
                    );

                if (userDocText.length === 0) continue;

                // Combine all chunks into one text for comparison
                const combinedUserDocText = userDocText.join(' ');

                // For each document from other users, calculate similarity
                for (const otherDoc of otherUsersDocuments) {
                    try {
                        // Get the other document's text from ChromaDB
                        const otherDocText =
                            await this.ragService.fetchDocumentsFromVectorDB(
                                otherDoc.documentId,
                                otherDoc.userId
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
                            similarityScore,
                        });
                    } catch (error) {
                        console.error(
                            `Error processing document ${otherDoc.documentId}:`,
                            error
                        );
                        // Continue with other documents
                    }
                }
            } catch (error) {
                console.error(
                    `Error processing user document ${docId}:`,
                    error
                );
                // Continue with other user documents
            }
        }

        // Sort by similarity score (highest first) and take the top 'limit' results
        const topSimilarDocuments = similarityResults
            .sort((a, b) => b.similarityScore - a.similarityScore)
            .slice(0, limit)
            .map((result) => ({
                documentId: result.documentId,
                otherUserId: result.otherUserId,
            }));

        return topSimilarDocuments;
    }

    /**
     * Calculate similarity between two text documents
     * This is a simplified approach using cosine similarity of embeddings
     */
    private async calculateSimilarity(
        text1: string,
        text2: string
    ): Promise<number> {
        try {
            // Skip ChromaDB and just use OpenAI embeddings directly
            const embeddings = new (
                await import('@langchain/openai')
            ).OpenAIEmbeddings({
                openAIApiKey: process.env.OPENAI_API_KEY!,
            });

            // Get embeddings for both texts
            const [embedding1, embedding2] = await embeddings.embedDocuments([
                text1,
                text2,
            ]);

            // Calculate cosine similarity
            const dotProduct = embedding1.reduce(
                (sum, val, i) => sum + val * embedding2[i],
                0
            );
            const magnitude1 = Math.sqrt(
                embedding1.reduce((sum, val) => sum + val * val, 0)
            );
            const magnitude2 = Math.sqrt(
                embedding2.reduce((sum, val) => sum + val * val, 0)
            );

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
        documents: Array<{ documentId: string; otherUserId: string }>
    ): Promise<FlashCardDTO[]> {
        const flashcardSets: FlashCardDTO[] = [];

        for (const doc of documents) {
            const flashcardSet = await FlashcardSet.findOne({
                documentId: doc.documentId,
                userId: doc.otherUserId,
            });

            if (flashcardSet) {
                flashcardSets.push({
                    userId: doc.otherUserId,
                    documentId: doc.documentId,
                    flashcards: flashcardSet.flashcards,
                });
            }
        }

        return flashcardSets;
    }

    /**
     * Fetch quizzes for a list of documents
     */
    private async fetchQuizzesForDocuments(
        documents: Array<{ documentId: string; otherUserId: string }>
    ): Promise<QuizDTO[]> {
        const quizSets: QuizDTO[] = [];

        for (const doc of documents) {
            const quizSet = await QuizSet.findOne({
                documentId: doc.documentId,
                userId: doc.otherUserId,
            });

            if (quizSet) {
                quizSets.push({
                    userId: doc.otherUserId,
                    documentId: doc.documentId,
                    quiz: quizSet.quiz,
                });
            }
        }

        return quizSets;
    }
}

export default new StudyService();
