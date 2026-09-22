package it.nexera.ris.common.helpers;

public class ExecutionTimeHelper {

    private ExecutionTimeHelper() {
    }

    public static long getStartExecutionTime() {
        return System.nanoTime();
    }

    public static long getExecutionTime(long startTime) {
        long endTime = System.nanoTime();
        return endTime - startTime;
    }
}
