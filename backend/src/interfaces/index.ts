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
    subscribed: boolean;
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
    userId: string;
    documentName: string;
    flashcards: FlashCard[];
}

export interface FlashCard {
    front: string;
    back: string;
}

/**
 * Represents a multiple choice quiz for data transfer between frontend <-> backend
 */

export interface QuizDTO {
    userId: string;
    documentName: string;
    quiz: Quiz[];
}

export interface Quiz {
    question: string;
    answer: string;
    options: {
        [key: string]: string;
    };
}
