import axiosInstance from './axiosInstance';
import { BacklinkedChunk, ChatSession, Document } from './types';


// API function to get all chat sessions for the current user
export const getChatSessions = async (): Promise<ChatSession[]> => {
  try {
    const response = await axiosInstance.get('/chat/sessions');
    return response.data;
  } catch (error) {
    console.error('Error fetching chat sessions:', error);
    throw error;
  }
};


// API function to start a new chat session
export const startChatSession = async (title: string): Promise<ChatSession> => {
  try {
    const response = await axiosInstance.post('/chat/start', {
      title: title
    });
    return response.data;
  } catch (error) {
    console.error('Error starting chat session:', error);
    throw error;
  }
};


// API function to upload a file
export const uploadFile = async (file: File): Promise<Document> => {
  try {
    const formData = new FormData();
    formData.append('file', file);

    const response = await axiosInstance.post('/ingest/file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    console.log('File uploaded successfully:', response.data);
    return response.data;
  } catch (error) {
    console.error('Error uploading file:', error);
    throw error;
  }
};

// Get all documents uploaded by the user
export const fetchDocuments = async (): Promise<Document[]> => {
  const response = await axiosInstance.get<Document[]>('/documents');
  console.log('Fetched documents:', response.data);
  return response.data;
};


export const fetchRelatedChunks = async (documentId: string): Promise<BacklinkedChunk[]> => {
  const res = await axiosInstance.get(`/documents/${documentId}/related`);
  console.log('Fetched related chunks:', res.data);
  return res.data;
};


export async function getChatSession(sessionId: string): Promise<ChatSession | null> {
  try {
    const res = await axiosInstance.get<ChatSession>(`/chat/sessions/${sessionId}`);
    return res.data;
  } catch (err) {
    console.error("Failed to fetch chat session:", err);
    return null;
  }
}

/** DELETE /chat/sessions/{sessionId} */
export const deleteChatSession = async (sessionId: string): Promise<void> => {
  try {
    await axiosInstance.delete(`/chat/sessions/${sessionId}`);
  } catch (err) {
    console.error('Error deleting chat session:', err);
    throw err;
  }
};
