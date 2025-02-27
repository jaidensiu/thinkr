import { Request, Response } from 'express';
import UserService from '../services/userAuthService';
import { AuthPayload, Result, UserDTO } from '../interfaces';

/**
 * Handles user login with google auth and jwt
 */
export const userAuthLogin = async (
    req: Request,
    res: Response
): Promise<void> => {
    const { googleId, name, email } = req.body;

    if (!googleId || !name || !email) {
        res.status(400).json({
            message: 'googleId, name, or email is missing or invalid',
        } as Result);
        return;
    }

    const authPayload: AuthPayload = {
        googleId,
        name,
        email
    };

    const user: UserDTO = await UserService.findCreateUser(authPayload);

    res.status(200).json({
        data: { user },
    } as Result);
};
