package com.example.igor.lassecontrol;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
ImageButton mic ;
ImageButton sound;
    int result;
    final int recordCode=0;
private TextView command;
    TextToSpeech tts;
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        sound = (ImageButton) findViewById(R.id.soundBtn);
        sound.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(MainActivity.this,"Problema na inicialização da Linguagen",Toast.LENGTH_LONG).show();
                if(result==TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(MainActivity.this,"Lingua não suportada      ",Toast.LENGTH_LONG).show();}
            }
            else{
                String text=command.getText().toString();
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case recordCode:
                if(resultCode==RESULT_OK && data!= null){
                    ArrayList<String> texto = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    command.setText(texto.get(0));
                }
                break;

        }
    }
}
