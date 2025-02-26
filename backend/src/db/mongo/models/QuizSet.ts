import { Schema, model, HydratedDocumentFromSchema } from 'mongoose';
import { Quiz, QuizDTO } from '../../../interfaces';

export interface IQuizSet extends Document {
    userId: string;
    documentName: string;
    quiz: Quiz[];
}

const quizSetSchema = new Schema<IQuizSet>({
    userId: { type: String, required: true },
    documentName: { type: String, required: true, trim: true },
    quiz: [
        {
            question: { type: String, required: true, trim: true },
            answer: { type: String, required: true, trim: true },
            options: { type: Schema.Types.Mixed, required: true },
        },
    ],
});

const QuizSet = model<IQuizSet>('QuizSet', quizSetSchema);

export default QuizSet;
