package ovh.snacking.snacking.view.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.ExpandableInvoicesSection;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.view.activity.MainActivity;

/**
 * Created by Alex on 11/11/2016.
 *
 * Fragment to display invoices by date
 */

public class InvoicesExpandableListFragment extends android.support.v4.app.Fragment
        implements ExpandableInvoicesSection.ExpandableInvoicesSectionListener {

    public static final String SECTION_ONGOING = "SectionOngoing";
    public static final String SECTION_FINISHED = "SectionFinished";
    public static final String SECTION_YESTERDAY = "SectionYesterday";
    public static final String SECTION_LASTWEEK = "SectionLastWekk";

    OnInvoicesExpandableListener mListener;
    private Realm realm;
    private long mLastClickTime = 0;
    private SectionedRecyclerViewAdapter mAdapter;
//    private ExpandableListView mList;
//    private ArrayList<InvoicesExpandableListAdapter.Group> mListItems;
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
        View layout = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        // Populate adapter
        if(mAdapter == null) {
            mAdapter = new SectionedRecyclerViewAdapter();
            populateAdapter();
        }

        // Set adapter to recycler view
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        //mAdapter = new InvoicesExpandableListAdapter(getContext(), mListItems);
        //mList.setAdapter(mAdapter);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        // On invoice click : go to edit the invoice
        /*mList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return false;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                final Integer invoiceId = ((Invoice) parent.getExpandableListAdapter().getChild(groupPosition, childPosition)).getId();
                mListener.goToInvoice(invoiceId);
                return true;
            }
        });*/


        // On long click : remove the print_invoice
        /*mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
        });*/

        // On fab click : new Invoice
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.newInvoice();
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

            res = realm.where(Invoice.class).greaterThanOrEqualTo("date", today).equalTo("state", Invoice.FINISHED).findAllSorted("date", Sort.DESCENDING);

        } catch (ParseException e) {
            res = realm.where(Invoice.class).equalTo("state", Invoice.FINISHED).findAllSorted("date", Sort.DESCENDING);
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

            res = realm.where(Invoice.class).greaterThanOrEqualTo("date", today).equalTo("state", Invoice.ONGOING).findAllSorted("date", Sort.DESCENDING);

        } catch (ParseException e) {
            res = realm.where(Invoice.class).equalTo("state", Invoice.ONGOING).findAllSorted("date", Sort.DESCENDING);
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

    private ArrayList<Invoice> getLastWeekInvoices() {
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

    private void populateAdapter() {

        // Section invoice state = ONGOING
        ArrayList<Invoice> invoices = getTodayEnCoursInvoices();
        String headerOngoing = String.format("%s (%s)", getString(R.string.invoices_ongoing), invoices.size());
        Section sectionEnCours = new ExpandableInvoicesSection(mAdapter, headerOngoing, invoices, this, true);
        mAdapter.addSection(SECTION_ONGOING, sectionEnCours);

        // Invoice state = FINISHED
        invoices = getTodayTermineeInvoices();
        String headerFinished = String.format("%s (%s)", getString(R.string.invoices_finished), invoices.size());
        Section sectionFinished = new ExpandableInvoicesSection(mAdapter, headerFinished, invoices, this, false);
        mAdapter.addSection(SECTION_FINISHED, sectionFinished);

        // Yesterday
        invoices = getYesterdayInvoices();
        String headerYesterday = String.format("%s (%s)", getString(R.string.invoices_yesterday), invoices.size());
        Section sectionYesterday = new ExpandableInvoicesSection(mAdapter, headerYesterday, invoices, this, false);
        ((ExpandableInvoicesSection) sectionYesterday).setImgHeader(R.drawable.ic_calendar_black_24dp);
        mAdapter.addSection(SECTION_YESTERDAY, sectionYesterday);

        // Week
        invoices = getLastWeekInvoices();
        String headerLastWeek = String.format("%s (%s)", getString(R.string.invoices_last_week), invoices.size());
        Section sectionLastWeek = new ExpandableInvoicesSection(mAdapter, headerLastWeek, invoices, this, false);
        ((ExpandableInvoicesSection) sectionLastWeek).setImgHeader(R.drawable.ic_calendar_black_24dp);
        mAdapter.addSection(SECTION_LASTWEEK, sectionLastWeek);
    }

    /*private ArrayList<InvoicesExpandableListAdapter.Group> setListItems() {
        ArrayList<InvoicesExpandableListAdapter.Group> listGroups = new ArrayList<>();

        InvoicesExpandableListAdapter.Group group;
        ArrayList<Invoice> invoices;

        // Invoice state = EN COURS
        invoices = getTodayEnCoursInvoices();
        group = new InvoicesExpandableListAdapter.Group();
        group.setHeaderLabel(getString(R.string.invoices_ongoing) + " (" + invoices.size() + ")");
        group.setItems(invoices);
        listGroups.add(group);

        // Invoice state = FINISHED
        invoices = getTodayTermineeInvoices();
        group = new InvoicesExpandableListAdapter.Group();
        group.setHeaderLabel(getString(R.string.invoices_finished) + " (" + invoices.size() + ")");
        group.setItems(invoices);
        listGroups.add(group);

        // Yesterday
        invoices = getYesterdayInvoices();
        group = new InvoicesExpandableListAdapter.Group();
        group.setIcon(R.drawable.ic_calendar_black_24dp);
        group.setHeaderLabel(getString(R.string.invoices_yesterday) + " (" + invoices.size() + ")");
        group.setItems(invoices);
        listGroups.add(group);

        // Week
        invoices = getLastWeekInvoices();
        group = new InvoicesExpandableListAdapter.Group();
        group.setIcon(R.drawable.ic_calendar_black_24dp);
        group.setHeaderLabel(getString(R.string.invoices_last_week) + " (" + invoices.size() + ")");
        group.setItems(invoices);
        listGroups.add(group);

        return listGroups;
    }*/

    public void refreshAdapter() {
        /*mListItems = setListItems();
        mAdapter = new InvoicesExpandableListAdapter(getContext(), mListItems);
        mList.setAdapter(mAdapter);

        // Expand the header groups by default
        mList.expandGroup(0, true);*/
    }

    @Override
    public void onInvoiceSelected(Invoice invoice) {
        mListener.goToInvoice(invoice);
    }

    @Override
    public void onInvoiceLongClick(final Invoice invoice, final int adapterPosition, final int positionInSection) {
        if (Invoice.FINISHED.equals(invoice.getState())) {
            if (Invoice.FACTURE.equals(invoice.getType())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialog_title_create_avoir)
                        .setMessage("Voulez-vous créer un avoir pour " + invoice.getCustomer().getName() + " (Facture n°" + invoice.getRef() + ") ?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Integer invoiceId = createInvoice(invoice.getCustomer().getId(), Invoice.AVOIR, invoice.getId());
                                Invoice invoiceCreated = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();
                                ((ExpandableInvoicesSection) mAdapter.getSectionForPosition(adapterPosition)).addItem(invoiceCreated);
                                mAdapter.notifyItemRemoved(adapterPosition);
                                mListener.goToInvoice(invoiceCreated);
                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog ad = builder.create();
                ad.show();

            } else {
                Toast.makeText(getActivity(), "Impossible de créer un avoir sur un avoir", Toast.LENGTH_LONG).show();
            }
        } else if (Invoice.ONGOING.equals(invoice.getState())){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            if (Invoice.FACTURE.equals(invoice.getType())) {
                builder.setTitle("Supprimer la facture");
            } else if (Invoice.AVOIR.equals(invoice.getType())) {
                builder.setTitle("Supprimer l'avoir");
            } else {
                builder.setTitle("Supprimer");
            }

            builder.setMessage("Etes-vous sur ?")
                    .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    invoice.deleteFromRealm();
                                }
                            });
                            ((ExpandableInvoicesSection) mAdapter.getSectionForPosition(adapterPosition)).removeItem(positionInSection);
                            mAdapter.notifyItemRemoved(adapterPosition);
                        }
                    })
                    .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog ad = builder.create();
            ad.show();
        }
    }

    private Integer createInvoice(final Integer customerId, final Integer invoiceType, final Integer factureSourceId) {
        final Integer newInvoiceId = nextInvoiceId();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Customer customer = realm.where(Customer.class).equalTo("id", customerId).findFirst();
                Invoice invoice = realm.createObject(Invoice.class, newInvoiceId);
                invoice.setCustomer(customer);
                invoice.setUser(((MainActivity) getActivity()).getUser());
                invoice.setType(invoiceType);
                invoice.setFk_facture_source(factureSourceId);
            }
        });
        return newInvoiceId;
    }
    private Integer nextInvoiceId() {
        if(null != realm.where(Invoice.class).findFirst()) {
            Integer nextId = realm.where(Invoice.class).max("id").intValue() + 1;
            return nextId;
        } else {
            return 1;
        }
    }


    public interface OnInvoicesExpandableListener {
        void goToInvoice(Invoice invoice);
        void createAvoir(Invoice invoice);
        void newInvoice();
    }
}
