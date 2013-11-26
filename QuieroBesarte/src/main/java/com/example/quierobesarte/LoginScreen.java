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
import org.json.JSONObject;

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
                            i.putExtra("weddingId", checkCodeResponse.toString());
                            startActivity(i);
                        }

                    }
                }).start();
            }
        });
    }

    public int checkCode(String sPassword) {

        InputStream content = null;

        try {

            //PHP Script Path
            upLoadServerUri = "http://quierobesarte.es.nt5.unoeuro-server.com/api/Wedding/" + sPassword;
            HttpClient httpclient = new DefaultHttpClient();

            //APP VERSION
            HttpGet httpGet = new HttpGet(upLoadServerUri);
            httpGet.setHeader("App-Version","1.0");

            HttpResponse response = httpclient.execute(httpGet);


            if(response.getStatusLine().getStatusCode() == 204)
            {
                dialog.dismiss();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "No existe ninguna boda con esa clave!!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
            else if(response.getStatusLine().getStatusCode() == 426)
            {
                dialog.dismiss();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Por favor actualice la aplicación descargando la última versión en su Market Place", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
            else
            {
                Helper helper = new Helper();
                content = response.getEntity().getContent();
                String result = helper.convertStreamToString(content);
                JSONObject jObject = new JSONObject(result);
                Integer idWedding = jObject.getInt("Id");
                dialog.dismiss();
                return idWedding;
            }


        } catch (Exception e) {
            dialog.dismiss();
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
