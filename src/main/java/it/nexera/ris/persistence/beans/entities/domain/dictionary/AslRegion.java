package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "dic_asl_region")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "ASL_REG_SEQ", allocationSize = 1)
public class AslRegion extends Dictionary {

    private static final long serialVersionUID = -2484987220461266833L;
}
