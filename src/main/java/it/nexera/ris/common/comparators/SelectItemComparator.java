package it.nexera.ris.common.comparators;

import javax.faces.model.SelectItem;
import java.util.Comparator;

public class SelectItemComparator implements Comparator<SelectItem> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(SelectItem o1, SelectItem o2) {
        return o1.getLabel().compareTo(o2.getLabel());
    }

}
