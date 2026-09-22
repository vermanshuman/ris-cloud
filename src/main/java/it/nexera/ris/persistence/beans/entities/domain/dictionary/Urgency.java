package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.*;
import java.util.Arrays;

@Entity
@Table(name = "dic_urgency")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "URGENCY_SEQ", allocationSize = 1)
public class Urgency extends Dictionary {

    private static final long serialVersionUID = -3602727206110880877L;

    @Column(name = "priority_order")
    private Long priority;

    @Column(name = "icon_name")
    private String iconName;

    @Column(name = "icon_content")
    @Lob
    private byte[] iconContent;

    @Column(name = "filter_code")
    private String filterCode;

    @Column(name = "icon_hashcode")
    private Integer iconHashCode;

    @Column(name = "show_alert")
    private Boolean showAlert;

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getFilterCode() {
        return filterCode;
    }

    public void setFilterCode(String filterCode) {
        this.filterCode = filterCode;
    }

    public byte[] getIconContent() {
        return iconContent;
    }

    public void setIconContent(byte[] iconContent) {
        this.iconContent = iconContent;
    }

    public Integer getIconHashCode() {
        return iconHashCode;
    }

    public void setIconHashCode(Integer iconHashCode) {
        this.iconHashCode = iconHashCode;
    }

    public Boolean getShowAlert() {
        return showAlert == null ? false : showAlert;
    }

    public void setShowAlert(Boolean showAlert) {
        this.showAlert = showAlert;
    }

    public String getIconUrlTemp() {
        if (!ValidationHelper.isNullOrEmpty(this.getIconContent())
                || !ValidationHelper.isNullOrEmpty(this.getIconName())) {
            try {
                if (!FileHelper.existInLocalTemp(this.getIconName())
                        || (this.getIconHashCode() != null && this
                        .getIconHashCode().intValue() != calculateIconHashCode(
                        iconName, iconContent))) {
                    FileHelper.writeFileToLocalTemp(this.getIconName(),
                            this.getIconContent());
                }
                return String.format("%s%s", "/Temp/", this.getIconName());
            } catch (Exception e) {
            }
        }
        return null;
    }

    @Transient
    public int calculateIconHashCode(String iconName, byte[] iconContent) {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(iconContent);
        result = prime * result
                + ((iconName == null) ? 0 : iconName.hashCode());
        return result;
    }

}
