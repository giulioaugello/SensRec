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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;


import static android.view.View.VISIBLE;



public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String TAG = "Main Activity";
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String Game;
    private String checked;
    private String line;
    private BufferedReader br;
    JSONArray tests = new JSONArray();
    private String User;
    private String Hand;
    private ArrayList<String> List = new ArrayList<>();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        verifyStoragePermissions(this);
        Button go = findViewById(R.id.button);
        go.setEnabled(false);
        startService();
    }

    public void startService() {
        startService(new Intent(this, ansyncService.class));
    }
    // Stop the service
    public void stopService() {
        stopService(new Intent(this, ansyncService.class));
    }



    @Override
    protected void onResume() {
        super.onResume();
        RadioGroup rg = findViewById(R.id.hand);
        rg.clearCheck();
    }

    public void StartRec(View view) {

        TextView error = findViewById(R.id.errorMessage);
        EditText editlabel = findViewById(R.id.editTextUsername);
        //RadioGroup rg = findViewById(R.id.radioGroup);
        User = editlabel.getText().toString();
        if (User.equals("")) {
            error.setVisibility(VISIBLE);
        } else {
            infodialog(view);
        }


    }


    public void onRadioButtonClicked(View view) {

//        RadioGroup rg = findViewById(R.id.gamemode);
//        Game = ((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText().toString();
//        if(Game.equals("LETTER")){
//            TextView info = findViewById(R.id.descriprionSentence);
//            info.setText(R.string.mode1);
//            randomWord(true);
//        }else {
//            TextView info = findViewById(R.id.descriprionSentence);
//            info.setText(R.string.mode);
//            randomWord(false);
//        }
        randomWord();

        RadioGroup hand = findViewById(R.id.hand);
        if (hand.getCheckedRadioButtonId() == -1) {
            // no radio buttons are checked
        } else {
            Button go = findViewById(R.id.button);
            go.setEnabled(true);
        }

    }



    public void hand (View view) {
        RadioGroup rg = findViewById(R.id.hand);
        Hand = ((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText().toString();
        //RadioGroup hand = findViewById(R.id.gamemode);
        if (rg.getCheckedRadioButtonId() == -1) {
            // no radio buttons are checked
        } else {
            Button go = findViewById(R.id.button);
            go.setEnabled(true);
        }
    }


    private void infodialog(View v) {
        //final TextView Error = findViewById(R.id.errorMessage);
       //final RadioGroup rg = findViewById(R.id.gamemode);

        Log.d("error", "mess");
        AlertDialog.Builder infoAlert = new AlertDialog.Builder(v.getContext());
        infoAlert.setTitle("Instructions");
//        if (Game.equals("LETTER")){
//            infoAlert.setMessage(R.string.mode1);
//        } else {
//            infoAlert.setMessage(R.string.mode);
//        }
        infoAlert.setMessage("Press OK to start playing or CANCEL to go back!");


        infoAlert.setCancelable(false);
        infoAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent data = new Intent("com.tesi.sensrec");
                data.putExtra("User", User);
                data.putExtra("Game",Game);
                data.putExtra("words",line);
                data.putExtra("hand",Hand);
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


