package com.example.cypher;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.media.MediaPlayer;
import android.widget.VideoView;
import android.net.Uri;
public class MainActivity extends AppCompatActivity {
    private Button detectUnusedButton;
    private EditText ipv4Input;
    private TextView resultText;
    private EditText urlEditText;
    private Button checkButton;
    private Button resolveButton;
    private TextView resultTextView;
    private static final int USAGE_STATS_PERMISSION_REQUEST = 1001;
    private List<String> unusedApps;
    private ArrayAdapter<String> appListAdapter;
    private VideoView vv;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.video);
        // Create a FrameLayout to hold the VideoView and ImageView
//        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
//        startActivity(intent);
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Create a FrameLayout to hold the VideoView
//        FrameLayout frameLayout = new FrameLayout(this);
//        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameL            ayout.LayoutParams.MATCH_PARENT
//        ));


//        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.start_video.mp4; // Replace "your_video" with the actual video filename
//        videoView2.setVideoURI(Uri.parse(videoPath));
//
//        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                // Stop video playback
//                videoView2.stopPlayback();
//
//                // Proceed with the next part of your app here
//                // For example, you can start a new activity or perform any desired action
//            }
//        });
//        videoView2.start();
        setContentView(R.layout.activity_main);

        ipv4Input = findViewById(R.id.ipv4Input);
        resultText = findViewById(R.id.resultText);
        Button connectButton = findViewById(R.id.packages);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the button click event here
                // You can open the new XML layout or perform any other action
                // For example, to open a new activity with the new XML layout:
                Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                startActivity(intent);
            }
        });

        urlEditText = findViewById(R.id.urlEditText);
        checkButton = findViewById(R.id.checkButton);
        resolveButton = findViewById(R.id.resolveButton);
        resultTextView = findViewById(R.id.resultTextView);

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = urlEditText.getText().toString();

                if (isValidUrl(url)) {
                    resultTextView.setText("Valid URL");
                    resolveButton.setVisibility(View.VISIBLE);
                } else {
                    resultTextView.setText("Invalid URL");
                    resolveButton.setVisibility(View.GONE);
                }
            }
        });

        resolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = urlEditText.getText().toString();
                new ResolveIPAddressTask().execute(url);
            }
        });
    }
    public void checkIP(View view) {
        String ipAddress = ipv4Input.getText().toString().trim();
        if (!ipAddress.isEmpty()) {
            new CheckIPTask().execute(ipAddress);
        }
    }

    private class CheckIPTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String apiKey = "HFixQGLASOFWZYxV0YNERQo8qZPaO4P6"; // Replace with your IPQualityScore API key
            String ipAddress = params[0];
            String apiUrl = "https://ipqualityscore.com/api/json/ip/" + apiKey + "/" + ipAddress;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Log.d("API Response", response.toString());
                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String fraudScore = jsonObject.getString("fraud_score");

                    // Check if "ISP" field exists in the JSON response (with uppercase letters)
                    if (jsonObject.has("ISP")) {
                        String isp = jsonObject.getString("ISP");
                        resultText.setText("IP: " + ipv4Input.getText().toString() + "\nISP: " + isp + "\nFraud Score: " + fraudScore);
                    } else {
                        resultText.setText("IP: " + ipv4Input.getText().toString() + "\nFraud Score: " + fraudScore);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    resultText.setText("Error parsing JSON response.");
                }
            } else {
                resultText.setText("Error checking IP address.");
            }
        }

    }
    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivityForResult(intent, USAGE_STATS_PERMISSION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == USAGE_STATS_PERMISSION_REQUEST) {
            if (hasUsageStatsPermission()) {
                detectUnusedApps();
            } else {
                Toast.makeText(this, "Usage stats permission not granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void detectUnusedApps() {
        long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000); // 1 week ago
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, oneWeekAgo, System.currentTimeMillis());

        if (usageStatsList != null && !usageStatsList.isEmpty()) {
            PackageManager packageManager = getPackageManager();

            for (UsageStats usageStats : usageStatsList) {
                String packageName = usageStats.getPackageName();
                try {
                    ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);

                    // Exclude system apps
                    if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        unusedApps.add(packageManager.getApplicationLabel(appInfo).toString());
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        Collections.sort(unusedApps);
        appListAdapter.notifyDataSetChanged();
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // Regular expression pattern for a valid URL
        String URL_PATTERN = "^((http|https|ftp)://)?([A-Za-z0-9.-]+)(:[0-9]+)?(/.*)?$";
        Pattern pattern = Pattern.compile(URL_PATTERN);
        Matcher matcher = pattern.matcher(url);

        return matcher.matches();
    }

    private class ResolveIPAddressTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];

            try {
                URL parsedUrl = new URL(url);
                String host = parsedUrl.getHost();
                InetAddress[] addresses = InetAddress.getAllByName(host);

                for (InetAddress address : addresses) {
                    if (address instanceof Inet4Address) {
                        return "IPv4 Address: " + address.getHostAddress();
                    }
                }

                return "No IPv4 address found for " + host;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Malformed URL: " + e.getMessage();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return "Unable to resolve the IP address for " + url + ": " + e.getMessage();
            } catch (Exception ex) {
                ex.printStackTrace();
                return "An error occurred: " + ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String ipAddressInfo) {
            resultTextView.setText(ipAddressInfo);
        }
    }
}
