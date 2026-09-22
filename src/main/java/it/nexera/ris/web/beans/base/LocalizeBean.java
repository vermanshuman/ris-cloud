package it.nexera.ris.web.beans.base;

import it.nexera.ris.common.enums.LocaleType;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import java.io.Serializable;
import java.util.Locale;

@Named("localizeBean")
@SessionScoped
public class LocalizeBean implements Serializable {
    private static final long serialVersionUID = -4670177084406774037L;

    private String locale;

    public LocalizeBean() {
    }
    @PostConstruct
    public void init() {
        setLocale(LocaleType.IT.getValue());
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public LocaleType getLocaleType() {
        return LocaleType.fromString(locale);
    }

    public Locale getCurrentLocale() {
        return new Locale(locale);
    }
}
