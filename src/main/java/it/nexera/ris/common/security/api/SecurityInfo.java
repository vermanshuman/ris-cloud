package it.nexera.ris.common.security.api;

import it.nexera.ris.common.enums.UserStatuses;

/**
 * SecurityInfo class encapsulates user authentication details
 */
public class SecurityInfo {
    private Long _userID = 0L;

    private String _userName = null;

    private String _password = null;

    private UserStatuses status;

    public SecurityInfo() {
    }

    public SecurityInfo(Long userID, String userName, String password, UserStatuses status) {
        set_userID(userID);
        set_userName(userName);
        set_password(password);
        setStatus(status);
    }

    public Long get_userID() {
        return _userID;
    }

    public void set_userID(Long _userID) {
        this._userID = _userID;
    }

    public String get_userName() {
        return _userName;
    }

    public void set_userName(String _userName) {
        this._userName = _userName;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public UserStatuses getStatus() {
        return status;
    }

    public void setStatus(UserStatuses status) {
        this.status = status;
    }
}
