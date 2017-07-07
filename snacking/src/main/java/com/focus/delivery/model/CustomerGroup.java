package com.focus.delivery.model;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Alex on 06/02/2017.
 */

public class CustomerGroup extends RealmObject {
    public static final String FIELD_NAME       = "name";
    public static final String FIELD_POSITION   = "position";
    public static final String FIELD_CUSTOMERS  = "customers";

    private Integer position;
    private String name;
    private RealmList<Customer> customers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public RealmList<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(RealmList<Customer> customers) {
        this.customers = customers;
    }

    public static CustomerGroup create(Realm realm) {
        CustomerGroup newGroup = realm.createObject(CustomerGroup.class);
        newGroup.setPosition(realm.where(CustomerGroup.class).findAll().size() - 1);
        return newGroup;
    }

    public static void delete(Realm realm, long position) {
        CustomerGroup customerGroup = realm.where(CustomerGroup.class).equalTo(FIELD_POSITION, position).findFirst();

        RealmResults<CustomerGroup> groups = realm.where(CustomerGroup.class).greaterThan(FIELD_POSITION, position).findAll();
        for (CustomerGroup group : groups) {
            group.setPosition(group.getPosition() - 1);
        }

        // Otherwise it has been deleted already.
        if (customerGroup != null) {
            customerGroup.deleteFromRealm();
        }
    }
}
