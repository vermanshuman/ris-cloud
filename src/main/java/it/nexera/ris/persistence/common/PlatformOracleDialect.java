package it.nexera.ris.persistence.common;

import org.hibernate.dialect.Oracle12cDialect;

public class PlatformOracleDialect extends Oracle12cDialect {
    /* (non-Javadoc)
     * @see org.hibernate.dialect.Oracle8iDialect#getCreateSequenceString(java.lang.String)
     */
    @Override
    public String getCreateSequenceString(String sequenceName) {
        return super.getCreateSequenceString(sequenceName) + " nocache";
    }
}
