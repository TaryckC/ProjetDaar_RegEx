import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State {
    public int id; // identifiant unique
    public Map<Character, List<State>> transitions = new HashMap<>();
    public List<State> epsilonTransitions = new ArrayList<>();
    public boolean isAccept;
    

    public State(int id, boolean isAccept) {
        this.id = id;
        this.isAccept= isAccept;
    }

    public void addTransition(char c, State target){
        transitions.putIfAbsent(c, new ArrayList<>());
        transitions.get(c).add(target);
    }

    public void addEpsilonTransition(State target){
        epsilonTransitions.add(target);
    }
}
