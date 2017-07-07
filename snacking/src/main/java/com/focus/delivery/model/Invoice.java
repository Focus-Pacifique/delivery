package com.focus.delivery.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Alex on 11/10/2016.
 *
 */

public class Invoice extends RealmObject {

    public static final int ONGOING = 0;
    public static final int FINISHED = 1;

    public static final int FACTURE = 0;
    public static final int AVOIR = 1;

    @PrimaryKey
    private Integer id;
    private Integer type;
    private Integer state;
    private Date date;
    private String ref;
    private Customer customer;
    private RealmList<Line> lines;

    private Integer fk_facture_source;
    private Integer id_dolibarr;
    private Boolean isPOSTToDolibarr;
    private Integer counterPrint;

    public Invoice() {
        super();
        this.type = FACTURE;
        this.date = new Date();
        this.state = ONGOING;
        this.lines = new RealmList<>();
        this.ref = "0000";
        this.isPOSTToDolibarr = false;
        this.counterPrint = 0;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public RealmList<Line> getLines() {
        return lines;
    }

    public void setLines(RealmList<Line> line) {
        this.lines = line;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Boolean isPOSTToDolibarr() {
        return isPOSTToDolibarr;
    }

    public void setIsPOSTToDolibarr(Boolean isPOSTToDolibarr) {
        this.isPOSTToDolibarr = isPOSTToDolibarr;
    }

    public Integer getFk_facture_source() {
        return fk_facture_source;
    }

    public void setFk_facture_source(Integer fk_facture_source) {
        this.fk_facture_source = fk_facture_source;
    }

    public Integer getCounterPrint() {
        return counterPrint;
    }

    public void addCounterPrint() {
        this.counterPrint++;
    }

    public Integer getId_dolibarr() {
        return id_dolibarr;
    }

    public void setId_dolibarr(Integer id_dolibarr) {
        this.id_dolibarr = id_dolibarr;
    }
}
