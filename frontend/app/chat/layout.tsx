
import { SidebarProvider } from "@/components/ui/sidebar"
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import ClientLayout from "./ClientLayout";
import { getCurrentUserServer } from "@/lib/currentUser";
import { UserProvider } from "@/context/UserContext";
export default async function ProtectedLayout({ children }: { children: React.ReactNode }) {
  const cookieStore = await cookies();
  const token = cookieStore.get("accessToken");
  if (!token) redirect("/login"); 
  const user = await getCurrentUserServer();
  return (
    
      <UserProvider initialUser={user}>
        <SidebarProvider defaultOpen={true}>
          <ClientLayout>{children}</ClientLayout>
        </SidebarProvider>
      </UserProvider>

  );
}