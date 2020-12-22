package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.pdf.PrintedPdfDocument;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.squareup.picasso.Picasso;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FypActivity extends AppCompatActivity {

    // numbers is use to select the specific option among all options

    int counter=0;
    int action;
    int keycode;

    PrintedPdfDocument pdfDocument;

    //Pick image path for different modules

    String currentimagepath = null;
    static final int REQUEST_IMAGE_CAPTURE1 = 1;
    static final int REQUEST_IMAGE_CAPTURE2 = 2;
    static final int REQUEST_IMAGE_CAPTURE3 = 3;
    static final int REQUEST_IMAGE_CAPTURE4 = 4;
    static final int REQUEST_IMAGE_CAPTURE5 = 5;

    //last pressed key inorder to detect and move the selected item to next module

    int lastpressed=0;

    //imageBitemap var is used to store all the images and overwrite the previous one

    Bitmap imageBitmap;

    //texttospeak is used to store the text reterived from the image by the firebase from document

    String texttospeak = "";

    //texttospeakforcritical is used to store the critical info reterived from the image by the firebase from document

    String texttospeakforcritical = "";

    //Audio

    TextToSpeech mTTS;

    //qrcode reader

    String qrdatatospeak = "";

    //objectsdetected for scene if %age of accuracy is less than 50%

    String objectdetected="";

    //image labler

    private FirebaseAutoMLLocalModel localModel,localModel1;

    //For running the image labeler

    FirebaseVisionImageLabeler labeler,labeler1;

    //currency and scence detection initializaiton

    String mynote,myscene;
    float myconfidence;
    float myconfidencescene;
    float temp = -999;
    float temp1 = -999;
    String tempnote = "";
    String tempscene="";

    // This Material Card is used for the text speaking module
    MaterialCardView textviewcard;
    // This Material Card is used for the Currency detecting module
    MaterialCardView currencyviewcard;
    // This Material Card is used for the QR code detecting module
    MaterialCardView qrcodeviewcard;
    // This Material Card is used for the critical information detection module
    MaterialCardView criticalviewcard;
    // This Material Card is used for the scene detection module
    MaterialCardView sceneviewcard;

    //critical information strings

    String gotexpirydate = "I Got Expiry Date";
    String gotwarninglabels = "I Got Warning Labels";
    String gotproteinchart = "I Got Protien Information";
    String gotmfgdate = "I Got Manufacturing Date";
    String gotbestbeforedate = "I Got Best Before Date";
    String gotcaution = "I Got Caution Information";
    String gotingredient = "I Got Ingridents Information";

    //labels to check in the critical information text

    String[] expirydate = {"expiry date", "exp", "days to expire", "expiration date", "daystoexpiry", "daystoexpire", "valid till", "exp", "expiry", "used by"};
    String[] mfgdate = {"mfg", "mfg date", "mfgdate", "createdon", "mfg.date"};
    String[] bestbeforedate = {"best before", "best before date", "bestbefore"};
    String[] warninglabels = {"warning label", "warning", "warninglabel", "danger", "keep out", "hazard", "danger"};
    String[] cautionmessage = {"caution", "caution messages", "cautions", "be careful", "do not cross"};
    String[] ingredients = {"recipe", "ingredients", "ingredient list"};
    String[] proteinchart = {"protein chart", "protein"};

    // Currency Notes text to speak depending upon the probability of the detection

    String got10ruppee = "Got Ten Ruppee Note";
    String got20ruppee = "Got Twenty Ruppee Note";
    String got50ruppee = "Got Fifty Ruppee Note";
    String got100ruppee = "Got hundred Ruppee Note";
    String got500ruppee = "Got Five Hundred Ruppee Note";
    String got1000ruppee = "Got Thousand Ruppee Note";
    String got5000ruppee = "Got Five Thousand Ruppee Note";

    //var initialization inorder to get the content inside the qr-code

    FirebaseVisionBarcodeDetectorOptions options;
    FirebaseVisionBarcodeDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fyp);

        //Getting layouts from their id's

        //Text module layout
        textviewcard=findViewById(R.id.textviewcard);

        //Currency module layout

        currencyviewcard=findViewById(R.id.currencyviewcard);

        //QR code module layout

        qrcodeviewcard=findViewById(R.id.qrcodeviewcard);

        //Critical Information module layout

        criticalviewcard=findViewById(R.id.criticalviewcard);

        //Scene Detection module layout

        sceneviewcard=findViewById(R.id.sceneviewcard);

        //text speech initiallization

        mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTTS.setLanguage(Locale.ENGLISH);
            }
        });


    }

    // Dispatch key events inorder to detect the volume button pressed and perform events on the basis of those events

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        action=event.getAction();
        keycode=event.getKeyCode();

        switch (keycode){
            case KeyEvent.KEYCODE_VOLUME_UP:{
                if(KeyEvent.ACTION_UP==action){
                    lastpressed=1;
                    nextitem();
                }
                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN:{
                if(KeyEvent.ACTION_DOWN==action){
                    lastpressed=2;
                    optionselected();
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    //optionselected function is called depending upon the event called by used which is eith up or down
    //By using the up button we move the option to next module and upon pressing the lower button we opt that option

    private void optionselected() {
        Toast.makeText(this, "Down Key", Toast.LENGTH_SHORT).show();
        AudioManager am =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        assert am != null;
        am.setStreamVolume(
                AudioManager.STREAM_RING,
                am.getStreamMaxVolume(AudioManager.STREAM_RING),
                0);
        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);

        if(counter==1){
            if(lastpressed==2){
                mTTS.speak("Text Detection Selected", TextToSpeech.QUEUE_FLUSH, null);
                pickimage(REQUEST_IMAGE_CAPTURE1);
            }
        }
        else if(counter==2){
            if(lastpressed==2){
                mTTS.speak("Currency Detection Selected", TextToSpeech.QUEUE_FLUSH, null);
                pickimage(REQUEST_IMAGE_CAPTURE2);
            }
        }
        else if(counter==3){
            if(lastpressed==2){
                mTTS.speak("Q R Code Detection Selected", TextToSpeech.QUEUE_FLUSH, null);
                pickimage(REQUEST_IMAGE_CAPTURE3);
            }
        }
        else if(counter==4){
            if(lastpressed==2){
                mTTS.speak("Critical Information Detection Selected", TextToSpeech.QUEUE_FLUSH, null);
                pickimage(REQUEST_IMAGE_CAPTURE4);
            }
        }
        else if(counter==5){
            mTTS.speak("Scene Detection Selected", TextToSpeech.QUEUE_FLUSH, null);
            pickimage(REQUEST_IMAGE_CAPTURE5);
        }

    }

    private void pickimage(int x) {

//        if (mTTS != null) {
//            mTTS.stop();
//        }

        // Call image pick from the gallary and send module var along to detect the module working...

        int myvar=x;
        String fileName = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
            currentimagepath = imageFile.getAbsolutePath();

            Uri imageuri = FileProvider.getUriForFile(FypActivity.this, "com.example.fyp.fileprovider", imageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra("android.intent.extra.quickCapture",true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
            startActivityForResult(intent, myvar);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //nextitem will be marked and focused by rounding yellow border
    //and the last one and the other one are un-marked

    @SuppressLint("ResourceAsColor")
    private void nextitem() {
        counter++;
        if(counter==1){
            Toast.makeText(this, "Counter 1", Toast.LENGTH_SHORT).show();
            textviewcard.setStrokeColor(R.color.myyellow);
            textviewcard.setStrokeWidth(25);


            currencyviewcard.setStrokeWidth(0);
            qrcodeviewcard.setStrokeWidth(0);
            criticalviewcard.setStrokeWidth(0);
            sceneviewcard.setStrokeWidth(0);
//            currencyviewcard.setStrokeColor(R.color.NoStroke);
//            qrcodeviewcard.setStrokeColor(R.color.NoStroke);
//            criticalviewcard.setStrokeColor(R.color.NoStroke);
//            sceneviewcard.setStrokeColor(R.color.NoStroke);

            mTTS.speak("Text Detection", TextToSpeech.QUEUE_FLUSH, null);



        }
        else if(counter==2){
            Toast.makeText(this, "Counter 2", Toast.LENGTH_SHORT).show();
            currencyviewcard.setStrokeColor(R.color.myyellow);
            currencyviewcard.setStrokeWidth(25);


//            qrcodeviewcard.setStrokeColor(R.color.NoStroke);
//            criticalviewcard.setStrokeColor(R.color.NoStroke);
//            sceneviewcard.setStrokeColor(R.color.NoStroke);
//            textviewcard.setStrokeColor(R.color.NoStroke);
            qrcodeviewcard.setStrokeWidth(0);
            criticalviewcard.setStrokeWidth(0);
            sceneviewcard.setStrokeWidth(0);
            textviewcard.setStrokeWidth(0);

            mTTS.speak("Currency Detection", TextToSpeech.QUEUE_FLUSH, null);


        }
        else if(counter==3){
            Toast.makeText(this, "Counter 3", Toast.LENGTH_SHORT).show();
            qrcodeviewcard.setStrokeColor(R.color.myyellow);
            qrcodeviewcard.setStrokeWidth(25);

//            criticalviewcard.setStrokeColor(R.color.NoStroke);
//            sceneviewcard.setStrokeColor(R.color.NoStroke);
//            textviewcard.setStrokeColor(R.color.NoStroke);
//            currencyviewcard.setStrokeColor(R.color.NoStroke);
            criticalviewcard.setStrokeWidth(0);
            sceneviewcard.setStrokeWidth(0);
            textviewcard.setStrokeWidth(0);
            currencyviewcard.setStrokeWidth(0);

            mTTS.speak("Q R Code Detection", TextToSpeech.QUEUE_FLUSH, null);


        }
        else if(counter==4){
            Toast.makeText(this, "Counter 4", Toast.LENGTH_SHORT).show();
            criticalviewcard.setStrokeColor(R.color.myyellow);
            criticalviewcard.setStrokeWidth(25);

//            sceneviewcard.setStrokeColor(R.color.NoStroke);
//            textviewcard.setStrokeColor(R.color.NoStroke);
//            currencyviewcard.setStrokeColor(R.color.NoStroke);
//            qrcodeviewcard.setStrokeColor(R.color.NoStroke);
            sceneviewcard.setStrokeWidth(0);
            textviewcard.setStrokeWidth(0);
            currencyviewcard.setStrokeWidth(0);
            qrcodeviewcard.setStrokeWidth(0);

            mTTS.speak("Critical Information Detection", TextToSpeech.QUEUE_FLUSH, null);


        }
        else if(counter==5){
            Toast.makeText(this, "Counter 5", Toast.LENGTH_SHORT).show();
            sceneviewcard.setStrokeColor(R.color.myyellow);
            sceneviewcard.setStrokeWidth(25);

//            textviewcard.setStrokeColor(R.color.NoStroke);
//            currencyviewcard.setStrokeColor(R.color.NoStroke);
//            qrcodeviewcard.setStrokeColor(R.color.NoStroke);
//            criticalviewcard.setStrokeColor(R.color.NoStroke);
            textviewcard.setStrokeWidth(0);
            currencyviewcard.setStrokeWidth(0);
            qrcodeviewcard.setStrokeWidth(0);
            criticalviewcard.setStrokeWidth(0);

            mTTS.speak("Scene Detection", TextToSpeech.QUEUE_FLUSH, null);


        }
        else if(counter==6){
            counter=0;
            nextitem();
        }
    }

    // onActivityresult is the defualt function and it is override and used to capture image and save in the bitmap for further use

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                // Make sure the request was successful
                Uri u = Uri.fromFile(new File(currentimagepath));
                Bitmap bitmap = BitmapFactory.decodeFile(currentimagepath);
                imageBitmap = bitmap;
                detecttextfromimage();
            }
        }
        else if(requestCode==2){
            if (resultCode == RESULT_OK) {

                // Make sure the request was successful
                Uri u = Uri.fromFile(new File(currentimagepath));
                Bitmap bitmap = BitmapFactory.decodeFile(currentimagepath);
                imageBitmap = bitmap;
                //currecny Detector
                setLabelerFromLocalModel(u);
            }
        }
        else if(requestCode==3){
            if (resultCode == RESULT_OK) {

                // Make sure the request was successful
                Uri u = Uri.fromFile(new File(currentimagepath));
                Bitmap bitmap = BitmapFactory.decodeFile(currentimagepath);
                imageBitmap = bitmap;
                //qrcode Detector
                qrcodereader(imageBitmap);
            }
        }
        else if(requestCode==4){
            if (resultCode == RESULT_OK) {

                // Make sure the request was successful
                Uri u = Uri.fromFile(new File(currentimagepath));
                Bitmap bitmap = BitmapFactory.decodeFile(currentimagepath);
                imageBitmap = bitmap;
                //Critical information Detector
                detecttextfromimageforcriticaldata(imageBitmap);
            }
        }
        else if(requestCode==5){
            if (resultCode == RESULT_OK) {

                // Make sure the request was successful
                Uri u = Uri.fromFile(new File(currentimagepath));
                Bitmap bitmap = BitmapFactory.decodeFile(currentimagepath);
                imageBitmap = bitmap;
                //Scene Detector
                setLabelerFromLocalModelforscene(imageBitmap);
            }
        }
    }

    //this function is used for the detecing the image from the camera and detct the scene and speck it out depending upon the results
    //from the results of out models

    private void setLabelerFromLocalModelforscene(Bitmap imageBitmap) {
        localModel1 = new FirebaseAutoMLLocalModel.Builder()
                .setAssetFilePath("model/manifest1.json")
                .build();
        try {
            FirebaseVisionOnDeviceAutoMLImageLabelerOptions options =
                    new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel1)
                            .setConfidenceThreshold(0.0f)  // Evaluate your model in the Firebase console
                            // to determine an appropriate value.
                            .build();
            labeler1 = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
            processImageLabeler1(labeler1, image);
        } catch (FirebaseMLException e) {
            // ...
        }
    }
    private void processImageLabeler1(FirebaseVisionImageLabeler labeler1, FirebaseVisionImage image) {
        labeler1.processImage(image).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onComplete(@NonNull Task<List<FirebaseVisionImageLabel>> task) {
                for (FirebaseVisionImageLabel label : task.getResult()) {
                    String eachlabel = label.getText().toUpperCase();
                    float confidence = label.getConfidence();
                    Log.d("fijwejhfiuew", eachlabel);
                    Log.d("fijwejhfiuew", String.valueOf(confidence));
                    if (confidence > temp1) {
                        temp1 = confidence;
                        tempscene = eachlabel;
                    }
                }

                // these confidence will override the previous confidence if the current confidence is more than
                // the previous one and save it for the finall speech

                myconfidencescene=temp1;
                myscene=tempscene;
                if(myconfidencescene<0.50){
                    detectobjecttotell(imageBitmap);
                }
                else{
                    mTTS.speak("I think it is a "+myscene, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("OnFail", "" + e);
                Toast.makeText(FypActivity.this, "Something went wrong! " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //If the percentage of confidence on the scene is less than 50% then we are sending image to the
    //firebase and detect the objects in it by sending it to firebase and getting the resuts back from it

    private void detectobjecttotell(Bitmap bitmap) {
        //////// object detection ///////////
        FirebaseVisionImage image = null;
        image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionImageLabeler labeler=FirebaseVision.getInstance()
                .getOnDeviceImageLabeler();

        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                for (FirebaseVisionImageLabel label : labels) {
                    String text = label.getText();
                    float confidence = label.getConfidence();
                    String entityid = label.getEntityId();

                    Log.d("FirebaseImageObjects","Text:"+text+"     " +
                            "confidence:"+confidence+"      "+
                            "entityid:"+entityid);

                    Toast.makeText(FypActivity.this, text, Toast.LENGTH_SHORT).show();
                    objectdetected=objectdetected+text;
                    objectdetected=objectdetected+"  ";
                }
                mTTS.speak("I am not sure about scene but i see "+objectdetected, TextToSpeech.QUEUE_FLUSH, null);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FirebaseImageObjects","Error "+e);
                    }
                });


        ///////// object detection //////////
    }

    // Here is the function to read the content from the qrcode this function will be called from the image picked
    //This will send the qr-code to the firebase and it will send the content back to us and speech it aloud

    private void qrcodereader(Bitmap bitmap) {
        ///////////Bar code/////////////////

        qrdatatospeak="";
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE)
                        .build();
        detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);
        final Task<List<FirebaseVisionBarcode>> result = detector.
                detectInImage(FirebaseVisionImage.fromBitmap(bitmap));
        result.addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                        for (FirebaseVisionBarcode barcode : firebaseVisionBarcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Toast.makeText(FypActivity.this, "Detecting Objects", Toast.LENGTH_SHORT).show();
                            Log.d("FirebaseVisionBarcode", "Bounds " + bounds);
                            Point[] corners = barcode.getCornerPoints();
                            Log.d("FirebaseVisionBarcode", "Corners " + corners);
                            String rawValue = barcode.getRawValue();
                            qrdatatospeak = rawValue;
                            Log.d("FirebaseVisionBarcode", "RawValue " + rawValue);
                            int valueType = barcode.getValueType();
                            switch (valueType) {
                                case FirebaseVisionBarcode.TYPE_WIFI:
                                    String ssid = Objects.requireNonNull(barcode.getWifi()).getSsid();
                                    String password = barcode.getWifi().getPassword();
                                    int type = barcode.getWifi().getEncryptionType();

                                    Log.d("FirebaseVisionBarcode", "Ssid "
                                            + ssid);
                                    Log.d("FirebaseVisionBarcode",
                                            "Password " + password);
                                    Log.d("FirebaseVisionBarcode", "Type " +
                                            "" + type);
                                    break;
                                case FirebaseVisionBarcode.TYPE_URL:
                                    String title = Objects.requireNonNull(barcode.getUrl()).getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    Log.d("FirebaseVisionBarcode",
                                            "Title " + title);
                                    Log.d("FirebaseVisionBarcode",
                                            "Url " + url);
                                    break;
                            }
                        }

                        if (!qrdatatospeak.equals("")) {
                            mTTS.speak("There is also Q R Data I got. Here it is",
                                    TextToSpeech.QUEUE_FLUSH, null);

                            mTTS.speak(qrdatatospeak, TextToSpeech.QUEUE_FLUSH, null);

                        } else {
                            mTTS.speak("There is No Q R Data.",
                                    TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FirebaseVisionBarcode", "Error " + e);
                    }
                });
        //////////////bar code////////////
    }

    //This function will be called when user want to detect the critical information like expiry date, manufacture date
    //and other critical infomation like this
    //There information will be detected on the basis of the content we get back from the firebase
    
    private void detecttextfromimageforcriticaldata(Bitmap imageBitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance().getCloudDocumentTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                    @Override
                    public void onSuccess(FirebaseVisionDocumentText result) {
                        Log.d("theText", String.valueOf(result));
                        displaytextfromimageforcriticaldata(result);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FypActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void displaytextfromimageforcriticaldata(FirebaseVisionDocumentText result) {

        int x=0;
        texttospeakforcritical = result.getText();
        Log.d("thisisthetextigot", texttospeakforcritical);
        Toast.makeText(this, "Got The Text", Toast.LENGTH_SHORT).show();
        if (!texttospeakforcritical.equals("")) {
            String small = texttospeakforcritical.toLowerCase();
            for (String s : expirydate) {
                if (small.contains(s)) {
                    x = 1;
                    mTTS.speak(gotexpirydate+" "+small, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(FypActivity.this, "I Got Expiry", Toast.LENGTH_SHORT).show();
                }
            }
            for (String warninglabel : warninglabels) {
                if (small.contains(warninglabel)) {
                    x = 1;
                    mTTS.speak(gotwarninglabels+" "+small, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(FypActivity.this, "I Got warning", Toast.LENGTH_SHORT).show();

                }
            }
            for (String s : proteinchart) {
                if (small.contains(s)) {
                    x = 1;
                    mTTS.speak(gotproteinchart+" "+small, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(FypActivity.this, "I Got Protein", Toast.LENGTH_SHORT).show();

                }
            }
            for (String s : mfgdate) {
                if (small.contains(s)) {
                    x = 1;
                    mTTS.speak(gotmfgdate+" "+small, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(FypActivity.this, "I Got mfgdate", Toast.LENGTH_SHORT).show();

                }
            }
            for (String s : bestbeforedate) {
                if (small.contains(s)) {
                    x = 1;
                    mTTS.speak(gotbestbeforedate+" "+small, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(FypActivity.this, "I Got bestbeforedate", Toast.LENGTH_SHORT).show();

                }
            }
            for (String s : cautionmessage) {
                if (small.contains(s)) {
                    x = 1;
                    mTTS.speak(gotcaution+" "+small, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(FypActivity.this, "I Got caution", Toast.LENGTH_SHORT).show();

                }
            }
            for (String ingredient : ingredients) {
                if (small.contains(ingredient)) {
                    x = 1;
                    mTTS.speak(gotingredient+" "+small, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(FypActivity.this, "I Got ingredients", Toast.LENGTH_SHORT).show();

                }
            }
        }
        if (x == 0) {
            mTTS.speak("There is no Critical Information", TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    
    //This function will send iamge to firebaase and get the content back from the firebase

    private void detecttextfromimage() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance().getCloudDocumentTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                    @Override
                    public void onSuccess(FirebaseVisionDocumentText result) {
                        Log.d("theText", String.valueOf(result));
                        displaytextfromimage(result);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FypActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void displaytextfromimage(FirebaseVisionDocumentText result) {

        texttospeak = result.getText();
        Log.d("thisisthetextigot", texttospeak);
        Toast.makeText(this, "Got The Text", Toast.LENGTH_SHORT).show();
        if (!texttospeak.equals("")) {
            mTTS.speak(texttospeak, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    private MappedByteBuffer loadmodelfile(MainActivity mainActivity) throws IOException {
        AssetFileDescriptor fileDescriptor=mainActivity.getAssets().openFd("model.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startoffset,declaredLength);
    }
    
    // This is the function that will be called when we select the currency module. It will pass the bitmap to the model that is stored in the local storage 
    // and send the result back on the basis of confidence and confidence will be overridden depending upon the probability
    // and speech the currency notes

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
    private void processImageLabeler(FirebaseVisionImageLabeler labeler,
                                     FirebaseVisionImage image) {
        labeler.processImage(image).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onComplete(@NonNull Task<List<FirebaseVisionImageLabel>> task) {
                for (FirebaseVisionImageLabel label : task.getResult()) {
                    String eachlabel = label.getText().toUpperCase();
                    float confidence = label.getConfidence();
                    Log.i("label", eachlabel);
                    Log.i("confidence", String.valueOf(confidence));
                    if (confidence > temp) {
                        temp = confidence;
                        tempnote = eachlabel;
                    }
                }
                myconfidence = temp;
                mynote = tempnote;
                Log.d("mynote", mynote + " " + myconfidence);

                speakfornote();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("OnFail", "" + e);
                Toast.makeText(FypActivity.this, "Something went wrong! " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    //this function will be called upon the final selection of the currency note and speak it up

    private void speakfornote() {
        Log.i("Note", mynote);
        Log.i("Confidence", String.valueOf(myconfidence));
        if (mynote.equals("10")) {
//                    myspeaker();
            mTTS.speak(got10ruppee, TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(FypActivity.this, "I got 10 Ruppee Note", Toast.LENGTH_SHORT).show();
        }
        if (mynote.equals("20")) {
//                    myspeaker();
            mTTS.speak(got20ruppee, TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(FypActivity.this, "I got 20 Ruppee Note", Toast.LENGTH_SHORT).show();
        }
        if (mynote.equals("50")) {
//                    myspeaker();
            mTTS.speak(got50ruppee, TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(FypActivity.this, "I got 50 Ruppee Note", Toast.LENGTH_SHORT).show();
        }
        if (mynote.equals("100")) {
//                    myspeaker();
            mTTS.speak(got100ruppee, TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(FypActivity.this, "I got 100 Ruppee Note", Toast.LENGTH_SHORT).show();
        }
        if (mynote.equals("500")) {
//                    myspeaker();
            mTTS.speak(got500ruppee, TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(FypActivity.this, "I got 500 Ruppee Note", Toast.LENGTH_SHORT).show();
        }
        if (mynote.equals("1000")) {
//                    myspeaker();
            mTTS.speak(got1000ruppee, TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(FypActivity.this, "I got 1000 Ruppee Note", Toast.LENGTH_SHORT).show();
        }
        if (mynote.equals("5000")) {
//                    myspeaker();
            mTTS.speak(got5000ruppee, TextToSpeech.QUEUE_FLUSH, null);
            Toast.makeText(FypActivity.this, "I got 5000 Ruppee Note", Toast.LENGTH_SHORT).show();
        }
    }


}
