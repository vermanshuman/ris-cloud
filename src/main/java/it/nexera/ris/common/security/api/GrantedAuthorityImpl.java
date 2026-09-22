package it.nexera.ris.common.security.api;

import org.springframework.security.core.GrantedAuthority;

/**
 * This class is necessary for Spring framework (security subsystem)
 */
public class GrantedAuthorityImpl implements GrantedAuthority {
    private static final long serialVersionUID = 8936638757416839316L;

    String _authority = null;

    public GrantedAuthorityImpl(String authority) {
        this._authority = authority;
    }

    public String getAuthority() {
        return this._authority;
    }

    public int compareTo(Object o) {
        if (o == null || o.getClass() != GrantedAuthorityImpl.class
                || this._authority == null) {
            return 1;
        }
        if (this._authority.equals(((GrantedAuthorityImpl) o).getAuthority()) == true) {
            return 0;
        }
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if ((obj instanceof GrantedAuthorityImpl) == false) {
            return false;
        }
        if (this._authority == null) {
            return false;
        }
        return this._authority.equals(((GrantedAuthorityImpl) obj)._authority);
    }

    @Override
    public int hashCode() {
        if (this._authority == null) {
            return 0;
        }
        int hash = 0;
        for (int idx = 0; idx < this._authority.length(); idx++) {
            hash = 31 * hash + this._authority.charAt(idx);
        }
        return hash;
    }
}
