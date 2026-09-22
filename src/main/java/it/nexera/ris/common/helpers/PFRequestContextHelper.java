package it.nexera.ris.common.helpers;


import org.primefaces.PrimeFaces;

public class PFRequestContextHelper {
    public static void executeJS(String str) {
        PrimeFaces context = PrimeFaces.current();
        if (context != null) {
            context.executeScript(str);
        }
    }
}
