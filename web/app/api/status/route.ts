import { authorized, failureResponse, ghHeaders, ready, OWNER, REPO, WORKFLOW } from "../../../lib/build";

export const runtime = "nodejs";
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export async function POST(req: Request) {
  if (!ready()) return failureResponse("Build service not configured by administrator", 503);
  let data: Record<string, unknown>;
  try { data = await req.json(); } catch { return failureResponse("Invalid JSON body", 400); }
  if (!authorized(data?.access_code)) return failureResponse("Invalid build access code", 401);
  const id = data?.request_id;
  if (typeof id !== "string" || !UUID.test(id)) return failureResponse("Invalid build request ID", 400);

  const response = await fetch(
    `https://api.github.com/repos/${OWNER}/${REPO}/actions/workflows/${WORKFLOW}/runs?event=repository_dispatch&per_page=100`,
    { headers: ghHeaders(), cache: "no-store", signal: AbortSignal.timeout(15000) },
  ).catch(() => null);
  if (!response?.ok) return failureResponse("Could not fetch build status", 502);

  const payload = (await response.json()) as { workflow_runs?: Array<{
    id: number; display_title: string; status: string; conclusion: string | null;
    html_url: string; artifacts_url: string;
  }> };
  const run = payload.workflow_runs?.find((r) => r.display_title === `Root Lab / ${id}`);
  if (!run) return Response.json({ state: "queued", message: "Waiting for GitHub to start the workflow" }, { headers: { "Cache-Control": "no-store" } });
  return Response.json({
    state: run.status, conclusion: run.conclusion,
    run_url: run.html_url, run_id: run.id,
    message: run.status === "completed"
      ? (run.conclusion === "success" ? "Experimental APK build succeeded. Open workflow artifacts to obtain the APK." : "APK build failed. Open the workflow log.")
      : "Build is processing.",
  }, { headers: { "Cache-Control": "no-store" } });
}
