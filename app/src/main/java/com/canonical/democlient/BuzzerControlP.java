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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class BuzzerControlP extends Dialog implements
        android.view.View.OnClickListener {

    private Activity c;
    private Button buzzer_button;
    private Button buzzer_button_cancel;

    private TextView testview_seconds;
    private SeekBar seekBar;
    private double progress = 0;
    private boolean put_done;

    private String msg_type_done;

    public BuzzerControlP(Activity a, String type) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        msg_type_done = type;
        put_done = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.buzzer_control_p);

        testview_seconds = (TextView) findViewById(R.id.textView_beep);

        buzzer_button = (Button) findViewById(R.id.button_beep);
        buzzer_button.setOnClickListener(this);

        buzzer_button_cancel = (Button) findViewById(R.id.button_beep_cancel);
        buzzer_button_cancel.setOnClickListener(this);

        seekBar = (SeekBar) findViewById(R.id.seekBar_beep);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = (double)progresValue;
                testview_seconds.setText(String.valueOf(progress) + " seconds");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do something here,
                //if you want to do anything at the start of
                // touching the seekbar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Display the value in textview

            }
        });

        LocalBroadcastManager.getInstance(this.c).registerReceiver(mBuzzerPutDoneReceiver,
                new IntentFilter(msg_type_done));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_beep:
                buzzer_button.setText("Putting ...");
                buzzer_button.setEnabled(false);
                sendMessage();
                break;
            case R.id.button_beep_cancel:
                dismiss();
                break;
            default:
                break;
        }
    }

    private void sendMessage() {
        Intent intent = new Intent("msg_buzzer_p_beep");
        // You can also include some extra data.
        intent.putExtra("beep", progress);
        LocalBroadcastManager.getInstance(this.c).sendBroadcast(intent);
    }

    private BroadcastReceiver mBuzzerPutDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            put_done = intent.getBooleanExtra("put_done", false);
            if(put_done) {
                buzzer_button.setEnabled(true);
                buzzer_button.setText("SET");
            }
        }
    };
}
