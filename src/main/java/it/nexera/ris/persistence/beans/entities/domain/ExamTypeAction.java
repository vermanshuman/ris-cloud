package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ThreeStateCheckbox;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "exam_type_action")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "EXAM_TYPE_ACT_SEQ", allocationSize = 1)
public class ExamTypeAction extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = 8790641132585926490L;

    @ManyToOne
    @JoinColumn(name = "exam_type_id", foreignKey = @ForeignKey(name = "FK_EX_TYPE_ACT_EX_TYPE"))
    private ExamType examType;

    @ManyToOne
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "FK_EX_TYPE_ACT_ROLE"))
    private Role role;

    @Column
    private ThreeStateCheckbox state;

    public ExamTypeAction() {
        super();
    }

    public ExamTypeAction(ExamType examType, Role role, ThreeStateCheckbox state) {
        super();
        this.examType = examType;
        this.role = role;
        this.state = state;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public ThreeStateCheckbox getState() {
        return state;
    }

    public void setState(ThreeStateCheckbox state) {
        this.state = state;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
