package ovh.snacking.snacking.controller;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.realm.Realm;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.Line;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.view.fragment.PreferencesFragment;

/**
 * Created by alexis on 17/06/17.
 * Invoice controller
 */

public class InvoiceController {

    public static Invoice createFacture(Context context, Customer customer) {
        return createInvoice(context, customer, Invoice.FACTURE, null);
    }

    public static Invoice createAvoir(Context context, Customer customer, Integer factureSourceId) {
        return createInvoice(context, customer, Invoice.AVOIR, factureSourceId);
    }

    private static Invoice createInvoice(Context context, final Customer customer, final Integer invoiceType, final Integer factureSourceId) {
        final Integer newInvoiceId = RealmSingleton.getInstance(context).nextInvoiceId();
        Realm realm = RealmSingleton.getInstance(context).getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Invoice invoice = realm.createObject(Invoice.class, newInvoiceId);
                invoice.setCustomer(customer);
                invoice.setType(invoiceType);
                invoice.setFk_facture_source(factureSourceId);
            }
        });
        return realm.where(Invoice.class).equalTo("id", newInvoiceId).findFirst();
    }

    public static String computeInvoiceNextReference(Context context, Integer invoiceType) {
        String prefix = "";
        String nextInvoiceNumber = "";
        switch (invoiceType) {
            case Invoice.FACTURE :
                prefix = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesFragment.PREF_INVOICE_PREFIX_FACTURE, "");
                nextInvoiceNumber = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesFragment.PREF_INVOICE_NEXT_FACTURE, "");
                break;
            case Invoice.AVOIR :
                prefix = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesFragment.PREF_INVOICE_PREFIX_AVOIR, "");
                nextInvoiceNumber = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesFragment.PREF_INVOICE_NEXT_AVOIR, "");
                break;
        }

        // 2 numbers of the current year
        SimpleDateFormat simpleDate = new SimpleDateFormat("yy", Locale.FRANCE);

        int invoiceNumber = Integer.parseInt(nextInvoiceNumber);

        return prefix +
                PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesFragment.PREF_INVOICE_USERNAME, "") +
                simpleDate.format(new Date()) + "-" +
                String.format(Locale.FRANCE, "%04d", invoiceNumber);
    }

    public static Integer getTotalHT(Invoice invoice) {
        Integer total_ht = 0;
        for (Line line : invoice.getLines()) {
            total_ht += line.getTotal_ht_round();
        }
        return total_ht;
    }

    public static HashMap<Double, Integer> getTotalTaxes(Invoice invoice) {
        HashMap<Double, Integer> totalTaxes = new HashMap<>();

        // First compute the total of each tax rate
        for(Line line : invoice.getLines()) {
            Double taxRate = line.getProd().getTaxRate();
            if (totalTaxes.containsKey(taxRate)) {
                int currentTaxAmount = totalTaxes.get(taxRate);
                totalTaxes.remove(taxRate);
                totalTaxes.put(taxRate, currentTaxAmount + line.getTotal_tax_round());
            } else {
                totalTaxes.put(line.getProd().getTaxRate(), line.getTotal_tax_round());
            }
        }

        return totalTaxes;
    }

    public static Integer getTotalTax2(Invoice invoice) {
        Integer total_tax2 = 0;
        for (Line line : invoice.getLines()) {
            total_tax2 += line.getTotal_tax2_round();
        }
        return total_tax2;
    }

    public static Integer getTotalTTC(Invoice invoice) {
        Integer total_ttc = 0;
        for (Line line : invoice.getLines()) {
            total_ttc += line.getTotal_ttc_round();
        }
        return total_ttc;
    }

    private static void updateNextInvoiceNumber(Context context, Invoice invoice) {
        switch (invoice.getType()) {
            case Invoice.FACTURE :
                String nextFactureNb = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesFragment.PREF_INVOICE_NEXT_FACTURE, "");
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putString(PreferencesFragment.PREF_INVOICE_NEXT_FACTURE, String.valueOf(Integer.parseInt(nextFactureNb) + 1))
                        .apply();
                break;
            case Invoice.AVOIR :
                String nextAvoirNb = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferencesFragment.PREF_INVOICE_NEXT_AVOIR, "");
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putString(PreferencesFragment.PREF_INVOICE_NEXT_AVOIR, String.valueOf(Integer.parseInt(nextAvoirNb) + 1))
                        .apply();
                break;
        }
    }

    public static boolean finishInvoice(final Context context, final Invoice invoice) {
        Realm realm = RealmSingleton.getInstance(context).getRealm();
        if (invoice.getLines().size() > 0 && invoice.getState() == Invoice.ONGOING) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    invoice.setState(Invoice.FINISHED);

                    // Actual date
                    invoice.setDate(new Date());

                    // Compute next invoice reference
                    invoice.setRef(computeInvoiceNextReference(context, invoice.getType()));
                }
            });
            updateNextInvoiceNumber(context, invoice);
            return true;
        }
        // When we modified the last invoice
        else if (invoice.getLines().size() > 0 && invoice.getState() == Invoice.FINISHED) {
            return true;
        } else {
            Toast.makeText(context, "La facture est vide", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
