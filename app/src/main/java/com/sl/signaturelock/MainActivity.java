package com.sl.signaturelock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //На всякий случай проверяем, не остался ли с прошлого раза readfile
        checkFileDeleted(getString(R.string.READFILE));
    }

    public void checkFileDeleted(String file){
        File readFile = new File(getFilesDir(),file);
        if (readFile.exists()) readFile.delete();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.info_setting:
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
                return true;
            case R.id.exit_setting:
                finish();
                return true;
            default:
                return false;
        }
    }

    public void SignatureButton(View view){
        Intent intent = new Intent(MainActivity.this, SignatureActivity.class);
        startActivity(intent);
    }

    public void RegisterButton(View view){
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
