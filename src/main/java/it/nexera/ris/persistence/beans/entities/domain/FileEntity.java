package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "file_entity")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "FILE_ENTITY_SEQ", allocationSize = 1)
public class FileEntity extends IndexedEntity {
    private static final long serialVersionUID = 4757606192487491676L;

    @Column
    private String name;

    @Column(name = "content")
    @Lob
    private byte[] content;

    @Column(name = "filePath")
    private String path;

    @Column(name = "version_of_save", columnDefinition = "NUMBER(19,0) default 0")
    private Long versionOfSave;

    @Transient
    private Boolean incrementVersionOnSave;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getVersionOfSave() {
        return versionOfSave;
    }

    public void setVersionOfSave(Long versionOfSave) {
        this.versionOfSave = versionOfSave;
    }

    public Boolean getIncrementVersionOnSave() {
        return incrementVersionOnSave;
    }

    public void setIncrementVersionOnSave(Boolean incrementVersionOnSave) {
        this.incrementVersionOnSave = incrementVersionOnSave;
    }
}
