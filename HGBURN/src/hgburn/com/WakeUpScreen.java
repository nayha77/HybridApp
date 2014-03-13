package hgburn.com;

import android.content.Context;
import android.os.PowerManager;

/**
 * ��ũ���� ON�Ѵ�. ������ 4.2���ʹ� getWindows() ����
 * @author IKCHOI
 *
 */
public class WakeUpScreen {
 
    private static PowerManager.WakeLock wakeLock;
 
    /**
     * timeout�� �����ϸ�, �ڵ����� �������
     * @param context
     * @param timeout
     */
    public static void acquire(Context context, long timeout) {
 
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP  |
                PowerManager.FULL_WAKE_LOCK         |
                PowerManager.ON_AFTER_RELEASE
                , context.getClass().getName());
 
        if(timeout > 0)
            wakeLock.acquire(timeout);
        else
            wakeLock.acquire();
 
    }
 
    /**
     * �� �޼ҵ带 ����ϸ�, �ݵ�� release�� ����� ��
     * @param context
     */
    public static void acquire(Context context) {
        acquire(context, 0);
    }
 
    public static void release() {
        if (wakeLock.isHeld())
            wakeLock.release();
    }
}