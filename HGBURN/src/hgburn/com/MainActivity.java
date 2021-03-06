package hgburn.com;

import java.io.IOException;

import org.apache.http.util.EncodingUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity  {
	
    private static final String TAG = "HGBURN_TAG";

	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	// GCM 서비스를 설정 할 때, 복사해둔  Project Number
	private static final String SENDER_ID = "160756605987";
	
	private WebView mWebView;
	private BackPressCloseHandler backPressCloseHandler;
	
	private final Handler handler = new Handler();
	
	private GoogleCloudMessaging gcm;
	private Context context;
	private String regId;
	private TelephonyManager tManager;
	

	public boolean isNetworkConnected(Context context){
	    boolean isConnected = false;

	    ConnectivityManager manager = 
	        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

	    if (mobile.isConnected() || wifi.isConnected()){
	        isConnected = true;
	    }else{
	        isConnected = false;
	    }
	    return isConnected;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if( !isNetworkConnected(this) ){
		    new AlertDialog.Builder(this)
		    .setIcon(android.R.drawable.ic_dialog_alert)
		    .setTitle("네트워크 연결 오류").setMessage("네트워크 연결 상태 확인 후 다시 시도해 주십시요.")
		    .setPositiveButton("확인", new DialogInterface.OnClickListener()
		    {
		        @Override
		        public void onClick( DialogInterface dialog, int which )
		        {
		            finish();
		        }
		    }).show();
		    
		    startActivity(new Intent(this,Splash.class));  //메인로딩 3초
		    
		    return;
		} 
		
		startActivity(new Intent(this,Splash.class));  //메인로딩 3초
		
		context = this.getApplicationContext();
		
		// 구글 플레이 서비스에 의존하는 앱은 항상 해당 서비스 기능을 사용하기 전에 
		// 해당 디바이스가 구글 플레이 서비스와 호환 확인하여야 함.
		// 예제에서는 onCreate()와 onResume()에서 모두 확인
		if(checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			// 앱이 GCM 서버로부터 메시지를 수신하려면 먼저, GCM 서버에 메시지를 수신 할 앱을 등록해야 함.
			// GCM 서버에 앱을 등록하면 registration ID가 발급되고, 발급된 ID를 프리퍼런스에 저장하여 다음 번에 재사용함.
			regId = getRegistrationId(context);
			// 앱이 새로 설치되거나, 앱이 업데이트 된 경우에는 registration ID를 새롭게 받아옴
			if(regId.isEmpty()) {
				registerInBackground();
			} 
			// 웹 애플리케이션에서 해당 registration ID를 삭제했을 경우가 있으므로
			// 앱이 새로 시작될 때마다 registration ID를 웹 sendRegistrationIdToBackend 에 재전송함.
			sendRegistrationIdToBackend();
		}
		
		mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true); // 웹뷰에서 자바스크립트실행가능
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(new AndroidBridge(), "HybridApp");   // Bridge 인스턴스 등록 , //  Android 4.2 @JavascriptInterface 어노테이션 추가해 주어야 함 
        
        String url = "http://nayha.dlinkddns.com/";
        Bundle extras = getIntent().getExtras();
        
        if(extras != null)
        	url = extras.getString("url");        
        	
        mWebView.loadUrl(url);
        Log.i(TAG, "====url=" + url);	
        mWebView.setWebViewClient(new HelloWebViewClient());  // WebViewClient 지정	
        
        backPressCloseHandler = new BackPressCloseHandler(this); //백버튼 두 번 눌러 종료하기
    }
	
	private void registerInBackground() {

		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
		
				String msg = "";
				if(gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(context);
				}
				try {
					regId = gcm.register(SENDER_ID);
					msg = "디바이스가 등록되었습니다. \nregistration ID=" + regId;
					storeRegistrationId(context, regId);
				} catch (IOException e) {
					msg = "에러 : " + e.getMessage();
				}
				return msg;
			}

	/*		@Override
			protected void onPostExecute(String result) {
				textDisplay.append(result + "\n");
			}*/

		}.execute();
	}

	private void storeRegistrationId(Context context, String regId) {
		// 발급받은 registration_id를 프리퍼런스에 저장한다.
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	// HP UUID /  DATA SAVE
	private void savePreferences(String uuid,String phone){
	SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("UUID", uuid);
	editor.putString("PHONE",phone);
	editor.commit();
	}	
	
	private void sendRegistrationIdToBackend() {
		// registration ID를 savePreferences 에 저장하는 코드
		tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				//String result = GCMUtil.register(tManager.getDeviceId(), tManager.getLine1Number(), regId);
				savePreferences(tManager.getDeviceId(), tManager.getLine1Number());
	    		return "";
			}
/*			@Override
			protected void onPostExecute(String result) {
				textDisplay.append(result + "\n");
			}*/
		}.execute();
	}
	
	private String getRegistrationId(Context context) {

		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if(registrationId.isEmpty()) {
			Log.i(TAG, "Registration ID가 없습니다.");  
			//textDisplay.append("Registration ID가 없습니다.\n");
			return "";
		}
		
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if(registeredVersion != currentVersion) {
			Log.i(TAG, "앱의 버전이 변경되었습니다.");  
			//textDisplay.append("앱의 버전이 변경되었습니다.\n");
			return "";
		}
		return registrationId;
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("패키지 이름을 알 수 없습니다. " + e);
		}
	}

	private SharedPreferences getGCMPreferences(Context context) {
		return this.getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();
	}
	
	// 디바이스 체크
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resultCode != ConnectionResult.SUCCESS) {
			if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, 
						this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "이 디바이스는 구글 플레이 서비스를 지원하지 않습니다.");  
				//textDisplay.append("이 디바이스는 구글 플레이 서비스를 지원하지 않습니다\n");
				finish();
			}
			return false;
		}
		return true;
	}
	
	// 백버튼 인식  === > 웹뷰도 백 적용 11
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    // 백버튼 종료 처리
    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
    
    
    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }	
    
	// 웹 에서 App 호출 ( web 로그인이 완료시 해당 스크립트 호출)
	private class AndroidBridge {
		@JavascriptInterface
    	public void setMessage(final String id,final String pwd,final String userType) { 
    		handler.post(new Runnable() {
			    	public void run() {
			    		//GCMUtil.registerWeb(getUUID(),getPHONE(),regId,id,pwd,userType);	
			    		//다시 Post 로 던지기
			    		String url = "http://hgburn.vps.phps.kr/push/register";
		    	        String postData = "uuid="+getUUID()+"&phone="+getPHONE()+"&webP="+pwd+"&webId="+id+"&webType="+userType+"&reg_id="+regId;
			    		//String postData = "userId="+id+"&userType="+userType+"&appId=4444";
			    		Log.d("HybridApp_postData", postData);
			    		mWebView.postUrl(url,  EncodingUtils.getBytes(postData, "BASE64"));			    		
			    	}
    		});
    	}
    }  	
    // UUID 값 불러오기
	private String getUUID(){
	SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
	return pref.getString("UUID", "");
	}
    // PHONE 값 불러오기
	private String getPHONE(){
	SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
	return pref.getString("PHONE", "");
	}	

/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/

/*	@Override
	public void onClick(View v) {

		if(v == findViewById(R.id.btnSend)) {
			String message = editMessage.getText().toString();
			if(message.isEmpty()) {
				Toast.makeText(getApplicationContext(), "전송 할 메시지를 입력하세요.", Toast.LENGTH_SHORT).show();
				return;
			}
			// 사용자가 입력한 메시지를 GCM 서버에 전송
			new AsyncTask<String, Void, String>() {

				@Override
				protected String doInBackground(String... params) {
					String result = GCMUtil.sendMessage(params[0]);	
					return result;
				}

				@Override
				protected void onPostExecute(String result) {
					textDisplay.append("전송 결과 : " + result + "\n");
				}

			}.execute(message);
		} else if(v == findViewById(R.id.btnClear)) {
			textDisplay.setText("");
			editMessage.setText("");
		}
	}*/


  
}
