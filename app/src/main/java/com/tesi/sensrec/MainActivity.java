package com.tesi.sensrec;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;


import static android.view.View.VISIBLE;



public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String TAG = "Main Activity";
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String line;
    private BufferedReader br;
    private String User;
    private String Hand;
    private int cont = 0;

   // private int  cont = 0;
    /* Checks if the app has permission to write to device storage
      If the app does not has permission then the user will be prompted to grant permissions
     @param activity*/

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        Button go = findViewById(R.id.playButton);
        go.setEnabled(false);
        startService();
    }

    public void startService() {
        startService(new Intent(this, AnsyncService.class));
    }
    // Stop the service
    public void stopService() {
        stopService(new Intent(this, AnsyncService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        RadioGroup rg = findViewById(R.id.hand);
        rg.clearCheck();
        EditText us = findViewById(R.id.editTextUsername);
        us.setText("");
        TextView error = findViewById(R.id.errorMessage);
        error.setVisibility(View.INVISIBLE);
        Button go = findViewById(R.id.playButton);
        go.setEnabled(false);
    }

    public void StartRec(View view) {

        TextView error = findViewById(R.id.errorMessage);
        EditText editlabel = findViewById(R.id.editTextUsername);

        User = editlabel.getText().toString();
        if (User.isEmpty()) {
            error.setVisibility(VISIBLE);
        } else {
            infodialog(view);
        }


    }

    public void hand (View view) {

        randomWord();

        RadioGroup rg = findViewById(R.id.hand);
        Hand = ((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText().toString();

        if (rg.getCheckedRadioButtonId() == -1) {
            // no radio buttons are checked
        } else {
            Button go = findViewById(R.id.playButton);
            go.setEnabled(true);
        }
    }

    private void infodialog(View v) {
        Log.d("error", "mess");

        AlertDialog.Builder infoAlert = new AlertDialog.Builder(v.getContext());
        infoAlert.setTitle("Instructions");
        infoAlert.setMessage(R.string.alertDialogMessage);


        infoAlert.setCancelable(false);
        infoAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent data = new Intent("com.tesi.sensrec");
                data.putExtra("User", User);
                data.putExtra("words", line);
                data.putExtra("hand", Hand);
                stopService();
                startActivity(data);
            }

        });

        infoAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });


        AlertDialog alert = infoAlert.create();
        alert.show();
    }

    private String randomWord() {

        try {

            br = new BufferedReader(new InputStreamReader(getAssets().open("TestWords.txt")));
            cont = 0;
            int ran = new Random().nextInt(20) /*+ 1*/;
            line = br.readLine();
            while (line != null && cont != ran) {
                line = br.readLine();
                cont++;
            }
            Log.d(TAG, "n" + cont);
            Log.d(TAG, "w: " + line);

        } catch (Exception e) {
            Log.e(TAG, "onCreate: " + e.getMessage());
        }
        return line;
    }

}


