package com.focus.delivery.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import com.focus.delivery.R;

/**
 * Created by alexis on 16/06/17.
 */

public class AdapterTax extends RecyclerView.Adapter<AdapterTax.ViewHolder> {
    private ArrayList<String[]> mList;

    public AdapterTax(ArrayList<String[]> taxes) {
        mList = taxes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tax_amount, parent, false);

        // create and return ViewHolder
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final String[] price = mList.get(position);

        viewHolder.tvTaxLabel.setText(String.valueOf(price[0]));
        viewHolder.tvTaxAmount.setText(String.valueOf(price[1]));
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTaxLabel;
        private final TextView tvTaxAmount;

        private ViewHolder(View view) {
            super(view);
            tvTaxLabel = (TextView) view.findViewById(R.id.tvTaxLabel);
            tvTaxAmount = (TextView) view.findViewById(R.id.tvTaxAmount);
        }
    }
}
