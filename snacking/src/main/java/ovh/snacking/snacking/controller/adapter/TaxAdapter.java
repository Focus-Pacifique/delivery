package ovh.snacking.snacking.controller.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ovh.snacking.snacking.R;

/**
 * Created by alexis on 16/06/17.
 */

public class TaxAdapter extends RecyclerView.Adapter<TaxAdapter.TaxViewHolder> {
    private ArrayList<String[]> mList;
    private Context mContext;

    public TaxAdapter(Context context, ArrayList<String[]> taxes) {
        mList = taxes;
        mContext = context;
    }

    @Override
    public TaxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tax_amount, parent, false);

        // create and return ViewHolder
        return new TaxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaxViewHolder viewHolder, int position) {
        final String[] price = mList.get(position);

        viewHolder.tvTaxLabel.setText(String.valueOf(price[0]));
        viewHolder.tvTaxAmount.setText(String.valueOf(price[1]));
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

    public static class TaxViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTaxLabel;
        private final TextView tvTaxAmount;

        private TaxViewHolder(View view) {
            super(view);
            tvTaxLabel = (TextView) view.findViewById(R.id.tvTaxLabel);
            tvTaxAmount = (TextView) view.findViewById(R.id.tvTaxAmount);
        }
    }
}
