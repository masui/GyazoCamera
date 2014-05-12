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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

//    private static final String api = "http://gyazo.com/upload.cgi";
    private static final String api = "http://133.27.246.189:8000/upload";
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

            File f = new File(Environment.getExternalStorageDirectory().getPath()+"/gyazotest/");
            if(!f.exists())
                f.mkdir();
            String name = f.getAbsolutePath()+"/gyazo-test.jpg";
            FileOutputStream os = new FileOutputStream(name);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer);
            os.flush();
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        GyazoRequest req = new GyazoRequest(Request.Method.POST, api, errorListener, data);
        mQueue.add(req);

    }

    private void send(byte[] data){
        URLConnection c = null;
        try {
            c = new URL(api).openConnection();
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            ((HttpURLConnection)c).setRequestMethod("POST");
            c.setDoOutput(true);
            c.connect();

            OutputStream os = c.getOutputStream();
            os.write(new String("--"+boundary+"\r\n").getBytes("UTF-8"));
            os.write("Content-Disposition: form-data; name=\"id\" \r\n".getBytes());
            os.write("\r\n".getBytes());
            os.write(new String(id+"\r\n").getBytes());
            os.write(new String("--"+boundary+"\r\n").getBytes());
            os.write("Content-Disposition: form-data; name=\"imagedata\"; filename=\"imagedata\"".getBytes());
            os.write("\r\n".getBytes());
            os.write(data);
            os.write("\r\n".getBytes());
            os.write(new String("--"+boundary+"-- \r\n").getBytes());
            os.close();

            InputStream is = c.getInputStream();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class GyazoRequest extends Request{
        private Map<String, String> mParams = null;
        private static final String boundary = "----BOUNDARYBOUNDARY----";
        private byte[] mData = null;

        public GyazoRequest(int method, String url, Response.ErrorListener listener, byte[] data) {
            super(method, url, listener);
            mData = data;
        }

        @Override
        protected Response parseNetworkResponse(NetworkResponse response) {
            return null;
        }

        @Override
        protected void deliverResponse(Object response) {
            Log.d("deliver", "response");
        }

        @Override
        public int compareTo(Object another) {
            return 0;
        }

        @Override
        public Map<String, String> getParams(){
            return mParams;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> params = new HashMap<String, String>();
            params.put("Content-Type", "multipart/form-data; boundary="+boundary);
            return params;
        }

        @Override
        public byte[] getBody() throws AuthFailureError{
//              return mData;
//            ByteArrayOutputStream s = new ByteArrayOutputStream();
//            try {
//                s.write(new String("--"+boundary+"\r\n").getBytes());
//                s.write("Content-Disposition: form-data; name=\"id\" \r\n".getBytes());
//                s.write("\r\n".getBytes());
//                s.write(new String(id+"\r\n").getBytes());
//                s.write(new String("--"+boundary+"\r\n").getBytes());
//                s.write("Content-Disposition: form-data; name=\"imagedata\"; filename=\"imagedata\"".getBytes());
//                s.write("\r\n".getBytes());
//                s.write(mData);
//                s.write("\r\n".getBytes());
//                s.write(new String("--"+boundary+"-- \r\n").getBytes());
//                s.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return s.toByteArray();
//            byte[] a = s.toByteArray();

            String d = null;
            d = new String(mData, Charset.defaultCharset());
            String body = "--"+boundary+"\r\n"
                    +"Content-Disposition: form-data; name=\"id\" \r\n"
                    +"\r\n"
                    +id+"\r\n"
                    +"--"+boundary+"\r\n"
                    +"Content-Disposition: form-data; name=\"imagedata\"; filename=\"imagedata\" \r\n"
                    +"\r\n"
                    +d+"\r\n"
                    +"--"+boundary+"-- \r\n";
            Log.d("body", body);
            return body.getBytes();
        }
    }

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
        }
    };

}