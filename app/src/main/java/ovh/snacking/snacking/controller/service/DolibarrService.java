package ovh.snacking.snacking.controller.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
import ovh.snacking.snacking.controller.API.APIDolibarr;
import ovh.snacking.snacking.controller.API.APIFileUpload;
import ovh.snacking.snacking.util.Constants;
import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.CustomerAndGroupBinding;
import ovh.snacking.snacking.model.DolibarrInvoice;
import ovh.snacking.snacking.model.Invoice;
import ovh.snacking.snacking.model.Line;
import ovh.snacking.snacking.model.ProductAndGroupBinding;
import ovh.snacking.snacking.model.ProductCustomerPriceDolibarr;
import ovh.snacking.snacking.model.Product;
import ovh.snacking.snacking.model.User;
import ovh.snacking.snacking.model.Value;
import ovh.snacking.snacking.view.activity.MainActivity;
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

    private static String mAPIKKey;
    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation() // Get only the fields with @Expose, allow to filter the fields from the dolibarr REST API
            .setDateFormat("yyyy-MM-dd") // To parse correctly the date with GSON. I have to use DATE_FORMAT(date, '%Y-%m-%d') dans api_invoice.class de dolibarr
            .create();
    private static OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
    private static Retrofit.Builder builder = new Retrofit.Builder();
    private Realm realm;
    private APIDolibarr dolibarr;
    private APIFileUpload retrofit;
    private User mUser;
    private Intent mLocalIntent;

    public DolibarrService() {
        super("DolibarrService");
    }

    public static void changeApiBaseUrl(String newApiBaseUrl) {

        builder.addConverterFactory(GsonConverterFactory.create())
                .baseUrl(newApiBaseUrl)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .callFactory(httpClientBuilder.build());
    }

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
                    } catch (IllegalStateException ise) {
                        ise.printStackTrace();
                        res = -11;
                    }
                    if (res == 0) {
                        sendBroadcastMessage(R.string.post_invoices_dolibarr_success);
                    }
                } else if (Constants.LOGIN_TO_DOLIBARR.equals(action)) {
                    Intent intentMainActivity = new Intent(this, MainActivity.class);
                    intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentMainActivity);
                } else if (Constants.SYNC_DATA_WITH_DOLIBARR.equals(action)) {
                    syncData();
                }
            } else {
                sendBroadcastMessage(R.string.service_dolibarr_error);
            }
            realm.close();
        }
    }

    private void syncData() {
        Integer res;
        try {
            res = postInvoices();
            if (res == 0) {
                res = updateData();
                if (res == 0) {
                    sendBroadcastMessage(R.string.update_data_dolibarr_success);
                } else if (res == -1) {
                    sendBroadcastMessage(R.string.update_data_dolibarr_fail_1);
                }  else if (res == -5) {
                    sendBroadcastMessage(R.string.update_data_dolibarr_fail_5);
                } else {
                    sendBroadcastMessage(R.string.update_data_dolibarr_fail);
                }
            } else {
                sendBroadcastMessage(R.string.post_invoices_dolibarr_fail);
            }
        } catch (IOException e) {
            sendBroadcastMessage(R.string.post_invoices_dolibarr_fail);
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
        /*retrofit = new Retrofit.Builder()
                .baseUrl("")
                .build()
                .create(APIFileUpload.class);*/

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
            return null != res && res.has("success");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendBroadcastMessage(int message) {
        mLocalIntent = new Intent(Constants.BROADCAST_MESSAGE_INTENT).putExtra(Constants.BROADCAST_MESSAGE_SEND, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(mLocalIntent);
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

        final Date modifiedDate = new Date();
        try {
            //Prevent incoherence into the database, IMPORTANT
            // DO NOT DELETE ANYTHING BEFORE INVOICE POSTED
            if(realm.where(Invoice.class).equalTo("state", Invoice.TERMINEE).equalTo("isPOSTToDolibarr", false).findAll().size() > 0) {
                return -1;
            }

            // Product part
            final List<Product> products = dolibarr.getAllProducts(mAPIKKey).execute().body();
            if (null != products && !products.isEmpty()) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // Set the new modified date to product
                        for(Product product : products) {
                            product.setModifiedDate(modifiedDate);
                        }

                        realm.copyToRealmOrUpdate(products);

                        // Delete the product from realm who are not anymore into dolibarr
                        RealmResults<Product> productsToDelete = realm.where(Product.class).lessThan("modifiedDate", modifiedDate).findAll();
                        for(Product product : productsToDelete) {
                            // First delete product from table ProductAndGroupBinding
                            RealmResults<ProductAndGroupBinding> binds = realm.where(ProductAndGroupBinding.class).equalTo("product.id", product.getId()).findAll();
                            for(ProductAndGroupBinding bind : binds) {
                                deleteProductBind(bind);
                            }

                            // Then delete product from realm
                            product.deleteFromRealm();
                        }
                    }
                });
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                    }
                });
            } else {
                return -2;
            }

            // Customer part
            final List<Customer> customers = dolibarr.getAllCustomers(mAPIKKey).execute().body();
            if (null != customers && !customers.isEmpty()) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // Set the new modified date to customer
                        for(Customer customer : customers) {
                            customer.setModifiedDate(modifiedDate);
                        }

                        realm.copyToRealmOrUpdate(customers);

                        // Delete the customer from realm who are not anymore into dolibarr
                        RealmResults<Customer> customersToDelete = realm.where(Customer.class).lessThan("modifiedDate", modifiedDate).findAll();
                        for(Customer customer : customersToDelete) {
                            // First delete customer from table CustomerAndGroupBinding
                            RealmResults<CustomerAndGroupBinding> binds = realm.where(CustomerAndGroupBinding.class).equalTo("customer.id", customer.getId()).findAll();
                            for(CustomerAndGroupBinding bind : binds) {
                                deleteCustomerBind(bind);
                            }

                            // Then delete customer from realm
                            customer.deleteFromRealm();
                        }
                    }
                });
            } else {
                return -3;
            }


            // Price of product per customer part
            final List<ProductCustomerPriceDolibarr> prices = dolibarr.getAllProductsCustomerPrice(mAPIKKey).execute().body();
            if (null != prices && !prices.isEmpty()) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // Set the new modified date to price
                        for(ProductCustomerPriceDolibarr price : prices) {
                            price.setModifiedDate(modifiedDate);
                        }

                        realm.copyToRealmOrUpdate(prices);

                        // Delete the price from realm who are not anymore into dolibarr
                        realm.where(ProductCustomerPriceDolibarr.class).lessThan("modifiedDate", modifiedDate).findAll().deleteAllFromRealm();
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
            return -6;
        }

        // Set the date of the last sync
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Value value = realm.where(Value.class).findFirst();
                if (null != value) {
                    value.setLastSync(new Date());
                }
            }
        });

        // If all is fine
        return 0;
    }

    private boolean getDolibarrInvoices() throws IOException, IllegalStateException {
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

    private Integer postInvoices() throws IOException, IllegalStateException {
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

            // If many avoirs are related ton one single invoice
            if (!facture_source.isPOSTToDolibarr()) {
                if (!postInvoiceToDolibarr(facture_source)) {
                    return -4;
                }
            }

            if (postInvoiceToDolibarr(avoir)) {
                setPaidAndConsume(avoir.getId_dolibarr(), facture_source.getId_dolibarr());
            } else {
                return -5;
            }

            /*final Integer fk_facture_source = createInvoice(facture_source);
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
                    Integer fk_avoir = createInvoice(avoir);
                    if (fk_avoir != 0) {
                        // Validate the avoir
                        if (validateInvoice(fk_avoir, avoir)) {
                            if (!setPaidAndConsume(fk_avoir, fk_facture_source)) {
                                return -5;
                            }
                        } else {
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
            }*/
        }


        // Then post the rest of the factures
        RealmResults<Invoice> invoices = realm.where(Invoice.class).equalTo("state", Invoice.TERMINEE).equalTo("isPOSTToDolibarr", false).findAll();
        for (final Invoice facture : invoices) {
            if (Invoice.TERMINEE.equals(facture.getState()) && !facture.isPOSTToDolibarr() && Invoice.FACTURE.equals(facture.getType())) {

                if (!postInvoiceToDolibarr(facture)) {
                    return -6;
                }
            }
        }

        // If all is fine
        return 0;
    }

    private boolean postInvoiceToDolibarr(final Invoice invoice) throws IOException, IllegalStateException {
        if (invoiceExists(invoice.getRef(), invoice.getTotalTTC(), invoice.getCustomer().getId())) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    invoice.setIsPOSTToDolibarr(true);
                }
            });
            return true;
        } else {
            Integer fk_dolibarr_invoice = createInvoice(invoice);
            if (fk_dolibarr_invoice > 0) {
                return validateInvoice(fk_dolibarr_invoice, invoice);
            } else {
                return false;
            }
        }
    }

    private Integer createInvoice(Invoice invoice) throws IOException, IllegalStateException {
        Call<JsonPrimitive> call = dolibarr.createInvoiceFromJson(mAPIKKey, convertInvoice2Json(invoice));
        JsonPrimitive req = call.execute().body();
        if (req == null) {
            return -1;
        }
        // Return the id of the invoice from the response of the Dolibarr REST server
        return req.getAsInt();
    }

    private Boolean validateInvoice(final Integer fk_dolibarr_invoice, final Invoice invoice) throws IOException, IllegalStateException {
        Call<JsonPrimitive> call = dolibarr.validateInvoice(fk_dolibarr_invoice, mAPIKKey);
        JsonPrimitive req = call.execute().body();
        if (req != null && req.getAsBoolean()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    invoice.setIsPOSTToDolibarr(true);
                    invoice.setId_dolibarr(fk_dolibarr_invoice);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    private Boolean setPaidAndConsume(final Integer fk_avoir_dolibarr, final Integer fk_facture_dolibarr) throws IOException, IllegalStateException {
        Call<JsonPrimitive> call = dolibarr.setPaidAndConsumeAvoir(fk_avoir_dolibarr, fk_facture_dolibarr, mAPIKKey);
        JsonPrimitive req = call.execute().body();
        return req != null && req.getAsBoolean();
    }

    private Boolean invoiceExists(final String ref_client, final Integer total_ttc, final Integer fk_soc) throws IOException, IllegalStateException {
        Call<JsonPrimitive> call = dolibarr.invoiceRefClientTotalExists(ref_client, total_ttc, fk_soc, mAPIKKey);
        JsonPrimitive req = call.execute().body();
        return req != null && req.getAsBoolean();
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
                obj.addProperty("note_private", "Tablette " + invoice.getUser().getName() + ", ref " + invoice.getRef());
            } else if (Invoice.AVOIR.equals(invoice.getType())) {
                Invoice facture_source = realm.where(Invoice.class).equalTo("id", invoice.getFk_facture_source()).findFirst();
                obj.addProperty("note_private", "Tablette " + invoice.getUser().getName() + ", ref " + invoice.getRef() + ", facture " + facture_source.getRef());
                obj.addProperty("type", 2);  // 0=Facture de doit, 2=Facture avoir
                obj.addProperty("fk_facture_source", facture_source.getId_dolibarr());
            }
            obj.addProperty("cond_reglement_id", 1);            // 1=A reception

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
                Integer total_tgc = line.getTotal_tgc();
                Integer total_ttc = line.getTotal_ttc();
                if (Invoice.AVOIR.equals(invoice.getType())) {
                    subprice = -subprice;
                    total_ht = -total_ht;
                    total_tss = -total_tss;
                    total_tgc = -total_tgc;
                    total_ttc = -total_ttc;
                }
                lineJson.addProperty("subprice", subprice);
                lineJson.addProperty("total_ht", total_ht);
                lineJson.addProperty("total_tva", total_tss);
                lineJson.addProperty("total_localtax1", total_tgc);
                lineJson.addProperty("total_ttc", total_ttc);

                lineJson.addProperty("qty", line.getQty());
                lineJson.addProperty("tva_tx", line.getProd().getTva_tx());
                lineJson.addProperty("localtax1_tx", line.getProd().getLocaltax1_tx());
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

    private void deleteCustomerBind(CustomerAndGroupBinding bind) {
        // Set correct position of customer
        RealmResults<CustomerAndGroupBinding> lines = realm.where(CustomerAndGroupBinding.class)
                .equalTo("group.id", bind.getGroup().getId())
                .greaterThan("position", bind.getPosition())
                .findAll();
        for (CustomerAndGroupBinding line : lines) {
            line.setPosition(line.getPosition() - 1);
        }
        //Delete the line
        bind.deleteFromRealm();
    }

    private void deleteProductBind(ProductAndGroupBinding bind) {
        // Set correct position of product
        RealmResults<ProductAndGroupBinding> lines = realm.where(ProductAndGroupBinding.class)
                .equalTo("group.id", bind.getGroup().getId())
                .greaterThan("position", bind.getPosition())
                .findAll();
        for (ProductAndGroupBinding line : lines) {
            line.setPosition(line.getPosition() - 1);
        }
        //Delete the line
        bind.deleteFromRealm();
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