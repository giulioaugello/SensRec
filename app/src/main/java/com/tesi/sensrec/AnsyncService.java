package com.tesi.sensrec;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Arrays;


import static android.content.ContentValues.TAG;

public class AnsyncService extends Service {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }
    @Override
    public void onCreate() {
       // Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        File myDirectoryPath = Environment.getExternalStorageDirectory();
        File dir = new File(String.valueOf(myDirectoryPath));
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (final File child : directoryListing) {
                if (child.getName().contains(".csv")){
                    Log.d(TAG, "onStartCommand: file:" + child);
                    Uri file = Uri.fromFile(child);
                    StorageReference riversRef = storageRef.child("rawCsv/" + file.getLastPathSegment());
                    riversRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            if(child.delete()){
                                //Toast.makeText(ansyncService.this, "deleted", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                     .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Toast.makeText(ansyncService.this, "Failed "+e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        } else {
           // Toast.makeText(this, "empty dir", Toast.LENGTH_LONG).show();
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
     //   Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
    }
}
