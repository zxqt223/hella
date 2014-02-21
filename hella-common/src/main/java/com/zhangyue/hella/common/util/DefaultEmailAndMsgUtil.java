package com.zhangyue.hella.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEmailAndMsgUtil {
	private static Logger log = LoggerFactory.getLogger(DefaultEmailAndMsgUtil.class);

	@SuppressWarnings("deprecation")
    public static boolean postEmail(String url, String content) throws ClientProtocolException, IOException {
		HttpPost httppost = new HttpPost(url);
		// 建立HttpPost对象
		httppost.setEntity(new StringEntity(content, HTTP.UTF_8));
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httppost);
		httpClient.getParams().setParameter("http.protocol.content-charset", "UTF-8");  
		if (response.getStatusLine().getStatusCode() == 200) {// 如果状态码为200,就是正常返回
			String result = EntityUtils.toString(response.getEntity());
			log.debug("Email result is " + result);
			return true;
		}
		return false;
	}


	public static boolean postMsg(String url) throws ClientProtocolException, IOException {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
		conn.setConnectTimeout(1000 * 30);
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			is = conn.getInputStream();
			isr = new InputStreamReader(is, "utf-8");
			br = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			log.debug("Msg result is " + sb.toString());
			if(sb.length()>0){
				return true;
			}
		} catch (Exception e) {
			return false;
		} finally {
			if (br != null)
				br.close();
			if (isr != null)
				isr.close();
			if (is != null)
				is.close();
			conn.disconnect();
		}
		return false;
	}
}
