import { Router } from 'express';
import { getSuggestedMaterials } from '../controllers/suggestedMaterialsController';

const router = Router();

router.get('/', getSuggestedMaterials);

export default router; 