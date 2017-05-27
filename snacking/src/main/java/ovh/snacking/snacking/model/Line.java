package ovh.snacking.snacking.model;

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
    private Double total_tva;
    private Double total_tgc;
    private Double total_ttc;

    public Integer getTotal_ht_round() {
        return (int) Math.round(total_ht);
    }

    public Integer getTotal_tva_round() {
        return (int) Math.round(total_tva);
    }

    public Integer getTotal_tgc_round() {
        return (int) Math.round(total_tgc);
    }

    public Integer getTotal_ttc_round() {
        // vrai montant
        // return (int) Math.round(total_ttc);

        // arrondi
        return getTotal_ht_round() + getTotal_tva_round() + getTotal_tgc_round();
    }

    public void setTotal_tgc(Double total_tgc) {
        this.total_tgc = total_tgc;
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

    public void setTotal_ht(Double total_ht) {
        this.total_ht = total_ht;
    }

    public void setTotal_tva(Double total_tva) {
        this.total_tva = total_tva;
    }

    public void setTotal_ttc(Double total_ttc) {
        this.total_ttc = total_ttc;
    }

    public void updatePrices() {
        this.total_ht = qty * subprice;
        this.total_tva = total_ht * (prod.getTva_tx()/100);
        this.total_tgc = total_ht * (prod.getLocaltax1_tx()/100);
        this.total_ttc = total_ht + total_tva + total_tgc;
    }

    public void addQuantity(Integer quantity) {
        this.qty += quantity;
        updatePrices();
    }

}
