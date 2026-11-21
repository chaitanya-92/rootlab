package dev.rootlab.manager;

import java.util.Arrays;
import java.util.List;

/**
 * Safe registry: separate KernelSU and KernelSU Next integration boundaries.
 * Authentication and permission-management operations must be implemented
 * against documented upstream APIs for pinned versions before enabling them.
 */
public final class BackendRegistry {
    private BackendRegistry() {}
    private static BackendAdapter placeholder(final String id, final String name) {
        return new BackendAdapter() {
            @Override public String id() { return id; }
            @Override public String displayName() { return name; }
            @Override public Readiness readiness() { return Readiness.NOT_INTEGRATED; }
        };
    }
    public static List<BackendAdapter> available() {
        return Arrays.asList(placeholder("kernelsu", "KernelSU"),
                             placeholder("kernelsu-next", "KernelSU Next"));
    }
}
