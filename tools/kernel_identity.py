#!/usr/bin/env python3
"""Emit KernelSU Kbuild manager identity values from a release CERTIFICATE.

The input is the DER-encoded signing certificate (not the private key).
APK must be signed using this certificate and APK Signature Scheme v2.
The *same* configuration must be used for the compatible kernel build.
"""
import argparse
import hashlib
from pathlib import Path
import re

def identity(package, certificate):
    if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)+", package):
        raise ValueError("Invalid package")
    if not certificate or not certificate.startswith(b"0"):
        raise ValueError("Expected nonempty DER-encoded X.509 certificate")
    return (
        f"KSU_PACKAGE_NAME={package}\n"
        f"KSU_EXPECTED_SIZE=0x{len(certificate):x}\n"
        f"KSU_EXPECTED_HASH={hashlib.sha256(certificate).hexdigest()}\n"
    )

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--package", default="com.lordh3x.manager")
    p.add_argument("--cert-der", type=Path, required=True)
    p.add_argument("--output", type=Path)
    args = p.parse_args()
    result = identity(args.package, args.cert_der.read_bytes())
    if args.output:
        args.output.write_text(result)
        print(f"Wrote kernel identity configuration to {args.output}")
    else:
        print(result, end="")

if __name__ == "__main__":
    main()
