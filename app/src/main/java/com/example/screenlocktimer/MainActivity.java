package com.example.screenlocktimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.admin.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends AppCompatActivity {

    // Visuals
    private ToggleButton tbtn2;
    private ToggleButton tbtn;
    private TextView tv;
    private SeekBar sb;

    // Variables
    private int time;
    private TimerThread tt;
    private DevicePolicyManager dpm;
    private ComponentName cn;
    public static final int RESULT_ENABLE = 11;

    // Class
    public static class DeviceAdmin extends DeviceAdminReceiver {

        @Override
        public void onEnabled(Context context, Intent intent) {
            Toast.makeText(context, "Device Admin : enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
            Toast.makeText(context, "Device Admin : disabled", Toast.LENGTH_SHORT).show();
        }
    }

    private class TimerThread extends Thread {
        private boolean hasStarted = false;
        private int time;

        public TimerThread(int time){
            this.time = time;
        }

        @Override
        public void run() {
            hasStarted = true;
            try{
                while(time > 0){
                    Thread.sleep(60000);
                    time --;
                    runOnUiThread(new Runnable(){
                        public void run() {
                            updateTimeInfo();
                            sb.setProgress(time);
                            if(time == 0){
                                tbtn2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.colorWidgets)));
                                tbtn2.setChecked(false);
                                sb.setEnabled(true);
                            }
                        }
                    });
                }
                dpm.lockNow();

            }catch(Exception e){
                //e.printStackTrace();
                hasStarted = false;
            }
        }
        public boolean hasStarted(){
            return this.hasStarted;
        }
    }

    //Methods
    private void updateTimeInfo(){
        time = sb.getProgress();
        if(time > 60){
            int s = time / 60;
            int m = time - 60 * s;
            if(s < 2){
                tv.setText(s+" Stunde "+m+" min");
            }else{
                tv.setText(s+" Stunden "+m+" min");
            }

        } else{
            tv.setText(time+" min");
        }
    }

    // Listener
    private OnCheckedChangeListener occl = new OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch(buttonView.getText().toString()){
                case "Start Timer":
                    if (dpm.isAdminActive(cn)) {
                        if(time != 0){
                            sb.setEnabled(false);
                            tt = new TimerThread(time);
                            tt.start();
                            tbtn2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.colorWidgetsRed)));
                            Toast.makeText(MainActivity.this, "Timer started", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            tbtn2.setChecked(false);
                        }
                    } else{
                        tbtn2.setChecked(false);
                        Toast.makeText(MainActivity.this, "You need to enable DeviceAdmin", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "Stop Timer":
                    sb.setEnabled(true);
                    tbtn2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.colorWidgets)));
                    tt.interrupt();
                    break;
                case "OFF":
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "ScreenLock permission");
                    startActivityForResult(intent, RESULT_ENABLE);
                    break;
                case "ON":
                    dpm.removeActiveAdmin(cn);
                    tbtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.colorWidgetsRed)));
                    break;
            }
        }
    };

    private OnSeekBarChangeListener oscl = new OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateTimeInfo();
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        cn = new ComponentName(this, DeviceAdmin.class);

        tbtn2 = findViewById(R.id.toggleButton2);
        tbtn = findViewById(R.id.toggleButton);
        tv = findViewById(R.id.textView2);
        sb = findViewById(R.id.seekBar);

        updateTimeInfo();

        tt = new TimerThread(time);

        sb.setOnSeekBarChangeListener(oscl);
        tbtn.setOnCheckedChangeListener(occl);
        tbtn2.setOnCheckedChangeListener(occl);

    }

    @Override
    protected void onResume() {
        super.onResume();
        tbtn.setChecked(dpm.isAdminActive(cn));
        tbtn.setBackgroundTintList(dpm.isAdminActive(cn) ? ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.colorWidgets)):ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.colorWidgetsRed)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RESULT_ENABLE) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(MainActivity.this, "Problem to enable DeviceAdmin", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
