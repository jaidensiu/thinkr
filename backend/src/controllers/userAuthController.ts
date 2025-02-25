import { Request, Response } from 'express';
import UserService from '../services/userAuthService';
import { Result, UserDTO } from '../interfaces';

/**
 * Handles user login with google auth and jwt
 */
export const userAuthLogin = async (
    req: Request,
    res: Response
): Promise<void> => {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        res.status(400).json({
            message: 'Authorization header is missing or invalid',
        } as Result);
        return;
    }

    const idToken = authHeader.split(' ')[1];
    const googleUser = await UserService.verifyGoogleToken(idToken);

    if (!googleUser) {
        res.status(401).json({
            message: 'Invalid Google token',
        } as Result);
        return;
    }

    const user: UserDTO = await UserService.findCreateUser(googleUser);
    const token = UserService.generateToken(user.userId);

    res.status(200).json({
        data: { token, user },
    } as Result);
};
