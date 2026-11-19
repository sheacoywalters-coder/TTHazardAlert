package com.example.tthazardalert;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AlertLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_log);
        DataStore.get().ensureLoaded(this);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<Alert>(this,
                android.R.layout.simple_list_item_1, DataStore.get().alerts) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                String sev = getItem(position).getSeverity();
                int color = Color.parseColor("#2E7D32");               // Advisory: green
                if (sev.equals("Watch"))   color = Color.parseColor("#EF6C00");  // orange
                if (sev.equals("Warning")) color = Color.parseColor("#C62828");  // red
                if (sev.equals("Severe"))  color = Color.parseColor("#6A0000");  // dark red
                tv.setTextColor(color);
                tv.setTypeface(null, sev.equals("Severe") ? Typeface.BOLD : Typeface.NORMAL);
                return tv;
            }
        });
    }

    public void goBack(View v) { finish(); }
}