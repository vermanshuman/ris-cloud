package it.nexera.ris.persistence.view;

import it.nexera.ris.common.annotations.View;

import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "worklist_view")
@View(sql = WorklistView.QUERY)
public class WorklistView extends BaseWorklistView {
    private static final long serialVersionUID = 4444424016133918867L;

    public static final String CREATE_PART = "CREATE OR REPLACE VIEW worklist_view ";

    protected static final String QUERY = CREATE_PART

            + SELECT_PART

            + FROM_MAIN_PART;
}
