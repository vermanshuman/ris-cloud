package it.nexera.ris.web.services.base;

import it.nexera.ris.common.helpers.LogHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseService implements Runnable {

    private static final int SLEEP_TIME_MULTIPLIER = 30;

    protected final Logger log = LogManager.getLogger(getClass());

    protected volatile Object monitor;

    protected volatile long sleepTimeMs;

    protected boolean stopFlag;

    private ExecutorService executorService;

    protected String name;

    private boolean isRunning;

    private Date lastStartTime;

    public BaseService(String name) {
        stopFlag = true;
        this.name = name;

        monitor = new Object();
    }

    public void start() {
        executorService = Executors
                .newSingleThreadExecutor(new ThreadFactoryEx(name));
        System.out.println("Running " + name + "...");
        stopFlag = false;
        executorService.execute(this);
    }

    public void stop() {
        stopFlag = true;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        isRunning = true;
        synchronized (monitor) {
            try {
                monitor.wait(1 * 30 * 1000);
            } catch (InterruptedException e) {
                isRunning = false;
                System.out.println("interrupted.. " + name);
                return;
            }
        }

        // Do not use while(true)
        int i = 0;

        while (i < 10) {
            lastStartTime = new Date();
            if (monitor == null || stopFlag) {
                isRunning = false;
                return;
            }

            //LogHelper.debugInfo(log, this.name + " started");

            runInternal();

            if (monitor != null && !stopFlag) {
                synchronized (monitor) {
                    try {
                        log.info("For service " + name + " sleep time is " + sleepTimeMs);
                        monitor.wait(sleepTimeMs);
                    } catch (InterruptedException e) {
                        isRunning = false;
                        System.out.println("interrupted.. " + name);
                        return;
                    }
                }
            } else {
                // Object will be destroyed
                isRunning = false;
                onDestroy();
                return;
            }

            i++;
            if (i > 5) {
                i = 0;
            }
        }
        isRunning = false;
    }

    protected void runInternal() {
        try {
            preRoutineFuncInternal();
            preRoutineFunc();
            routineFunc();
            postRoutineFunc();

        } catch (Exception e) {
            isRunning = false;
            LogHelper.log(log, e);
        } finally {
            postRoutineFuncInternal();
        }
    }

    protected void onDestroy() {

    }

    private void preRoutineFuncInternal() {
        if (stopFlag) {
            return;
        }

        updateSleepTime();
    }

    protected void postRoutineFuncInternal() {
        if (stopFlag) {
            return;
        }
    }

    protected final void routineFunc() {
        if (stopFlag) {
            return;
        }

        routineFuncInternal();
    }

    protected abstract void routineFuncInternal();

    protected void preRoutineFunc() {
        if (stopFlag) {
            return;
        }
    }

    protected void postRoutineFunc() {
        if (stopFlag) {
            return;
        }
    }

    // Poll time methods

    protected abstract int getPollTimeKey();

    protected void updateSleepTime() {
        System.out.println("BASE POLLL");
        sleepTimeMs = getPollTimeKey() * SLEEP_TIME_MULTIPLIER * 1000;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        if (monitor != null) {
            synchronized (monitor) {
                monitor = null;
            }
        }

        super.finalize();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public Date getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(Date lastStartTime) {
        this.lastStartTime = lastStartTime;
    }

}
