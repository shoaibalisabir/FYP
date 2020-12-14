package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
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
import com.google.android.gms.vision.label.ImageLabel;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import com.squareup.picasso.Picasso;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileStore;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Button selectimage, detecttext, detectcriticaldata
            , currency, detectqrdata,detectobjects, detectmyscene;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;
    String currentimagepath = null;
    TextToSpeech mTTS;
    String texttospeak = "";
    String[] expirydate = {"expiry date", "exp", "days to expire", "expiration date", "daystoexpiry", "daystoexpire", "valid till", "exp", "expiry", "used by"};
    String[] mfgdate = {"mfg", "mfg date", "mfgdate", "createdon", "mfg.date"};
    String[] bestbeforedate = {"best before", "best before date", "bestbefore"};
    String[] warninglabels = {"warning label", "warning", "warninglabel", "danger", "keep out", "hazard", "danger"};
    String[] cautionmessage = {"caution", "caution messages", "cautions", "be careful", "do not cross"};
    String[] ingredients = {"recipe", "ingredients", "ingredient list"};
    String[] proteinchart = {"protein chart", "protein"};
    String mynote,myscene;
    float myconfidence;
    float myconfidencescene;
    float temp = -999;
    float temp1 = -999;
    String tempnote = "";
    String tempscene="";

    String qrdatatospeak = "";

    String objectdetected="";


    protected Interpreter tflite;
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 1.0f;
    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 255.0f;
    private MappedByteBuffer tfliteModel;
    private TensorImage inputImageBuffer;
    private TensorBuffer outputProbabilityBuffer;
    private TensorProcessor probabilityProcessor;
    private  int imageSizeX;
    private  int imageSizeY;
    private List<String> labels;


    String gotexpirydate = "I Got Expiry Date";
    String gotwarninglabels = "I Got Warning Labels";
    String gotproteinchart = "I Got Protien Information";
    String gotmfgdate = "I Got Manufacturing Date";
    String gotbestbeforedate = "I Got Best Before Date";
    String gotcaution = "I Got Caution Information";
    String gotingredient = "I Got Ingridents Information";


    FirebaseAutoMLRemoteModel remoteModel; // For loading the model remotely
    FirebaseVisionImageLabeler labeler,labeler1; //For running the image labeler
    FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder; // Which option is use to run the labeler local or remotely
    ProgressDialog progressDialog; //Show the progress dialog while model is downloading...
    FirebaseModelDownloadConditions conditions; //Conditions to download the model
    FirebaseVisionImage image; // preparing the input image
    private FirebaseAutoMLLocalModel localModel,localModel1;

    MediaPlayer beepmediaplayer;

    @Override
    protected void onPause() {
        super.onPause();
    }

    ImageView imageView;


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity.this, MySensorService.class);
        startService(intent);
    }


    // Currency Notes

    String got10ruppee = "Got Ten Ruppee Note";
    String got20ruppee = "Got Twenty Ruppee Note";
    String got50ruppee = "Got Fifty Ruppee Note";
    String got100ruppee = "Got hundred Ruppee Note";
    String got500ruppee = "Got Five Hundred Ruppee Note";
    String got1000ruppee = "Got Thousand Ruppee Note";
    String got5000ruppee = "Got Five Thousand Ruppee Note";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectimage = findViewById(R.id.selectimage);
        detecttext = findViewById(R.id.detecttext);
        detectcriticaldata = findViewById(R.id.speak);
        imageView = findViewById(R.id.imageview);
        beepmediaplayer = MediaPlayer.create(this, R.raw.beep);
        detectqrdata = findViewById(R.id.detectqrdata);
        detectmyscene=findViewById(R.id.detectmyscene);

        detectmyscene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String whatsceneisdetected=myscene+"   "+myconfidencescene;
                Intent intent=new Intent(MainActivity.this,DetectObjects.class);
                Log.d("gwwegewfg",whatsceneisdetected);
                intent.putExtra("detctobjects",whatsceneisdetected);
                startActivity(intent);
            }
        });

        detectobjects=findViewById(R.id.detectobjects);

        mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTTS.setLanguage(Locale.ENGLISH);
            }
        });


        try{
            tflite=new Interpreter(loadmodelfile(this));
        }catch (Exception e) {
            e.printStackTrace();
        }


        detectqrdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!qrdatatospeak.equals("")) {
                    mTTS.speak("There is also Q R Data I got. Here it is",
                            TextToSpeech.QUEUE_FLUSH, null);

                    mTTS.speak(qrdatatospeak, TextToSpeech.QUEUE_FLUSH, null);

                } else {
                    mTTS.speak("There is No Q R Data.",
                            TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        currency = findViewById(R.id.currency);
        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Note", mynote);

                ///// tflite //////////
/*
                int imageTensorIndex = 0;
                int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
                imageSizeY = imageShape[1];
                imageSizeX = imageShape[2];
                DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

                int probabilityTensorIndex = 0;
                int[] probabilityShape =
                        tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
                DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

                inputImageBuffer = new TensorImage(imageDataType);
                outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
                probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();

                inputImageBuffer = loadImage(imageBitmap);

                tflite.run(inputImageBuffer.getBuffer(),outputProbabilityBuffer.getBuffer().rewind());
                showresult();
*/
                ////// tflite //////


                Log.i("Confidence", String.valueOf(myconfidence));
                if (mynote.equals("10")) {
//                    myspeaker();
                    mTTS.speak(got10ruppee, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(MainActivity.this, "I got 10 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if (mynote.equals("20")) {
//                    myspeaker();
                    mTTS.speak(got20ruppee, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(MainActivity.this, "I got 20 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if (mynote.equals("50")) {
//                    myspeaker();
                    mTTS.speak(got50ruppee, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(MainActivity.this, "I got 50 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if (mynote.equals("100")) {
//                    myspeaker();
                    mTTS.speak(got100ruppee, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(MainActivity.this, "I got 100 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if (mynote.equals("500")) {
//                    myspeaker();
                    mTTS.speak(got500ruppee, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(MainActivity.this, "I got 500 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if (mynote.equals("1000")) {
//                    myspeaker();
                    mTTS.speak(got1000ruppee, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(MainActivity.this, "I got 1000 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
                if (mynote.equals("5000")) {
//                    myspeaker();
                    mTTS.speak(got5000ruppee, TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(MainActivity.this, "I got 5000 Ruppee Note", Toast.LENGTH_SHORT).show();
                }
            }
        });


        selectimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = "photo";
                File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                try {
                    File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                    currentimagepath = imageFile.getAbsolutePath();

                    Uri imageuri = FileProvider.getUriForFile(MainActivity.this, "com.example.fyp.fileprovider", imageFile);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        detectobjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,DetectObjects.class);
                Log.d("gwwegewfg",objectdetected);
                intent.putExtra("detctobjects",objectdetected);
                startActivity(intent);
            }
        });

        detecttext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!texttospeak.equals("")) {
                    mTTS.speak(texttospeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });


        detectcriticaldata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = 0;
                if (!texttospeak.equals("")) {
                    String small = texttospeak.toLowerCase();
                    for (String s : expirydate) {
                        if (small.contains(s)) {
                            x = 1;
                            mTTS.speak(gotexpirydate, TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(MainActivity.this, "I Got Expiry", Toast.LENGTH_SHORT).show();
                        }
                    }
                    for (String warninglabel : warninglabels) {
                        if (small.contains(warninglabel)) {
                            x = 1;
                            mTTS.speak(gotwarninglabels, TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(MainActivity.this, "I Got warning", Toast.LENGTH_SHORT).show();
                        }
                    }
                    for (String s : proteinchart) {
                        if (small.contains(s)) {
                            x = 1;
                            mTTS.speak(gotproteinchart, TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(MainActivity.this, "I Got Protein", Toast.LENGTH_SHORT).show();
                        }
                    }
                    for (String s : mfgdate) {
                        if (small.contains(s)) {
                            x = 1;
                            mTTS.speak(gotmfgdate, TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(MainActivity.this, "I Got mfgdate", Toast.LENGTH_SHORT).show();
                        }
                    }
                    for (String s : bestbeforedate) {
                        if (small.contains(s)) {
                            x = 1;
                            mTTS.speak(gotbestbeforedate, TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(MainActivity.this, "I Got bestbeforedate", Toast.LENGTH_SHORT).show();
                        }
                    }
                    for (String s : cautionmessage) {
                        if (small.contains(s)) {
                            x = 1;
                            mTTS.speak(gotcaution, TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(MainActivity.this, "I Got caution", Toast.LENGTH_SHORT).show();
                        }
                    }
                    for (String ingredient : ingredients) {
                        if (small.contains(ingredient)) {
                            x = 1;
                            mTTS.speak(gotingredient, TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(MainActivity.this, "I Got ingridents", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if (x == 0) {
                    mTTS.speak("There is no Critical Information", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    private MappedByteBuffer loadmodelfile(MainActivity mainActivity) throws IOException {
        AssetFileDescriptor fileDescriptor=mainActivity.getAssets().openFd("model.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startoffset,declaredLength);
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }


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
                        Toast.makeText(MainActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displaytextfromimage(FirebaseVisionDocumentText result) {

        texttospeak = result.getText();
        Log.d("thisisthetextigot", texttospeak);
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

    private void setLabelerFromLocalModelforscene(Uri uri) {
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

    private void processImageLabeler(FirebaseVisionImageLabeler labeler, FirebaseVisionImage image) {
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
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("OnFail", "" + e);
                Toast.makeText(MainActivity.this, "Something went wrong! " + e, Toast.LENGTH_SHORT).show();
            }
        });
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
                myconfidencescene=temp1;
                myscene=tempscene;
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
                Log.e("TAG", "onFailure: " + e);
                Toast.makeText(MainActivity.this, "err" + e, Toast.LENGTH_SHORT).show();
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

    private TensorImage loadImage(final Bitmap bitmap) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(getPreprocessNormalizeOp())
                        .build();
        return imageProcessor.process(inputImageBuffer);
    }
    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }
    private TensorOperator getPostprocessNormalizeOp(){
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }
    private void showresult(){

        try{
            labels = FileUtil.loadLabels(this,"newdict.txt");
        }catch (Exception e){
            e.printStackTrace();
        }
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                        .getMapWithFloatValue();
        float maxValueInMap =(Collections.max(labeledProbability.values()));

        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
            if (entry.getValue()==maxValueInMap) {
                Log.d("fwiejfbjiwf",entry.getKey());
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        qrdatatospeak = "";
        objectdetected="";

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Uri u = Uri.fromFile(new File(currentimagepath));
                Bitmap bitmap = BitmapFactory.decodeFile(currentimagepath);
                imageBitmap = bitmap;
                Picasso.get().setLoggingEnabled(true);
                Picasso.get().load(u).resize(300, 300).onlyScaleDown().into(imageView);
                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show();
                //currecny Detector
                setLabelerFromLocalModel(u);
                //text detector
                detecttextfromimage();
                //scene detector
                setLabelerFromLocalModelforscene(u);

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

                            Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
                            objectdetected=objectdetected+text;
                            objectdetected=objectdetected+"  ";


                        }
                        Toast.makeText(MainActivity.this, "Just above Log:"+objectdetected, Toast.LENGTH_SHORT).show();
                        Log.d("detctobjects", objectdetected);

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("FirebaseImageObjects","Error "+e);
                            }
                        });


                ///////// object detection //////////


                ///////////Bar code/////////////////

                FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                        .getVisionBarcodeDetector();

                final Task<List<FirebaseVisionBarcode>> result = detector.
                        detectInImage(FirebaseVisionImage.fromBitmap(bitmap)).
                        addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                                for (FirebaseVisionBarcode barcode : firebaseVisionBarcodes) {
                                    Rect bounds = barcode.getBoundingBox();
                                    Toast.makeText(MainActivity.this, "Detecting Objects", Toast.LENGTH_SHORT).show();
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
        }
    }
}
