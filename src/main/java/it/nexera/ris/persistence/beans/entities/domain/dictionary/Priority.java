package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "dic_priority")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PRIORITY_SEQ", allocationSize = 1)
public class Priority extends Dictionary {
    private static final long serialVersionUID = -1933711219525571263L;

    @Column
    private Integer days;

    @Column
    private String color;

    @Column(name = "has_color")
    private Boolean hasColor;

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getHasColor() {
        return hasColor;
    }

    public void setHasColor(Boolean hasColor) {
        this.hasColor = hasColor;
    }

    public String getBackgroundStyle() {
        if (this.color != null && !this.color.isEmpty()
                && this.hasColor != null && this.hasColor) {
            StringBuilder sb = new StringBuilder();
            sb.append("background-color: #");
            sb.append(this.color);
            return sb.toString();
        } else {
            return null;
        }
    }
}
