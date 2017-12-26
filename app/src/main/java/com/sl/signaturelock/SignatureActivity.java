package com.sl.signaturelock;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

public class SignatureActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        drawingView = (DrawingView) findViewById(R.id.drawingViewSign);
        textView = (TextView) findViewById(R.id.responseTextView);
    }

    public void clearCanvas(View view){
        drawingView.startNew();
    }

    public void acceptSignature(View view){
        try {
            String token = getToken();
            //Отправляем фотографию
            sendData(token, drawingView.getBase64Picture(), drawingView.getTime());
        } catch (IOException e ){
            e.printStackTrace();
            showToast("IOException");
        } catch (org.json.JSONException e){
            e.printStackTrace();
            showToast("JSONException");
        }
    }

    public void sendData(String token, String picture, double time) throws org.json.JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        jsonObject.put("image", picture);
        jsonObject.put("time", time);

        new AsyncRequest().execute(deleteUnnecessaryChars(jsonObject.toString()));
    }

    public String getToken() throws IOException{
        FileInputStream fin0 = openFileInput(getString(R.string.TOKENFILE));
        byte[] buffer0 = new byte[fin0.available()];
        fin0.read(buffer0, 0, fin0.available());
        fin0.close();
        return new String(buffer0, "UTF-8");
    }

    public String deleteUnnecessaryChars(String string){
        string = string.replace("\\n","");
        string = string.replace("\\","");
        return string;
    }

    private class AsyncRequest extends AsyncTask<String, Void, String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textView.setText(getString(R.string.WAIT));
        }

        @Override
        protected String[] doInBackground(String... params) {
            String[] answer = new String[2];
            try {
                URL obj = new URL(getString(R.string.LOGURL));
                HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                writeData(conn, params[0]);

                JSONObject mainObject = new JSONObject(readData(conn));

                if (mainObject.getBoolean("success")){
                    answer[1] = "success";
                    try {
                        byte[] buffer = readFile(getString(R.string.SECRETFILE));

                        //дешифруем баффер

                        //записываем в формате key%содержание файла, на случай если надо будет сохранить
                        writeFile(getString(R.string.READFILE),
                                (mainObject.getString("key") + "%").getBytes());

                        writeFile(getString(R.string.READFILE),
                                new InformProtect(mainObject.getString("key").getBytes()).decrypt(buffer));

                    } catch (Exception e){
                        e.printStackTrace();
                        answer[0] = "error";
                        answer[1] = "No file / could not open";
                        return answer;
                    }
                } else {
                    answer[1] = mainObject.getString("message");
                }
                answer[0] = "No Error";
                return answer;

            } catch (Exception e){
                e.printStackTrace();
                answer[0] = "error";
                answer[1] = "Problems while connecting";
            }
            return answer;
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

        public byte[] readFile(String file) throws IOException{
            FileInputStream fin = openFileInput(file);
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer, 0, fin.available());
            fin.close();
            return buffer;
        }

        public void writeFile(String file, byte[] data) throws IOException{
            FileOutputStream outputStream = openFileOutput(file, Context.MODE_APPEND);
            outputStream.write(data);
            outputStream.close();
        }

        @Override
        protected void onPostExecute(String[] answer) {
            super.onPostExecute(answer);
            if (answer[0].equals("error")) {
                textView.setText(getString(R.string.MAKESIGNATURE));
                showToast(answer[1]);
            } else{
                if (answer[1].equals("success")) {
                    Intent intent = new Intent(SignatureActivity.this, PasswordsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    textView.setText(getString(R.string.MAKESIGNATURE));
                    showToast(answer[1]);
                }
            }
        }
    }

    public void showToast(String text){
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.show();
    }
}
