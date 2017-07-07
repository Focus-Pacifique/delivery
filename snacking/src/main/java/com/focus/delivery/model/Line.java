package com.focus.delivery.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Alex on 21/10/2016.
 */

public class Line extends RealmObject {

    @PrimaryKey
    private Integer id;
    private Product prod;
    private Double subprice;
    private Integer qty;
    private Double total_ht;
    private Double total_tax;
    private Double total_tax2;
    private Double total_ttc;

    public Integer getTotal_ht_round() {
        return (int) Math.round(total_ht);
    }

    public Integer getTotal_tax_round() {
        return (int) Math.round(total_tax);
    }

    public Integer getTotal_tax2_round() {
        return (int) Math.round(total_tax2);
    }

    public Integer getTotal_ttc_round() {
        // vrai montant
        // return (int) Math.round(total_ttc);

        // arrondi pour la TGC et la TSS
        return getTotal_ht_round() + getTotal_tax_round() + getTotal_tax2_round();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Product getProd() {
        return prod;
    }

    public void setProd(Product prod) {
        this.prod = prod;
    }

    public Double getSubprice() {
        return subprice;
    }

    public void setSubprice(Double subprice) {
        this.subprice = subprice;
    }

    public void updatePrices() {
        this.total_ht = qty * subprice;
        this.total_tax = total_ht * (prod.getTaxRate()/100);
        this.total_tax2 = total_ht * (prod.getSecondTaxRate()/100);
        this.total_ttc = total_ht + total_tax + total_tax2;
    }

    public void addQuantity(Integer quantity) {
        this.qty += quantity;
        updatePrices();
    }

}
