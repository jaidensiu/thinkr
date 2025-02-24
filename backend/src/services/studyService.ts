import { ChatPromptTemplate, PromptTemplate } from '@langchain/core/prompts';
import { ChatOpenAI, OpenAI } from '@langchain/openai';
import { FlashCardDTO } from '../interfaces';
import RAGService from './RAGService';
import {
    JsonOutputParser,
    StructuredOutputParser,
} from '@langchain/core/output_parsers';
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
            template: `Generate a list of flashcards from the following content where for each flashcard 
            the front is the term or word and the back is the corresponding definition of the word. 
            Keep terms short as possible and do not repeat terms or create flashcards with similar terms with similar definitions.
            Keep definitions concise and not too overly verbose.
            Always generate a only valid json array of json objects as specified in the formatting instructions.
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
}

export default new StudyService();
