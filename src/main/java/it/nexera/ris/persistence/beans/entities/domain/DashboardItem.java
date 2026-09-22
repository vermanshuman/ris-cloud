package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "dashboard_item")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "DASHBOARD_ITEM_SEQ", allocationSize = 1)
public class DashboardItem extends IndexedEntity {
    private static final long serialVersionUID = 7389822344576657936L;

    @Column
    private String description;

    @Column(name = "href_edit")
    private String hrefEdit;

    @Column(name = "href_list")
    private String hrefList;

    @Column(name = "image_path")
    private String imagePath;

    @Column
    private String name;

    @Column
    private String title;

    public String getDescription() {
        return description;
    }

    public String getHrefEdit() {
        return hrefEdit;
    }

    public String getHrefList() {
        return hrefList;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHrefEdit(String hrefEdit) {
        this.hrefEdit = hrefEdit;
    }

    public void setHrefList(String hrefList) {
        this.hrefList = hrefList;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
