package com.canonical.democlient;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class BuzzerControl extends Dialog implements
        android.view.View.OnClickListener {

    private Activity c;
    private Dialog d;
    private Button buzzer_ok, buzzer_cancel;

    private EditText editText;
    private int tone;

    private String msg_type_done;
    private boolean put_done = true;

    public BuzzerControl(Activity a, String msg_type) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        msg_type_done = msg_type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.buzzer_control);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_buzzer);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.c,
                R.array.buzzer_tone_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        buzzer_ok = (Button) findViewById(R.id.button_buzzer_ok);
        buzzer_ok.setOnClickListener(this);
        buzzer_cancel = (Button) findViewById(R.id.button_buzzer_cancel);
        buzzer_cancel.setOnClickListener(this);

        LocalBroadcastManager.getInstance(this.c).registerReceiver(mBuzzerPutDoneReceiver,
                new IntentFilter(msg_type_done));

        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
                Log.e("BuzzerControl", "Position: " + String.valueOf(position));
                switch(position) {
                    case 0:
                        tone = 1915;
                        break;
                    case 1:
                        tone = 1700;
                        break;
                    case 2:
                        tone = 1519;
                        break;
                    case 3:
                        tone = 1432;
                        break;
                    case 4:
                        tone = 1275;
                        break;
                    case 5:
                        tone = 1136;
                        break;
                    case 6:
                        tone = 1014;
                        break;
                    case 7:
                        tone = 956;
                        break;
                }
            }

            public void onNothingSelected(AdapterView arg0) {
                Log.e("BuzzerControl", "No selection");
                tone = 1915;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_buzzer_ok:
                put_done = false;
                buzzer_ok = (Button) findViewById(R.id.button_buzzer_ok);
                buzzer_ok.setText("Putting ...");
                buzzer_ok.setEnabled(false);
                sendMessage();
                break;
            case R.id.button_buzzer_cancel:
                dismiss();
                break;
            default:
                break;
        }
    }

    private void sendMessage() {
        Intent intent = new Intent("msg_buzzer_tone");
        // You can also include some extra data.
        intent.putExtra("tone", tone);
        LocalBroadcastManager.getInstance(this.c).sendBroadcast(intent);
    }

    private BroadcastReceiver mBuzzerPutDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            put_done = intent.getBooleanExtra("put_done", false);
            if(put_done) {
                buzzer_ok.setEnabled(true);
                buzzer_ok.setText("SET");
            }
        }
    };
}
