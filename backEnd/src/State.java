import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State {
    public int id; // unique identifier
    public Map<Character, List<State>> transitions = new HashMap<>();
    public List<State> epsilonTransitions = new ArrayList<>();
    public boolean isAccept;

    // Creates a new NFA state with the specified identifier and accepting flag.
    public State(int id, boolean isAccept) {
        this.id = id;
        this.isAccept = isAccept;
    }

    // Adds a labeled transition to the target state.
    public void addTransition(char c, State target) {
        transitions.putIfAbsent(c, new ArrayList<>());
        transitions.get(c).add(target);
    }

    // Adds an epsilon transition to the target state.
    public void addEpsilonTransition(State target) {
        epsilonTransitions.add(target);
    }
}
