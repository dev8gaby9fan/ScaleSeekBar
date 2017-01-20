package com.fsh.scaleseekbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.fsh.scaleseekbar.widget.ScaleSeekBar;
import com.fsh.scaleseekbar.widget.WithTag;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ScaleSeekBar scaleSeekBar;
    private List<WithTag> times;
    private TextView mText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scaleSeekBar = (ScaleSeekBar) findViewById(R.id.time_selector);
        mText = (TextView) findViewById(R.id.tv_show_msg);

        times = new ArrayList<>();
        times.add(new WithTag("1小时"));
        times.add(new WithTag("2小时"));
        times.add(new WithTag("3小时"));
        times.add(new WithTag("4小时"));
        times.add(new WithTag("5小时"));
        scaleSeekBar.setTopText(times).
                setOnItemSelectedListener(new ScaleSeekBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                mText.setText("您选择的时间是："+times.get(position).getTag());
            }
        });
        mText.setText("您选择的时间是："+scaleSeekBar.getSelect().getTag());
    }
}
