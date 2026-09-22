package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "request_attached_file")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "REQUEST_ATTACH_SEQ", allocationSize = 1)
public class RequestAttachedFile extends IndexedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "radiology_exam_request_id")
    private RadiologyExamRequest radiologyExamRequest;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "rotate_degree")
    private Long rotateDegree;

    @Transient
    private Long tempId;

    @Transient
    private Boolean isImage;

    public RadiologyExamRequest getRadiologyExamRequest() {
        return radiologyExamRequest;
    }

    public void setRadiologyExamRequest(RadiologyExamRequest radiologyExamRequest) {
        this.radiologyExamRequest = radiologyExamRequest;
    }

    public Boolean getIsImage() {
        if (filePath.endsWith(".pdf")) {
            return false;
        } else {
            return true;
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getRotateDegree() {
        return rotateDegree;
    }

    public void setRotateDegree(Long rotateDegree) {
        this.rotateDegree = rotateDegree;
    }

    public Long getTempId() {
        return tempId;
    }

    public void setTempId(Long tempId) {
        this.tempId = tempId;
    }
}
