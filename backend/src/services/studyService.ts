import { PromptTemplate } from '@langchain/core/prompts';
import { ChatOpenAI } from '@langchain/openai';
import { FlashCardDTO, QuizDTO } from '../interfaces';
import RAGService from './RAGService';
import { StructuredOutputParser } from '@langchain/core/output_parsers';
import { z } from 'zod';

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

    public async createFlashCards(
        embeddingIds: string[],
        collection: string
    ): Promise<FlashCardDTO[]> {
        const docs = await this.ragService.fetchDocumentsFromVectorDB(
            embeddingIds,
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

        const flashcards = await chain.invoke({
            content: docs.join('\n'),
            format_instructions: parser.getFormatInstructions(),
        });

        return flashcards as FlashCardDTO[];
    }

    public async createQuiz(
        embeddingIds: string[],
        collection: string
    ): Promise<QuizDTO[]> {
        const docs = await this.ragService.fetchDocumentsFromVectorDB(
            embeddingIds,
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
                answer: z.string().describe('The letter of the correct answer (e.g., "A")'),
                options: z.record(z.string()).describe('Key-value pairs of options (e.g., { "A": "Option 1", "B": "Option 2" })'),
            })
        );
    
        const parser = StructuredOutputParser.fromZodSchema(quizSchema);
        const chain = quizPrompt.pipe(this.llm).pipe(parser);
    
        const quiz = await chain.invoke({
            content: docs.join('\n'),
            format_instructions: parser.getFormatInstructions(),
        });
    
        return quiz as QuizDTO[];
    }
}

export default new StudyService();
