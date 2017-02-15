package ovh.snacking.snacking.model;

import android.net.Uri;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Pc on 17/12/2016.
 */

public class InvoiceChange extends RealmObject {
    @PrimaryKey
    @Required
    private Integer id;
    private Date date;
    private Integer fk_invoice;
    private String uri;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getFk_invoice() {
        return fk_invoice;
    }

    public void setFk_invoice(Integer fk_invoice) {
        this.fk_invoice = fk_invoice;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
