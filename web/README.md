# Root Lab Web UI

This is the **application UI layer**, separate from the Android APK.

## Development

```bash
cd web
npm install
cp .env.example .env.local
npm run dev
```

Open http://localhost:3000.

## Secure server configuration

Set `GITHUB_ACTIONS_TOKEN` to a fine-grained GitHub token for
`chaitanya-92/rootlab` with repository **Contents: read** and
**Actions: write** (GitHub repository dispatch endpoint may also require
Contents: write; grant the minimum repository permissions needed for
`POST /repos/{owner}/{repo}/dispatches`).

Set `ROOTLAB_ACCESS_CODE` to a long private value (at least 12 bytes).
Both stay on the server; **never** use a `NEXT_PUBLIC_` variable.
The UI asks the operator for the access code; it is sent only by HTTPS
to the application's own API.

The server validates inputs before dispatching the GitHub event.
The browser never receives the GitHub token. A GitHub workflow on main
uses the event payload to select name/package/logo, build the APK and
associate the workflow run with a generated request ID.

Use appropriate application-level authentication, quotas, audit logging
and rate limiting before offering public build access to unknown users.
The access-code gate is appropriate only for a private operator prototype.

## Deploying

Deploy this `web` folder as a Next.js project on a server that supports
Node.js API routes; configure the two secret environment variables.
Merely committing the web frontend to GitHub does **not** publish it.
Connect the deployment to the repository, with Root Directory = `web`.

## Important limitations

An APK produced by the upstream KernelSU debug workflow is not
kernel-authenticated and **cannot** confer root on an unrooted device.
A matching released kernel/LKM and manager certificate binding remain
necessary for exclusive management and root operations.
