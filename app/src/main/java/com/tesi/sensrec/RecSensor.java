package com.tesi.sensrec;


import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
    private CountDownTimer countDownTimer;
    private int numchar;
    private File check;
    private EditText textKeyPressed;
    private PrintWriter printWriter;
    private TextView textToWrite;
    private TextView textView2;
    private String TouchedView = String.valueOf(-1);
    private final String TAG = "RecSensor";
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
        testWord = getIntent().getStringExtra("words");
        Hand = getIntent().getStringExtra("hand");
    }

    private void createCountDown(long currenTime) {
        timer = currenTime;
        end = false;

        countDownTimer = new CountDownTimer(timer,100) {


            @Override
            public void onTick(long left) {
                lefTime = left;
                timer = left;

                updateCountDownText();

            }

            @Override
            public void onFinish() { //se scade il tempo

                end = true;

                failDialog("Time's up, retry");

            }
        };

    }

    private void setView() {
        setContentView(R.layout.test);

        textToWrite = findViewById(R.id.toWrite);
        textKeyPressed = findViewById(R.id.keyPressed);
        textToWrite.setVisibility(View.VISIBLE);
        textKeyPressed.setVisibility(View.VISIBLE);

        textKeyPressed.setRawInputType(InputType.TYPE_CLASS_TEXT);
        textKeyPressed.setCursorVisible(false);
        textKeyPressed.setFocusable(false);

        textToWrite.setText(testWord);
        textToWrite.setFocusable(false);
        textToWrite.setCursorVisible(false);

        ArrayList<View> allButtons;
        allButtons = (findViewById(R.id.keyboard)).getTouchables();
        for(View tmp : allButtons){
            if(!tmp.getTag().equals("disable")) {
                tmp.setOnTouchListener(handleTouch);
            }else{
                tmp.setVisibility(View.INVISIBLE);
            }

        }

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
            long time = System.currentTimeMillis();
            filename = User +  "-" + Hand + "-" + time + "-" + testWord + ".csv";
            Log.d(TAG, "onCreate: " + filename);
            check = new File(path + "/" + filename);
            //FilePath = path+"/"+filename;
            //check if file not exist then append header
            boolean v = check.exists();
            if (!v) {

                printWriter = new PrintWriter(new FileOutputStream(check, true));
                printWriter.print("Timestamp" + "," + "Ax" + "," + "Ay" + "," + "Az" + "," + "Gx" + "," + "Gy" + "," + "Gz" + "," + "Mx" +
                        "," + "My" + "," + "Mz" + "," + "Ox" + "," + "Oy" + "," + "Oz" +  "," + "Grx" + "," + "Gry" + "," + "Grz"+"," +"View"+","+ "Pressure" + "," + User +"," + Hand +","+ System.currentTimeMillis() + "\n");

            }

        }catch (IOException e){
            Log.i(TAG, "CATCH: " + e.getMessage());
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

                if (testWord.length() < 30){
                    timer = 30000;
                }else if (testWord.length() < 40){
                    timer = 40000;
                }else if (testWord.length() >= 40){
                    timer = 50000;
                }

                createCountDown(timer);

                if (check.delete()){
                    Log.i("pausepause", "resumeDialog cancellato");
                }else{
                    Log.i("pausepause", "resumeDialog non cancellato");
                }
                createNewFileCSV();
            }
        });
        AlertDialog alert = miaAlert.create();
        alert.show();
    }

    protected void onResume (){
        super.onResume();
        onSensoResume();
        Log.i("pausepause", "ONRESUME");
        if (pause) resumeDialog("THE GAME HAS BEEN INTERRUPTED","Please press ok to restart it");

    }

    private void onSensoResume() {
        mLastAccSet = false;
        mLastMagSet = false;
        if (mAcc != null){mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);}
        if (mGyr != null){mSensorManager.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_NORMAL);}
        if (mMag != null){mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_NORMAL);}
        if (mGrav != null){mSensorManager.registerListener(this, mGrav, SensorManager.SENSOR_DELAY_NORMAL);}
    }

    protected void onPause(){
        super.onPause();
        //closeRoutine();
        Log.i("pausepause", "ONPAUSE");
        pause = true;
        mSensorManager.unregisterListener(this);
        countDown = false;
        printWriter.close();

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
//            if (check.delete()){
//                Log.i("pausepause", "onPause cancellato");
//            }else{
//                Log.i("pausepause", "onPause non cancellato");
//            }
//        }

        TextView cDTextView = findViewById(R.id.text_view_countdown);
        cDTextView.setVisibility(View.INVISIBLE);

        if(countDownTimer != null){
            countDownTimer.cancel();
        }
        textKeyPressed.getText().clear();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //se torno indietro senza aver finito di scrivere cancella il file
        if (check.delete()){
            Log.i("pausepause", "onPause cancellato");
        }else{
            Log.i("pausepause", "onPause non cancellato");
        }

    }

    //cancella il file e lo salva nello storage
    private void closeRoutine() {
        mSensorManager.unregisterListener(this);
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
        countDownTimer.cancel();

        miaAlert.setCancelable(false);
        miaAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                printWriter.close();
                countDown = false;
                if (check.delete()){
                    Log.i("pausepause", "failDialog cancellato");
                }else{
                    Log.i("pausepause", "failDialog non cancellato");
                }
                finish();
            }
        });
        AlertDialog alert = miaAlert.create();
        alert.show();
    }

    private void updateCountDownText() {
        int seconds = (int) (timer / 1000) % 60;
        int cent = (int) (timer / 100) % 10;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", seconds, cent);

        TextView time = findViewById(R.id.text_view_countdown);
        time.setText(timeLeftFormatted);

        Log.d("Timer",String.valueOf(timer));
    }




    public View.OnTouchListener handleTouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                pressure = event.getPressure();

                SentenceMode(v);

            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                pressure = 0;

                checkString(v);

                TouchedView = "-1"; //setto a -1 quando alzo il dito
                Log.d("TAG", TouchedView);
            }
            return true;
        }

        private void checkString(View v) {
            if (!textToWrite.getText().toString().startsWith(textKeyPressed.getText().toString())) {
                TextView error = findViewById(R.id.textViewWrongKey);
                error.setVisibility(View.VISIBLE);
                textKeyPressed.setText(undo);

            } else if (textToWrite.getText().toString().equals(textKeyPressed.getText().toString())) {
                    countDownTimer.cancel();
                    finishDialog("Game Over");
            }else if (textToWrite.getText().toString().startsWith(textKeyPressed.getText().toString())){

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

    //viene chiamata ogni volta che premo un tasto sulla tastiera
    private void SentenceMode(View v) {
        TextView error = findViewById(R.id.textViewWrongKey);
        error.setVisibility(View.INVISIBLE);

        TouchedView = v.getTag().toString(); //quando premo imposta touchedView con l'ultimo tasto premuto
        undo = textKeyPressed.getText().toString(); //è la frase scritta prima di premere un nuovo tasto
        textKeyPressed.append(TouchedView);

        if(!countDown) {
            countDownTimer.start();
            countDown = true;
            TextView cDTextView = findViewById(R.id.text_view_countdown);
            cDTextView.setVisibility(View.VISIBLE);
            updateCountDownText();
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

        //Call CSV.Write to Insert the sensor's values, the tag of the TouchedView = v.getTag().toString(); element clicked(-1 if nothing clicked) and the path of the file created(check)
        //append Timestamp
        CSV.Write(SensorVal, TouchedView, printWriter, pressure);

    }
}




