package com.focus.delivery.adapter;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.focus.delivery.R;
import com.focus.delivery.interfaces.FilterableList;
import com.focus.delivery.model.Product;
import com.focus.delivery.util.LibUtil;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by alex on 08/07/17.
 */

public class SectionExpandableProduct extends StatelessSection implements FilterableList {

    private SectionExpandableProductListener mListener;
    private Context mContext;
    private long mLastClickTime = 0;
    private SectionedRecyclerViewAdapter mSectionAdapter;
    private boolean mExpanded;
    private String mSectionName;
    private List<Product> mList;
    private ArrayList<Product> mFilteredList;
    private String mQuery;

    public interface SectionExpandableProductListener {
        void onProductSelected(Product product);
    }

    public SectionExpandableProduct(SectionedRecyclerViewAdapter sectionAdapter, String sectionName, List<Product> list, Fragment frag, boolean expanded) {
        // call constructor with layout resources for this Section header and items
        super(R.layout.section_header_expandable, R.layout.item_product);

        this.mSectionAdapter = sectionAdapter;
        this.mContext = frag.getContext();
        this.mSectionName = sectionName;
        this.mList = list;
        this.mFilteredList = new ArrayList<>(list);
        this.mListener = (SectionExpandableProductListener) frag;
        this.mExpanded = expanded;
        this.mQuery = "";
    }

    @Override
    public int getContentItemsTotal() {
        return mExpanded ? mFilteredList.size() : 0;
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        // return a custom instance of ViewHolder for the items of this section
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;

        // Highlight color
        int highlightColor = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);

        // bind your view here
        final Product selectedProduct = mFilteredList.get(position);

        itemHolder.tvRef.setText(mQuery.isEmpty() ? selectedProduct.getRef() : LibUtil.highlight(mQuery, selectedProduct.getRef(), highlightColor));
        itemHolder.tvLabel.setText(mQuery.isEmpty() ? selectedProduct.getLabel() : LibUtil.highlight(mQuery, selectedProduct.getLabel(), highlightColor));

        itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                mListener.onProductSelected(selectedProduct);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

        // Title
        headerHolder.tvTitle.setText(String.format("%s (%s)", mSectionName, mFilteredList.size()));

        // Arrow expand/collapse
        headerHolder.imgExpand.setImageResource(
                mExpanded ? R.drawable.ic_arrow_drop_up_black_24dp : R.drawable.ic_arrow_drop_down_black_24dp
        );

        // Handle the expand event
        headerHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpanded = !mExpanded;
                headerHolder.imgExpand.setImageResource(
                        mExpanded ? R.drawable.ic_arrow_drop_up_black_24dp : R.drawable.ic_arrow_drop_down_black_24dp
                );
                mSectionAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void filter(String query) {
        mQuery = query;
        if (TextUtils.isEmpty(query)) {
            mFilteredList = new ArrayList<>(mList);
            this.setVisible(true);
        }
        else {
            mFilteredList.clear();
            for (Product product : mList) {
                if (product.getRef().toLowerCase().contains(query.toLowerCase()) ||                         // Search by product ref
                    product.getLabel().toLowerCase().contains(query.toLowerCase())) {                       // Search by product label
                    mFilteredList.add(product);
                }
            }
            this.setVisible(!mFilteredList.isEmpty());
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final ImageView imgExpand;

        private HeaderViewHolder(View view) {
            super(view);
            tvTitle = (TextView) view.findViewById(R.id.header_title);
            imgExpand = (ImageView) view.findViewById(R.id.header_expand);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvRef;
        private final TextView tvLabel;

        private ItemViewHolder(View view) {
            super(view);
            tvRef = (TextView) view.findViewById(R.id.tvRef);
            tvLabel = (TextView) view.findViewById(R.id.tvLabel);
        }
    }
}