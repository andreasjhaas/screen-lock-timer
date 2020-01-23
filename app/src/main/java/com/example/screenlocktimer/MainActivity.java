package com.example.screenlocktimer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.admin.*;
import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    // Visuals
    private Button btn;
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
                    runOnUiThread(new Runnable(){
                        public void run() {
                            if(time > 60){
                                int s = time / 60;
                                int m = time - 60 * s;
                                if(s < 2){
                                    tv.setText(s+" Stunde "+m+" min");
                                }else{
                                    tv.setText(s+" Stunden "+m+" min");
                                }

                            }
                            else{
                                tv.setText(time+" min");
                                sb.setProgress(time);
                            }
                        }
                    });
                    Thread.sleep(60000);
                    time --;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        cn = new ComponentName(this, DeviceAdmin.class);

        btn = findViewById(R.id.button);
        tbtn = findViewById(R.id.toggleButton);
        tv = findViewById(R.id.textView2);
        sb = findViewById(R.id.seekBar);

        tt = new TimerThread(time);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                time = progress;
                if(progress > 60){
                    int s = progress / 60;
                    int m = progress - 60 * s;
                    if(s < 2){
                        tv.setText(s+" Stunde "+m+" min");
                    }else{
                        tv.setText(s+" Stunden "+m+" min");
                    }

                }
                else{
                    tv.setText(progress+" min");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (tbtn.isChecked()) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "ScreenLock permission");
                    startActivityForResult(intent, RESULT_ENABLE);
                } else {
                    dpm.removeActiveAdmin(cn);
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dpm.isAdminActive(cn)) {
                    if(tt.hasStarted()){
                        tt.interrupt();
                        tt = new TimerThread(time);
                        tt.start();
                    }else{
                        tt = new TimerThread(time);
                        tt.start();
                    }
                    Toast.makeText(MainActivity.this, "Timer started", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "You need to enable DeviceAdmin", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        tbtn.setChecked(dpm.isAdminActive(cn));
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
