jest.mock('../db/mongo/models/User', () => {
    const mockFindOne = jest.fn();
    const mockSave = jest.fn().mockResolvedValue(undefined);

    function MockUser(this: any, userData: any) {
        this.email = userData.email;
        this.name = userData.name;
        this.googleId = userData.googleId;
        this.subscribed = userData.subscribed;
        this.save = mockSave;
    }

    return {
        __esModule: true,
        mockFindOne,
        mockSave,
        default: Object.assign(MockUser, {
            findOne: mockFindOne,
        }),
    };
});

import UserAuthService from './userAuthService';
import { AuthPayload } from '../interfaces';

const { mockFindOne, mockSave } = require('../db/mongo/models/User');

describe('UserAuthService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('returns existing user data when user is found', async () => {
        mockFindOne.mockResolvedValue({
            email: 'existing@example.com',
            name: 'Existing User',
            googleId: 'existing-google-id',
            subscribed: true,
        });

        const payload: AuthPayload = {
            email: 'existing@example.com',
            name: 'Existing User',
            googleId: 'existing-google-id',
        };

        const result = await UserAuthService.findCreateUser(payload);

        expect(mockFindOne).toHaveBeenCalledWith({
            googleId: 'existing-google-id',
        });
        expect(mockSave).not.toHaveBeenCalled();
        expect(result).toEqual({
            email: 'existing@example.com',
            name: 'Existing User',
            googleId: 'existing-google-id',
            subscribed: true,
        });
    });

    it('creates and saves a new user when none is found', async () => {
        mockFindOne.mockResolvedValue(null);

        const payload: AuthPayload = {
            email: 'new@example.com',
            name: 'New User',
            googleId: 'new-google-id',
        };

        const result = await UserAuthService.findCreateUser(payload);

        expect(mockFindOne).toHaveBeenCalledWith({ googleId: 'new-google-id' });
        expect(mockSave).toHaveBeenCalled();
        expect(result).toEqual({
            email: 'new@example.com',
            name: 'New User',
            googleId: 'new-google-id',
            subscribed: false,
        });
    });
});
