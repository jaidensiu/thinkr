import request from 'supertest';
import express from 'express';
import { AuthPayload, Result } from '../../interfaces';

jest.mock('../../db/mongo/models/User', () => {
    const mockFindOne = jest.fn();
    const mockSave = jest.fn().mockResolvedValue(undefined);

    function MockUser(this: any, userData: any) {
        this.email = userData.email;
        this.name = userData.name;
        this.googleId = userData.googleId;
        this.subscribed = userData.subscribed || false;
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

import authRouter from '../../routes/userAuthRoutes';
const { mockFindOne, mockSave } = require('../../db/mongo/models/User');

const app = express();
app.use(express.json());
app.use('/auth', authRouter);

describe('User Auth Login (Mocked)', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    // Input: Valid googleId, name, and email for a new user
    // Expected status code: 200
    // Expected behavior: User.findOne called, new user saved
    // Expected output: user object with provided data and subscribed=false
    it('should create a new user when none exists', async () => {
        mockFindOne.mockResolvedValue(null);

        const payload: AuthPayload = {
            googleId: 'new-google-id',
            name: 'New User',
            email: 'new@example.com',
        };

        const response = await request(app)
            .post('/auth/login')
            .send(payload)
            .expect(200);

        const result = response.body as Result;
        expect(result.data).toBeDefined();
        expect(result.data.user).toBeDefined();
        expect(result.data.user.googleId).toBe('new-google-id');
        expect(result.data.user.name).toBe('New User');
        expect(result.data.user.email).toBe('new@example.com');
        expect(result.data.user.subscribed).toBe(false);

        expect(mockFindOne).toHaveBeenCalledWith({ googleId: 'new-google-id' });
        expect(mockSave).toHaveBeenCalled();
    });

    // Input: Valid googleId, name, and email for an existing user
    // Expected status code: 200
    // Expected behavior: User.findOne called, no save operation
    // Expected output: existing user object with correct subscription status
    it('should return existing user when found', async () => {
        mockFindOne.mockResolvedValue({
            email: 'existing@example.com',
            name: 'Existing User',
            googleId: 'existing-google-id',
            subscribed: true,
        });

        const payload: AuthPayload = {
            googleId: 'existing-google-id',
            name: 'Existing User',
            email: 'existing@example.com',
        };

        const response = await request(app)
            .post('/auth/login')
            .send(payload)
            .expect(200);

        const result = response.body as Result;
        expect(result.data).toBeDefined();
        expect(result.data.user).toBeDefined();
        expect(result.data.user.googleId).toBe('existing-google-id');
        expect(result.data.user.name).toBe('Existing User');
        expect(result.data.user.email).toBe('existing@example.com');
        expect(result.data.user.subscribed).toBe(true);

        expect(mockFindOne).toHaveBeenCalledWith({
            googleId: 'existing-google-id',
        });
        expect(mockSave).not.toHaveBeenCalled();
    });

    // Input: Missing required fields (googleId, name, or email)
    // Expected status code: 400
    // Expected behavior: validation error, no database queries
    // Expected output: error message
    it('should return 400 when required fields are missing', async () => {
        await request(app)
            .post('/auth/login')
            .send({
                name: 'Test User',
                email: 'test@example.com',
            })
            .expect(400);

        await request(app)
            .post('/auth/login')
            .send({
                googleId: 'test-id',
                email: 'test@example.com',
            })
            .expect(400);

        await request(app)
            .post('/auth/login')
            .send({
                googleId: 'test-id',
                name: 'Test User',
            })
            .expect(400);

        expect(mockFindOne).not.toHaveBeenCalled();
    });
});
