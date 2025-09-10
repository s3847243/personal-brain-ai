// lib/api/userApi.ts

import axiosInstance from "./axiosInstance";

export async function registerUser(payload: {
  fullName: string;
  email: string;
  password: string;
}): Promise<{ success: boolean; message?: string }> {
  try {
    const response = await fetch("http://localhost:8080/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || "Registration failed");
    }

    return { success: true };
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'An unknown error occurred';
    return { success: false, message };
  }
}

export async function loginUser(payload: {
  email: string;
  password: string;
}){
  try {
    const response = await fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(payload),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Login failed");
    }
    console.log("Login response data:", data);

    return {data: data.user, success: true};
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'An unknown error occurred';
    return { success: false, message };
  }
}


export async function logoutRequest(): Promise<void> {
  await axiosInstance.post("/api/auth/logout");
}