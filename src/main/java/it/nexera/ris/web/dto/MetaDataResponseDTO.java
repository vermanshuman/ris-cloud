package it.nexera.ris.web.dto;

import java.util.List;

public class MetaDataResponseDTO extends ResponseDto {

    private List<Metadata> metadata;

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Metadata> metadata) {
        this.metadata = metadata;
    }
}
