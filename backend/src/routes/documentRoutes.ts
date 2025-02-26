import { Router } from 'express';
import {
    deleteDocuments,
    getDocuments,
    uploadDocuments,
} from '../controllers/documentController';
import multer from 'multer';

const upload = multer();
const router = Router();

router.post('/upload', upload.single('document'), uploadDocuments);
router.delete('/delete', deleteDocuments);
router.get('/retrieve', getDocuments);

export default router;
