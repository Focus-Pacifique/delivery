package ovh.snacking.snacking.model;

import com.google.gson.annotations.Expose;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Alex on 18/10/2016.
 */

public class ProductCustomerPriceDolibarr extends RealmObject {

    @Expose @PrimaryKey private Integer id;
    @Expose private Integer fk_product;
    @Expose private Integer fk_soc;
    @Expose private Integer price;
    @Index
    private Date modifiedDate;

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

    public Integer getFk_product() {
        return fk_product;
    }

    public void setFk_product(Integer fk_product) {
        this.fk_product = fk_product;
    }

    public Integer getFk_soc() {
        return fk_soc;
    }

    public void setFk_soc(Integer fk_soc) {
        this.fk_soc = fk_soc;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

}
