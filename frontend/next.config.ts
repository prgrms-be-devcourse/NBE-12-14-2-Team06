import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  reactCompiler: true,
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${process.env.NEXT_PUBLIC_BASE_URL}/api/:path*`,
        //예를 들면 브라우저가 /api/v1/posts를 부르면, Next가 http://localhost:8080/api/v1/posts로 대신 전달
      },
    ];
  },
};

export default nextConfig;