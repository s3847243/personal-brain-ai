
import axios from "axios";
import { cookies } from "next/headers";

const serverAxios = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});
export async function getCurrentUserServer() {
  const cookieStore = await cookies();
  const token = cookieStore.get("accessToken")?.value;
  if (!token) return null;

  const res = await serverAxios.get("/api/user/me", {
    headers: {
      Cookie: `accessToken=${token}`,
    },
  });

  return res.data;
}