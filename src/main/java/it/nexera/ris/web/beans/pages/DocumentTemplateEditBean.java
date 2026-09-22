package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.comparators.SelectItemComparator;
import it.nexera.ris.common.enums.DocumentGenerationTags;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.DocumentTemplate;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TemplateDocumentModel;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("documentTemplateEditBean")
@ViewScoped
public class DocumentTemplateEditBean extends
        EntityEditPageBean<DocumentTemplate> implements Serializable {

    private static final long serialVersionUID = -8408355464215707746L;

    private Long modelType;

    private List<SelectItem> modelTypes;

    private List<SelectItem> tags;

    private String selectedTag;

    private String selectedTagFooter;

    private String selectedTagHeader;

    private String insertTag;

    //NUMBER OF THE ZOOM DROPDOWN INSIDE THE CKEDITOR TOOLBAR
    private static final long PDF_ZOOM_NUMBER = 9l;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        fillDropDown();
        fillValueFromEntity();
    }

    private void fillDropDown() {
        try {
            this.setModelTypes(ComboboxHelper
                    .fillList(TemplateDocumentModel.class));
            this.setTags(ComboboxHelper.fillList(DocumentGenerationTags.class));
            java.util.Collections.sort(this.getTags(),
                    new SelectItemComparator());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void fillValueFromEntity() {
        if ((this.getModelType() == null || this.getModelType() == 0)
                && this.getEntity().getModel() != null) {
            this.setModelType(this.getEntity().getModel().getId());
        }
    }

    public void addTagBody() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedTag())) {
            for (DocumentGenerationTags tag : DocumentGenerationTags.values()) {
                if (tag.name().equals(getSelectedTag())) {
                    this.setInsertTag(tag.getTag());
                    break;
                }
            }
        } else {
            this.setInsertTag("");
        }
    }

    public void addTagHeader() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedTagHeader())) {
            for (DocumentGenerationTags tag : DocumentGenerationTags.values()) {
                if (tag.name().equals(getSelectedTagHeader())) {
                    this.setInsertTag(tag.getTag());
                    break;
                }
            }
        } else {
            this.setInsertTag("");
        }
    }

    public void addTagFooter() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedTagFooter())) {
            for (DocumentGenerationTags tag : DocumentGenerationTags.values()) {
                if (tag.name().equals(getSelectedTagFooter())) {
                    this.setInsertTag(tag.getTag());
                    break;
                }
            }
        } else {
            this.setInsertTag("");
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldExeption("name");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getName())
                && this.getEntity().isNew()) {
            try {
                DocumentTemplate model = DaoManager.get(DocumentTemplate.class,
                        new Criterion[]{
                                Restrictions.eq("name", this.getEntity().getName())
                        });
                if (model != null) {
                    addFieldExeption("name", "nameTaken");
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
                addFieldExeption("name", "nameTaken");
            }
        }

        if (ValidationHelper.isNullOrEmpty(this.getModelType())) {
            addRequiredFieldExeption("modelType");
        }

        if (this.getEntity().getHeader()) {
            if (ValidationHelper.isNullOrEmpty(this.getEntity()
                    .getHeaderContent())
                    || this.getEntity().getHeaderContent().equals("<br/>")) {
                addRequiredFieldExeption("headerValidator");
            } else if (this.getEntity().getHeaderContent().length() > 65500) {
                addFieldExeption("headerValidator", "documentTemplateTooLarge");

                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper.getValidation("docTemplatesHeader"),
                        ResourcesHelper
                                .getValidation("documentTemplateTooLarge"));
            }

            if (ValidationHelper.isNullOrEmpty(this.getEntity()
                    .getHeaderHeight())) {
                addRequiredFieldExeption("spinnerHeaderHeight");
            } else if (this.getEntity().getHeaderHeight() < 10l) {
                addFieldExeption("spinnerHeaderHeight", "valueLessThan10");
            }
        }

        if (this.getEntity().getFooter()) {
            if (ValidationHelper.isNullOrEmpty(this.getEntity()
                    .getFooterContent())
                    || this.getEntity().getFooterContent().equals("<br/>")) {
                addRequiredFieldExeption("footerValidator");
            } else if (this.getEntity().getFooterContent().length() > 65500) {
                addFieldExeption("footerValidator", "documentTemplateTooLarge");

                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper.getValidation("docTemplatesFooter"),
                        ResourcesHelper
                                .getValidation("documentTemplateTooLarge"));
            }

            if (ValidationHelper.isNullOrEmpty(this.getEntity()
                    .getFooterHeight())) {
                addRequiredFieldExeption("spinnerFooterHeight");
            } else if (this.getEntity().getFooterHeight() < 10l) {
                addFieldExeption("spinnerFooterHeight", "valueLessThan10");
            }
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getBodyContent())
                || this.getEntity().getBodyContent().equals("<br/>")) {
            addRequiredFieldExeption("bodyValidator");
        } else if (this.getEntity().getBodyContent().length() > 65500) {
            addFieldExeption("bodyValidator", "documentTemplateTooLarge");

            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("docTemplatesBody"),
                    ResourcesHelper.getValidation("documentTemplateTooLarge"));
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getWidth())) {
            addRequiredFieldExeption("spinnerPageWidth");
        } else if (this.getEntity().getWidth() < 60l) {
            addFieldExeption("spinnerPageWidth", "valueLessThan60");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getHeight())) {
            addRequiredFieldExeption("spinnerPageHeight");
        } else if (this.getEntity().getHeight() < 60l) {
            addFieldExeption("spinnerPageHeight", "valueLessThan60");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getMarginTop())) {
            addRequiredFieldExeption("spinnerMarginTop");
        } else if (this.getEntity().getMarginTop() < 0l) {
            addFieldExeption("spinnerMarginTop", "valueLessThanZero");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getMarginBottom())) {
            addRequiredFieldExeption("spinnerMarginBottom");
        } else if (this.getEntity().getMarginBottom() < 0l) {
            addFieldExeption("spinnerMarginBottom", "valueLessThanZero");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getMarginLeft())) {
            addRequiredFieldExeption("spinnerMarginLeft");
        } else if (this.getEntity().getMarginLeft() < 0l) {
            addFieldExeption("spinnerMarginLeft", "valueLessThanZero");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getMarginRight())) {
            addRequiredFieldExeption("spinnerMarginRight");
        } else if (this.getEntity().getMarginRight() < 0l) {
            addFieldExeption("spinnerMarginRight", "valueLessThanZero");
        }

        if (!this.getValidationFailed()) {
            if (this.getEntity().getWidth() < (this.getEntity()
                    .getMarginRight() + this.getEntity().getMarginLeft())) {
                addFieldExeption("spinnerMarginRight", "smallDocWidth");
                addFieldExeption("spinnerMarginLeft", "smallDocWidth", false);
                addFieldExeption("spinnerPageWidth", "smallDocWidth", false);
            }

            Long height = this.getEntity().getHeight()
                    - this.getEntity().getMarginBottom()
                    - this.getEntity().getMarginTop();

            if (this.getEntity().getHeader()) {
                height -= this.getEntity().getHeaderHeight();
            }
            if (this.getEntity().getFooter()) {
                height -= this.getEntity().getFooterHeight();
            }

            if (height <= 0l) {
                addFieldExeption("spinnerMarginTop", "smallDocHeight");
                addFieldExeption("spinnerMarginBottom", "smallDocHeight", false);
                addFieldExeption("spinnerPageHeight", "smallDocHeight", false);

                if (this.getEntity().getHeader()) {
                    addFieldExeption("spinnerHeaderHeight", "smallDocHeight");
                }
                if (this.getEntity().getFooter()) {
                    addFieldExeption("spinnerFooterHeight", "smallDocHeight");
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        this.getEntity()
                .setModel(
                        DaoManager.get(TemplateDocumentModel.class,
                                this.getModelType()));

        if (this.getEntity().getHeader()) {
            this.getEntity().setHeaderContent(
                    this.replaceLocaleImagesUrl(this.getEntity()
                            .getHeaderContent()));
        }

        this.getEntity().setBodyContent(
                this.replaceLocaleImagesUrl(this.getEntity().getBodyContent()));

        if (this.getEntity().getFooter()) {
            this.getEntity().setFooterContent(
                    this.replaceLocaleImagesUrl(this.getEntity()
                            .getFooterContent()));
        }

        DaoManager.save(this.getEntity());
    }

    private String replaceLocaleImagesUrl(String text) {
        String correctUrl = this.getRequest().getHeader("referer");

        if (!ValidationHelper.isNullOrEmpty(correctUrl)
                && correctUrl.contains(this.getCurrentPage().getPagesContext())) {
            correctUrl = "src=\""
                    + correctUrl.substring(0, correctUrl.indexOf(this
                    .getCurrentPage().getPagesContext()));
            return text.replaceAll("src=\"../", correctUrl);
        }

        return text;
    }

    public Long getModelType() {
        return modelType;
    }

    public void setModelType(Long modelType) {
        this.modelType = modelType;
    }

    public List<SelectItem> getModelTypes() {
        return modelTypes;
    }

    public void setModelTypes(List<SelectItem> modelTypes) {
        this.modelTypes = modelTypes;
    }

    public List<SelectItem> getTagsEx() {
        List<SelectItem> list = new ArrayList<SelectItem>();
        for (SelectItem item : tags) {
            if (!item.getValue().equals(DocumentGenerationTags.PAGE_BREAK)) {
                list.add(item);
            }
        }
        return list;
    }

    public void setTagsEx(List<SelectItem> modelTypes) {
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

    public String getSelectedTagFooter() {
        return selectedTagFooter;
    }

    public void setSelectedTagFooter(String selectedTagFooter) {
        this.selectedTagFooter = selectedTagFooter;
    }

    public String getSelectedTagHeader() {
        return selectedTagHeader;
    }

    public void setSelectedTagHeader(String selectedTagHeader) {
        this.selectedTagHeader = selectedTagHeader;
    }

    public String getInsertTag() {
        return insertTag;
    }

    public void setInsertTag(String insertTag) {
        this.insertTag = insertTag;
    }
}
