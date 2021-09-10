package com.mycrolinks.passwdmgr.manager;

import android.os.StrictMode;

import java.io.IOException;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MasterPassword {
    private static void threadOverride() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public final static String domain = "http://192.168.0.8:8080";

    public static boolean isUserExists(String email) {
        threadOverride();
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder().add("email", email).build();
        Request request = new Request.Builder().url(domain + "/isUserExists").post(formBody).build();
        try {
            Response response = client.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string().equals("true");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkCredentials(String email, String password) {
        threadOverride();
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder().add("email", email).add("password", password).build();
        Request request = new Request.Builder().url(domain + "/checkCredentials").post(formBody).build();
        try {
            Response response = client.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string().equals("true");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setUser(String email, String password) {
        threadOverride();
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder().add("email", email).add("password", password).build();
        Request request = new Request.Builder().url(domain + "/setUser").post(formBody).build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void delUser(String email) {
        threadOverride();
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder().add("email", email).build();
        Request request = new Request.Builder().url(domain + "/delUser").post(formBody).build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
