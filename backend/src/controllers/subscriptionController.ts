import { Result } from '../interfaces';
import { Request, Response } from 'express';
import SubscriptionService from '../services/subscriptionService';

export const subscribe = async (req: Request, res: Response): Promise<void> => {
    try {
        const { userId } = req.body;

        if (!userId) {
            res.status(400).json({
                message: 'You must provide a userId identifier',
            });
            return;
        }

        const user = await SubscriptionService.updateAndGetSubscriberStatus(
            userId,
            true
        );

        res.status(200).json({ data: user } as Result);
    } catch (error) {
        console.error('Error subscribing:', error);
        res.status(500).json({
            message: 'Internal server error',
        });
        return;
    }
};

export const unsubscribe = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const { userId } = req.body;

        if (!userId) {
            res.status(400).json({
                message: 'You must provide a userId identifier',
            });
            return;
        }

        const user = await SubscriptionService.updateAndGetSubscriberStatus(
            userId,
            false
        );

        res.status(200).json({ data: user } as Result);
    } catch (error) {
        console.error('Error subscribing:', error);
        res.status(500).json({
            message: 'Internal server error',
        });
        return;
    }
};
