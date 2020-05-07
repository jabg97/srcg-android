package com.example.srcg.srcg;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.text.InputType;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.content.Intent;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity implements OnClickListener{

    View root_view;
    Context context = this;
    TextView txt_nuip,txt_url;
    Button btn_scan,btn_clean,buttonConnect,buttonDisConnect;
    String nuip_manual="0",server="127.0.0.1:1234";
    private Socket socket;
    @Override
    public void onClick(View view) {

        if(view.getId()==R.id.btn_scan) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Escanee el codigo QR o Codigo de Barras");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(false);
            integrator.setOrientationLocked(true);
            integrator.initiateScan();

        }


        if (view.getId()==R.id.btn_clean){

            txt_nuip.setText("");
            txt_url.setText(server);

        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Snackbar.make(root_view, "Cancelado", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
            } else {
                String resultado = result.getContents();

                    txt_nuip.setText(resultado);
                    txt_url.setText(server);

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefe=getSharedPreferences("datos",Context.MODE_PRIVATE);
        String memoria =  prefe.getString("server","");
        if(memoria.isEmpty()||memoria == null){
            server="127.0.0.1:1234";
        }else {
            server =memoria;
        }

        root_view =  (RelativeLayout) findViewById(R.id.content_main);
        txt_nuip = (TextView) findViewById(R.id.txt_nuip);
        txt_url = (TextView) findViewById(R.id.txt_url);
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(this);
        btn_clean = (Button) findViewById(R.id.btn_clean);
        btn_clean.setOnClickListener(this);
        buttonConnect = (Button)findViewById(R.id.connect);
        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        buttonDisConnect = (Button)findViewById(R.id.disconnect);
        buttonDisConnect.setOnClickListener(buttonDisConnectOnClickListener);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    OnClickListener buttonDisConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }};

    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    new Thread(new ClientThread()).start();

                }};

    class ClientThread implements Runnable {

        @Override
        public void run() {

                try {

                    String direccion[] = server.toString().split(":");
                    socket = new Socket(direccion[0],Integer.parseInt(direccion[1]));

            }catch (UnknownHostException e1){
                e1.printStackTrace();
            }catch (IOException e1){
                e1.printStackTrace();
            }

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_codigo) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ingrese el codigo");

// Set up the input
            final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_NORMAL);
            input.setText(nuip_manual);
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    nuip_manual = input.getText().toString();
                    txt_nuip.setText(nuip_manual);
                    txt_url.setText(server);
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }

        if (id == R.id.action_server) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("(IP:Puerto)");

// Set up the input
            final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
            input.setText(server);
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    server = input.getText().toString();
                    SharedPreferences preferencias=getSharedPreferences("datos",Context.MODE_PRIVATE);
                    Editor editor=preferencias.edit();
                    editor.putString("server", server);
                    editor.commit();
                    txt_url.setText(server);
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
