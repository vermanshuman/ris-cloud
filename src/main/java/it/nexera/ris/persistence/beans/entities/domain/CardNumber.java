package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "card_number")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "CARD_NUM_SEQ", allocationSize = 1)
public class CardNumber extends IndexedEntity {

    private static final long serialVersionUID = -7964842580399974439L;

    @Column(name = "card_number")
    private Long cardNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_type_id", foreignKey = @ForeignKey(name = "FK_CARD_NUMBER_EXAM_TYPE"))
    private ExamType examType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ris_sector_id", foreignKey = @ForeignKey(name = "FK_CARD_NUMBER_RIS_SECTOR"))
    private Sector risSector;

    public Long getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(Long cardNumber) {
        this.cardNumber = cardNumber;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public Sector getRisSector() {
        return risSector;
    }

    public void setRisSector(Sector risSector) {
        this.risSector = risSector;
    }

}
