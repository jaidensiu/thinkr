import User from '../db/mongo/models/User';
import { UserDTO } from '../interfaces';

class SubscriptionService {
    constructor() {
        // TODO: Simulate payments
        // These payments are mocked right now
    }

    private async makePayment(userId: string): Promise<void> {
        return;
    }

    private async cancelPayment(userId: string): Promise<void> {
        console.log(`Payment for user: ${userId} cancelled!`);
        return;
    }

    public async updateAndGetSubscriberStatus(
        userId: string,
        subscribe: boolean
    ): Promise<UserDTO> {
        let user = await User.findOne({ _id: userId });
        if (!user) {
            throw new Error('Error: User not found');
        }

        if (!user.subscribed && subscribe) {
            await this.makePayment(userId);
        } else if (user.subscribed && !subscribe) {
            await this.cancelPayment(userId);
        }

        await User.updateOne(
            { _id: userId },
            {
                subscribed: subscribe,
            }
        );

        return {
            email: user.email!,
            name: user.name,
            googleId: user.googleId,
            userId: user._id,
            subscribed: subscribe,
        } as UserDTO;
    }
}

export default new SubscriptionService();
