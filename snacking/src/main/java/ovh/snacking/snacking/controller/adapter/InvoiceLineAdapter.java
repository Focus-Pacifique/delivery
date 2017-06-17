package ovh.snacking.snacking.controller.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.Line;
import ovh.snacking.snacking.util.RealmSingleton;

/**
 * Created by Alex on 21/11/2016.
 * Invoice line adapter
 */

public class InvoiceLineAdapter extends RealmRecyclerViewAdapter<Line, RecyclerView.ViewHolder> {

    public static final int VIEW_PRINT = 1;
    public static final int VIEW_EDIT  = 2;

    private Integer invoiceType;
    private Context mContext;
    private int mViewType;

    public InvoiceLineAdapter(Context context, OrderedRealmCollection<Line> realmResults, Integer invoiceType, int viewType) {
        super(realmResults, true);
        this.invoiceType = invoiceType;
        this.mContext = context;
        this.mViewType = viewType;
    }

    @Override
    public int getItemViewType(int position) {
        return mViewType;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Line line = getData().get(position);
        switch (mViewType) {
            case VIEW_PRINT :
                bindPrintViewHolder((PrintViewHolder) viewHolder, line);
                break;
            case VIEW_EDIT :
                bindEditViewHolder((EditViewHolder) viewHolder, line);
                break;
        }
    }

    private void bindPrintViewHolder(PrintViewHolder viewHolder, Line line) {
        viewHolder.tvProduct.setText(String.valueOf(line.getProd().getLabel()));
        viewHolder.tvPriceHT.setText(NumberFormat.getIntegerInstance(Locale.FRANCE).format(Invoice.AVOIR == invoiceType ? -line.getSubprice() : line.getSubprice()));
        viewHolder.tvQty.setText(NumberFormat.getIntegerInstance(Locale.FRANCE).format(line.getQty()));
        viewHolder.tvTotalLineHT.setText(NumberFormat.getIntegerInstance(Locale.FRANCE).format(Invoice.AVOIR == invoiceType ? -line.getTotal_ht_round() : line.getTotal_ht_round()));
    }

    private void bindEditViewHolder(EditViewHolder viewHolder, final Line line) {
        NumberFormat nf = new DecimalFormat("#,###.##");

        viewHolder.tvQty.setText(String.valueOf(line.getQty().toString()));
        viewHolder.tvProduct.setText(String.valueOf(line.getProd().getRef()));
        viewHolder.tvPriceHT.setText(String.valueOf("(" + line.getSubprice() + " HT) "));
        Double taxes = line.getProd().getTaxRate() + line.getProd().getSecondTaxRate();
        viewHolder.tvTaxes.setText(String.valueOf(nf.format(taxes)  + "%"));

        // Handle plus button
        viewHolder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLineQuantity(1, line);
            }
        });

        // Handle minus button
        viewHolder.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (line.getQty() > 1) {
                    addLineQuantity(-1, line);
                } else {
                   deleteLine(line);
                }
            }
        });

        // Handle delete button
        viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               deleteLine(line);
            }
        });
    }

    private void deleteLine(final Line line) {
        final Realm realm = RealmSingleton.getInstance(mContext).getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Line.class).equalTo("id", line.getId()).findFirst().deleteFromRealm();
            }
        });
        realm.close();
    }

    private void addLineQuantity(final Integer qty, final Line line) {
        final Realm realm = RealmSingleton.getInstance(mContext).getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                line.addQuantity(qty);
            }
        });
        realm.close();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_PRINT :
                return new PrintViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice_line_print, parent, false));
            case VIEW_EDIT :
                return new EditViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice_line_edit, parent, false));
            default:
                return new PrintViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice_line_print, parent, false));
        }
    }

    public static class PrintViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProduct;
        private final TextView tvPriceHT;
        private final TextView tvQty;
        private final TextView tvTotalLineHT;

        private PrintViewHolder(View view) {
            super(view);
            tvProduct = (TextView) view.findViewById(R.id.tvProduct);
            tvPriceHT = (TextView) view.findViewById(R.id.tvPriceHT);
            tvQty = (TextView) view.findViewById(R.id.tvQty);
            tvTotalLineHT = (TextView) view.findViewById(R.id.tvTotalLineHT);
        }
    }

    public static class EditViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProduct;
        private final TextView tvPriceHT;
        private final TextView tvQty;
        private final TextView tvTaxes;
        private final Button btnPlus;
        private final Button btnMinus;
        private final ImageButton btnDelete;

        private EditViewHolder(View view) {
            super(view);
            tvProduct = (TextView) view.findViewById(R.id.tvProduct);
            tvPriceHT = (TextView) view.findViewById(R.id.tvPriceHT);
            tvQty = (TextView) view.findViewById(R.id.tvQty);
            tvTaxes = (TextView) view.findViewById(R.id.tvTaxes);
            btnPlus = (Button) view.findViewById(R.id.btnPlus);
            btnMinus = (Button) view.findViewById(R.id.btnMinus);
            btnDelete = (ImageButton) view.findViewById(R.id.btnDelete);
        }
    }
}
