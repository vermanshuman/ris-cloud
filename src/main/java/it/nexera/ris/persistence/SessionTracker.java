package it.nexera.ris.persistence;

public class SessionTracker {
    private static SessionTracker instance;

    @SuppressWarnings("unused")
    private int count = 0;

    private Object monitor = new Object();

    public static synchronized SessionTracker getInstance() {
        if (instance == null) {
            instance = new SessionTracker();
        }

        return instance;
    }

    public void sessionOpening() {
        this.sessionOpening(null);
    }

    public void sessionClosing() {
        this.sessionClosing(null);
    }

    public void sessionOpening(String clazz) {
        synchronized (monitor) {
            count++;
        }
        //        System.out.println("Opening session... "
        //                + (clazz == null ? "" : String.format("from: %s", clazz))
        //                + " Opened sessions: " + count);
    }

    public void sessionClosing(String clazz) {
        synchronized (monitor) {
            count--;
        }
        //        System.out.println("Closing session... "
        //                + (clazz == null ? "" : String.format("from: %s", clazz))
        //                + " Opened sessions: " + count);
    }
}
