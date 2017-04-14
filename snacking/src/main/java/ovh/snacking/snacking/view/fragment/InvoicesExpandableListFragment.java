package ovh.snacking.snacking.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.InvoicesExpandableListAdapter;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.view.activity.MainActivity;

/**
 * Created by Alex on 11/11/2016.
 *
 * Fragment to display invoices by date
 */

public class InvoicesExpandableListFragment extends android.support.v4.app.Fragment {

    OnInvoicesExpandableListener mListener;
    private Realm realm;
    private long mLastClickTime = 0;
    private InvoicesExpandableListAdapter mAdapter;
    private ExpandableListView mList;
    private ArrayList<InvoicesExpandableListAdapter.Group> mListItems;
    private FloatingActionButton fab;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnInvoicesExpandableListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnInvoicesExpandableListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_expandable_list, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        mList = (ExpandableListView) layout.findViewById(R.id.exp_listview);
        mListItems = setListItems();
        mAdapter = new InvoicesExpandableListAdapter(getContext(), mListItems);
        mList.setAdapter(mAdapter);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        // On invoice click : go to edit the invoice
        mList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return false;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                final Integer invoiceId = ((Invoice) parent.getExpandableListAdapter().getChild(groupPosition, childPosition)).getId();
                mListener.onInvoiceSelected(invoiceId);
                return true;
            }
        });


        // On long click : remove the print_invoice
        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);

                    final Integer invoiceId = ((Invoice) ((ExpandableListView) parent).getExpandableListAdapter().getChild(groupPosition, childPosition)).getId();
                    mListener.onInvoiceLongClick(invoiceId);
                    return true;
                }
                return false;
            }
        });

        // On fab click : new Invoice
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onNewInvoice();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.title_manage_invoices_fragment));
        fab.setImageResource(R.drawable.ic_new);
        fab.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private ArrayList<Invoice> getTodayTermineeInvoices() {
        RealmResults<Invoice> res;
        try {
            final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            final String strToday = dateFormat.format(new Date());
            final Date today = dateFormat.parse(strToday);

            res = realm.where(Invoice.class).greaterThanOrEqualTo("date", today).equalTo("state", Invoice.TERMINEE).findAllSorted("date", Sort.DESCENDING);

        } catch (ParseException e) {
            res = realm.where(Invoice.class).equalTo("state", Invoice.TERMINEE).findAllSorted("date", Sort.DESCENDING);
        }

        ArrayList<Invoice> invoices = new ArrayList<>();
        for (Invoice invoice : res) {
            invoices.add(invoice);
        }
        return invoices;
    }

    private ArrayList<Invoice> getTodayEnCoursInvoices() {
        RealmResults<Invoice> res;
        try {
            final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            final String strToday = dateFormat.format(new Date());
            final Date today = dateFormat.parse(strToday);

            res = realm.where(Invoice.class).greaterThanOrEqualTo("date", today).equalTo("state", Invoice.EN_COURS).findAllSorted("date", Sort.DESCENDING);

        } catch (ParseException e) {
            res = realm.where(Invoice.class).equalTo("state", Invoice.EN_COURS).findAllSorted("date", Sort.DESCENDING);
        }

        ArrayList<Invoice> invoices = new ArrayList<>();
        for (Invoice invoice : res) {
            invoices.add(invoice);
        }
        return invoices;
    }

    private ArrayList<Invoice> getYesterdayInvoices() {
        try {
            final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            final String strYesterday = dateFormat.format(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24));
            final Date yesterday = dateFormat.parse(strYesterday);

            final String strToday = dateFormat.format(new Date());
            final Date today = dateFormat.parse(strToday);

            return getInvoiceBetweenDates(yesterday, today);
        } catch (ParseException e) {
            Date now = new Date();
            return getInvoiceBetweenDates(now, now);
        }
    }

    private ArrayList<Invoice> getWeekInvoices() {
        try {
            final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            final String strLastWeek = dateFormat.format(new Date(System.currentTimeMillis() - 7 * 1000 * 60 * 60 * 24));
            final Date lastWeek = dateFormat.parse(strLastWeek);

            final String strYesterday = dateFormat.format(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24));
            final Date yesterday = dateFormat.parse(strYesterday);

            return getInvoiceBetweenDates(lastWeek, yesterday);
        } catch (ParseException e) {
            Date now = new Date();
            return getInvoiceBetweenDates(now, now);
        }
    }

    private ArrayList<Invoice> getInvoiceBetweenDates(Date start, Date end) {
        ArrayList<Invoice> invoices = new ArrayList<>();
        RealmResults<Invoice> res = realm.where(Invoice.class).between("date", start, end).findAllSorted("date", Sort.DESCENDING);
        for (Invoice invoice : res) {
            invoices.add(invoice);
        }

        return invoices;
    }

    private ArrayList<InvoicesExpandableListAdapter.Group> setListItems() {
        ArrayList<InvoicesExpandableListAdapter.Group> listGroups = new ArrayList<>();

        InvoicesExpandableListAdapter.Group group;
        ArrayList<Invoice> invoices;

        // Invoice state = EN COURS
        invoices = getTodayEnCoursInvoices();
        group = new InvoicesExpandableListAdapter.Group();
        group.setHeaderLabel(getString(R.string.expandable_list_group_encours) + " (" + invoices.size() + ")");
        group.setItems(invoices);
        listGroups.add(group);

        // Invoice state = TERMINEE
        invoices = getTodayTermineeInvoices();
        group = new InvoicesExpandableListAdapter.Group();
        group.setHeaderLabel(getString(R.string.expandable_list_group_terminee) + " (" + invoices.size() + ")");
        group.setItems(invoices);
        listGroups.add(group);

        // Yesterday
        invoices = getYesterdayInvoices();
        group = new InvoicesExpandableListAdapter.Group();
        group.setIcon(R.drawable.ic_calendar_black_24dp);
        group.setHeaderLabel(getString(R.string.expandable_list_group_yesterday) + " (" + invoices.size() + ")");
        group.setItems(invoices);
        listGroups.add(group);

        // Week
        invoices = getWeekInvoices();
        group = new InvoicesExpandableListAdapter.Group();
        group.setIcon(R.drawable.ic_calendar_black_24dp);
        group.setHeaderLabel(getString(R.string.expandable_list_group_lastweek) + " (" + invoices.size() + ")");
        group.setItems(invoices);
        listGroups.add(group);

        return listGroups;
    }

    public void refreshAdapter() {
        mListItems = setListItems();
        mAdapter = new InvoicesExpandableListAdapter(getContext(), mListItems);
        mList.setAdapter(mAdapter);

        // Expand the header groups by default
        mList.expandGroup(0, true);
    }

    public interface OnInvoicesExpandableListener {
        void onInvoiceSelected(Integer invoiceId);
        void onInvoiceLongClick(Integer invoiceId);
        void onNewInvoice();
    }
}
