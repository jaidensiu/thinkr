import express from 'express';
import dotenv from 'dotenv';
import authRouter from './routes/userAuthRoutes';
import connectMongoDB from './db/mongo/connection';

dotenv.config();

const PORT = 3000;
const app = express();

app.use(express.json());
app.use('/auth', authRouter);
// TODO: Protect routes?

connectMongoDB();
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
