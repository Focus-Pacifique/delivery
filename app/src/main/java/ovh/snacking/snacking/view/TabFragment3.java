package ovh.snacking.snacking.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import io.realm.Realm;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.LineAdapter;
import ovh.snacking.snacking.controller.RealmSingleton;
import ovh.snacking.snacking.model.Invoice;

/**
 * Created by Alex on 14/11/2016.
 */

public class TabFragment3 extends Fragment {

    private Realm realm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = RealmSingleton.getInstance(getContext()).getRealm();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Integer invoiceId = ((EditingInvoiceFragment) getParentFragment()).getInvoiceId();
        final Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();

        View layout = inflater.inflate(R.layout.tab_fragment_3, container, false);

        TextView text_view_invoice = (TextView) layout.findViewById(R.id.text_view_invoice);
        if (Invoice.AVOIR.equals(invoice.getType())) {
            text_view_invoice.setText(R.string.invoice_type_avoir);
            text_view_invoice.setTextColor(Color.DKGRAY);
        } else {
            text_view_invoice.setText(R.string.invoice_type_facture);
            text_view_invoice.setTextColor(Color.BLUE);
        }

        ListView listView = (ListView) layout.findViewById(R.id.list_view_selected_products);
        LineAdapter lineInvoiceAdapter = new LineAdapter(getContext(), invoice.getLines(), invoice.getId());
        listView.setAdapter(lineInvoiceAdapter);

        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
