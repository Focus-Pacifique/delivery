package ovh.snacking.snacking.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ACER on 06/02/2017.
 */

public class CustomerAndGroupBinding extends RealmObject {
    @PrimaryKey
    private Integer id;
    private CustomerGroup group;
    private Customer customer;
    private Integer position;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CustomerGroup getGroup() {
        return group;
    }

    public void setGroup(CustomerGroup group) {
        this.group = group;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
