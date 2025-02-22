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
    id: string;
}
