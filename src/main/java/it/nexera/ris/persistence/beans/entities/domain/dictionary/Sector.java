package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.domain.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "dic_sector")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "SECTOR_SEQ", allocationSize = 1)
@Getter
@Setter
public class Sector extends Dictionary {
    private static final long serialVersionUID = 7453534527979903015L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", foreignKey = @ForeignKey(name = "FK_SECTOR_HOSPITAL"))
    private Hospital hospital;

    @Column(name = "istat")
    private String istat;

    @Column(name = "cdr")
    private String cdr;

    @Column(name = "acc_num_prefix")
    private String accNumPrefix;

    @OneToMany(mappedBy = "sector", fetch = FetchType.LAZY)
    private List<Diagnostic> diagnostic;

    @ManyToMany(mappedBy = "sectors")
    private List<User> users;

    @Column(name = "dicom_host")
    private String sectorDICOMHost;

    @Column(name = "dicom_port")
    private String sectorDICOMPort;

    @Column(name = "dicom_aetitle")
    private String sectorDICOMAETitle;

    @Column(name = "weasis_url")
    private String weasisUrl;

    @Column(name = "code_affinity_domain ")
    private String codeAffinityDomain ;

    @Column(name = "app_inviante_unidoc")
    private String sendingApp ;

    @Column(name = "codice_tipologia_documento")
    private String documentTypeCode ;

    @Column(name = "document_format_code")
    private String documentFormatCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sector sector = (Sector) o;
        return Objects.equals(getId(), sector.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
