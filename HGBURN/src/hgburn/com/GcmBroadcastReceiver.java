package hgburn.com;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
	private static String TAG = "GcmBroadcastReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		// GCM 서비스가 전송한 인텐트(com.google.android.c2dm.intent.RECEIVE)를 수신
		// GcmIntentService가 인텐트를 처리하도록 명시적으로 지정
		Log.i(TAG, "====context=" + ToStringBuilder.reflectionToString(context, ToStringStyle.MULTI_LINE_STYLE));	
		Log.i(TAG, "====intent=" + ToStringBuilder.reflectionToString(intent, ToStringStyle.MULTI_LINE_STYLE));			
		ComponentName comp = new ComponentName(context.getPackageName(),GcmIntentService.class.getName() );

		
		// 메시지 수신시 진동 발생
       Vibrator vibrator = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
        
        //화면 키키
        WakeUpScreen.acquire(context, 10000); 
		
		// 서비스를 시작. 해당 서비스가 실행되는 동안, 디바이스가 꺼지지 않도록 함
		startWakefulService(context, intent.setComponent(comp));
		setResultCode(Activity.RESULT_OK);
	}
}
