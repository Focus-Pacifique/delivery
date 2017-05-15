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
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Alex on 19/10/2016.
 */

public interface APIDolibarr {

    @GET(".")
    Call<JsonObject> getAPIServerStatus(@Query("api_key") String apiKey);

    @GET("login")
    Call<JsonObject> getAPIKey(@Query("login") String login, @Query("password") String password);

    @GET("product/list")
    Call<List<Product>> getAllProducts(@Query("api_key") String apiKey);

    @GET("productcustomerprice/list")
    Call<List<ProductCustomerPriceDolibarr>> getAllProductsCustomerPrice(@Query("api_key") String apiKey);

    @GET("customer/list")
    Call<List<Customer>> getAllCustomers(@Query("api_key") String apiKey);

    @GET("invoice/summarylist")
    Call<List<DolibarrInvoice>> getInvoices(@Query("lastid") Integer lastId, @Query("api_key") String apiKey);

    @POST("invoice")
    Call<JsonPrimitive> createInvoiceFromJson(@Query("api_key") String apiKey, @Body JsonObject jsonObject);

    @PUT("invoice/{id}/validate")
    Call<JsonPrimitive> validateInvoice(@Path("id") Integer id, @Query("api_key") String apiKey);

    @PUT("invoice/{idavoir}/setpaid/{idinvoice}")
    Call<JsonPrimitive> setPaidAndConsumeAvoir(@Path("idavoir") Integer idavoir, @Path("idinvoice") Integer idinvoice, @Query("api_key") String apiKey);

    @GET("invoice/refclient")
    Call<JsonPrimitive> invoiceRefClientTotalExists(@Query("ref_client") String ref_client,
                                                    @Query("total_ttc") Integer total_ttc,
                                                    @Query("fk_soc") Integer fk_soc,
                                                    @Query("api_key") String apiKey);
}
