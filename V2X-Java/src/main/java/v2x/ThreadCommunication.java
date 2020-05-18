package v2x;

public class ThreadCommunication {
    private volatile boolean ready;

    public ThreadCommunication(boolean ready) {
        this.ready = ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean getReady() {
        return this.ready;
    }
}
