package hgburn.com;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// GCM 서비스가 전송한 인텐트(com.google.android.c2dm.intent.RECEIVE)를 수신
		// GcmIntentService가 인텐트를 처리하도록 명시적으로 지정
		ComponentName comp = new ComponentName(context.getPackageName(),
				GcmIntentService.class.getName());
		// 서비스를 시작. 해당 서비스가 실행되는 동안, 디바이스가 꺼지지 않도록 함
		startWakefulService(context, intent.setComponent(comp));
		setResultCode(Activity.RESULT_OK);
	}
}
