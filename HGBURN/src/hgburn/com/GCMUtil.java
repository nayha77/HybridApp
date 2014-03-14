package hgburn.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class GCMUtil {

	//private static final String URL_REGISTER = "http://hgburn.vps.phps.kr/push/register";
	private static final String URL_REGISTER = "http://hgburn.vps.phps.kr/push/register";
	private static final String URL_MESSAGE = "http://hgburn.vps.phps.kr/push/send";
	
	public static String register(String uuid, String phone, String regId) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("uuid", uuid);
		map.put("phone", phone);
		map.put("reg_id", regId);
		String result = post(URL_REGISTER, map);
		return result;
	}
	public static String registerWeb(String uuid, String phone, String regId,String webId ,String webP,String webType) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("uuid", uuid);
		map.put("phone", phone);
		map.put("reg_id", regId);
		map.put("webId", webId);
		map.put("webP", webP);
		map.put("webType", webType);		
		String result = post(URL_REGISTER, map);
		return result;
	}	
	
	public static String sendMessage(String message) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", message);
		String result = post(URL_MESSAGE, map);
		return result;
	}
	
	private static String post(String url, Map<String, String> params) {
		String result = "FAIL";

		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			
			List<NameValuePair> elements = new ArrayList<NameValuePair>();
			
			// 파라미터 설정 및 인코딩
			for(String key : params.keySet()) {
				String value = params.get(key);
				elements.add(new BasicNameValuePair(key, value));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(elements, "utf-8"));
			
			// 요청 송신 및 응답 수신
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			if(entity != null) {
				InputStream is = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				result = reader.readLine();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
