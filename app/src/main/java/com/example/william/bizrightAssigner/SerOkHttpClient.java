package com.example.william.bizrightAssigner;

import android.app.Application;

import okhttp3.OkHttpClient;

public class SerOkHttpClient extends Application {
    public OkHttpClient client;
    public String sessionUsername;

    public String getSessionUsername() {
        return sessionUsername;
    }

    public void setSessionUsername(String sessionUsername) {
        this.sessionUsername = sessionUsername;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }
}
