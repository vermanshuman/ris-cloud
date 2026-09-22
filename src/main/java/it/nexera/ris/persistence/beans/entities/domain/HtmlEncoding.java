package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "html_encoding")
@Setter
@Getter
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "HTML_ENCODING_SEQ", allocationSize = 1)
public class HtmlEncoding extends IndexedEntity {
    private static final long serialVersionUID = 4187040521575819240L;

    public transient final Logger log = LogManager.getLogger(HtmlEncoding.class);

    @Column(name = "html_entity")
    private String htmlEntity;

    @Column(name = "html_replacement")
    private String htmlReplacement;
}
