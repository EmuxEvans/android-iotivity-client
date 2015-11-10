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
import android.widget.Button;
import android.widget.SeekBar;

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class LedControl extends Dialog implements
        android.view.View.OnClickListener {

    private Activity c;
    private Dialog d;
    private Button led_ok;
    private Button led_cancel;

    private boolean put_done = true;

    private SeekBar seekBar;
    private int progress = 0;
    private String msg_type_done;

    public LedControl(Activity a, String msg_type, int progress_curr) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        progress = progress_curr;
        msg_type_done = msg_type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.led_control);

        seekBar = (SeekBar) findViewById(R.id.seekBarLed);
        seekBar.setMax(255);
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
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

        led_ok = (Button) findViewById(R.id.button_led_ok);
        led_ok.setOnClickListener(this);

        led_cancel = (Button) findViewById(R.id.button_led_cancel);
        led_cancel.setOnClickListener(this);

        LocalBroadcastManager.getInstance(this.c).registerReceiver(mLedPutDoneReceiver,
                new IntentFilter(msg_type_done));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_led_ok:
                put_done = false;
                led_ok = (Button) findViewById(R.id.button_led_ok);
                led_ok.setText("Putting ...");
                led_ok.setEnabled(false);
                sendMessage();
                break;
            case R.id.button_led_cancel:
                dismiss();
                break;
            default:
                break;
        }
    }

    private void sendMessage() {
        Intent intent = new Intent("msg_led_adjust");
        // You can also include some extra data.
        intent.putExtra("progress", progress);
        LocalBroadcastManager.getInstance(this.c).sendBroadcast(intent);
    }

    private BroadcastReceiver mLedPutDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            put_done = intent.getBooleanExtra("put_done", false);
            if(put_done) {
                led_ok = (Button) findViewById(R.id.button_led_ok);
                led_ok.setEnabled(true);
                led_ok.setText("SET");
            }
        }
    };
}
