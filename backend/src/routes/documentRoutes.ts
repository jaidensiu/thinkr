import { Router } from 'express';
import {
    deleteDocument,
    getDocuments,
    uploadDocuments,
} from '../controllers/documentController';
import multer from 'multer';

const upload = multer();
const router = Router();

router.post('/upload', upload.single('document'), uploadDocuments);
router.delete('/delete', deleteDocument);
router.get('/retrieve', getDocuments);

export default router;
