package it.nexera.ris.common.executors;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.Action;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class ThreadExecutor {

    private static final Logger log = LogManager.getLogger(ThreadExecutor.class);

    public static void execute(final Action action) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    action.onBeforeExecute();
                    action.execute();
                    action.onSuccess();
                } catch (Exception e) {
                    try {
                        action.onException(e);
                    } catch (Exception e1) {
                        LogHelper.log(log, e);
                    }
                } finally {
                    action.onExecuted();
                }
            }
        }).start();
    }

}
