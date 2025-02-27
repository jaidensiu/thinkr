/**
 * Represents API result
 */
export interface Result {
    message?: string;
    data?: any;
}

/**
 * Auth info payload for when user logs in
 */
export interface AuthPayload {
    googleId: string;
    name: string;
    email: string;
}

/**
 * Represents a user for data transfer between frontend <-> backend
 */
export interface UserDTO {
    email: string;
    name?: string;
    googleId: string;
    subscribed: boolean;
}

/**
 * Represents a document for data transfer between frontend <-> backend
 */
export interface DocumentDTO {
    documentId: string;
    uploadTime: string;
    activityGenerationComplete: boolean;
}

/**
 * Represents a flashcard for data transfer between frontend <-> backend
 */
export interface FlashCardDTO {
    userId: string;
    documentId: string;
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
    documentId: string;
    quiz: Quiz[];
}

export interface Quiz {
    question: string;
    answer: string;
    options: {
        [key: string]: string;
    };
}
