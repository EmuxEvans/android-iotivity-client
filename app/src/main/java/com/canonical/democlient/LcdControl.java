package com.canonical.democlient;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class LcdControl extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button lcd_ok, lcd_cancel;

    private EditText editText;
    private String lcd_str;

    public LcdControl(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lcd_control);

        lcd_str = "";

        lcd_ok = (Button) findViewById(R.id.button_lcd_ok);
        lcd_ok.setOnClickListener(this);
        lcd_cancel = (Button) findViewById(R.id.button_lcd_cancel);
        lcd_cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_lcd_ok:
                editText = (EditText) findViewById(R.id.editTextLcd);
                String temp = editText.getText().toString();
                if(!temp.isEmpty())
                    lcd_str = temp;

                dismiss();
                break;
            case R.id.button_lcd_cancel:
                dismiss();
                break;
            default:
                break;
        }
    }

    public String get_string() {
        return lcd_str;
    }
}
