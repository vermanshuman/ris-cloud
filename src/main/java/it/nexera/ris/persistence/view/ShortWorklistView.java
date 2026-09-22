package it.nexera.ris.persistence.view;

import it.nexera.ris.common.annotations.View;

import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "SHORT_WORKLIST_VIEW")
@View(sql = ShortWorklistView.QUERY)
public class ShortWorklistView extends BaseWorklistView {
    private static final long serialVersionUID = -7062454812574110290L;

    public static final String CREATE_PART = "CREATE OR REPLACE VIEW SHORT_WORKLIST_VIEW ";

    protected static final String QUERY = CREATE_PART

            + SELECT_PART

            + FROM_SHORT_PART;

}
