package com.mycrolinks.passwdmgr.manager;

import android.os.StrictMode;

import java.io.IOException;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EnvelopeEncryption {
    public final static String domain = "http://192.168.0.8:8080";

    private static void threadOverride() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static String encrypt(String text) {
        threadOverride();
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder().add("text", text).build();
        Request request = new Request.Builder().url(domain + "/encrypt").post(formBody).build();
        try {
            Response response = client.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String cipher) {
        threadOverride();
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder().add("cipher", cipher).build();
        Request request = new Request.Builder().url(domain + "/decrypt").post(formBody).build();
        try {
            Response response = client.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
