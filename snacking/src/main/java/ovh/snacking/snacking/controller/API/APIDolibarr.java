package ovh.snacking.snacking.controller.API;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;

import ovh.snacking.snacking.model.Customer;
import ovh.snacking.snacking.model.DolibarrInvoice;
import ovh.snacking.snacking.model.Product;
import ovh.snacking.snacking.model.ProductCustomerPriceDolibarr;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Alex on 19/10/2016.
 */

public interface APIDolibarr {

    @GET(".")
    Call<JsonObject> getAPIServerStatus(@Query("DOLAPIKEY") String DOLAPIKEY);

    @GET("login")
    Call<JsonObject> getAPIKey(@Query("login") String login, @Query("password") String password);

    // Products
    @GET("products")
    Call<List<Product>> getAllProducts(@Query("DOLAPIKEY") String DOLAPIKEY);

    // ProductCustomerPrice
    @GET("productcustomerprices")
    Call<List<ProductCustomerPriceDolibarr>> getAllProductsCustomerPrice(@Query("DOLAPIKEY") String DOLAPIKEY);

    // Customer
    @GET("customer/list")
    Call<List<Customer>> getAllCustomers(@Query("DOLAPIKEY") String DOLAPIKEY);

    //Invoices
    @GET("invoices/summarylist")
    Call<List<DolibarrInvoice>> getInvoices(@Query("lastid") Integer lastId, @Query("DOLAPIKEY") String DOLAPIKEY);

    @POST("invoices")
    Call<JsonPrimitive> createInvoiceFromJson(@Query("DOLAPIKEY") String DOLAPIKEY, @Body JsonObject jsonObject);

    @GET("invoices/validateinvoice/{id}")
    Call<JsonPrimitive> validateInvoice(@Path("id") Integer id, @Query("DOLAPIKEY") String DOLAPIKEY);

    @GET("invoices/setpaidandconsumeavoir")
    Call<JsonPrimitive> setPaidAndConsumeAvoir(@Query("idavoir") Integer idavoir, @Query("idinvoice") Integer idinvoice, @Query("DOLAPIKEY") String DOLAPIKEY);

    @GET("invoices/invoicerefclienttotalexists")
    Call<JsonPrimitive> invoiceRefClientTotalExists(@Query("ref_client") String ref_client,
                                                    @Query("total_ttc") Integer total_ttc,
                                                    @Query("fk_soc") Integer fk_soc,
                                                    @Query("DOLAPIKEY") String DOLAPIKEY);
}
