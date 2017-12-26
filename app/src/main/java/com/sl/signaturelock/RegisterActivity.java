package com.sl.signaturelock;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;


import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;


public class RegisterActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private TextView textView;
    private ProgressBar progressBar;
    private String[] pictures;
    private double[] time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        drawingView = (DrawingView) findViewById(R.id.drawingViewReg);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.regTextView);
        progressBar.setMax(getResources().getInteger(R.integer.NUMBEROFPICTURES));
        progressBar.setProgress(0);
        pictures = new String[getResources().getInteger(R.integer.NUMBEROFPICTURES)];
        time = new double[getResources().getInteger(R.integer.NUMBEROFPICTURES)];
    }

    public void clearCanvas(View view) {
        drawingView.startNew();
    }

    public void acceptSignature(View view){
        int nextStep = progressBar.getProgress() + 1;
        if (nextStep == 6) return;
        //сохраняем изображение и время
        pictures[nextStep - 1] = drawingView.getBase64Picture();
        time[nextStep - 1] = drawingView.getTime();

        drawingView.startNew();
        progressBar.setProgress(nextStep);
        if (nextStep == getResources().getInteger(R.integer.NUMBEROFPICTURES)) {

            //проверяем не зарегистрированы ли мы:
            registerCheck();

            //отправляем данные
            try{
                sendData(pictures, time);
            } catch (org.json.JSONException e){
                e.printStackTrace();
                showToast("Error with JSON");
            }
        }

    }

    public void registerCheck(){
        File file = new File(getFilesDir(), getString(R.string.TOKENFILE));
        if (file.exists()){
            Toast toast = Toast.makeText(getApplicationContext(), "You already registered", Toast.LENGTH_LONG);
            toast.show();
            finish();
            return;
        }
    }

    public void sendData(String[] pictures, double[] time) throws org.json.JSONException{
        JSONObject resultJson = new JSONObject();
        for (int i = 1; i <= getResources().getInteger(R.integer.NUMBEROFPICTURES); i++) {
            resultJson.put("image" + i, pictures[i-1]);
            resultJson.put("time" + i, String.valueOf(time[i-1]));
        }
        new AsyncRequest().execute(deleteUnnecessaryChars(resultJson.toString()));
    }

    public String deleteUnnecessaryChars(String string){
        string = string.replace("\\n","");
        string = string.replace("\\","");
        return string;
    }

    private class AsyncRequest extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textView.setText(getString(R.string.WAIT));
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL obj = new URL(getString(R.string.REGURL));
                HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                //записываем информацию в запрос
                writeData(conn, params[0]);

                JSONObject mainObject = new JSONObject(readData(conn));

                if (mainObject.getBoolean("success")) {

                    writeFile(getString(R.string.SECRETFILE),
                            new InformProtect(mainObject.getString("key").getBytes())
                            .encrypt(getString(R.string.COMMONSTRING).getBytes()));

                    //Запишем токен
                    writeFile(getString(R.string.TOKENFILE),
                            mainObject.getString("token").getBytes());


                } else {
                    return null;
                }
                return mainObject.toString();

            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        public void writeData(HttpsURLConnection conn,String data) throws IOException{
            DataOutputStream output = new DataOutputStream(conn.getOutputStream());
            output.writeBytes(data);
            output.flush();
            output.close();
        }

        public String readData(HttpsURLConnection conn) throws IOException{
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), Charset.forName("UTF8"))
            );
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }

        public void writeFile(String file, byte[] data) throws IOException{
            FileOutputStream outputStream = openFileOutput(file, Context.MODE_APPEND);
            outputStream.write(data);
            outputStream.close();
        }

        @Override
        protected void onPostExecute(String answer) {
            super.onPostExecute(answer);
            if (answer != null) {
                showToast("Registered successfully");
            } else{
                showToast("Error while register");
            }
            finish();
        }
    }

    public void showToast(String text){
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.show();
    }

}
