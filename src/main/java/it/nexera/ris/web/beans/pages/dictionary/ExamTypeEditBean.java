package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.ExamTypeWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named("examTypeEditBean")
@ViewScoped
public class ExamTypeEditBean extends EntityEditPageBean<ExamType> implements
        Serializable {

    private static final long serialVersionUID = -4689304292128601635L;

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getEntity().getDescription())) {
            addRequiredFieldExeption("form:description");
        } else if (checkDescriptionExistence(this.getEntity().getDescription())) {
            addFieldExeption("form:description", "descriptionAlreadyInUse");
        }
    }

    private boolean checkDescriptionExistence(String description)
            throws HibernateException, IllegalAccessException,
            PersistenceBeanException {
        List<ExamType> examTypes = null;
        if (this.getEntity().isNew()) {
            examTypes = DaoManager.load(ExamType.class);
        } else {
            examTypes = DaoManager.load(ExamType.class, new Criterion[]
                    {Restrictions.ne("id", this.getEntity().getId())});
        }
        if (!ValidationHelper.isNullOrEmpty(examTypes)) {
            for (ExamType type : examTypes) {
                if (type.getDescription().equals(description)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DaoManager.save(this.getEntity());
    }

    @Override
    public void afterSave() {
        /*refillListInSession("WorkListExamTypes");
        refillListInHttpSession("WorkListExamTypes");*/
        refillListInSession("AgendaExamTypes");
        super.afterSave();
    }

    private void refillListInSession(String nameInSession) {
        if (SessionHelper.get(nameInSession) != null) {
            ExamTypeWrapper etw = new ExamTypeWrapper(this.getEntity());
            etw.setSelected(Boolean.FALSE);

            @SuppressWarnings("unchecked")
            List<ExamTypeWrapper> list = (List<ExamTypeWrapper>) SessionHelper
                    .get(nameInSession);

            boolean exist = false;

            for (ExamTypeWrapper examTypeWrapper : list) {
                if (etw.getId() == examTypeWrapper.getId()) {
                    examTypeWrapper.setValue(this.getEntity().getDescription());
                    exist = true;
                    break;
                }
            }

            if (!exist) {
                list.add(etw);
            }

            SessionHelper.put(list, nameInSession);
        }
    }
    
    /*private void refillListInHttpSession(String nameInSession)
    {
        if (HttpSessionHelper.get(nameInSession) != null)
        {
            ExamTypeWrapper etw = new ExamTypeWrapper(this.getEntity());
            etw.setSelected(Boolean.FALSE);
            
            @SuppressWarnings("unchecked")
            List<ExamTypeWrapper> list = (List<ExamTypeWrapper>) HttpSessionHelper
            .get(nameInSession);
            
            boolean exist = false;
            
            for (ExamTypeWrapper examTypeWrapper : list)
            {
                if (etw.getId() == examTypeWrapper.getId())
                {
                    examTypeWrapper.setValue(this.getEntity().getDescription());
                    exist = true;
                    break;
                }
            }
            
            if (!exist)
            {
                list.add(etw);
            }
            
            HttpSessionHelper.put(list, nameInSession);
        }
    }*/
}
