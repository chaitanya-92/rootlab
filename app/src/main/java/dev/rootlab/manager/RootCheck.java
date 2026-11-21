package dev.rootlab.manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Consent-driven generic root *verification*, not a KernelSU manager API.
 * The user presses a button before we execute "su -c id". This may cause a
 * system superuser confirmation prompt. uid=0 proves only that this command
 * received root privileges; it does not identify or configure any backend.
 */
public final class RootCheck {
    private RootCheck() {}
    public enum State { GRANTED, DENIED, UNAVAILABLE, ERROR }
    public static final class Result {
        public final State state;
        public final String detail;
        public Result(State state, String detail) { this.state = state; this.detail = detail; }
    }
    public static Result verify() {
        Process process = null;
        try {
            process = new ProcessBuilder("su", "-c", "id").redirectErrorStream(true).start();
            if (!process.waitFor(8, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new Result(State.ERROR, "Root request timed out or was not answered.");
            }
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null && output.length() < 2048) {
                    output.append(line).append('\n');
                }
            }
            boolean root = process.exitValue() == 0 && output.toString().matches("(?s).*\\buid=0\\b.*");
            if (root) return new Result(State.GRANTED, "Verified: su returned uid=0. Backend identity is not verified.");
            return new Result(State.DENIED, "No root authorization: " + shorten(output.toString()));
        } catch (java.io.IOException e) {
            return new Result(State.UNAVAILABLE, "No accessible su command: " + e.getClass().getSimpleName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Result(State.ERROR, "Verification interrupted.");
        } catch (SecurityException e) {
            return new Result(State.DENIED, "Execution blocked by Android security policy.");
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
        }
    }
    private static String shorten(String s) {
        return s.length() > 180 ? s.substring(0, 180) : s;
    }
}
