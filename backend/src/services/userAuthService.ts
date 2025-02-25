import { OAuth2Client, TokenPayload } from 'google-auth-library';
import jwt from 'jsonwebtoken';
import dotenv from 'dotenv';
import User from '../db/mongo/models/User';
import { UserDTO } from '../interfaces';

dotenv.config();

class UserAuthService {
    private googleClient: OAuth2Client;
    private jwtSecret: string;

    constructor() {
        this.googleClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);
        this.jwtSecret = process.env.JWT_SECRET!;
    }

    /**
     * Verifies a Google ID token and returns the payload.
     */
    public async verifyGoogleToken(
        idToken: string
    ): Promise<TokenPayload | null> {
        try {
            const ticket = await this.googleClient.verifyIdToken({
                idToken,
                audience: process.env.GOOGLE_CLIENT_ID,
            });
            const payload = ticket.getPayload();
            return payload || null;
        } catch (error) {
            console.error('Error verifying Google token: ', error);
            return null;
        }
    }

    /**
     * Generates a JWT for a user.
     */
    public generateToken(userId: string): string {
        return jwt.sign({ userId }, this.jwtSecret, { expiresIn: '1h' });
    }

    /**
     * Verifies a JWT and returns the decoded payload.
     */
    public verifyToken(token: string): { userId: string } | null {
        try {
            const decoded = jwt.verify(token, this.jwtSecret) as {
                userId: string;
            };
            return decoded;
        } catch (error) {
            console.error('Error verifying JWT: ', error);
            return null;
        }
    }

    /**
     * Finds user and creates user if user is not found based on Google credentials.
     */
    public async findCreateUser(googleUser: TokenPayload): Promise<UserDTO> {
        let user = await User.findOne({ googleId: googleUser.sub });
        if (!user) {
            user = new User({
                name: googleUser.name,
                email: googleUser.email,
                googleId: googleUser.sub,
            });
            await user.save();
        }

        return {
            email: googleUser.email!,
            name: googleUser.name,
            googleId: googleUser.sub,
            userId:user._id,
        } as UserDTO;
    }
}

export default new UserAuthService();
