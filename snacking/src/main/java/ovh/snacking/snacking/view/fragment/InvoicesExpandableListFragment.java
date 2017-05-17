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
    public static final String SECTION_LASTWEEK = "SectionLastWeek";
    public static final String SECTION_NINETY_DAYS = "SectionArchives";

    OnInvoicesExpandableListener mListener;
    private Realm realm;
    private SectionedRecyclerViewAdapter mAdapter;
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

    public SectionedRecyclerViewAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        realm = RealmSingleton.getInstance(getContext()).getRealm();

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        // Populate adapter
        if(mAdapter == null) {
            mAdapter = new SectionedRecyclerViewAdapter();
        }

        // Set adapter to recycler view
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

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

        // Refresh data into adapter
        populateAdapter();

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
            final String strLastWeek = dateFormat.format(new Date( (System.currentTimeMillis()/1000 - 7 * 60 * 60 * 24) * 1000) );
            final Date lastWeek = dateFormat.parse(strLastWeek);

            final String strYesterday = dateFormat.format(new Date( (System.currentTimeMillis()/1000 - 60 * 60 * 24) * 1000) );
            final Date yesterday = dateFormat.parse(strYesterday);

            return getInvoiceBetweenDates(lastWeek, yesterday);
        } catch (ParseException e) {
            Date now = new Date();
            return getInvoiceBetweenDates(now, now);
        }
    }

    private ArrayList<Invoice> getNinetyDaysInvoices() {
        try {
            final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            final String strLastWeek = dateFormat.format(new Date( (System.currentTimeMillis()/1000 - 7 * 60 * 60 * 24) * 1000) );
            final Date lastWeek = dateFormat.parse(strLastWeek);

            final String strNinetyDays = dateFormat.format(new Date( (System.currentTimeMillis()/1000 - 90 * 60 * 60 * 24) * 1000) );
            final Date ninetyDays = dateFormat.parse(strNinetyDays);

            return getInvoiceBetweenDates(lastWeek, ninetyDays);
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
        Section sectionEnCours = mAdapter.getSection(SECTION_ONGOING);
        if(sectionEnCours == null) {
            sectionEnCours = new ExpandableInvoicesSection(mAdapter, getString(R.string.invoices_ongoing), invoices, this, true);
            mAdapter.addSection(SECTION_ONGOING, sectionEnCours);
        } else {
            ((ExpandableInvoicesSection) sectionEnCours).setList(invoices);
        }

        // Invoice state = FINISHED
        invoices = getTodayTermineeInvoices();
        Section sectionFinished = mAdapter.getSection(SECTION_FINISHED);
        if(sectionFinished == null) {
            sectionFinished = new ExpandableInvoicesSection(mAdapter, getString(R.string.invoices_finished), invoices, this, false);
            mAdapter.addSection(SECTION_FINISHED, sectionFinished);
        } else {
            ((ExpandableInvoicesSection) sectionFinished).setList(invoices);
        }


        // Yesterday
        invoices = getYesterdayInvoices();
        Section sectionYesterday = mAdapter.getSection(SECTION_YESTERDAY);
        if(sectionYesterday == null) {
            sectionYesterday = new ExpandableInvoicesSection(mAdapter, getString(R.string.invoices_yesterday), invoices, this, false);
            mAdapter.addSection(SECTION_YESTERDAY, sectionYesterday);
        } else {
            ((ExpandableInvoicesSection) sectionYesterday).setList(invoices);
        }


        // Week
        invoices = getLastWeekInvoices();
        Section sectionLastWeek = mAdapter.getSection(SECTION_LASTWEEK);
        if(sectionLastWeek == null) {
            sectionLastWeek = new ExpandableInvoicesSection(mAdapter, getString(R.string.invoices_last_week), invoices, this, false);
            mAdapter.addSection(SECTION_LASTWEEK, sectionLastWeek);
        } else {
            ((ExpandableInvoicesSection) sectionLastWeek).setList(invoices);
        }
        ((ExpandableInvoicesSection) sectionLastWeek).setImgHeader(R.drawable.ic_calendar_black_24dp);

        // 90 days
        invoices = getNinetyDaysInvoices();
        Section sectionNinetyDays = mAdapter.getSection(SECTION_NINETY_DAYS);
        if(sectionNinetyDays == null) {
            sectionNinetyDays = new ExpandableInvoicesSection(mAdapter, getString(R.string.invoices_ninety_days), invoices, this, false);
            mAdapter.addSection(SECTION_NINETY_DAYS, sectionNinetyDays);
        } else {
            ((ExpandableInvoicesSection) sectionNinetyDays).setList(invoices);
        }
        ((ExpandableInvoicesSection) sectionNinetyDays).setImgHeader(R.drawable.ic_calendar_black_24dp);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onInvoiceSelected(Invoice invoice) {
        mListener.goToInvoice(invoice);
    }

    @Override
    public void onInvoiceLongClick(final Invoice invoice) {
        // If the invoice is FINISHED
        if (Invoice.FINISHED.equals(invoice.getState())) {
            displayDialogCreateAvoir(invoice);
        }

        // If the invoice is ONGOING
        else if (Invoice.ONGOING.equals(invoice.getState())){
            displayDialogRemoveInvoice(invoice);
        }
    }

    private void displayDialogCreateAvoir(final Invoice invoice) {
        if (Invoice.FACTURE.equals(invoice.getType())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_title_create_avoir)
                    .setMessage("Voulez-vous créer un avoir pour " + invoice.getCustomer().getName() + " (Facture n°" + invoice.getRef() + ") ?")
                    .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Integer invoiceId = createInvoice(invoice.getCustomer().getId(), Invoice.AVOIR, invoice.getId());
                            Invoice invoiceCreated = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();
                            populateAdapter();
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
    }

    private void displayDialogRemoveInvoice(final Invoice invoice) {
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
                        populateAdapter();
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

    private Integer createInvoice(final Integer customerId, final Integer invoiceType, final Integer factureSourceId) {
        final Integer newInvoiceId = RealmSingleton.getInstance(getContext()).nextInvoiceId();
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

    public interface OnInvoicesExpandableListener {
        void goToInvoice(Invoice invoice);
        void newInvoice();
    }
}
