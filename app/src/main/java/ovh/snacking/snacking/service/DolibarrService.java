package ovh.snacking.snacking.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import ovh.snacking.snacking.BuildConfig;
import ovh.snacking.snacking.R;
import ovh.snacking.snacking.controller.APIDolibarr;
import ovh.snacking.snacking.controller.APIFileUpload;
import ovh.snacking.snacking.controller.Constants;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.DolibarrInvoice;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.Line;
import ovh.snacking.snacking.model.ProductCustomerPriceDolibarr;
import ovh.snacking.snacking.model.Product;
import ovh.snacking.snacking.model.User;
import ovh.snacking.snacking.model.Value;
import ovh.snacking.snacking.view.LoginActivity;
import ovh.snacking.snacking.view.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DolibarrService extends IntentService {

    public DolibarrService() {
        super("DolibarrService");
    }

    private Realm realm;
    private APIDolibarr dolibarr;
    private APIFileUpload retrofit;
    private User mUser;
    private static String mAPIKKey;
    private Intent mLocalIntent;

    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation() // Get only the fields with @Expose, allow to filter the fields from the dolibarr REST API
            .setDateFormat("yyyy-MM-dd") // To parse correctly the date with GSON. I have to use DATE_FORMAT(date, '%Y-%m-%d') dans api_invoice.class de dolibarr
            .create();

    private static OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
    private static Retrofit.Builder builder = new Retrofit.Builder();

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            realm = Realm.getDefaultInstance();
            if(initializeConnexion()) {
                if (Constants.GET_DATA_FROM_DOLIBARR.equals(action)) {
                    Integer res = updateData();
                    if (res == 0) {
                        sendBroadcastMessage(R.string.update_data_dolibarr_success);
                    } else if (res == -1) {
                        sendBroadcastMessage(R.string.update_data_dolibarr_fail_1);
                    }  else if (res == -5) {
                        sendBroadcastMessage(R.string.update_data_dolibarr_fail_5);
                    } else {
                        sendBroadcastMessage(R.string.update_data_dolibarr_fail);
                    }
                } else if (Constants.POST_INVOICE_TO_DOLIBARR.equals(action)) {
                    Integer res;
                    try {
                        res = postInvoices();
                    } catch (IOException e) {
                        e.printStackTrace();
                        res = -10;
                    }
                    if (res == 0) {
                        sendBroadcastMessage(R.string.post_invoices_dolibarr_success);
                    }
                } else if (Constants.LOGIN_TO_DOLIBARR.equals(action)) {
                    Intent intentMainActivity = new Intent(this, MainActivity.class);
                    intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentMainActivity);
                }
            } else {
                sendBroadcastMessage(R.string.service_dolibarr_error);
            }
            realm.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcastMessage(R.string.service_dolibarr_end);
    }

    private boolean initializeConnexion() {
        mUser = realm.where(User.class).equalTo("isActive", true).findFirst();
        if(null == mUser || mUser.getName().isEmpty() || mUser.getApiKey().isEmpty() || mUser.getServerURL().isEmpty()) {
            sendBroadcastMessage(R.string.login_dolibarr_credentials_empty);
            return false;
        }
        // HTTP interceptor for logging debug
        if (BuildConfig.DEBUG) {
            // enable logging for debug builds
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(loggingInterceptor);
        }

        mAPIKKey = mUser.getApiKey();

        // Connect to Dolibarr REST server
        builder.addConverterFactory(GsonConverterFactory.create())
                .baseUrl(mUser.getServerURL())
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .callFactory(httpClientBuilder.build());
        dolibarr = builder.build().create(APIDolibarr.class);

        // Upload file to server ...
        retrofit = new Retrofit.Builder()
                .baseUrl("")
                .build()
                .create(APIFileUpload.class);

        if(isAPIServerAvailable()) {
            sendBroadcastMessage(R.string.service_dolibarr_start);
            return true;
        } else {
            sendBroadcastMessage(R.string.login_dolibarr_fail);
            /*Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/
            stopSelf();
            return false;
        }
    }

    private boolean isAPIServerAvailable() {
        try {
            Call<JsonObject> call = dolibarr.getAPIServerStatus(mAPIKKey);
            JsonObject res = call.execute().body();
            if (null != res && res.has("success")) {
                return true;
            } else {
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendBroadcastMessage(int message) {
        mLocalIntent = new Intent(Constants.BROADCAST_MESSAGE_INTENT).putExtra(Constants.BROADCAST_MESSAGE_SEND, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(mLocalIntent);
    }

    public static void changeApiBaseUrl(String newApiBaseUrl) {

        builder.addConverterFactory(GsonConverterFactory.create())
                .baseUrl(newApiBaseUrl)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .callFactory(httpClientBuilder.build());
    }


    private void uploadFile(Uri fileUri) {
        File file = new File(fileUri.getPath());

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("pdf", file.getName(), requestFile);

        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), descriptionString);

        Call<ResponseBody> call = retrofit.upload(description, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.v("Upload", "success");
                //TODO supprime le file local
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    // GET informations from dolibarr REST server
    private Integer updateData() {
        try {
            //Prevent incoherence into the database, IMPORTANT
            // DO NOT DELETE ANYTHING BEFORE INVOICE POSTED
            if(realm.where(Invoice.class).equalTo("state", Invoice.TERMINEE).equalTo("isPOSTToDolibarr", false).findAll().size() > 0) {
                return -1;
            }

            // Create or update Products into Realm
            final List<Product> products = dolibarr.getAllProducts(mAPIKKey).execute().body();
            if (null != products && !products.isEmpty()) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(products);
                    }
                });
            } else {
                return -2;
            }

            // Create or update ProductsCustomerPrice into Realm
            final List<ProductCustomerPriceDolibarr> prices = dolibarr.getAllProductsCustomerPrice(mAPIKKey).execute().body();
            if (null != prices && !prices.isEmpty()) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(prices);
                    }
                });
            } else {
                return -3;
            }

            // Create or update Customers into Realm
            final List<Customer> customers = dolibarr.getAllCustomers(mAPIKKey).execute().body();
            if (null != customers && !customers.isEmpty()) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(customers);
                    }
                });
            } else {
                return -4;
            }

            // Create or update DolibarrInvoice into Realm
            if (!getDolibarrInvoices()) {
                return -5;
            }


        } catch (IOException e) {
            //Log.d("REALM", e.getMessage());
            return -6;
        }

        // Set the date of the last sync
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Value value = realm.where(Value.class).findFirst();
                if (null != value) {
                    value.setLastSync(new Date());
                } //else {
                  //  Log.d("REALM", "Value object doesn't exist");
                //}
            }
        });

        // If all is fine
        return 0;
    }

    private boolean getDolibarrInvoices() throws IOException {
        // Get the local last invoice id
        Integer localLastId;
        if (null != realm.where(DolibarrInvoice.class).max("id")) {
            localLastId = realm.where(DolibarrInvoice.class).max("id").intValue() - 1;
        } else {
            localLastId = 0;
        }

        // Create or update DolibarrInvoice into Realm
        final List<DolibarrInvoice> dolibarrInvoices = dolibarr.getInvoices(localLastId, mAPIKKey).execute().body();
        if (null != dolibarrInvoices && !dolibarrInvoices.isEmpty()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(dolibarrInvoices);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    private Integer postInvoices() throws IOException {
        // TODO Post backup invoices en async avec APIRetrofit
        /* RealmResults<InvoiceChange> invoicesBackup = realm.where(InvoiceChange.class).findAll();
        for (final InvoiceChange invvoiceBackup : invoicesBackup) {
            Call<JsonPrimitive> call = dolibarr.postInvoiceBackup(convertInvoiceBackup2Json(invvoiceBackup));
            JsonPrimitive req = call.execute().body();
            if (req == null) {
                return -1;
            }
            // Return the id of the invoice from the response of the Dolibarr REST server
            Boolean bool = req.getAsBoolean();
        }*/



        // First, post the avoirs and their corresponding facture
        RealmResults<Invoice> avoirs = realm.where(Invoice.class).equalTo("state", Invoice.TERMINEE).equalTo("isPOSTToDolibarr", false).equalTo("type", Invoice.AVOIR).findAll();
        for (final Invoice avoir : avoirs) {

            // Get the corresponding facture
            Invoice facture_source = realm.where(Invoice.class).equalTo("id", avoir.getFk_facture_source()).findFirst();
            final Integer fk_facture_source = postInvoice(facture_source);

            if (fk_facture_source != 0) {

                // Validate the previous facture
                if (validateInvoice(fk_facture_source, facture_source)) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            //avoir.setFk_facture_source(fk_facture_source);
                            avoir.setFk_facture_source_dolibarr(fk_facture_source);
                        }
                    });

                    // POST the avoir
                    Integer fk_avoir = postInvoice(avoir);
                    if (fk_avoir != 0) {
                        // Validate the avoir
                        if (!validateInvoice(fk_avoir, avoir)) {
                            return -4;
                        }
                    } else {
                        return -3;
                    }
                } else {
                    return -2;
                }
            } else {
                return -1;
            }
        }


        // Then post the rest of the factures
        RealmResults<Invoice> invoices = realm.where(Invoice.class).equalTo("state", Invoice.TERMINEE).equalTo("isPOSTToDolibarr", false).findAll();
        for (final Invoice facture : invoices) {
            if (Invoice.TERMINEE.equals(facture.getState()) && !facture.isPOSTToDolibarr() && Invoice.FACTURE.equals(facture.getType())) {
                Integer fk_facture = postInvoice(facture);
                if (fk_facture != 0) {
                    if (!validateInvoice(fk_facture, facture)) {
                        return -6;
                    }
                } else {
                    return -5;
                }
            }
        }

        // If all is fine
        return 0;
    }

    private Integer postInvoice(Invoice invoice) throws IOException {
        Call<JsonPrimitive> call = dolibarr.createInvoiceFromJson(mAPIKKey, convertInvoice2Json(invoice));
        JsonPrimitive req = call.execute().body();
        if (req == null) {
            return -1;
        }
        // Return the id of the invoice from the response of the Dolibarr REST server
        return req.getAsInt();
    }

    private Boolean validateInvoice(final Integer fk_invoice_dolibarr, final Invoice invoice) throws IOException {
        Call<JsonPrimitive> call = dolibarr.validateInvoice(fk_invoice_dolibarr, mAPIKKey);
        JsonPrimitive req = call.execute().body();
        if (req == null) {
            return false;
        }
        if (req.getAsBoolean()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    invoice.setIsPOSTToDolibarr(true);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    private JsonObject convertInvoice2Json(Invoice invoice) {

        /****************************************/
        /****          INVOICE PART        ******/
        /****************************************/
        JsonObject obj = new JsonObject();
        try {
            // TODO Important, it's to say to www/dolibarr/htdocs/compta/facture/class/facture.class.php that we came from tablet
            //TODO mais je vais le virer après, quand on passera directement des objets InvoiceLignes pour avoir un fichier facture propre sans modif de ma part
            obj.addProperty("fromTablet", 1);
            obj.addProperty("socid", invoice.getCustomer().getId());
            Long date = invoice.getDate().getTime() / 1000;
            obj.addProperty("date", date.intValue());
            if (Invoice.FACTURE.equals(invoice.getType())) {
                obj.addProperty("type", 0); // 0=Facture de doit, 2=Facture avoir
            } else if (Invoice.AVOIR.equals(invoice.getType()) && invoice.getFk_facture_source_dolibarr() != null) {
                obj.addProperty("type", 2);  // 0=Facture de doit, 2=Facture avoir
                obj.addProperty("fk_facture_source", invoice.getFk_facture_source_dolibarr());
            }
            obj.addProperty("cond_reglement_id", 1);            // 1=A reception
            obj.addProperty("note_private", "Tablette " + invoice.getUser().getName() + ", ref " + invoice.getRef());
            obj.addProperty("ref_client", invoice.getRef());
            obj.addProperty("modelpdf", "crabe");


            /****************************************/
            /****       INVOICE LINES PART     ******/
            /****************************************/
            JsonArray array = new JsonArray();
            int rang = 1;
            // Lines of the invoice
            for (Line line : invoice.getLines()) {

                JsonObject lineJson = new JsonObject();
                lineJson.addProperty("fk_product", line.getProd().getId());
                lineJson.addProperty("product_type", line.getProd().getType());

                // Handle negative price
                Integer subprice = line.getSubprice();
                Integer total_ht = line.getTotal_ht();
                Integer total_tss = line.getTotal_tva();
                Integer total_ttc = line.getTotal_ttc();
                if (Invoice.AVOIR.equals(invoice.getType())) {
                    subprice = -subprice;
                    total_ht = -total_ht;
                    total_tss = -total_tss;
                    total_ttc = -total_ttc;
                }
                lineJson.addProperty("subprice", subprice);
                lineJson.addProperty("total_ht", total_ht);
                lineJson.addProperty("total_tva", total_tss);
                lineJson.addProperty("total_ttc", total_ttc);

                lineJson.addProperty("qty", line.getQty());
                lineJson.addProperty("tva_tx", line.getProd().getTva_tx());
                lineJson.addProperty("rang", rang);
                array.add(lineJson);

                rang++;
            }
            //Log.d("ARRAY", array.toString());
            obj.add("lines", array);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return obj;
    }

    //private JsonObject convertInvoiceBackup2Json(InvoiceChange invoice) {

        /****************************************/
        /****          INVOICE PART        ******/
        /****************************************/
        /*JsonObject obj = new JsonObject();
        try {
            obj.addProperty("user", invoice.getCustomer());
            Long date = invoice.getDate().getTime() / 1000;
            obj.addProperty("date", date.intValue());
            obj.addProperty("ref", invoice.getRef());
            obj.addProperty("customer", invoice.getCustomer());*/

            /****************************************/
            /****       INVOICE LINES PART     ******/
            /****************************************/
            /*JsonArray array = new JsonArray();
            for (LineInvoiceChangeBackup line : invoice.getLines()) {

                JsonObject lineJson = new JsonObject();
                lineJson.addProperty("qty", line.getProd());
                lineJson.addProperty("prod", line.getQty());
            }
            obj.add("lines", array);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return obj;*/
    //}
}






    /*private boolean computeAllCustomerFullInfo() {
        final RealmResults<Customer> customers = realm.where(Customer.class).findAll();

        if(!customer.isEmpty()) {
            RealmResults<Customer> custFullBefore = realm.where(Customer.class).equalTo("id", 31).findAll();
            for (Customer c : custFullBefore ) {
                if (c != null) {
                    RealmResults<Product> PRODUCT_Dolibarr_BEFORE = c.getProducts().sort("ref");
                    for (Product p : PRODUCT_Dolibarr_BEFORE) {
                        Log.d("PRODUCT_Dolibarr_BEFORE", p.getRef() + " " + p.getPrice());
                    }
                }
            }

            // Create or update Customers into Realm
            for (final Customer customer : customers) {
                createOrUpdateCustomerFullInfo(customer);
            }
            RealmResults<Customer> custFullAfter = realm.where(Customer.class).equalTo("id", 31).findAll();
            for (Customer c : custFullAfter ) {
                if (c != null) {
                    RealmResults<Product> PRODUCT_Dolibarr_AFTER = c.getProducts().sort("ref");
                    for (Product p : PRODUCT_Dolibarr_AFTER) {
                        Log.d("PRODUCT_Dolibarr_AFTER", p.getRef() + " " + p.getPrice());
                    }
                }
            }
            return true;

        } else {
            Log.d("REALM", "customerDolibarrs from Dolibarr are empty");
            return false;
        }
    }*/

    /*private void createOrUpdateCustomerFullInfo(final Customer customerDolibarr) {
        final RealmResults<Product> products = realm.where(Product.class).findAll();

        if(!products.isEmpty()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (customerDolibarr.getId() == 31) {) {
                        Log.d("TOMO", "Complexe");
                    }

                    Customer customer = realm.where(Customer.class).equalTo("id", customerDolibarr.getId()).findFirst();
                    if (customer == null) {
                        customer = realm.createObject(Customer.class, customerDolibarr.getId());
                    }

                    // Update the fields
                    customer.setName(customerDolibarr.getName());
                    customer.setAddress(customerDolibarr.getAddress());
                    customer.setZip(customerDolibarr.getZip());
                    customer.setTown(customerDolibarr.getTown());
                    customer.setPhone(customerDolibarr.getPhone());

                    // Set products with default and custom price
                    RealmResults<ProductCustomerPriceDolibarr> prices = realm.where(ProductCustomerPriceDolibarr.class).equalTo("fk_soc", customer.getId()).findAll();

                    for (Product prod : products) {
                        // Log.d("PRODUCT", prod.getRef() + " " + prod.getPrice());

                        // Set custom price if exist
                        if (!prices.isEmpty()) {
                            ProductCustomerPriceDolibarr prodPrice = prices.where().equalTo("fk_product", prod.getId()).findFirst();
                            if (null != prodPrice) {
                                Product newProd = new Product(prod);

                                //                    Log.d("PRICES", prodPrice.getId() + " " + prodPrice.getFk_product() + " " + prodPrice.getFk_soc() + " " + prodPrice.getPrice());
                                prod.setPrice(prodPrice.getPrice());
                                prod.setTva_tx(prodPrice.getTva_tx());
                                prod.setPrice_ttc(prodPrice.getPrice_ttc());
                            }
                        } else {
                            //                Log.d("ProductCustomerPriceDolibarr", "Empty");
                        }
                        Log.d("PRICES", prod.getRef() + " " + prod.getPrice());
                        customerFull.getProductDolibarrs().add(prod);
                    }
                }
            });
        } else {
            Log.d("REALM", "products from Dolibarr are empty");
        }

        realm.copyToRealmOrUpdate(customerFullInfo);
    }
}*/
