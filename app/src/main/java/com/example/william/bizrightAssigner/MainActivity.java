package com.example.william.bizrightAssigner;

import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


import com.example.william.bizrightAssigner.barcode.BarcodeActivity_Jar;
import com.example.william.bizrightAssigner.Info.GlobalInfo;
import com.example.william.bizrightAssigner.barcode.PODetailActivity;
import com.example.william.bizrightAssigner.barcode.Sys;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity  implements OnClickListener {
    private TextView username;
    private TextView pwd;
    private TextView smallTotal;
    private TextView alltTotal;
    private TextView Sku;
    private TextView UPC;
    private OkHttpClient client;
    private CheckBox sys;

    //	private PopupWindow mPopupWindow;
    private Button navi_button_oned_scan, navi_button_psam, navi_button_rs232,
            navi_button_caobiao, navi_button_t485 , navi_button_simpleic , navi_uhf_btn , navi_button_powersercurity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

        actionBar.hide();
        initView();

    }

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {

        };
    };

    private void initView() {
        navi_button_oned_scan = (Button) findViewById(R.id.navi_button_oned_scan);
        navi_button_oned_scan.setOnClickListener(this);

    }
    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        Intent oneIntent;
        switch (arg0.getId()) {
            case R.id.navi_button_oned_scan:
                //login and get the token
                if(checkLoginAndGetToken()=="0"){
                    return;
                }else{
                    sys = findViewById(R.id.sys);
                    if(sys.isChecked()){
                        Toast.makeText(this, "checked" , Toast.LENGTH_SHORT).show();
                        String token = checkLoginAndGetToken();
                        // load scan page
                        oneIntent = new Intent(this, Sys.class);
                        oneIntent.putExtra("token", token);
                        startActivity(oneIntent);
                        break;

                    }else {
                        String token = checkLoginAndGetToken();
                        // load scan page
                        oneIntent = new Intent(this, BarcodeActivity_Jar.class);
                        oneIntent.putExtra("token", token);
                        startActivity(oneIntent);
                        break;
                    }
                }


            default:
                break;
        }
    }

    private String checkLoginAndGetToken(){
        username = findViewById(R.id.username);
        pwd = findViewById(R.id.pwd);

        username.setText("william.x");
        pwd.setText("abc123456");

        if (username.getText().toString().matches("")) {
            Toast.makeText(this, "You did not enter a username", Toast.LENGTH_SHORT).show();
            return "0";
        }
        if (pwd.getText().toString().matches("")) {
            Toast.makeText(this, "You did not enter a pwd", Toast.LENGTH_SHORT).show();
            return "0";
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.cookieJar(new CookieJar() {
            private final HashMap<String, List<Cookie>> cookieStore = new HashMap<String, List<Cookie>>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        });
        SerOkHttpClient serOkHttpClient= (SerOkHttpClient) getApplication();
        client=serOkHttpClient.getClient();
        client = builder.addInterceptor(logging)
                .build();
        serOkHttpClient.setClient(client);
        String token=null;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("http://"+GlobalInfo.IP_ADDRESS+":8080/TPA/api/Authorize/leadToAuthorize?userName="+username.getText()+"&&pwd="+pwd.getText())
                .build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            JsonParser parser = new JsonParser();

            JsonElement json =  parser.parse(response.body().string());
            token=json.getAsJsonObject().get("result").getAsJsonObject().get("authorization").toString().substring(1,json.getAsJsonObject().get("result").getAsJsonObject().get("authorization").toString().length()-1);
            Toast.makeText(this, "login successfull", Toast.LENGTH_SHORT).show();
            serOkHttpClient.setSessionUsername(username.getText().toString());
            return token;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0";


    }

    public static class sessionUsername {
        private String Name;

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }
    }
}
