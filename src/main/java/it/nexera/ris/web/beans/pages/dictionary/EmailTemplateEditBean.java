package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.comparators.SelectItemComparator;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.MailHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.EmailTemplate;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.PrimeFaces;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Named("emailTemplateEditBean")
@ViewScoped
public class EmailTemplateEditBean extends EntityEditPageBean<EmailTemplate> implements Serializable {

    public static final String EMAIL_TEMPLATE_ID_SESSION_ID = "emailTemplateId";

    private List<SelectItem> tags;

    private String selectedTag;

    private String insertTag;

    @Override
    public void setEntityId(Long entityId) {
        String emailTemplateId = (String) SessionHelper.get(EMAIL_TEMPLATE_ID_SESSION_ID);
        if (emailTemplateId != null) {
            SessionHelper.removeObject(EMAIL_TEMPLATE_ID_SESSION_ID);
        }
        super.setEntityId(emailTemplateId != null ? Long.valueOf(emailTemplateId) : entityId);
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
        this.setTags(ComboboxHelper.fillList(MailHelper.EmailTag.class));
        Collections.sort(this.getTags(), new SelectItemComparator());
    }


    public void addTagBody() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedTag())) {
            for (MailHelper.EmailTag tag : MailHelper.EmailTag.values()) {
                if (tag.getId().equals(Long.parseLong(getSelectedTag()))) {
                    this.setInsertTag(tag.getTag());
                    break;
                }
            }
        } else {
            this.setInsertTag("");
        }
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException {

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        DaoManager.save(getEntity());
    }

    @Override
    public void goBack() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public PageTypes getPageType() {
        return PageTypes.EMAIL_TEMPLATE_EDIT;
    }

    public List<SelectItem> getTags() {
        return tags;
    }

    public void setTags(List<SelectItem> tags) {
        this.tags = tags;
    }

    public String getSelectedTag() {
        return selectedTag;
    }

    public void setSelectedTag(String selectedTag) {
        this.selectedTag = selectedTag;
    }

    public String getInsertTag() {
        return insertTag;
    }

    public void setInsertTag(String insertTag) {
        this.insertTag = insertTag;
    }
}

