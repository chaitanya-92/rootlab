# Root Lab — KernelSU-based branded manager (NOT YET VERIFIED)

This is the correct path to a real KernelSU manager rather than the
diagnostic-only Android scaffold in `app/`.

## Default branding

- Display name: `LORD H3X`
- Android applicationId: `com.lordh3x.manager`
- Logos: `hex`, `bolt`, `shield`
- Telegram: https://t.me/Who_colki
- GitHub: https://github.com/chaitanya-92
- Credit: Built by LORD H3X

All three logos are editable Android vector drawables created from the
selection parameter. Do not rename upstream Kotlin/JNI package namespaces;
the official build already provides `KSU_NAME` and `KSU_PACKAGE_NAME`
Gradle properties. Full KernelSU manager source, native bridge, ksud and kernel
must be retained from a **pinned and verified** tiann/KernelSU revision.

## Example of local build preparation

```sh
git clone https://github.com/tiann/KernelSU.git KernelSU
cd KernelSU
git checkout <reviewed-upstream-commit-sha>
cd ..
python3 tools/brand_kernelsu.py --upstream KernelSU --name "LORD H3X" --package com.lordh3x.manager --logo hex
```

KernelSU upstream requires a Rust-built `ksud` copied into the manager's
`jniLibs` as `libksud.so` before release compilation. Follow the exact
requirements in that checked-out revision's AGENTS.md and official build docs.

Gradle manager invocation (after ksud is ready, and using secure release signing):

```sh
cd KernelSU/manager
./gradlew assembleRelease -PKSU_NAME="LORD H3X" -PKSU_PACKAGE_NAME=com.lordh3x.manager
```

**Important:** a debug signature or arbitrary release signature does not get
exclusive management rights just by modifying the package name. Use a dedicated
release signing key securely, NOT checked into Git, and derive the kernel's
expected manager certificate identity using the public DER certificate:

```sh
keytool -exportcert -rfc -keystore /secure/manager.jks -alias youralias > /secure/cert.pem
openssl x509 -in /secure/cert.pem -outform DER -out /secure/cert.der
python3 tools/kernel_identity.py --package com.lordh3x.manager --cert-der /secure/cert.der --output /secure/kernel-identity.mk
```

KernelSU's `kernel/Kbuild` uses `KSU_PACKAGE_NAME`,
`KSU_EXPECTED_SIZE`, and `KSU_EXPECTED_HASH` for the manager identity.
Supply those three values to the **matching kernel build environment**.
Do not change signature checks or claim it will authenticate against an
unmodified upstream kernel. Verify the resulting signed APK's certificate with
`apksigner verify --print-certs` and compare the actual DER certificate
hash/size to the kernel build manifest.

## One-click workflow (debug-only)

GitHub Actions -> **Build branded upstream KernelSU Manager (experimental)** -> Run workflow:
- Enter manager name (defaults to LORD H3X)
- Enter custom application ID (defaults to com.lordh3x.manager)
- Choose **hex**, **bolt**, or **shield**

The workflow checks out pinned KernelSU commit `2a60b2893b8d0d885d1ceff2287d8ad854f0e0f4`, patches both upstream UI themes and vector icons, compiles arm64 `ksud`, and attempts an original manager debug APK build.

**Debug APK is NOT authorized to manage root by default.** Do not flash arbitrary kernel/LKM images.

## Unfinished, mandatory integration work

- Real KernelSU upstream is obtained as a pinned checkout by the experimental build workflow rather than copied or vendored into the rootlab repository.
- No release-signing credentials are configured.
- No native ksud or actual kernel/LKM artifact is built here.
- No validated phone/device/partition combination is configured.
- No flashing workflow; do not attempt a generic image across devices.
- No real device test of root-status reporting, superuser policies, or modules.
- The branding tool now patches BOTH upstream HomeMaterial.kt and HomeMiuix.kt so the real manager replaces Support Us / Learn KernelSU with Telegram, Built by LORD H3X, and GitHub. Actual upstream compilation and on-device rendering are still unverified.

KernelSU is licensed under GPL obligations; preserve license notices and
publish complete corresponding source for distributed modified binaries.

Never label the current APK Root Working without real kernel communication.
