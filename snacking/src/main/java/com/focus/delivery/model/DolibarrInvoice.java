package com.focus.delivery.model;

import com.google.gson.annotations.Expose;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Alex on 03/02/2017.
 */

public class DolibarrInvoice extends RealmObject {
    @Expose
    @PrimaryKey
    private Integer id;
    @Expose private String ref;
    @Expose private String ref_client;
    @Expose private Date date;
    @Expose private Integer socid;
    @Expose private Integer total_ttc;

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

    public String getRef_client() {
        return ref_client;
    }

    public void setRef_client(String ref_client) {
        this.ref_client = ref_client;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getSocid() {
        return socid;
    }

    public void setSocid(Integer socid) {
        this.socid = socid;
    }

    public Integer getTotal_ttc() {
        return total_ttc;
    }

    public void setTotal_ttc(Integer total_ttc) {
        this.total_ttc = total_ttc;
    }
}
