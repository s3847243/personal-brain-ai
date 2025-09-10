"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { registerUser } from "@/lib/api";
export default function RegisterForm() {
  const router = useRouter();
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState(""); 
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
      e.preventDefault();
      if (password !== confirmPassword) {
        setError("Passwords do not match");
        return;
      }
      setError("");
      const result = await registerUser({
      fullName,
      email,
      password,
    });

    if (!result.success) {
      setError(result.message || "Registration failed");
      return;
    }
    
    // Navigate to login page after successful registration
    router.push("/login");
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-md">
      <Card>
        <CardHeader>
          <CardTitle>Create an account</CardTitle>
          <CardDescription>Fill in the details to register</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div>
              <Label htmlFor="name">Full Name</Label>
              <Input id="name" type="text" required value={fullName} onChange={(e) => setFullName(e.target.value)} />
            </div>
            <div>
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div>
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="confirmPassword">Confirm Password</Label>
              <Input
                id="confirmPassword"
                type="password"
                required
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              {error && <p className="text-sm text-red-600 mt-1">{error}</p>}
            </div>

            <Button type="submit" className="w-full hover:bg-gray-200 cursor-pointer">
              Register
            </Button>
          </form>

          <div className="mt-4 text-center text-sm text-gray-500">
            Already have an account?{" "}
            <a href="/login" className="underline underline-offset-4 text-sm">
              Login
            </a>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
