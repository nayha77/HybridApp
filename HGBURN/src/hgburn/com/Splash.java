package hgburn.com;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.splash);
         
	        Handler hd = new Handler();
	        hd.postDelayed(new Runnable() {
	 
	            @Override
	            public void run() {
	                finish();       // 3 ���� �̹����� �ݾƹ���
	            }
	        }, 3000);		
	}

}
