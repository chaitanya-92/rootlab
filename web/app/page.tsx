"use client";

import { useEffect, useState } from "react";

type Logo = "hex" | "bolt" | "shield";
type BuildResult = { request_id?: string; state: string; conclusion?: string | null; run_url?: string; message?: string; error?: string };

const LOGOS: { id: Logo; name: string; caption: string }[] = [
  { id: "hex", name: "H3X", caption: "Cyber hexagon" },
  { id: "bolt", name: "Bolt", caption: "Electric power" },
  { id: "shield", name: "Shield", caption: "Protected root" },
];

function LogoMark({ kind, size = 72 }: { kind: Logo; size?: number }) {
  return <svg width={size} height={size} viewBox="0 0 108 108" role="img" aria-label={`${kind} logo`}>
    {kind === "hex" && <>
      <path d="M54 16 L79 30 L79 78 L54 92 L29 78 L29 30 Z" fill="#191c29" />
      <path d="M41 39 V69 M67 39 V69 M41 54 H67" fill="none" stroke="#00e0b8" strokeWidth="6" strokeLinecap="round" />
    </>}
    {kind === "bolt" && <>
      <path d="M54 14 L88 31 L88 77 L54 94 L20 77 L20 31 Z" fill="#161925" />
      <path d="M61 28 L40 58 L53 58 L44 81 L71 47 L56 47 Z" fill="#ffc247" />
    </>}
    {kind === "shield" && <>
      <path d="M54 14 L85 26 L82 69 Q78 84 54 95 Q30 84 26 69 L23 26 Z" fill="#171b2a" />
      <path d="M40 53 L51 65 L70 42" fill="none" stroke="#55ddf1" strokeWidth="6" strokeLinecap="round" strokeLinejoin="round" />
    </>}
  </svg>;
}

const PACKAGE_PATTERN = /^[a-zA-Z_][a-zA-Z0-9_]*(?:\.[a-zA-Z_][a-zA-Z0-9_]*)+$/;

