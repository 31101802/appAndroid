package com.example.quierobesarte;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.L;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.example.quierobesarte.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MenuScreen extends BaseActivity  {


    private static final String TEST_FILE_NAME = "Universal Image Loader @#&=+-_.,!()~'%20.png";


    Button bUpload;
    int SELECT_FILE1;
    ProgressDialog dialog = null;
    int serverResponseCode;
    String weddingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_screen);

        File testImageOnSdCard = new File("/mnt/sdcard", TEST_FILE_NAME);
        if (!testImageOnSdCard.exists()) {
            copyTestImageToSdCard(testImageOnSdCard);
        }

        Bundle extras = getIntent().getExtras();
        weddingId = extras.getString("weddingId");


        bUpload = (Button) findViewById(R.id.btnUpload);
        bUpload.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                openGallery(SELECT_FILE1);
            }

        });

    }


    public void onImageGridClick(View view) {

        dialog = ProgressDialog.show(MenuScreen.this, "", "Cargando imágenes...", true);
        new Thread(new Runnable() {
            public void run() {


                String [] arrayPhotos = getImages(weddingId.toString());


                if(arrayPhotos != null)
                {


                    List<String> listString = new ArrayList<String>();
                    for(int i=0; i< arrayPhotos.length;i++)
                    {
                        listString.add(arrayPhotos[i].toString().replace("/Thumbnail", ""));
                    }

                    String[] arrayPhotosBig = new String[ listString.size() ];
                    listString.toArray( arrayPhotosBig );
                    Intent intent = new Intent(getApplicationContext(), ImageGridActivity.class);
                    intent.putExtra(Constants.Extra.IMAGES, arrayPhotos);
                    intent.putExtra(Constants.Extra.IMAGESBIG, arrayPhotosBig);
                    intent.putExtra("weddingId", weddingId.toString());
                    startActivity(intent);
                }

            }
        }).start();


    }

    public String [] getImages(String id) {

        InputStream content = null;

        try {


            String getImagesURI = Constants.Config.URL + "/api/images/"+ id + "?page=0&numItems=2000";
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(getImagesURI);
            httpGet.setHeader("App-Version", Constants.Config.VERSION);
            HttpResponse response = httpclient.execute(httpGet);

            if(response.getStatusLine().getStatusCode() == 426)
            {
                dialog.dismiss();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Por favor actualice la aplicación descargando la última versión en su Market Place!!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                return null;

            }
            content = response.getEntity().getContent();
            Helper helper = new Helper();

            String result = helper.convertStreamToString(content);
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<String> stringArrayList = new ArrayList<String>();



            if(jsonArray.length() > 0)
            {

                for (int i=0; i < jsonArray.length(); i++)
                {
                    JSONObject oneObject = jsonArray.getJSONObject(i);
                    // Pulling items from the array
                    stringArrayList.add(Constants.Config.URL + oneObject.getString("thumbnailPath"));

                }

                String [] stringArray = stringArrayList.toArray(new String[stringArrayList.size()]);
                dialog.dismiss();
                return stringArray;
            }
            else
            {
                dialog.dismiss();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Aun no hay fotos, anímate a subir!!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
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

        return null;

    }



    @Override
    public void onBackPressed() {
        imageLoader.stop();
        super.onBackPressed();
    }

    private void copyTestImageToSdCard(final File testImageOnSdCard) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream is = getAssets().open(TEST_FILE_NAME);
                    FileOutputStream fos = new FileOutputStream(testImageOnSdCard);
                    byte[] buffer = new byte[8192];
                    int read;
                    try {
                        while ((read = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, read);
                        }
                    } finally {
                        fos.flush();
                        fos.close();
                        is.close();
                    }
                } catch (IOException e) {
                    L.w("Can't copy test image onto SD card");
                }
            }
        }).start();
    }

    public void openGallery(int req_code) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una foto para subir! "), req_code);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            final Uri selectedImageUri = data.getData();
            if (requestCode == SELECT_FILE1)
            {
                System.out.println("selectedPath1 : " + getPath(selectedImageUri));
            }

            Log.i("getPath(selectedImageUri)", "getPath(selectedImageUri) :"
                    + getPath(selectedImageUri));

            dialog = ProgressDialog.show(MenuScreen.this, "", "Subiendo tu foto...", true);
            new Thread(new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast =
                                    Toast.makeText(getApplicationContext(),
                                            "Comenzando la subida!!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                    uploadFile(getPath(selectedImageUri));
                }
            }).start();
        }

    }

    public String getPath(Uri uri) {

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public int uploadFile(String sourceFileUri) {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"
                    + sourceFileUri);

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast toast =
                            Toast.makeText(getApplicationContext(),
                                    "No existe el fichero!!", Toast.LENGTH_SHORT);

                    toast.show();
                }
            });

            return 0;

        } else {
            try {


                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);


                //Resize the images
                Bitmap myBitmap = BitmapFactory.decodeFile(sourceFileUri);
                File dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                Bitmap out = Bitmap.createScaledBitmap(myBitmap, 320, 480, false);



                String upLoadServerUri = "http://quierobesarte.es.nt5.unoeuro-server.com/Uploader/Upload/?guid=" + weddingId;
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_files", sourceFile.getName());


                Log.i("fileName", "fileName :"
                        + sourceFile.getName());

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_files\";filename=\""
                        + sourceFile.getName() + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();

                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {

                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(MenuScreen.this, "Foto subida correctamente!!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                else if(serverResponseCode == 401)
                {
                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(MenuScreen.this, "Lo sentimos esta boda no está activa!!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }


                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();

                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MenuScreen.this, "Ha habido un problema con la subida!!",
                                Toast.LENGTH_SHORT).show();
                    }
                });


            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MenuScreen.this, "Ha habido un problema con la subida!!",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
            dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }


}
