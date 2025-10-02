import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextSearcher {

    // Read the text and search on each line specific character combinations based
    // on the given DFA.
    public static List<String> searchText(String filePath, DFA dfa) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            List<String> res = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                Character[] chars = toCharacterArray(line);
                if (dfa.validateString(chars)[1] != -1) {
                    res.add(line);
                }
            }
            return res;
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
