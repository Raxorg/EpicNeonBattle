package com.badlogic.gdx.backends.android;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import java.io.IOException;

public class AndroidMusic implements OnCompletionListener, Music {
    private final AndroidAudio audio;
    private boolean isPrepared = true;
    protected Music.OnCompletionListener onCompletionListener;
    private MediaPlayer player;
    private float volume = 1.0f;
    protected boolean wasPlaying = false;

    AndroidMusic(AndroidAudio audio, MediaPlayer player) {
        this.audio = audio;
        this.player = player;
        this.onCompletionListener = null;
        this.player.setOnCompletionListener(this);
    }

    public void dispose() {
        if (this.player != null) {
            try {
                this.player.release();
                this.player = null;
                this.onCompletionListener = null;
                synchronized (this.audio.musics) {
                    this.audio.musics.remove(this);
                }
            } catch (Throwable th) {
                this.player = null;
                this.onCompletionListener = null;
                synchronized (this.audio.musics) {
                    this.audio.musics.remove(this);
                }
            }
        }
    }

    public boolean isLooping() {
        if (this.player == null) {
            return false;
        }
        return this.player.isLooping();
    }

    public boolean isPlaying() {
        if (this.player == null) {
            return false;
        }
        return this.player.isPlaying();
    }

    public void pause() {
        if (this.player != null) {
            if (this.player.isPlaying()) {
                this.player.pause();
            }
            this.wasPlaying = false;
        }
    }

    public void play() {
        if (this.player != null && !this.player.isPlaying()) {
            try {
                if (!this.isPrepared) {
                    this.player.prepare();
                    this.isPrepared = true;
                }
                this.player.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void setLooping(boolean isLooping) {
        if (this.player != null) {
            this.player.setLooping(isLooping);
        }
    }

    public void setVolume(float volume) {
        if (this.player != null) {
            this.player.setVolume(volume, volume);
            this.volume = volume;
        }
    }

    public float getVolume() {
        return this.volume;
    }

    public void setPan(float pan, float volume) {
        if (this.player != null) {
            float leftVolume = volume;
            float rightVolume = volume;
            if (pan < 0.0f) {
                rightVolume *= 1.0f - Math.abs(pan);
            } else if (pan > 0.0f) {
                leftVolume *= 1.0f - Math.abs(pan);
            }
            this.player.setVolume(leftVolume, rightVolume);
            this.volume = volume;
        }
    }

    public void stop() {
        if (this.player != null) {
            if (this.isPrepared) {
                this.player.seekTo(0);
            }
            this.player.stop();
            this.isPrepared = false;
        }
    }

    public void setPosition(float position) {
        if (this.player != null) {
            try {
                if (!this.isPrepared) {
                    this.player.prepare();
                    this.isPrepared = true;
                }
                this.player.seekTo((int) (1000.0f * position));
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public float getPosition() {
        if (this.player == null) {
            return 0.0f;
        }
        return ((float) this.player.getCurrentPosition()) / 1000.0f;
    }

    public float getDuration() {
        if (this.player == null) {
            return 0.0f;
        }
        return ((float) this.player.getDuration()) / 1000.0f;
    }

    public void setOnCompletionListener(Music.OnCompletionListener listener) {
        this.onCompletionListener = listener;
    }

    public void onCompletion(MediaPlayer mp) {
        if (this.onCompletionListener != null) {
            Gdx.app.postRunnable(new Runnable() {
                public void run() {
                    AndroidMusic.this.onCompletionListener.onCompletion(AndroidMusic.this);
                }
            });
        }
    }
}
