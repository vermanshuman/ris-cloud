package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.RequestNote;

import java.util.List;

public class WaitingListRegistrationListBeanWrapper {

    private RadiologyExamRequest selectedRequest;

    private List<RadiologyExamRequestItem> radExamRequestItemsForCancel;

    private List<RadiologyExamRequestItem> radExamRequestItemsForAnnula;

    private RequestNote note;

    public RadiologyExamRequest getSelectedRequest() {
        return selectedRequest;
    }

    public void setSelectedRequest(RadiologyExamRequest selectedRequest) {
        this.selectedRequest = selectedRequest;
    }

    public List<RadiologyExamRequestItem> getRadExamRequestItemsForCancel() {
        return radExamRequestItemsForCancel;
    }

    public void setRadExamRequestItemsForCancel(List<RadiologyExamRequestItem> radExamRequestItemsForCancel) {
        this.radExamRequestItemsForCancel = radExamRequestItemsForCancel;
    }

    public List<RadiologyExamRequestItem> getRadExamRequestItemsForAnnula() {
        return radExamRequestItemsForAnnula;
    }

    public void setRadExamRequestItemsForAnnula(List<RadiologyExamRequestItem> radExamRequestItemsForAnnula) {
        this.radExamRequestItemsForAnnula = radExamRequestItemsForAnnula;
    }

    public RequestNote getNote() {
        return note;
    }

    public void setNote(RequestNote note) {
        this.note = note;
    }
}
