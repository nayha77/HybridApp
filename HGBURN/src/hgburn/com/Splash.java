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
	                finish();      
	                //페이드 인 페이드 아웃 효과 res/anim/fadein, fadeout xml을 만들어 줘야 합니다.
	        		overridePendingTransition(R.layout.fadein, R.layout.fadeout);	                
	            }
	        }, 2000);		
	       	        
	}
	 public void onBackPressed(){} //splash 이미지 띄우는 과정에 백 버튼을 누를 수도 있다. 백버튼 막기
}
