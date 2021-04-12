package com.tesi.sensrec;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
    private String smartphone, smartphoneShared;
    private int cont = 0;

    public SharedPreferences sharedPreferences;

   // private int  cont = 0;
    /* Checks if the app has permission to write to device storage
      If the app does not has permission then the user will be prompted to grant permissions
     @param activity*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        smartphoneShared = sharedPreferences.getString("smartphoneName", null);

        EditText editSmartphone = findViewById(R.id.editTextSmartph);

        if (smartphoneShared != null){
            editSmartphone.setText(smartphoneShared);
        }

        Log.i("smartsmart", "onCreate " + smartphoneShared);

        verifyStoragePermissions(this);

        Button go = findViewById(R.id.playButton);
        go.setEnabled(false);

        startService();
    }

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

        if (requestCode == REQUEST_EXTERNAL_STORAGE){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        }

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
        EditText editSmartphone = findViewById(R.id.editTextSmartph);
        String shared = sharedPreferences.getString("smartphoneName", null);
        if (shared != null){
            editSmartphone.setText(shared);
        }
        Log.i("smartsmart", "onResume " + smartphoneShared + " " + shared);
//        editSmartphone.setText("");
//        TextView error = findViewById(R.id.errorMessage);
//        error.setVisibility(View.INVISIBLE);
        Button go = findViewById(R.id.playButton);
        go.setEnabled(false);
    }

    public void StartRec(View view) {

        //TextView error = findViewById(R.id.errorMessage);
        EditText editTextUsername = findViewById(R.id.editTextUsername);
        EditText editTextSmartphone = findViewById(R.id.editTextSmartph);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        User = editTextUsername.getText().toString();
        smartphone = editTextSmartphone.getText().toString();

        if (User.isEmpty() || smartphone.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.Error), Toast.LENGTH_LONG).show();
            //error.setVisibility(View.VISIBLE);
        } else if (smartphone.contains("-") || smartphone.contains(".") || User.contains("-") || User.contains(".")){
            Toast.makeText(this, getResources().getString(R.string.noSpecial), Toast.LENGTH_LONG).show();
        }else {
            editor.putString("smartphoneName", smartphone).apply();
            Log.i("smartsmart", smartphone);
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
                //data.putExtra("smartphone", smartphoneShared);
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
            int ran = new Random().nextInt(30) /*+ 1*/;
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.counters) {
            countersDialog();
        }
        return MainActivity.super.onOptionsItemSelected(item);
    }

    private void countersDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);

        final View view  = getLayoutInflater().inflate(R.layout.counters_dialog,null);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }

}


