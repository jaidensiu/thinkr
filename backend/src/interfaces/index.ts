/**
 * Represents API result
 */
export interface Result {
    message?: string;
    data?: any;
}

/**
 * Represents a user for data transfer between frontend <-> backend
 */
export interface UserDTO {
    email: string;
    name?: string;
    googleId?: string;
    userId: string;
}

/**
 * Represents a document for data transfer between frontend <-> backend
 */
export interface DocumentDTO {
    url?: string;
    documentId: string;
    uploadTime: string;
}

/**
 * Represents a flashcard for data transfer between frontend <-> backend
 */
export interface FlashCardDTO {
    front: string;
    back: string;
}
