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
  - Endpoint: `/auth/login`
  - Method: `POST`
  - Headers: `Authorization: Bearer <Google ID Token>`
  - Body: N/A
  - Response:
  ```json
  {
      "data": {
         "token":  <string> // jwt access token,
         "user": { 
               "email": <string>,
               "name": <string>,
               "googleId": <string>,
               "id": <string> // db user id
         }
      }
   }
   ```
- **RAG Query:**
  - Endpoint: `/rag/query`
  - Method: `POST`
  - Body: raw 
   ```json
   { 
      "query": "Your question here" 
   }
   ```

- **Documents:**
  - Endpoint: `/document/upload`
  - Method: `POST`
  - Body: multipart/form-data
   ```json
   {
      "documents": "<your file(s) here>",
      "email": "user email"
   }
   ```
  - Response:
  ```json
   {
      "data": {
         "docs": [
            {
               "name": "first file",
               "uploadTime": "time of file upload"
            },
            {
               "name": "second file",
               "uploadTime": "time of file upload"
            }
         ]
      }
   }
  ```

  - Endpoint: `/document/delete`
  - Method: `DELETE`
  - Body: raw
   ```json
   {
      "email": "your email",
      "paths": [
         "name of first file you want to delete",
         "name of second file you want to delete"
      ]
   }
   ```
  - Response: N/A

  - Endpoint: `/document/retrieve`
  - Method: `GET`
  - Body: raw
  ```json
   {
      "email": "rayhownh@gmail.com",
      "paths": [
         "name of first file you want",
         "name of second file you want"
      ] // optional
   }
  ```
  - Response:
  ```json
   {
      "data": {
         "docs": [
            {
               "url": "second file's s3 document urlt",
               "name": "first file",
               "uploadTime": "time of file upload"
            },
            {
               "url": "second file's s3 document url",
               "name": "second file",
               "uploadTime": "time of file upload"
            }
         ]
      }
   }
  ```
  - Note: Returns all of the user's files if no paths are provided
