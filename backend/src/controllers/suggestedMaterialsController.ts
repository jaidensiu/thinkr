import { Request, Response } from 'express';
import { Result } from '../interfaces';
import SuggestedMaterialsService from '../services/suggestedMaterialsService';

/**
 * Handles retrieving suggested study materials for a user
 */
export const getSuggestedMaterials = async (
    req: Request,
    res: Response
): Promise<void> => {
    try {
        const userId = req.query.userId as string;
        const limit = req.query.limit ? parseInt(req.query.limit as string) : 5;

        if (!userId) {
            res.status(400).json({
                message: 'You must provide a userId identifier',
            } as Result);
            return;
        }

        const suggestedMaterials = await SuggestedMaterialsService.getSuggestedMaterials(
            userId,
            limit
        );

        res.status(200).json({
            data: suggestedMaterials,
        } as Result);
    } catch (error) {
        console.error('Error retrieving suggested materials:', error);
        res.status(500).json({
            message: 'Internal server error',
        } as Result);
    }
}; 