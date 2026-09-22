package it.nexera.ris.persistence;

public interface IAggregated {
    public void tryToCallAggregationDlg();

    public void aggregateAction();

    public void selectAllItemsForAggregation();

    public void unselectItemsForAggregation();
}
