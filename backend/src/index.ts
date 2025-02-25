import express from 'express';
import dotenv from 'dotenv';
import authRouter from './routes/userAuthRoutes';
import documentRouter from './routes/documentRoutes';
import studyRouter from './routes/studyRoutes';
import ragRouter from './routes/ragRoutes';
import connectMongoDB from './db/mongo/connection';
import chatRouter from './routes/chatRoutes';

dotenv.config();

const PORT = 3000;
const app = express();

app.use(express.json());
app.use('/auth', authRouter);
app.use('/document', documentRouter);
app.use('/study', studyRouter);
app.use('/rag', ragRouter);
app.use('/chat', chatRouter);

connectMongoDB();

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
