package ovh.snacking.snacking.util;

import android.content.Context;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;
import ovh.snacking.snacking.model.Invoice;

/**
 * Created by Alex on 20/10/2016.
 */

public class RealmSingleton {

    private static RealmSingleton mInstance;

    private RealmSingleton(Context context) {

        Realm.init(context);

        // TODO Améliorer migration, a changer pour la prod, https://realm.io/docs/java/latest/#migrations
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(3)
                .migration(realmMigration())
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

    private RealmMigration realmMigration() {
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
                    oldVersion = 2;
                }


                // Migrate to version 3:
                if (oldVersion == 2) {
                    schema.get("Invoice")
                            .addField("id_dolibarr", Integer.class)
                            .removeField("fk_facture_source_dolibarr");
                    schema.get("Product")
                            .addField("modifiedDate", Date.class)
                            .addIndex("modifiedDate");
                    schema.get("Customer")
                            .addField("modifiedDate", Date.class)
                            .addIndex("modifiedDate");
                    schema.get("ProductCustomerPriceDolibarr")
                            .addField("modifiedDate", Date.class)
                            .addIndex("modifiedDate");
                    schema.get("User")
                            .addField("initialValues", Integer.class);
                    oldVersion = 3;
                }

                if (oldVersion == 3) {
                    if(schema.get("Product").hasField("localtax1_tx")) {
                        schema.get("Product")
                                .removeField("localtax1_tx");
                    }
                    schema.get("Product")
                            .addField("localtax1_tx", Double.class)
                            .removeField("price")
                            .addField("price", Integer.class)
                            .removeField("tva_tx")
                            .addField("tva_tx", Double.class);

                    if(schema.get("Line").hasField("total_tgc")) {
                        schema.get("Line")
                                .removeField("total_tgc");
                    }
                    schema.get("Line")
                            .addField("total_tgc", Integer.class);

                    schema.get("ProductCustomerPriceDolibarr")
                            .removeField("price")
                            .addField("price", Integer.class);

                    oldVersion = 4;
                }
            }
        };

        return migration;
    }

    public Integer nextInvoiceId() {
        if(null != getRealm().where(Invoice.class).findFirst()) {
            Integer nextId = getRealm().where(Invoice.class).max("id").intValue() + 1;
            return nextId;
        } else {
            return 1;
        }
    }
}
