package ovh.snacking.snacking.util;

import android.content.Context;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

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
                    schema.get("Product")
                            .addField("localtax1_tx", Float.class);
                    schema.get("Line")
                            .addField("total_tgc", Integer.class);

                    oldVersion = 4;
                }
            }
        };

        // TODO Améliorer migration, a changer pour la prod, https://realm.io/docs/java/latest/#migrations
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(3)
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
