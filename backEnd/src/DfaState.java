import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DfaState {
    public int id;
    public Set<State> nfaSet; // NFA states
    public boolean isAccept;

    // Encapsulates a DFA state built from a set of NFA states.
    public DfaState(int id, Set<State> nfaSet, boolean isAccept) {
        this.id = id;
        this.nfaSet = nfaSet;
        this.isAccept = isAccept;
    }

    @Override
    // Provides a readable snapshot of the DFA state composition.
    public String toString() {
        List<Integer> ids = new ArrayList<>();
        if (nfaSet != null) {
            for (State state : nfaSet) {
                ids.add(state.id);
            }
            Collections.sort(ids);
        }
        return "DfaState{id=" + id + ", isAccept=" + isAccept + ", nfaSet=" + ids + "}";
    }
}
