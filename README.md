# Root Lab

**Two root managers. One automation platform.**

Root Lab is a work-in-progress web platform to independently configure, generate, sign and verify Android **KernelSU Manager** and **KernelSU Next Manager** APKs.

## Architecture

| | KernelSU | KernelSU Next |
|---|---|---|
| Upstream | https://github.com/tiann/KernelSU | https://github.com/KernelSU-Next/KernelSU-Next |
| Branch tracked in initial adapter | `main` | `dev` |
| Project source | separate, pinned commit | separate, pinned commit |
| Configuration | independent adapter | independent adapter |
| Native compilation | upstream-specific | upstream-specific |
| Output | separately verified APK | separately verified APK |

**These are not merged or interchangeable products.**

## Status

Initial technical audit and implementation are underway. Root Lab is **not yet a production APK build service**. No device compatibility guarantee is made for a re-signed manager APK. Kernel-side manager certificate matching must be verified against the actual kernel build. User signing keys must not be passed to untrusted build workflows.

Related research project: https://github.com/chaitanya-92/Android_Exploits

## License and upstream obligations

KernelSU and KernelSU Next have GPL licensing obligations. Any redistribution of modified managers must retain applicable notices and provide corresponding source where required.

