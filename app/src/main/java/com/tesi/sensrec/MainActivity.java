package com.tesi.sensrec;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static android.view.View.INVISIBLE;
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
        setContentView(R.layout.activity_main);
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
        RadioGroup rg = findViewById(R.id.gamemode);
        rg.clearCheck();
    }

    public void StartRec(View view) {

        TextView Error = findViewById(R.id.textView7);
        EditText editlabel = findViewById(R.id.editText);
        //RadioGroup rg = findViewById(R.id.radioGroup);
        User = editlabel.getText().toString();
        if (User.equals("")) {
            Error.setVisibility(VISIBLE);

        } else {

                infodialog(view,"Instructions");
        }


    }


    public void onRadioButtonClicked(View view) {

        RadioGroup rg = findViewById(R.id.gamemode);
        Game = ((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText().toString();
        if(Game.equals("LETTER")){
            TextView info = findViewById(R.id.textView2);
            info.setText(R.string.mode1);
            randomWord(true);
        }else {
            TextView info = findViewById(R.id.textView2);
            info.setText(R.string.mode);
            randomWord(false);
        }

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
        RadioGroup hand = findViewById(R.id.gamemode);
        if (hand.getCheckedRadioButtonId() == -1) {
            // no radio buttons are checked
        } else {
            Button go = findViewById(R.id.button);
            go.setEnabled(true);
        }
    }


    private void infodialog(View v, String s) {
        final TextView Error = findViewById(R.id.textView7);
       final RadioGroup rg = findViewById(R.id.gamemode);

        Log.d("error", "mess");
        AlertDialog.Builder miaAlert = new AlertDialog.Builder(v.getContext());
        miaAlert.setTitle(s);
        if (Game.equals("LETTER")){
            miaAlert.setMessage(R.string.mode1);
        } else {
            miaAlert.setMessage(R.string.mode);
        }


        miaAlert.setCancelable(false);
        miaAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
        AlertDialog alert = miaAlert.create();
        alert.show();
    }

    private String randomWord(boolean gameMode) {
        if (!gameMode) {
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
        } else {
            try {

                br = new BufferedReader(new InputStreamReader(getAssets().open("testChar.txt")));
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
    }




}


