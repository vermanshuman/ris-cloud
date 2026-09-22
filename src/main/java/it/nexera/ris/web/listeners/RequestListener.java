package it.nexera.ris.web.listeners;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ObjectReattachHelper;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.IndexedView;
import it.nexera.ris.web.beans.PageBean;
import it.nexera.ris.web.beans.session.SessionBean;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RequestListener implements PhaseListener {
    public static String HibernateSessionAttribute = "HibernateSessionAttribute";

    private static final long serialVersionUID = 860858096388185527L;

    private transient final Logger log = LogManager.getLogger(RequestListener.class);

    /* (non-Javadoc)
     * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void beforePhase(PhaseEvent event) {
        //             System.out.println(event.getPhaseId());
        try {
            String sourceComponentId = FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestParameterMap().get("javax.faces.source");
            if (!"documentGenerationCheckbox".equals(sourceComponentId)) {
                reattachObjects(event);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            System.out.println(e.getMessage());
            System.out.println(LogHelper.readStackTrace(e));
        }
    }

    /* (non-Javadoc)
     * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void afterPhase(PhaseEvent event) {
        //        System.out.println(event.getPhaseId());
    }

    @SuppressWarnings("unchecked")
    protected <T> T getManagedBean(String beanName, FacesContext facesContext,
                                   Class<T> calzz) {
        ELContext elCtx = facesContext.getELContext();
        ExpressionFactory ef = facesContext.getApplication()
                .getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(elCtx, beanName, calzz);
        return (T) ve.getValue(elCtx);
    }

    private void reattachObjects(PhaseEvent event) {
        for (Entry<String, Object> entry : FacesContext.getCurrentInstance()
                .getViewRoot().getViewMap().entrySet()) {
            if (entry.getValue() instanceof PageBean) {
                try {
                    entry.setValue(ObjectReattachHelper.getInstance()
                            .reattachBean((PageBean) entry.getValue()));
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
        }

        SessionBean sb = this.getManagedBean("#{sessionBean}",
                event.getFacesContext(), SessionBean.class);
        if (sb != null && sb.getSession() != null && sb.getViewState() != null) {
            reattachMap(sb.getViewState());
            reattachMap(sb.getSession());
        }
    }

    @SuppressWarnings("unchecked")
    private void reattachMap(Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equals("WorkListStates")
                    || entry.getKey().equals("WorkListExamTypes")
                    || entry.getKey().equals("WorkListUrgencies")
                    || entry.getKey().equals("AgendaStates")
                    || entry.getKey().equals("AgendaExamTypes")
                    || entry.getKey().equals("AgendaUrgencies")
                    || entry.getKey().equals("EventCalendarDiscardDay")) {
                continue;
            }

            if (entry.getValue() instanceof IEntity
                    && !(entry.getValue() instanceof IndexedView)) {
                try {
                    if (!((IEntity) entry.getValue()).isNew()) {
                        entry.setValue(ObjectReattachHelper.getInstance()
                                .reattachObject((IEntity) entry.getValue()));
                    } else {
                        IEntity newEntity = (IEntity) entry.getValue()
                                .getClass().newInstance();
                        ObjectReattachHelper.getInstance().mergeAllFields(
                                newEntity, (IEntity) entry.getValue());
                        entry.setValue(newEntity);
                    }
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            } else if (entry.getValue() instanceof List<?>) {
                if (((List<?>) entry.getValue()).size() > 0
                        && (((List<?>) entry.getValue()).get(0) instanceof IEntity && !(entry
                        .getValue() instanceof IndexedView))) {
                    List<IEntity> list = ((List<IEntity>) entry.getValue());

                    for (int i = 0; i < list.size(); i++) {
                        try {
                            if (!list.get(i).isNew()) {
                                list.set(i, ObjectReattachHelper.getInstance()
                                        .reattachObject(list.get(i)));
                            } else {
                                IEntity newEntity = list.get(i).getClass()
                                        .newInstance();
                                newEntity = ObjectReattachHelper.getInstance()
                                        .mergeAllFields(newEntity, list.get(i));
                                list.set(i, newEntity);
                            }
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }
                    }

                    entry.setValue(list);
                }
            }

            map.put(entry.getKey(), entry.getValue());
        }
    }

    /* (non-Javadoc)
     * @see javax.faces.event.PhaseListener#getPhaseId()
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.PROCESS_VALIDATIONS;
    }

}
