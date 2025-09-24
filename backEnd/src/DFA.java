import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DFA {
    public DfaState start;
    public Set<DfaState> states = new HashSet<>();
    public Map<DfaState, Map<Character, DfaState>> trans = new HashMap<>();
}
