package com.badlogic.gdx.backends.android;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class AndroidAudio implements Audio {
    private final AudioManager manager;
    protected final List<AndroidMusic> musics = new ArrayList();
    private final SoundPool soundPool;

    public AndroidAudio(Context context, AndroidApplicationConfiguration config) {
        if (config.disableAudio) {
            this.soundPool = null;
            this.manager = null;
            return;
        }
        this.soundPool = new SoundPool(config.maxSimultaneousSounds, 3, 100);
        this.manager = (AudioManager) context.getSystemService("audio");
        if (context instanceof Activity) {
            ((Activity) context).setVolumeControlStream(3);
        }
    }

    protected void pause() {
        if (this.soundPool != null) {
            synchronized (this.musics) {
                for (AndroidMusic music : this.musics) {
                    if (music.isPlaying()) {
                        music.pause();
                        music.wasPlaying = true;
                    } else {
                        music.wasPlaying = false;
                    }
                }
            }
            this.soundPool.autoPause();
        }
    }

    protected void resume() {
        if (this.soundPool != null) {
            synchronized (this.musics) {
                for (int i = 0; i < this.musics.size(); i++) {
                    if (((AndroidMusic) this.musics.get(i)).wasPlaying) {
                        ((AndroidMusic) this.musics.get(i)).play();
                    }
                }
            }
            this.soundPool.autoResume();
        }
    }

    public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
        if (this.soundPool != null) {
            return new AndroidAudioDevice(samplingRate, isMono);
        }
        throw new GdxRuntimeException("Android audio is not enabled by the application config.");
    }

    public Music newMusic(FileHandle file) {
        if (this.soundPool == null) {
            throw new GdxRuntimeException("Android audio is not enabled by the application config.");
        }
        AndroidMusic music;
        AndroidFileHandle aHandle = (AndroidFileHandle) file;
        MediaPlayer mediaPlayer = new MediaPlayer();
        if (aHandle.type() == FileType.Internal) {
            try {
                AssetFileDescriptor descriptor = aHandle.assets.openFd(aHandle.path());
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                mediaPlayer.prepare();
                music = new AndroidMusic(this, mediaPlayer);
                synchronized (this.musics) {
                    this.musics.add(music);
                }
            } catch (Exception ex) {
                throw new GdxRuntimeException("Error loading audio file: " + file + "\nNote: Internal audio files must be placed in the assets directory.", ex);
            }
        }
        try {
            mediaPlayer.setDataSource(aHandle.file().getPath());
            mediaPlayer.prepare();
            music = new AndroidMusic(this, mediaPlayer);
            synchronized (this.musics) {
                this.musics.add(music);
            }
        } catch (Exception ex2) {
            throw new GdxRuntimeException("Error loading audio file: " + file, ex2);
        }
        return music;
    }

    public Sound newSound(FileHandle file) {
        if (this.soundPool == null) {
            throw new GdxRuntimeException("Android audio is not enabled by the application config.");
        }
        AndroidFileHandle aHandle = (AndroidFileHandle) file;
        if (aHandle.type() == FileType.Internal) {
            try {
                AssetFileDescriptor descriptor = aHandle.assets.openFd(aHandle.path());
                AndroidSound sound = new AndroidSound(this.soundPool, this.manager, this.soundPool.load(descriptor, 1));
                descriptor.close();
                return sound;
            } catch (IOException ex) {
                throw new GdxRuntimeException("Error loading audio file: " + file + "\nNote: Internal audio files must be placed in the assets directory.", ex);
            }
        }
        try {
            return new AndroidSound(this.soundPool, this.manager, this.soundPool.load(aHandle.file().getPath(), 1));
        } catch (Exception ex2) {
            throw new GdxRuntimeException("Error loading audio file: " + file, ex2);
        }
    }

    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
        if (this.soundPool != null) {
            return new AndroidAudioRecorder(samplingRate, isMono);
        }
        throw new GdxRuntimeException("Android audio is not enabled by the application config.");
    }

    public void dispose() {
        if (this.soundPool != null) {
            synchronized (this.musics) {
                Iterator it = new ArrayList(this.musics).iterator();
                while (it.hasNext()) {
                    ((AndroidMusic) it.next()).dispose();
                }
            }
            this.soundPool.release();
        }
    }
}
