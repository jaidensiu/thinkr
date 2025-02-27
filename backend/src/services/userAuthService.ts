import dotenv from 'dotenv';
import User from '../db/mongo/models/User';
import { AuthPayload, UserDTO } from '../interfaces';

dotenv.config();

class UserAuthService {
    /**
     * Finds user and creates user if user is not found, identified via googleId.
     */
    public async findCreateUser(authPayload: AuthPayload): Promise<UserDTO> {
        let user = await User.findOne({ googleId: authPayload.googleId });
        if (!user) {
            user = new User({
                name: authPayload.name,
                email: authPayload.email,
                googleId: authPayload.googleId,
                subscribed: false,
            });
            await user.save();
        }

        return {
            email: authPayload.email,
            name: authPayload.name,
            googleId: authPayload.googleId,
            subscribed: user.subscribed,
        } as UserDTO;
    }
}

export default new UserAuthService();
