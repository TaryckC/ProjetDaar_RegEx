import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.lang.Exception;
import java.security.KeyStore.Entry;
import java.text.Collator;

public class RegEx {
  //MACROS
  static final int CONCAT = 0xC04CA7;
  static final int ETOILE = 0xE7011E;
  static final int ALTERN = 0xA17E54;
  static final int PROTECTION = 0xBADDAD;

  static final int PARENTHESEOUVRANT = 0x16641664;
  static final int PARENTHESEFERMANT = 0x51515151;
  static final int DOT = 0xD07;
  static int counter=0;
  
  //REGEX
  private static String regEx;
  
  //CONSTRUCTOR
  public RegEx(){}

  //MAIN
public static void main(String[] args) {
    System.out.println("Welcome to Bogota, Mr. Thomas Anderson.");

    // 1. Lire l'expression régulière
    if (args.length != 0) {
        regEx = args[0];
    } else {
        Scanner scanner = new Scanner(System.in);
        System.out.print("  >> Please enter a regEx: ");
        regEx = scanner.next();
        scanner.close();
    }

    System.out.println("  >> Parsing regEx \"" + regEx + "\".");
    System.out.println("  >> ...");

    if (regEx.length() < 1) {
        System.err.println("  >> ERROR: empty regEx.");
    } else {
        // Pour debug : afficher les codes ASCII
        System.out.print("  >> ASCII codes: [" + (int) regEx.charAt(0));
        for (int i = 1; i < regEx.length(); i++) {
            System.out.print("," + (int) regEx.charAt(i));
        }
        System.out.println("].");

        try {
            // 2. Parser
            RegExTree ret = parse();
            System.out.println("  >> Tree result: " + ret.toString() + ".");

            // 3. Construire l'automate NFA
            NFA nfa = toNFA(ret);
            System.out.println("  >> NFA built: start=" + nfa.start.id + ", accept=" + nfa.accept.id);

            // 4. Debug transitions du start
            System.out.println("  >> Start transitions: "
                + nfa.start.transitions.size() + " symbol(s), "
                + nfa.start.epsilonTransitions.size() + " epsilon(s).");
            
             System.out.println("  >> accept transitions: "
                + nfa.accept.transitions.size() + " symbol(s), "
                + nfa.accept.epsilonTransitions.size() + " epsilon(s).");

              printNFA(nfa);

              // 5. Construire le DFA
              DFA dfa = determinize(nfa);

              // 6. Afficher le DFA pour debug
              System.out.println("  >> DFA states:");
              for (DfaState q : dfa.states) {
                  String ids = keyOf(q.nfaSet);
                  System.out.println("    q" + q.id + " = {" + ids + "} "
                      + (q.isAccept ? "(final)" : ""));
              }

              System.out.println("  >> DFA transitions:");
              for (Map.Entry<DfaState, Map<Character, DfaState>> entry : dfa.trans.entrySet()) {
                  DfaState from = entry.getKey();
                  for (Map.Entry<Character, DfaState> t : entry.getValue().entrySet()) {
                      char c = t.getKey();
                      DfaState to = t.getValue();
                      System.out.println("    q" + from.id + " --" + c + "--> q" + to.id);
                  }
              }

              System.out.println("######### Nouveaux print du DFA");
              dfa.printDFA();

              System.out.println("######### Nouveaux print du DFA minimisé");
              DFA minimizedDFA = DFA.updateDFA(dfa);
              minimizedDFA.printDFA();


        } catch (Exception e) {
            System.err.println("  >> ERROR: syntax error for regEx \"" + regEx + "\".");
            e.printStackTrace();
        }
    }



    System.out.println("  >> ...");
    System.out.println("  >> Parsing completed.");
    System.out.println("Goodbye Mr. Anderson.");
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
                if (visited.add(target)) queue.add(target);
            }
        }
        // Epsilon transitions
        for (State target : s.epsilonTransitions) {
            System.out.println("  " + s.id + " --ε--> " + target.id);
            if (visited.add(target)) queue.add(target);
        }
    }
}




  public static NFA toNFA(RegExTree tree) {
    if (tree.subTrees.isEmpty()) {
        State s = new State(counter++,false);
        State t = new State(counter++,true);
        s.addTransition((char) tree.root, t);
        return new NFA(s, t);
    }else if (tree.root == CONCAT){
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
      State s = new State(counter++,false);
      State t = new State(counter++,true);
      s.addEpsilonTransition(left.start);
      s.addEpsilonTransition(right.start);
      left.accept.addEpsilonTransition(t);
      right.accept.addEpsilonTransition(t);
      return new NFA(s, t);
    } else if (tree.root == ETOILE){
      NFA child = toNFA(tree.subTrees.get(0));
      child.accept.addEpsilonTransition(child.start);
      
      State s = new State(counter++,false);
      State t = new State(counter++,true);
      s.addEpsilonTransition(child.start);
      s.addEpsilonTransition(t);
      child.accept.addEpsilonTransition(t);
      child.accept.isAccept = false;
      
      return new NFA(s, t);

    }
    throw new IllegalStateException("Operator cases not implemented yet: root=" + tree.root);

  }

  //FROM REGEX TO SYNTAX TREE
  private static RegExTree parse() throws Exception {
    //BEGIN DEBUG: set conditionnal to true for debug example
    if (false) throw new Exception();
    RegExTree example = exampleAhoUllman();
    if (false) return example;
    //END DEBUG

    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    for (int i=0;i<regEx.length();i++) 
        result.add(new RegExTree(charToRoot(regEx.charAt(i)),new ArrayList<RegExTree>()));
    
    return parse(result);
  }
  private static int charToRoot(char c) {
    if (c=='.') return DOT;
    if (c=='*') return ETOILE;
    if (c=='|') return ALTERN;
    if (c=='(') return PARENTHESEOUVRANT;
    if (c==')') return PARENTHESEFERMANT;
    return (int)c;
  }
  private static RegExTree parse(ArrayList<RegExTree> result) throws Exception {
    while (containParenthese(result)) result=processParenthese(result);
    while (containEtoile(result)) result=processEtoile(result);
    while (containConcat(result)) result=processConcat(result);
    while (containAltern(result)) result=processAltern(result);

    if (result.size()>1) throw new Exception();

    return removeProtection(result.get(0));
  }
  private static boolean containParenthese(ArrayList<RegExTree> trees) {
    for (RegExTree t: trees) if (t.root==PARENTHESEFERMANT || t.root==PARENTHESEOUVRANT) return true;
    return false;
  }
  private static ArrayList<RegExTree> processParenthese(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    for (RegExTree t: trees) {
      if (!found && t.root==PARENTHESEFERMANT) {
        boolean done = false;
        ArrayList<RegExTree> content = new ArrayList<RegExTree>();
        while (!done && !result.isEmpty())
          if (result.get(result.size()-1).root==PARENTHESEOUVRANT) { done = true; result.remove(result.size()-1); }
          else content.add(0,result.remove(result.size()-1));
        if (!done) throw new Exception();
        found = true;
        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
        subTrees.add(parse(content));
        result.add(new RegExTree(PROTECTION, subTrees));
      } else {
        result.add(t);
      }
    }
    if (!found) throw new Exception();
    return result;
  }
  private static boolean containEtoile(ArrayList<RegExTree> trees) {
    for (RegExTree t: trees) if (t.root==ETOILE && t.subTrees.isEmpty()) return true;
    return false;
  }
  private static ArrayList<RegExTree> processEtoile(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    for (RegExTree t: trees) {
      if (!found && t.root==ETOILE && t.subTrees.isEmpty()) {
        if (result.isEmpty()) throw new Exception();
        found = true;
        RegExTree last = result.remove(result.size()-1);
        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
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
    for (RegExTree t: trees) {
      if (!firstFound && t.root!=ALTERN) { firstFound = true; continue; }
      if (firstFound) if (t.root!=ALTERN) return true; else firstFound = false;
    }
    return false;
  }
  private static ArrayList<RegExTree> processConcat(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    boolean firstFound = false;
    for (RegExTree t: trees) {
      if (!found && !firstFound && t.root!=ALTERN) {
        firstFound = true;
        result.add(t);
        continue;
      }
      if (!found && firstFound && t.root==ALTERN) {
        firstFound = false;
        result.add(t);
        continue;
      }
      if (!found && firstFound && t.root!=ALTERN) {
        found = true;
        RegExTree last = result.remove(result.size()-1);
        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
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
    for (RegExTree t: trees) if (t.root==ALTERN && t.subTrees.isEmpty()) return true;
    return false;
  }
  private static ArrayList<RegExTree> processAltern(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    RegExTree gauche = null;
    boolean done = false;
    for (RegExTree t: trees) {
      if (!found && t.root==ALTERN && t.subTrees.isEmpty()) {
        if (result.isEmpty()) throw new Exception();
        found = true;
        gauche = result.remove(result.size()-1);
        continue;
      }
      if (found && !done) {
        if (gauche==null) throw new Exception();
        done=true;
        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
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
    if (tree.root==PROTECTION && tree.subTrees.size()!=1) throw new Exception();
    if (tree.subTrees.isEmpty()) return tree;
    if (tree.root==PROTECTION) return removeProtection(tree.subTrees.get(0));

    ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
    for (RegExTree t: tree.subTrees) subTrees.add(removeProtection(t));
    return new RegExTree(tree.root, subTrees);
  }
  
  //EXAMPLE
  // --> RegEx from Aho-Ullman book Chap.10 Example 10.25
  private static RegExTree exampleAhoUllman() {
    RegExTree a = new RegExTree((int)'a', new ArrayList<RegExTree>());
    RegExTree b = new RegExTree((int)'b', new ArrayList<RegExTree>());
    RegExTree c = new RegExTree((int)'c', new ArrayList<RegExTree>());
    ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
    subTrees.add(c);
    RegExTree cEtoile = new RegExTree(ETOILE, subTrees);
    subTrees = new ArrayList<RegExTree>();
    subTrees.add(b);
    subTrees.add(cEtoile);
    RegExTree dotBCEtoile = new RegExTree(CONCAT, subTrees);
    subTrees = new ArrayList<RegExTree>();
    subTrees.add(a);
    subTrees.add(dotBCEtoile);
    return new RegExTree(ALTERN, subTrees);
  }

  private static Set<State> epsilonClosure(Set<State> S) {
    Set<State> closure = new HashSet<>(S);          // inclure S dès le départ
    Deque<State> stack = new ArrayDeque<>(S);       // pile/queue de travail

    while (!stack.isEmpty()) {
        State s = stack.pop();
        for (State t : s.epsilonTransitions) {
            if (!closure.contains(t)) {
                closure.add(t);
                stack.push(t);  // explorer aussi les ε de ce nouvel état
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
    // retourne l’union des clés S_i.transitions.keySet() pour tous les états S_i ∈ S
    Set<Character> sCharacters = new HashSet<>();
    for(State s :S ){
      sCharacters.addAll(s.transitions.keySet());
    }
    return sCharacters;
  }

  public static String keyOf(Set<State> set) {
    if (set == null || set.isEmpty()) return "Ø"; // clé pour l’ensemble vide 
    List<Integer> ids = new ArrayList<>(set.size());
    for (State s : set) ids.add(s.id);
    Collections.sort(ids);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ids.size(); i++) {
        if (i > 0) sb.append(',');
        sb.append(ids.get(i));
    }
    return sb.toString(); // ex: "0,2,5"
  }

  public static DFA determinize(NFA nfa){
    DFA dfa = new DFA();
    Map<String,DfaState> registre = new HashMap<>();
    Set<State> startSet = new HashSet<>();
    startSet.add(nfa.start);
    Set<State> closure = epsilonClosure(startSet);
    int nextId = 0;
    DfaState start = registre.get(keyOf(closure));
    if (start == null){
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
            if (targetSet.isEmpty()) continue; // pas d’état

            
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



//UTILITARY CLASS
class RegExTree {
  protected int root;
  protected ArrayList<RegExTree> subTrees;
  public RegExTree(int root, ArrayList<RegExTree> subTrees) {
    this.root = root;
    this.subTrees = subTrees;
  }
  //FROM TREE TO PARENTHESIS
  public String toString() {
    if (subTrees.isEmpty()) return rootToString();
    String result = rootToString()+"("+subTrees.get(0).toString();
    for (int i=1;i<subTrees.size();i++) result+=","+subTrees.get(i).toString();
    return result+")";
  }
  private String rootToString() {
    if (root==RegEx.CONCAT) return ".";
    if (root==RegEx.ETOILE) return "*";
    if (root==RegEx.ALTERN) return "|";
    if (root==RegEx.DOT) return ".";
    return Character.toString((char)root);
  }
}