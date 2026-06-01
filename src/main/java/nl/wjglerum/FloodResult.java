package nl.wjglerum;

public record FloodResult(int requested, int succeeded, int failed, long durationMs) {
}
