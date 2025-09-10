import {Geist_Mono } from "next/font/google";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { AuthProvider } from "@/context/AuthContext";
const geistMono = Geist_Mono({
    variable: "--font-geist-mono",
    subsets: ["latin"],
});
export default function LoginLayout({children}:{children: React.ReactNode}) {


    return(
        <AuthProvider>
        <div className={`${geistMono.variable} font-mono min-h-screen flex` }>
            <div className="w-1/2 bg-gray-100 flex flex-col justify-between p-8">
                <Link href="/" className="group inline-block">
                   <span className="relative text-sm font-semibold">
                        Personal Brain.Ai
                        <span
                        className="absolute left-0 bottom-0 h-[1px] w-0 bg-black transition-all duration-300 group-hover:w-full"
                        />
                    </span>
                </Link>

            </div>

            {/* Right Side (Login Page Content) */}
            <div className="w-1/2 flex justify-center items-center p-6">
                <div className="absolute top-6 right-6">
                    <Link
                        href="/register"
                        className="text-sm font-medium text-gray-900 underline hover:text-black"
                    >
                        <Button className="border-2 border-solid cursor-pointer hover:bg-gray-400">
                        Sign Up
                        </Button>
                    </Link>
                </div>
                {children}
            </div>
        </div>
        </AuthProvider>

    );

}