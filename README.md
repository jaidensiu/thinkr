# thinkr

### Backend

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

## Usage

- **User Authentication:**

  **Endpoint: `/auth/login`**
  - Method: `POST`
  - Headers: `Authorization: Bearer <Google ID Token>`
  - Body: N/A
  - Response:
  ```json
  {
      "data": {
         "token":  "jwt access token",
         "user": { 
               "email": "user email",
               "name": "user name",
               "googleId": "google id of user",
               "userId": "unique user id",
               "subscribed": false
         }
      }
   }
   ```
- **RAG Query:**

  **Endpoint: `/rag/query`**
  - Method: `POST`
  - Body: raw 
   ```json
   { 
      "query": "Your question here" 
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

- **Chat:**

  **Endpoint: `/chat`**
  - Method: `POST`
  - Body: raw
  ```json
  {
     "userId": "user123",
     "metadata": {
        "source": "web",
        "topic": "general"
     }
  }
  ```
  - Response:
  ```json
  {
     "data": {
        "session": {
           "sessionId": "unique-session-id",
           "userId": "user123",
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
              "topic": "general"
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
           "userId": "user123",
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

- **Documents:**

  **Endpoint: `/document/upload`**
  - Method: `POST`
  - Body: multipart/form-data
   ```json
   {
      "documents": "<your file(s) here>",
      "userId": "userId"
   }
   ```
  - Response:
  ```json
   {
      "data": {
         "docs": [
            {
               "documentId": "first file",
               "uploadTime": "time of file upload"
            },
            {
               "documentId": "second file",
               "uploadTime": "time of file upload"
            }
         ]
      }
   }
  ```

  **Endpoint: `/document/delete`**
  - Method: `DELETE`
  - Body: raw
   ```json
   {
      "userId": "userId",
      "paths": [
         "path of first file",
         "path of second file"
      ]
   }
   ```
  - Response: N/A

  **Endpoint: `/document/retrieve`**
  - Method: `GET`
  - Body: raw, paths is an OPTIONAL field
  ```json
   {
      "userId": "userId",
      "paths": [
         "path of first file",
         "path of second file"
      ]
   }
  ```
  - Response:
  ```json
   {
      "data": {
         "docs": [
            {
               "url": "second file's s3 document urlt",
               "documentId": "first file",
               "uploadTime": "time of file upload"
            },
            {
               "url": "second file's s3 document url",
               "documentId": "second file",
               "uploadTime": "time of file upload"
            }
         ]
      }
   }
  ```
  - Note: Returns all of the user's files if no paths are provided for `GET /document/retrieve`. The `paths` must include file types (e.g. `file1.pdf` is one path, not `file1` by itself)

- **Study:**

  **Endpoint: `/study/flashcards`**
  - Method: `POST`
  - Body: raw
  ```json
   {
      "userId": "userId",
      "path": "file path"
   }
  ```
  - Response:
  ```json
  {
      "data": {
         "userId": "userId",
         "documentName": "file path",
         "flashcards": [
            {
               "front": "first word",
               "back": "definition of first word"
            },
            {
               "front": "second word",
               "back": "definition of second word"
            },
         ]
      }
   }
   ```

   **Endpoint: `/study/quiz`**
   - Method: `POST`
   - Body: raw
   ```json
   {
      "userId": "userId",
      "path": "file path"
   }
   ```
   - Response
   ```json
   {
      "data": {
         "userId": "userId",
         "documentName": "file path",
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
               "question": "Question 1",
               "answer": "C",
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
   - Body: raw, paths is an OPTIONAL field
   ```json
   {
      "userId": "userId",
      "paths": [
         "file path 1", 
         "file path 2"
      ]
   }
   ```
   - Response
   ```json
   {
      "data": [
         {
            "userId": "userId",
            "documentName": "file path 1",
            "flashcards": [
               {
                  "front": "first word",
                  "back": "definition of first word"
               },
               {
                  "front": "second word",
                  "back": "definition of second word"
               },
            ]
         }
      ]
   }
   ```
   - Note: if paths are not provided, we retrieve all of the user's past generated quizzes


   **Endpoint: `/study/flashcards`**
   - Method: `GET`
   - Body: raw, paths is an OPTIONAL field
   ```json
   {
      "userId": "userId",
      "paths": [
         "file path 1", 
         "file path 2"
      ]
   }
   ```
   - Response
   ```json
   {
      "data": [
         {
            "userId": "userId",
            "documentName": "file path 1",
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
                  "question": "Question 1",
                  "answer": "C",
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
   - Note: if paths are not provided, we retrieve all of the user's past generated flashcards

  **Endpoint: `/subscription`**
  - Method: `POST`
  - Body: raw
  ```json
   {
      "userId": "your user id"
   }
  ```
  - Response
  ```json
      {
      "data": {
         "email": "user email",
         "name": "user name",
         "googleId": "google id of user",
         "userId": "unique user id",
         "subscribed": true
      }
   }
   ```
   
  **Endpoint: `/subscription`**
  - Method: `DELETE`
  - Body: raw
  ```json
   {
      "userId": "your user id"
   }
  ```
  - Response
  ```json
      {
      "data": {
         "email": "user email",
         "name": "user name",
         "googleId": "google id of user",
         "userId": "unique user id",
         "subscribed": false
      }
   }
  ```
