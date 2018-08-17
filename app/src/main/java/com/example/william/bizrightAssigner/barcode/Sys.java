package com.example.william.bizrightAssigner.barcode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import com.example.william.bizrightAssigner.Info.GlobalInfo;
import com.example.william.bizrightAssigner.Util.DatePickerFragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.jb.barcode.BarcodeManager;
import android.jb.barcode.BarcodeManager.Callback;
import android.jb.utils.Tools;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.william.bizrightAssigner.Bean.DataTransferContainer;
import com.example.william.bizrightAssigner.Bean.EventLog;
import com.example.william.bizrightAssigner.Bean.SmallLocationPool;
import com.example.william.bizrightAssigner.R;
import com.example.william.bizrightAssigner.SerOkHttpClient;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;


public class Sys extends FragmentActivity implements OnClickListener, DatePickerDialog.OnDateSetListener {

	private final String Tag = "Sys";
	private TextView main_tv,patchDate, tv_codeid;
	private Button start_bu1, patch,pick_time;
	private LinearLayout ll_loding;
	private ScrollView scrollview;
	private long nowTime = 0;
	private long lastTime = 0;
	private final int Handler_SHOW_RESULT = 1999;
	private final int Handler_Close_Loding = 2001;
	private BeepManager beepManager;
	private BarcodeManager barcodeManager;
	private byte[] codeBuffer;
	private String codeId;
	private String token;
	private SerOkHttpClient serOkHttpClient;
	private OkHttpClient client;
	private String scanData;
	public static final MediaType JSON =MediaType.parse("application/json; charset=utf-8");
	private SmallLocationPool smallLocationPool=new SmallLocationPool();
	private EventLog eventLog;
	private String flags=new String();//=new String[]{"po","barcode"}; check scanner flag


	private DataTransferContainer dataProtect=new DataTransferContainer();

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Handler_SHOW_RESULT:
				tv_codeid.setText("");
				if (null != codeId) {
					tv_codeid.setText(codeId);
				}
				if (null != codeBuffer) {
					String codeType = Tools.returnType(codeBuffer);
					String val = null;
					if (codeType.equals("default")) {
						val = new String(codeBuffer);
					} else {
						try {
							val = new String(codeBuffer, codeType);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					main_tv.append(val + "\n");
					Log.d(Tag, val);
					//beepManager.play();
					scrollview.fullScroll(ScrollView.FOCUS_DOWN);
				}
				break;
				
			case Handler_Close_Loding:
				ll_loding.setVisibility(View.GONE);
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sys);

		//get token
		Intent intent = getIntent();
		token = intent.getStringExtra("token");

		serOkHttpClient= (SerOkHttpClient) getApplication();
		client=serOkHttpClient.getClient();

		/**
		 * 监听橙色按钮按键广播
		 */
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.jb.action.F4key");
		registerReceiver(f4Receiver, intentFilter);

		initView();

		beepManager = new BeepManager(this, true, false);
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		//若有启动有扫描服务则开启
		sendBroadcast(new Intent("ReLoadCom"));
		if (null != barcodeManager) {
			barcodeManager.Barcode_Close();
			barcodeManager.Barcode_Stop();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(f4Receiver);
		super.onDestroy();
	}

	private void initView() {

		scrollview = (ScrollView) findViewById(R.id.scrollview);
		main_tv = (TextView) findViewById(R.id.main_tv);
		start_bu1 = (Button) findViewById(R.id.start_bu1);
		patch = (Button) findViewById(R.id.patch);
		patchDate = (TextView) findViewById(R.id.patchDate);
		tv_codeid = (TextView) findViewById(R.id.tv_codeid);
		patchDate.setFocusable(false);
		patchDate.setClickable(false);

		start_bu1.setOnClickListener(this);

	}

	Callback dataReceived = new Callback() {
		@Override
		public void Barcode_Read(byte[] buffer, String codeId, int errorCode) {
			// TODO Auto-generated method stub
			if (null != buffer) {
				codeBuffer = buffer;
				Sys.this.codeId = codeId;
				Message msg = new Message();
				msg.what = Handler_SHOW_RESULT;
				mHandler.sendMessage(msg);
				barcodeManager.Barcode_Stop();
			}
		}
	};

	/**
	 * 捕获扫描物理按键广播
	 */
	private BroadcastReceiver f4Receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// Bundle bundle = intent.getExtras();
			if (intent.hasExtra("F4key")) {
				if (intent.getStringExtra("F4key").equals("down")) {
					Log.e("trig", "key down");
					// isContines = true;
					if (null != barcodeManager) {
						nowTime = System.currentTimeMillis();

						if (nowTime - lastTime > 200) {
							barcodeManager.Barcode_Stop();
							lastTime = nowTime;
							if (null != barcodeManager) {
								barcodeManager.Barcode_Start();
							}
						}
					}
				} else if (intent.getStringExtra("F4key").equals("up")) {
					Log.e("trig", "key up");
				}
			}
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.start_bu1:

			nowTime = System.currentTimeMillis();
			barcodeManager.Barcode_Stop();
			// 按键时间不低于200msf
			if (nowTime - lastTime > 200) {
				System.out.println("scan(0)");
				if (null != barcodeManager) {
					barcodeManager.Barcode_Start();
				}
				lastTime = nowTime;
			}
			break;
		case R.id.patch:
			patchUpdate();

		break;

		default:
			break;
		}
	}

	private void patchUpdate() {
		if(patchDate.getText()==null&&!(patchDate.getText().length()>7)&&!(patchDate.getText().length()<11)){
			Toast.makeText(this, "please double check your date" , Toast.LENGTH_SHORT).show();

		}else{
			okhttp3.Request requestSmallData;
			Gson g =new Gson();
			String daynow=patchDate.getText().toString();
			requestSmallData = new okhttp3.Request.Builder()
					.url("http://"+ GlobalInfo.IP_ADDRESS+":8080/TPA/api/AmazonVendorOutputPOLines/GetPatchUpdate?day="+daynow)
					.header("Authorization", "Bearer " + token)
					.build();
			okhttp3.Response response2 = null;

			try {
				response2 = client.newCall(requestSmallData).execute();
				String oj=response2.body().string();
				main_tv.setText(oj);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public SmallLocationPool getSmallLocationPool() {
		return smallLocationPool;
	}

	public void setSmallLocationPool(SmallLocationPool smallLocationPool) {
		this.smallLocationPool = smallLocationPool;
	}


	public DataTransferContainer getDataProtect() {
		return dataProtect;
	}

	public void setDataProtect(DataTransferContainer dataProtect) {
		this.dataProtect = dataProtect;
	}
	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");


	}

	public void onDateSet(DatePicker view, int year, int month, int day) {

		//use date in your activity
		patchDate.setText(Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(day));
	}


	@Override
	public void onPointerCaptureChanged(boolean hasCapture) {

	}

}
