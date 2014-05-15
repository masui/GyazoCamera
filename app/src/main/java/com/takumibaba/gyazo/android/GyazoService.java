package com.takumibaba.gyazo.android;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by takumi on 2014/05/11.
 */
public class GyazoService extends IntentService {

    private static final String gyazoAddress = "http://upload.gyazo.com/upload.cgi";
    private static final String lindaAddress = "http://linda.babascript.org";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GyazoService(String name) {
        super(name);
    }

    public GyazoService() {
        super("GyazoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Uri uri = (Uri) intent.getParcelableExtra("data");

        try{
            InputStream stream = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
            stream.close();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer);
            send(buffer.toByteArray());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void send(byte[] data){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String gyazoid = pref.getString("gyazoID", "");
        HttpPost gyazoPost = new HttpPost(gyazoAddress);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.setCharset(Consts.UTF_8);
        builder.addTextBody("id", gyazoid, ContentType.create("text/plain", Charset.defaultCharset()));
        builder.addBinaryBody("imagedata", data);
        gyazoPost.setEntity(builder.build());
        HttpClient client = new DefaultHttpClient();
        try{
            client.execute(gyazoPost, new ResponseHandler<Object>() {
                @Override
                public Object handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader r = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                    String line = null;
                    while ((line = r.readLine()) != null){
                        stringBuilder.append(line);
                    }
                    String url = stringBuilder.toString();
                    sendTuple(url);
                return null;
                }
            });
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTuple(String url){
        HttpPost lindaPost = new HttpPost(lindaAddress+"/baba");
        JSONObject tuple = new JSONObject();
        try {
            tuple.put("service", "gyazo");
            tuple.put("url", url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
        list.add(new BasicNameValuePair("tuple", tuple.toString()));
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(list, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        lindaPost.setEntity(entity);
        try {
            new DefaultHttpClient().execute(lindaPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}