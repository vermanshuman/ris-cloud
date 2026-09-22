package it.nexera.ris.web.beans.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.DocumentTemplate;

import java.io.Serializable;

public class DocumentTemplateWrapper implements Serializable {

    private static final long serialVersionUID = -1477485911085572915L;

    private Boolean header;

    private Boolean footer;

    private String headerContent;

    private String bodyContent;

    private String footerContent;

    public DocumentTemplateWrapper() {
        super();
    }

    public DocumentTemplateWrapper(DocumentTemplate template) {
        super();
        this.header = template.getHeader();
        this.footer = template.getFooter();
        this.headerContent = template.getHeaderContent();
        this.bodyContent = template.getBodyContent();
        this.footerContent = template.getFooterContent();
    }

    /**
     * @param header
     * @param footer
     * @param headerContent
     * @param bodyContent
     * @param footerContent
     */
    public DocumentTemplateWrapper(Boolean header, Boolean footer,
                                   String headerContent, String bodyContent, String footerContent) {
        super();
        this.header = header;
        this.footer = footer;
        this.headerContent = headerContent;
        this.bodyContent = bodyContent;
        this.footerContent = footerContent;
    }

    public Boolean getHeader() {
        return header;
    }

    public void setHeader(Boolean header) {
        this.header = header;
    }

    public Boolean getFooter() {
        return footer;
    }

    public void setFooter(Boolean footer) {
        this.footer = footer;
    }

    public String getHeaderContent() {
        return headerContent;
    }

    public void setHeaderContent(String headerContent) {
        this.headerContent = headerContent;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public String getFooterContent() {
        return footerContent;
    }

    public void setFooterContent(String footerContent) {
        this.footerContent = footerContent;
    }

}
