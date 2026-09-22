package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "sub_menu_item")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "SUB_MENU_SEQ", allocationSize = 1)
public class SubMenuItem extends IndexedEntity {
    private static final long serialVersionUID = -8865173086910457231L;

    @Column
    private String image;

    @Column(name = "localizeKey")
    private String localizeKey;

    @Column(name = "page_code")
    private String pageCode;

    @Column(name = "page_code_1")
    private String pageCode1;

    @Column(name = "page_code_2")
    private String pageCode2;

    @ManyToOne
    @JoinColumn(name = "parent_menu_item_id", foreignKey = @ForeignKey(name = "FK_SUB_MENU_IT_MENU_IT_PARENT"))
    private MenuItem parent;

    @Column(name = "rendered_id")
    private String renderedId;

    @Column(name = "sub_group")
    private Long subGroup;

    @Column(name = "order_in_sub_group")
    private Long orderInSubGroup;

    @Column
    private Integer seq;

    public String getImage() {
        return image;
    }

    public String getLocalizeKey() {
        return localizeKey;
    }

    public String getPageCode() {
        return pageCode;
    }

    public String getPageCode1() {
        return pageCode1;
    }

    public MenuItem getParent() {
        return parent;
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

    public void setPageCode1(String pageCode1) {
        this.pageCode1 = pageCode1;
    }

    public void setParent(MenuItem parent) {
        this.parent = parent;
    }

    public void setRenderedId(String renderedId) {
        this.renderedId = renderedId;
    }

    public Long getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(Long subGroup) {
        this.subGroup = subGroup;
    }

    public String getPageCode2() {
        return pageCode2;
    }

    public void setPageCode2(String pageCode2) {
        this.pageCode2 = pageCode2;
    }

    public Long getOrderInSubGroup() {
        return orderInSubGroup;
    }

    public void setOrderInSubGroup(Long orderInSubGroup) {
        this.orderInSubGroup = orderInSubGroup;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }
}
