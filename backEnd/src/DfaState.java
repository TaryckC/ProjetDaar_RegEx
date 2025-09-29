import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DfaState {
    public int id;
    public Set<State> nfaSet;     // l’ensemble d’états NFA
    public boolean isAccept;

    public DfaState(int id, Set<State> nfaSet, boolean isAccept) {
        this.id = id;
        this.nfaSet = nfaSet;
        this.isAccept = isAccept;
    }

    @Override
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
