package com.example.cypher;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity3 extends AppCompatActivity {

    private LinearLayout buttonLayout;
    private PackageManager packageManager;
    private TextView tv;
    private EditText ipv4Input;
    private TextView resultText;

    // Define the TensorFlow Lite interpreter
    private Interpreter tflite;

    // Array of known permissions for your model
    private String[] permissionsInDataset = {
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.BLUETOOTH",
            "android.permission.CAMERA",
            // Add your list of known permissions here
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        buttonLayout = findViewById(R.id.buttonLayout);
        packageManager = getPackageManager();
        tv = findViewById(R.id.permissionsTextView);

        // Load the TensorFlow Lite model during initialization
        loadTFLiteModel();
        retrieveAppPermissions();
    }

    private void loadTFLiteModel() {
        try {
            FileChannel fileChannel;
            try (FileInputStream inputStream = new FileInputStream("model.tflite")) {
                fileChannel = inputStream.getChannel();
            }
            long startOffset = 0; // Change this if needed
            long declaredLength = fileChannel.size(); // Use the file size as declared length
            MappedByteBuffer tfliteModel = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

            // Initialize the TensorFlow Lite interpreter
            Interpreter.Options options = new Interpreter.Options();
            tflite = new Interpreter(tfliteModel, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void retrieveAppPermissions() {
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo packageInfo : installedPackages) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                String packageName = packageInfo.packageName;

                Button appButton = new Button(this);
                appButton.setText(appName);
                appButton.setTag(packageName);
                appButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String selectedPackage = (String) view.getTag();
                        displayPermissions(selectedPackage);
                    }
                });

                buttonLayout.addView(appButton);
            }
        }
    }

    private void displayPermissions(String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] permissions = packageInfo.requestedPermissions;

            // Perform inference with the TensorFlow Lite model
            float[] inputTensor = convertPermissionsToTestArray(permissions, permissionsInDataset);
            String outputTensor = new String(); // Modify based on your model's output shape

            //tflite.run(inputTensor, outputTensor);

            // Process the output (post-processing logic)

            // Display the result in the UI
            String result;
            if (outputTensor == "benign")
                result = "Result: " + "Malicious";
            else
                result = "Result: " + "Not Malicious";
            //Toast.makeText(this,  outputTensor, Toast.LENGTH_SHORT).show();
            tv.setText(result);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    private float[] convertPermissionsToTestArray(String[] permissionsFromApp, String[] permissionsInDataset) {
        float[] testArray = new float[permissionsInDataset.length];

        // Create a set of permissions obtained from the app for efficient lookup
        Set<String> appPermissionsSet = new HashSet<>(Arrays.asList(permissionsFromApp));

        for (int i = 0; i < permissionsInDataset.length; i++) {
            // Check if the permission exists in the set obtained from the app
            if (appPermissionsSet.contains(permissionsInDataset[i])) {
                // Assign a value of 1 if the app has the permission
                testArray[i] = 1.0f;
            } else {
                // Assign a value of 0 if the app does not have the permission
                testArray[i] = 0.0f;
            }
        }

        return testArray;
    }

}
