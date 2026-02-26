package com.bit.bitstream;

import android.net.Uri;
import java.io.Serializable;

public class Song implements Serializable{
    private long id;
    private String title;
    private String artist;
    private String album;
    private String path;
    private long duration;
    private String albumArt;
    private boolean isLiked;
    private boolean isVideo;

    public Song() {
        this.isLiked = false;
    }
    public Song(long id, String title, String artist, String album, String path, long duration, String albumArt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.duration = duration;
        this.albumArt = albumArt;
        this.isLiked = false;
        this.isVideo = path != null && path.toLowerCase().endsWith(".mp4");
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist != null ? artist : "Unknown Artist"; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public String getAlbumArt() { return albumArt; }
    public void setAlbumArt(String albumArt) { this.albumArt = albumArt; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    public boolean isVideo() { return isVideo; }
    public void setVideo(boolean video) { isVideo = video; }

    public Uri getUri() {
        return Uri.parse(path);
    }
    public String getFormattedDuration() {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

}

