package dev.rootlab.manager;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conservative, OFFLINE device triage. This is not a root detection API,
 * kernel module load test, or guarantee that an image can safely be flashed.
 *
 * Compatibility is ultimately decided by the pinned KernelSU engine, matching
 * KMI/ABI, firmware ramdisk and boot layout, bootloader state and OEM policy.
 */
public final class KernelCompatibility {
    private KernelCompatibility() {}

    private static final Pattern VERSION =
            Pattern.compile("(?<![0-9])([0-9]+)\\.([0-9]+)(?:\\.[0-9]+)?");
    private static final Pattern KMI =
            Pattern.compile("([0-9]+)\\.([0-9]+)\\.[0-9]+-android([0-9]+)-([0-9]+)");

    public enum Path {
        GKI_LKM_CANDIDATE,
        CUSTOM_KERNEL_REQUIRED,
        NEEDS_MANUAL_ASSESSMENT,
        ABI_NOT_BUILT
    }

    public static final class Result {
        public final Path path;
        public final String kmi;
        public final String explanation;
        public final String nextStep;

        private Result(Path path, String kmi, String explanation, String nextStep) {
            this.path = path;
            this.kmi = kmi;
            this.explanation = explanation;
            this.nextStep = nextStep;
        }

        public String display() {
            return "Kernel pathway: " + path.name().replace('_', ' ') +
                    "\\nKMI: " + kmi +
                    "\\n" + explanation +
                    "\\nNext: " + nextStep;
        }
    }

    public static Result assess(String kernelRelease, String abi) {
        if (kernelRelease == null || kernelRelease.trim().isEmpty() ||
                "unknown".equalsIgnoreCase(kernelRelease) ||
                "unavailable".equalsIgnoreCase(kernelRelease)) {
            return manual("Kernel release could not be read.");
        }

        if (abi == null || abi.trim().isEmpty() ||
                "unknown".equalsIgnoreCase(abi)) {
            return manual("CPU ABI could not be read.");
        }

        // Current Root Lab branded-manager CI only packages these native ksud
        // ABIs. Do not call an unsupported APK architecture "unrootable".
        String normalizedAbi = abi.toLowerCase(Locale.ROOT);
        if (!normalizedAbi.equals("arm64-v8a") && !normalizedAbi.equals("x86_64")) {
            return new Result(Path.ABI_NOT_BUILT, "unknown",
                    "This build does not currently package a native ksud for " + abi + ".",
                    "Use a matching upstream engine/manager build; no image will be flashed.");
        }

        Matcher version = VERSION.matcher(kernelRelease);
        if (!version.find()) return manual("Unrecognized kernel version.");

        int major, minor;
        try {
            major = Integer.parseInt(version.group(1));
            minor = Integer.parseInt(version.group(2));
        } catch (NumberFormatException e) {
            return manual("Unrecognized kernel version.");
        }

        if (major < 5 || (major == 5 && minor < 10)) {
            return new Result(Path.CUSTOM_KERNEL_REQUIRED, "not GKI 2.0",
                    "Older/non-GKI kernel: the supported GKI LKM route cannot be assumed.",
                    "A device-maintained kernel with a matched KernelSU integration is required.");
        }

        Matcher kmiMatch = KMI.matcher(kernelRelease);
        if (!kmiMatch.find()) {
            return manual("Kernel is 5.10+ but no complete GKI KMI was found in its release.");
        }

        int kmiMajor, kmiMinor;
        try {
            kmiMajor = Integer.parseInt(kmiMatch.group(1));
            kmiMinor = Integer.parseInt(kmiMatch.group(2));
        } catch (NumberFormatException e) {
            return manual("Invalid KMI version.");
        }
        if (kmiMajor != major || kmiMinor != minor) {
            return manual("Reported kernel version and KMI disagree.");
        }

        String kmi = kmiMatch.group(1) + "." + kmiMatch.group(2) +
                "-android" + kmiMatch.group(3) + "-" + kmiMatch.group(4);
        return new Result(Path.GKI_LKM_CANDIDATE, kmi,
                "GKI-style KMI found. This is only an LKM evaluation candidate, NOT confirmed support or root.",
                "Verify exact KernelSU LKM/KMI, firmware boot or init_boot layout, bootloader, " +
                "signing identity and stock-image backup before any installation.");
    }

    private static Result manual(String explanation) {
        return new Result(Path.NEEDS_MANUAL_ASSESSMENT, "unknown",
                explanation + " Device metadata cannot prove engine compatibility.",
                "Do not flash a generic image; confirm the upstream installation prerequisites.");
    }
}
