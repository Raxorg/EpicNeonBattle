package com.badlogic.gdx.backends.android;

import android.media.AudioRecord;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AndroidAudioRecorder implements AudioRecorder {
    private AudioRecord recorder;

    public AndroidAudioRecorder(int samplingRate, boolean isMono) {
        int channelConfig = isMono ? 16 : 12;
        this.recorder = new AudioRecord(1, samplingRate, channelConfig, 2, AudioRecord.getMinBufferSize(samplingRate, channelConfig, 2));
        if (this.recorder.getState() != 1) {
            throw new GdxRuntimeException("Unable to initialize AudioRecorder.\nDo you have the RECORD_AUDIO permission?");
        }
        this.recorder.startRecording();
    }

    public void dispose() {
        this.recorder.stop();
        this.recorder.release();
    }

    public void read(short[] samples, int offset, int numSamples) {
        int read = 0;
        while (read != numSamples) {
            read += this.recorder.read(samples, offset + read, numSamples - read);
        }
    }
}
