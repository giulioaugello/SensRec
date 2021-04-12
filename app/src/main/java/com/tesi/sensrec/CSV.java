package com.tesi.sensrec;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;


public class CSV {

    public static StringBuilder generateCSVRow(float[] args, String view, float pressure, long time){

        StringBuilder var = new StringBuilder();
        var.append(time);
        var.append(",");


        //append all elements in args(SensorVal)
        for(float n: args){

            var.append(n);
            var.append(",");

        }
        var.append(view);
        var.append(",");
        var.append(pressure);
        var.append("\n");

        //Log.d( "var:",var.toString());

        return var;

    }


//    public static void Write(float[] args, String view, PrintWriter printWriter, float pressure, long time){
//
//        StringBuilder var = new StringBuilder();
//        var.append(time);
//        var.append(",");
//        //printWriter.print(System.currentTimeMillis());
//
//        //printWriter.append(",");
//
//
//        //append all elements in args(SensorVal)
//        for(float n: args){
//            var.append(n);
//            var.append(",");
//            //printWriter.print(n);
//            //printWriter.append(",");
//
//        }
//        var.append(view);
//        var.append(",");
//        var.append(pressure);
//        var.append("\n");
//        //printWriter.append(TouchedView+"\n");
//        //Log.d( "var:",var.toString());
//
//        printWriter.print(var);
//
//    }

}
