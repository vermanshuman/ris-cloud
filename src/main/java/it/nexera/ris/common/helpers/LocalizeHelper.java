package it.nexera.ris.common.helpers;

import it.nexera.ris.persistence.LocalizeManager;

public class LocalizeHelper {
    public static String getString(String str1, String str2) {
        switch (LocalizeManager.getInstance().getLocalizeBean().getLocaleType()) {
            case EN: {
                return str1;
            }
            case IT: {
                return str2;
            }
        }

        return "";
    }
}
