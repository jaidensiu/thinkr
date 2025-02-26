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
     * Retrieves quizzes for a userId and based on the documentIds passed
     * Gets all quizzes for user if documentIds are not provided
     */
    public async retrieveQuizzes(
        documentIds: string[],
        userId: string
    ): Promise<QuizDTO[]> {
        const quizzes = await QuizSet.find({ userId: userId });
        const filteredQuizzes =
            documentIds && documentIds.length > 0
                ? quizzes.filter((q) => documentIds.includes(q.documentId))
                : quizzes;

        return filteredQuizzes.map((q) => ({
            userId: userId,
            documentId: q.documentId,
            quiz: q.quiz.map((quiz) => ({
                question: quiz.question,
                answer: quiz.answer,
                options: quiz.options,
            })),
        })) as QuizDTO[];
    }

    /**
     * Retrieves flashcards for a userId and based on the documentIds passed
     * Gets all flashcards for user if documentIds are not provided
     */
    public async retrieveFlashcards(
        documentIds: string[],
        userId: string
    ): Promise<FlashCardDTO[]> {
        const flashCards = await FlashcardSet.find({ userId: userId });
        const filteredFlashcards =
            documentIds && documentIds.length > 0
                ? flashCards.filter((f) => documentIds.includes(f.documentId))
                : flashCards;

        return filteredFlashcards.map((f) => ({
            userId: userId,
            documentId: f.documentId,
            flashcards: f.flashcards.map((flashcard) => ({
                front: flashcard.front,
                back: flashcard.back,
            })),
        })) as FlashCardDTO[];
    }

    /**
     * Generates both quizzes and flashcards for a documentId and userId and adds them to the db
     */
    public async generateStudyActivities(documentId: string, userId: string) {
        const text = await DocumentService.extractTextFromFile(
            `${userId}-${documentId}`
        );
        await this.ragService.ensureVectorStore(userId);
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
     * Deletes quizzes and flashcards linked to a set of documentIds from the db
     */
    public async deleteStudyActivities(documentIds: string[], userId: string) {
        await this.ragService.deleteDocuments(documentIds, userId);
        await QuizSet.deleteMany({ documentId: { $in: documentIds }, userId });
        await FlashcardSet.deleteMany({
            documentId: { $in: documentIds },
            userId,
        });
    }
}

export default new StudyService();
