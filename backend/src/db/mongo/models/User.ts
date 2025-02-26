import { Schema, model, Document } from 'mongoose';

export interface IUser extends Document {
    email: string;
    name?: string;
    googleId: string;
    subscribed: boolean;
}

const userSchema = new Schema<IUser>({
    email: { type: String, required: true, unique: true },
    name: { type: String },
    googleId: { type: String, required: true, unique: true },
    subscribed: { type: Boolean, required: true, default: false },
});

const User = model<IUser>('User', userSchema);

export default User;