export default function Home() {
  const [name, setName] = useState("LORD H3X");
  const [packageName, setPackageName] = useState("com.lordh3x.manager");
  const [logo, setLogo] = useState<Logo>("hex");
  const [code, setCode] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<BuildResult | null>(null);

  const validName = name.trim().length > 0 && name.trim().length <= 40 && !/[\r\n<>]/.test(name);
  const validPackage = packageName.length <= 100 && PACKAGE_PATTERN.test(packageName);
  const canBuild = validName && validPackage && code.length > 0 && !submitting;

  useEffect(() => {
    const id = result?.request_id;
    if (!id || result?.state === "completed") return;
    const poll = setInterval(async () => {
      try {
        const response = await fetch("/api/status", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          cache: "no-store",
          body: JSON.stringify({ request_id: id, access_code: code }),
        });
        const data = (await response.json()) as BuildResult;
        if (response.ok) setResult((prev) => prev?.request_id === id ? { ...data, request_id: id } : prev);
      } catch { /* Keep the previously known state on transient network errors. */ }
    }, 10000);
    return () => clearInterval(poll);
  }, [result?.request_id, result?.state, code]);

  async function startBuild(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!canBuild) return;
    setSubmitting(true);
    setResult(null);
    try {
      const response = await fetch("/api/build", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ manager_name: name.trim(), package_name: packageName, logo, access_code: code }),
      });
      const data = (await response.json()) as BuildResult;
      setResult(response.ok ? data : { state: "error", message: data.error ?? "Could not start the build" });
    } catch {
      setResult({ state: "error", message: "Cannot reach the Root Lab build service." });
    } finally {
      setSubmitting(false);
    }
  }

  return <div className="appShell">
    <aside className="sidebar">
      <div className="brand"><div className="brandGlyph">R<span>✦</span></div><div><strong>ROOT LAB</strong><small>MANAGER STUDIO</small></div></div>
      <div className="navLabel">WORKSPACE</div>
      <div className="navCurrent"><span className="navIcon">◈</span> Manager Builder <span className="navActiveDot"/></div>
      <div className="navFoot">Powered by the open-source KernelSU engine.<br/>Custom branding does not grant root.</div>
    </aside>

    <main className="main">
      <header className="topbar"><div className="breadcrumb">Workspace <span>/</span> Manager Builder</div><div className="topTag"><span className="liveDot"/> Developer preview</div></header>
      <div className="content">
        <div className="headline"><div className="eyebrow">CUSTOM MANAGER BUILDER <span className="line"/></div>
          <h1>Build your <em>identity.</em></h1>
          <p>Personalize the official KernelSU manager with your own name, package ID and logo. Configure it here; Root Lab handles the build workflow.</p>
        </div>

        <div className="twoCol">
          <form className="builderPanel" onSubmit={startBuild}>
            <div className="sectionTop"><span className="sectionIndex">01</span><div><h2>Manager details</h2><p>Choose how your manager appears on Android.</p></div></div>
            <label className="fieldLabel" htmlFor="managerName">Manager display name</label>
            <input id="managerName" className="textInput" autoComplete="off" maxLength={40} required value={name} onChange={e => setName(e.target.value)} placeholder="e.g. LORD H3X" />
            {!validName && <p className="validation">Use 1–40 characters without line breaks.</p>}

            <label className="fieldLabel" htmlFor="packageName">Android package name</label>
            <input id="packageName" className="textInput codeInput" autoCapitalize="none" autoCorrect="off" spellCheck={false} maxLength={100} required value={packageName} onChange={e => setPackageName(e.target.value)} placeholder="com.example.manager" />
            {!validPackage && <p className="validation">Use a valid dotted Android application ID.</p>}
            <p className="fieldHint">Must match the identity configured in a compatible KernelSU engine for privileged management.</p>

            <div className="thinRule"/>
            <div className="sectionTop"><span className="sectionIndex">02</span><div><h2>Choose a logo</h2><p>Three options, one distinct identity.</p></div></div>
            <div className="logoOptions" role="radiogroup" aria-label="Manager logo">
              {LOGOS.map(item => <button type="button" key={item.id} role="radio" aria-checked={logo === item.id} className={`logoOption ${logo === item.id ? "chosen" : ""}`} onClick={() => setLogo(item.id)}>
                <span className="logoImage"><LogoMark kind={item.id} size={78}/></span>
                <span className="logoName">{item.name}</span><span className="logoCaption">{item.caption}</span>
                <span className="selector">{logo === item.id ? "✓" : ""}</span>
              </button>)}
            </div>

            <div className="thinRule"/>
            <div className="sectionTop"><span className="sectionIndex">03</span><div><h2>Generate your APK</h2><p>Private build access protects your workflow from unauthorized requests.</p></div></div>
            <label className="fieldLabel" htmlFor="access">Root Lab builder access code</label>
            <input id="access" className="textInput" type="password" autoComplete="off" value={code} onChange={e => setCode(e.target.value)} placeholder="Enter your private build code" required/>
            <button className="generate" disabled={!canBuild} type="submit">{submitting ? "Starting build…" : "Generate manager APK"}<span>↗</span></button>
            <div className="securityNote">This build creates an experimental APK. A matching kernel, signing identity and device validation are required for real root management.</div>

            {result && <div className={`buildResult ${result.state === "error" || result.conclusion === "failure" ? "buildError" : ""}`} role="status" aria-live="polite">
              <strong>{result.state === "completed" ? (result.conclusion === "success" ? "Build completed" : "Build failed") : result.state === "error" ? "Could not start build" : "Build request submitted"}</strong>
              <p>{result.message}</p>
              {result.run_url && <a href={result.run_url} target="_blank" rel="noopener noreferrer">View build &amp; download artifact ↗</a>}
              {result.request_id && <small>Request ID: {result.request_id}</small>}
            </div>}
          </form>

          <div className="previewColumn"><div className="previewHeader"><div><span className="sectionIndex">PREVIEW</span><h2>Live app preview</h2></div><span className="previewBadge">Android</span></div>
            <div className="phoneOuter"><div className="phoneScreen"><div className="phoneSystem"><span>9:41</span><span>▰ ▰ ◕</span></div>
              <div className="phoneBody"><div className="appIcon"><LogoMark kind={logo} size={92}/></div><h3>{name.trim() || "Your Manager"}</h3><span className="packagePreview">{packageName || "com.example.manager"}</span>
                <div className="rootStatus"><span className="statusSymbol">!</span><div><strong>Root not verified</strong><small>KernelSU engine must be installed and authorized</small></div></div>
                <div className="mockCard"><div><span>Manager version</span><strong>Experimental build</strong></div><div><span>Root backend</span><strong>Requires compatible KernelSU</strong></div></div>
                <div className="mockCredit"><a href="https://t.me/Who_colki" target="_blank" rel="noopener noreferrer">Telegram · @Who_colki ↗</a><strong>Built by LORD H3X</strong><a href="https://github.com/chaitanya-92" target="_blank" rel="noopener noreferrer">GitHub · chaitanya-92 ↗</a></div>
              </div><div className="phoneGesture"/></div></div>
            <p className="previewDisclaimer">Illustrative preview. Actual Android UI can vary by upstream KernelSU version and device.</p>
          </div>
        </div>
        <footer className="footer">ROOT LAB <span>·</span> Built by LORD H3X <span>·</span> <a href="https://t.me/Who_colki" target="_blank" rel="noopener noreferrer">Telegram</a> <span>·</span> <a href="https://github.com/chaitanya-92/rootlab" target="_blank" rel="noopener noreferrer">GitHub</a></footer>
      </div>
    </main>
  </div>;
}
