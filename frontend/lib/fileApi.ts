import axiosInstance from './axiosInstance';

export const deleteDocument = async (documentId: string): Promise<void> => {
  try {
    await axiosInstance.delete(`/documents/${documentId}`);
  } catch (err) {
    console.error('Error deleting document:', err);
    throw err;
  }
};