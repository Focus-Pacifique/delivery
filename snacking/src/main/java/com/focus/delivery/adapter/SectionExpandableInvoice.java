package com.focus.delivery.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import com.focus.delivery.R;
import com.focus.delivery.controller.InvoiceController;
import com.focus.delivery.model.Invoice;
import com.focus.delivery.util.LibUtil;
import com.focus.delivery.view.fragment.InvoicesExpandableListFragment;

/**
 * Created by alexis on 21/04/17.
 */

public class SectionExpandableInvoice extends StatelessSection implements InvoicesExpandableListFragment.FilterableSection {

    private SectionExpandableInvoicesListener mListener;
    private Context mContext;
    private long mLastClickTime = 0;
    private SectionedRecyclerViewAdapter mSectionAdapter;
    private boolean mExpanded;
    private String mSectionName;
    private ArrayList<Invoice> mList;
    private ArrayList<Invoice> mFilteredList;
    private int mImgHeader;
    private String mQuery;

    public interface SectionExpandableInvoicesListener {
        void onInvoiceSelected(Invoice invoice);
        void deleteInvoice(Invoice invoice, SectionExpandableInvoice adapter);
        void createAvoirFromFacture(Invoice invoice, SectionedRecyclerViewAdapter adapter);
    }

    public SectionExpandableInvoice(SectionedRecyclerViewAdapter sectionAdapter, String sectionName, ArrayList<Invoice> list, Fragment frag, boolean expanded) {
        super(R.layout.section_header_expandable, R.layout.item_invoice);

        this.mSectionAdapter = sectionAdapter;
        this.mContext = frag.getContext();
        this.mSectionName = sectionName;
        this.mList = list;
        this.mFilteredList = new ArrayList<>(list);
        this.mListener = (SectionExpandableInvoicesListener) frag.getActivity();
        this.mImgHeader = 0;
        this.mExpanded = expanded;
        this.mQuery = "";
    }

    public void setImgHeader(int imgHeader) {
        this.mImgHeader = imgHeader;
    }

    public void setList(ArrayList<Invoice> invoices) {
        mList = invoices;
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
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;
        NumberFormat nf = new DecimalFormat("#,###.##");

        // Highlight color
        int highlightColor = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);

        // bind your view here
        final Invoice invoice = mFilteredList.get(position);

        // Invoice type & totalTTC
        if (Invoice.FACTURE == invoice.getType()) {
            itemHolder.invoiceType.setText(R.string.invoice_type_facture);
            itemHolder.invoiceType.setTextColor(Color.BLUE);

            itemHolder.invoiceTotalTTC.setText(String.valueOf(nf.format(InvoiceController.getTotalTTC(invoice)) + " TTC"));
        } else {
            itemHolder.invoiceType.setText(R.string.invoice_type_avoir);
            itemHolder.invoiceType.setTextColor(Color.DKGRAY);

            itemHolder.invoiceTotalTTC.setText(String.valueOf(nf.format(-InvoiceController.getTotalTTC(invoice)) + " TTC"));
        }

        // Customer ref & totalTTC
        if (null != invoice.getCustomer()) {
            if(!mQuery.isEmpty())
                itemHolder.customerName.setText(LibUtil.highlight(mQuery, String.valueOf(invoice.getCustomer().getName()), highlightColor));
            else
                itemHolder.customerName.setText(String.valueOf(invoice.getCustomer().getName()));
        } else {
            itemHolder.customerName.setText(String.valueOf("Problème dans la base"));
        }

        //Datetime
        SimpleDateFormat simpleDate = new SimpleDateFormat("HH:mm  dd/MM", Locale.FRANCE);
        itemHolder.datetime.setText(simpleDate.format(invoice.getDate()));

