package ovh.snacking.snacking.view.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintJob;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.realm.Realm;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.adapter.ExpandableInvoicesSection;
import ovh.snacking.snacking.controller.print.InvoicePrintDocumentAdapter;
import ovh.snacking.snacking.controller.service.DolibarrService;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.CustomerGroup;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.InvoiceChange;
import ovh.snacking.snacking.model.ProductGroup;
import ovh.snacking.snacking.model.User;
import ovh.snacking.snacking.model.Value;
import ovh.snacking.snacking.util.Constants;
import ovh.snacking.snacking.util.RealmSingleton;
import ovh.snacking.snacking.util.LibUtil;
import ovh.snacking.snacking.view.dialogFragment.CustomerSectionFragment;
import ovh.snacking.snacking.view.dialogFragment.DatePickerEndFragment;
import ovh.snacking.snacking.view.dialogFragment.DatePickerStartFragment;
import ovh.snacking.snacking.view.fragment.CustomerOfGroupFragment;
import ovh.snacking.snacking.view.fragment.EditingInvoiceFragment;
import ovh.snacking.snacking.view.fragment.GroupCustomerFragment;
import ovh.snacking.snacking.view.fragment.GroupProductFragment;
import ovh.snacking.snacking.view.fragment.InvoiceStatementFragment;
import ovh.snacking.snacking.view.fragment.InvoicesExpandableListFragment;
import ovh.snacking.snacking.view.fragment.PrintInvoiceFragment;
import ovh.snacking.snacking.view.fragment.PrintInvoiceStatementFragment;
import ovh.snacking.snacking.view.fragment.ProductOfGroupFragment;
import ovh.snacking.snacking.view.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity
        implements InvoicesExpandableListFragment.OnInvoicesExpandableListener,
        CustomerSectionFragment.CustomerSectionFragmentListener,
        EditingInvoiceFragment.OnEditInvoiceListener,
        PrintInvoiceFragment.OnPrintListener,
        PrintInvoiceStatementFragment.OnPrintInvoiceStatementListener,
        GroupCustomerFragment.OnGroupCustomerSelectedListener,
        GroupProductFragment.OnGroupProductSelectedListener,
        DatePickerStartFragment.OnDatePickerStartFragment,
        DatePickerEndFragment.OnDatePickerEndFragment,
        ExpandableInvoicesSection.ExpandableInvoicesSectionListener {

    public static final String TAG_CUSTOMER_SELECT = "ovh.snacking.snacking.view.dialogFragment.CustomerSectionFragment";
    public static final String TAG_EDITING_INVOICE = "ovh.snacking.snacking.view.fragment.EditingInvoiceFragment";
    //public static final String TAG_MANAGING_INVOICE = "ovh.snacking.snacking.view.ManagingInvoice";
    public static final String TAG_PRINT_INVOICE = "ovh.snacking.snacking.view.PrntInvoice";
    public static final String TAG_PRINT_INVOICE_STATEMENT = "ovh.snacking.snacking.view.fragment.PrintInvoiceStatementFragment";
    public static final String TAG_INVOICE_STATEMENT = "ovh.snacking.snacking.view.fragment.InvoiceStatementFragment";
    public static final String TAG_SETTINGS_FRAGMENT = "ovh.snacking.snacking.view.fragment.SettingsFragment";
    public static final String TAG_MANAGE_GROUP_CUSTOMER = "ovh.snacking.snacking.view.fragment.GroupCustomerFragment";
    public static final String TAG_CUSTOMER_OF_GROUP = "ovh.snacking.snacking.view.fragment.CustomerOfGroupFragment";
    public static final String TAG_MANAGE_GROUP_PRODUCT = "ovh.snacking.snacking.view.fragment.GroupProductFragment";
    public static final String TAG_PRODUCT_OF_GROUP = "ovh.snacking.snacking.view.fragment.ProductOfGroupFragment";
    public static final String TAG_INVOICES_EXPANDABLE_LIST = "ovh.snacking.snacking.view.fragment.InvoicesExpandableListFragment";

    private Realm realm;
    private User mUser;

    private FragmentManager fm;

    // Toolbar
    private Toolbar mToolbar;

    // Nav drawer
    private DrawerLayout mDrawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle mDrawerToggle;

    //Print
    private ArrayList<PrintJob> mPrintJobs;

    public User getUser() {
        return mUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = RealmSingleton.getInstance(getApplicationContext()).getRealm();

        mUser = realm.where(User.class).equalTo("isActive", true).findFirst();

        setContentView(R.layout.activity_main);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(navView);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        fm = getSupportFragmentManager();

        mPrintJobs = new ArrayList<>();

        // Set user name and app infos in the navigationview
        ((TextView) navView.getHeaderView(0).findViewById(R.id.nav_header_app_infos)).setText(getString(R.string.app_name) + " (" + getVersionInfo() + ")");
        ((TextView) navView.getHeaderView(0).findViewById(R.id.nav_header_user)).setText("user " + mUser.getName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO Ask for PIN

        checkSynchronisation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If no fragment is selected (start of the activity, we go on the first fragment
        if (getNavViewCheckedItem(navView) < 0)
            selectDrawerItem(navView.getMenu().findItem(R.id.nav_manage_invoice_fragment));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...
        switch (item.getItemId()) {
            /*case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                launchFragment(getFragment(TAG_SETTINGS_FRAGMENT), TAG_SETTINGS_FRAGMENT, true);
                return true;*/

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.app_bar_actions, menu);
        return true;
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // First uncheck all the navView menu items
        uncheckAllNavViewItems();

        switch(menuItem.getItemId()) {
            case R.id.nav_manage_invoice_fragment:
                launchFragment(getFragment(TAG_INVOICES_EXPANDABLE_LIST), TAG_INVOICES_EXPANDABLE_LIST, true);
                break;
            case R.id.nav_invoice_statement_fragment:
                launchFragment(getFragment(TAG_PRINT_INVOICE_STATEMENT), TAG_PRINT_INVOICE_STATEMENT, true);
                break;
            case R.id.nav_manage_group_customer_fragment:
                launchFragment(getFragment(TAG_MANAGE_GROUP_CUSTOMER), TAG_MANAGE_GROUP_CUSTOMER, true);
                break;
            case R.id.nav_manage_group_product_fragment:
                launchFragment(getFragment(TAG_MANAGE_GROUP_PRODUCT), TAG_MANAGE_GROUP_PRODUCT, true);
                break;
            case R.id.nav_settings:
                launchFragment(getFragment(TAG_SETTINGS_FRAGMENT), TAG_SETTINGS_FRAGMENT, true);
                break;
            default:
        }

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);

        // Set action bar title
        setTitle(menuItem.getTitle());

        // Close the navigation drawer
        mDrawerLayout.closeDrawers();
    }

    private int getMenuItemId(String TAG) {
        switch(TAG) {
            case TAG_INVOICES_EXPANDABLE_LIST:
                return R.id.nav_manage_invoice_fragment;
            case TAG_PRINT_INVOICE_STATEMENT:
                return R.id.nav_invoice_statement_fragment;
            case TAG_MANAGE_GROUP_CUSTOMER:
                return R.id.nav_manage_group_customer_fragment;
            case TAG_MANAGE_GROUP_PRODUCT:
                return R.id.nav_manage_group_product_fragment;
            case TAG_SETTINGS_FRAGMENT:
                return R.id.nav_settings;
            default:
                return R.id.nav_manage_invoice_fragment;
        }
    }

    private int getNavViewCheckedItem(NavigationView navigationView) {
        final Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isChecked()) {
                return i;
            }
        }
        return -1;
    }

    private void uncheckAllNavViewItems() {
        final Menu menu = navView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            item.setChecked(false);
        }
    }

    private Integer createInvoice(final Integer customerId, final Integer invoiceType, final Integer factureSourceId) {
        final Integer newInvoiceId = RealmSingleton.getInstance(MainActivity.this).nextInvoiceId();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Customer customer = realm.where(Customer.class).equalTo("id", customerId).findFirst();
                Invoice invoice = realm.createObject(Invoice.class, newInvoiceId);
                invoice.setCustomer(customer);
                invoice.setUser(mUser);
                invoice.setType(invoiceType);
                invoice.setFk_facture_source(factureSourceId);
            }
        });
        return newInvoiceId;
    }

    private Integer createInvoice(final Integer customerId, final Integer invoiceType) {
        return createInvoice(customerId, invoiceType, null);
    }

    /***********************************/
    /**  InvoiceExpandableList frag  **/
    /**********************************/
    @Override
    public void newInvoice() {
        launchFragment(getFragment(TAG_CUSTOMER_SELECT), TAG_CUSTOMER_SELECT, true);
    }

    @Override
    public void goToInvoice(Invoice invoice) {
        if (Invoice.ONGOING.equals(invoice.getState())) {
            EditingInvoiceFragment frag = (EditingInvoiceFragment) getFragment(MainActivity.TAG_EDITING_INVOICE);
            frag.setInvoiceId(invoice.getId());
            launchFragment(frag, TAG_EDITING_INVOICE, true);
        } else if (Invoice.FINISHED.equals(invoice.getState())) {
            PrintInvoiceFragment frag = (PrintInvoiceFragment) getFragment(MainActivity.TAG_PRINT_INVOICE);
            frag.setInvoiceId(invoice.getId());
            launchFragment(frag, TAG_PRINT_INVOICE, true);
        }
    }


    /**********************************/
    /**  Group Customer select part  **/
    /**********************************/
    @Override
    public void onGroupCustomerSelected(CustomerGroup group) {
        final CustomerOfGroupFragment frag = (CustomerOfGroupFragment) getFragment(MainActivity.TAG_CUSTOMER_OF_GROUP);
        frag.setCustomerGroup(group);
        launchFragment(frag, TAG_CUSTOMER_OF_GROUP, true);
    }


    /*********************************/
    /**  Group Product select part  **/
    /*********************************/
    @Override
    public void onGroupProductSelected(ProductGroup productGroup) {
        final ProductOfGroupFragment frag = (ProductOfGroupFragment) getFragment(MainActivity.TAG_PRODUCT_OF_GROUP);
        frag.setProductGroup(productGroup);
        launchFragment(frag, TAG_PRODUCT_OF_GROUP, true);
    }


    /****************************/
    /**  Customer select part  **/
    /****************************/
    @Override
    public void onCustomerSelected(final Integer customerId) {
        final EditingInvoiceFragment frag = (EditingInvoiceFragment) getFragment(MainActivity.TAG_EDITING_INVOICE);
        Integer invoiceId = createInvoice(customerId, Invoice.FACTURE);
        frag.setInvoiceId(invoiceId);
        launchFragment(frag, TAG_EDITING_INVOICE, true);
    }


    /****************************/
    /**  Editing invoice part  **/
    /****************************/
    @Override
    public void onShowInvoice(Integer invoiceId) {
        PrintInvoiceFragment frag = (PrintInvoiceFragment) getFragment(MainActivity.TAG_PRINT_INVOICE);
        frag.setInvoiceId(invoiceId);
        launchFragment(frag, TAG_PRINT_INVOICE, true);
    }

    /************************************/
    /**  Print invoice fragment part  **/
    /***********************************/

    @Override
    public boolean onFinishInvoice(Integer invoiceId) {
        final Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();
        boolean isFinished;

        // Finish the invoice
        if (invoice.getLines().size() > 0 && Invoice.ONGOING.equals(invoice.getState())) {
            finishInvoice(invoice);
            isFinished = true;
        } else if (invoice.getLines().size() > 0 && Invoice.FINISHED.equals(invoice.getState())) {
            isFinished = true;
        } else {
            Toast.makeText(getApplicationContext(), "Impossible de terminer, il n'y a aucun produits dans la facture", Toast.LENGTH_LONG).show();
            isFinished = false;
        }
        return isFinished;
    }

    @Override
    public void onChangeLastInvoice(View container_invoice, final Integer invoiceId) {
        final Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();

        // Keep a copy of the original invoice for safety
        // TODO a mettre dans un service/background thread
        final Date now = new Date();
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd-MM-yyyy-HH-mm");
        PdfDocument document = createPDFFromView(container_invoice);
        String filename = simpleDate.format(now) + "_" + invoice.getRef();
        final Uri uri = saveInvoiceAsPdf(document, filename);
        realm.executeTransaction(new Realm.Transaction() {
             @Override
             public void execute(Realm realm) {
                InvoiceChange invoiceChange = realm.createObject(InvoiceChange.class, nextInvoiceChangeId());
                 invoiceChange.setDate(now);
                 invoiceChange.setFk_invoice(invoiceId);
                 invoiceChange.setUri(uri.toString());
             }
        });

        /*realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                InvoiceChange invoiceBackup = realm.createObject(InvoiceChange.class, nextInvoiceChangeBackupId());
                invoiceBackup.setInvoice(invoice);

                for (Line line : invoice.getLines()) {
                    LineInvoiceChangeBackup lineBackup = realm.createObject(LineInvoiceChangeBackup.class, nextLineInvoiceChangeBackupId());
                    lineBackup.setProd(line.getProd().getRef());
                    lineBackup.setQty(line.getQty());
                    invoiceBackup.getLines().add(lineBackup);
                }
            }
        });*/

        EditingInvoiceFragment frag = (EditingInvoiceFragment) getFragment(MainActivity.TAG_EDITING_INVOICE);
        frag.setInvoiceId(invoiceId);
        launchFragment(frag, TAG_EDITING_INVOICE, true);
    }

    @Override
    public void onPrint(View container_invoice, Integer invoiceId) {
        final Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();

        // Todo virer car le print manager prend deja printershare, à tester
        if (isAppInstalled("com.dynamixsoftware.printershare") && isBluetoothEnabled()) {
            try {
                PdfDocument document = createPDFFromView(container_invoice);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "printershare_invoice.pdf");
                document.writeTo(new FileOutputStream(file));
                document.close();
                printFromPrinterShare(Uri.fromFile(file), "application/pdf");

            } catch (IOException e) {
                throw new RuntimeException("Error generating file", e);
            }
        } else {
            // Get a PrintManager instance
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

            // Set job name, which will be displayed in the print queue
            String jobName = getString(R.string.app_name) + " - Facture";

            // Start a print job, passing in a PrintDocumentAdapter implementation
            // to handle the generation of a print document
            PrintJob printJob = printManager.print(jobName, new InvoicePrintDocumentAdapter(this, container_invoice), null);
            // Save the job object for later status checking
            mPrintJobs.add(printJob);
            //cleanFragmentManager();
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                invoice.addCounterPrint();
            }
        });

        if (Invoice.FACTURE.equals(invoice.getType())) {
            setTitle("Facture terminée, imprimée (" + invoice.getCounterPrint() + ") fois");
        } else if (Invoice.AVOIR.equals(invoice.getType())) {
            setTitle("Avoir terminé, imprimée (" + invoice.getCounterPrint() + ") fois");
        }
    }


    /**********************************************/
    /**  Print invoice statement fragment part  **/
    /*********************************************/

    @Override
    public void onStartDateSet(int year, int month, int day) {
        PrintInvoiceStatementFragment frag = (PrintInvoiceStatementFragment)getFragment(TAG_PRINT_INVOICE_STATEMENT);
        frag.setStartDate(year, month, day);
    }

    @Override
    public void onEndDateSet(int year, int month, int day) {
        PrintInvoiceStatementFragment frag = (PrintInvoiceStatementFragment)getFragment(TAG_PRINT_INVOICE_STATEMENT);
        frag.setEndDate(year, month, day);
    }


    @Override
    public void onShowInvoiceStatement(Customer customer, Date startDate, Date endDate) {
        InvoiceStatementFragment frag = (InvoiceStatementFragment) fm.findFragmentByTag(TAG_INVOICE_STATEMENT);
        if (frag != null) {
            fm.beginTransaction()
                    .remove(frag)
                    .commit();
        }
        fm.executePendingTransactions();

        frag = new InvoiceStatementFragment();
        frag.setInformations(customer, startDate, endDate);

        if (!frag.isAdded()) {
            fm.beginTransaction()
                    .replace(R.id.invoice_statement, frag, TAG_INVOICE_STATEMENT)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    @Override
    public void onPrintInvoiceStatement(View invoice_statement) {
        // Todo virer car le print manager prend deja printershare, à tester
        if (isAppInstalled("com.dynamixsoftware.printershare") && isBluetoothEnabled()) {
            try {
                PdfDocument document = createPDFFromView(invoice_statement);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "printershare_invoice_statement.pdf");
                document.writeTo(new FileOutputStream(file));
                document.close();
                printFromPrinterShare(Uri.fromFile(file), "application/pdf");

            } catch (IOException e) {
                throw new RuntimeException("Error generating file", e);
            }
        } else {
            // Get a PrintManager instance
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

            // Set job name, which will be displayed in the print queue
            String jobName = getString(R.string.app_name) + " - Relevé de facture";

            // Start a print job, passing in a PrintDocumentAdapter implementation
            // to handle the generation of a print document
            PrintJob printJob = printManager.print(jobName, new InvoicePrintDocumentAdapter(this, invoice_statement), null);
            // Save the job object for later status checking
            mPrintJobs.add(printJob);
            //cleanFragmentManager();
        }
    }


    /********************/
    /**  PrinterShare  **/
    /********************/
    private void printFromPrinterShare(Uri data_uri, String data_type) {
        cleanFragmentManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage("com.dynamixsoftware.printershare");
        intent.setDataAndType(data_uri, data_type);
        startActivity(intent);
    }


    /***************/
    /**  Methods  **/
    /***************/
    @Override
    public void onBackPressed() {
        int index = fm.getBackStackEntryCount() - 1;

        if (index == 0) {
            fm.popBackStack();
        } else if(index > 0) {
            String tagCallingFrag = fm.getBackStackEntryAt(index).getName();
            String tagPreviousFrag = fm.getBackStackEntryAt(index - 1).getName();

            // Update navdrawer and title
            uncheckAllNavViewItems();
            MenuItem item = navView.getMenu().findItem(getMenuItemId(tagPreviousFrag));
            item.setChecked(true);
            setTitle(item.getTitle());

            // If the calling tag is printinvoicefragment, we shouldn't be able to edit again the invoice
            if(tagCallingFrag.equals(TAG_PRINT_INVOICE) && tagPreviousFrag.equals(TAG_EDITING_INVOICE)) {
                fm.popBackStack();
                fm.popBackStack();
            } else {
                fm.popBackStack();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void launchFragment(Fragment fragment, String tag, boolean addToBackStack) {
        fm.executePendingTransactions();
        if (!fragment.isAdded()) {
            if (fragment instanceof DialogFragment) {
                ((DialogFragment) fragment).show(fm, tag);
            } else {
                if (addToBackStack) {
                    fm.beginTransaction()
                            .replace(R.id.fragment_container, fragment, tag)
                            .addToBackStack(tag)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                } else {
                    fm.beginTransaction()
                            .replace(R.id.fragment_container, fragment, tag)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();
                }
            }
        }
    }

    private Fragment getFragment(String tag) {

        Fragment frag = fm.findFragmentByTag(tag);
        if (frag != null) {
            return frag;
        } else {
            switch (tag) {
                case TAG_CUSTOMER_SELECT:
                    frag = new CustomerSectionFragment();
                    break;
                case TAG_EDITING_INVOICE:
                    frag = new EditingInvoiceFragment();
                    break;
                case TAG_PRINT_INVOICE:
                    frag =  new PrintInvoiceFragment();
                    break;
                case TAG_SETTINGS_FRAGMENT:
                    frag =  new SettingsFragment();
                    break;
                case TAG_PRINT_INVOICE_STATEMENT:
                    frag =  new PrintInvoiceStatementFragment();
                    break;
                case TAG_INVOICE_STATEMENT:
                    frag =  new InvoiceStatementFragment();
                    break;
                case TAG_MANAGE_GROUP_CUSTOMER:
                    frag =  new GroupCustomerFragment();
                    break;
                case TAG_CUSTOMER_OF_GROUP:
                    frag =  new CustomerOfGroupFragment();
                    break;
                case TAG_MANAGE_GROUP_PRODUCT:
                    frag =  new GroupProductFragment();
                    break;
                case TAG_PRODUCT_OF_GROUP:
                    frag =  new ProductOfGroupFragment();
                    break;
                case TAG_INVOICES_EXPANDABLE_LIST:
                    frag = new InvoicesExpandableListFragment();
                    break;
                default:
                    //Log.e("SWITCH MainActivity", "No case");
            }
            return frag;
        }
    }

    private void cleanFragmentManager() {
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    private boolean isBluetoothEnabled() {
        boolean bluetoothEnabled;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothEnabled = mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled();
        return bluetoothEnabled;
    }

    private Integer nextInvoiceChangeId() {
        if(null != realm.where(InvoiceChange.class).findFirst()) {
            Integer nextId = realm.where(InvoiceChange.class).max("id").intValue() + 1;
            return nextId;
        } else {
            return 1;
        }
    }

    private void finishInvoice(final Invoice invoice) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                invoice.setState(Invoice.FINISHED);

                // Save the number into the database
                Integer next = nextNumber(invoice);
                invoice.setNumber(next);

                // Actual date
                invoice.setDate(new Date());

                // Update the new values of max facture and avoir into object Value
                realm.where(Value.class).findFirst().update(invoice);
            }
        });
    }

    private Integer nextNumber(Invoice invoice) {
        Value value = realm.where(Value.class).findFirst();
        Integer nextNumber;
        if (Invoice.AVOIR.equals(invoice.getType())) {
            nextNumber = value.getLastNumberAvoir() + 1;
        } else {
            nextNumber = value.getLastNumberFacture() + 1;
        }
        return nextNumber;
    }

    private String nextRef(Invoice invoice) {
        Integer max = nextNumber(invoice);
        return invoice.computeRef(max);
    }

    private PrintedPdfDocument createPDFFromView(View content) {
        PrintAttributes printAttrs = new PrintAttributes.Builder().
                setColorMode(PrintAttributes.COLOR_MODE_COLOR).
                setMediaSize(PrintAttributes.MediaSize.ISO_A5). //TODO A5
                setMinMargins(PrintAttributes.Margins.NO_MARGINS).
                build();

        // New doc
        PrintedPdfDocument document = new PrintedPdfDocument(getApplicationContext(), printAttrs);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(content.getWidth(), content.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Draw on the page
        content.draw(page.getCanvas());
        document.finishPage(page);

        return document;
    }

    private Uri saveInvoiceAsPdf(PdfDocument document, String filename) {
        try {
            File dir = getDir("ChangedInvoices", MODE_PRIVATE);
            File f = new File(dir, filename + ".pdf");
            FileOutputStream fos = new FileOutputStream(f);
            document.writeTo(fos);
            document.close();
            fos.close();
            return Uri.fromFile(f);

        } catch (IOException e) {
            throw new RuntimeException("Error generating file", e);
        }
    }

    private void checkSynchronisation() {
        // Check if an update is needed
        Date lastSyncDate;
        if (realm.where(Value.class).findFirst().getLastSync() == null) {
            lastSyncDate = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000); // if never synchronized, set lastSyncDate 7 days before today
        } else {
            lastSyncDate = realm.where(Value.class).findFirst().getLastSync();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        int days = sdf.format(new Date()).compareTo(sdf.format(lastSyncDate));

        if(days >= 1) {
            // Sync now if number of days >= 1
            Intent intent = new Intent(MainActivity.this, DolibarrService.class);
            intent.setAction(Constants.SYNC_DATA_WITH_DOLIBARR);
            startService(intent);

            // Check and set the alarm
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment.PREF_KEY_AUTO_SYNC, true)) {
                LibUtil.schedulePeriodicSync(getApplicationContext());
            } else {
                LibUtil.removePeriodicSync(getApplicationContext());
            }
        }
    }

    public String getVersionInfo() {
        String strVersion = "v";
        PackageInfo packageInfo;
        try {
            packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            strVersion += packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            strVersion += "unknown";
        }
        return strVersion;
    }

    /**
     * ExpandableInvoicesSection listener
     */
    @Override
    public void onInvoiceSelected(Invoice invoice) {
        goToInvoice(invoice);
    }

    @Override
    public void deleteInvoice(final Invoice invoice, ExpandableInvoicesSection section) {
        displayDialogDeleteInvoice(invoice, section);
    }

    @Override
    public void createAvoirFromFacture(Invoice invoice, SectionedRecyclerViewAdapter adapter) {
        displayDialogCreateAvoir(invoice, adapter);
    }

    private void displayDialogCreateAvoir(final Invoice invoice, final SectionedRecyclerViewAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.dialog_title_create_avoir)
                .setMessage(invoice.getCustomer().getName() + "\n" +
                        "(Facture n°" + invoice.getRef() + ")")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Integer invoiceId = createInvoice(invoice.getCustomer().getId(), Invoice.AVOIR, invoice.getId());
                        Invoice invoiceCreated = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();

                        // Notify and insert invoice into adapter
                        ((ExpandableInvoicesSection) adapter.getSection(InvoicesExpandableListFragment.SECTION_ONGOING)).insert(invoiceCreated);

                        goToInvoice(invoiceCreated);
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

    private void displayDialogDeleteInvoice(final Invoice invoice, final ExpandableInvoicesSection section) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Supprimer ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // First delete from adapter
                        section.remove(invoice);

                        // Then delete it from realm
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                invoice.deleteFromRealm();
                            }
                        });
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
