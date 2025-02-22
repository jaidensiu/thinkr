import { ChromaClient } from 'chromadb';
import { OpenAIEmbeddings } from '@langchain/openai';
import { Chroma } from '@langchain/community/vectorstores/chroma';
import dotenv from 'dotenv';

dotenv.config();

async function initializeChroma() {
    const client = new ChromaClient({ path: process.env.VECTOR_STORE_URL });
    
    // Create embeddings instance
    const embeddings = new OpenAIEmbeddings({
        openAIApiKey: process.env.OPENAI_API_KEY
    });

    // Create or get collection
    const collection = await client.createCollection({
        name: "documents",
        metadata: { "description": "Test documents collection" }
    });

    // Example documents
    const documents = [
        "This is a test document about artificial intelligence.",
        "Here's another document about machine learning.",
        "This document discusses natural language processing."
    ];

    // Create Chroma instance with documents
    await Chroma.fromTexts(
        documents,
        { source: "test" },
        embeddings,
        {
            collectionName: "documents",
            url: process.env.VECTOR_STORE_URL
        }
    );

    console.log("ChromaDB initialized with test documents!");
}

initializeChroma().catch(console.error); 