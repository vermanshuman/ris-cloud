package it.nexera.ris.web.beans.wrappers;

import org.primefaces.behavior.ajax.AjaxBehavior;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.BehaviorEvent;

public class AjaxBehaviorEx extends AjaxBehavior {
    @Override
    public Object saveState(FacesContext context) {
        final FixedAjaxBehaviorStateHolder stateHolder = new FixedAjaxBehaviorStateHolder();
        stateHolder.setUpdate(getUpdate());
        stateHolder.setProcess(getProcess());
        stateHolder.setOnComplete(getOncomplete());
        stateHolder.setOnError(getOnerror());
        stateHolder.setOnSuccess(getOnsuccess());
        stateHolder.setOnStart(getOnstart());
        stateHolder.setListener(getListener());

        if (initialStateMarked()) {
            return null;
        }

        return UIComponentBase.saveAttachedState(context, stateHolder);
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (state != null && context != null) {
            Object obj = UIComponentBase.restoreAttachedState(context, state);
            if (obj instanceof FixedAjaxBehaviorStateHolder) {
                FixedAjaxBehaviorStateHolder stateHolder = (FixedAjaxBehaviorStateHolder) obj;

                setUpdate(stateHolder.getUpdate());
                setProcess(stateHolder.getProcess());
                setOncomplete(stateHolder.getOnComplete());
                setOnerror(stateHolder.getOnError());
                setOnsuccess(stateHolder.getOnSuccess());
                setOnstart(stateHolder.getOnStart());
                setListener(stateHolder.getListener());
            }
        }
    }

    @Override
    public void broadcast(BehaviorEvent event) throws AbortProcessingException {
        ELContext eLContext = FacesContext.getCurrentInstance().getELContext();

        // Backward compatible implementation of listener invocation
        MethodExpression listener = getListener();
        if (listener != null) {
            try {
                listener.invoke(eLContext, new Object[]{
                        event
                });
            } catch (IllegalArgumentException exception) {
                listener.invoke(eLContext, new Object[0]);
            }
        }
    }
}
