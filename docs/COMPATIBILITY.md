# Root Lab device compatibility contract

## Scope of this prototype

- The single Android APK reads Android version, CPU ABI, device and kernel metadata without requiring manual input.
- User-triggered `su -c id` tests *existing* root grants. It does not establish KernelSU identity, manager trust, or module capability.
- The KernelSU and KernelSU Next adapters are intentionally **NOT_INTEGRATED**. The UI must never claim their permissions/modules features work.
- No boot flashing, root exploit, key or signing-certificate bypass, or automatic root installation is implemented.

## Gates before real root management

1. Pin and audit the upstream manager + kernel API versions independently for KernelSU and KernelSU Next.
2. Implement each backend's authenticated manager interface and signature/package validation according to that upstream's API.
3. Add tests for authorized and unauthorized managers and certificate rotation.
4. Only offer kernel/LKM installation where the exact device and partition layout are known and supported. Require user consent and tested recovery instructions.
5. Test superuser grant/revoke, modules, persistence after reboot, and recovery on real devices.
6. Package architecture-specific dependencies correctly, publish corresponding GPL source and license notices, and secure user signing keys.
7. Present a compatibility result of SUPPORTED, NEEDS_SETUP, UNKNOWN or UNSUPPORTED; never equate metadata detection with confirmed root support.

This repository is a development foundation, **not a universal rooting tool**.
