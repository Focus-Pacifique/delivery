package ovh.snacking.snacking.controller;

/**
 * Created by Alex on 24/10/2016.
 */

public final class Constants {

    // Define action for the dolibarr service
    public static final String SYNC_DATA_WITH_DOLIBARR =
            "ovh.snacking.snacking.action.SYNC_DATA_WITH_DOLIBARR";

    public static final String GET_DATA_FROM_DOLIBARR =
            "ovh.snacking.snacking.action.GET_DATA_FROM_DOLIBARR";

    public static final String POST_INVOICE_TO_DOLIBARR =
            "ovh.snacking.snacking.action.POST_INVOICE_TO_DOLIBARR";

    public static final String LOGIN_TO_DOLIBARR =
            "ovh.snacking.snacking.action.LOGIN_TO_DOLIBARR";

    // Defines the synchronisation Intent action
    public static final String BROADCAST_MESSAGE_INTENT =
            "ovh.snacking.snacking.action.BROADCAST_MESSAGE_INTENT";

    // Defines the key to receive the broadcast message
    public static final String BROADCAST_MESSAGE_SEND =
            "ovh.snacking.snacking.action.BROADCAST_MESSAGE_SEND";

    // Defines the key for the login datas
    public static final String LOGIN_USER =
            "ovh.snacking.snacking.view.login.LOGIN_USER";
    public static final String PASSWORD_USER =
            "ovh.snacking.snacking.view.login.PASSWORD_USER";
    public static final String URL_SERVER =
            "ovh.snacking.snacking.view.login.URL_SERVER";
}
