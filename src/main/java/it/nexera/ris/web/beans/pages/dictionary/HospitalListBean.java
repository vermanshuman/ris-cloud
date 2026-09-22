package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Hospital;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("hospitalListBean")
@ViewScoped
public class HospitalListBean extends EntityLazyListPageBean<Hospital>
        implements Serializable {

    private static final long serialVersionUID = -5299709409765272136L;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.loadList(Hospital.class, new Order[]{
                Order.desc("createDate")
        });
    }

    @Override
    public void deleteEntity() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        //if this can be done with jpa or hibernate annotations please fix 
        if (this.getEntityDeleteId() != null) {
            Transaction tr = null;
            try {
                tr = PersistenceSessionManager.getBean().getSession()
                        .beginTransaction();

                Hospital hospital = DaoManager.get(Hospital.class,
                        this.getEntityDeleteId());

                for (Sector sector : hospital.getSectors()) {
                    sector.setHospital(null);
                    DaoManager.save(sector);
                }
                DaoManager.remove(Hospital.class, this.getEntityDeleteId());
            } catch (Exception e) {
                if (tr != null) {
                    tr.rollback();
                }
                if (e instanceof ConstraintViolationException) {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper.getValidation("deleteFail"), "");
                } else {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            e.getMessage(), e.getCause().getMessage());
                }

                LogHelper.log(log, e);
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    try {
                        tr.commit();
                    } catch (Exception e) {
                        if (e instanceof ConstraintViolationException) {
                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_ERROR,
                                            ResourcesHelper
                                                    .getValidation("deleteFail"),
                                            "");
                        } else {
                            MessageHelper.addGlobalMessage(
                                    FacesMessage.SEVERITY_ERROR,
                                    e.getMessage(), e.getCause().getMessage());
                        }

                        tr.rollback();

                        LogHelper.log(log, e);
                    }
                    if (tr != null && tr.getStatus() != TransactionStatus.ROLLED_BACK) {
                        afterEntityRemoved();
                    }
                }
            }
            this.onLoad();
        }
    }

}
