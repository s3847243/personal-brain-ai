// app/not-found.tsx
'use client';

import { useEffect } from 'react';
import { usePathname } from 'next/navigation';
import Link from 'next/link';

export default function NotFound() {
  const pathname = usePathname();

  useEffect(() => {
    console.error("404 Error: User attempted to access non-existent route:", pathname);
  }, [pathname]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 dark:bg-gray-900">
      <div className="text-center px-4">
        <h1 className="text-5xl font-bold mb-4 text-gray-900 dark:text-white">404</h1>
        <p className="text-xl text-gray-700 dark:text-gray-300 mb-4">Oops! Page not found</p>
        <Link
          href="/"
          className="text-blue-600 hover:text-blue-800 underline"
        >
          Return to Home
        </Link>
      </div>
    </div>
  );
}
