package it.nexera.ris.web.beans;

import it.nexera.ris.common.helpers.*;
import org.primefaces.component.tabview.TabView;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlForm;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BaseValidationPageBean extends PageBean {
    private HashMap<String, Integer> tabs;

    public void addFieldExeption(String id, String message) {
        id = fixComponentId(id);
        this.addFieldExeption(this.getComponentById(id), message, true);
    }

    public void addFieldExeption(String id, String message, Boolean showMessage) {
        id = fixComponentId(id);
        this.addFieldExeption(this.getComponentById(id), message, showMessage);
    }

    public void addException(String message) {
        this.setValidationFailed(true);
        this.getExceptions().add(ResourcesHelper.getValidation(message));
    }

    public void markInvalid(String id, String message) {
        id = fixComponentId(id);
        UIComponent component = this.getComponentById(id);
        markInvalid(component, message);
    }

    public void markInvalid(UIComponent component, String message) {
        this.getMarkedIvalidFields().add(completeId(component));
        this.setValidationFailed(true);
        ValidatorHelper.markNotValid(component,
                ResourcesHelper.getValidation(message), this.getContext(),
                this.getTabs());
    }

    public void addFieldExeption(UIComponent component, String message,
                                 Boolean showMessage) {
        this.getMarkedIvalidFields().add(completeId(component));
        this.setValidationFailed(true);
        ValidatorHelper.markNotValid(component,
                ResourcesHelper.getValidation(message), this.getContext(),
                this.getTabs());
        if (Boolean.TRUE.equals(showMessage)) {
            this.getExceptions().add(ResourcesHelper.getValidation(message));
        }
    }

    public void addFieldExeptionWithParametr(String id, String message,
                                             Long parametr) {
        id = fixComponentId(id);
        UIComponent component = this.getComponentById(id);
        this.getMarkedIvalidFields().add(completeId(component));
        this.setValidationFailed(true);
        ValidatorHelper.markNotValid(component, String.format("%s %s!",
                ResourcesHelper.getValidation(message), parametr.toString()), this
                .getContext(), this.getTabs());
        this.getExceptions().add(String.format("%s %s!",
                ResourcesHelper.getValidation(message), parametr.toString()));
    }

    public void addRequiredFieldExeption(String id) {
        id = fixComponentId(id);
        this.addRequiredFieldExeption(this.getComponentById(id));
    }

    public void addRequiredFieldExeption(String id, String labelId) {
        id = fixComponentId(id);
        this.addRequiredFieldExeption(this.getComponentById(id));
        this.getContext().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(
                        "%s %s", ResourcesHelper.getValidation(labelId),
                        " is required field!"), null));
    }

    public String fixComponentId(String id) {
        if (!id.startsWith("form:")) {
            id = "form:" + id;
        }
        return id;
    }

    private String completeId(UIComponent component) {
        StringBuilder sb = new StringBuilder();
        UIComponent parent = component.getParent();
        while (parent != null) {
            if (parent.getClass().equals(TabView.class)
                    || parent.getClass().equals(HtmlForm.class)
                    || UIComponent.isCompositeComponent(parent)) {
                sb.insert(0, String.format("%s:", parent.getId()));
            }

            parent = parent.getParent();
        }
        sb.append(component.getId());
        return sb.toString();
    }

    public void addRequiredFieldExeption(UIComponent component) {
        try {
            Method method = null;

            try {
                method = component.getClass().getDeclaredMethod("getLabel",
                        new Class[0]);
            } catch (Exception e) {
            }

            if (method == null) {
                try {
                    method = component.getClass().getMethod("getLabel",
                            new Class[0]);
                } catch (Exception e) {
                }
            }

            if (method != null) {
                String label = (String) method.invoke(component, new Object[0]);
                if (!ValidationHelper.isNullOrEmpty(label)) {
                    this.getExceptions()
                            .add(String.format("%s %s", label,
                                    "is required field!"));
                    this.markInvalid(component, "requiredField");
                    return;
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        this.addFieldExeption(component, "requiredField");
    }

    public void cleanFieldExeption(String id) {
        ValidatorHelper.markValid(this.getComponentById(id), this.getContext(),
                this.getTabs());
    }

    public boolean getValidationFailed() {
        return this.getViewState().get("validateFail") == null ? false
                : (Boolean) this.getViewState().get("validateFail");
    }

    public void setValidationFailed(boolean value) {
        if ((this.getViewState().get("validateFail") == null || this
                .getViewState().get("validateFail") == Boolean.FALSE)
                && value == true) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("validationFailed"),
                    ResourcesHelper.getValidation("checkData"));
        }

        this.getViewState().put("validateFail", value);
    }

    public void setMarkedIvalidFields(List<String> list) {
        this.getViewState().put("notValidFields", list);
    }

    @SuppressWarnings("unchecked")
    public List<String> getMarkedIvalidFields() {
        if (this.getViewState().get("notValidFields") == null) {
            this.getViewState().put("notValidFields", new ArrayList<String>());
        }

        return (List<String>) this.getViewState().get("notValidFields");
    }

    public void cleanValidation() {
        this.setTabs(null);
        for (String id : getMarkedIvalidFields()) {
            try {
                ValidatorHelper.markValid(getComponentById(fixComponentId(id)),
                        this.getContext(), this.getTabs());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        this.setValidationFailed(false);
        this.setMarkedIvalidFields(new ArrayList<String>());
        this.setExceptions(new ArrayList<String>());
    }

    public void setTabs(HashMap<String, Integer> tabs) {
        this.tabs = tabs;
    }

    public HashMap<String, Integer> getTabs() {
        if (tabs == null) {
            tabs = new HashMap<String, Integer>();
        }
        return tabs;
    }

    @SuppressWarnings("unchecked")
    public List<String> getExceptions() {
        if (this.getViewState().get("exceptions") == null) {
            this.getViewState().put("exceptions", new ArrayList<String>());
        }
        return (List<String>) this.getViewState().get("exceptions");
    }

    public void setExceptions(List<String> exceptions) {
        this.getViewState().put("exceptions", exceptions);
    }

    public UIComponent getComponentById(String id) {
        UIComponent component = this.getContext().getViewRoot()
                .findComponent(id);

        return component;
    }
}
