package com.example.william.bizrightAssigner.barcode;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.jb.barcode.BarcodeManager;
import android.jb.barcode.BarcodeManager.Callback;
import android.jb.utils.Tools;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.william.bizrightAssigner.Bean.ClosedPO;
import com.example.william.bizrightAssigner.Bean.DataTransferContainer;
import com.example.william.bizrightAssigner.Bean.EventLog;
import com.example.william.bizrightAssigner.Bean.Item;
import com.example.william.bizrightAssigner.Bean.SmallLocationPool;
import com.example.william.bizrightAssigner.SerOkHttpClient;
import com.example.william.bizrightAssigner.R;
import com.example.william.bizrightAssigner.Info.GlobalInfo;
import com.example.william.bizrightAssigner.Util.EventLogProcess;
import com.example.william.bizrightAssigner.Util.TableAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class BarcodeActivity_Jar extends Activity implements OnClickListener, DatePickerDialog.OnDateSetListener,GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

	private final String Tag = "BarcodeActivity";
	private TextView main_tv, tv_codeid,movedQTY,ItemName;
	private Button start_bu1, stop_bu2,receivedMoved,moveOut;
	private ImageView productImage;
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
	private  OkHttpClient client;
	private String scanData;
	public static final MediaType JSON =MediaType.parse("application/json; charset=utf-8");
	private SmallLocationPool smallLocationPool=new SmallLocationPool();
	private 	EventLog eventLog;
	private String flags=new String();//=new String[]{"po","barcode"}; check scanner flag
	private static final String DEBUG_TAG = "Gestures";
	private GestureDetectorCompat mDetector;


	private DataTransferContainer dataProtect=new DataTransferContainer();
	private String PO;

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
                            //返回数据
                            scanData=val;

                            //check typeof scaner
                            //action
							if(flags.equals("barcode")) {
								scanAction(val);
							}
							if(flags.equals("po")){
								moveout(val);
							}
							if(flags.equals("ParkingPO")){
								ParkingByPO(val);
							}

						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//main_tv.append(val + "\n");
					Log.d(Tag, val);
					beepManager.play();
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
		setContentView(R.layout.activity_barcode);


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

		mDetector = new GestureDetectorCompat(this,this);
		// Set the gesture detector as the double tap
		// listener.
		mDetector.setOnDoubleTapListener(BarcodeActivity_Jar.this);

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
			tableListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
										long id) {

					DataTransferContainer item  = (DataTransferContainer) parent.getItemAtPosition(position);
					getItemDetail(item.getSKU());


				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	//after click item
	private void getItemDetail(String sku) {
		okhttp3.Request requestSmallData;
		Gson g =new Gson();

		// String po="3GF8H4PA";
		requestSmallData = new okhttp3.Request.Builder()
				.url("http://"+ GlobalInfo.IP_ADDRESS+":8080/TPA/api/AmazonVendorOutputPOLines/GetItemDetail?sku="+sku)
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;

		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj=response2.body().string();
			DataTransferContainer d=g.fromJson(oj,DataTransferContainer.class);
			if(d.getItemName()!=null){
				setContentView(R.layout.activity_itemdetail);
				String a=d.getItemName();
				TextView vs=findViewById(R.id.ItemName);
				vs.setText(a);
				productImage=(ImageView)findViewById(R.id.ProductImage);

				if(d.getImgUrl()!=null) {
					Picasso.with(this).load(d.getImgUrl()).placeholder(R.mipmap.ic_launcher)
							.error(R.mipmap.ic_launcher)
							.into(productImage, new com.squareup.picasso.Callback() {


								@Override
								public void onSuccess() {

								}

								@Override
								public void onError() {

								}
							});
				}

			}


		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private void showParkingPOItems(String PO) {
		okhttp3.Request requestSmallData;
		Gson g =new Gson();

		// String po="3GF8H4PA";
		requestSmallData = new okhttp3.Request.Builder()
				.url("http://"+ GlobalInfo.IP_ADDRESS+":8080/TPA/api/AmazonVendorOutputPOLines/GetItemsByParkingPO?PO="+PO)
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;

		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj=response2.body().string();
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



	public String getQtyAndSmallAreabyUPC(String val){
		//use token get data for test
		val.toString();
		val="638104014748";
		okhttp3.Request requestSmallData = new okhttp3.Request.Builder()
				.url("http://"+ GlobalInfo.IP_ADDRESS+":8080/TPA/api/AmazonVendorOutputPOLines/getQtyAndSmallAreabyUPC?upc=638104014748")
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;
		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj=response2.body().string();
			return oj;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return "0";
	}

	//after scan action
	public void scanAction(String val){
		main_tv.setText("");

	    //area check
		final DataTransferContainer dataTransferContainer=checkarea(val);
		if(dataTransferContainer==null||dataTransferContainer.getSKU()==null){
			String csku=checkSKU(val);
			if(csku!=null&&!csku.equals("")){
				Toast.makeText(this, csku , Toast.LENGTH_SHORT).show();

			}
			//return ;
		}else {
			String s1 = "Please Move to:\n";
			String s2="<font color='#FF0000'><B>"+dataTransferContainer.getArea()+"</B></font><br>";

			String s3 = "SKU : \n";
			String s4="<font color='#FF0000'><B>"+dataTransferContainer.getSKU()+"</B></font><br>";

			String s5 = "QTY : \n";
			String s6="<font color='#FF0000'><B>"+dataTransferContainer.getQTY()+"</B></font><br>";



			main_tv.setText(Html.fromHtml(s1));
			main_tv.append(Html.fromHtml(s2));

			main_tv.append(Html.fromHtml(s3));
			main_tv.append(Html.fromHtml(s4));

			main_tv.append(Html.fromHtml(s5));
			main_tv.append(Html.fromHtml(s6));

			receivedMoved.setEnabled(true);
			movedQTY.setEnabled(true);
			String s00=dataTransferContainer.getQTY().toString();

			movedQTY.setText(s00);

			dataProtect.setArea(dataTransferContainer.getArea());
			dataProtect.setQTY(dataTransferContainer.getQTY());
			dataProtect.setSKU(dataTransferContainer.getSKU());
			if (dataTransferContainer.getPONum() != null) {
				dataProtect.setPONum(dataTransferContainer.getPONum());
				String s7 = "PO# : \n";
			//	String s8="<font color='#FF0000'><B>"+dataTransferContainer.getPONum()+"</B></font><br>";
				final String s8=dataTransferContainer.getPONum();
				main_tv.append(Html.fromHtml(s7));


				final SpannableString ss=new SpannableString(s8);
				ss.setSpan(new ClickableSpan() {
					@Override
					public void onClick(View view) {
						setContentView(R.layout.activity_podetail);
						showPOItems(s8);


					}
				},0,s8.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				main_tv.append(ss);
				main_tv.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}


    }



	public String checkSKU(String upc) {
		okhttp3.Request requestSmallData;
		Gson g =new Gson();

		requestSmallData = new okhttp3.Request.Builder()
				.url("http://"+ GlobalInfo.IP_ADDRESS+":8080/TPA/api/AmazonVendorOutputPOLines/getCheckUPC?upc="+upc)
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;

		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj=response2.body().string();
			DataTransferContainer s=g.fromJson(oj,DataTransferContainer.class);

			if(s!=null &&s.getCheckSKU()!=null&&!s.getCheckSKU().trim().equals("")){

				return s.getCheckSKU();
			}



		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;


	}

	//return ready to pack and ship
	public void checkFull(){

		okhttp3.Request requestSmallData;
		Gson g =new Gson();

		requestSmallData = new okhttp3.Request.Builder()
				.url("http://"+ GlobalInfo.IP_ADDRESS+":8080/TPA/api/AmazonVendorOutputPOLines/getcheckFull")
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;

		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj=response2.body().string();
			List<DataTransferContainer> s=g.fromJson(oj,new TypeToken<List<DataTransferContainer>>(){}.getType());

			main_tv.setText("");
			//title
			String s1 = "Ready to Pack PO# : <br>";
			main_tv.append(Html.fromHtml(s1));

			//po num s
			if(response2.message().equals("OK")){
				for (DataTransferContainer d:s
						) {
					final String s8=d.getPONum();
					final SpannableString ss=new SpannableString(s8);
					ss.setSpan(new ClickableSpan() {
						@Override
						public void onClick(View view) {
							setContentView(R.layout.activity_parkingdetail);
							showPOItems(s8);
						}
					},0,s8.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					main_tv.append(d.getArea()+" : ");
					main_tv.append(ss);
					String br="<br>";
					main_tv.append(Html.fromHtml(br));
					main_tv.setMovementMethod(LinkMovementMethod.getInstance());


				}

			}else{
				Toast.makeText(this, "Unsuccessful" , Toast.LENGTH_SHORT).show();
				movedQTY.setText("");
			}


		} catch (IOException e) {
			e.printStackTrace();
		}



	}


	public void moveout(String po) {

		//check length if its ponumber
		if(po.length()>8){
			Toast.makeText(this, "Please doublecheck your PO Number! PO Number Length not correct!" , Toast.LENGTH_SHORT).show();
			return;
		}

		okhttp3.Request requestSmallData;
		Gson g =new Gson();
		String jsonString;

		ClosedPO closedPO=new ClosedPO();
		closedPO.setPONum(po);
		closedPO.setClosedDate(EventLogProcess.getTime());

		jsonString = g.toJson(closedPO);
		RequestBody body = RequestBody.create(JSON, jsonString);

		requestSmallData = new okhttp3.Request.Builder()
				.url("http://"+ GlobalInfo.IP_ADDRESS+":8080/TPA/api/ClosedPOes/PostClosedPO")
				.header("Authorization", "Bearer " + token)
				.post(body)
				.build();
		okhttp3.Response response2 = null;

		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj=response2.body().string();
			ClosedPO closedPO1=g.fromJson(oj,ClosedPO.class);
			if(closedPO1.getPONum()==null){
				Toast.makeText(this, "Please doublecheck your PO Number!"+oj , Toast.LENGTH_SHORT).show();
			}else {

				main_tv.setText(closedPO1.getPONum() + " is closed!\n");
				start_bu1.setEnabled(true);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private DataTransferContainer checkarea(String upc) {
		okhttp3.Request requestSmallData;
		Gson g =new Gson();

		requestSmallData = new okhttp3.Request.Builder()
				.url("http://"+ GlobalInfo.IP_ADDRESS+":8080/TPA/api/AmazonVendorOutputPOLines/getCheckArea?upc="+upc)
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;

		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj=response2.body().string();
			DataTransferContainer s=g.fromJson(oj,DataTransferContainer.class);
return s;

		} catch (IOException e) {
			e.printStackTrace();
		}
return null;



    }



    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		//若有启动有扫描服务则关闭
		sendBroadcast(new Intent("ReleaseCom"));
		if (barcodeManager == null) {
			barcodeManager = BarcodeManager.getInstance();
		}
		barcodeManager.Barcode_Open(this, dataReceived);
		ll_loding.setVisibility(View.VISIBLE);
		//给500ms启动扫描模块 避免此时操作扫描模块
		mHandler.sendEmptyMessageDelayed(Handler_Close_Loding, 500);
		super.onResume();
	}


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


	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(f4Receiver);
		super.onDestroy();
	}

	private void initView() {
		ll_loding = (LinearLayout) findViewById(R.id.ll_loding);
		scrollview = (ScrollView) findViewById(R.id.scrollview);
		main_tv = (TextView) findViewById(R.id.main_tv);
		tv_codeid = (TextView) findViewById(R.id.tv_codeid);
		start_bu1 = (Button) findViewById(R.id.start_bu1);

		receivedMoved = (Button) findViewById(R.id.receivedMoved);
		receivedMoved.setEnabled(false);

		moveOut = (Button) findViewById(R.id.moveOut);
		//moveOut.setEnabled(false);

		movedQTY = (TextView)findViewById(R.id.movedQTY);
		movedQTY.setEnabled(false);
		start_bu1.setOnClickListener(this);
		ItemName=findViewById(R.id.ItemName);



	}

	Callback dataReceived = new Callback() {

		@Override
		public void Barcode_Read(byte[] buffer, String codeId, int errorCode) {
			// TODO Auto-generated method stub
			if (null != buffer) {
				codeBuffer = buffer;
				BarcodeActivity_Jar.this.codeId = codeId;
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
			flags="barcode";
			nowTime = System.currentTimeMillis();
			barcodeManager.Barcode_Stop();
			// 按键时间不低于200ms
			if (nowTime - lastTime > 200) {
				if (null != barcodeManager) {
					barcodeManager.Barcode_Start();
				}
				lastTime = nowTime;
			}
			break;
			case R.id.moveOut:
				flags="po";
				nowTime = System.currentTimeMillis();
				barcodeManager.Barcode_Stop();
				// 按键时间不低于200ms
				if (nowTime - lastTime > 200) {
					//System.out.println("scan(0)");
					if (null != barcodeManager) {
						barcodeManager.Barcode_Start();

					}
					lastTime = nowTime;
				}
				break;

			case R.id.PoolList:
				getPoolList();
				break;
			case R.id.ParkingPONumbers:
				GetParkings();
				break;
			case R.id.Parking:
				MoveToParking(PO);
				break;
			case R.id.ReadyForPack:
				checkFull();

				break;


			case R.id.receivedMoved:
				if(!movedQTY.getText().toString().matches("")&&dataProtect.getArea().equals("Small")&&dataProtect.getQTY()>=Integer.parseInt(movedQTY.getText().toString())) {
					putupdateSmallPoolAndcheck();

					receivedMoved.setEnabled(false);
				}else if(!movedQTY.getText().toString().matches("")&&!dataProtect.getArea().equals("Small")&&dataProtect.getQTY()>=Integer.parseInt(movedQTY.getText().toString())){
					putupdateMediumPool();

					receivedMoved.setEnabled(false);
				}else{
					Toast.makeText(this, "Please check qty or your sku", Toast.LENGTH_SHORT).show();
				}
				break;

		default:
			break;
		}
	}


	//find parking by po
	public void ParkingByPO(String po) {
		okhttp3.Request requestSmallData;
		Gson g = new Gson();
		String jsonString;


		requestSmallData = new okhttp3.Request.Builder()
				.url("http://" + GlobalInfo.IP_ADDRESS + ":8080/TPA/api/AmazonVendorOutputPOLines/GetParkingByPO?PO="+po)
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;

		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj = response2.body().string();
			if(response2.message().equals("OK")){
				Toast.makeText(this, "successful" , Toast.LENGTH_SHORT).show();
				setContentView(R.layout.activity_parkingdetail);

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

			}else{
				Toast.makeText(this, "Unsuccessful" , Toast.LENGTH_SHORT).show();
				movedQTY.setText("");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	//return Parking BY PO
	public void GetParkings() {
		okhttp3.Request requestSmallData;
		Gson g = new Gson();
		String jsonString;


		requestSmallData = new okhttp3.Request.Builder()
				.url("http://" + GlobalInfo.IP_ADDRESS + ":8080/TPA/api/AmazonVendorOutputPOLines/GetParkings")
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;

		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj = response2.body().string();
			List<DataTransferContainer> s=g.fromJson(oj,new TypeToken<List<DataTransferContainer>>(){}.getType());
			main_tv.setText("");
			//title
			String s1 = "Parking PO# : <br>";
			main_tv.append(Html.fromHtml(s1));

			//po num s
			if(response2.message().equals("OK")){
				for (DataTransferContainer d:s
						) {
					final String s8=d.getPONum();
					final SpannableString ss=new SpannableString(s8);
					ss.setSpan(new ClickableSpan() {
						@Override
						public void onClick(View view) {
							setContentView(R.layout.activity_parkingdetail);
							showParkingPOItems(s8);


						}
					},0,s8.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					main_tv.append(ss);
					String br="<br>";
					main_tv.append(Html.fromHtml(br));
					main_tv.setMovementMethod(LinkMovementMethod.getInstance());


				}

			}else{
				Toast.makeText(this, "Unsuccessful" , Toast.LENGTH_SHORT).show();
				movedQTY.setText("");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	public void putupdateMediumPool() {
		String sku=dataProtect.getSKU();
		String PONum=dataProtect.getPONum();
		Integer qty=Integer.parseInt(movedQTY.getText().toString());
		okhttp3.Request requestSmallData;
		Gson g = new Gson();
		String jsonString;


		requestSmallData = new okhttp3.Request.Builder()
				.url("http://" + GlobalInfo.IP_ADDRESS + ":8080/TPA/api/MediumlocationPoolItems/GetIDAndQtyToUpdated?sku="+sku+"&&PONum="+PONum+"&&qty="+qty)
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;

		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj = response2.body().string();
			if(response2.message().equals("OK")){
				Toast.makeText(this, "successful" , Toast.LENGTH_SHORT).show();
				movedQTY.setText("");
				if(oj.length()>0){
					main_tv.setText("");
				}
			}else{
				Toast.makeText(this, "Unsuccessful" , Toast.LENGTH_SHORT).show();
				movedQTY.setText("");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	//get po numbers  and show main tv
	public void MoveToParking(String PO) {

		okhttp3.Request requestSmallData;
		Gson g = new Gson();
		String jsonString;

		requestSmallData = new okhttp3.Request.Builder()
				.url("http://" + GlobalInfo.IP_ADDRESS + ":8080/TPA/api/AmazonVendorOutputPOLines/GetParking?PO="+PO)
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;
		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj = response2.body().string();
			if(response2.message().equals("OK")){
				Toast.makeText(this, "Successful! Moved to Parking. PO: "+PO , Toast.LENGTH_SHORT).show();

			}else{
				Toast.makeText(this, "Unsuccessful" , Toast.LENGTH_SHORT).show();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public void putupdateSmallPoolAndcheck() {
		System.out.println();
		String sku=dataProtect.getSKU();
		String PONum=dataProtect.getPONum();
		Integer qty=Integer.parseInt(movedQTY.getText().toString());
		okhttp3.Request requestSmallData;
		Gson g = new Gson();
		String jsonString;


		requestSmallData = new okhttp3.Request.Builder()
				.url("http://" + GlobalInfo.IP_ADDRESS + ":8080/TPA/api/SmallLocationPools/GetIDAndQtyToUpdated?sku="+sku+"&&qty="+qty)
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;
		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj = response2.body().string();
			if(response2.message().equals("OK")){
				Toast.makeText(this, "successful" , Toast.LENGTH_SHORT).show();

				if(oj.length()>0&&!oj.trim().equals("<br>")){
					//String s1="<font color='#FF0000'><B>"+oj.substring(1,oj.length()-1)+" is FULL, Please move out from area, ship and close the order.</B></font><br>";
					main_tv.setText("");
					//main_tv.setText(Html.fromHtml(s1));
					receivedMoved.setEnabled(false);


				}

			}else{
				Toast.makeText(this, "Unsuccessful" , Toast.LENGTH_SHORT).show();
				movedQTY.setText("");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	//get po numbers  and show main tv
	public void getPoolList() {

		okhttp3.Request requestSmallData;
		Gson g = new Gson();
		String jsonString;


		requestSmallData = new okhttp3.Request.Builder()
				.url("http://" + GlobalInfo.IP_ADDRESS + ":8080/TPA/api/AmazonVendorOutputPOLines/GetMediumPoolPOs")
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;
		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj = response2.body().string();
			List<DataTransferContainer> s=g.fromJson(oj,new TypeToken<List<DataTransferContainer>>(){}.getType());
			main_tv.setText("");
			//title
			String s1 = "PO# : <br>";
			main_tv.append(Html.fromHtml(s1));

			//po num s
			if(response2.message().equals("OK")){
				for (DataTransferContainer d:s
					 ) {
						final String s8=d.getPONum();
						final SpannableString ss=new SpannableString(s8);
						ss.setSpan(new ClickableSpan() {
							@Override
							public void onClick(View view) {
								setContentView(R.layout.activity_podetail);
								showPOItems(s8);
								PO=s8;

							}
						},0,s8.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						main_tv.append(" M"+d.getArea()+" : ");
						main_tv.append(ss);
						String br="<br>";
					main_tv.append(Html.fromHtml(br));
						main_tv.setMovementMethod(LinkMovementMethod.getInstance());


				}

			}else{
				Toast.makeText(this, "Unsuccessful" , Toast.LENGTH_SHORT).show();
				movedQTY.setText("");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


	}



	/**
	 * check sku avaliable
	 * put or post
	 * log user action
	 *
	 *
	 * **/
	public void putupdateSmallPool() {

		SmallLocationPool stemp=getSmallLocationPoolbySKU(smallLocationPool.getSku());
		okhttp3.Request requestSmallData;
		Gson g = new Gson();
		String jsonString;

		//check sku avaliable in table
		if(stemp!=null) {

			if(Integer.valueOf(movedQTY.getText().toString())>stemp.getAssigned()){
				Toast.makeText(this, "unsuccessful!Please make sure smaller than total QTY" , Toast.LENGTH_SHORT).show();
				return;
			}

			// if avaliable update
			//calculate total
			stemp.setMoveIn(Integer.valueOf(movedQTY.getText().toString()));

			jsonString = g.toJson(stemp);
			RequestBody body = RequestBody.create(JSON, jsonString);

			requestSmallData = new okhttp3.Request.Builder()
					.url("http://" + GlobalInfo.IP_ADDRESS + ":8080/TPA/api/SmallLocationPools/PutSmallLocationPool/?id="+stemp.getId())
					.header("Authorization", "Bearer " + token)
					.put(body)
					.build();
			okhttp3.Response response2 = null;

			try {
				response2 = client.newCall(requestSmallData).execute();
				String oj = response2.body().string();
				if(response2.message().equals("OK")){
					Toast.makeText(this, "successful" , Toast.LENGTH_SHORT).show();
					movedQTY.setText("");
					main_tv.forceLayout();
					eventLog=new EventLog();
					eventLog.setMessage("successful");
					eventLog.setProcessName(serOkHttpClient.getSessionUsername());
					eventLog.setEventName("move to small area");
					eventLog.setMessage("Sku="+stemp.getSku()+" and QTY="+stemp.getMoveIn());
					eventLog.setEnterDate(EventLogProcess.getTime());
					new EventLogProcess().addEventLog(eventLog,token,client);

				}else{
					Toast.makeText(this, "Unsuccessful" , Toast.LENGTH_SHORT).show();

				}
			} catch (IOException e) {
				e.printStackTrace();
			}


		}else{
			if(Integer.valueOf(movedQTY.getText().toString())>smallLocationPool.getAssigned()){
				Toast.makeText(this, "unsuccessful!Please make sure smaller than total QTY" , Toast.LENGTH_SHORT).show();
				return;
			}
			stemp=new SmallLocationPool();
			stemp.setMoveIn(Integer.valueOf(movedQTY.getText().toString()));

			stemp.setSku(smallLocationPool.getSku());
			stemp.setUpc(smallLocationPool.getUpc());
			stemp.setAssigned(smallLocationPool.getAssigned());
			jsonString = g.toJson(stemp);
			RequestBody body = RequestBody.create(JSON, jsonString);


			requestSmallData = new okhttp3.Request.Builder()
					.url("http://" + GlobalInfo.IP_ADDRESS + ":8080/TPA/api/SmallLocationPools/PostSmallLocationPool")
					.header("Authorization", "Bearer " + token)
					.post(body)
					.build();
			okhttp3.Response response2 = null;

			try {
				response2 = client.newCall(requestSmallData).execute();
				String oj = response2.body().string();
				SmallLocationPool s = g.fromJson(oj, SmallLocationPool.class);
				if(response2.message().equals("OK")){
					Toast.makeText(this, "successful" , Toast.LENGTH_SHORT).show();
					movedQTY.setText("");
					main_tv.forceLayout();
					eventLog=new EventLog();
					eventLog.setMessage("successful");
					eventLog.setProcessName(serOkHttpClient.getSessionUsername());
					eventLog.setEventName("move to small area");
					eventLog.setMessage("Sku="+stemp.getSku()+" and QTY="+stemp.getMoveIn());
					eventLog.setEnterDate(EventLogProcess.getTime());
					new EventLogProcess().addEventLog(eventLog,token,client);


				}else{
					Toast.makeText(this, "Unsuccessful" , Toast.LENGTH_SHORT).show();

				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public SmallLocationPool getSmallLocationPoolbySKU(String sku){
		okhttp3.Request requestSmallData;
		Gson g =new Gson();

		requestSmallData = new okhttp3.Request.Builder()
				.url("http://"+ GlobalInfo.IP_ADDRESS+":8080/TPA/api/SmallLocationPools/GetSmallLocationPoolbySKU?sku="+sku)
				.header("Authorization", "Bearer " + token)
				.build();
		okhttp3.Response response2 = null;

		try {
			response2 = client.newCall(requestSmallData).execute();
			String oj=response2.body().string();
			SmallLocationPool s=g.fromJson(oj,SmallLocationPool.class);
			return s;

		} catch (IOException e) {
			e.printStackTrace();
		}


		return null;
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

	@Override
	public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

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
		//Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());

		if (event2.getX() - event1.getX() >100 && Math.abs(velocityX) > 200){
			setContentView(R.layout.activity_barcode);
			initView();
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
