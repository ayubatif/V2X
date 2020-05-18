package v2x;

public class ThreadCommunication {
    private volatile boolean ready;

    public ThreadCommunication(boolean ready) {
        this.ready = ready;
    }

    public synchronized void setReady(boolean ready) {
        this.ready = ready;
    }

    public synchronized boolean getReady() {
        return this.ready;
    }
}
