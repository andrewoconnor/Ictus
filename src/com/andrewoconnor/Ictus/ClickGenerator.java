package com.andrewoconnor.Ictus;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew
 * Date: 2/22/12
 * Time: 7:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClickGenerator {

    private static final int SAMPLE_RATE = 44100;

    private final AudioTrack mTrack;
    private final short[] mClickLow;
    private final int mClickLenLow;
    private final short[] mClickHigh;
    private final int mClickLenHigh;
    private double mBeatsPerSecond;
    private boolean mPlaying = false;
    private Activity mActivity;
    private Pattern mPattern;
    private boolean mPlayingLoop;

    public ClickGenerator(short[] mClickLow, int mClickLenLow,
                          short[] mClickHigh, int mClickLenHigh, Activity mActivity) {
        this.mClickLow = mClickLow;
        this.mClickLenLow = mClickLenLow;
        this.mClickHigh = mClickHigh;
        this.mClickLenHigh = mClickLenHigh;
        this.mActivity = mActivity;

        int bufferSize =
                AudioTrack.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM);

        mPattern = ((Loop) mActivity.getApplicationContext()).getCurrentPattern();

    }

    public void writeClick(){
        short[] deadSecond = new short[mTrack.getSampleRate()];

        int totalFrames = 0;
        double elapsed = 0;

        while(mPlaying){

            int beatCount = mPattern.getBeatCount();
            int numSubdiv = mPattern.getNumSubdivisions(beatCount);
            int subdivCount = mPattern.getSubdivCount();

            mBeatsPerSecond = ((double) mPattern.getTempo() / 60);

            double period = (1 / (mBeatsPerSecond * numSubdiv));
            elapsed += period;

            int periodFrames = (int) (period * mTrack.getSampleRate());
            totalFrames += periodFrames;

            boolean isActivated = mPattern.getSubdivisionActivated(beatCount, subdivCount);

            if (isActivated) {

                if(beatCount == 1 && subdivCount == 1 && mPattern.getBeatAccent()){
                    int clickFramesToWrite = Math.min(mClickLenHigh, periodFrames);
                    mTrack.write(mClickHigh, 0, clickFramesToWrite);
                    periodFrames -= clickFramesToWrite;

                } else {
                    int clickFramesToWrite = Math.min(mClickLenLow, periodFrames);
                    mTrack.write(mClickLow, 0, clickFramesToWrite);
                    periodFrames -= clickFramesToWrite;
                }

            }

            if ((subdivCount == numSubdiv) && (mPlayingLoop) && (beatCount == mPattern.getNumBeats())){
                mPattern.incrementSubdivCount();
                mPattern = ((Loop) mActivity.getApplicationContext()).getNextPatternInLoop();
            } else {
                mPattern.incrementSubdivCount();
            }


            while (periodFrames > 0) {
                int toWrite = Math.min(periodFrames, deadSecond.length);
                mTrack.write(deadSecond, 0, toWrite);
                periodFrames -= toWrite;
            }
        }
    }

    public void play() {
        mPlaying = true;
        mTrack.play();
        writeClick();
    }

    public void stop() {
        mPlaying = false;
        mTrack.stop();
    }

    public boolean isPlaying(){
        return mPlaying;
    }

    public void setPlayingLoop(boolean _flag){
        mPlayingLoop = _flag;
    }

    public boolean isPlayingLoop(){
        return mPlayingLoop;
    }

    public void setPattern(Pattern _p){
        mPattern = _p;
    }
}