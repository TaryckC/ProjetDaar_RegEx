import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KMP {

    // KMP :
    // Runs the KMP search for pattern P across every line of the target file.
    public static List<String> searchPhase(Character[] P, String filePath) throws IOException {
        if (P == null)
            throw new IllegalArgumentException("pattern must not be null");
        if (P.length == 0)
            throw new IllegalArgumentException("pattern must not be empty");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            int[] co = computeCO(P);
            List<String> res = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                int q = 0; // index in pattern
                int i = 0; // index in current line
                while (i < line.length()) {
                    char currentChar = line.charAt(i);
                    if (P[q].equals(currentChar)) {
                        q++;
                        i++;
                        if (q == P.length) {
                            res.add(line);
                            break;
                        }
                    } else {
                        int instruction = co[q];
                        switch (instruction) {
                            case -1 -> {
                                i++;
                                q = 0;
                            }
                            case 0 -> q = 0;
                            case 1 -> {
                                if (i > 0) {
                                    i--;
                                }
                                q = 1;
                            }
                            default -> q = instruction;
                        }
                        if (i < 0) {
                            i = 0;
                        }
                    }
                }
            }
            return res;
        }
    }

    // Builds the failure table used during the KMP scan.
    private static int[] computeCO(Character[] P) {
        int[] CO = new int[P.length];
        CO[0] = -1;
        // Step 1
        for (int i = 1; i < CO.length; i++) {
            int prefixeLengthCounter = 0;
            for (int j = 0; j < i; i++) {
                if (P[i - 1].equals(P[j]))
                    prefixeLengthCounter = prefixeLengthCounter + 1;
                else {
                    break;
                }
            }
            // Step 2
            if ((P[i - 1].equals(P[CO[i - 1]]) && (CO[CO[i - 1]] == -1)))
                CO[i - 1] = -1;
            // Step 3
            if ((P[i - 1].equals(P[CO[i - 1]]) && (CO[CO[i - 1]] != -1)))
                CO[i - 1] = CO[CO[i - 1]];
        }
        return CO;
    }
}
