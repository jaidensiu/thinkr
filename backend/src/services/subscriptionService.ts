import User from '../db/mongo/models/User';
import { UserDTO } from '../interfaces';

class SubscriptionService {
    /**
     * Updates subscriber status and returns the user information with updated status
     */
    public async updateAndGetSubscriberStatus(
        userId: string,
        subscribe: boolean
    ): Promise<UserDTO> {
        let user = await User.findOne({ googleId: userId });
        if (!user) {
            throw new Error('Error: User not found');
        }

        await User.updateOne(
            { googleId: userId },
            {
                subscribed: subscribe,
            }
        );

        return {
            email: user.email!,
            name: user.name,
            googleId: user.googleId,
            subscribed: subscribe,
        } as UserDTO;
    }
}

export default new SubscriptionService();
