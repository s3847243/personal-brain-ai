import axiosInstance from "./axiosInstance";

export const deleteUserAccount = async (): Promise<void> => {
  try {
    await axiosInstance.delete(`/user/delete`);
  } catch (err) {
    console.error('Error deleting user account:', err);
    throw err;
  }
};