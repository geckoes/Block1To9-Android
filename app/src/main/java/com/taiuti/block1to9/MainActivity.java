package com.taiuti.block1to9;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.taiuti.block1to9.core.Block1To9;
import com.taiuti.block1to9.core.Preferences;

public class MainActivity extends AppCompatActivity {

    Block1To9 global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getTheme().applyStyle(new Preferences(this).getFontStyle().getResId(), true);

        Button tapToBegin = findViewById(R.id.startToBegin);
        tapToBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, Block1to9Activity.class);
                startActivity(i);

            }
        });
        global = (Block1To9) getApplication();
        global.calculateScale();

        tapToBegin.setScaleX(global.getScaleX());
        tapToBegin.setScaleY(global.getScaleY());

        TextView tv_title = findViewById(R.id.tv_rules_title);
        TextView tv1 = findViewById(R.id.tv_rules1);
        TextView tv2 = findViewById(R.id.tv_rules2);
        TextView tv3 = findViewById(R.id.tv_rules3);
        TextView tv4 = findViewById(R.id.tv_rules4);

        tv_title.setTextScaleX(global.getScaleX());
        tv1.setTextScaleX(global.getScaleX());
        tv2.setTextScaleX(global.getScaleX());
        tv3.setTextScaleX(global.getScaleX());
        tv4.setTextScaleX(global.getScaleX());

    }

    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings:
                Intent settings = new Intent(MainActivity.this, SettingsActivity_ori.class);
                startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
