package com.opau.music;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void delLib(View v) {
        ((App)getApplication()).getLibraryManager().deleteLibrary();
        System.exit(0);
    }
}