#!/usr/bin/env python3
"""Apply Root Lab branding to a CLEAN checkout of tiann/KernelSU.

This script does not build or flash a kernel, disable authentication, generate
keys, or claim root works. Run after checking out a pinned upstream revision.
"""
import argparse
import pathlib
import re
import xml.etree.ElementTree as ET

ANDROID_NS = "http://schemas.android.com/apk/res/android"
ET.register_namespace("android", ANDROID_NS)

LOGOS = {
    "hex": [
        ("M54,16 L79,30 L79,78 L54,92 L29,78 L29,30 Z", "#191C29"),
        ("M41,39 L41,69 M67,39 L67,69 M41,54 L67,54", "#00E0B8"),
    ],
    "bolt": [
        ("M54,14 L88,31 L88,77 L54,94 L20,77 L20,31 Z", "#161925"),
        ("M61,28 L40,58 L53,58 L44,81 L71,47 L56,47 Z", "#FFC247"),
    ],
    "shield": [
        ("M54,14 L85,26 L82,69 Q78,84 54,95 Q30,84 26,69 L23,26 Z", "#171B2A"),
        ("M40,53 L51,65 L70,42", "#55DDF1"),
    ],
}

def logo_xml(choice):
    paths = LOGOS[choice]
    body = []
    for index, (data, color) in enumerate(paths):
        if choice in ("hex", "shield") and index == 1:
            body.append(f'    <path android:pathData="{data}" android:strokeColor="{color}" android:strokeWidth="6" android:strokeLineCap="round" android:strokeLineJoin="round" android:fillColor="@android:color/transparent"/>')
        else:
            body.append(f'    <path android:pathData="{data}" android:fillColor="{color}"/>')
    return ('<?xml version="1.0" encoding="utf-8"?>\n'
            '<vector xmlns:android="http://schemas.android.com/apk/res/android" '
            'android:width="108dp" android:height="108dp" '
            'android:viewportWidth="108" android:viewportHeight="108">\n'
            + "\n".join(body) + '\n</vector>\n')

def apply(upstream, name, package, logo):
    app = upstream / "manager" / "app"
    gradle = app / "build.gradle.kts"
    manifest = app / "src/main/AndroidManifest.xml"
    if not gradle.is_file() or not manifest.is_file() or not (upstream / "kernel/Kbuild").is_file():
        raise ValueError("Expected a full pinned KernelSU source checkout, including manager and kernel")
    if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)+", package):
        raise ValueError("Invalid Android package name")
    if len(package) > 100 or not name or len(name) > 40 or "\n" in name:
        raise ValueError("Invalid application name or package length")
    if logo not in LOGOS:
        raise ValueError("Unsupported logo")
    # Keep upstream Kotlin/JNI namespaces unchanged: Gradle already exposes
    # KSU_PACKAGE_NAME and KSU_NAME as supported applicationId and app_name settings.
    gradle_text = gradle.read_text()
    for key in ("KSU_PACKAGE_NAME", "KSU_NAME"):
        if key not in gradle_text:
            raise ValueError(f"Upstream no longer supports {key}; re-audit before building")
    res = app / "src/main/res"
    (res / "drawable/ic_launcher_foreground.xml").write_text(logo_xml(logo))
    (res / "drawable/ic_launcher_monochrome.xml").write_text(logo_xml(logo))
    # Use the official foreground in the adaptive launcher; nothing in the
    # engine's JNI or UID/authorization logic is modified by cosmetic branding.
    print(f"Branded upstream assets: name={name!r}; package={package}; logo={logo}")
    print("Use Gradle -PKSU_NAME and -PKSU_PACKAGE_NAME; sign with the matching dedicated release key.")
    print("A matching KernelSU engine must be built with the same package/certificate identity.")

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--upstream", type=pathlib.Path, required=True)
    parser.add_argument("--name", default="LORD H3X")
    parser.add_argument("--package", default="com.lordh3x.manager")
    parser.add_argument("--logo", choices=sorted(LOGOS), default="hex")
    args = parser.parse_args()
    apply(args.upstream.resolve(), args.name, args.package, args.logo)

if __name__ == "__main__":
    main()
