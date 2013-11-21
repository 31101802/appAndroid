package com.example.quierobesarte;

import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class LoginScreen extends Activity {

    EditText password;
    Button enterButton;
    ProgressDialog dialog = null;

    String upLoadServerUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        enterButton = (Button) findViewById(R.id.button);
        password = (EditText) findViewById(R.id.editText);


        enterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String sPassword = password.getText().toString();
                if (sPassword.matches("")) {

                    Toast toast =
                            Toast.makeText(getApplicationContext(),
                                    "Introduce una clave por favor :)", Toast.LENGTH_SHORT);

                    toast.show();
                    return;
                }


                dialog = ProgressDialog.show(LoginScreen.this, "", "Comprobando clave...", true);

                new Thread(new Runnable() {
                    public void run() {

                        Integer checkCodeResponse = checkCode(sPassword);

                        if(checkCodeResponse > 0)
                        {
                            Intent i = new Intent(getApplicationContext(), MenuScreen.class);
                            Log.i("sPassword", "sPassword :"
                                    + checkCodeResponse.toString());
                            i.putExtra("weddingId", checkCodeResponse.toString());
                            startActivity(i);
                        }

                    }
                }).start();
            }
        });
    }

    public int checkCode(String sPassword) {
        Log.i("sPassword", "sPassword :"
                + sPassword);

        InputStream content = null;
        try {

            /************* Php script path ****************/
            upLoadServerUri = "http://quierobesarte.cloudapp.net/Quierobesarte.Api/checkCode.php?q=" + sPassword;
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(upLoadServerUri));
            content = response.getEntity().getContent();
            Helper helper = new Helper();
            String result = helper.convertStreamToString(content);

            Log.i("result", "result :"
                    + result);

            if (!result.matches("0")) {
                dialog.dismiss();
                return Integer.parseInt(result);

            } else {
                dialog.dismiss();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "No existe ninguna boda con esa clave!!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });

            }


        } catch (Exception e) {
            dialog.dismiss();
            Log.e("[GET REQUEST]", "Network exception", e);
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Ha habido un error!!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

    return 0;

    }


}
