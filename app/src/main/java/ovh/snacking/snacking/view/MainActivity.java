package ovh.snacking.snacking.view;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintJob;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.Constants;
import ovh.snacking.snacking.controller.InvoicePrintDocumentAdapter;
import ovh.snacking.snacking.controller.RealmSingleton;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.CustomerGroup;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.InvoiceChange;
import ovh.snacking.snacking.model.ProductGroup;
import ovh.snacking.snacking.model.User;
import ovh.snacking.snacking.model.Value;
import ovh.snacking.snacking.service.DolibarrService;
import ovh.snacking.snacking.util.SyncUtils;

public class MainActivity extends AppCompatActivity
        implements ManagingInvoiceFragment.OnInvoiceListener,
        CustomerSectionFragment.OnCustomerDialogListener,
        EditingInvoiceFragment.OnEditInvoiceListener,
        PrintInvoiceFragment.OnPrintListener,
        PrintInvoiceStatementFragment.OnPrintInvoiceStatementListener,
        GroupCustomerFragment.OnGroupCustomerSelectedListener,
        GroupProductFragment.OnGroupProductSelectedListener,
        DatePickerStartFragment.OnDatePickerStartFragment,
        DatePickerEndFragment.OnDatePickerEndFragment {

    public static final String TAG_CUSTOMER_SELECT = "ovh.snacking.snacking.view.CustomerSectionFragment";
    public static final String TAG_EDITING_INVOICE = "ovh.snacking.snacking.view.EditingInvoiceFragment";
    public static final String TAG_MANAGING_INVOICE = "ovh.snacking.snacking.view.ManagingInvoice";
    public static final String TAG_PRINT_INVOICE = "ovh.snacking.snacking.view.PrntInvoice";
    public static final String TAG_PRINT_INVOICE_STATEMENT = "ovh.snacking.snacking.view.PrintInvoiceStatementFragment";
    public static final String TAG_INVOICE_STATEMENT = "ovh.snacking.snacking.view.InvoiceStatementFragment";
    public static final String TAG_SETTINGS_FRAGMENT = "ovh.snacking.snacking.view.SettingsFragment";
    public static final String TAG_MANAGE_GROUP_CUSTOMER = "ovh.snacking.snacking.view.GroupCustomerFragment";
    public static final String TAG_CUSTOMER_OF_GROUP = "ovh.snacking.snacking.view.CustomerOfGroupFragment";
    public static final String TAG_MANAGE_GROUP_PRODUCT = "ovh.snacking.snacking.view.GroupProductFragment";
    public static final String TAG_PRODUCT_OF_GROUP = "ovh.snacking.snacking.view.ProductOfGroupFragment";

    private Realm realm;
    private User mUser;

    private FragmentManager fm;
    private DrawerLayout mDrawerLayout;
    private NavigationView nvDrawer;
    private ArrayList<PrintJob> mPrintJobs;

    private void setInitialValues() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // Set default value for group of products
                if (null == realm.where(ProductGroup.class).findFirst()) {
                    ProductGroup group1 = realm.createObject(ProductGroup.class, nextProductGroupId());
                    group1.setName("Groupe 1");
                    group1.setPosition(1);

                    ProductGroup group2 = realm.createObject(ProductGroup.class, nextProductGroupId());
                    group2.setName("Groupe 2");
                    group2.setPosition(2);
                }

                if (null == realm.where(CustomerGroup.class).findFirst()) {
                    CustomerGroup group = realm.createObject(CustomerGroup.class, nextCustomerGroupId());
                    group.setName("Favoris");
                    group.setPosition(1);
                }
            }
        });
    }

    private boolean isUserSignedIn() {
        // If the user already exist
        return null != realm.where(User.class).equalTo("isActive", true).findFirst();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = RealmSingleton.getInstance(getApplicationContext()).getRealm();

        if (!isUserSignedIn()) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            mUser = realm.where(User.class).equalTo("isActive", true).findFirst();
            setInitialValues();
        }

        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.left_drawer);
        setupDrawerContent(nvDrawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fm = getSupportFragmentManager();
        mPrintJobs = new ArrayList<>();

        Fragment frag = getFragment(TAG_MANAGING_INVOICE);
        if (!frag.isAdded()) {
            fm.beginTransaction()
                    .add(R.id.fragment_container, frag, TAG_MANAGING_INVOICE)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO Ask for PIN

        checkSynchronisation();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                //Intent intent = new Intent(getApplicationContext(), SynchronizationActivity.class);
                /*Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);*/
                launchFragment(getFragment(TAG_SETTINGS_FRAGMENT), TAG_SETTINGS_FRAGMENT, true);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void setupDrawerContent(NavigationView navigationView) {
        /*TextView user = ((TextView) findViewById(R.id.nav_header_user));
        user.setText(mUser.getName());*/
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

        //cleanFragmentManager();

        switch(menuItem.getItemId()) {
            case R.id.nav_invoice_statement_fragment:
                launchFragment(getFragment(TAG_PRINT_INVOICE_STATEMENT), TAG_PRINT_INVOICE_STATEMENT, true);
                break;
            case R.id.nav_manage_group_customer_fragment:
                launchFragment(getFragment(TAG_MANAGE_GROUP_CUSTOMER), TAG_MANAGE_GROUP_CUSTOMER, true);
                break;
            case R.id.nav_manageing_invoice_fragment:
                launchFragment(getFragment(TAG_MANAGING_INVOICE), TAG_MANAGING_INVOICE, true);
                break;
            case R.id.nav_manage_group_product_fragment:
                launchFragment(getFragment(TAG_MANAGE_GROUP_PRODUCT), TAG_MANAGE_GROUP_PRODUCT, true);
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

    private Integer createInvoice(final Integer customerId, final Integer invoiceType, final Integer factureSourceId) {
        final Integer newInvoiceId = nextInvoiceId();
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

    /*****************************/
    /**  Managing invoice frag  **/
    /*****************************/
    @Override
    public void onNewInvoice() {
        launchFragment(getFragment(TAG_CUSTOMER_SELECT), TAG_CUSTOMER_SELECT, true);
    }

    @Override
    public void onInvoiceSelected(Integer invoiceId) {
        Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();

        if (Invoice.EN_COURS.equals(invoice.getState())) {
            EditingInvoiceFragment frag = (EditingInvoiceFragment) getFragment(MainActivity.TAG_EDITING_INVOICE);
            frag.setInvoiceId(invoiceId);
            setTitle(invoice.getCustomer().getName());
            launchFragment(frag, TAG_EDITING_INVOICE, true);

        } else if (Invoice.TERMINEE.equals(invoice.getState())) {
            PrintInvoiceFragment frag = (PrintInvoiceFragment) getFragment(MainActivity.TAG_PRINT_INVOICE);
            frag.invoiceId = invoiceId;

            if (Invoice.FACTURE.equals(invoice.getType())) {
                setTitle("Facture terminée, imprimée (" + invoice.getCounterPrint() + ") fois");
            } else if (Invoice.AVOIR.equals(invoice.getType())) {
                setTitle("Avoir terminé, imprimée (" + invoice.getCounterPrint() + ") fois");
            }
            launchFragment(frag, TAG_PRINT_INVOICE, true);
        }
    }

    @Override
    public void onInvoiceLongClick(Integer invoiceId) {
        final Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();

        if (Invoice.TERMINEE.equals(invoice.getState())) {
            if (Invoice.FACTURE.equals(invoice.getType())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_title_create_avoir)
                        .setMessage("Voulez-vous créer un avoir pour " + invoice.getCustomer().getName() + " (Facture n°" + invoice.getRef() + ") ?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final EditingInvoiceFragment frag = (EditingInvoiceFragment) getFragment(MainActivity.TAG_EDITING_INVOICE);
                                Integer invoiceId = createInvoice(invoice.getCustomer().getId(), Invoice.AVOIR, invoice.getId());
                                setTitle("Nouvel AVOIR : " + invoice.getCustomer().getName());
                                frag.setInvoiceId(invoiceId);
                                launchFragment(frag, TAG_EDITING_INVOICE, true);

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
                Toast.makeText(getApplicationContext(), "Impossible de créer un avoir sur un avoir", Toast.LENGTH_LONG).show();
            }
        } else if (Invoice.EN_COURS.equals(invoice.getState())){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

    /**********************************/
    /**  Group Customer select part  **/
    /**********************************/
    @Override
    public void onGroupCustomerSelected(CustomerGroup group) {
        final CustomerOfGroupFragment frag = (CustomerOfGroupFragment) getFragment(MainActivity.TAG_CUSTOMER_OF_GROUP);
        setTitle("CustomerOfGroup " + group.getName());
        frag.setCustomerGroup(group);
        launchFragment(frag, TAG_CUSTOMER_OF_GROUP, true);
    }


    /*********************************/
    /**  Group Product select part  **/
    /*********************************/
    @Override
    public void onGroupProductSelected(ProductGroup productGroup) {
        final ProductOfGroupFragment frag = (ProductOfGroupFragment) getFragment(MainActivity.TAG_PRODUCT_OF_GROUP);
        setTitle("ProductOfGroup " + productGroup.getName());
        frag.setProductGroup(productGroup);
        launchFragment(frag, TAG_PRODUCT_OF_GROUP, true);
    }


    /****************************/
    /**  Customer select part  **/
    /****************************/
    @Override
    public void onCustomerSelected(final Integer customerId) {
        final EditingInvoiceFragment frag = (EditingInvoiceFragment) getFragment(MainActivity.TAG_EDITING_INVOICE);
        Customer customer = realm.where(Customer.class).equalTo("id", customerId).findFirst();
        Integer invoiceId = createInvoice(customerId, Invoice.FACTURE);
        setTitle("Nouvelle FACTURE : " + customer.getName());
        frag.setInvoiceId(invoiceId);
        launchFragment(frag, TAG_EDITING_INVOICE, false);
    }


    /****************************/
    /**  Editing invoice part  **/
    /****************************/
    @Override
    public void onEditInvoiceBack() {
        launchFragment(getFragment(TAG_MANAGING_INVOICE), TAG_MANAGING_INVOICE, false);
    }

    @Override
    public void onShowInvoice(Integer invoiceId) {

        PrintInvoiceFragment frag = (PrintInvoiceFragment) getFragment(MainActivity.TAG_PRINT_INVOICE);
        frag.invoiceId = invoiceId;
        Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();
        if (null == invoice) {
            setTitle("Invoice null");
        } else if (Invoice.FACTURE.equals(invoice.getType())) {
            setTitle("Facture à imprimer");
        } else if (Invoice.AVOIR.equals(invoice.getType())) {
            setTitle("Avoir à imprimer");
        } else {
            setTitle(R.string.title_print);
        }
        launchFragment(frag, TAG_PRINT_INVOICE, true);
    }

    /************************************/
    /**  Print invoice fragment part  **/
    /***********************************/
    @Override
    public void onPrintBack() {
        launchFragment(getFragment(TAG_MANAGING_INVOICE), TAG_MANAGING_INVOICE, false);
    }

    @Override
    public boolean onFinishInvoice(Integer invoiceId) {
        final Invoice invoice = realm.where(Invoice.class).equalTo("id", invoiceId).findFirst();
        boolean isFinished;

        // Finish the invoice
        if (invoice.getLines().size() > 0 && Invoice.EN_COURS.equals(invoice.getState())) {
            finishInvoice(invoice);
            isFinished = true;
        } else if (invoice.getLines().size() > 0 && Invoice.TERMINEE.equals(invoice.getState())) {
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
        setTitle(invoice.getCustomer().getName());
        launchFragment(frag, TAG_EDITING_INVOICE, false);
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
    public void onBackButtonPressed() {
        onBackPressed();
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
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void launchFragment(Fragment fragment, String tag, boolean addToBackStack) {
        fm.executePendingTransactions();
        if (!fragment.isAdded()) {
            if (addToBackStack) {
                fm.beginTransaction()
                        .replace(R.id.fragment_container, fragment, tag)
                        .addToBackStack(null)
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
                case TAG_MANAGING_INVOICE:
                    frag =  new ManagingInvoiceFragment();
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
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            bluetoothEnabled = false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable
                bluetoothEnabled = false;
            } else {
                bluetoothEnabled = true;
            }
        }
        return bluetoothEnabled;
    }

    private Integer nextInvoiceId() {
        if(null != realm.where(Invoice.class).findFirst()) {
            Integer nextId = realm.where(Invoice.class).max("id").intValue() + 1;
            return nextId;
        } else {
            return 1;
        }
    }

    private Integer nextInvoiceChangeId() {
        if(null != realm.where(InvoiceChange.class).findFirst()) {
            Integer nextId = realm.where(InvoiceChange.class).max("id").intValue() + 1;
            return nextId;
        } else {
            return 1;
        }
    }

    private Integer nextProductGroupId() {
        if(null != realm.where(ProductGroup.class).findFirst()) {
            Integer nextId = realm.where(ProductGroup.class).max("id").intValue() + 1;
            return nextId;
        } else {
            return 1;
        }
    }

    private Integer nextCustomerGroupId() {
        if(null != realm.where(CustomerGroup.class).findFirst()) {
            return realm.where(CustomerGroup.class).max("id").intValue() + 1;
        } else {
            return 1;
        }
    }

    private void finishInvoice(final Invoice invoice) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                invoice.setState(Invoice.TERMINEE);

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
        Long diff = System.currentTimeMillis() - realm.where(Value.class).findFirst().getLastSync().getTime();
        Long days = TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS);

        if(days >= 1) {
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment.PREF_KEY_AUTO_SYNC, true)) {
                SyncUtils.schedulePeriodicSync(getApplicationContext());
            } else {
                SyncUtils.removePeriodicSync(getApplicationContext());
            }
        }
    }
}
