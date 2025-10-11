import java.util.List;

public class TestResult {
    private final String method;
    private final String pattern;
    private final List<String> result;
    private final float time;

    // Captures the outcome of running a search method against a pattern.
    public TestResult(String method, String pattern, List<String> result, float time) {
        this.method = method;
        this.pattern = pattern;
        this.result = result;
        this.time = time;
    }

    // Provides the tested pattern for reporting.
    public String getPattern() {
        return pattern;
    }

    // Returns the collected matches for the pattern.
    public List<String> getResult() {
        return result;
    }

    // Serializes the test result into a compact JSON line.
    public String toJson() {
        return "{"
                + "\"method\":\"" + method + "\","
                + "\"pattern\":\"" + pattern + "\","
                + "\"resultSize\":" + result.size() + ","
                + "\"time\":" + time / 1000000 // Nanosec to ms
                + "}";
    }
}
