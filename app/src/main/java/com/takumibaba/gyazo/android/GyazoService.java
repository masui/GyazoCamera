package com.takumibaba.gyazo.android;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by takumi on 2014/05/11.
 */
public class GyazoService extends IntentService {

    private static final String api = "http://gyazo.com/upload.cgi";
    private static final String id  = "0554d8f0577059d2a8eda175d889ebf9";
    private static RequestQueue mQueue;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GyazoService(String name) {
        super(name);
    }

    public GyazoService(){
        super("GyazoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mQueue = Volley.newRequestQueue(this.getApplicationContext());
        Uri uri = (Uri) intent.getParcelableExtra("data");

        String[] projection = {MediaStore.Images.Media.DATA};
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(uri, projection, null, null, null);
        int index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        c.moveToFirst();
        String path = Uri.parse(c.getString(index)).getLastPathSegment().toString();

//        InputStreamEntity entity = new InputStreamEntity(stream, fd.getStatSize());
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id);
        params.put("imagedata", "");
        Request req = new Request(Request.Method.POST, api, this.errorListener) {
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
        };
        mQueue.add(req);
    }

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
        }
    };
}