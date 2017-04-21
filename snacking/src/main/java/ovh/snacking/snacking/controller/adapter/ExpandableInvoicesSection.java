package ovh.snacking.snacking.controller.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.model.Invoice;

/**
 * Created by alexis on 21/04/17.
 */

public class ExpandableInvoicesSection extends StatelessSection {
    private ExpandableInvoicesSectionListener mListener;
    private Context mContext;
    private SectionedRecyclerViewAdapter mSectionAdapter;
    private boolean mExpanded;
    private String mTitle;
    private ArrayList<Invoice> mList;
    private int mImgHeader;

    public interface ExpandableInvoicesSectionListener {
        void onInvoiceSelected(Invoice invoice);
        void onInvoiceLongClick(Invoice invoice, int adapterPosition, int positionInSection);
    }

    public ExpandableInvoicesSection(SectionedRecyclerViewAdapter sectionAdapter, String title, ArrayList<Invoice> list, Fragment frag, boolean expanded) {
        super(R.layout.section_header_expandable, R.layout.section_item_invoice);

        this.mSectionAdapter = sectionAdapter;
        this.mContext = frag.getContext();
        this.mTitle = title;
        this.mList = list;
        this.mListener = (ExpandableInvoicesSectionListener) frag;
        this.mImgHeader = 0;
        this.mExpanded = expanded;
    }

    public void setImgHeader(int imgHeader) {
        this.mImgHeader = imgHeader;
    }

    public void removeItem(int position) {
        mList.remove(position);
    }

    @Override
    public int getContentItemsTotal() {
        return mExpanded ? mList.size() : 0;
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        // return a custom instance of ViewHolder for the items of this section
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final ItemViewHolder itemHolder = (ItemViewHolder) holder;

        // bind your view here
        final Invoice invoice = mList.get(position);

        // Invoice type part
        if (Invoice.FACTURE.equals(invoice.getType())) {
            itemHolder.invoiceType.setText(R.string.invoice_type_facture);
            itemHolder.invoiceType.setTextColor(Color.BLUE);
        } else {
            itemHolder.invoiceType.setText(R.string.invoice_type_avoir);
            itemHolder.invoiceType.setTextColor(Color.DKGRAY);
        }

        //Customer ref
        if (null != invoice.getCustomer()) {
            itemHolder.customerName.setText(String.valueOf(invoice.getCustomer().getName()));
        } else {
            itemHolder.customerName.setText(String.valueOf("Problème dans la base"));
        }

        //Datetime
        SimpleDateFormat simpleDate = new SimpleDateFormat("HH:mm  dd/MM", Locale.FRANCE);
        itemHolder.datetime.setText(simpleDate.format(invoice.getDate()));

        //Invoice state part
        if (Invoice.ONGOING.equals(invoice.getState())) {
            itemHolder.invoiceState.setText(R.string.invoice_state_en_cours);
            itemHolder.invoiceState.setTextColor(Color.YELLOW);
        } else if (Invoice.FINISHED.equals(invoice.getState())) {
            itemHolder.invoiceState.setText(R.string.invoice_state_terminee);
            itemHolder.invoiceState.setTextColor(Color.GREEN);
        }

        itemHolder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
           @Override
           public boolean onLongClick(View v) {
               int adapterPosition = itemHolder.getAdapterPosition();
               if (adapterPosition != RecyclerView.NO_POSITION) {
                   int positionInSection = mSectionAdapter.getPositionInSection(adapterPosition);
                   mListener.onInvoiceLongClick(invoice, adapterPosition, positionInSection);//, mSectionAdapter.getPositionInSection());
               }



               return true;
           }
       });

        itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        headerHolder.tvTitle.setText(mTitle);

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

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final ImageView imgHeader;
        private final TextView tvTitle;
        private final ImageView imgExpand;

        public HeaderViewHolder(View view) {
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
        private final TextView customerName;
        private final TextView datetime;
        private final TextView invoiceState;

        private ItemViewHolder(View view) {
            super(view);
            rootView = view;
            invoiceType = (TextView) view.findViewById(R.id.invoice_type);
            customerName = (TextView) view.findViewById(R.id.customer_name);
            datetime = (TextView) view.findViewById(R.id.invoice_datetime);
            invoiceState = (TextView) view.findViewById(R.id.invoice_state);
        }
    }
}
