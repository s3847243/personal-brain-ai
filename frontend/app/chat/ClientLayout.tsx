// app/chat/ClientLayout.tsx
"use client";

import { AppSidebar } from "@/components/chat/app-sidebar";
import FileViewer from "@/components/files/File-Viewer";
import {SidebarTrigger, useSidebar } from "@/components/ui/sidebar";
import { getChatSessions, deleteChatSession } from "@/lib/chatApi";
import { ChatSession } from "@/lib/types";
import { usePathname } from "next/navigation";
import { useRouter } from "next/navigation";
import { useState, useEffect } from "react";
export default  function ClientLayout({ children }: { children: React.ReactNode }) {
  const { open } = useSidebar();
  const [chatSessions, setChatSessions] = useState<ChatSession[]>([]);
  const pathname = usePathname();
  const router = useRouter();
  // Fetch chat sessions when component mounts
  useEffect(() => {
    fetchChatSessions();
  }, []);

  // add an api here to get all the chats
  const fetchChatSessions = async () => {
    try {
      const sessions = await getChatSessions();
      setChatSessions(sessions);

    } catch (error) {
      console.error('Failed to fetch chat sessions:', error);
    }
  };
  const handleDeleteSession = async (sessionId: string) => {
    const prev = chatSessions;
    setChatSessions(prev => prev.filter(s => s.id !== sessionId));

    try {
      await deleteChatSession(sessionId);
      if (pathname === `/chat/${sessionId}`) {
        router.replace("/chat");
      }
    } catch (e) {
      console.error("Delete failed:", e);
      setChatSessions(prev); 
    }
  };
  return (
    <div className="flex h-screen w-full overflow-hidden">
      
      <div className="flex">
          <div className={`${open ? 'w-60' : 'w-0'}  border-gray-200 bg-white transition-all duration-300 overflow-hidden`}>
            <AppSidebar chatSessions={chatSessions} onDeleteSession={handleDeleteSession} />
          </div>
          
          <div className= {`flex items-start justify-center pt-3 bg-white border-r border-gray-200 `} >
            <SidebarTrigger />
          </div>
      </div>

    
      <div className={`${open ? 'flex-[4]' : 'flex-[5]'} transition-all duration-300 flex flex-col relative`}>
       
        <main className="flex-1 overflow-auto">
          {children}
        </main>
      </div>

      <div className="flex-[1.5] border-l border-gray-200 bg-white overflow-auto">
        <FileViewer />
      </div>
    </div>
  );
}
