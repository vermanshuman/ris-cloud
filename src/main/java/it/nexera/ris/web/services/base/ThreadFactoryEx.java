package it.nexera.ris.web.services.base;

import java.util.concurrent.ThreadFactory;

public class ThreadFactoryEx implements ThreadFactory {
    private String name;

    public ThreadFactoryEx(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, name);
    }

}
