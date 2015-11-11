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

    private TextView testview_seconds;
    private SeekBar seekBar;
    private int progress = 0;

    private String msg_type;

    public BuzzerControlP(Activity a, String type) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        msg_type = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.buzzer_control_p);

        buzzer_button = (Button) findViewById(R.id.button_beep);
        buzzer_button.setOnClickListener(this);

        seekBar = (SeekBar) findViewById(R.id.seekBar_beep);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_buzzer_ok:
                sendMessage();
                dismiss();
                break;
            default:
                break;
        }
    }

    private void sendMessage() {
        Intent intent = new Intent(msg_type);
        // You can also include some extra data.
        intent.putExtra("beep", progress);
        LocalBroadcastManager.getInstance(this.c).sendBroadcast(intent);
    }
}
