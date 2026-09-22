package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Urgency;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.UrgencyWrapper;
import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.primefaces.model.file.UploadedFile;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named("urgencyEditBean")
@ViewScoped
public class UrgencyEditBean extends EntityEditPageBean<Urgency> implements
        Serializable {

    private static final long serialVersionUID = 7303192533413841795L;

    private UploadedFile iconFile;

    private String iconName;

    private byte[] iconContent;

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (!getEntity().isNew()) {
            setIconName(getEntity().getIconName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCode())) {
            addRequiredFieldExeption("form:code");
        } else if (!ValidationHelper.isUnique(Urgency.class, "code", getEntity()
                .getCode(), getEntity().getId())) {
            addFieldExeption("form:code", "codeAlreadyInUse");
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getDescription())) {
            addRequiredFieldExeption("form:description");
        }

        if (getEntity().getPriority() == null) {
            addRequiredFieldExeption("form:priority");
        }

        if (ValidationHelper.isNullOrEmpty(getIconName())) {
            addRequiredFieldExeption("form:icon");
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getFilterCode())) {
            addRequiredFieldExeption("form:filterCode");
        }
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
        if (getIconName().contains(" ")) {
            this.getEntity().setIconName(getIconName().replaceAll(" ", "_"));
        } else {
            this.getEntity().setIconName(getIconName());
        }

        this.getEntity().setIconContent(getIconContent());
        this.getEntity().setIconHashCode(
                getEntity().calculateIconHashCode(getIconName(),
                        getIconContent()));

        DaoManager.save(this.getEntity());
    }

    @Override
    public void afterSave() {
        /*refillListInSession("WorkListUrgencies");
        refillListInHttpSession("WorkListUrgencies");*/
        refillListInSession("AgendaUrgencies");
        super.afterSave();
    }

    private void refillListInSession(String nameInSession) {
        if (SessionHelper.get(nameInSession) != null) {
            UrgencyWrapper uw = new UrgencyWrapper(this.getEntity());
            uw.setSelected(Boolean.FALSE);

            @SuppressWarnings("unchecked")
            List<UrgencyWrapper> list = (List<UrgencyWrapper>) SessionHelper
                    .get(nameInSession);

            boolean exist = false;

            for (UrgencyWrapper urgencyWrapper : list) {
                if (uw.getId() == urgencyWrapper.getId()) {
                    urgencyWrapper.setValue(this.getEntity().getDescription());
                    exist = true;
                    break;
                }
            }

            if (!exist) {
                list.add(uw);
            }

            SessionHelper.put(list, nameInSession);
        }
    }
    
    /*private void refillListInHttpSession(String nameInSession)
    {
        if (HttpSessionHelper.get(nameInSession) != null)
        {
            UrgencyWrapper uw = new UrgencyWrapper(this.getEntity());
            uw.setSelected(Boolean.FALSE);
            
            @SuppressWarnings("unchecked")
            List<UrgencyWrapper> list = (List<UrgencyWrapper>) HttpSessionHelper
            .get(nameInSession);
            
            boolean exist = false;
            
            for (UrgencyWrapper urgencyWrapper : list)
            {
                if (uw.getId() == urgencyWrapper.getId())
                {
                    urgencyWrapper.setValue(this.getEntity().getDescription());
                    exist = true;
                    break;
                }
            }
            
            if (!exist)
            {
                list.add(uw);
            }
            
            HttpSessionHelper.put(list, nameInSession);
        }
    }*/

    public void uploadIcon() {
        if (this.getIconFile() != null) {
            this.setIconName(this.getIconFile().getFileName());

            try {
                this.setIconContent(IOUtils.toByteArray(this.getIconFile()
                        .getInputStream()));

                FileHelper.writeFileToLocalTemp(this.getIconName(),
                        this.getIconContent());
            } catch (IOException e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void clearIcon() {
        this.setIconName("");
        this.setIconContent(null);
        this.getEntity().setIconName("");
        this.getEntity().setIconContent(null);
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public UploadedFile getIconFile() {
        return iconFile;
    }

    public void setIconFile(UploadedFile iconFile) {
        this.iconFile = iconFile;
    }

    public byte[] getIconContent() {
        return iconContent;
    }

    public void setIconContent(byte[] iconContent) {
        this.iconContent = iconContent;
    }

}
