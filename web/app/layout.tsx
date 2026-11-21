import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Root Lab — Custom KernelSU Manager",
  description: "Configure a branded KernelSU manager APK, trigger a secure build and inspect build results.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}
