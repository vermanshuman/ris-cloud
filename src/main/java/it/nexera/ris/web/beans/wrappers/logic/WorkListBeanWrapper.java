package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RequestNote;
import it.nexera.ris.persistence.beans.entities.domain.UserPreference;
import it.nexera.ris.persistence.view.BaseWorklistView;
import it.nexera.ris.persistence.view.WorklistView;

import java.util.List;

public class WorkListBeanWrapper {

    private RadiologyExamRequest selectedRequest;

    private BaseWorklistView selectedViewToCreatePdf;

    private RadiologyExamRequest splitedRequest;

    private List<UserPreference> wlStatesUserPreferences;

    private List<UserPreference> exTypesUserPreferences;

    private RequestNote note;

    private UserPreference labelPrintActivePreference;

    private UserPreference labelPrintPrinterNamePreference;

    private UserPreference labelPrintCountPreference;

    private UserPreference labelPrintActivePreferenceDocg;

    private UserPreference labelPrintPrinterNamePreferenceDocg;

    private UserPreference labelPrintCountPreferenceDocg;

    private UserPreference openPdfDocPref;

    public RequestNote getNote() {
        if (note == null) {
            note = new RequestNote();
        }

        return note;
    }

    public void setNote(RequestNote note) {
        this.note = note;
    }


    public RadiologyExamRequest getSelectedRequest() {
        return selectedRequest;
    }

    public void setSelectedRequest(RadiologyExamRequest selectedRequest)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        this.selectedRequest = selectedRequest;

        if (getSelectedRequest() != null) {
            setSelectedViewToCreatePdf(DaoManager.get(WorklistView.class,
                    selectedRequest.getId()));
        }
    }

    public BaseWorklistView getSelectedViewToCreatePdf() {
        return selectedViewToCreatePdf;
    }

    public void setSelectedViewToCreatePdf(WorklistView selectedViewToCreatePdf) {
        this.selectedViewToCreatePdf = selectedViewToCreatePdf;
    }

    public RadiologyExamRequest getSplitedRequest() {
        return splitedRequest;
    }

    public void setSplitedRequest(RadiologyExamRequest splitedRequest) {
        this.splitedRequest = splitedRequest;
    }

    public List<UserPreference> getWlStatesUserPreferences() {
        return wlStatesUserPreferences;
    }

    public void setWlStatesUserPreferences(List<UserPreference> wlStatesUserPreferences) {
        this.wlStatesUserPreferences = wlStatesUserPreferences;
    }

    public List<UserPreference> getExTypesUserPreferences() {
        return exTypesUserPreferences;
    }

    public void setExTypesUserPreferences(List<UserPreference> exTypesUserPreferences) {
        this.exTypesUserPreferences = exTypesUserPreferences;
    }

    public UserPreference getLabelPrintActivePreference() {
        return labelPrintActivePreference;
    }

    public void setLabelPrintActivePreference(UserPreference labelPrintActivePreference) {
        this.labelPrintActivePreference = labelPrintActivePreference;
    }

    public UserPreference getLabelPrintPrinterNamePreference() {
        return labelPrintPrinterNamePreference;
    }

    public void setLabelPrintPrinterNamePreference(UserPreference labelPrintPrinterNamePreference) {
        this.labelPrintPrinterNamePreference = labelPrintPrinterNamePreference;
    }

    public UserPreference getLabelPrintCountPreference() {
        return labelPrintCountPreference;
    }

    public void setLabelPrintCountPreference(UserPreference labelPrintCountPreference) {
        this.labelPrintCountPreference = labelPrintCountPreference;
    }

    public UserPreference getLabelPrintActivePreferenceDocg() {
        return labelPrintActivePreferenceDocg;
    }

    public void setLabelPrintActivePreferenceDocg(UserPreference labelPrintActivePreferenceDocg) {
        this.labelPrintActivePreferenceDocg = labelPrintActivePreferenceDocg;
    }

    public UserPreference getLabelPrintPrinterNamePreferenceDocg() {
        return labelPrintPrinterNamePreferenceDocg;
    }

    public void setLabelPrintPrinterNamePreferenceDocg(UserPreference labelPrintPrinterNamePreferenceDocg) {
        this.labelPrintPrinterNamePreferenceDocg = labelPrintPrinterNamePreferenceDocg;
    }

    public UserPreference getLabelPrintCountPreferenceDocg() {
        return labelPrintCountPreferenceDocg;
    }

    public void setLabelPrintCountPreferenceDocg(UserPreference labelPrintCountPreferenceDocg) {
        this.labelPrintCountPreferenceDocg = labelPrintCountPreferenceDocg;
    }

    public UserPreference getOpenPdfDocPref() {
        return openPdfDocPref;
    }

    public void setOpenPdfDocPref(UserPreference openPdfDocPref) {
        this.openPdfDocPref = openPdfDocPref;
    }
}
