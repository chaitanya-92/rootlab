package dev.rootlab.manager;

/** Root backends require their own authenticated APIs; su alone is insufficient. */
public interface BackendAdapter {
    String id();
    String displayName();
    /** Never report READY from package presence, Build fields, or a successful generic su check. */
    Readiness readiness();
    enum Readiness { NOT_INTEGRATED, AUTHENTICATED, UNAVAILABLE }
}
