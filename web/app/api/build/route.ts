import { randomUUID } from "node:crypto";
import { authorized, failureResponse, ghHeaders, ready, OWNER, REPO, validateBrand } from "../../../lib/build";

export const runtime = "nodejs";

export async function POST(req: Request) {
  if (!ready()) return failureResponse("Build service not configured by administrator", 503);
  let data: unknown;
  try { data = await req.json(); } catch { return failureResponse("Invalid JSON body", 400); }
  const body = data as Record<string, unknown>;
  if (!authorized(body?.access_code)) return failureResponse("Invalid build access code", 401);
  const validated = validateBrand(body);
  if (!validated.ok) return failureResponse(validated.error, 400);

  // Correlate the GitHub Actions run using its unique workflow run-name.
  const request_id = randomUUID();
  const response = await fetch(`https://api.github.com/repos/${OWNER}/${REPO}/dispatches`, {
    method: "POST",
    headers: ghHeaders(),
    body: JSON.stringify({
      event_type: "rootlab-manager-build",
      client_payload: { ...validated.value, request_id },
    }),
    cache: "no-store",
    signal: AbortSignal.timeout(15000),
  }).catch(() => null);

  if (!response?.ok) {
    return failureResponse("Could not start GitHub build. Check token Actions permission and workflow configuration.", 502);
  }
  return Response.json({ request_id, state: "queued", message: "Build request submitted. This is an experimental APK, not root-authorized." }, {
    status: 202, headers: { "Cache-Control": "no-store" },
  });
}
