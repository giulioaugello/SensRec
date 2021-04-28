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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    private String smartphone;
    private int cont = 0;

    private TextView aCount, bCount, cCount, dCount, eCount, fCount, gCount, hCount, iCount, jCount, kCount, lCount, mCount, nCount, oCount;
    private TextView pCount, qCount, rCount, sCount, tCount, uCount, vCount, wCount, xCount, yCount, zCount, spaceCount;
    private Button resetCounters;
    public static int a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, space;


    public SharedPreferences sharedPreferences, countersSharedPreferences;

   // private int  cont = 0;
    /* Checks if the app has permission to write to device storage
      If the app does not has permission then the user will be prompted to grant permissions
     @param activity*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        String userShared = sharedPreferences.getString("userName", null);
        String smartphoneShared = sharedPreferences.getString("smartphoneName", null);

        EditText editSmartphone = findViewById(R.id.editTextSmartph);
        if (smartphoneShared != null){
            editSmartphone.setText(smartphoneShared);
        }

        EditText editUs = findViewById(R.id.editTextUsername);
        if (userShared != null){
            editUs.setText(userShared);
        }

        verifyStoragePermissions(this);

        Button go = findViewById(R.id.playButton);
        go.setEnabled(false);

        startService();

        countersSharedPreferences = getSharedPreferences("counters", MODE_PRIVATE);

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
        String sharedU = sharedPreferences.getString("userName", null);
        if (sharedU != null){
            us.setText(sharedU);
        }

        EditText editSmartphone = findViewById(R.id.editTextSmartph);
        String shared = sharedPreferences.getString("smartphoneName", null);
        if (shared != null){
            editSmartphone.setText(shared);
        }

        Log.i("smartsmart", "onResume " + sharedU + " " + shared);

        Button go = findViewById(R.id.playButton);
        go.setEnabled(false);
    }

    public void StartRec(View view) {

        EditText editTextUsername = findViewById(R.id.editTextUsername);
        EditText editTextSmartphone = findViewById(R.id.editTextSmartph);

        String userSh = sharedPreferences.getString("userName", null);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        User = editTextUsername.getText().toString().trim();
        smartphone = editTextSmartphone.getText().toString().trim();

        Pattern p = Pattern.compile("[^a-zA-Z0-9\\s]"); //indica cosa posso inserire negli editText (\\s = spazio)
        Matcher matcherUser = p.matcher(User);
        Matcher matcherSmartphone = p.matcher(smartphone);
        boolean booleanMatcherUser = matcherUser.find();
        boolean booleanMatcherSmartphone = matcherSmartphone.find();

        if (User.isEmpty() || smartphone.isEmpty()) {

            Toast.makeText(this, getResources().getString(R.string.Error), Toast.LENGTH_LONG).show();

        } else if (booleanMatcherUser || booleanMatcherSmartphone){

            Toast.makeText(this, getResources().getString(R.string.noSpecial), Toast.LENGTH_LONG).show();

        } else if (userSh != null && !userSh.equals(User)){

            changeUserDialog(view);

        } else {

            editor.putString("userName", User).apply();
            editor.putString("smartphoneName", smartphone).apply();
            infodialog(view);
        }

//        else if (smartphone.contains("-") || smartphone.contains(".") || User.contains("-") || User.contains(".")){
//
//            Toast.makeText(this, getResources().getString(R.string.noSpecial), Toast.LENGTH_LONG).show();
//
//        }

    }

    private void changeUserDialog(View view) {
        String userSh = sharedPreferences.getString("userName", null);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        AlertDialog.Builder infoAlert = new AlertDialog.Builder(view.getContext());
        infoAlert.setTitle(getResources().getString(R.string.attention));
        infoAlert.setMessage(getResources().getString(R.string.changeUser, userSh));

        infoAlert.setCancelable(false);
        infoAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                countersSharedPreferences.edit().clear().apply();
                editor.putString("userName", User).apply();
            }

        });

        infoAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editTextUsername = findViewById(R.id.editTextUsername);
                editTextUsername.setText(userSh);
            }
        });


        AlertDialog alert = infoAlert.create();
        alert.show();
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
        infoAlert.setTitle(getResources().getString(R.string.instructions));
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
            int ran = new Random().nextInt(45) /*+ 1*/;
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

        initializeTextView(view);
        inizializeCountersPreferences();
        setTextCounters();

        resetCounters.setOnClickListener(v -> {
            resetButtonAlert(view, dialog);
        });

        dialog.show();
    }

    private void resetButtonAlert(View v, Dialog dialogDismiss) {

        AlertDialog.Builder infoAlert = new AlertDialog.Builder(v.getContext());
        infoAlert.setTitle(getResources().getString(R.string.attention));
        infoAlert.setMessage(getResources().getString(R.string.resetOk));

        infoAlert.setCancelable(false);

        infoAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                countersSharedPreferences.edit().clear().apply();
                dialogDismiss.dismiss();
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

    private void initializeTextView(View view){
        aCount = view.findViewById(R.id.a_count);
        bCount = view.findViewById(R.id.b_count);
        cCount = view.findViewById(R.id.c_count);
        dCount = view.findViewById(R.id.d_count);
        eCount = view.findViewById(R.id.e_count);
        fCount = view.findViewById(R.id.f_count);
        gCount = view.findViewById(R.id.g_count);
        hCount = view.findViewById(R.id.h_count);
        iCount = view.findViewById(R.id.i_count);
        jCount = view.findViewById(R.id.j_count);
        kCount = view.findViewById(R.id.k_count);
        lCount = view.findViewById(R.id.l_count);
        mCount = view.findViewById(R.id.m_count);
        nCount = view.findViewById(R.id.n_count);
        oCount = view.findViewById(R.id.o_count);
        pCount = view.findViewById(R.id.p_count);
        qCount = view.findViewById(R.id.q_count);
        rCount = view.findViewById(R.id.r_count);
        sCount = view.findViewById(R.id.s_count);
        tCount = view.findViewById(R.id.t_count);
        uCount = view.findViewById(R.id.u_count);
        vCount = view.findViewById(R.id.v_count);
        wCount = view.findViewById(R.id.w_count);
        xCount = view.findViewById(R.id.x_count);
        yCount = view.findViewById(R.id.y_count);
        zCount = view.findViewById(R.id.z_count);
        spaceCount = view.findViewById(R.id.space_count);

        resetCounters = view.findViewById(R.id.reset_counters);
    }

    private void inizializeCountersPreferences(){

        a = countersSharedPreferences.getInt("a", 0);
        b = countersSharedPreferences.getInt("b", 0);
        c = countersSharedPreferences.getInt("c", 0);
        d = countersSharedPreferences.getInt("d", 0);
        e = countersSharedPreferences.getInt("e", 0);
        f = countersSharedPreferences.getInt("f", 0);
        g = countersSharedPreferences.getInt("g", 0);
        h = countersSharedPreferences.getInt("h", 0);
        i = countersSharedPreferences.getInt("i", 0);
        j = countersSharedPreferences.getInt("j", 0);
        k = countersSharedPreferences.getInt("k", 0);
        l = countersSharedPreferences.getInt("l", 0);
        m = countersSharedPreferences.getInt("m", 0);
        n = countersSharedPreferences.getInt("n", 0);
        o = countersSharedPreferences.getInt("o", 0);
        p = countersSharedPreferences.getInt("p", 0);
        q = countersSharedPreferences.getInt("q", 0);
        r = countersSharedPreferences.getInt("r", 0);
        s = countersSharedPreferences.getInt("s", 0);
        t = countersSharedPreferences.getInt("t", 0);
        u = countersSharedPreferences.getInt("u", 0);
        v = countersSharedPreferences.getInt("v", 0);
        w = countersSharedPreferences.getInt("w", 0);
        x = countersSharedPreferences.getInt("x", 0);
        y = countersSharedPreferences.getInt("y", 0);
        z = countersSharedPreferences.getInt("z", 0);
        space = countersSharedPreferences.getInt("space", 0);
    }

    private void setTextCounters(){
        aCount.setText(String.valueOf(a));
        bCount.setText(String.valueOf(b));
        cCount.setText(String.valueOf(c));
        dCount.setText(String.valueOf(d));
        eCount.setText(String.valueOf(e));
        fCount.setText(String.valueOf(f));
        gCount.setText(String.valueOf(g));
        hCount.setText(String.valueOf(h));
        iCount.setText(String.valueOf(i));
        jCount.setText(String.valueOf(j));
        kCount.setText(String.valueOf(k));
        lCount.setText(String.valueOf(l));
        mCount.setText(String.valueOf(m));
        nCount.setText(String.valueOf(n));
        oCount.setText(String.valueOf(o));
        pCount.setText(String.valueOf(p));
        qCount.setText(String.valueOf(q));
        rCount.setText(String.valueOf(r));
        sCount.setText(String.valueOf(s));
        tCount.setText(String.valueOf(t));
        uCount.setText(String.valueOf(u));
        vCount.setText(String.valueOf(v));
        wCount.setText(String.valueOf(w));
        xCount.setText(String.valueOf(x));
        yCount.setText(String.valueOf(y));
        zCount.setText(String.valueOf(z));
        spaceCount.setText(String.valueOf(space));
    }

}


