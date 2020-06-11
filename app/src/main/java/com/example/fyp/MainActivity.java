package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button selectimage,detecttext,speak,currency;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;
    String currentimagepath=null;
    TextToSpeech mTTS,mTTS1,mTTS2,mTTS3,mTTS4,mTTS5,mTTS6,mTTS7,mTTS8,mTTS9,mTTS10,mTTS11,mTTS12,mTTS13,mTTS14;
    String texttospeak="";
    String[] expirydate= {"expiry date","exp","days to expire","expiration date","daystoexpiry","daystoexpire","valid till","exp","expiry","used by"};
    String[] mfgdate= {"mfg","mfg date","mfgdate","createdon","mfg.date"};
    String[] bestbeforedate={"best before","best before date","bestbefore"};
    String[] warninglabels={"warning label","warning","warninglabel","danger","keep out","hazard","danger"};
    String[] cautionmessage={"caution","caution messages","cautions","be careful","do not cross"};
    String[] ingredients={"recipe","ingredients","ingredient list"};
    String[] proteinchart={"protein chart","protein"};
    String mynote;
    float myconfidence;
    float temp=-999;
    String tempnote="";


    FirebaseAutoMLRemoteModel remoteModel; // For loading the model remotely
    FirebaseVisionImageLabeler labeler; //For running the image labeler
    FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder; // Which option is use to run the labeler local or remotely
    ProgressDialog progressDialog; //Show the progress dialog while model is downloading...
    FirebaseModelDownloadConditions conditions; //Conditions to download the model
    FirebaseVisionImage image; // preparing the input image
    private FirebaseAutoMLLocalModel localModel;

    MediaPlayer beepmediaplayer;
    @Override
    protected void onPause() {
        super.onPause();
    }

    ImageView imageView;


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent=new Intent(MainActivity.this,MySensorService.class);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectimage=findViewById(R.id.selectimage);
        detecttext=findViewById(R.id.detecttext);
        speak=findViewById(R.id.speak);
        imageView=findViewById(R.id.imageview);
        beepmediaplayer=MediaPlayer.create(this, R.raw.beep);


        currency=findViewById(R.id.currency);
        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Note",mynote);
                Log.i("Confidence", String.valueOf(myconfidence));
                if(mynote.equals("10")){
                    Toast.makeText(MainActivity.this, "I got 10 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if(mynote.equals("20")){
                    Toast.makeText(MainActivity.this, "I got 20 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if(mynote.equals("50")){
                    Toast.makeText(MainActivity.this, "I got 50 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if(mynote.equals("100")){
                    Toast.makeText(MainActivity.this, "I got 100 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if(mynote.equals("500")){
                    Toast.makeText(MainActivity.this, "I got 500 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if(mynote.equals("1000")){
                    Toast.makeText(MainActivity.this, "I got 1000 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if(mynote.equals("5000")){
                    Toast.makeText(MainActivity.this, "I got 5000 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
            }
        });


        selectimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName="photo";
                File storageDirectory=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                try {
                    File imageFile=File.createTempFile(fileName,".jpg",storageDirectory);
                    currentimagepath=imageFile.getAbsolutePath();

                    Uri imageuri=FileProvider.getUriForFile(MainActivity.this,"com.example.fyp.fileprovider",imageFile);

                    Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri);
                    startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        detecttext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detecttextfromimage();
            }
        });

        mTTS=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS","Initilization Failed");
                }
            }
        });

        mTTS1=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS1.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS1","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS1","Initilization Failed");
                }
            }
        });

        mTTS2=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS2.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS2","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS2","Initilization Failed");
                }
            }
        });

        mTTS3=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS3.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS3","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS3","Initilization Failed");
                }
            }
        });

        mTTS4=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS4.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS4","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS4","Initilization Failed");
                }
            }
        });

        mTTS5=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS5.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS5","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS5","Initilization Failed");
                }
            }
        });

        mTTS6=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS6.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS6","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS6","Initilization Failed");
                }
            }
        });

        mTTS7=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS7.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS7","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS7","Initilization Failed");
                }
            }
        });

        mTTS8=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS8.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS8","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS8","Initilization Failed");
                }
            }
        });

        mTTS9=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS9.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS9","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS9","Initilization Failed");
                }
            }
        });

        mTTS10=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS10.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS10","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS10","Initilization Failed");
                }
            }
        });

        mTTS11=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS11.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS11","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS11","Initilization Failed");
                }
            }
        });

        mTTS12=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS12.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS12","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS12","Initilization Failed");
                }
            }
        });

        mTTS13=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS13.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS13","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS13","Initilization Failed");
                }
            }
        });

        mTTS14=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS14.setLanguage(Locale.ENGLISH);

                    if (result ==TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("mTTS14","Language not supported");
                    }
                    else {
                        speak.setEnabled(true);
                    }
                }
                else {
                    Log.e("mTTS14","Initilization Failed");
                }
            }
        });

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myspeaker();
            }
        });

    }

    private void myspeaker() {
        mTTS.setPitch(1);
        mTTS.setSpeechRate(1);

        mTTS1.setPitch(1);
        mTTS1.setSpeechRate(1);

        mTTS2.setPitch(1);
        mTTS2.setSpeechRate(1);

        mTTS3.setPitch(1);
        mTTS3.setSpeechRate(1);

        mTTS4.setPitch(1);
        mTTS4.setSpeechRate(1);

        mTTS5.setPitch(1);
        mTTS5.setSpeechRate(1);

        mTTS6.setPitch(1);
        mTTS6.setSpeechRate(1);

        mTTS7.setPitch(1);
        mTTS7.setSpeechRate(1);

        //// Currency Notes

        mTTS8.setPitch(1);
        mTTS8.setSpeechRate(1);

        mTTS9.setPitch(1);
        mTTS9.setSpeechRate(1);

        mTTS10.setPitch(1);
        mTTS10.setSpeechRate(1);

        mTTS11.setPitch(1);
        mTTS11.setSpeechRate(1);

        mTTS12.setPitch(1);
        mTTS12.setSpeechRate(1);

        mTTS13.setPitch(1);
        mTTS13.setSpeechRate(1);

        mTTS14.setPitch(1);
        mTTS14.setSpeechRate(1);


        int a=0,b=0,c=0,d=0,e=0,f=0,g=0,h=0;

        if(!tempnote.isEmpty()){
            h=1;
        }

        String gotexpirydate="Got Expiry Date";
        String gotwarninglabels="Got Warning Labels";
        String gotproteinchart="Got Protien Information";
        String gotmfgdate="Got Manufacturing Date";
        String gotbestbeforedate="Got Best Before Date";
        String gotcaution="Got Caution Information";
        String gotingredient="Got Ingridents Information";

        // Currency Notes

        String got10ruppee="Got Ten Ruppee Note";
        String got20ruppee="Got Twenty Ruppee Note";
        String got50ruppee="Got Fifty Ruppee Note";
        String got100ruppee="Got hundred Ruppee Note";
        String got500ruppee="Got Five Hundred Ruppee Note";
        String got1000ruppee="Got Thousand Ruppee Note";
        String got5000ruppee="Got Five Thousand Ruppee Note";

        if(!texttospeak.equals("")){
            String small=texttospeak.toLowerCase();
            for(int i=0;i<expirydate.length;++i){
                Toast.makeText(this, "Inside Loop", Toast.LENGTH_SHORT).show();
                if(small.contains(expirydate[i])){
                    Toast.makeText(this, "Got Expiry", Toast.LENGTH_SHORT).show();
                    a=1;
                }
            }
            for(int i=0;i<warninglabels.length;++i){
                Toast.makeText(this, "Inside Loop", Toast.LENGTH_SHORT).show();
                if(small.contains(warninglabels[i])){
                    Toast.makeText(this, "Got warning", Toast.LENGTH_SHORT).show();
                    b=1;
                }
            }
            for(int i=0;i<proteinchart.length;++i){
                Toast.makeText(this, "Inside Loop", Toast.LENGTH_SHORT).show();
                if(small.contains(proteinchart[i])){
                    Toast.makeText(this, "Got Protein", Toast.LENGTH_SHORT).show();
                    c=1;
                }
            }
            for(int i=0;i<mfgdate.length;++i){
                Toast.makeText(this, "Inside Loop", Toast.LENGTH_SHORT).show();
                if(small.contains(mfgdate[i])){
                    Toast.makeText(this, "Got mfgdate", Toast.LENGTH_SHORT).show();
                    d=1;
                }
            }
            for(int i=0;i<bestbeforedate.length;++i){
                Toast.makeText(this, "Inside Loop", Toast.LENGTH_SHORT).show();
                if(small.contains(bestbeforedate[i])){
                    Toast.makeText(this, "Got bestbeforedate", Toast.LENGTH_SHORT).show();
                    e=1;
                }
            }
            for(int i=0;i<cautionmessage.length;++i){
                Toast.makeText(this, "Inside Loop", Toast.LENGTH_SHORT).show();
                if(small.contains(cautionmessage[i])){
                    Toast.makeText(this, "Got caution", Toast.LENGTH_SHORT).show();
                    f=1;
                }
            }
            for(int i=0;i<ingredients.length;++i){
                Toast.makeText(this, "Inside Loop", Toast.LENGTH_SHORT).show();
                if(small.contains(ingredients[i])){
                    Toast.makeText(this, "Got ingridents", Toast.LENGTH_SHORT).show();
                    g=1;
                }
            }
            if(a==1){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS1.speak(gotexpirydate,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(b==1){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS2.speak(gotwarninglabels,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(c==1){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS3.speak(gotproteinchart,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(d==1){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS4.speak(gotmfgdate,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(e==1){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS5.speak(gotbestbeforedate,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(f==1){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS6.speak(gotcaution,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(g==1){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS7.speak(gotingredient,TextToSpeech.QUEUE_FLUSH,null);
            }
        }

        if(h==1){
            if(tempnote.equals("10")){
                beepmediaplayer.start();
                beepmediaplayer.start();
                Toast.makeText(this, "Inside 10", Toast.LENGTH_SHORT).show();
                mTTS8.speak(got10ruppee,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(tempnote.equals("20")){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS9.speak(got20ruppee,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(tempnote.equals("50")){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS10.speak(got50ruppee,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(tempnote.equals("100")){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS11.speak(got100ruppee,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(tempnote.equals("500")){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS12.speak(got500ruppee,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(tempnote.equals("1000")){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS13.speak(got1000ruppee,TextToSpeech.QUEUE_FLUSH,null);
            }
            if(tempnote.equals("5000")){
                beepmediaplayer.start();
                beepmediaplayer.start();
                mTTS14.speak(got5000ruppee,TextToSpeech.QUEUE_FLUSH,null);
            }
        }
        mTTS.speak(texttospeak,TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    protected void onDestroy() {
        if (mTTS!=null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }


    private void detecttextfromimage() {
        FirebaseVisionImage image=FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionDocumentTextRecognizer detector= FirebaseVision.getInstance().getCloudDocumentTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                    @Override
                    public void onSuccess(FirebaseVisionDocumentText result) {
                        displaytextfromimage(result);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error : "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displaytextfromimage(FirebaseVisionDocumentText result) {

        texttospeak = result.getText();
        Toast.makeText(this, "Got The Text", Toast.LENGTH_SHORT).show();

    }

    private void setLabelerFromLocalModel(Uri uri) {
        localModel = new FirebaseAutoMLLocalModel.Builder()
                .setAssetFilePath("model/manifest.json")
                .build();
        try {
            FirebaseVisionOnDeviceAutoMLImageLabelerOptions options =
                    new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
                            .setConfidenceThreshold(0.0f)  // Evaluate your model in the Firebase console
                            // to determine an appropriate value.
                            .build();
            labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
            processImageLabeler(labeler, image);
        } catch (FirebaseMLException e) {
            // ...
        }
    }

    private void processImageLabeler(FirebaseVisionImageLabeler labeler, FirebaseVisionImage image) {
        labeler.processImage(image).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onComplete(@NonNull Task<List<FirebaseVisionImageLabel>> task) {
                for (FirebaseVisionImageLabel label : task.getResult()) {
                    String eachlabel = label.getText().toUpperCase();
                    float confidence = label.getConfidence();
                    Log.i("label",eachlabel);
                    Log.i("confidence", String.valueOf(confidence));
                    if(confidence>temp){
                        temp=confidence;
                        tempnote=eachlabel;
                    }
                }
                myconfidence=temp;
                mynote=tempnote;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("OnFail", "" + e);
                Toast.makeText(MainActivity.this, "Something went wrong! " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLabelerFromRemoteLabel(final Uri uri) {
        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isComplete()) {
                            optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel);
                            FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder
                                    .setConfidenceThreshold(0.0f)
                                    .build();
                            try {
                                labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
                                image = FirebaseVisionImage.fromFilePath(MainActivity.this, uri);
                                processImageLabeler(labeler, image);
                            } catch (FirebaseMLException | IOException exception) {
                                Log.e("TAG", "onSuccess: " + exception);
                                Toast.makeText(MainActivity.this, "Ml exeception", Toast.LENGTH_SHORT).show();
                            }
                        } else
                            Toast.makeText(MainActivity.this, "Not downloaded", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: "+e );
                Toast.makeText(MainActivity.this, "err"+e, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fromRemoteModel() {
        progressDialog.show();                                         /* model name*/
        remoteModel = new FirebaseAutoMLRemoteModel.Builder("Flowers_2020124223430").build();
        conditions = new FirebaseModelDownloadConditions.Builder().requireWifi().build();
        //first download the model
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Uri u = Uri.fromFile(new File(currentimagepath));
                Bitmap bitmap= BitmapFactory.decodeFile(currentimagepath);
                imageBitmap=bitmap;
                Picasso.get().setLoggingEnabled(true);
                Picasso.get().load(u).resize(300,300).onlyScaleDown().into(imageView);
                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show();
                //currecny Detector
                setLabelerFromLocalModel(u);

            }
        }
    }
}
