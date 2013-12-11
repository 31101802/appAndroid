package es.quierobesarte.app;

import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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


        ImageView img = (ImageView)findViewById(R.id.imageView2);
        img.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://www.facebook.com/pages/Quiero-Besarte/281514515307124"));
                startActivity(intent);
            }
        });

        ImageView img2 = (ImageView)findViewById(R.id.imageView3);
        img2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://twitter.com/QuierobesarteES"));
                startActivity(intent);
            }
        });

        ImageView img3 = (ImageView)findViewById(R.id.imageView4);
        img3.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://vimeo.com/quierobesarte"));
                startActivity(intent);
            }
        });





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

                        String checkCodeResponse = checkCode(sPassword);

                        if(checkCodeResponse.length() > 0)
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




    public String checkCode(String sPassword) {

        InputStream content = null;

        try {

            //PHP Script Path
            upLoadServerUri = Constants.Config.URL + "/api/Wedding/" + sPassword;
            HttpClient httpclient = new DefaultHttpClient();

            //APP VERSION
            HttpGet httpGet = new HttpGet(upLoadServerUri);
            httpGet.setHeader("App-Version", Constants.Config.VERSION);

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
                String idWedding = jObject.getString("Id");
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

    return "";

    }


}
