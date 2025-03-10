jest.mock('../db/mongo/models/User', () => {
    const mockFindOne = jest.fn();
    const mockUpdateOne = jest.fn();

    return {
        __esModule: true,
        mockFindOne,
        mockUpdateOne,
        default: {
            findOne: mockFindOne,
            updateOne: mockUpdateOne,
        },
    };
});

import SubscriptionService from './subscriptionService';
import { UserDTO } from '../interfaces';

const { mockFindOne, mockUpdateOne } = require('../db/mongo/models/User');

describe('SubscriptionService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    describe('updateAndGetSubscriberStatus', () => {
        it('updates subscription to true and returns updated user data when user is found', async () => {
            mockFindOne.mockResolvedValue({
                googleId: 'user-1',
                email: 'test@example.com',
                name: 'Test User',
                subscribed: false,
            });
            mockUpdateOne.mockResolvedValue({ modifiedCount: 1 });

            const result: UserDTO =
                await SubscriptionService.updateAndGetSubscriberStatus(
                    'user-1',
                    true
                );

            expect(mockFindOne).toHaveBeenCalledWith({ googleId: 'user-1' });
            expect(mockUpdateOne).toHaveBeenCalledWith(
                { googleId: 'user-1' },
                { subscribed: true }
            );
            expect(result).toEqual({
                email: 'test@example.com',
                googleId: 'user-1',
                name: 'Test User',
                subscribed: true,
            });
        });

        it('updates subscription to false and returns updated user data when user is found', async () => {
            mockFindOne.mockResolvedValue({
                googleId: 'user-2',
                email: 'test2@example.com',
                name: 'Test User 2',
                subscribed: true,
            });
            mockUpdateOne.mockResolvedValue({ modifiedCount: 1 });

            const result: UserDTO =
                await SubscriptionService.updateAndGetSubscriberStatus(
                    'user-2',
                    false
                );

            expect(mockFindOne).toHaveBeenCalledWith({ googleId: 'user-2' });
            expect(mockUpdateOne).toHaveBeenCalledWith(
                { googleId: 'user-2' },
                { subscribed: false }
            );
            expect(result).toEqual({
                email: 'test2@example.com',
                googleId: 'user-2',
                name: 'Test User 2',
                subscribed: false,
            });
        });

        it('throws an error if user is not found', async () => {
            mockFindOne.mockResolvedValue(null);

            await expect(
                SubscriptionService.updateAndGetSubscriberStatus(
                    'unknown-id',
                    true
                )
            ).rejects.toThrow('Error: User not found');

            expect(mockFindOne).toHaveBeenCalledWith({
                googleId: 'unknown-id',
            });
            expect(mockUpdateOne).not.toHaveBeenCalled();
        });
    });

    describe('getSubscriberStatus', () => {
        it('returns subscriber status when user is found', async () => {
            mockFindOne.mockResolvedValue({
                googleId: 'user-3',
                email: 'test3@example.com',
                name: 'Test User 3',
                subscribed: true,
            });

            const result: UserDTO =
                await SubscriptionService.getSubscriberStatus('user-3');

            expect(mockFindOne).toHaveBeenCalledWith({ googleId: 'user-3' });
            expect(result).toEqual({
                email: 'test3@example.com',
                googleId: 'user-3',
                name: 'Test User 3',
                subscribed: true,
            });
        });

        it('throws an error if user is not found', async () => {
            mockFindOne.mockResolvedValue(null);

            await expect(
                SubscriptionService.getSubscriberStatus('unknown-id')
            ).rejects.toThrow('Error: User not found');

            expect(mockFindOne).toHaveBeenCalledWith({
                googleId: 'unknown-id',
            });
        });
    });
});
