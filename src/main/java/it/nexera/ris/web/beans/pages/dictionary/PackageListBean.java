package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Package;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.PackageRadiologyExam;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Named("packageListBean")
@ViewScoped
public class PackageListBean extends EntityLazyListPageBean<Package> implements
        Serializable {

    private static final long serialVersionUID = -2642325745379881448L;

    private String entityCode;

    private String entityDescription;

    private List<SelectItem> examTypes;

    private List<String> selectedExamTypes;

    private List<SelectItem> examTypesCopy;

    private String labName;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setExamTypes(ComboboxHelper.fillList(ExamType.class, true));

        this.setExamTypesCopy(new ArrayList<SelectItem>());
        for (ExamType et : DaoManager.load(ExamType.class)) {
            this.getExamTypesCopy().add(
                    new SelectItem(et.getId(), et.getDescription()));
        }

        this.loadList(Package.class, new Order[]{
                Order.asc("code")
        });
    }

    public void prepareToCopy() {
        Iterator<Package> iterator = this.getLazyModel().iterator();
        while (iterator.hasNext()) {
            Package pack = (Package) iterator.next();
            if (pack.getId().equals(this.getEntityEditId())) {
                this.setEntityCode(pack.getCode());
                this.setEntityDescription(pack.getDescription());
                this.setSelectedExamTypes(new ArrayList<String>());
                break;
            }
        }
    }

    public void makeCopy() {
        if (validate()) {
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
            return;
        }
        Iterator<Package> iterator = this.getLazyModel().iterator();
        while (iterator.hasNext()) {
            Package pack = (Package) iterator.next();
            if (pack.getId().equals(this.getEntityEditId())) {
                Transaction tr = null;
                try {
                    tr = DaoManager.getSession().beginTransaction();
                    for (String examTypeId : this.getSelectedExamTypes()) {
                        Package copy = pack.clone();
                        if (!copy.getCode().equals(this.getEntityCode())) {
                            copy.setCode(getEntityCode());
                        }
                        if (!copy.getDescription().equals(
                                this.getEntityDescription())) {
                            copy.setDescription(this.getEntityDescription());
                        }
                        copy.setExamType(DaoManager.get(ExamType.class,
                                Long.parseLong(examTypeId)));
                        DaoManager.save(copy);
                        for (PackageRadiologyExam exam : copy
                                .getRadiologyExams()) {
                            DaoManager.save(exam);
                        }
                    }
                } catch (Exception e) {
                    if (tr != null) {
                        tr.rollback();
                    }
                    LogHelper.log(log, e);
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            "",
                            ResourcesHelper.getString("objectCopyException"));
                } finally {
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        try {
                            tr.commit();
                            this.onLoad();
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                            MessageHelper.addGlobalMessage(
                                    FacesMessage.SEVERITY_ERROR, "",
                                    ResourcesHelper
                                            .getString("objectCopyException"));
                        }
                    }
                }
                break;
            }
        }
    }

    private boolean validate() {
        boolean validationFailed = false;
        if (ValidationHelper.isNullOrEmpty(this.getEntityCode())) {
            this.addRequiredFieldExeption("code");
            validationFailed = true;
        } else {
            cleanFieldExeption("form:code");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntityDescription())) {
            this.addRequiredFieldExeption("description");
            validationFailed = true;
        } else {
            cleanFieldExeption("form:description");
        }
        if (ValidationHelper.isNullOrEmpty(this.getSelectedExamTypes())) {
            this.addRequiredFieldExeption("examTypes");
            validationFailed = true;
        } else {
            cleanFieldExeption("form:examTypes");
        }
        return validationFailed;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public String getEntityDescription() {
        return entityDescription;
    }

    public void setEntityDescription(String entityDescription) {
        this.entityDescription = entityDescription;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public List<SelectItem> getExamTypes() {
        return examTypes;
    }

    public void setExamTypes(List<SelectItem> examTypes) {
        this.examTypes = examTypes;
    }

    public List<String> getSelectedExamTypes() {
        return selectedExamTypes;
    }

    public void setSelectedExamTypes(List<String> selectedExamTypes) {
        this.selectedExamTypes = selectedExamTypes;
    }

    public List<SelectItem> getExamTypesCopy() {
        return examTypesCopy;
    }

    public void setExamTypesCopy(List<SelectItem> examTypesCopy) {
        this.examTypesCopy = examTypesCopy;
    }
}
