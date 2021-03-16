package com.tesi.sensrec;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Locale;


public class RecSensor extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAcc, mGyr, mMag, mGrav;
    private float[]SensorVal= new float[15];
    private String filename;

    //Array of Last accelleration and magnetic value for Orientation
    private float[] mLastAcc = new float[3];
    private float[] mLastMag = new float[3];
    private boolean mLastAccSet = false;
    private boolean mLastMagSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float pressure;
    private String User;
    private String Game;
    private String testWord;
    private String line;
    private String TestName;
    private String undo;
    private String Hand;
    private long timer;
    private long tmpTimer;
    private long lefTime;
    private int cont;
    private CountDownTimer ct;
    private int numchar;
    private File check;
    private EditText editText;
    private PrintWriter printWriter;
    private TextView textView;
    private TextView textView2;
    private String TouchedView= String.valueOf(-1);
    private String TAG = "RecSensor";
    private BufferedReader br;
    private long time = 0;
    private boolean countDown= false;
    private boolean end = false;
    private ArrayList<String> listWords = new ArrayList<>();
    private boolean Ct;
    private boolean finish;
    private boolean pause;
    private ArrayList<File> Files = new ArrayList<>();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setData();
        setView();
        setSensor();
        createNewFileCSV();
        countDown = false;
    }

    private void setSensor() {
        mSensorManager= (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAcc=  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyr=  mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMag=  mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGrav= mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    private void setData() {
        User = getIntent().getStringExtra("User");
        //Game = getIntent().getStringExtra("Game");
        testWord = getIntent().getStringExtra("words");
        Hand = getIntent().getStringExtra("hand");

    }

    private void createCountDown(long currenTime) {
        timer = currenTime;
        end = false;

        ct= new CountDownTimer(timer,100) {


            @Override
            public void onTick(long left) {
                lefTime = left;
                timer = left;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                //textView.setBackgroundColor(Color.parseColor("#6AFB92"));

                end = true;
//                if (Game.equals("LETTER")){
//                    countDown = false;
//                    numchar++;
//                    if (numchar < testWord.length()) {
//                        editText.setText(String.valueOf(testWord.charAt(numchar)));
//                        updateCountDownText();
//                    }else if(numchar == testWord.length()){
//                        finishDialog("game over");
//                    }
//                }else{
//                    failDialog("time's up, retry");
//                }
                failDialog("Time's up, retry");

            }
        };

    }

    private void setView() {
        setContentView(R.layout.test);
//        if(Game.equals("SENTENCE")) {
//            textView =findViewById(R.id.editText2);
//            editText = findViewById(R.id.editText);
//            textView.setVisibility(View.VISIBLE);
//            editText.setVisibility(View.VISIBLE);
//        }else{
//            textView2 = findViewById(R.id.editText2);
//            textView =findViewById(R.id.editText4);
//            editText = findViewById(R.id.editText3);
//            textView.setVisibility(View.VISIBLE);
//            editText.setVisibility(View.VISIBLE);
//            textView2.setVisibility(View.VISIBLE);
//            textView2.setFocusable(false);
//            textView2.setCursorVisible(false);
//            numchar = 0;
//            char first = testWord.charAt(numchar);
//            editText.setText(String.valueOf(first));
//        }

        textView = findViewById(R.id.toWrite);
        editText = findViewById(R.id.keyPressed);
        textView.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);

        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        //editText.setTextIsSelectable(true);
        editText.setCursorVisible(false);
        editText.setFocusable(false);
        textView.setText(testWord);
        textView.setFocusable(false);
        textView.setCursorVisible(false);

        ArrayList<View> allButtons;
        allButtons = (findViewById(R.id.keyboard)).getTouchables();
        for(View tmp : allButtons){
            if(!tmp.getTag().equals("disable")) {
                tmp.setOnTouchListener(handleTouch);
            }else{
                tmp.setVisibility(View.INVISIBLE);
            }

        }
//        if (Game.equals("SENTENCE")){
//            if (testWord.length()<30){
//                timer = 30000;
//            }else if (testWord.length()<40){
//                timer = 40000;
//            }else if (testWord.length()>=40){
//                timer = 50000;
//            }
//            createCountDown(timer);
//        }else {
//            timer = 2100;
//            tmpTimer = timer;
//        }

        if (testWord.length() < 30){
            timer = 30000;
        }else if (testWord.length() < 40){
            timer = 40000;
        }else if (testWord.length() >= 40){
            timer = 50000;
        }
        createCountDown(timer);

    }

    private void createNewFileCSV() {
        try {
            File path = null;
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q){
                path = Environment.getExternalStorageDirectory();
            } else if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.Q){
                path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            }
            Log.i("api30api30", "IN CREATE PATH: " + path);
            long time = System.currentTimeMillis();
            filename= User +  "-" + Hand + "-" + time + "-" + testWord + ".csv";
            Log.d(TAG, "onCreate: " + filename);
            check = new File(path + "/" + filename);
            //FilePath = path+"/"+filename;
            //check if file not exist then append header
            Log.i("api30api30", "IN CREATE: " + check + check.exists());
            boolean v = check.exists();
            if (!v) {
                //check.createNewFile();

                printWriter = new PrintWriter(new FileOutputStream(check, true));
                printWriter.print("Timestamp" + "," + "Ax" + "," + "Ay" + "," + "Az" + "," + "Gx" + "," + "Gy" + "," + "Gz" + "," + "Mx" +
                        "," + "My" + "," + "Mz" + "," + "Ox" + "," + "Oy" + "," + "Oz" +  "," + "Grx" + "," + "Gry" + "," + "Grz"+"," +"View"+","+ "Pressure" + "," + User +"," + Hand +","+ System.currentTimeMillis() + "\n");
                //printWriter.close();

            }

//            File path = Environment.getExternalStorageDirectory();
//            long time = System.currentTimeMillis();
////            filename= User +  "-"+Hand+"-"+time+"-"+Game+"-"+testWord+".csv";
//            filename= User +  "-" + Hand + "-" + time + "-" + testWord + ".csv";
//            Log.d(TAG, "onCreate: " + filename);
//            check = new File(path + "/" + filename);
//            //FilePath = path+"/"+filename;
//            //check if file not exist then append header
//            Log.i("api30api30", "IN CREATE: " + check + check.exists());
//            boolean v = check.exists();
//            if (!v) {
//                Log.i("api30api30", "IN CREATE: " + printWriter);
//                //check.createNewFile();
//
//                printWriter = new PrintWriter(new FileOutputStream(check, true));
//                printWriter.print("Timestamp" + "," + "Ax" + "," + "Ay" + "," + "Az" + "," + "Gx" + "," + "Gy" + "," + "Gz" + "," + "Mx" +
//                        "," + "My" + "," + "Mz" + "," + "Ox" + "," + "Oy" + "," + "Oz" +  "," + "Grx" + "," + "Gry" + "," + "Grz"+"," +"View"+","+ "Pressure" + "," + User +"," + Hand +","+ System.currentTimeMillis() + "\n");
//                //printWriter.close();
//
//            }

        }catch (IOException e){
            Log.i("api30api30", "CATCH: " + e.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void resumeDialog(String title, String message) {
        Log.d("error", "mess");
        AlertDialog.Builder miaAlert = new AlertDialog.Builder(this);
        miaAlert.setTitle(title);
        miaAlert.setMessage(message);

        miaAlert.setCancelable(false);
        miaAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                pause = false;
//                if (Game.equals("LETTER")){
//                    numchar = 0;
//                    timer = 2100;
//                    tmpTimer = timer;
//                    editText = findViewById(R.id.editText3);
//                    char first = testWord.charAt(numchar);
//                    editText.setText(String.valueOf(first));
//                }else{
//                    if (testWord.length()<30){
//                        timer = 30000;
//                    }else if (testWord.length()<40){
//                        timer = 40000;
//                    }else if (testWord.length()>=40){
//                        timer = 50000;
//                    }
//
//                    createCountDown(timer);
//                }

                if (testWord.length()<30){
                    timer = 30000;
                }else if (testWord.length()<40){
                    timer = 40000;
                }else if (testWord.length()>=40){
                    timer = 50000;
                }

                createCountDown(timer);

                check.delete();
                createNewFileCSV();
            }
        });
        AlertDialog alert = miaAlert.create();
        alert.show();
    }



    protected void onResume (){
        super.onResume();
        onSensoResume();
        if (pause) resumeDialog("THE GAME HAS BEEN INTERRUPTED","please press ok to restart it");

    }

    private void onSensoResume() {
        mLastAccSet=false;
        mLastMagSet=false;
        if (mAcc!=null){mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);}
        if (mGyr!=null){mSensorManager.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_NORMAL);}
        if (mMag!=null){mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_NORMAL);}
        if (mGrav!=null){mSensorManager.registerListener(this, mGrav, SensorManager.SENSOR_DELAY_NORMAL);}
    }

    protected void onPause(){
        super.onPause();
        //closeRoutine();
        pause = true;
        mSensorManager.unregisterListener(this);
        countDown = false;
        printWriter.close();
        Log.i("api30api30", "IN ONPAUSE: " + printWriter);
//        if (Game.equals("LETTER")) {
//            TextView cDTextView = findViewById(R.id.text_view_countdown);
//            cDTextView.setVisibility(View.INVISIBLE);
//            textView2.setText("");
//        }else{
//            TextView cDTextView = findViewById(R.id.text_view_countdown);
//            cDTextView.setVisibility(View.INVISIBLE);
//        }

        TextView cDTextView = findViewById(R.id.text_view_countdown);
        cDTextView.setVisibility(View.INVISIBLE);

        if(ct!= null){
            ct.cancel();
        }
        editText.getText().clear();

    }

    private void closeRoutine() {
        mSensorManager.unregisterListener(this);
        Log.i("api30api30", "IN CLOSER: " + printWriter);
        printWriter.close();
        for(final File elem : Files){
            Uri file = Uri.fromFile(elem);
            StorageReference riversRef = storageRef.child("rawCsv/" + file.getLastPathSegment());
            riversRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    elem.delete();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RecSensor.this, "Failed " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        }
        finish();
    }

    public void failDialog (String s) {
        Log.d("error", "mess");
        mSensorManager.unregisterListener(this);
        AlertDialog.Builder miaAlert = new AlertDialog.Builder(this);
        miaAlert.setTitle(s);
        ct.cancel();

        miaAlert.setCancelable(false);
        miaAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.i("api30api30", "IN FAILD: " + printWriter);
                printWriter.close();
                countDown = false;
                check.delete();
                finish();
            }
        });
        AlertDialog alert = miaAlert.create();
        alert.show();
    }

    private void updateCountDownText() {
        int seconds = (int) (timer / 1000) % 60;
        int cent = (int) (timer / 100) % 10;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", seconds,cent);
        TextView time = findViewById(R.id.text_view_countdown);
        time.setText(timeLeftFormatted);
        Log.d("Timer",String.valueOf(timer));
//        if(Game.equals("LETTER")) {
//            if (end) {
//                ct.cancel();
//                tmpTimer = tmpTimer - 200;
//                timer = tmpTimer;
//                createCountDown(timer);
//                ct.start();
//            }
//        }
    }




    public View.OnTouchListener handleTouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                pressure = event.getPressure();
