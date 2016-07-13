package com.ekeneijeoma.lookup;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_LOCATION = 2;

    Typeface font;

    RadioButton setAll, setEvery, setRandom;
    CheckBox setOnFoot, setOnBicycle, setInVehicle;
    Button setWallpaper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        font = Typeface.createFromAsset(getAssets(),
                "WorkSans-Regular.otf");

        ((TextView) findViewById(R.id.descriptionTextView)).setTypeface(font);

        setAll = (RadioButton) findViewById(R.id.setAll);
        setAll.setTypeface(font);
        setAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIntersection(setAll);
            }
        });

        setEvery = (RadioButton) findViewById(R.id.setEvery);
        setEvery.setTypeface(font);
        setEvery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIntersection(setEvery);
            }
        });

        setRandom = (RadioButton) findViewById(R.id.setRandom);
        setRandom.setTypeface(font);
        setRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIntersection(setRandom);
            }
        });

        setOnFoot = (CheckBox) findViewById(R.id.setOnFoot);
        setOnFoot.setTypeface(font);
        setOnFoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.useOnFoot = setOnFoot.isChecked();
            }
        });

        setOnBicycle = (CheckBox) findViewById(R.id.setOnBicycle);
        setOnBicycle.setTypeface(font);
        setOnBicycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.useOnBicycle = setOnBicycle.isChecked();
            }
        });

        setInVehicle = (CheckBox) findViewById(R.id.setInVehicle);
        setInVehicle.setTypeface(font);
        setInVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.useInVehicle = setInVehicle.isChecked();
            }
        });

        setWallpaper = (Button) findViewById(R.id.setWallpaper);
        setWallpaper.setTypeface(font);
        setWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });
    }

    public void setIntersection(RadioButton rb) {
        Settings.useAll = false;
        Settings.useEvery = false;
        Settings.useRandom = false;

        Settings.everyCount = 0;

        if (rb.getText().equals("All")) {
            Settings.useAll = true;
            setEvery.setChecked(false);
            setRandom.setChecked(false);
        } else if (rb.getText().equals("Every 3")) {
            Settings.useEvery = true;
            setAll.setChecked(false);
            setRandom.setChecked(false);
        } else if (rb.getText().equals("Random")) {
            Settings.useRandom = true;
            setAll.setChecked(false);
            setEvery.setChecked(false);
        }
    }

    public void setWallpaper() {
        Intent intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        startActivity(intent);
    }

    protected void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        } else {
            setWallpaper();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Toast t = Toast.makeText(this, "Press home button to see wallaper", Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
            t.show();
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Toast t = Toast.makeText(this, "cancelled", Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
            t.show();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setWallpaper();
            } else {
                checkPermissions();
            }
        }
    }
}
