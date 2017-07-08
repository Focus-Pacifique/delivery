package com.focus.delivery.model;


import com.google.gson.annotations.Expose;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Alex on 20/10/2016.
 */

public class Customer extends RealmObject {

    public static final String FIELD_ID             = "id";
    public static final String FIELD_NAME           = "name";
    public static final String FIELD_ADDRESS        = "address";
    public static final String FIELD_ZIP            = "zip";
    public static final String FIELD_TOWN           = "town";
    public static final String FIELD_PHONE          = "phone";
    public static final String FIELD_MODIFIED_DATE  = "modifiedDate";

    @Expose
    @PrimaryKey
    private Integer id;
    @Expose private String name;
    @Expose private String address;
    @Expose private String zip;
    @Expose private String town;
    @Expose private String phone;
    @Index private Date modifiedDate;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}