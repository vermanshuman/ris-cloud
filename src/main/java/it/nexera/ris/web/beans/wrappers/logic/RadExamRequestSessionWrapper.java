package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;
import java.util.List;

public class RadExamRequestSessionWrapper implements Serializable {
    private static final long serialVersionUID = -6879758953639272803L;

    private Long radExamRequestId;

    private List<Long> selectedRadExamRequestItemList;

    public RadExamRequestSessionWrapper(Long radExamRequestId,
                                        List<Long> selectedRadExamRequestItemList) {
        super();
        this.radExamRequestId = radExamRequestId;
        this.selectedRadExamRequestItemList = selectedRadExamRequestItemList;
    }

    public Long getRadExamRequestId() {
        return radExamRequestId;
    }

    public void setRadExamRequestId(Long radExamRequestId) {
        this.radExamRequestId = radExamRequestId;
    }

    public List<Long> getSelectedRadExamRequestItemList() {
        return selectedRadExamRequestItemList;
    }

    public void setSelectedRadExamRequestItemList(
            List<Long> selectedRadExamRequestItemList) {
        this.selectedRadExamRequestItemList = selectedRadExamRequestItemList;
    }

}
