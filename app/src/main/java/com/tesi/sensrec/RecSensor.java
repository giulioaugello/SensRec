package com.tesi.sensrec;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;


public class RecSensor extends AppCompatActivity implements SensorEventListener {

    private final String TAG = "RecSensor";

    private SensorManager mSensorManager;
    private Sensor mAcc, mGyr, mMag, mGrav;

    private float[]SensorVal= new float[15]; //contiene tutti i valori da stampare
    private ArrayList<StringBuilder> rowsToLoad;

    //Array of Last accelleration and magnetic value for Orientation
    private float[] mLastAcc = new float[3];
    private float[] mLastMag = new float[3];

    private boolean mLastAccSet = false;
    private boolean mLastMagSet = false;

    private float[] mR = new float[9]; //serve per getRotation (in onSensorChanged)
    private float[] mOrientation = new float[3];
    private float pressure;

    private String User;
    private String testWord;
    private String Hand;
    private String smartphone;

    private String undo;
    private long timer;
    private long lefTime;
    private CountDownTimer countDownTimer;
    private long time = 0;
    private boolean countDown= false;
    private boolean end = false;
    private boolean pause;

    private EditText textKeyPressed;
    private TextView textToWrite;
    private TextView textView2;
    private String TouchedView = String.valueOf(-1);

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private File check;
    private String filename;
    private PrintWriter printWriter;
    private BufferedReader br;
    private ArrayList<File> Files = new ArrayList<>();

    private long countWordDone;
    private String[] arrOfStr;
    private String tempText;
    private String wordDone;
    private SpannableString ssb;
    private BackgroundColorSpan bcsYellow;
    private ArrayList<Long> startLong;
    private ArrayList<Long> endLong;
    private ArrayList<ArrayList<Long>> startTimeWrongKey;
    private ArrayList<ArrayList<Long>> endTimeWrongKey;
    private String firstCharacter;
    private ArrayList<StringBuilder> lastWord;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setData();
        setView();
        setSensor();
        createNewFileCSV();
        countDown = false;

