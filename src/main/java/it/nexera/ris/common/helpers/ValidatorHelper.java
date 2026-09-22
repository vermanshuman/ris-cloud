package it.nexera.ris.common.helpers;

import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.picklist.PickList;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.HashMap;

/**
 * ValidatorHelper class
 * Used for validation highlighting
 */
public class ValidatorHelper {
    public static String errorStyle = "background-color: #FFE5E5;";

    public static void markNotValid(UIComponent component, String tmpMessage,
                                    FacesContext context, HashMap<String, Integer> tabs) {

        setStyleClass(component, "not_valid");
        putTitle(component, tmpMessage);

        while (component.getParent() != null) {
            component = component.getParent();
            if (component instanceof Tab) {
                Tab tab = (Tab) component;
                tab.setTitleStyle("border: 1px solid red;");
                
                
/* All commented lines were initiated by 0029151 (remove all tooltips)             
 *   tab.setTitletip(ResourcesHelper.getValidation("checkData"));
*/
                tabs.put(component.getClientId(context), 1);
                if (component.getParent() instanceof TabView) {
                    TabView tabView = (TabView) component.getParent();
                    int index = component.getParent().getChildren()
                            .indexOf(tab);
                    if (tabs.size() == 1) {
                        tabView.setActiveIndex(index);
                    } else {
                        tabView.setActiveIndex(tabView.getActiveIndex() > index ? index
                                : tabView.getActiveIndex());
                    }
                }
            }
        }
    }

    private static void putTitle(UIComponent component, String message) {
        if (component instanceof SelectOneMenu || component instanceof PickList || component instanceof Calendar) {
            setStyleClass(component, message.replaceAll(" ", "_"));
        } else {
/*            component.getAttributes().put("title", message);
*/
        }
    }

    private static void setStyleClass(UIComponent component, String styleClass) {
        if (component.getAttributes().get("styleClass") == null) {
            component.getAttributes().put("styleClass", styleClass);
        } else {
            component.getAttributes().put(
                    "styleClass",
                    String.format("%s %s",
                            component.getAttributes().get("styleClass")
                                    .toString().replaceAll(styleClass, "")
                                    .trim(), styleClass));
        }
    }

    public static void markValid(UIComponent component, FacesContext context,
                                 HashMap<String, Integer> tabs) {

        if (component.getAttributes().get("styleClass") != null) {
            component.getAttributes().put(
                    "styleClass",
                    component.getAttributes().get("styleClass").toString()
                            .replaceAll("not_valid", "").trim());
        }
        /*component.getAttributes().put("title", "");*/

        while (component.getParent() != null) {
            component = component.getParent();
            if (component instanceof Tab) {
                if (tabs.get(component.getClientId(context)) == null) {
                    Tab tab = (Tab) component;
                    tab.setTitleStyle("");
/*                    tab.setTitletip("");
*/
                }
                break;
            }
        }
    }
}
