import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextSearcher {

    // Read the text and search on each line specific character combinations based
    // on the given DFA.
    public static List<String> searchText(String filePath, DFA dfa) throws IOException {
        List<String> res = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder currentLine = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                if (ch == '\n') {
                    evaluateLine(dfa, res, currentLine);
                    currentLine.setLength(0);
                } else {
                    currentLine.append((char) ch);
                }
            }
            if (currentLine.length() > 0) {
                evaluateLine(dfa, res, currentLine);
            }
        }
        return res;
    }

    private static void evaluateLine(DFA dfa, List<String> results, StringBuilder buffer) {
        String rawLine = buffer.toString();
        Character[] chars = toCharacterArray(rawLine);
        if (dfa.matches(chars)) {
            results.add(rawLine.replace("\r", ""));
        }
    }

    private static Character[] toCharacterArray(String s) {
        Character[] arr = new Character[s.length()];
        for (int i = 0; i < s.length(); i++) {
            arr[i] = s.charAt(i);
        }
        return arr;
    }
}
