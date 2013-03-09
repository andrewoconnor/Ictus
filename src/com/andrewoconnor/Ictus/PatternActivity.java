package com.andrewoconnor.Ictus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.*;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew
 * Date: 2/10/13
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatternActivity extends Activity {

    private static final int CLICK_BUFFER_SIZE = 4096;

    private TextView mTvTempo;

    private int mNumPresses = 0;
    private double mAvgTempo = 0;
    private double mTempoSum = 0;
    private long mCurrTime = 0;
    private long mLastTime = 0;
    private long mTimeDiff = 0;
    private ArrayList<Integer> mTempos = new ArrayList<Integer>();
    private SeekBar mSeekTempo;
    private String mTempo;

    private Spinner mSpnrBeats;
    private Spinner mSpnrSubdiv;
    private Spinner mSpnrSelect;

    private Button mBtnPlay;
    private ToggleButton mTbtnAccent;

    private Pattern mPattern;
    private ClickGenerator mClick;

    private short[] mClickLow;
    private int mClickLenLow;
    private short[] mClickHigh;
    private int mClickLenHigh;

    private BroadcastReceiver mReceiver;
    private TelephonyManager mTeleMgr;
    private PhoneStateListener mPhoneStateListener;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pattern);

        mPattern = ((Loop) getApplicationContext()).getCurrentPattern();

        mPattern.rewindPattern();

        initLayout();

        initReceivers();

        if (mClick == null){

            try {
                initClick();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mClick = new ClickGenerator(mClickLow, mClickLenLow, mClickHigh, mClickLenHigh, this);

        }

        if (mClick.isPlaying())
            mBtnPlay.setText("stop");

    }

    public void initLayout(){

        int patternNum = ((Loop) getApplicationContext()).getSelectedPatternNum();

        TextView tvPatternTitle = (TextView) findViewById(R.id.txtPatternTitle);
        tvPatternTitle.setText("Edit pattern " + patternNum);

        final String[] beatArray = getResources().getStringArray(R.array.beats);

        mSpnrBeats = (Spinner) findViewById(R.id.spnrPatNumBeats);
        mSpnrSubdiv = (Spinner) findViewById(R.id.spnrPatNumSubdivisions);
        mSpnrSelect = (Spinner) findViewById(R.id.spnrPatSelectedBeat);

        mBtnPlay = (Button) findViewById(R.id.btnPatPlayPattern);
        mTbtnAccent = (ToggleButton) findViewById(R.id.tbtnAccentBeat);

        ArrayAdapter<CharSequence> beatsAdapter =
                ArrayAdapter.createFromResource(this, R.array.beats, R.layout.spinner_text);

        final ArrayAdapter<CharSequence> subdivAdapter =
                ArrayAdapter.createFromResource(this, R.array.subdivisions, R.layout.spinner_text);

        beatsAdapter.setDropDownViewResource(R.layout.spinner_text);
        subdivAdapter.setDropDownViewResource(R.layout.spinner_text);

        mSpnrBeats.setAdapter(beatsAdapter);
        mSpnrSubdiv.setAdapter(subdivAdapter);

        mTvTempo = (TextView) findViewById(R.id.tvTempo);

        mSeekTempo = (SeekBar) findViewById(R.id.seekPatTempo);

        mSeekTempo.setMax(230);
        mSeekTempo.setProgress((mPattern.getTempo() - 10));
        mSeekTempo.incrementProgressBy(1);

        mTvTempo.setText(mPattern.getTempo() + "");
        mSpnrBeats.setSelection((mPattern.getNumBeats() - 1));
        mSpnrSubdiv.setSelection((mPattern.getNumSubdivisions(mPattern.getSelectedBeat()) - 1));
        mTbtnAccent.setChecked(mPattern.getBeatAccent());


        mSeekTempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (progress >= 0 && progress <= mSeekTempo.getMax()) {

                    String progressString = Integer.toString(progress + 10);
                    mTvTempo.setText(progressString);
                    mPattern.setTempo(progress + 10);

                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        mSpnrBeats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                int numBeats = Integer.parseInt(parent.getItemAtPosition(pos).toString());

                if (numBeats != mPattern.getNumBeats()) {
                    mPattern.setNumBeats(numBeats);
                    mPattern.setSelectedBeat(1);
                }

                String[] beats = Arrays.copyOfRange(beatArray, 0, numBeats);

                ArrayAdapter<String> selectAdapter = new ArrayAdapter<String>(parent.getContext(), R.layout.spinner_text, beats);
                selectAdapter.setDropDownViewResource(R.layout.spinner_text);

                mSpnrSelect.setAdapter(selectAdapter);

                int pos1 = selectAdapter.getPosition(mPattern.getSelectedBeat() + "");

                mSpnrSelect.setSelection(pos1);

            }

            public void onNothingSelected(AdapterView parent) {
                // Do nothing.
            }
        });

        mSpnrSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                int selectedBeat = Integer.parseInt(parent.getItemAtPosition(pos).toString());

                mPattern.setSelectedBeat(selectedBeat);

                int numSubdivisions = mPattern.getNumSubdivisions(selectedBeat);

                int pos1 = subdivAdapter.getPosition(numSubdivisions + "");

                mSpnrSubdiv.setSelection(pos1);
            }

            public void onNothingSelected(AdapterView parent) {
                // Do nothing.
            }

        });

        mSpnrSubdiv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                int numSubdiv = Integer.parseInt(parent.getItemAtPosition(pos).toString());

                if (numSubdiv != mPattern.getNumSubdivisions(mPattern.getSelectedBeat()))
                    mPattern.setNumSubdivisions(mPattern.getSelectedBeat(), numSubdiv);
            }

            public void onNothingSelected(AdapterView parent) {
                // Do nothing.
            }
        });

        mTbtnAccent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPattern.setBeatAccent(isChecked);
            }
        });
    }

    public void initReceivers(){

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: pause click
                    stopPattern();
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                    // Call over
                    mPattern.rewindPattern();
                    mClick.setPattern(mPattern);
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    stopPattern();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        mTeleMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        if(mTeleMgr != null) {
            mTeleMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @Override
    public void onResume(){

        super.onResume();

        if (!ScreenReceiver.wasScreenOn) {
            ScreenReceiver.wasScreenOn = true;
        }

        mSpnrSubdiv.setSelection((mPattern.getNumSubdivisions(mPattern.getSelectedBeat()) - 1));
        mSpnrSelect.setSelection((mPattern.getSelectedBeat() - 1));
    }

    @Override
    public void onPause(){

        super.onPause();

        if (!ScreenReceiver.wasScreenOn) {

            if(mReceiver != null)
                unregisterReceiver(mReceiver);

            if(mTeleMgr != null) {
                mTeleMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            }

            stopPattern();
        } else {
            ScreenReceiver.wasScreenOn = false;
        }

    }

    public void increaseTempo(View view){
        mTempo = mTvTempo.getText().toString();
        int bpm = Integer.parseInt(mTempo);
        bpm++;
        if (bpm > 240)
            bpm = 240;
        mTvTempo.setText("" + bpm);
        mPattern.setTempo(bpm);
        bpm -= 10;
        mSeekTempo.setProgress(bpm);

    }

    public void decreaseTempo(View view){
        mTempo = mTvTempo.getText().toString();
        int bpm = Integer.parseInt(mTempo);
        bpm--;
        if (bpm < 10)
            bpm = 10;
        mTvTempo.setText("" + bpm);
        mPattern.setTempo(bpm);
        bpm -= 10;
        mSeekTempo.setProgress(bpm);
    }

    public void tapTempo(View view){
        mNumPresses++;

        mCurrTime = System.currentTimeMillis();

        if((mCurrTime - mLastTime) > 2000){
            mNumPresses = 1;
        }

        if(mNumPresses > 1){
            mTimeDiff = mCurrTime - mLastTime;

            double seconds = (((double) mTimeDiff) / 1000);
            int bpm = (int) (60 * (1 / seconds));

            mTimeDiff = 0;

            if (bpm < 10)
                bpm = 10;
            else if (bpm > 240)
                bpm = 240;

            mTempos.add(bpm);

            mTempoSum = 0;

            for (int i = 0; i < mTempos.size(); i++){
                 mTempoSum += mTempos.get(i);
            }

            mAvgTempo = (mTempoSum / mTempos.size());

            if( (Math.abs(bpm - mAvgTempo) > 9) || (mTempos.size() > 4) ){
                mTempos.clear();
                mTempoSum = 0;
                mAvgTempo = 0;
                mNumPresses = 0;
            }

            if (mAvgTempo != 0)
                bpm = (int) mAvgTempo;

            if (bpm < 10)
                bpm = 10;
            else if (bpm > 240)
                bpm = 240;

            mTvTempo.setText("" + bpm);
            mPattern.setTempo(bpm);
            bpm -= 10;
            mSeekTempo.setProgress(bpm);

        }
        mLastTime = mCurrTime;
    }

    public void clickEdit(View view){

        int tempo = Integer.parseInt(mTvTempo.getText().toString());
        mPattern.setTempo(tempo);

        Intent activity1 = new Intent(PatternActivity.this, SubdivisionActivity.class);
        startActivity(activity1);
    }

    public void clickPlay(View view){

        if (mClick.isPlaying()){
            mClick.stop();
            mBtnPlay.setText("play");
            mPattern.rewindPattern();
            mClick.setPattern(mPattern);
        } else{

            mBtnPlay.setText("stop");

            AsyncTask<Void, Void, Void> clickTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    mClick.play();

                    return null;
                }
            };

            clickTask.execute();
        }
    }

    public void initClick() throws IOException {

        InputStream raw = getResources().openRawResource(R.raw.low);

        mClickLow = new short[CLICK_BUFFER_SIZE];
        mClickLenLow = 0;
        while (mClickLenLow < CLICK_BUFFER_SIZE) {
            int a = raw.read();
            if (a == -1) {
                break;
            }

            int b = raw.read();
            if (b == -1) {
                throw new EOFException("Found EOF half way through short.");
            }

            // Little endian byte pair to short.
            mClickLow[mClickLenLow++] = (short)((b << 8) | (a & 0xff));
        }

        raw = getResources().openRawResource(R.raw.high);

        mClickHigh = new short[CLICK_BUFFER_SIZE];
        mClickLenHigh = 0;
        while (mClickLenHigh < CLICK_BUFFER_SIZE) {
            int a = raw.read();
            if (a == -1) {
                break;
            }

            int b = raw.read();
            if (b == -1) {
                throw new EOFException("Found EOF half way through short.");
            }

            // Little endian byte pair to short.
            mClickHigh[mClickLenHigh++] = (short)((b << 8) | (a & 0xff));
        }
    }

    public void applyPattern(View v){
        mPattern.applyPattern();
    }

    public void donePattern(View v){

        stopPattern();
        onPause();
        finish();
    }

    public void stopPattern(){

        if (mClick.isPlaying()){
            mClick.stop();
            mBtnPlay.setText("play");
        }
    }

}