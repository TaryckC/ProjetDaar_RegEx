import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Test {

    // The test folder must contain the following files :
    // - pattern_regex.txt
    // - pattern_words.txt
    public static void runTests(String path_to_test_folder, String path_to_texts, String path_to_results_writing)
            throws IOException {
        String regexFile = path_to_test_folder + "/pattern_regex.txt";
        String wordsFile = path_to_test_folder + "/pattern_words.txt";

        try {
            // 1) Tests pour les RegEx -> DFA
            List<TestResult> regexResults = runComparisonTest(regexFile, path_to_texts, true);
            System.out.println("End of Regex results computations");

            // 2) Tests pour les mots complets -> KMP + egrep
            List<TestResult> wordResults = runComparisonTest(wordsFile, path_to_texts, false);
            System.out.println("End of complete words results computations");

            // 3) Comparing results
            String errorsOutPath = path_to_results_writing + "/results_comparison.txt";
            try (var errWriter = Files.newBufferedWriter(Path.of(errorsOutPath), StandardCharsets.UTF_8)) {
                for (int i = 0; i < wordResults.size(); i += 3) {
                    List<String> list1 = wordResults.get(i).getResult();
                    List<String> list2 = wordResults.get(i + 1).getResult();
                    List<String> list3 = wordResults.get(i + 2).getResult();

                    if (!(list1.equals(list2) && list2.equals(list3))) {
                        errWriter.write(i / 3 + ";" + wordResults.get(i).getPattern());
                        errWriter.newLine();
                    }
                }

                for (int i = 0; i < regexResults.size(); i += 2) {
                    List<String> list1 = regexResults.get(i).getResult();
                    List<String> list2 = regexResults.get(i + 1).getResult();

                    if (!(list1.equals(list2))) {
                        // System.out.println(regexResults.get(i).getResult());
                        // System.out.println("-----------------------------------");
                        // System.out.println(regexResults.get(i+1).getResult());
                        errWriter.write(i / 2 + ";" + regexResults.get(i).getPattern());
                        errWriter.newLine();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 4) Writing results in two separate JSON files
            String regexOutPath = path_to_results_writing + "/results_regex.json";
            String wordsOutPath = path_to_results_writing + "/results_words.json";
            // Regex
            try (var writer = Files.newBufferedWriter(Path.of(regexOutPath), StandardCharsets.UTF_8)) {
                for (TestResult r : regexResults) {
                    writer.write(r.toJson());
                    writer.newLine();
                }
            }

            // Words
            try (var writer = Files.newBufferedWriter(Path.of(wordsOutPath), StandardCharsets.UTF_8)) {
                for (TestResult r : wordResults) {
                    writer.write(r.toJson());
                    writer.newLine();
                }
            }

            System.out.println("JSON written.");

        } catch (Exception e) {
            throw new IOException("Failed to run tests from folder: " + path_to_test_folder, e);
        }
    }

    private static List<String> collectTextFiles(Path folder) throws IOException {
        try (var stream = Files.list(folder)) {
            return stream.filter(Files::isRegularFile)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .map(Path::toString)
                    .toList();
        }
    }

    private static List<TestResult> runComparisonTest(String path_to_test, String path_to_texts, boolean containsRegex)
            throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(path_to_test))) {
            String line;
            List<TestResult> results = new ArrayList<>();
            List<String> textFiles = collectTextFiles(Paths.get(path_to_texts));
            while ((line = br.readLine()) != null) {
                String pattern = line.trim();
                if (pattern.isEmpty()) {
                    continue;
                }
                for (String path : textFiles) {

                    if (!containsRegex) {
                        // KMP
                        long startTime = System.nanoTime();
                        Character[] chars = pattern.chars().mapToObj(c -> (char) c).toArray(Character[]::new);
                        List<String> kmpSearchResult = KMP.searchPhase(chars, path);
                        long endTime = System.nanoTime();
                        long duration = endTime - startTime;

                        results.add(new TestResult("KMP", pattern, kmpSearchResult, duration));

                    }

                    // DFA
                    long startTime = System.nanoTime();
                    DFA dfa = RegEx.buildDFA(pattern);
                    List<String> dfaSearchResult = TextSearcher.searchText(path, dfa);
                    long endTime = System.nanoTime();
                    long duration = endTime - startTime;

                    results.add(new TestResult("DFA", pattern, dfaSearchResult, duration));

                    if (pattern.equals("cat")) {
                        // dfa.printDFA();
                        // System.err.println(dfa.alphabet);
                    }

                    // egrep
                    startTime = System.nanoTime();
                    List<String> egrepResult = runEgrep(pattern, path);
                    endTime = System.nanoTime();
                    duration = endTime - startTime;

                    results.add(new TestResult("EGR", pattern, egrepResult, duration));
                }
            }
            return results;
        }
    }

    private static List<String> runEgrep(String pattern, String filePath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("egrep", pattern, filePath);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        List<String> egrepResult = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String l;
            while ((l = reader.readLine()) != null) {
                if (l.indexOf('\r') != -1) {
                    l = l.replace("\r", "");
                }
                egrepResult.add(l);
            }
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for egrep", ie);
        }

        if (exitCode != 0 && exitCode != 1) {
            throw new IOException("egrep failed with exit code " + exitCode + " for pattern '" + pattern + "'");
        }

        if (egrepResult.isEmpty() && exitCode != 1) {
            // Fallback to Java regex to avoid empty results when egrep cannot be used
            // (e.g., sandbox restrictions)
            egrepResult = emulateEgrep(pattern, filePath);
        }

        return egrepResult;
    }

    private static List<String> emulateEgrep(String pattern, String filePath) throws IOException {
        Pattern javaPattern;
        try {
            javaPattern = Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new IOException("Invalid regex pattern for emulateEgrep: " + pattern, e);
        }
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = javaPattern.matcher(line);
                if (matcher.find()) {
                    result.add(line);
                }
            }
        }
        return result;
    }
}
