package it.nexera.ris.common.enums;

public enum PacsType implements DbEnum {
    LTA, CACHE, WORKLIST_DICOM;

    @Override
    public String getRealName() {
        return this.name();
    }

    @Override
    public Object getRealObject() {
        return this;
    }
}
