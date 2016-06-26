package com.zju.lab.ct;

import com.zju.lab.ct.model.HttpCode;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by wuhaitao on 2016/4/7.
 */
public class HttpTest {
    public static void main(String[] args) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        URI uri = null;
        try {
            uri = new URIBuilder().setHost("10.13.81.185")
                    .setPort(8080)
                    .setPath("/login")
                    .setParameter("username", "admin").setParameter("password", "admin").build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        HttpPost post = new HttpPost(uri);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if(HttpCode.OK.getCode() == statusCode){
                HttpEntity entity = response.getEntity();
                System.out.println(entity.toString());
            } else{
                HttpEntity entity = response.getEntity();
                String log;
                if(entity != null){
                    System.out.println(entity.toString());
                }
            }

        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
