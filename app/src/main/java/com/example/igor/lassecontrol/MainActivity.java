package com.example.igor.lassecontrol;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
static String MQTTHOST="tcp://200.239.93.2:1883";
static String USER="lasse";
static String PASS="lassemqtt8266";
    MqttAndroidClient client;
    ImageButton mic ;
    ImageButton sound;
    int result;
    final int recordCode=0;
    private TextView command;
    private TextView msgreceived;
    private String topico="/lasse/kelis/rec/app";
    private String resposta="/lasse/kelis/res/app";
    TextToSpeech tts;
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    String clientId = MqttClient.generateClientId();
     client =
            new MqttAndroidClient(this.getApplicationContext(), MQTTHOST,
                    clientId);
    MqttConnectOptions options = new MqttConnectOptions();
    options.setUserName(USER);
    options.setPassword(PASS.toCharArray());
    try {
        IMqttToken token = client.connect(options);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
            Toast.makeText(MainActivity.this,"Conectado com sucesso", Toast.LENGTH_SHORT).show();
            subscribe(resposta);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Toast.makeText(MainActivity.this,"Problema na conexão com o servidor", Toast.LENGTH_LONG).show();
            }
        });
    } catch (MqttException e) {
        e.printStackTrace();
    }
        tts= new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i== TextToSpeech.SUCCESS){
                  result = tts.setLanguage(Locale.getDefault());
                }else{
                    Toast.makeText(MainActivity.this,"Seu dispositivo não suporta a fala",Toast.LENGTH_SHORT).show();
                }
            }
        });

        command= (TextView) findViewById(R.id.commandText);
        msgreceived= (TextView) findViewById(R.id.mqttTextView);
        sound = (ImageButton) findViewById(R.id.soundBtn);
        sound.setOnClickListener(   new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(MainActivity.this,"Problema na inicialização da Linguagem",Toast.LENGTH_LONG).show();
                if(result==TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(MainActivity.this,"Lingua não suportada      ",Toast.LENGTH_LONG).show();}

            }
            else{
                String text=msgreceived.getText().toString();
                //publish(topico,text);
                tts.speak(text,TextToSpeech.QUEUE_FLUSH,null);
            }
        }
    });

    mic=(ImageButton) findViewById(R.id.micBtn);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say something");
               try {
                   startActivityForResult(i, recordCode);
               }catch (ActivityNotFoundException e){
                   Toast.makeText(MainActivity.this,"Seu dispositivo não suporta a conversão de voz em texto",Toast.LENGTH_LONG).show();
               }
            }
        });
    client.setCallback(new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            msgreceived.setText(new String(message.getPayload()));
            tts.speak(msgreceived.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case recordCode:
                if(resultCode==RESULT_OK && data!= null){
                    ArrayList<String> texto = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    command.setText(texto.get(0));
                    publish(topico,command.getText().toString());
                }
                break;

        }

    }
    public void publish(String topic, String message){
    try {
        client.publish(topic, message.getBytes(),0,false);
    } catch (MqttException e) {
        e.printStackTrace();
    }
    }
    public void subscribe(String topic){
        try{
            client.subscribe(topic,0);
        }catch(MqttException e){
            e.printStackTrace();
        }
    }
}

