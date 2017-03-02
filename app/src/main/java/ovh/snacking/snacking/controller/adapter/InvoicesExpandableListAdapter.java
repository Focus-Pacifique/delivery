package ovh.snacking.snacking.controller.adapter;

import ovh.snacking.snacking.R;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.Product;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Alex on 29/01/2017.
 *
 * Adapter to display invoices by date
 */

public class InvoicesExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private ArrayList<ExpandableListGroup> mGroups;

    public InvoicesExpandableListAdapter(Context context, ArrayList<ExpandableListGroup> groups) {
        this.mGroups = groups;
        this.mContext = context;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ExpandableListGroup group = (ExpandableListGroup) getGroup(groupPosition);

        final InvoicesExpandableListAdapter.ViewHolder groupHolder;
        if (convertView == null) {
            groupHolder = new InvoicesExpandableListAdapter.ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_list_group_item, parent, false);
            groupHolder.icon = (ImageView) convertView.findViewById(R.id.group_img);
            groupHolder.customerName = (TextView) convertView.findViewById(R.id.group_header);
            groupHolder.expandImage = (ImageView) convertView.findViewById(R.id.group_expand);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (InvoicesExpandableListAdapter.ViewHolder) convertView.getTag();
        }

        // Icon
        if (groupHolder.icon != null)
            groupHolder.icon.setImageResource(group.getIcon());

        // Group name
        groupHolder.customerName.setText(String.valueOf(group.getName()));

        // Expand icon
        if (isExpanded) {
            groupHolder.expandImage.setImageResource(R.drawable.ic_expand_more_black_24dp);
        } else {
            groupHolder.expandImage.setImageResource(R.drawable.ic_expand_less_black_24dp);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Invoice child = (Invoice) getChild(groupPosition, childPosition);
        final InvoicesExpandableListAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.section_item_invoice, parent, false);
            viewHolder = new InvoicesExpandableListAdapter.ViewHolder();
            viewHolder.invoiceType = (TextView) convertView.findViewById(R.id.invoice_type);
            viewHolder.customerName = (TextView) convertView.findViewById(R.id.customer_name);
            viewHolder.datetime = (TextView) convertView.findViewById(R.id.invoice_datetime);
            viewHolder.invoiceState = (TextView) convertView.findViewById(R.id.invoice_state);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (InvoicesExpandableListAdapter.ViewHolder) convertView.getTag();
        }

        // Invoice type part
        if (Invoice.FACTURE.equals(child.getType())) {
            viewHolder.invoiceType.setText(R.string.invoice_type_facture);
            viewHolder.invoiceType.setTextColor(Color.BLUE);
        } else {
            viewHolder.invoiceType.setText(R.string.invoice_type_avoir);
            viewHolder.invoiceType.setTextColor(Color.DKGRAY);
        }

        //Customer ref
        if (null != child.getCustomer()) {
            viewHolder.customerName.setText(String.valueOf(child.getCustomer().getName()));
        } else {
            viewHolder.customerName.setText(String.valueOf("Problème dans la base"));
        }

        //Datetime
        SimpleDateFormat simpleDate = new SimpleDateFormat("HH:mm  dd/MM", Locale.FRANCE);
        viewHolder.datetime.setText(simpleDate.format(child.getDate()));

        //Invoice state part
        if (Invoice.EN_COURS.equals(child.getState())) {
            viewHolder.invoiceState.setText(R.string.invoice_state_en_cours);
            viewHolder.invoiceState.setTextColor(Color.YELLOW);
        } else if (Invoice.TERMINEE.equals(child.getState())) {
            viewHolder.invoiceState.setText(R.string.invoice_state_terminee);
            viewHolder.invoiceState.setTextColor(Color.GREEN);
        }

        return convertView;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList chList = mGroups.get(groupPosition).getItems();
        return chList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList chList = mGroups.get(groupPosition).getItems();
        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView invoiceType;
        TextView customerName;
        TextView datetime;
        TextView invoiceState;
        ImageView expandImage;
    }

    public static class ExpandableListChild {
        private String invoiceType;
        private String customerName;
        private String datetime;
        private String invoiceState;

        public String getInvoiceType() {
            return invoiceType;
        }

        public void setInvoiceType(String invoiceType) {
            this.invoiceType = invoiceType;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public String getInvoiceState() {
            return invoiceState;
        }

        public void setInvoiceState(String invoiceState) {
            this.invoiceState = invoiceState;
        }
    }

    public static class ExpandableListGroup {
        private String name;
        private int icon;
        private ArrayList items;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public ArrayList getItems() {
            return items;
        }

        public void setItems(ArrayList items) {
            this.items = items;
        }
    }
}
