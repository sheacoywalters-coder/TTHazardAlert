package com.example.tthazardalert;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class SubscribersActivity extends AppCompatActivity {

    private ListView listView;
    private EditText phoneInput;
    private Spinner addRegionSpinner;
    private ArrayAdapter<Subscriber> adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable refresher = new Runnable() {
        @Override
        public void run() {
            refresh(null);
            handler.postDelayed(this, 2000);   // check again in 2 seconds
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribers);
        listView = findViewById(R.id.listView);
        phoneInput = findViewById(R.id.phoneInput);
        addRegionSpinner = findViewById(R.id.addRegionSpinner);
        DataStore.get().ensureLoaded(this);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, DataStore.get().subscribers);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(refresher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refresher);
    }

    public void addSubscriber(View v) {
        final String phone = phoneInput.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(this, "Enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        String region = addRegionSpinner.getSelectedItem().toString();

        DataStore ds = DataStore.get();
        ds.subscribers.removeIf(x -> x.getPhone().equals(phone));
        ds.subscribers.add(new Subscriber(phone, region));
        ds.save(this);

        phoneInput.setText("");
        Toast.makeText(this, phone + " added to " + region, Toast.LENGTH_SHORT).show();
        refresh(null);
    }

    public void refresh(View v) {
        adapter.notifyDataSetChanged();
    }

    public void goBack(View v) { finish(); }
}