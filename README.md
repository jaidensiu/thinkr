# thinkr

## Backend Setup

1. `npm install`.
2. Set up mongoDB (connection URI)
3. Set up google auth credentials (client id) in Google Cloud Console.
4. Generate JWT secret, you can use this https://jwtsecret.com/generate.
5. Create a `.env` file in the `backend` directory with the following variables:
   ```plaintext
   GOOGLE_CLIENT_ID=<your_google_client_id>
   JWT_SECRET=<your_jwt_secret>
   MONGO_URI=<your_mongodb_uri>
   OPENAI_API_KEY=<your_openai_api_key>
   VECTOR_STORE_URL=http://localhost:8000
   AWS_ACCESS_KEY_ID=<your AWS access key>
   AWS_SECRET_ACCESS_KEY=<your AWS IAM secret access key>
   AWS_REGION=<your aws_region>
   S3_BUCKET_NAME=<your s3 bucket name>
   ```
6. **Run ChromaDB:**
   Ensure Docker is running, then execute:
   ```bash
   docker pull chromadb/chroma
   docker run -d -p 8000:8000 chromadb/chroma
   ```
7. **Initialize ChromaDB:**
   Populate ChromaDB with initial documents:
   ```bash
   npm run init-chroma
   ```
8. `npm start` or `npm run dev` (nodemon).

## Architecture Overview

### ChromaDB Implementation

The system uses ChromaDB to store document embeddings with the following architecture:

- **User-Specific Collections**: Each user has their own ChromaDB collection named `user_{googleId}`
- **Document Metadata**: Documents within a collection are tagged with metadata including:
  - `googleId`: The owner of the document
  - `documentId`: The identifier of the document
  - `chunkIndex`: Position of the chunk within the document
- **Document Chunking**: Large documents are split into manageable chunks for better retrieval
- **Filtering**: Queries can be filtered to specific documents or search across all user documents

## API Endpoints

### User Authentication

**Endpoint: `/auth/login`**
- Method: `POST`
- Body: raw
```json
{
 "googleId": "google id of user",
 "name": "name of user",
 "email": "email of user"
}
```
- Response:
```json
{
    "data": {
       "user": { 
             "email": "user email",
             "name": "user name",
             "googleId": "google id of user",
             "subscribed": false
       }
    }
 }
```

### RAG Query

**Endpoint: `/rag/query`**
- Method: `POST`
- Body: raw 
```json
{ 
   "query": "Your question here",
   "googleId": "user123",
   "documentId": "optional_specific_document.pdf"
}
```
- Response:
```json
{
    "data": {
       "response": "AI-generated answer based on your documents"
    }
 }
```
- Note: If `documentId` is provided, the query will only search within that document. Otherwise, it searches across all user documents.

### Chat

**Endpoint: `/chat`**
- Method: `POST`
- Body: raw
```json
{
   "googleId": "user123",
   "metadata": {
      "source": "web",
      "topic": "general",
      "documentId": "optional_specific_document.pdf"
   }
}
```
- Response:
```json
{
   "data": {
      "session": {
         "sessionId": "unique-session-id",
         "googleId": "user123",
         "messages": [
            {
               "role": "system",
               "content": "You are a helpful assistant that provides accurate information based on the context provided.",
               "timestamp": "2023-07-10T12:34:56.789Z"
            }
         ],
         "createdAt": "2023-07-10T12:34:56.789Z",
         "updatedAt": "2023-07-10T12:34:56.789Z",
         "metadata": {
            "source": "web",
            "topic": "general",
            "documentId": "optional_specific_document.pdf"
         }
      }
   }
}
```

**Endpoint: `/chat/:sessionId/message`**
- Method: `POST`
- Body: raw
```json
{
   "message": "What is artificial intelligence?"
}
```
- Response:
```json
{
   "data": {
      "response": "Artificial intelligence (AI) refers to the simulation of human intelligence in machines..."
   }
}
```

**Endpoint: `/chat/:sessionId`**
- Method: `GET`
- Response:
```json
{
   "data": {
      "session": {
         "sessionId": "unique-session-id",
         "googleId": "user123",
         "messages": [
            {
               "role": "system",
               "content": "You are a helpful assistant...",
               "timestamp": "2023-07-10T12:34:56.789Z"
            },
            {
               "role": "user",
               "content": "What is artificial intelligence?",
               "timestamp": "2023-07-10T12:35:10.123Z"
            },
            {
               "role": "assistant",
               "content": "Artificial intelligence (AI) refers to...",
               "timestamp": "2023-07-10T12:35:15.456Z"
            }
         ],
         "createdAt": "2023-07-10T12:34:56.789Z",
         "updatedAt": "2023-07-10T12:35:15.456Z",
         "metadata": {
            "source": "web",
            "topic": "general"
         }
      }
   }
}
```

**Endpoint: `/chat/:sessionId`**
- Method: `DELETE`
- Response:
```json
{
   "message": "Chat session deleted successfully"
}
```

### Documents

**Endpoint: `/document/upload`**
- Method: `POST`
- Body: multipart/form-data
```json
{
   "document": "<your file (single) here>",
   "googleId": "googleId"
}
```
- Response:
```json
{
   "data": {
      "docs": {
         "documentId": "first file",
         "uploadTime": "time of file upload",
         "activityGenerationComplete": false
      },
   }
}
```
- Note: The system supports PDF, JPEG, PNG, TIFF, and text files. Document text is extracted and stored in ChromaDB for retrieval.

