package com.example.tthazardalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

        for (SmsMessage m : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            String sender = m.getOriginatingAddress();
            String body = m.getMessageBody().trim().toUpperCase();
            Log.d("HazardApp", "SMS received from " + sender + ": " + body);
            CommandProcessor.handle(context, sender, body);
        }
    }
}
