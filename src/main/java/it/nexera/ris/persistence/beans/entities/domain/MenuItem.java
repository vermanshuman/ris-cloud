package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "menu_item")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "MENU_ITEM_SEQ", allocationSize = 1)
public class MenuItem extends IndexedEntity {

    private static final long serialVersionUID = -3926217086036227735L;

    @Column
    private String image;

    @Column(name = "localizeKey")
    private String localizeKey;

    @Column(name = "page_code")
    private String pageCode;

    @ManyToOne
    @JoinColumn(name = "parent_menu_item_id", foreignKey = @ForeignKey(name = "FK_MENU_ITEM_MENU_ITEM_PARENT"))
    private MenuItem parent;

    @Column
    private Integer position;

    @Column(name = "rendered_id")
    private String renderedId;

    public String getImage() {
        return image;
    }

    public String getLocalizeKey() {
        return localizeKey;
    }

    public String getPageCode() {
        return pageCode;
    }

    public MenuItem getParent() {
        return parent;
    }

    public Integer getPosition() {
        return position;
    }

    public String getRenderedId() {
        return renderedId;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setLocalizeKey(String localizeKey) {
        this.localizeKey = localizeKey;
    }

    public void setPageCode(String pageCode) {
        this.pageCode = pageCode;
    }

    public void setParent(MenuItem parent) {
        this.parent = parent;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setRenderedId(String renderedId) {
        this.renderedId = renderedId;
    }

}
