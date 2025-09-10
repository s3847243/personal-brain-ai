export interface ChatSession {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessage {
  id: string;
  sessionId: string;
  content: string;
  role: 'user' | 'assistant';
  createdAt: string;
}
export interface Document {
  id: string;
  userId: string;
  originalFilename: string;
  storageKey: string;
  publicUrl: string;
  sourceType: 'FILE' | 'URL';
  createdAt: string;
}

export interface BacklinkedChunk {
  sourceChunkId: string;
  relatedChunkId: string;
  relatedText: string | null; 
  relatedDocumentTitle: string | null; 

}

export type User = {
  id: string;
  fullName: string;
  email: string;
  jobTitle?: string;
};

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  user: User;
  tokens: AuthTokens;
  message: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken?: string;
  expiresIn?: number;
  tokenType?: string;
}

export interface LogoutResponse {
  message: string;
}

export interface ApiError {
  error: string;
  message?: string;
  statusCode?: number;
}

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

export type RegisterResponse = {
  id: string;
  fullName: string;
  email: string;
  // any other fields your API returns
};