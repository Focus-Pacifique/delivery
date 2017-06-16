package ovh.snacking.snacking.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by Pc on 29/11/2016.
 */

public class Value extends RealmObject {

    private Integer lastNumberFacture;
    private Integer lastNumberAvoir;
    private Integer currentYear;
    private Date lastSync;

    public Value() {
        initialize();
    }

    private void initialize() {
        // To start at invoice n°4 and not n°1
        this.lastNumberAvoir = 3;
        this.lastNumberFacture = 3;
        SimpleDateFormat simpleDate = new SimpleDateFormat("yy");
        Integer year = Integer.valueOf(simpleDate.format(new Date()));
        this.currentYear = year;
    }

    public void update(Invoice invoice) {
        SimpleDateFormat simpleDate = new SimpleDateFormat("yy");
        Integer year = Integer.valueOf(simpleDate.format(new Date()));
        if (year.equals(currentYear)) {
            if (Invoice.AVOIR.equals(invoice.getType())) {
                addNumberAvoir();
            } else {
                addNumberFacture();
            }
        } else {
            initialize();
        }

    }

    public Integer getLastNumberFacture() {
        return lastNumberFacture;
    }

    public Integer getLastNumberAvoir() {
        return lastNumberAvoir;
    }

    private void addNumberFacture() {
        this.lastNumberFacture += 1;
    }

    private void addNumberAvoir() {
        this.lastNumberAvoir += 1;
    }

    public Integer getCurrentYear() {
        return currentYear;
    }

    public Date getLastSync() {
        return lastSync;
    }

    public void setLastSync(Date lastSync) {
        this.lastSync = lastSync;
    }

}