        //Invoice state part
        if (invoice.getState() == Invoice.ONGOING) {
            itemHolder.invoiceState.setText(R.string.invoice_state_en_cours);
            itemHolder.invoiceState.setTextColor(Color.YELLOW);
        } else if (Invoice.FINISHED == invoice.getState()) {
            itemHolder.invoiceState.setText(R.string.invoice_state_terminee);
            itemHolder.invoiceState.setTextColor(Color.GREEN);
        }

        // Invoice ref
        if(!mQuery.isEmpty())
            itemHolder.invoiceRef.setText(LibUtil.highlight(mQuery, String.valueOf(invoice.getRef()), highlightColor));
        else
            itemHolder.invoiceRef.setText(String.valueOf(invoice.getRef()));

        itemHolder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
           @Override
           public boolean onLongClick(View v) {
               int adapterPosition = itemHolder.getAdapterPosition();
               if (adapterPosition != RecyclerView.NO_POSITION) {
                   if(invoice.getState() == Invoice.FINISHED && Invoice.FACTURE == invoice.getType())
                       mListener.createAvoirFromFacture(invoice, mSectionAdapter);
                   else if (Invoice.ONGOING == invoice.getState())
                       mListener.deleteInvoice(invoice, SectionExpandableInvoice.this);
               }
               return true;
           }
       });

        itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                mListener.onInvoiceSelected(invoice);
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

        // Icon
        if (mImgHeader != 0)
            headerHolder.imgHeader.setImageResource(mImgHeader);

        // Arrow expand/collapse
        headerHolder.imgExpand.setImageResource(
                mExpanded ? R.drawable.ic_arrow_drop_up_black_24dp : R.drawable.ic_arrow_drop_down_black_24dp
        );

        // Handle the expand event
        headerHolder.rootView.setOnClickListener(new View.OnClickListener() {
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

    // Insert a new item to the RecyclerView on a predefined position
    public void insert(Invoice invoice) {
        mList.add(0, invoice);
        mFilteredList.add(0, invoice);
        mSectionAdapter.notifyItemInsertedInSection(mSectionName, 0);
        //mSectionAdapter.notifyItemRangeChangedInSection(mSectionName, 0, mFilteredList.size());
    }

    // Remove a RecyclerView item containing a specified Data object
    public void remove(Invoice invoice) {
        mList.remove(invoice);
        int position = mFilteredList.indexOf(invoice);
        mFilteredList.remove(position);
        mSectionAdapter.notifyItemRemovedFromSection(mSectionName, position);
        //mSectionAdapter.notifyItemRangeChangedInSection(mSectionName, position, mFilteredList.size());
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
            for (Invoice invoice : mList) {
                if (invoice.getRef().toLowerCase().contains(query.toLowerCase()) ||             // Search by Ref
                        invoice.getCustomer().getName().toLowerCase().contains(query.toLowerCase())) {        // Search by customer name
                    mFilteredList.add(invoice);
                }
            }
            this.setVisible(!mFilteredList.isEmpty());
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final ImageView imgHeader;
        private final TextView tvTitle;
        private final ImageView imgExpand;

        private HeaderViewHolder(View view) {
            super(view);
            rootView = view;
            imgHeader = (ImageView) view.findViewById(R.id.header_img);
            tvTitle = (TextView) view.findViewById(R.id.header_title);
            imgExpand = (ImageView) view.findViewById(R.id.header_expand);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView invoiceType;
        private final TextView invoiceRef;
        private final TextView customerName;
        private final TextView invoiceTotalTTC;
        private final TextView datetime;
        private final TextView invoiceState;

        private ItemViewHolder(View view) {
            super(view);
            rootView = view;
            invoiceType = (TextView) view.findViewById(R.id.invoice_type);
            invoiceRef = (TextView) view.findViewById(R.id.invoice_ref);
            customerName = (TextView) view.findViewById(R.id.customer_name);
            invoiceTotalTTC = (TextView) view.findViewById(R.id.invoice_total_ttc);
            datetime = (TextView) view.findViewById(R.id.invoice_datetime);
            invoiceState = (TextView) view.findViewById(R.id.invoice_state);
        }
    }
}
