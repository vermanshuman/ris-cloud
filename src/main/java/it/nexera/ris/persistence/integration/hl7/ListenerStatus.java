package it.nexera.ris.persistence.integration.hl7;

public class ListenerStatus {
    private int port;

    private boolean isRunning;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}
