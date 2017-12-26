package com.sl.signaturelock;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class PasswordsActivity extends AppCompatActivity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords);
        editText = (EditText) findViewById(R.id.editText);
        try {
            editText.setText(getFileContent(getString(R.string.READFILE)));
        }catch (Exception e){
            e.printStackTrace();
            editText.setText(getString(R.string.NOFILE));
        }
    }

    public String getFileContent(String file) throws Exception{
        FileInputStream fin = openFileInput(file);
        byte[] buffer = new byte[fin.available()];
        fin.read(buffer, 0, fin.available());
        File read = new File(getFilesDir(), file);
        if (read.exists()) read.deleteOnExit();
        fin.close();
        String str = new String(buffer, "UTF-8");
        str = str.substring(str.indexOf('%') + 1);
        return str;
    }

    public void save(View view){
            String newtext = editText.getText().toString();
            try{
                //получаем из readfile ключ шифрования
                String key = getKey();

                //шифруем newtext и записываем в файл
                FileOutputStream outputStream = openFileOutput(getString(R.string.READFILE), Context.MODE_PRIVATE);
                outputStream.write(new InformProtect(key.getBytes()).encrypt(newtext.getBytes()));
                outputStream.close();

                showToast("File changed successfully!");
            }catch (Exception e){
                e.printStackTrace();
                showToast("Error while saving");
            }
    }

    public void showToast(String text){
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.show();
    }

    public String getKey() throws Exception {
        FileInputStream fin = openFileInput(getString(R.string.READFILE));
        byte[] buffer = new byte[fin.available()];
        fin.read(buffer, 0, fin.available());
        String key = new String(buffer, "UTF-8");
        key = key.substring(0, key.indexOf('%'));
        return key;
    }

    public void goBack(View view){
        checkFileDeleted(getString(R.string.READFILE));
        finish();
    }

    public void checkFileDeleted(String file){
        File readFile = new File(getFilesDir(),file);
        if (readFile.exists()) readFile.delete();
    }
}
