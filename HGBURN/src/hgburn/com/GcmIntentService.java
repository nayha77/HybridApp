package hgburn.com;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	private static String TAG = "GcmIntentService";
	private static final int NOTIFICATION_ID = 1;
	private NotificationManager notificationManager;
	
	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// 브로드캐스트 리시버가 전달한 인텐트를 처리
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		
		if(!extras.isEmpty()) {
			if(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				sendNotification("전송 에러 : " + extras.toString(),"error");
			} else if(GoogleCloudMessaging.MESSAGE_TYPE_DELETED	.equals(messageType)) {
				sendNotification("서버에서 메시지 삭제 : " + extras.toString(),"del");
			} else if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				sendNotification(extras.getString("message"),extras.getString("params"));
			}
		}
		// wake lock을 해제
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
	private void sendNotification(String msg,String params) {
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		//Intent intent = new Intent(this, MainActivity.class);
		Log.i(TAG, "====param=" + params);	
		Intent notificationIntent = new Intent(this,MainActivity.class);
		notificationIntent.putExtra("url", "http://vavacar.com/"+params);
		//notificationIntent.putExtra("param", param);
		//notificationIntent.setDataAndType(Uri.parse("www.naver.com"),"URLTEST");
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("vavacar.com")
			.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
			.setContentText(msg)
			.setAutoCancel(true)
			.setContentIntent(contentIntent);
		
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}
}
