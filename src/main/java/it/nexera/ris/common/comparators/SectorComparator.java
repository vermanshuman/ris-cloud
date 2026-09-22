package it.nexera.ris.common.comparators;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;

import java.util.Comparator;

public class SectorComparator implements Comparator<Sector> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Sector o1, Sector o2) {
        return o1.getDescription().compareToIgnoreCase(o2.getDescription());
    }

}
