package it.nexera.ris.persistence.beans.entities.domain.common;

import it.nexera.ris.common.enums.FSEValidationStatus;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "file_validation")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "FILE_VALIDATION_SEQ", allocationSize = 1)
public class FileValidation  extends IndexedEntity {

    private String path;

    @Lob
    private String responce;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private FSEValidationStatus status;

    @Column(name = "work_flow_instance_id")
    private String workflowInstanceId;

    @Column(name = "file_entity_id")
    private Long fileEntityId;

    @Column(name = "rad_exam_request_id")
    private Long radiologyExamRequestId;

    public FSEValidationStatus getStatus() {
        return status;
    }

    public void setStatus(FSEValidationStatus status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getResponce() {
        return responce;
    }

    public void setResponce(String responce) {
        this.responce = responce;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(String workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public Long getFileEntityId() {
        return fileEntityId;
    }

    public void setFileEntityId(Long fileEntityId) {
        this.fileEntityId = fileEntityId;
    }

    public Long getRadiologyExamRequestId() {
        return radiologyExamRequestId;
    }

    public void setRadiologyExamRequestId(Long radiologyExamRequestId) {
        this.radiologyExamRequestId = radiologyExamRequestId;
    }
}
