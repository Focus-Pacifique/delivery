package ovh.snacking.snacking.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.net.Inet4Address;
import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.Line;
import ovh.snacking.snacking.model.ProductGroup;
import ovh.snacking.snacking.model.User;

/**
 * Created by Alex on 20/10/2016.
 */

public class RealmSingleton {

    private static RealmSingleton mInstance;

    private RealmSingleton(Context context) {

        Realm.init(context);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

                // DynamicRealm exposes an editable schema
                RealmSchema schema = realm.getSchema();

                // Migrate to version 1:
                if (oldVersion == 0) {
                    schema.get("User")
                            .addField("serverURL", String.class)
                            .addField("apiKey", String.class);
                    schema.create("ProductGroup")
                            .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY)
                            .addField("name", String.class)
                            .addField("position", Integer.class);
                    schema.get("Product")
                            .addRealmObjectField("group", schema.get("ProductGroup"));
                    oldVersion = 1;
                }

                // Migrate to version 2:
                if (oldVersion == 1) {
                    schema.create("DolibarrInvoice")
                            .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY)
                            .addField("ref", String.class)
                            .addField("ref_client", String.class)
                            .addField("date", Date.class)
                            .addField("socid", Integer.class)
                            .addField("total_ttc", Float.class);
                    schema.create("CustomerGroup")
                            .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY)
                            .addField("name", String.class)
                            .addField("position", Integer.class);
                    schema.create("CustomerAndGroupBinding")
                            .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY)
                            .addField("position", Integer.class)
                            .addRealmObjectField("customer", schema.get("Customer"))
                            .addRealmObjectField("group", schema.get("CustomerGroup"));
                    schema.get("Product")
                            .removeField("group");
                    schema.create("ProductAndGroupBinding")
                            .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY)
                            .addField("position", Integer.class)
                            .addRealmObjectField("product", schema.get("Product"))
                            .addRealmObjectField("group", schema.get("ProductGroup"));

                    /*
                    if (schema.get("ProductGroup").isRequired("id")) {
                        schema.get("ProductGroup")
                                .removeField("id")
                                .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY);
                    }*/
                    oldVersion = 2;
                }


                // Migrate to version 2:
                /*if (oldVersion == 2) {
                    schema.get("DolibarrInvoice")
                            .removeField("date")
                            .addField("date", Date.class);
                }*/

                /*if (oldVersion == 2) {
                    schema.remove("InvoiceChangeBackup");
                    schema.remove("LineInvoiceChangeBackup");
                    schema.create("InvoiceChange")
                            .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY)
                            .addField("date", Date.class)
                            .addField("fk_invoice", Integer.class)
                            .addField("uri", String.class);

                    oldVersion = 0;

                }*/
            }
        };

        // TODO Améliorer migration, a changer pour la prod, https://realm.io/docs/java/latest/#migrations
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(2)
                .migration(migration)
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public static synchronized RealmSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RealmSingleton(context);
        }
        return mInstance;
    }

    public Realm getRealm() {
        return Realm.getDefaultInstance();
    }
}
