package ru.mirea.goryacheva.httpurlconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView ipTextView;
    private TextView countryTextView;
    private TextView regionTextView;
    private TextView cityTextView;
    private TextView postalcodeTextView;

    private final String url = "https://ip.seeip.org/geoip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipTextView = findViewById(R.id.textView);
        countryTextView = findViewById(R.id.textView7);
        regionTextView = findViewById(R.id.textView8);
        cityTextView = findViewById(R.id.textView9);
        postalcodeTextView = findViewById(R.id.textView10);
    }

    public void onClick(View view) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkinfo = null;
        if (connectivityManager != null) { networkinfo = connectivityManager.getActiveNetworkInfo(); }
        if (networkinfo != null && networkinfo.isConnected()) {
            new DownloadPageTask().execute(url); // запускаем в новом потоке
        } else { Toast.makeText(this, "Нет интернета", Toast.LENGTH_SHORT).show(); }
    }

    private class DownloadPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ipTextView.setText("Загружаем...");
        }

        @Override
        protected String doInBackground(String... urls) {
            try { return downloadIpInfo(urls[0]); }
            catch (IOException e) { e.printStackTrace(); return "error"; }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(MainActivity.class.getSimpleName(), result);

            try {
                JSONObject responseJson = new JSONObject(result);

                String ip = responseJson.getString("ip");
                String country = responseJson.getString("country");
                String region = responseJson.getString("region");
                String city = responseJson.getString("city");
                String postal_code = responseJson.getString("postal_code");

                ipTextView.setText(ip);
                countryTextView.setText(country);
                regionTextView.setText(region);
                cityTextView.setText(city);
                postalcodeTextView.setText(postal_code);

                Log.d(MainActivity.class.getSimpleName(), ip);
            } catch (JSONException e) { e.printStackTrace(); }
            super.onPostExecute(result);
        }
    }

    private String downloadIpInfo(String address) throws IOException {
        InputStream inputStream = null;
        String data = "";

        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read = 0;

                while ((read = inputStream.read()) != -1) { bos.write(read); }

                byte[] result = bos.toByteArray();
                bos.close();
                data = new String(result);
            } else { data = connection.getResponseMessage() + " . Error Code : " + responseCode; }

            connection.disconnect();
        } catch (MalformedURLException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
        finally {
            if (inputStream != null) { inputStream.close(); }
        }
        return data;
    }
}