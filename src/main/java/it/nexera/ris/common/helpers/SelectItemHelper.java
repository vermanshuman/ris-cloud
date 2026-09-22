package it.nexera.ris.common.helpers;

import javax.faces.model.SelectItem;

/**
 * SelectItemHelper
 * Used to fill drop down lists
 */
public class SelectItemHelper {
    public static SelectItem getNotSelected() {
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(ResourcesHelper.getString("notSelected"));
        sb.append(" -");
        return new SelectItem("", sb.toString());
    }

    public static SelectItem getVirtualEntity() {
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(ResourcesHelper.getString("notSelected"));
        sb.append(" -");

        return new SelectItem("-1", sb.toString());
    }

    public static SelectItem getAllElement() {
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(ResourcesHelper.getString("all"));
        sb.append(" -");
        return new SelectItem("", sb.toString());
    }

    public static SelectItem getNoneElement() {
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(ResourcesHelper.getString("none"));
        sb.append(" -");
        return new SelectItem("", sb.toString());
    }

    public static SelectItem getUnlimitedElement() {
        StringBuilder sb = new StringBuilder();
        sb.append(ResourcesHelper.getString("unlimited"));
        return new SelectItem("-1", sb.toString());
    }

    public static SelectItem getFreeElement() {
        StringBuilder sb = new StringBuilder();
        sb.append(ResourcesHelper.getString("free"));
        return new SelectItem("-1", sb.toString());
    }
}
