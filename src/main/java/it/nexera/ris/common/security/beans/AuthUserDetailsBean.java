package it.nexera.ris.common.security.beans;

import it.nexera.ris.common.enums.UserStatuses;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.UserNotFoundException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.security.api.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuthUserDetailsBean implements UserDetailsService {
    public transient final Logger log = LogManager.getLogger(getClass());

    HashMap<String, UserDetails> _users = new HashMap<String, UserDetails>();

    SecurityInfoFactMethod _securityController = null;

    SecurityInfoProc _securityInfoProc = null;

    public AuthUserDetailsBean() {
    }

    public void setSecurityController(SecurityInfoFactMethod securityController) {
        log.info("setSecurityController");
        this._securityController = securityController;
    }

    public void setSecurityInfoProc(SecurityInfoProc securityInfoProc) {
        log.info("setSecurityController");
        this._securityInfoProc = securityInfoProc;
    }

    public String getRequestParameter(String name) {
        return this.getRequest().getParameter(name);
    }

    public HttpServletRequest getRequest() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return (HttpServletRequest) ctx.getExternalContext().getRequest();
    }

    public UserDetails loadUserByUsername(String userName)
            throws UsernameNotFoundException, DataAccessException {
        SecurityInfo secInfo = null;
        try {
            secInfo = this._securityController.getSecurityInfoByLogin(userName);
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("User : " + userName
                    + " isn't found as UserNotFoundException has been generated...", e);
        } catch (IOException e) {
            throw new UsernameNotFoundException("User : " + userName
                    + " isn't found as IOException has been generated...", e);
        } catch (PersistenceBeanException e) {
            throw new UsernameNotFoundException(
                    "User : "
                            + userName
                            + " isn't found as UsernameNotFoundException has been generated...",
                    e);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        List<String> grantsList = this._securityInfoProc
                .procSecurityInfo(secInfo);
        Set<GrantedAuthority> authoritiesSet = this
                .getGrantedAuthoritiesFromList(grantsList);

        return new UserDetailsImpl(userName,
                (secInfo == null) ? "" : secInfo.get_password(),
                secInfo != null && !UserStatuses.INACTIVE.equals(secInfo.getStatus()),
                authoritiesSet);
    }

    Set<GrantedAuthority> getGrantedAuthoritiesFromList(List<String> grantsList) {
        Set<GrantedAuthority> authoritiesSet = new HashSet<GrantedAuthority>();

        GrantedAuthority grAuth = null;
        for (String grant : grantsList) {
            grAuth = new GrantedAuthorityImpl(grant);
            authoritiesSet.add(grAuth);
        }
        return authoritiesSet;
    }

}
