package com.example.customheader;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.ResolvingDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    HashMap<String,String> headerMap = new HashMap<String,String>();

    //Logs
    final private String TAG = "PlayerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.video_view);

        final EditText editText = (EditText)findViewById(R.id.urlInput);

        // Click Play
        findViewById(R.id.playBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoPlayer(editText.getText().toString());
            }
        });

        //Click Play Regular
        findViewById(R.id.playRegBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRegular(editText.getText().toString());
            }
        });

        // Click Clear
        findViewById(R.id.clearBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });
    }

    private void playRegular(String url) {
        if (player != null) {
            releasePlayer();
        }
        Uri uri = Uri.parse(url);
    }

    public void videoPlayer(String url){
        if (player != null) {
            releasePlayer();
        }

        Uri uri = Uri.parse(url);

        // Build HLS Media Source with Factory inside of it
        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(
                new ResolvingDataSource.Factory(
                        new DefaultHttpDataSourceFactory(Util.getUserAgent(this, getString(R.string.app_name))),
                        // Provide just-in-time request headers.
                        (DataSpec dataSpec) ->
                                dataSpec.withRequestHeaders(getCustomHeaders())
                ))
                .createMediaSource(uri);

        // Prepare Player
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setPlayWhenReady(playWhenReady);
        player.prepare(hlsMediaSource);
    }

    public HashMap getCustomHeaders(){
        headerMap = new HashMap<String, String>();
        String playerAuthen = "Null";
        String deviceID = "deviceid999999";

        try {
            StmAppPlayerAuthen stmAppPlayerAuthen = new StmAppPlayerAuthen();
            playerAuthen = stmAppPlayerAuthen.encrypt(Long.toString(System.currentTimeMillis() / 1000L) + "|deviceid999999");
            System.out.println("Kevin Custom Header --> " + playerAuthen);
        } catch (Exception e){
            Log.d(TAG,"StmAppSecurity Error " + e);
            return headerMap;
        }

        headerMap.put("PlayerAuthen", playerAuthen);
        return headerMap;
    }

    // Release Player
    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }
}