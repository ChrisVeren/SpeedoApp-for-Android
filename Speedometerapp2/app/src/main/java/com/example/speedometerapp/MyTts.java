package com.example.speedometerapp;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class MyTts {
    private TextToSpeech tts;
    TextToSpeech.OnInitListener initListener= new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if(status == TextToSpeech.SUCCESS){
                tts.setLanguage(Locale.ENGLISH);
            }
        }
    };

    public MyTts(Context context){
        tts = new TextToSpeech(context,initListener);
    }

    public void speak(String message){
        tts.speak(message,TextToSpeech.QUEUE_ADD,null,null);
    }
}
