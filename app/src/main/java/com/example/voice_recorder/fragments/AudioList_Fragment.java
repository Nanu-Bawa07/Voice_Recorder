package com.example.voice_recorder.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.voice_recorder.MainActivity;
import com.example.voice_recorder.R;
import com.example.voice_recorder.adapters.AudioList_Adapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;


public class AudioList_Fragment extends Fragment implements AudioList_Adapter.onItemListClick {
    private static final String TAG = "AudioList_Fragment";

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private RecyclerView audioList;
    private File[] allFiles;

    private AudioList_Adapter audioList_adapter;

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    private MainActivity mainActivity;

    private File filetoPlay = null;

    private TextView player_filename, player_header_title;
    private ImageButton player_play_btn, player_backward, player_forward;

    private SeekBar player_seekbar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;
    private int seekForwardTime = 2 * 1000; // 2 seconds forward
    private int seekBackwardTime = 2 * 1000; // 2 seconds delayed



    public AudioList_Fragment() { super(); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerSheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        audioList = view.findViewById(R.id.audio_list_view);
        player_filename = view.findViewById(R.id.player_filename);
        player_header_title = view.findViewById(R.id.player_header_title);
        player_play_btn = view.findViewById(R.id.player_play_btn);
        player_seekbar = view.findViewById(R.id.player_seekbar);
        player_backward = view.findViewById(R.id.player_backward);
        player_forward = view.findViewById(R.id.player_forward);

        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();

        audioList_adapter = new AudioList_Adapter(allFiles, this);
        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(audioList_adapter);



        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        player_play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    pauseAudio();
                }else if(!isPlaying && player_header_title.getText().toString().equalsIgnoreCase("Finished")) {
                    playAudio(filetoPlay);
                }else {
                    resumeAudio();
                }
            }
        });

        player_backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get current song position
                int currentPosition = mediaPlayer.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if(currentPosition - seekBackwardTime >= 0){
                    // backward song
                    mediaPlayer.seekTo(currentPosition - seekBackwardTime);
                }else{
                    // backward to starting position
                    mediaPlayer.seekTo(0);
                }
            }
        });
        player_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get current song position
                int currentPosition = mediaPlayer.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if(currentPosition + seekForwardTime <= mediaPlayer.getDuration()){
                    // forward song
                    mediaPlayer.seekTo(currentPosition + seekForwardTime);
                }else{
                    // forward to end position
                    mediaPlayer.seekTo(mediaPlayer.getDuration());
                }
            }
        });
    }

    @Override
    public void onClickListener(File file, int position) {
       // Log.d("PLAY LOG", "File Playing " + file.getName());
        filetoPlay = file;
        if (isPlaying) {
            stopAudio();
            playAudio(filetoPlay);
        }else {
            if (filetoPlay != null) {
                playAudio(filetoPlay);
                player_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        pauseAudio();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int progress = seekBar.getProgress();
                        mediaPlayer.seekTo(progress);
                        resumeAudio();
                    }
                });
            }
        }
    }

    @Override
    public void onDeleteClickListener(File file, int position) {
        File filetoPlay = file;
        if (filetoPlay.exists()) {
            filetoPlay.delete();
            this.getFragmentManager().beginTransaction().detach(this).attach(this).commit(); // calling fragment again

        }
    }
    private void pauseAudio() {
        mediaPlayer.pause();
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.play, null));
        isPlaying = false;
        player_header_title.setText("Paused");
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void resumeAudio() {
        mediaPlayer.start();
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.pause, null));
        isPlaying = true;
        player_header_title.setText("Playing");
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar,0);
    }

    private void stopAudio() {
        mediaPlayer.stop();
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.play, null));
        isPlaying = false;
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void playAudio(File filetoPlay) {

        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(filetoPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.pause, null));
        player_filename.setText(filetoPlay.getName());
        player_header_title.setText("Playing");
        isPlaying = true;
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopAudio();
                isPlaying = false;
                player_header_title.setText("Finished");
            }
        });
        player_seekbar.setMax(mediaPlayer.getDuration());

        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar,0);
    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                player_seekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 300);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isPlaying) {
            stopAudio();
        }
    }


}