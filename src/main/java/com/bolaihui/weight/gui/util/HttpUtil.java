package com.bolaihui.weight.gui.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by fz on 2015/8/30.
 */
public class HttpUtil {

    private static CloseableHttpClient httpClient = HttpClients.createDefault();

    public static String httpGet(String url, Map headers) throws IOException {

        HttpGet httpGet = new HttpGet(url);
        Iterator it = headers.keySet().iterator();
        while(it.hasNext()){
            String key = it.next().toString();
            httpGet.setHeader(new BasicHeader(key, headers.get(key).toString()));
        }
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        String result = null;
        try {
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null){
                    result = EntityUtils.toString(entity, "utf-8");
                }
                EntityUtils.consume(entity);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            httpResponse.close();
        }
        return result;
    }

    public static String httpPost(String url, Map headers, Map params) throws IOException {

        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        Iterator it = params.keySet().iterator();
        while(it.hasNext()){
            String key = it.next().toString();
            pairs.add(new BasicNameValuePair(key, params.get(key).toString()));
        }
        UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(pairs, "utf-8");
        Iterator it2 = headers.keySet().iterator();
        while(it2.hasNext()){
            String key = it2.next().toString();
            httpPost.setHeader(new BasicHeader(key, headers.get(key).toString()));
        }
        httpPost.setEntity(uefEntity);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        String result = null;
        try{
            HttpEntity entity = httpResponse.getEntity();
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                if (entity != null){
                    result = EntityUtils.toString(entity, "utf-8");
                }
                EntityUtils.consume(entity);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            httpResponse.close();
        }
        return result;
    }

    public static void main(String[] args) throws IOException {

        String url = "http://www.zimaoda.com";
        Map headers = new HashMap();
        Map params = new HashMap();
        String result = HttpUtil.httpPost(url, headers, params);
        System.out.println(result);
    }
}
