package com.tesi.sensrec;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;


public class CSV {

    public static void Write(float[] args, String view, PrintWriter printWriter, float pressure){

        StringBuilder var = new StringBuilder();
        var.append(System.currentTimeMillis());
        var.append(",");
        //printWriter.print(System.currentTimeMillis());

        //printWriter.append(",");


        //append all elements in args(SensorVal)
        for(float n: args){
            var.append(n);
            var.append(",");
            //printWriter.print(n);
            //printWriter.append(",");

        }
        var.append(view);
        var.append(",");
        var.append(pressure);
        var.append("\n");
        //printWriter.append(TouchedView+"\n");
        //Log.d( "var:",var.toString());
        printWriter.print(var);


    }

    public static void Write2 (long diffe,  PrintWriter printWriter){

        StringBuilder var = new StringBuilder();
        var.append(diffe);
        var.append("\n");
        printWriter.print(var);


    }
}
