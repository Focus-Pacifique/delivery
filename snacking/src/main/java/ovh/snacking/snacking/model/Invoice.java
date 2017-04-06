package ovh.snacking.snacking.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Alex on 11/10/2016.
 *
 */

public class Invoice extends RealmObject {

    public static final Integer EN_COURS = 0;
    public static final Integer TERMINEE = 1;

    public static final Integer FACTURE = 0;
    public static final Integer AVOIR = 1;

    @PrimaryKey
    private Integer id;
    private Integer type;
    private Integer state;
    private User user;
    private Date date;
    private Integer number;
    private String ref;
    private Customer customer;
    private RealmList<Line> lines;

    private Integer fk_facture_source;
    private Integer id_dolibarr;
    private Boolean isPOSTToDolibarr;
    private Integer counterPrint;

    public Invoice() {
        super();
        this.type = FACTURE;
        this.number = 0;
        this.date = new Date();
        this.state = EN_COURS;
        this.lines = new RealmList<>();
        this.ref = "0000";
        this.isPOSTToDolibarr = false;
        this.counterPrint = 0;

    }

    public String computeRef(Integer nb) {
        String ref;
        if (Invoice.AVOIR.equals(type)) {
            ref = "AV";
        } else {
            ref = "FA";
        }
        // 2 numbers of the current year
        SimpleDateFormat simpleDate = new SimpleDateFormat("yy");
        String year = simpleDate.format(date);

        ref += user.getName() + year + "-" + String.format("%04d", nb);

        return ref;
    }

    public Integer getTotalHT() {
        Integer total_ht = 0;
        for (Line line : lines) {
            total_ht += line.getTotal_ht();
        }
        return total_ht;
    }

    /*public Integer getTotalHTRound() {
        return (int) Math.round(getTotalHT());
    }*/

    public Integer getTotalTSS() {
        Integer total_tss = 0;
        for (Line line : lines) {
            total_tss += line.getTotal_tva();
        }
        return total_tss;
    }

    /*public Integer getTotalTSSRound() {
        return (int) Math.round(getTotalTSS());
    }*/

    public Integer getTotalTGC() {
        Integer total_tgc = 0;
        for (Line line : lines) {
            total_tgc += line.getTotal_tgc();
        }
        return total_tgc;
    }

    /*public Integer getTotalTGCRound() {
        return (int) Math.round(getTotalTGC());
    }*/

    public Integer getTotalTTC() {
        Integer total_ttc = 0;
        for (Line line : lines) {
            total_ttc += line.getTotal_ttc();
        }
        return total_ttc;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
        this.ref = computeRef(number);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RealmList<Line> getLines() {
        return lines;
    }

    public void setLines(RealmList<Line> line) {
        this.lines = line;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Boolean isPOSTToDolibarr() {
        return isPOSTToDolibarr;
    }

    public void setIsPOSTToDolibarr(Boolean isPOSTToDolibarr) {
        this.isPOSTToDolibarr = isPOSTToDolibarr;
    }

    public Integer getFk_facture_source() {
        return fk_facture_source;
    }

    public void setFk_facture_source(Integer fk_facture_source) {
        this.fk_facture_source = fk_facture_source;
    }

    public Integer getCounterPrint() {
        return counterPrint;
    }

    public void addCounterPrint() {
        this.counterPrint++;
    }

    public Integer getId_dolibarr() {
        return id_dolibarr;
    }

    public void setId_dolibarr(Integer id_dolibarr) {
        this.id_dolibarr = id_dolibarr;
    }
}
