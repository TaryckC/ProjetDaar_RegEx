import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class KMP {

    public static List<String> searchPhase(Character[] P, String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            int q = 0;
            int[] pi = computePi(P);
            List<String> res = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                char[] chars = line.toCharArray();
                for (char currentChar : chars) {
                    while (q > 0 && !P[q].equals(currentChar)) {
                        q = pi[q - 1];
                    }
                    if (P[q].equals(currentChar)) {
                        q = q+1;
                    }
                    if (q == P.length) {
                        res.add(line);
                        q = pi[q - 1];
                    }
                }
            }
            return res;
        }
    }

    private static int[] computePi(Character[] P) {
        int[] pi = new int[P.length];
        pi[0] = 0;
        for (int q = 1; q<P.length; q++) {
            int k = pi[q - 1];
            while (k > 0 && !P[k].equals(P[q])) {
                k = pi[k - 1];
            }
            if (P[k].equals(P[q])) {
                pi[q] = k+1;
            }
            else {
                pi[q] = 0;
            }
        }
        return pi;
    }
}
