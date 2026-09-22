package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "request_tag")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "REQUEST_TAG_SEQ", allocationSize = 1)
public class RequestTag extends IndexedEntity {

    @ManyToOne
    @JoinColumn(name = "radiology_exam_request_id")
    private RadiologyExamRequest radiologyExamRequest;

    @Column(name = "tag")
    private String tag;

    public RadiologyExamRequest getRadiologyExamRequest() {
        return radiologyExamRequest;
    }

    public void setRadiologyExamRequest(RadiologyExamRequest radiologyExamRequest) {
        this.radiologyExamRequest = radiologyExamRequest;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
