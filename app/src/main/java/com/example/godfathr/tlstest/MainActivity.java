package com.example.godfathr.tlstest;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

//import com.android.volley.Request;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.VolleyLog;


public class MainActivity extends AppCompatActivity {

    //the loading Dialog
    ProgressDialog pDialog;
    // Textview to show data
    TextView tlsversion, yearId, stamp, recordId, yearNumber;
    // background image
    ImageView weatherBackground;
    // JSON object that contains weather information
    JSONObject jsonObj;

    public static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            try {
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                //specs.add(ConnectionSpec.COMPATIBLE_TLS);
                //specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception exc) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
            }
        }

        return client;
    }

    private OkHttpClient getNewHttpClient() {
        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .cache(null)
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS);

        return enableTls12OnPreLollipop(client).build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // link the XML layout to this JAVA class
        setContentView(R.layout.activity_main);

        //link graphical items to variables
        recordId = (TextView) findViewById(R.id.recordId);
        yearId = (TextView) findViewById(R.id.yearId);
        stamp = (TextView) findViewById(R.id.stamp);
        yearNumber = (TextView) findViewById(R.id.yearNumber);
        tlsversion = (TextView) findViewById(R.id.tlsversion);


        // prepare the loading Dialog
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait ...");
        pDialog.setCancelable(false);

        // Check if Internet is working
        if (!isNetworkAvailable(this)) {
            // Show a message to the user to check his Internet
            Toast.makeText(this, "Please check your Internet connection", Toast.LENGTH_LONG).show();
        }
        else{
            new DownloadTimeStamp().execute("@request_url");
        }
    }

    ////////////////////make request in the background
    private static class DownloadTimeStamp extends com.example.godfathr.tlstest.DownloadTimeStamp {
        @Override
        protected Response doInBackground(String... strings) {
            int count = strings.length;
            //long totalSize = 0;
            for (int i = 0; i < count; i++) {
                OkHttpClient _client = new OkHttpClient();

                //HttpUrl.Builder urlBuilder = HttpUrl.parse("https://localhost/Timestamp/api/DateTimeRecords").newBuilder();
                HttpUrl.Builder urlBuilder = HttpUrl.parse(strings[i].toString()).newBuilder();

                String url = urlBuilder.build().toString();

                Request request = new Request.Builder()
                        .url(url)
                        .build();
                try {
                    Response result = _client.newCall(request).execute();
                    return result;
                } catch (IOException e) {
                    Log.e("Unable to make request", e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }
    }

    ////////////////////check internet connection
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
