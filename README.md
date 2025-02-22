# thinkr

### Backend

1. `npm install`.
2. Set up mongoDB (connection URI)
3. Set up google auth credentials (client id) in Google Cloud Console.
4. Generate JWT secret, you can use this https://jwtsecret.com/generate.
5. Create a `.env` file in root of `backend` directory with:
```
GOOGLE_CLIENT_ID=<web_client_ID_from_google_cloud_credentials>
JWT_SECRET=<randomly_generated_jwt_secret>
MONGO_URI=<your_mongoDB_connection_URI>
```
6. `npm start` or `npm run dev` (nodemon).
