package it.nexera.ris.common.security.beans;

import it.nexera.ris.common.security.api.SecurityInfo;
import it.nexera.ris.common.security.api.SecurityInfoProc;

import java.util.ArrayList;
import java.util.List;

public class SecurityInfoProcBean implements SecurityInfoProc {

    public SecurityInfoProcBean() {
    }

    public List<String> procSecurityInfo(SecurityInfo si) {
        List<String> resList = new ArrayList<String>();

        resList.add("ROLE_USER");

        return resList;
    }

}
