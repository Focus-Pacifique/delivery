package com.focus.delivery.model;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Alex on 29/01/2017.
 */

public class ProductGroup extends RealmObject {
    public static final String FIELD_NAME       = "name";
    public static final String FIELD_POSITION   = "position";
    public static final String FIELD_PRODUCTS   = "products";

    private Integer position;
    private String name;
    private RealmList<Product> products;

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Product> getProducts() {
        return products;
    }

    public static ProductGroup create(Realm realm) {
        ProductGroup newGroup = realm.createObject(ProductGroup.class);
        newGroup.setPosition(realm.where(ProductGroup.class).findAll().size() - 1);
        return newGroup;
    }

    public static void delete(Realm realm, long position) {
        ProductGroup productGroup = realm.where(ProductGroup.class).equalTo(FIELD_POSITION, position).findFirst();

        RealmResults<ProductGroup> groups = realm.where(ProductGroup.class).greaterThan(FIELD_POSITION, position).findAll();
        for (ProductGroup group : groups) {
            group.setPosition(group.getPosition() - 1);
        }

        // Otherwise it has been deleted already.
        if (productGroup != null) {
            productGroup.deleteFromRealm();
        }
    }
}
