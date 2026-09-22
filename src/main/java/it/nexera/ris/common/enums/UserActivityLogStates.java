package it.nexera.ris.common.enums;

public enum UserActivityLogStates {
    // WORKLIST ACTION TO TRACK

    ACCETTA,
    ANNULLA_ACCETTA,
    ESEGUI,
    ANNULLA_ESEGUI,
    REFERTA,
    CANCELLA,
    FIRMA,

    //HTML EDITOR ACTION TO TRACK

    SALVA,
    CHIUDI,
    RILASCIA,
    RIAPRI_REFERTO;

    public String toString() {
        return this.name();
    }

}
