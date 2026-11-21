import { timingSafeEqual } from "node:crypto";

export const OWNER = "chaitanya-92";
export const REPO = "rootlab";
export const WORKFLOW = "kernelsu-manager-build.yml";

export const LOGOS = ["hex", "bolt", "shield"] as const;
export type Logo = (typeof LOGOS)[number];

const PACKAGE_PATTERN = /^[a-zA-Z_][a-zA-Z0-9_]*(?:\.[a-zA-Z_][a-zA-Z0-9_]*)+$/;

export function validateBrand(input: unknown):
  | { ok: true; value: { manager_name: string; package_name: string; logo: Logo } }
  | { ok: false; error: string } {
  if (typeof input !== "object" || input === null) return { ok: false, error: "Invalid request" };
  const b = input as Record<string, unknown>;
  if (typeof b.manager_name !== "string" || !b.manager_name.trim() || b.manager_name.trim().length > 40 || /[\r\n<>]/.test(b.manager_name))
    return { ok: false, error: "Manager name must be 1–40 characters without line breaks" };
  if (typeof b.package_name !== "string" || b.package_name.length > 100 || !PACKAGE_PATTERN.test(b.package_name))
    return { ok: false, error: "Enter a valid Android package name (e.g. com.lordh3x.manager)" };
  if (typeof b.logo !== "string" || !LOGOS.includes(b.logo as Logo))
    return { ok: false, error: "Choose one of the three logos" };
  return { ok: true, value: { manager_name: b.manager_name.trim(), package_name: b.package_name, logo: b.logo as Logo } };
}

export function authorized(code: unknown): boolean {
  const expected = process.env.ROOTLAB_ACCESS_CODE;
  if (typeof code !== "string" || !expected || expected.length < 12) return false;
  const a = Buffer.from(code, "utf8");
  const b = Buffer.from(expected, "utf8");
  return a.length === b.length && timingSafeEqual(a, b);
}

export function ready(): boolean {
  return Boolean(process.env.GITHUB_ACTIONS_TOKEN && process.env.ROOTLAB_ACCESS_CODE && process.env.ROOTLAB_ACCESS_CODE.length >= 12);
}

export function ghHeaders(): HeadersInit {
  return {
    Authorization: `Bearer ${process.env.GITHUB_ACTIONS_TOKEN}`,
    Accept: "application/vnd.github+json",
    "X-GitHub-Api-Version": "2022-11-28",
    "Content-Type": "application/json",
  };
}

export function failureResponse(error: string, status: number): Response {
  return Response.json({ error }, { status, headers: { "Cache-Control": "no-store" } });
}