**Endpoint: `/document/delete`**
- Method: `DELETE`
- Body: raw
```json
{
   "googleId": "googleId",
   "documentIds": [
      "documentId of first file",
      "documentId of second file"
   ]
}
```
- Response: N/A
- Note: This deletes both the document from S3 and its embeddings from ChromaDB.

**Endpoint: `/document/retrieve`**
- Method: `GET`
- Body: raw, documentIds is an OPTIONAL field
```json
{
   "googleId": "googleId",
   "documentIds": [
      "documentId of first file",
      "documentId of second file"
   ]
}
```
- Response:
```json
{
   "data": {
      "docs": [
         {
            "documentId": "first file",
            "uploadTime": "time of file upload",
            "activityGenerationComplete": false
         },
         {
            "documentId": "second file",
            "uploadTime": "time of file upload",
            "activityGenerationComplete": true
         }
      ]
   }
}
```
- Note: Returns all of the user's files if no documentIds are provided. The `documentIds` must include file types (e.g. `file1.pdf` is one documentId, not `file1` by itself)

### Study

**Endpoint: `/study/flashcards`**
- Method: `POST`
- Body: raw
```json
{
   "googleId": "googleId",
   "documentId": "file documentId"
}
```
- Response:
```json
{
   "data": {
      "googleId": "googleId",
      "documentId": "file documentId",
      "flashcards": [
         {
            "front": "first word",
            "back": "definition of first word"
         },
         {
            "front": "second word",
            "back": "definition of second word"
         }
      ]
   }
}
```

**Endpoint: `/study/quiz`**
- Method: `POST`
- Body: raw
```json
{
   "googleId": "googleId",
   "documentId": "file documentId"
}
```
- Response
```json
{
   "data": {
      "googleId": "googleId",
      "documentId": "file documentId",
      "quiz": [
         {
            "question": "Question 1",
            "answer": "C",
            "options": {
               "A": "Answer 1",
               "B": "Answer 2",
               "C": "Answer 3",
               "D": "Answer 4"
            } 
         },
         {
            "question": "Question 2",
            "answer": "A",
            "options": {
               "A": "Answer 1",
               "B": "Answer 2",
               "C": "Answer 3",
               "D": "Answer 4"
            }
         }
      ]
   }
}
```

**Endpoint: `/study/quiz`**
- Method: `GET`
- Body: raw, documentIds is an OPTIONAL field
```json
{
   "googleId": "googleId",
   "documentIds": [
      "file documentId 1", 
      "file documentId 2"
   ]
}
```
- Response
```json
{
   "data": [
      {
         "googleId": "googleId",
         "documentId": "file documentId 1",
         "quiz": [
            {
               "question": "Question 1",
               "answer": "C",
               "options": {
                  "A": "Answer 1",
                  "B": "Answer 2",
                  "C": "Answer 3",
                  "D": "Answer 4"
               } 
            },
            {
               "question": "Question 2",
               "answer": "A",
               "options": {
                  "A": "Answer 1",
                  "B": "Answer 2",
                  "C": "Answer 3",
                  "D": "Answer 4"
               }
            }
         ]
      }
   ]
}
```
- Note: if documentIds are not provided, we retrieve all of the user's past generated quizzes

**Endpoint: `/study/flashcards`**
- Method: `GET`
- Body: raw, documentIds is an OPTIONAL field
```json
{
   "googleId": "googleId",
   "documentIds": [
      "file documentId 1", 
      "file documentId 2"
   ]
}
```
- Response
```json
{
   "data": [
      {
         "googleId": "googleId",
         "documentId": "file documentId 1",
         "flashcards": [
            {
               "front": "first word",
               "back": "definition of first word"
            },
            {
               "front": "second word",
               "back": "definition of second word"
            }
         ]
      }
   ]
}
```
- Note: if documentIds are not provided, we retrieve all of the user's past generated flashcards

### Subscription

**Endpoint: `/subscription`**
- Method: `POST`
- Body: raw
```json
{
   "googleId": "your user id"
}
```
- Response
```json
{
   "data": {
      "email": "user email",
      "name": "user name",
      "googleId": "google id of user",
      "subscribed": true
   }
}
```

**Endpoint: `/subscription`**
- Method: `DELETE`
- Body: raw
```json
{
   "googleId": "your user id"
}
```
- Response
```json
{
   "data": {
      "email": "user email",
      "name": "user name",
      "googleId": "google id of user",
      "subscribed": false
   }
}
```

## Testing with cURL

Here are some example cURL commands to test the API:

### Document Upload
```
curl -X POST http://localhost:3000/document/upload -F "document=@/path/to/your/document.pdf" -F "googleId=user123"
```

### RAG Query (All Documents)
```
curl -X POST http://localhost:3000/rag/query -H "Content-Type: application/json" -d '{"query": "What is the main topic of my documents?", "googleId": "user123"}'
```

### RAG Query (Specific Document)
```
curl -X POST http://localhost:3000/rag/query -H "Content-Type: application/json" -d '{"query": "What is discussed in this document?", "googleId": "user123", "documentId": "document.pdf"}'
```

### Create Chat Session
```
curl -X POST http://localhost:3000/chat -H "Content-Type: application/json" -d '{"googleId": "user123", "metadata": {"documentId": "document.pdf"}}'
```
