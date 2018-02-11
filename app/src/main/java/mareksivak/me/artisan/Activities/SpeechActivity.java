package mareksivak.me.artisan.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;

import java.util.ArrayList;

import mareksivak.me.artisan.R;

// SOURCE
//https://github.com/zagum/SpeechRecognitionView/blob/master/speechrecognitionview-sample/src/main/java/com/github/zagum/speechrecognitionview/sample/MainActivity.java


public class SpeechActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION_CODE = 1;


    SpeechRecognizer speechRecognizer;
    RecognitionProgressView recognitionProgressView;
    Button mListenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(SpeechActivity.this);
        recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);
        recognitionProgressView.setSpeechRecognizer(speechRecognizer);
        recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
            @Override
            public void onResults(Bundle results) {
                showResults(results);
            }
        });

        mListenButton = (Button) findViewById(R.id.btnSpeechStart);
        mListenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecognitionClicked();
            }
        });

        recognitionProgressView.play();
    }

    private void startRecognitionClicked() {
        if (ContextCompat.checkSelfPermission(SpeechActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            startRecognition();
            recognitionProgressView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startRecognition();
                }
            }, 50);
        }
    }
    private void startRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizer.startListening(intent);
    }

    private void showResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Toast.makeText(this, matches.get(0), Toast.LENGTH_LONG).show();
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, "Requires RECORD_AUDIO permission", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    REQUEST_RECORD_AUDIO_PERMISSION_CODE);
        }
    }

}
