package com.focus.delivery.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Alex on 21/10/2016.
 */

public class User extends RealmObject {
    @PrimaryKey
    private Integer id;
    private String name;
    private String serverURL;
    private String apiKey;
    private Boolean isActive;
    private Integer initialValues;

    public User() {
        this.isActive = false;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String token) {
        this.apiKey = token;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getInitialValues() {
        return initialValues;
    }

    public void setInitialValues(Integer initialValues) {
        this.initialValues = initialValues;
    }
}
