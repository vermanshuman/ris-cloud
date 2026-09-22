package it.nexera.ris.web.beans.wrappers;

import javax.el.MethodExpression;
import java.io.Serializable;

public class FixedAjaxBehaviorStateHolder implements Serializable {
    private static final long serialVersionUID = -1420465850872729133L;

    private String update;

    private String process;

    private String onComplete;

    private String onError;

    private String onSuccess;

    private String onStart;

    private MethodExpression listener;

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getOnComplete() {
        return onComplete;
    }

    public void setOnComplete(String onComplete) {
        this.onComplete = onComplete;
    }

    public String getOnError() {
        return onError;
    }

    public void setOnError(String onError) {
        this.onError = onError;
    }

    public String getOnSuccess() {
        return onSuccess;
    }

    public void setOnSuccess(String onSuccess) {
        this.onSuccess = onSuccess;
    }

    public String getOnStart() {
        return onStart;
    }

    public void setOnStart(String onStart) {
        this.onStart = onStart;
    }

    public MethodExpression getListener() {
        return listener;
    }

    public void setListener(MethodExpression listener) {
        this.listener = listener;
    }
}
