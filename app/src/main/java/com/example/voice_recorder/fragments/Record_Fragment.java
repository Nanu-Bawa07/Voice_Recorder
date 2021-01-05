package com.example.voice_recorder.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.voice_recorder.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Record_Fragment extends Fragment implements View.OnClickListener{

    private NavController navController;
    private ImageButton list_btn, record_btn;
    private TextView record_filename;

    private boolean isRecording = false;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 123;

    private MediaRecorder mediaRecorder;
    private String recordFile;

    private Chronometer record_timer;



    public Record_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        list_btn = view.findViewById(R.id.record_list_btn);
        record_btn = view.findViewById(R.id.record_btn);
        record_timer = view.findViewById(R.id.record_timer);
        record_filename = view.findViewById(R.id.record_filename);

        list_btn.setOnClickListener(this);
        record_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_list_btn:
                if (isRecording) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            navController.navigate(R.id.action_record_Fragment_to_audioList_Fragment);
                            isRecording = false;
                        }
                    });
                    alertDialog.setNegativeButton("Cancel", null);
                    alertDialog.setTitle("Audio still Recording");
                    alertDialog.setMessage("Are you sure, you want to stop it?");
                    alertDialog.create().show();
                }else {
                    navController.navigate(R.id.action_record_Fragment_to_audioList_Fragment);
                }
                break;
            case R.id.record_btn:
                if (isRecording) {
                    // Stop Recording
                    stopRecording();
                    record_btn.setImageDrawable(getResources().getDrawable(R.drawable.microphone_gray,null));
                    isRecording = false;
                }else {
                    // Start Recording
                    if(checkPermission()) {
                        startRecording();
                        record_btn.setImageDrawable(getResources().getDrawable(R.drawable.microphone,null));
                        isRecording = true;
                    }
                }
                break;
        }
    }

    private void stopRecording() {
        record_timer.stop();

        record_filename.setText("Recording Stopped, File Saved : " + recordFile);

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private void startRecording() {
        record_timer.setBase(SystemClock.elapsedRealtime());
        record_timer.start();

        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss", Locale.US);
        Date now = new Date();

        recordFile = "Recording-" + formatter.format(now) + ".3gp";

        record_filename.setText("Recording, File Name : " + recordFile);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    private boolean checkPermission() {
        if(ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRecording) {
            stopRecording();
        }
    }
}