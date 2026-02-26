package com.bit.bitstream;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.bit.bitstream.Song;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "MusicPlayerService";

    private MediaPlayer mediaPlayer;
    private List<Song> playlist = new ArrayList<>();
    private int currentPosition = -1;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private boolean isPlaying = false;

    private final IBinder binder = new MusicBinder();
    private MusicPlayerCallback callback;

    public interface MusicPlayerCallback {
        void onSongChanged(Song song);
        void onPlaybackStateChanged(boolean isPlaying);
        void onProgressUpdate(int currentPosition, int duration);
        void onShuffleModeChanged(boolean isShuffle);
        void onRepeatModeChanged(boolean isRepeat);
    }

    public class MusicBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializePlayer();
    }

    private void initializePlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isRepeat) {
            play(currentPosition);
        } else {
            playNext();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        isPlaying = true;
        if (callback != null) {
            callback.onPlaybackStateChanged(true);
            callback.onSongChanged(getCurrentSong());
            callback.onProgressUpdate(0, mp.getDuration());
        }
        startProgressUpdater();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "Error: " + what + ", " + extra);
        return false;
    }

    private void startProgressUpdater() {
        new Thread(() -> {
            while (isPlaying && mediaPlayer != null) {
                try {
                    Thread.sleep(1000);
                    if (callback != null && mediaPlayer.isPlaying()) {
                        int current = mediaPlayer.getCurrentPosition();
                        int duration = mediaPlayer.getDuration();
                        callback.onProgressUpdate(current, duration);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setPlaylist(List<Song> songs) {
        this.playlist = new ArrayList<>(songs);
    }

    public void play(int position) {
        if (position < 0 || position >= playlist.size()) return;

        currentPosition = position;
        Song song = playlist.get(position);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepareAsync();

            if (callback != null) {
                callback.onSongChanged(song);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playPause() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        } else {
            mediaPlayer.start();
            isPlaying = true;
        }

        if (callback != null) {
            callback.onPlaybackStateChanged(isPlaying);
        }
    }

    public void playNext() {
        if (playlist.isEmpty()) return;

        if (isShuffle) {
            Random random = new Random();
            currentPosition = random.nextInt(playlist.size());
        } else {
            currentPosition = (currentPosition + 1) % playlist.size();
        }
        play(currentPosition);
    }

    public void playPrevious() {
        if (playlist.isEmpty()) return;

        currentPosition = (currentPosition - 1 + playlist.size()) % playlist.size();
        play(currentPosition);
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public void toggleShuffle() {
        isShuffle = !isShuffle;
        if (callback != null) {
            callback.onShuffleModeChanged(isShuffle);
        }
    }

    public void toggleRepeat() {
        isRepeat = !isRepeat;
        if (callback != null) {
            callback.onRepeatModeChanged(isRepeat);
        }
    }

    public Song getCurrentSong() {
        if (currentPosition >= 0 && currentPosition < playlist.size()) {
            return playlist.get(currentPosition);
        }
        return null;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setCallback(MusicPlayerCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}