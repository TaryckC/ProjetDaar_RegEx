public class NFA {
    public State start;
    public State accept;

    // Builds a simple NFA wrapper with explicit start and accept states.
    public NFA(State start, State accept) {
        this.start = start;
        this.accept = accept;
    }

}
