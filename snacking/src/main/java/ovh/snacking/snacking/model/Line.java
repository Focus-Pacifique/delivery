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
    private Integer subprice;
    private Integer qty;
    private Integer total_ht;
    private Integer total_tva;
    private Integer total_tgc;
    private Integer total_ttc;

    public Integer getTotal_tgc() {
        return total_tgc;
    }

    public void setTotal_tgc(Integer total_tgc) {
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

    public Integer getSubprice() {
        return subprice;
    }

    public void setSubprice(Integer subprice) {
        this.subprice = subprice;
    }

    public Integer getTotal_ht() {
        return total_ht;
    }

    public void setTotal_ht(Integer total_ht) {
        this.total_ht = total_ht;
    }

    public Integer getTotal_tva() {
        return total_tva;
    }

    public void setTotal_tva(Integer total_tva) {
        this.total_tva = total_tva;
    }

    public Integer getTotal_ttc() {
        return total_ttc;
    }

    public void setTotal_ttc(Integer total_ttc) {
        this.total_ttc = total_ttc;
    }

    public void updatePrices() {
        this.total_ht = qty * subprice;
        this.total_tva = Math.round(total_ht * (prod.getTva_tx()/100) );
        this.total_tgc = Math.round(total_ht * (prod.getLocaltax1_tx()/100) );
        this.total_ttc = total_ht + total_tva + total_tgc;
    }

    public void addQuantity(Integer quantity) {
        this.qty += quantity;
        updatePrices();
    }

}
