package com.yiilab.inside.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yiilab.inside.model.btc.TickerFields;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(
        name = "financial_instrument",
        indexes = {
                @Index(name = "hidden_idx", columnList = "hidden"),
                @Index(name = "name_idx", columnList = "name"),
                @Index(name = "category_idx", columnList = "category")
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="instrument_type")
@DiscriminatorValue("instrument")
public class FinancialInstrument extends AbstractEntity {
    @Column(nullable = false)
    private Byte decimalPlaces;

    @Column(nullable = false)
    private Float minPrice;

    @Column(nullable = false)
    private Float maxPrice;

    @Column(nullable = false)
    private Float minAmount;

    @Column(nullable = false)
    private Float fee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Exchange exchange;

    @Column(nullable = false)
    private String name;

    @Column
    private Long created;

    @Column
    private Long updated;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false)
    private Boolean hidden;

    @OneToMany(mappedBy = "financialInstrument", fetch = FetchType.LAZY)
    private List<FavoriteInstrument> favoriteInstruments;

    @Embedded
    private TickerFields tickerFields;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Byte getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(Byte decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public Float getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Float minPrice) {
        this.minPrice = minPrice;
    }

    public Float getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Float maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Float getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Float minAmount) {
        this.minAmount = minAmount;
    }

    public Float getFee() {
        return fee;
    }

    public void setFee(Float fee) {
        this.fee = fee;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }



    public TickerFields getTickerFields() {
        return tickerFields;
    }

    public void setTickerFields(TickerFields tickerFields) {
        this.tickerFields = tickerFields;
    }

    @PrePersist
    private void setDatesOnPersist(){
        final Long currentDate = new Date().getTime();
        setUpdated(currentDate);
        setCreated(currentDate);
    }

    @PreUpdate
    private void setDatesOnUpdate(){
        setUpdated(new Date().getTime());
    }
}