//                if(Game.equals("SENTENCE")){
//                    SentenceMode(v);
//                }else{
//                    letterMode(v);
//                }
                SentenceMode(v);

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                pressure = 0;
//                if (Game.equals("SENTENCE")){
//                    checkString(v);
//                }else{
//                }

                checkString(v);

                TouchedView = "-1";
                Log.d("TAG", TouchedView);
            }
            return true;
        }

//        private void letterMode(View v) {
//            TouchedView = v.getTag().toString();
//            textView2.append(TouchedView);
//            if (!textView.getText().toString().startsWith(textView2.getText().toString())){
//                failDialog("you typed the wrong key, retry");
//            }
//            if (numchar==0) {
//                createCountDown(timer);
//                ct.start();
//                countDown = true;
//                TextView cDTextView = findViewById(R.id.text_view_countdown);
//                cDTextView.setVisibility(View.VISIBLE);
//                updateCountDownText();
//            }
//        }

        private void checkString(View v) {
            if (!textView.getText().toString().startsWith(editText.getText().toString())) {
                TextView error = findViewById(R.id.textViewWrongKey);
                error.setVisibility(View.VISIBLE);
                editText.setText(undo);

            } else if (textView.getText().toString().equals(editText.getText().toString())) {
                    ct.cancel();
                    finishDialog("Game Over");
            }else if (textView.getText().toString().startsWith(editText.getText().toString())){

            }
        }


    };

    public void finishDialog(String s) {
        Log.d("error", "mess");
        AlertDialog.Builder miaAlert = new AlertDialog.Builder(this);
        miaAlert.setTitle(s);

        miaAlert.setCancelable(false);
        miaAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                countDown = false;
                Files.add(check);
                closeRoutine();
            }
        });
        AlertDialog alert = miaAlert.create();
        alert.show();
    }


    private void SentenceMode(View v) {
        TextView error = findViewById(R.id.textViewWrongKey);
        error.setVisibility(View.INVISIBLE);
        TouchedView = v.getTag().toString();
        undo = editText.getText().toString();
        editText.append(TouchedView);
        if(!countDown) {
            ct.start();
            countDown = true;
            TextView cDTextView = findViewById(R.id.text_view_countdown);
            cDTextView.setVisibility(View.VISIBLE);
            updateCountDownText();
            //textView.setBackgroundColor(Color.parseColor("#ff7b5a"));
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.info) {

        }
        return RecSensor.super.onOptionsItemSelected(item);
    }

    public void onSensorChanged(SensorEvent event) {
        //When one of sensors change its values these will be saved in SensorVal
        // Log.d("TAG", "SensorChanged : " + TouchedView);

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, SensorVal, 0, event.values.length);
            System.arraycopy(event.values, 0, mLastAcc, 0, event.values.length);
            mLastAccSet = true;

        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, SensorVal, 3, event.values.length);
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, SensorVal, 6, event.values.length);
            System.arraycopy(event.values, 0, mLastMag, 0, event.values.length);
            mLastMagSet = true;

        }

        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            System.arraycopy(event.values, 0, SensorVal, 12, event.values.length);
            //System.arraycopy(event.values, 0, mLastMag, 0, event.values.length);
            //mLastMagSet = true;

        }

        if (mLastAccSet && mLastMagSet){
            SensorManager.getRotationMatrix(mR,null , mLastAcc, mLastMag);
            SensorManager.getOrientation(mR, mOrientation);
            System.arraycopy(mOrientation, 0, SensorVal, 9, event.values.length);

        }

        //Log.i("api30api30", Arrays.toString(SensorVal) + ", touchedView: " + TouchedView + ", printW: " + printWriter + ", press: " + pressure);

        //Call CSV.Write to Insert the sensor's values, the tag of the TouchedView = v.getTag().toString(); element clicked(-1 if nothing clicked) and the path of the file created(check)
        //append Timestamp
        CSV.Write(SensorVal, TouchedView, printWriter, pressure);

    }
}




