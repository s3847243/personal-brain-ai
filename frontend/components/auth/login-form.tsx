"use client";

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useRouter } from "next/navigation"
import { useState } from "react"
// import { useAuth } from "@/hooks/useAuth";
// import { useAuthContext } from "@/context/AuthContext";
import { useAuth } from "@/context/AuthContext";

export default function LoginForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const router = useRouter();
  const [error, setError] = useState('');
  const { login} = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    setError('');

    try {
      await login(email, password);
      console.log("Login successful");
      router.push('/chat');
    } catch (err) {
      setError('Login failed. Please check your credentials.' );
      console.error(err);
    } 
  };

  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <Card>
        <CardHeader>
          <CardTitle>Login to your account</CardTitle>
          <CardDescription>
            Enter your email below to login to your account
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <div className="flex flex-col gap-6">
              <div className="grid gap-3">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="m@example.com"
                  required
                  className="select-text"
                  style={{ 
                    userSelect: 'text',
                    WebkitUserSelect: 'text',
                    MozUserSelect: 'text',
                    msUserSelect: 'text'
                  }}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              <div className="grid gap-3">
                <div className="flex items-center">
                  <Label htmlFor="password">Password</Label>

                </div>
                <Input id="password" type="password" required className="select-text"
                  style={{ 
                    userSelect: 'text',
                    WebkitUserSelect: 'text',
                    MozUserSelect: 'text',
                    msUserSelect: 'text'
                  }} 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  />
                
                  <a
                    href="#"
                    className="justify-center inline-block text-sm underline-offset-4 hover:underline"
                  >
                    Forgot your password?
                  </a>
              </div>
               {error && (
                <div className="text-red-600 text-sm text-center">{error}</div>
              )}
              <div className="flex flex-col gap-3">
                <Button type="submit" className="w-full cursor-pointer hover:bg-gray-400 transition-colors">
                  Login
                </Button>
                {/* <Button variant="outline" className="w-full">
                  Login with Google
                </Button> */}
              </div>
            </div>
            <div className="mt-4 text-center text-sm">
              Don&apos;t have an account?{" "}
              <a href="/register" className="underline underline-offset-4">
                Sign up
              </a>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
