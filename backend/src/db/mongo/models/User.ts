import { Schema, model, Document } from 'mongoose';

/**
 * Interface representing a user in database for data transfer between backend <-> DB
 */
export interface IUser extends Document {
    email: string;
    name?: string;
    googleId: string;
}

const userSchema = new Schema<IUser>({
    email: { type: String, required: true, unique: true },
    name: { type: String },
    googleId: { type: String, required: true, unique: true },
});

const User = model<IUser>('User', userSchema);

export default User;
