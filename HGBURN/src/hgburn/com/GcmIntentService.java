package hgburn.com;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

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
				sendNotification("전송 에러 : " + extras.toString());
			} else if(GoogleCloudMessaging.MESSAGE_TYPE_DELETED	.equals(messageType)) {
				sendNotification("서버에서 메시지 삭제 : " + extras.toString());
			} else if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				sendNotification(extras.getString("text"));
			}
		}
		// wake lock을 해제
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
	private void sendNotification(String msg) {
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("HGBURN에서")
			.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
			.setContentText(msg);
		
		builder.setContentIntent(contentIntent);
		
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}
}
