package ovh.snacking.snacking.model;

import com.google.gson.annotations.Expose;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Alex on 12/10/2016.
 */

public class Product extends RealmObject {

    @Expose @PrimaryKey private Integer id;
    @Expose private String ref;
    @Expose private String label;
    @Expose private Integer type;
    @Expose private Float price;
    @Expose private Float tva_tx;
    @Expose private Float localtax1_tx;
    @Index private Date modifiedDate;

    public Product() {
        super();
        this.localtax1_tx = Float.valueOf(0);
    }

    public Float getLocaltax1_tx() {
        return localtax1_tx;
    }

    public void setLocaltax1_tx(Float localtax1_tx) {
        this.localtax1_tx = localtax1_tx;
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

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Float getTva_tx() {
        return tva_tx;
    }

    public void setTva_tx(Float tva_tx) {
        this.tva_tx = tva_tx;
    }

}
