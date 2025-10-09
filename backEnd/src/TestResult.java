import java.util.List;

public class TestResult {
    private final String method;
    private final String pattern;
    private final List<String> result;
    private final float time;

    public TestResult(String method, String pattern, List<String> result, float time) {
        this.method = method;
        this.pattern = pattern;
        this.result = result;
        this.time = time;
    }

    public String getPattern() {
        return pattern;
    }

    public List<String> getResult() {
        return result;
    }

    public String toJson() {
        return "{"
                + "\"method\":\"" + method + "\","
                + "\"pattern\":\"" + pattern + "\","
                // "\"result\":" + result.toString() + ","
                + "\"resultSize\":" + result.size() + ","
                + "\"time\":" + time / 1000000 // Nanosec to ms
                + "}";
    }
}
