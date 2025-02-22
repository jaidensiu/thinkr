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

- **RAG Query:**
  - Endpoint: `/rag/query`
  - Method: `POST`
  - Body: `{ "query": "Your question here" }`

