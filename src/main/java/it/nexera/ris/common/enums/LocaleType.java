package it.nexera.ris.common.enums;

public enum LocaleType {
    IT("it"), EN("en");

    private String value;

    private LocaleType(String value) {
        this.setValue(value);
    }

    public static LocaleType fromString(String str) {
        for (LocaleType item : LocaleType.values()) {
            if (item.getValue().equalsIgnoreCase(str)) {
                return item;
            }
        }

        return LocaleType.IT;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
