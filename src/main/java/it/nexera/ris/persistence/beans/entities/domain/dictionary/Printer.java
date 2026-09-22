package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "dic_printer")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PRINTER_SEQ", allocationSize = 1)
public class Printer extends Dictionary {
    private static final long serialVersionUID = -1987298486933552486L;

    @Column
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
