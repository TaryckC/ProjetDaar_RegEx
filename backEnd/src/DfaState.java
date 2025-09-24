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

   
}