        arrOfStr = new String[2];
        tempText = textToWrite.getText().toString();
        wordDone = "";
        countWordDone = timer;
        ssb = new SpannableString (textToWrite.getText().toString());
        bcsYellow = new BackgroundColorSpan(Color.YELLOW);
        startTimeWrongKey = new ArrayList<>();
        endTimeWrongKey = new ArrayList<>();
        rowsToLoad = new ArrayList<>();
        lastWord = new ArrayList<>();
    }

    private void setSensor() {
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyr = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGrav = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    private void setData() {
        User = getIntent().getStringExtra("User");
        testWord = getIntent().getStringExtra("words");
        Hand = getIntent().getStringExtra("hand");
    }

    private void createCountDown(long currenTime) {
        timer = currenTime;
        end = false;

        countDownTimer = new CountDownTimer(currenTime,100) {


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
        //Log.i("wordword", "NOOOOOOO");
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

            SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
            smartphone = sharedPreferences.getString("smartphoneName", null);
            String upperSmartphone = smartphone.toUpperCase();

            filename = User +  "-" + upperSmartphone +  "-" + Hand + "-" + time + "-" + testWord + ".csv";
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

    private void changeFile(File oldfile, String fName, String removeTerm){ //16 campo della lettera

        File path = null;
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q){
            path = Environment.getExternalStorageDirectory();
        } else if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.Q){
            path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        }

        int position = 16;
        String tempFile = "temp.csv";
        File newFile = new File(path + "/" + tempFile);

        String currentLine;
        String[] data;

        try { //189-221-312

            FileWriter fileWriter = new FileWriter(tempFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter1 = new PrintWriter(bufferedWriter);

            String oldFileNameFull = path + "/" + fName;
            FileReader fileReader = new FileReader(oldFileNameFull);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((currentLine = bufferedReader.readLine()) != null){

                data = currentLine.split(",");

                if (!(data[position].equalsIgnoreCase(removeTerm))){
                    printWriter1.println(currentLine);
                }

            }

            printWriter1.flush();
            printWriter1.close();
            fileReader.close();
            bufferedReader.close();
            bufferedWriter.close();
            fileWriter.close();

            if (oldfile.delete()){
                Log.i("filefile", "old file delete");
            }
            File dump = new File(oldFileNameFull);
            if (newFile.renameTo(dump)){
                Log.i("filefile", "new file rename");
            }


        }catch (Exception e){
            Log.i("filefile", "Catch: " + e.getMessage());
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
                    Log.i(TAG, "resumeDialog delete");
                }else{
                    Log.i(TAG, "resumeDialog not delete");
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
        Log.i(TAG, "ONRESUME");
        if (pause) {
            resumeDialog("The game has been interrupted","Please press ok to restart it");
        }

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
        Log.i(TAG, "ONPAUSE");
        pause = true;
        mSensorManager.unregisterListener(this);
        countDown = false;
        printWriter.close();

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
//            if (check.delete()){
//                Log.i(TAG, "onPause cancellato");
//            }else{
//                Log.i(TAG, "onPause non cancellato");
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
        //go back without finishing writing, it deletes the file
        if (check.delete()){
            Log.i(TAG, "onBackPressed delete");
        }else{
            Log.i(TAG, "onBackPressed not delete");
        }

    }

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
                    Log.i(TAG, "failDialog delete");
                }else{
                    Log.i(TAG, "failDialog not delete");
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

        Log.d("Timer", String.valueOf(timer));
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

    };




    private void checkString(View v) {
        if (!textToWrite.getText().toString().startsWith(textKeyPressed.getText().toString())) {
            TextView error = findViewById(R.id.textViewWrongKey);
            error.setVisibility(View.VISIBLE);

            Button retryButton = findViewById(R.id.retry_button);
            retryButton.setVisibility(View.VISIBLE);
            //textKeyPressed.setText(undo);

            textKeyPressed.setText(wordDone);


            mSensorManager.unregisterListener(this);
            countDownTimer.cancel();
            countDown = false;

            createCountDown(countWordDone);

            rowsToLoad.clear();

            ArrayList<View> allButtons;
            allButtons = (findViewById(R.id.keyboard)).getTouchables();
            for(View tmp : allButtons){
                Button tmpButton = (Button) tmp;
                tmpButton.setEnabled(false);
            }

            retryButton.setOnClickListener(v1 -> {

                if(!countDown) {
                    onSensoResume();
                    countDownTimer.start();
                    countDown = true;
                    TextView cDTextView = findViewById(R.id.text_view_countdown);
                    cDTextView.setVisibility(View.VISIBLE);
                    updateCountDownText();
                    retryButton.setVisibility(View.INVISIBLE);

                    for(View tmp : allButtons){
                        Button tmpButton = (Button) tmp;
                        tmpButton.setEnabled(true);
                    }
                }
            });

        } else {

            textKeyPressed.addTextChangedListener(mTextEditorWatcher);

            if (textToWrite.getText().toString().equals(textKeyPressed.getText().toString())) {

                for (StringBuilder stringBuilder: rowsToLoad){
                    printWriter.print(stringBuilder);
                }
                rowsToLoad.clear();

                countDownTimer.cancel();
                finishDialog("Game Over");

            } else if (textKeyPressed.getText().toString().endsWith(" ")){

                for (StringBuilder stringBuilder: rowsToLoad){
                    printWriter.print(stringBuilder);
                }
                rowsToLoad.clear();
            }
        }
//        else if (textToWrite.getText().toString().equals(textKeyPressed.getText().toString())) {
//
//            countDownTimer.cancel();
//            finishDialog("Game Over");
//
//        }else if (textToWrite.getText().toString().startsWith(textKeyPressed.getText().toString())){
//
//            textKeyPressed.addTextChangedListener(mTextEditorWatcher);
//
//        }
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            if (textToWrite.getText().toString().startsWith(s.toString())){
                ssb.setSpan(bcsYellow, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textToWrite.setText(ssb);
            }
        }

        public void afterTextChanged(Editable s) {
        }
    };

    //viene chiamata ogni volta che premo un tasto sulla tastiera
    private void SentenceMode(View v) {
        TextView error = findViewById(R.id.textViewWrongKey);
        error.setVisibility(View.INVISIBLE);

        TouchedView = v.getTag().toString(); //quando premo imposta touchedView con l'ultimo tasto premuto
        undo = textKeyPressed.getText().toString(); //è la frase scritta prima di premere un nuovo tasto
        textKeyPressed.append(TouchedView);
//        Log.i("wordword", TouchedView + " ... " + undo);


        firstCharacter = tempText.substring(0, 1);

        //quando premo spazio e ho effettivamente uno spazio da premere
        if(TouchedView.equals(" ") && textToWrite.getText().toString().startsWith(textKeyPressed.getText().toString())){

            arrOfStr = tempText.split(" ", 2); //divide sempre in 2 tempText quando premo lo spazio
            wordDone = wordDone + arrOfStr[0] + " "; //frase precedente all'errore
            tempText = arrOfStr[1]; //frase che devo ancora scrivere

            countWordDone = timer; //salvo il timer quando premo spazio per reinserirlo se sbaglio

            Log.i("wordword", Arrays.toString(arrOfStr) + " AAA " + tempText + " AAA " + wordDone + " AAA " + arrOfStr[0]);

        } else if (TouchedView.equals(firstCharacter) && textToWrite.getText().toString().startsWith(textKeyPressed.getText().toString())){

            startLong = new ArrayList<>();

        }else if (!textToWrite.getText().toString().startsWith(textKeyPressed.getText().toString())){

            endLong = new ArrayList<>();

        }

        if(!countDown) {
            //Log.i("wordword", "SIIII");
            //onSensoResume();
            countDownTimer.start();
            countDown = true;
            TextView cDTextView = findViewById(R.id.text_view_countdown);
            cDTextView.setVisibility(View.VISIBLE);
            updateCountDownText();
        }
    }

    public void finishDialog(String s) {
        Log.d("error", "mess");

        //chiamare la funzione di modifica file se il booleano è true

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
        //Log.i(TAG, "SensorChanged: " + TouchedView);

        long time = System.currentTimeMillis();

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

        StringBuilder csvRow = CSV.generateCSVRow(SensorVal, TouchedView, pressure, time);
        rowsToLoad.add(csvRow);

        //Call CSV.Write to Insert the sensor's values, the tag of the TouchedView = v.getTag().toString(); element clicked(-1 if nothing clicked) and the path of the file created(check)
        //append Timestamp
        //CSV.Write(SensorVal, TouchedView, printWriter, pressure, time);

    }
}




