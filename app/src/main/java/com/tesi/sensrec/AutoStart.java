package com.tesi.sensrec;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        context.startService(new Intent(context, ansyncService.class));
    }
}