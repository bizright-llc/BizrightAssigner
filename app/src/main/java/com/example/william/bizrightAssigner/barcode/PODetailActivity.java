package com.example.william.bizrightAssigner.barcode;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.william.bizrightAssigner.Bean.DataTransferContainer;
import com.example.william.bizrightAssigner.Bean.EventLog;
import com.example.william.bizrightAssigner.Bean.SmallLocationPool;
import com.example.william.bizrightAssigner.Info.GlobalInfo;
import com.example.william.bizrightAssigner.R;
import com.example.william.bizrightAssigner.SerOkHttpClient;
import com.example.william.bizrightAssigner.Util.TableAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;


public class PODetailActivity extends FragmentActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener,GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {
    private final String Tag = "PODetailActivity";
    private TextView main_tv,patchDate, tv_codeid;
    private Button Parking;
    private String token;
    private SerOkHttpClient serOkHttpClient;
    private OkHttpClient client;
    private String scanData;
    public static final MediaType JSON =MediaType.parse("application/json; charset=utf-8");
    private SmallLocationPool smallLocationPool=new SmallLocationPool();
    private EventLog eventLog;
    private String flags=new String();//=new String[]{"po","barcode"}; check scanner flag
    GestureDetector detector;
    private float fontSize = 30;
    private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;

private String PO;

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.Parking:
               // MoveToParking();
                Toast.makeText(this, "Successful" , Toast.LENGTH_SHORT).show();

                break;

            default:
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podetail);

        //get token
        Intent intent = getIntent();
        token = intent.getStringExtra("token");

        serOkHttpClient= (SerOkHttpClient) getApplication();
        client=serOkHttpClient.getClient();

        initView();

        Parking = (Button) findViewById(R.id.Parking);
        Parking.setOnClickListener(this);


        //show po items status
        PO=intent.getStringExtra("PO");
        showPOItems(PO);
        mDetector = new GestureDetectorCompat(this,this);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);



    }

    private void showPOItems(String PO) {
        okhttp3.Request requestSmallData;
        Gson g =new Gson();

       // String po="3GF8H4PA";
        requestSmallData = new okhttp3.Request.Builder()
                .url("http://"+ GlobalInfo.IP_ADDRESS+":8080/TPA/api/AmazonVendorOutputPOLines/GetItemsByPO?PO="+PO)
                .header("Authorization", "Bearer " + token)
                .build();
        okhttp3.Response response2 = null;

        try {
            response2 = client.newCall(requestSmallData).execute();
            String oj=response2.body().string();

            setContentView(R.layout.activity_podetail);

            List<DataTransferContainer> s=g.fromJson(oj,new TypeToken<List<DataTransferContainer>>(){}.getType());

            //设置表格标题的背景颜色
            ViewGroup tableTitle = (ViewGroup) findViewById(R.id.table_title);
            tableTitle.setBackgroundColor(Color.rgb(255, 100, 10));

            List<DataTransferContainer> list = new ArrayList<DataTransferContainer>();

            for (DataTransferContainer d:s
                 ) {
                if(d.getQTY()==null)d.setQTY(0);
                if(d.getMoveIn()==null)d.setMoveIn("0");
                //if(d.getStatus()==null)d.setStatus("");
                if (d.getQTY() == Integer.parseInt(d.getMoveIn())) {
                    d.setStatus("Done");
                } else {
                    d.setStatus("Processing");
                }
                list.add(d);
            }
            ListView tableListView = (ListView) findViewById(R.id.my_list);
            TableAdapter adapter = new TableAdapter(this, list);
            tableListView.setAdapter(adapter);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void initView() {


    }



    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(DEBUG_TAG,"onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());

        if (event2.getX() - event1.getX() >100 && Math.abs(velocityX) > 200){
          //  setContentView(R.layout.activity_barcode);
          //  showPOItems(s8);

            Intent i=new Intent(PODetailActivity.this, BarcodeActivity_Jar.class);
            i.putExtra("token",token);
            startActivity(i);
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
      //  Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                            float distanceY) {
      //  Log.d(DEBUG_TAG, "onScroll: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
      //  Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
       // Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
      //  Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
       // Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
      //  Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        return true;
    }
}
