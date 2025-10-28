package com.uyscuti.social.circuit.utils.audiomixer.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.uyscuti.social.circuit.R;
import com.uyscuti.social.circuit.utils.audiomixer.AudioMixer;
import com.uyscuti.social.circuit.utils.audiomixer.input.AudioInput;
import com.uyscuti.social.circuit.utils.audiomixer.input.BlankAudioInput;
import com.uyscuti.social.circuit.utils.audiomixer.input.GeneralAudioInput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final int AUDIO_CHOOSE_REQUEST_CODE = 1;

    private Activity activity;

    private final String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "audio_mixer_output.mp3";

    private final List<Input> inputs = new ArrayList<>();
    private AudioMixer audioMixer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_mixer_layout);

        activity = this;

        findViewById(R.id.add_audio_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChooser();
            }
        });

        findViewById(R.id.mix_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputs.size() < 1) {
                    Toast.makeText(activity, "Add at least one audio.", Toast.LENGTH_SHORT).show();
                    startMixing();
                } else {
                    startMixing();
                }
            }
        });

        findViewById(R.id.remove_audio_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputs.size() > 0) {
                    inputs.remove(inputs.size() - 1);
                    Toast.makeText(activity, "Last audio removed. Number of inputs: " + inputs.size(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "No audio added.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.video_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the MainActivity
//                Intent intent = new Intent(AudioActivity.this, MainActivity.class);
//
//                // Add any additional data or flags if needed
//                // intent.putExtra("key", "value");
//
//                // Start the activity
//                startActivity(intent);
            }
        });

        checkPermission();
    }

    private void startMixing() {
        //For showing progress
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mixing audio...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);

        String audioOutPutPath = getOutputFilePath();

        try {
            audioMixer = new AudioMixer(audioOutPutPath);

            for (Input input : inputs) {
                AudioInput audioInput;
                if (input.uri != null) {
                    GeneralAudioInput ai = new GeneralAudioInput(activity, input.uri, null);
                    ai.setStartOffsetUs(input.startOffsetUs);
                    ai.setStartTimeUs(input.startTimeUs); // optional
                    ai.setEndTimeUs(input.endTimeUs); // optional
                    //ai.setVolume(0.5f); //optional

                    audioInput = ai;
                } else {
                    audioInput = new BlankAudioInput(5000000);
                }
                audioMixer.addDataSource(audioInput);
            }
            try {
                audioMixer.setMixingType(AudioMixer.MixingType.SEQUENTIAL);
                audioMixer.setProcessingListener(new AudioMixer.ProcessingListener() {
                    @Override
                    public void onProgress(double progress) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setProgress((int) (progress * 100));
                            }
                        });
                    }

                    @Override
                    public void onEnd() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setProgress(100);
                                progressDialog.dismiss();
//                                Toast.makeText(activity, "Success!!! Ouput path: " + audioOutPutPath, Toast.LENGTH_LONG).show();

                                // Copy the file to a designated location
//                                String destinationPath = getDestinationPath(); // Define your destination path
//                                copyFile(audioOutPutPath, destinationPath);

                                // Add the copied file to the MediaStore
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    MediaScannerConnection.scanFile(
                                            activity,
                                            new String[]{audioOutPutPath},
                                            null,
                                            new MediaScannerConnection.OnScanCompletedListener() {
                                                @Override
                                                public void onScanCompleted(String path, Uri uri) {
                                                    // File is added to MediaStore successfully
                                                    Toast.makeText(activity, "File is added to MediaStore successfully", Toast.LENGTH_LONG).show();

                                                }
                                            }
                                    );
                                } else {
                                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    Uri contentUri = Uri.fromFile(new File(audioOutPutPath));
                                    mediaScanIntent.setData(contentUri);
                                    sendBroadcast(mediaScanIntent);
                                }
                            }
                        });
                    }
                });
            } catch (Exception e) {
                Log.d("AudioMixer", "error message" + e.getMessage());
                e.printStackTrace();
            }


            progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "End", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    audioMixer.stop();
                    audioMixer.release();
                }
            });

            try {
                audioMixer.start();
                audioMixer.processAsync();
                progressDialog.show();
            } catch (IOException e) {
                audioMixer.release();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //audioMixer.setSampleRate(44100);  // optional
        //audioMixer.setBitRate(128000); // optional
        //audioMixer.setChannelCount(2); // 1 or 2 // optional
        //audioMixer.setLoopingEnabled(true); // Only works for parallel mixing


    }

    public void openChooser() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, AUDIO_CHOOSE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUDIO_CHOOSE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(activity, data.getData());
                String dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long duration = Integer.parseInt(dur) * 1000L; // milli to micro second
                retriever.release();

                Input input = new Input();
                input.uri = data.getData();
                input.durationUs = duration;
                inputs.add(input);
                Toast.makeText(activity, inputs.size() + " input(s) added.", Toast.LENGTH_SHORT).show();

                AudioInputSettingsDialog dialog = new AudioInputSettingsDialog(activity, input);
                dialog.setCancelable(false);
                dialog.show();

            } catch (Exception o) {
                Toast.makeText(activity, "Input not added.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        // request permission if it has not been grunted.
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission has been granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "[WARN] permission is not granted.", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private String getOutputFilePath() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "mixed_audio_" + System.currentTimeMillis() + ".mp3";
        File outputFile = new File(downloadsDir, fileName);
        return outputFile.getAbsolutePath();
    }
}