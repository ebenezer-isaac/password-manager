package com.mycrolinks.passwdmgr.manager;

import android.annotation.SuppressLint;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PasswordItem implements Serializable {
    JSONObject passwordItem;

    public PasswordItem(String title, String password, String timestamp) {
        passwordItem = new JSONObject();
        try {
            passwordItem.put("title", title);
            passwordItem.put("password", password);
            passwordItem.put("timestamp", timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PasswordItem(String title, String password) {
        passwordItem = new JSONObject();
        try {
            Date today = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            passwordItem.put("title", title);
            passwordItem.put("password", password);
            passwordItem.put("timestamp", df.format(today));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PasswordItem(JSONObject passwordItem) {
        this.passwordItem = passwordItem;
    }

    public String getTitle() {
        try {
            return passwordItem.getString("title");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPassword() {
        try {
            return passwordItem.getString("password");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTimestamp() {
        try {
            return passwordItem.getString("timestamp");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getJSONObject() {
        return passwordItem;
    }
}
