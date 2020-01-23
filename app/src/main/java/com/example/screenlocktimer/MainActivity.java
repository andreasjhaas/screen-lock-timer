package com.example.screenlocktimer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    // Visuals
    private Button btn;
    private ToggleButton tbtn;
    private TextView tv;
    private SeekBar sb;

    // Listener
    private SeekBar.OnSeekBarChangeListener sbcl = new SeekBar.OnSeekBarChangeListener(){

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.button);
        tbtn = findViewById(R.id.toggleButton);
        tv = findViewById(R.id.textView2);
        sb = findViewById(R.id.seekBar);
        sb.setOnSeekBarChangeListener(sbcl);

    }
}
