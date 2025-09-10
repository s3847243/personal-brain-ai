// middleware.ts - Simple route protection only
import { NextRequest, NextResponse } from 'next/server';

// export async function middleware(request: NextRequest): Promise<NextResponse> {
//   const protectedPaths = ['/chat'];
//   const authPaths = ['/login', '/register'];
  
//   const { pathname } = request.nextUrl;
//   const isProtectedPath = protectedPaths.some(path => pathname.startsWith(path));
//   const isAuthPath = authPaths.some(path => pathname.startsWith(path));

//   // Get tokens from cookies (just for basic checks)
//   const accessToken = request.cookies.get('accessToken')?.value;
//   const refreshToken = request.cookies.get('refreshToken')?.value;

//   // Protect routes - basic check
//   if (isProtectedPath) {
//     // If no tokens at all, redirect to login
//     if (!accessToken && !refreshToken) {
//       const loginUrl = new URL('/login', request.url);
//       loginUrl.searchParams.set('returnUrl', pathname);
//       return NextResponse.redirect(loginUrl);
//     }
    
//     // Let the page handle token validation and refresh
//     // The useAuth hook and context will handle the rest
//   }
  
//   // Redirect authenticated users away from auth pages
//   if (isAuthPath && accessToken) {
//     return NextResponse.redirect(new URL('/chat', request.url));
//   }
    
//   return NextResponse.next();
// }

// export const config = {
//   matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
// };

// export async function middleware(request: NextRequest) {
//   const accessToken = request.cookies.get('accessToken');
  
//   // List of protected paths
//   const protectedPaths = ['/chat'];
//   const isProtectedPath = protectedPaths.some(path => 
//     request.nextUrl.pathname.startsWith(path)
//   );

//   if (isProtectedPath) {
//     if (!accessToken) {
//       // No access token, redirect to login
//       return NextResponse.redirect(new URL('/login', request.url));
//     }

//     // Optional: Verify token with backend (for additional security)
//     try {
//       const verifyResponse = await fetch(`${process.env.BACKEND_URL}/auth/verify`, {
//         headers: {
//           'Cookie': request.headers.get('cookie') || ''
//         }
//       });

//       if (!verifyResponse.ok) {
//         // Token is invalid, redirect to login
//         const response = NextResponse.redirect(new URL('/login', request.url));
        
//         // Clear invalid cookies
//         response.cookies.set('accessToken', '', { maxAge: 0 });
//         response.cookies.set('refreshToken', '', { maxAge: 0 });
        
//         return response;
//       }
//     } catch (error) {
//       console.error('Token verification failed:', error);
//       // On verification error, redirect to login
//       return NextResponse.redirect(new URL('/login', request.url));
//     }
//   }

//   return NextResponse.next();
// }

// export const config = {
//   matcher: ['/chat/:path*']
// };
// Only protect specific app paths
// middleware.ts

const PROTECTED = ['/chat'];
const EXCLUDED = ['/login', '/api', '/_next', '/favicon.ico', '/assets', '/static', '/public', '/uploads','/register'];

export async function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  if (EXCLUDED.some(p => pathname.startsWith(p))) return NextResponse.next();
  if (!PROTECTED.some(p => pathname.startsWith(p))) return NextResponse.next();

  // Quick cookie presence check
  const hasAccess = !!req.cookies.get('accessToken')?.value;
  const hasRefresh = !!req.cookies.get('refreshToken')?.value; // path=/api/auth, still visible to middleware
  if (!hasAccess && !hasRefresh) {
    return NextResponse.redirect(new URL('/login', req.url));
  }
  // if (!hasAccess) return NextResponse.redirect(new URL('/login', req.url));
  const cookieHeader = req.headers.get('cookie') ?? '';

  // Authoritative check: call your backend via the SAME ORIGIN proxy
  const verifyRes = await fetch(new URL('/api/auth/verify', req.url), {
    method: 'GET',
    headers: { cookie: req.headers.get('cookie') ?? '' },
    cache: 'no-store',
  });

  if (verifyRes.ok) return NextResponse.next();
  // 3) Try refresh once (only if we have a refresh cookie)
  if (hasRefresh) {
    const refresh = await fetch(`${origin}/api/auth/refresh`, {
      method: 'POST',
      headers: { cookie: cookieHeader },
      cache: 'no-store',
    });

    if (refresh.ok || refresh.status === 204) {
      // Backend just set a new access cookie in the response.
      // Re-verify to be sure.
      const verify2 = await fetch(`${origin}/api/auth/verify`, {
        method: 'GET',
        headers: { cookie: cookieHeader },
        cache: 'no-store',
      });
      if (verify2.ok) return NextResponse.next();
    }
  }

  // 4) Give up → login (do NOT clear cookies here)
  return NextResponse.redirect(new URL('/login', req.url));
}

export const config = {
  matcher: ['/chat/:path*'],
};