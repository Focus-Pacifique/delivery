package com.focus.delivery.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.realm.Realm;
import com.focus.delivery.util.RealmSingleton;
import com.focus.delivery.model.User;

/**
 * Created by Alex on 24/10/2016.
 */

public class SplashActivity extends AppCompatActivity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = RealmSingleton.getInstance(getApplicationContext()).getRealm();

        if (alreadyLoggedIn()) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        realm.close();
        finish();
    }

    private boolean alreadyLoggedIn() {
        // If the user already exist
        return null != realm.where(User.class).equalTo("isActive", true).findFirst();
    }
}
