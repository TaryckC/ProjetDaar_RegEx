import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class RegEx {
  // MACROS
  static final int CONCAT = 0xC04CA7;
  static final int ETOILE = 0xE7011E;
  static final int ALTERN = 0xA17E54;
  static final int PROTECTION = 0xBADDAD;

  static final int PARENTHESEOUVRANT = 0x16641664;
  static final int PARENTHESEFERMANT = 0x51515151;
  static final int DOT = 0xD07;
  static int counter = 0;

  // REGEX
  private static String regEx;

  // CONSTRUCTOR
  public RegEx() {
  }

  // MAIN
  public static void main(String[] args) {
    try {
      Test.runTests(
          "ProjetDaar_RegEx/backEnd/Store/tests",
          "ProjetDaar_RegEx/backEnd/Store/texts",
          "ProjetDaar_RegEx/backEnd/Store/tests_results");
    } catch (Exception e) {
      System.err.println("Échec lors de la réalisation des tests : "
          + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
      e.printStackTrace();
    }
  }

  private static void runTests() {
    System.out.println("Launching DFA validation tests...\n");
    List<RegexTestCase> tests = new ArrayList<>();
    tests.add(new RegexTestCase("Literal match", "ab", "ab", true, 0, 1));
    tests.add(new RegexTestCase("Literal mismatch", "ab", "ac", false, 0, -1));
    tests.add(new RegexTestCase("Dot in middle", "a.b", "acb", true, 0, 2));
    tests.add(new RegexTestCase("Dot mismatch", "a.b", "ab", false, 0, -1));
    tests.add(new RegexTestCase("Kleene star", "a*b", "aaab", true, 0, 3));
    tests.add(new RegexTestCase("Alternation", "(a|b)c", "bc", true, 0, 1));

    int passed = 0;
    for (RegexTestCase test : tests) {
      try {
        int[] result = evaluatePattern(test.pattern, test.input);
        boolean success = test.shouldMatch
            ? result[1] == test.expectedEnd && result[0] == test.expectedStart
            : result[1] == -1;

        if (success) {
          passed++;
          System.out.println("[PASS] " + test.name);
        } else {
          System.out.println("[FAIL] " + test.name + " | expected start="
              + test.expectedStart + ", end=" + test.expectedEnd + " but got "
              + Arrays.toString(result));
        }
      } catch (Exception e) {
        System.out.println("[ERROR] " + test.name + " | " + e.getMessage());
      }
    }

    System.out.println("\nTest summary: " + passed + "/" + tests.size() + " passed.");
  }

  private static int[] evaluatePattern(String pattern, String input) throws Exception {
    counter = 0;
    regEx = pattern;
    RegExTree tree = parse();
    NFA nfa = toNFA(tree);
    DFA dfa = determinize(nfa);
    DFA minimized = DFA.updateDFA(dfa);
    return minimized.validateString(toCharacterArray(input));
  }

  /**
   * Build a minimized DFA instance from a regex pattern string.
   */
  public static DFA buildDFA(String pattern) throws Exception {
    if (pattern == null) {
      throw new IllegalArgumentException("pattern must not be null");
    }
    counter = 0;
    regEx = pattern;
    RegExTree tree = parse();
    NFA nfa = toNFA(tree);
    DFA dfa = determinize(nfa);
    return DFA.updateDFA(dfa);
  }

  private static Character[] toCharacterArray(String input) {
    Character[] array = new Character[input.length()];
    for (int i = 0; i < input.length(); i++) {
      array[i] = input.charAt(i);
    }
    return array;
  }

  private static class RegexTestCase {
    final String name;
    final String pattern;
    final String input;
    final boolean shouldMatch;
    final int expectedStart;
    final int expectedEnd;

    RegexTestCase(String name, String pattern, String input, boolean shouldMatch,
        int expectedStart, int expectedEnd) {
      this.name = name;
      this.pattern = pattern;
      this.input = input;
      this.shouldMatch = shouldMatch;
      this.expectedStart = expectedStart;
      this.expectedEnd = expectedEnd;
    }
  }

  public static void printNFA(NFA nfa) {
    Set<State> visited = new HashSet<>();
    Queue<State> queue = new LinkedList<>();
    queue.add(nfa.start);
    visited.add(nfa.start);

    System.out.println("NFA transitions:");
    while (!queue.isEmpty()) {
      State s = queue.poll();
      // Transitions symboliques
      for (Map.Entry<Character, List<State>> e : s.transitions.entrySet()) {
        for (State target : e.getValue()) {
          System.out.println("  " + s.id + " --" + e.getKey() + "--> " + target.id);
          if (visited.add(target))
            queue.add(target);
        }
      }
      // Epsilon transitions
      for (State target : s.epsilonTransitions) {
        System.out.println("  " + s.id + " --ε--> " + target.id);
        if (visited.add(target))
          queue.add(target);
      }
    }
  }

  private static NFA toNFA(RegExTree tree) {
    if (tree.subTrees.isEmpty()) {
      State s = new State(counter++, false);
      State t = new State(counter++, true);
      s.addTransition((char) tree.root, t);
      return new NFA(s, t);
    } else if (tree.root == CONCAT) {
      NFA left = toNFA(tree.subTrees.get(0));
      NFA right = toNFA(tree.subTrees.get(1));
      left.accept.isAccept = false;
      left.accept.addEpsilonTransition(right.start);
      return new NFA(left.start, right.accept);
    } else if (tree.root == ALTERN) {
      NFA left = toNFA(tree.subTrees.get(0));
      NFA right = toNFA(tree.subTrees.get(1));
      left.accept.isAccept = false;
      right.accept.isAccept = false;
      State s = new State(counter++, false);
      State t = new State(counter++, true);
      s.addEpsilonTransition(left.start);
      s.addEpsilonTransition(right.start);
      left.accept.addEpsilonTransition(t);
      right.accept.addEpsilonTransition(t);
      return new NFA(s, t);
    } else if (tree.root == ETOILE) {
      NFA child = toNFA(tree.subTrees.get(0));
      child.accept.addEpsilonTransition(child.start);

      State s = new State(counter++, false);
      State t = new State(counter++, true);
      s.addEpsilonTransition(child.start);
      s.addEpsilonTransition(t);
      child.accept.addEpsilonTransition(t);
      child.accept.isAccept = false;

      return new NFA(s, t);

    }
    throw new IllegalStateException("Operator cases not implemented yet: root=" + tree.root);
  }

  // FROM REGEX TO SYNTAX TREE
  private static RegExTree parse() throws Exception {

    ArrayList<RegExTree> result = new ArrayList<>();
    for (int i = 0; i < regEx.length(); i++)
      result.add(new RegExTree(charToRoot(regEx.charAt(i)), new ArrayList<>()));

    return parse(result);
  }

  private static int charToRoot(char c) {
    if (c == '.')
      return DOT;
    if (c == '*')
      return ETOILE;
    if (c == '|')
      return ALTERN;
    if (c == '(')
      return PARENTHESEOUVRANT;
    if (c == ')')
      return PARENTHESEFERMANT;
    return (int) c;
  }

  private static RegExTree parse(ArrayList<RegExTree> result) throws Exception {
    while (containParenthese(result))
      result = processParenthese(result);
    while (containEtoile(result))
      result = processEtoile(result);
    while (containConcat(result))
      result = processConcat(result);
    while (containAltern(result))
      result = processAltern(result);

    if (result.size() > 1)
      throw new Exception();

    return removeProtection(result.get(0));
  }

  private static boolean containParenthese(ArrayList<RegExTree> trees) {
    for (RegExTree t : trees)
      if (t.root == PARENTHESEFERMANT || t.root == PARENTHESEOUVRANT)
        return true;
    return false;
  }

  private static ArrayList<RegExTree> processParenthese(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<>();
    boolean found = false;
    for (RegExTree t : trees) {
      if (!found && t.root == PARENTHESEFERMANT) {
        boolean done = false;
        ArrayList<RegExTree> content = new ArrayList<>();
        while (!done && !result.isEmpty())
          if (result.get(result.size() - 1).root == PARENTHESEOUVRANT) {
            done = true;
            result.remove(result.size() - 1);
          } else
            content.add(0, result.remove(result.size() - 1));
        if (!done)
          throw new Exception();
        found = true;
        ArrayList<RegExTree> subTrees = new ArrayList<>();
        subTrees.add(parse(content));
        result.add(new RegExTree(PROTECTION, subTrees));
      } else {
        result.add(t);
      }
    }
    if (!found)
      throw new Exception();
    return result;
  }

  private static boolean containEtoile(ArrayList<RegExTree> trees) {
    for (RegExTree t : trees)
      if (t.root == ETOILE && t.subTrees.isEmpty())
        return true;
    return false;
  }

  private static ArrayList<RegExTree> processEtoile(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<>();
    boolean found = false;
    for (RegExTree t : trees) {
      if (!found && t.root == ETOILE && t.subTrees.isEmpty()) {
        if (result.isEmpty())
          throw new Exception();
        found = true;
        RegExTree last = result.remove(result.size() - 1);
        ArrayList<RegExTree> subTrees = new ArrayList<>();
        subTrees.add(last);
        result.add(new RegExTree(ETOILE, subTrees));
      } else {
        result.add(t);
      }
    }
    return result;
  }

  private static boolean containConcat(ArrayList<RegExTree> trees) {
    boolean firstFound = false;
    for (RegExTree t : trees) {
      if (!firstFound && t.root != ALTERN) {
        firstFound = true;
        continue;
      }
      if (firstFound)
        if (t.root != ALTERN)
          return true;
        else
          firstFound = false;
    }
    return false;
  }

  private static ArrayList<RegExTree> processConcat(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<>();
    boolean found = false;
    boolean firstFound = false;
    for (RegExTree t : trees) {
      if (!found && !firstFound && t.root != ALTERN) {
        firstFound = true;
        result.add(t);
        continue;
      }
      if (!found && firstFound && t.root == ALTERN) {
        firstFound = false;
        result.add(t);
        continue;
      }
      if (!found && firstFound && t.root != ALTERN) {
        found = true;
        RegExTree last = result.remove(result.size() - 1);
        ArrayList<RegExTree> subTrees = new ArrayList<>();
        subTrees.add(last);
        subTrees.add(t);
        result.add(new RegExTree(CONCAT, subTrees));
      } else {
        result.add(t);
      }
    }
    return result;
  }

  private static boolean containAltern(ArrayList<RegExTree> trees) {
    for (RegExTree t : trees)
      if (t.root == ALTERN && t.subTrees.isEmpty())
        return true;
    return false;
  }

  private static ArrayList<RegExTree> processAltern(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<>();
    boolean found = false;
    RegExTree gauche = null;
    boolean done = false;
    for (RegExTree t : trees) {
      if (!found && t.root == ALTERN && t.subTrees.isEmpty()) {
        if (result.isEmpty())
          throw new Exception();
        found = true;
        gauche = result.remove(result.size() - 1);
        continue;
      }
      if (found && !done) {
        if (gauche == null)
          throw new Exception();
        done = true;
        ArrayList<RegExTree> subTrees = new ArrayList<>();
        subTrees.add(gauche);
        subTrees.add(t);
        result.add(new RegExTree(ALTERN, subTrees));
      } else {
        result.add(t);
      }
    }
    return result;
  }

  private static RegExTree removeProtection(RegExTree tree) throws Exception {
    if (tree.root == PROTECTION && tree.subTrees.size() != 1)
      throw new Exception();
    if (tree.subTrees.isEmpty())
      return tree;
    if (tree.root == PROTECTION)
      return removeProtection(tree.subTrees.get(0));

    ArrayList<RegExTree> subTrees = new ArrayList<>();
    for (RegExTree t : tree.subTrees)
      subTrees.add(removeProtection(t));
    return new RegExTree(tree.root, subTrees);
  }

  // EXAMPLE
  // --> RegEx from Aho-Ullman book Chap.10 Example 10.25
  private static RegExTree exampleAhoUllman() {
    RegExTree a = new RegExTree((int) 'a', new ArrayList<>());
    RegExTree b = new RegExTree((int) 'b', new ArrayList<>());
    RegExTree c = new RegExTree((int) 'c', new ArrayList<>());
    ArrayList<RegExTree> subTrees = new ArrayList<>();
    subTrees.add(c);
    RegExTree cEtoile = new RegExTree(ETOILE, subTrees);
    subTrees = new ArrayList<>();
    subTrees.add(b);
    subTrees.add(cEtoile);
    RegExTree dotBCEtoile = new RegExTree(CONCAT, subTrees);
    subTrees = new ArrayList<>();
    subTrees.add(a);
    subTrees.add(dotBCEtoile);
    return new RegExTree(ALTERN, subTrees);
  }

  private static Set<State> epsilonClosure(Set<State> S) {
    Set<State> closure = new HashSet<>(S); // inclure S dès le départ
    Deque<State> stack = new ArrayDeque<>(S); // pile/queue de travail

    while (!stack.isEmpty()) {
      State s = stack.pop();
      for (State t : s.epsilonTransitions) {
        if (!closure.contains(t)) {
          closure.add(t);
          stack.push(t); // explorer aussi les ε de ce nouvel état
        }
      }
    }

    return closure;
  }

  public static Set<State> move(Set<State> S, char c) {
    Set<State> result = new HashSet<>();
    for (State s : S) {
      List<State> targets = s.transitions.get(c);
      if (targets != null) {
        result.addAll(targets);
      }
    }
    return result;
  }

  public static Set<Character> symbolsFrom(Set<State> S) {
    // retourne l’union des clés S_i.transitions.keySet() pour tous les états S_i ∈
    // S
    Set<Character> sCharacters = new HashSet<>();
    for (State s : S) {
      sCharacters.addAll(s.transitions.keySet());
    }
    return sCharacters;
  }

  public static String keyOf(Set<State> set) {
    if (set == null || set.isEmpty())
      return "Ø"; // clé pour l’ensemble vide
    List<Integer> ids = new ArrayList<>(set.size());
    for (State s : set)
      ids.add(s.id);
    Collections.sort(ids);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ids.size(); i++) {
      if (i > 0)
        sb.append(',');
      sb.append(ids.get(i));
    }
    return sb.toString(); // ex: "0,2,5"
  }

  public static DFA determinize(NFA nfa) {
    DFA dfa = new DFA();
    Map<String, DfaState> registre = new HashMap<>();
    Set<State> startSet = new HashSet<>();
    startSet.add(nfa.start);
    Set<State> closure = epsilonClosure(startSet);
    int nextId = 0;
    DfaState start = registre.get(keyOf(closure));
    if (start == null) {
      start = new DfaState(nextId++, closure, closure.contains(nfa.accept));
      registre.put(keyOf(startSet), start);
      dfa.states.add(start);
      dfa.trans.put(start, new HashMap<>());
    }
    dfa.start = start;

    Queue<DfaState> worklist = new LinkedList<>();
    worklist.add(start);

    while (!worklist.isEmpty()) {
      DfaState curr = worklist.poll();

      for (char c : symbolsFrom(curr.nfaSet)) {
        Set<State> moved = move(curr.nfaSet, c);
        Set<State> targetSet = epsilonClosure(moved);
        if (targetSet.isEmpty())
          continue; // pas d’état

        String k = keyOf(targetSet);
        DfaState target = registre.get(k);
        if (target == null) {
          target = new DfaState(nextId++, targetSet, targetSet.contains(nfa.accept));
          registre.put(k, target);
          dfa.states.add(target);
          dfa.trans.put(target, new HashMap<>());
          worklist.add(target); // on explore plus tard
        }

        // relier la transition
        dfa.trans.get(curr).put(c, target);
        dfa.alphabet.add(c);
      }
    }

    return dfa;

  }

}
