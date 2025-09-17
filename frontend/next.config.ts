/** @type {import('next').NextConfig} */
const API_BASE = process.env.NEXT_PUBLIC_API_BASE; // e.g. https://3-106-166-175.sslip.io or https://personal-brain-ai.onrender.com

const nextConfig = {
  async rewrites() {
    if (!API_BASE) return [];
    return [
      { source: '/api/:path*', destination: `${API_BASE}/api/:path*` },
    ];
  },
};

module.exports = nextConfig;

