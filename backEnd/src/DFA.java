import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DFA {
    public Set<Character> alphabet = new HashSet<>();
    public DfaState start;
    public Set<DfaState> states = new HashSet<>();
    public Map<DfaState, Map<Character, DfaState>> trans = new HashMap<>();

    // Implémentation à partir de : https://en.wikipedia.org/wiki/DFA_minimization
    private void removeUnreachableStates() {
        if (start == null)
            return;
        Set<DfaState> reachableStates = new HashSet<>();
        Set<DfaState> newStates = new HashSet<>();
        reachableStates.add(start);
        newStates.add(start);

        do {
            Set<DfaState> temp = new HashSet<>();
            Set<DfaState> frontier = new HashSet<>(newStates);

            for (DfaState currentState : frontier) {
                Map<Character, DfaState> out = trans.get(currentState);
                if (out == null)
                    continue;
                for (Character c : alphabet) {
                    DfaState next = out.get(c);
                    if (next != null) {
                        temp.add(next);
                    }
                }
            }
            temp.removeAll(reachableStates);
            newStates = temp;
            reachableStates.addAll(newStates);
        } while (!newStates.isEmpty());

        states.removeIf(state -> !reachableStates.contains(state));
        trans.keySet().removeIf(s -> !reachableStates.contains(s));
        for (Map<Character, DfaState> m : trans.values()) {
            if (m == null)
                continue;
            m.entrySet().removeIf(e -> e.getValue() == null || !reachableStates.contains(e.getValue()));
        }
    }

    // Hopcroft's algorithm :
    private List<Set<DfaState>> mergingNondistinguishableStates() {
        Set<DfaState> nonFinals = new HashSet<>(states);
        Set<DfaState> finals = new HashSet<>(nonFinals);
        nonFinals.removeIf(currentState -> currentState.isAccept);
        finals.removeIf(currentState -> !currentState.isAccept);

        List<Set<DfaState>> P = new ArrayList<>();
        P.add(finals);
        P.add(nonFinals);
        List<Set<DfaState>> W = new ArrayList<>(P);

        while (!W.isEmpty()) {
            Set<DfaState> A = W.remove(0);
            for (Character c : alphabet) {
                Set<DfaState> X = getCharTransitionTowardsSubset(c, A);

                List<Set<DfaState>> P_copy = new ArrayList<>(P);

                for (Set<DfaState> Y : P) {

                    Set<DfaState> XinterY = new HashSet<>(Y);
                    XinterY.retainAll(X); // intersection : X inter Y

                    Set<DfaState> XremovedFromY = new HashSet<>(Y);
                    XremovedFromY.removeAll(X); // différence : Y \ X

                    if (XinterY.isEmpty() || XremovedFromY.isEmpty()) {
                        continue;
                    }

                    P_copy.remove(Y);
                    P_copy.add(XinterY);
                    P_copy.add(XremovedFromY);

                    if (W.contains(Y)) {
                        W.remove(Y);
                        W.add(XinterY);
                        W.add(XremovedFromY);
                    } else {
                        if (XinterY.size() <= XremovedFromY.size()) {
                            W.add(XinterY);
                        } else {
                            W.add(XremovedFromY);
                        }
                    }
                }
                P = P_copy;
            }
        }
        return P;
    }

    private Set<DfaState> getCharTransitionTowardsSubset(Character c, Set<DfaState> subSet) {
        Set<DfaState> res = new HashSet<>();
        for (DfaState state : states) {
            Map<Character, DfaState> transtion = trans.get(state);
            if (transtion == null)
                continue;
            DfaState nextState = transtion.get(c);
            if (nextState == null)
                continue;
            if (subSet.contains(nextState))
                res.add(state);
        }
        return res;
    }

    public static DFA updateDFA(DFA dfa) {
        DFA res = new DFA();
        dfa.removeUnreachableStates();
        List<Set<DfaState>> nondistinguishableStates = dfa.mergingNondistinguishableStates();

        Map<DfaState, DfaState> oldToNew = new HashMap<>();
        Set<DfaState> newDFAStates = new HashSet<>();
        int counter = 0;

        for (Set<DfaState> set : nondistinguishableStates) {
            if (set.isEmpty())
                continue;
            DfaState newDFAstate = new DfaState(counter++, new HashSet<>(), false);
            boolean accept = false;
            for (DfaState current : set) {
                oldToNew.put(current, newDFAstate);
                if (dfa.start == current)
                    res.start = newDFAstate;
                newDFAstate.nfaSet.addAll(current.nfaSet);
                if (current.isAccept)
                    accept = true;
            }
            newDFAstate.isAccept = accept;
            newDFAStates.add(newDFAstate);
        }

        res.alphabet = dfa.alphabet;
        res.states = newDFAStates;
        res.trans = new HashMap<>();

        for (Set<DfaState> block : nondistinguishableStates) {
            if (block.isEmpty())
                continue;

            DfaState repOld = block.iterator().next();
            DfaState repNew = oldToNew.get(repOld);
            Map<Character, DfaState> newOut = new HashMap<>();

            for (Character c : res.alphabet) {
                DfaState targetOld = null;

                for (DfaState s : block) {
                    Map<Character, DfaState> out = dfa.trans.get(s);
                    if (out == null)
                        continue;
                    DfaState t = out.get(c);
                    if (t != null) {
                        targetOld = t;
                        break;
                    }
                }

                if (targetOld != null) {
                    DfaState targetNew = oldToNew.get(targetOld);
                    if (targetNew != null) {
                        newOut.put(c, targetNew);
                    }
                }
            }

            res.trans.put(repNew, newOut);
        }
        return res;
    }

    public void printDFA() {
        System.out.println("Alphabet: " + alphabet);
        System.out.println("Start state: " + (start != null ? start.id : "null"));
        System.out.println("States:");
        for (DfaState s : states) {
            System.out.println("  State " + s.id + (s.isAccept ? " (accepting)" : ""));
        }
        System.out.println("Transitions:");
        for (Map.Entry<DfaState, Map<Character, DfaState>> e : trans.entrySet()) {
            DfaState from = e.getKey();
            Map<Character, DfaState> map = e.getValue();
            if (map == null)
                continue;
            for (Map.Entry<Character, DfaState> t : map.entrySet()) {
                System.out.println("  " + from.id + " --" + t.getKey() + "--> " + t.getValue().id);
            }
        }
    }

    // TODO : We should've created a new Class for minimized DFA.
    // Check if a DFA can accept text (Has a start, Has at least a final state, can
    // transition from start to final state)
    // Precondition : the dfa is minimized (there is no unreachable state)
    public boolean validateDFAFortextMatching() {
        if (start == null)
            return false;
        return states.stream().anyMatch(state -> state.isAccept); // Since the DFA is minimized if there's at least a
                                                                  // final state, then we can access it.
    }

    // Returns an array where the first element represent the start of the validated
    // word end the second element the end of the word
    public int[] validateString(Character[] characters) {
        if (!validateDFAFortextMatching())
            return new int[] { 0, -1 }; // No match found
        int[] res = exploreFrom(characters, start, 0);
        return res;
    }

    public boolean matches(Character[] characters) {
        if (!validateDFAFortextMatching()) {
            return false;
        }
        if (start != null && start.isAccept) {
            return true;
        }
        int[] result = exploreFrom(characters, start, 0);
        if (result[1] != -1) {
            return true;
        }
        return false;
    }

    public int[] exploreFrom(Character[] characters, DfaState startNode, int startIndex) {
        int[] res = new int[2];
        int matchStart = startIndex;
        res[0] = matchStart; // Starting index of the current attempt
        res[1] = -1; // Ending index of the word
        DfaState currentNode = startNode;
        for (int i = startIndex; i < characters.length; i++) {
            Map<Character, DfaState> out = trans.get(currentNode);
            if (out == null)
                continue;
            DfaState nextNode = out.get(characters[i]);
            DfaState anyTransition = out.get((char) RegEx.DOT);
            if (nextNode != null && anyTransition != null) {
                int[] res_bis = exploreFrom(characters, nextNode, i + 1);
                if (res_bis[1] != -1)
                    return res_bis;
                else
                    nextNode = anyTransition;
            }
            if (nextNode == null && anyTransition != null)
                nextNode = anyTransition;
            if (nextNode == null) {
                if (currentNode.isAccept) {
                    res[1] = i - 1;
                    return res;
                } else {
                    currentNode = start;
                    matchStart++;
                    if (matchStart >= characters.length) {
                        res[0] = matchStart;
                        break;
                    }
                    res[0] = matchStart;
                    i = matchStart - 1; // re-evaluate current character from the new start
                }
            } else {
                if (currentNode.isAccept) {
                    res[1] = i;
                    return res;
                }
                currentNode = nextNode;
            }
        }
        if (currentNode.isAccept)
            res[1] = characters.length - 1;
        return res;
    }

}
