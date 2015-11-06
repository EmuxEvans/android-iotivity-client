package com.canonical.democlient; /**
 * Created by gerald on 2015/11/4.
 */

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class DemoResource {
    public static final String SENSOR_TEMPERATURE_KEY = "temperature";
    public static final String SENSOR_LIGHT_KEY = "light";
    public static final String SENSOR_SOUND_KEY = "sound";
    public static final String LED_STATUS_KEY = "status";
    public static final String LCD_KEY = "lcd";
    public static final String BUZZER_KEY = "tone";
    public static final String BUTTON_KEY = "button";
    public static final String TOUCH_KEY = "touch";

    private double mTemp;
    private int mLight;
    private int mSound;
    private int mLed;
    private String mLcd;
    private int mBuzzer;
    private int mButton;
    private int mTouch;

    private int mTemp_index;
    private int mLight_index;
    private int mSound_index;
    private int mLed_index;
    private int mLcd_index;
    private int mBuzzer_index;
    private int mButton_index;
    private int mTouch_index;

    public DemoResource() {
        mTemp = 0.0;
        mLight = 0;
        mLight = 0;
        mLed = 0;
        mLcd = "";
        mBuzzer = 0;
        mButton = 0;
        mTouch = 0;

        mTemp_index = 0;
        mLight_index = 0;
        mSound_index = 0;
        mLed_index = 0;
        mLcd_index = 0;
        mBuzzer_index = 0;
        mButton_index = 0;
        mTouch_index = 0;
    }

    public void sensorGetOcRepresentation(OcRepresentation rep) throws OcException {
        mTemp = rep.getValue(DemoResource.SENSOR_TEMPERATURE_KEY);
        mLight = rep.getValue(DemoResource.SENSOR_LIGHT_KEY);
        mSound = rep.getValue(DemoResource.SENSOR_SOUND_KEY);
    }

    public void ledGetOcRepresentation(OcRepresentation rep) throws OcException {
        mLed = rep.getValue(DemoResource.LED_STATUS_KEY);
    }

    public void lcdGetOcRepresentation(OcRepresentation rep) throws OcException {
        mLcd = rep.getValue(DemoResource.LCD_KEY);
    }

    public void buzzerGetOcRepresentation(OcRepresentation rep) throws OcException {
        mBuzzer = rep.getValue(DemoResource.BUZZER_KEY);
    }

    public void buttonGetOcRepresentation(OcRepresentation rep) throws OcException {
        mButton = rep.getValue(DemoResource.BUTTON_KEY);
        mTouch = rep.getValue(DemoResource.TOUCH_KEY);
    }


    public OcRepresentation sensorSetOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(SENSOR_TEMPERATURE_KEY, mTemp);
        rep.setValue(SENSOR_LIGHT_KEY, mLight);
        rep.setValue(SENSOR_SOUND_KEY, mSound);
        return rep;
    }

    public OcRepresentation ledSetOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(LED_STATUS_KEY, mLed);
        return rep;
    }

    public OcRepresentation lcdSetOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(LCD_KEY, mLcd);
        return rep;
    }

    public OcRepresentation buzzerSetOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(BUZZER_KEY, mBuzzer);
        return rep;
    }

    public double getTemp() { return mTemp; }
    public int getLight() { return mLight; }
    public int getSound() { return mSound; }
    public int getLed() { return mLed; };
    public String getLcd() { return mLcd; }
    public int getBuzzer() { return mBuzzer; }
    public int getButton() { return mButton; }
    public int getTouch() { return mTouch; };

    public void setTempIndex(int index) { mTemp_index = index; }
    public void setLightIndex(int index) { mLight_index = index; }
    public void setSoundIndex(int index) { mSound_index = index; }
    public void setLedIndex(int index) { mLed_index = index; }
    public void setLcdIndex(int index) { mLcd_index = index; }
    public void setBuzzerIndex(int index) { mBuzzer_index = index; }
    public void setButtonIndex(int index) { mButton_index = index; }
    public void setTouchIndex(int index) { mTouch_index = index; }

    public int getTempIndex() { return mTemp_index; }
    public int getLightIndex() { return mLight_index; }
    public int getSoundIndex() { return mSound_index; }
    public int getLedIndex() { return mLed_index; }
    public int getLcdIndex() { return mLcd_index; }
    public int getBuzzerIndex() { return mBuzzer_index; }
    public int getButtonIndex() { return mButton_index; }
    public int getTouchIndex() { return mTouch_index; }
}
