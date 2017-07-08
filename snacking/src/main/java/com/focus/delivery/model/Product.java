package com.focus.delivery.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Alex on 12/10/2016.
 */

public class Product extends RealmObject {

    public static final String FIELD_ID                 = "id";
    public static final String FIELD_REF                = "ref";
    public static final String FIELD_LABEL              = "label";
    public static final String FIELD_TYPE               = "type";
    public static final String FIELD_PRICES             = "price";
    public static final String FIELD_TAX_RATE           = "taxRate";
    public static final String FIELD_SECOND_TAX_RATE    = "secondTaxRate";
    public static final String FIELD_MODIFIED_DATE      = "modifiedDate";

    @Expose @PrimaryKey private Integer id;
    @Expose private String ref;
    @Expose private String label;
    @Expose private Integer type;
    @Expose private Double price;
    @SerializedName("tva_tx") @Expose private Double taxRate;
    @SerializedName("localtax1_tx") @Expose private Double secondTaxRate;
    @Index private Date modifiedDate;

    public Product() {
        super();
        this.secondTaxRate = 0.0;
    }

    public Double getSecondTaxRate() {
        return secondTaxRate;
    }

    public void setSecondTaxRate(Double secondTaxRate) {
        this.secondTaxRate = secondTaxRate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }

}
