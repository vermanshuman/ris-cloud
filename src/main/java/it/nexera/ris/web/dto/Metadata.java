package it.nexera.ris.web.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Metadata {
    @SerializedName("IDDoc")
    private Long idDoc;
    @SerializedName("FileName")
    private String fileName;
    @SerializedName("MimeType")
    private String mimeType;
    @SerializedName("FileSize")
    private Long fileSize;
    @SerializedName("CreateDate")
    private String createDate;
    @SerializedName("RadiologyExamId")
    private Long radiologyExamId;
    @SerializedName("Patient")
    private String patient;
}
