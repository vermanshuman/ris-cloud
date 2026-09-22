package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.PageTypes;

public class LogoutHelper extends BaseHelper {

    public static void showLogoutMessage() {
        try {
            RedirectHelper.sendRedirect(PageTypes.LOGIN.getPagesContext() + "?showLogoutDlg=true");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }
}
