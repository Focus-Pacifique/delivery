package com.focus.delivery.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.focus.delivery.R;
import com.focus.delivery.adapter.SectionExpandableInvoice;
import com.focus.delivery.interfaces.FilterableList;
import com.focus.delivery.model.Invoice;
import com.focus.delivery.util.RealmSingleton;
import com.focus.delivery.view.activity.MainActivity;

/**
 * Created by Alex on 11/11/2016.
 *
 * Fragment to display invoices by date
 */

public class InvoicesExpandableListFragment extends android.support.v4.app.Fragment implements
        SearchView.OnQueryTextListener {

    public static final String SECTION_ONGOING      = "En cours";
    public static final String SECTION_FINISHED     = "Terminées";
    public static final String SECTION_YESTERDAY    = "Hier";
    public static final String SECTION_LASTWEEK     = "Semaine dernière";
    public static final String SECTION_NINETY_DAYS  = "90 jours";

    OnInvoicesExpandableListener mListener;
    private Realm realm;
    private SectionedRecyclerViewAdapter mAdapter;
    private FloatingActionButton fab;
    private SearchView mSearchView;                 // SearchView
    private String mQuery = "";                     // Search query

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
        // Show the menu
        setHasOptionsMenu(true);

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

        fab.setImageResource(R.drawable.ic_create_white_24dp);
        fab.show();
    }

    /**
     *  Menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        getActivity().getMenuInflater().inflate(R.menu.menu_search, menu);

        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Configure the search info and add any event listeners
        mSearchView.setQueryHint(getString(R.string.hint_search_customer));
        mSearchView.setOnQueryTextListener(this);

        // Set the old search query
        mSearchView.setQuery(mQuery, false);

        // Display the search icon or the query if it is not empty
        if (mQuery.isEmpty()) {
            mSearchView.setIconified(true);
        } else
            mSearchView.setIconified(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        fab.hide();

        // To hide the soft keyboard
        mSearchView.clearFocus();
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
            sectionEnCours = new SectionExpandableInvoice(mAdapter, SECTION_ONGOING, invoices, this, true);
            mAdapter.addSection(SECTION_ONGOING, sectionEnCours);
        } else {
            ((SectionExpandableInvoice) sectionEnCours).setList(invoices);
        }

        // Invoice state = FINISHED
        invoices = getTodayTermineeInvoices();
        Section sectionFinished = mAdapter.getSection(SECTION_FINISHED);
        if(sectionFinished == null) {
            sectionFinished = new SectionExpandableInvoice(mAdapter, SECTION_FINISHED, invoices, this, false);
            mAdapter.addSection(SECTION_FINISHED, sectionFinished);
        } else {
            ((SectionExpandableInvoice) sectionFinished).setList(invoices);
        }


        // Yesterday
        invoices = getYesterdayInvoices();
        Section sectionYesterday = mAdapter.getSection(SECTION_YESTERDAY);
        if(sectionYesterday == null) {
            sectionYesterday = new SectionExpandableInvoice(mAdapter, SECTION_YESTERDAY, invoices, this, false);
            mAdapter.addSection(SECTION_YESTERDAY, sectionYesterday);
        } else {
            ((SectionExpandableInvoice) sectionYesterday).setList(invoices);
        }


        // Week
        invoices = getLastWeekInvoices();
        Section sectionLastWeek = mAdapter.getSection(SECTION_LASTWEEK);
        if(sectionLastWeek == null) {
            sectionLastWeek = new SectionExpandableInvoice(mAdapter, SECTION_LASTWEEK, invoices, this, false);
            mAdapter.addSection(SECTION_LASTWEEK, sectionLastWeek);
        } else {
            ((SectionExpandableInvoice) sectionLastWeek).setList(invoices);
        }
        ((SectionExpandableInvoice) sectionLastWeek).setImgHeader(R.drawable.ic_calendar_black_24dp);

        // 90 days
        invoices = getNinetyDaysInvoices();
        Section sectionNinetyDays = mAdapter.getSection(SECTION_NINETY_DAYS);
        if(sectionNinetyDays == null) {
            sectionNinetyDays = new SectionExpandableInvoice(mAdapter, SECTION_NINETY_DAYS, invoices, this, false);
            mAdapter.addSection(SECTION_NINETY_DAYS, sectionNinetyDays);
        } else {
            ((SectionExpandableInvoice) sectionNinetyDays).setList(invoices);
        }
        ((SectionExpandableInvoice) sectionNinetyDays).setImgHeader(R.drawable.ic_calendar_black_24dp);

        mAdapter.notifyDataSetChanged();
    }

    public interface OnInvoicesExpandableListener {
        void goToInvoice(Invoice invoice);
        void newInvoice();
    }

    /**
     * Listen for events on SearchView item. Called in 'onCreateOptionsMenu()'.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    @Override
    public boolean onQueryTextChange(String query) {
        mQuery = query;
        for (Section section : mAdapter.getSectionsMap().values()) {
            if (section instanceof FilterableList) {
                ((FilterableList) section).filter(query);
            }
        }
        mAdapter.notifyDataSetChanged();
        return true;
    }
}
