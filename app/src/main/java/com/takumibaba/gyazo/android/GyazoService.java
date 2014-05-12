package com.takumibaba.gyazo.android;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by takumi on 2014/05/11.
 */
public class GyazoService extends IntentService {

    private static final String gyazoAddress = "http://gyazo.com/upload.cgi";
    private static final String lindaAddress = "http://linda.babascript.org";
//    private static final String api = "http://133.27.246.189:8000/upload";
    private static final String id = "0554d8f0577059d2a8eda175d889ebf9";
    private static final String boundary = "----BOUNDARYBOUNDARY----";
    private static RequestQueue mQueue;
    private byte[] mBuffer = null;

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
        Network network = new BasicNetwork(new HttpClientStack(AndroidHttpClient.newInstance("Gyazo-Android/2.0")));
        mQueue = Volley.newRequestQueue(this.getApplicationContext());
        Uri uri = (Uri) intent.getParcelableExtra("data");

        InputStream stream = null;
        byte[] data = null;
        Bitmap bitmap = null;
        try{
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            stream = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(stream, null, options);
            stream.close();

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer);
            data = buffer.toByteArray();

//            File f = new File(Environment.getExternalStorageDirectory().getPath()+"/gyazotest/");
//            if(!f.exists())
//                f.mkdir();
//            String name = f.getAbsolutePath()+"/gyazo-test.jpg";
//            FileOutputStream os = new FileOutputStream(name);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
//            os.flush();
//            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        send(data);
//        GyazoRequest req = new GyazoRequest(Request.Method.POST, api, errorListener, data, id);
//        mQueue.add(req);

    }

    private void send(byte[] data){
        HttpPost post = new HttpPost(gyazoAddress);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.setCharset(Consts.UTF_8);

        builder.addTextBody("id", id, ContentType.create("text/plain", Charset.defaultCharset()));
        builder.addBinaryBody("imagedata", data);

        post.setEntity(builder.build());
        HttpClient client = new DefaultHttpClient();
        try{
            client.execute(post, new ResponseHandler<Object>() {
                @Override
                public Object handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader r = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                    String line = null;
                    while ((line = r.readLine()) != null){
                        stringBuilder.append(line);
                    }
                    String url = stringBuilder.toString();
                    HttpPost post = new HttpPost(lindaAddress+"/baba");
                    MultipartEntityBuilder b = MultipartEntityBuilder.create();
                    b.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    b.setCharset(Consts.UTF_8);
                    JSONObject json = new JSONObject();
                    try {
                        json.put("service", "gyazo");
                        json.put("url", url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    b.addPart("tuple", new StringBody(json.toString()));
//                    b.addTextBody("tuple", json.toString());
                    post.setEntity(b.build());
                    new DefaultHttpClient().execute(post, new ResponseHandler<Object>() {
                        @Override
                        public Object handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                            return null;
                        }
                    });
                    return null;
                }
            });
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}