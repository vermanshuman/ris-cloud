package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;

import java.io.Serializable;
import java.util.Date;

public class DocumentGenerationHistoryWrapper implements Serializable {
    private static final long serialVersionUID = -6976682501695434171L;

    private static final int maxDescriptionTitleLength = 3;

    private Long id;

    private FileEntity file;

    private String doctor;

    private String description;

    private String text;

    private Date date;

    private String examTypeDescription;

    private String sectorDescription;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FileEntity getFile() {
        return file;
    }

    public void setFile(FileEntity file) {
        this.file = file;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        StringBuilder str = null;
        if (getExamTypeDescription().length() <= maxDescriptionTitleLength) {
            str = new StringBuilder(getExamTypeDescription());
            str.append(" - ");
        } else {
            String tempDescription = getExamTypeDescription().substring(0,
                    maxDescriptionTitleLength);
            str = new StringBuilder(tempDescription);
            str.append(" - ");
        }
        str.append(DateTimeHelper.ToStringWithSeconds(getDate()));
        return str.toString();
    }

    public String getExamTypeDescription() {
        return examTypeDescription;
    }

    public void setExamTypeDescription(String examTypeDescription) {
        this.examTypeDescription = examTypeDescription;
    }

    public String getSectorDescription() {
        return sectorDescription;
    }

    public void setSectorDescription(String sectorDescription) {
        this.sectorDescription = sectorDescription;
    }


}
