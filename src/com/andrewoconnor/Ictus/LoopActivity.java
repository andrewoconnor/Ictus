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
import android.widget.LinearLayout.*;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew
 * Date: 2/27/13
 * Time: 6:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoopActivity extends Activity {

    private Loop mLoop;

    private Spinner mSpnrPatternNum;
    private TextView mEtLoopTitle;

    private Integer[] numPatterns;

    private LinearLayout mLLPatternOrder;
    private LinearLayout mRow;

    private int mPatternsOnLastRow;
    private int mPatternsInLoop;
    private int mNumRows;
    private boolean mFirstRow;
    private boolean mCascading;

    private int mFirstBtnSwapId;
    private int mFirstPatternNum;
    private boolean mPatternsSwapped;

    private ClickGenerator mClick;
    private static final int CLICK_BUFFER_SIZE = 4096;
    private short[] mClickLow;
    private int mClickLenLow;
    private short[] mClickHigh;
    private int mClickLenHigh;

    private Button mPlayLoop;
    private Button mAddToLoop;

    private BroadcastReceiver mReceiver;
    private TelephonyManager mTeleMgr;
    private PhoneStateListener mPhoneStateListener;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.loop);

        mLoop = (Loop) getApplicationContext();

        mPatternsInLoop = 0;
        mPatternsOnLastRow = 0;
        mNumRows = 0;
        mFirstRow = true;
        mCascading = false;

        mFirstBtnSwapId = 0;
        mFirstPatternNum = 0;
        mPatternsSwapped = false;

        initLayout();

        initReceivers();

        if (mClick == null){

            try {
                initClick();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mClick = new ClickGenerator(mClickLow, mClickLenLow, mClickHigh, mClickLenHigh, this);
            mClick.setPlayingLoop(true);
        }

        if (mLoop.getWasSaved()){
            loadLoop();
            mClick.setPattern(mLoop.getPattern(mLoop.getPatternNum(0)));

        } else {
            mAddToLoop.performClick();
        }

        mEtLoopTitle.setText(mLoop.getLoopName());

    }

    public void initLayout(){

        mPlayLoop = (Button) findViewById(R.id.btnPlayLoop);
        mAddToLoop = (Button) findViewById(R.id.btnAddToLoop);

        mSpnrPatternNum = (Spinner) findViewById(R.id.spnrPatternNum);
        mEtLoopTitle = (TextView) findViewById(R.id.tvLoopTitle);

        mLLPatternOrder = (LinearLayout) findViewById(R.id.LLPatternOrder);
        mLLPatternOrder.setOrientation(LinearLayout.VERTICAL);

        mSpnrPatternNum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                int selectedPattern = Integer.parseInt(parent.getItemAtPosition(pos).toString());

                if (selectedPattern != mLoop.getSelectedPatternNum()){
                    mLoop.setSelectedPatternNum(selectedPattern);
                }
            }

            public void onNothingSelected(AdapterView parent) {
                // Do nothing.
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
                    stopLoop();
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                    // Call over
                    rewind();
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    stopLoop();
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

        numPatterns = new Integer[mLoop.getNumPatterns()];

        for (int i = 0; i < numPatterns.length; i++){
            numPatterns[i] = (i + 1);
        }

        ArrayAdapter<Integer> patternsAdapter =
                new ArrayAdapter<Integer>(this, R.layout.spinner_text, numPatterns);

        patternsAdapter.setDropDownViewResource(R.layout.spinner_text);

        mSpnrPatternNum.setAdapter(patternsAdapter);

        int pos = patternsAdapter.getPosition(mLoop.getSelectedPatternNum());

        mSpnrPatternNum.setSelection(pos);

        if (!ScreenReceiver.wasScreenOn) {

            ScreenReceiver.wasScreenOn = true;
        }
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

            stopLoop();

        } else {
            ScreenReceiver.wasScreenOn = false;
        }
    }

    public void addToLoop(View v){

        mFirstBtnSwapId = 0;

        final float scale = getBaseContext().getResources().getDisplayMetrics().density;
        final int pixels = (int) (60 * scale + 0.5f);

        if (mLoop.getSelectedPatternNum() != 0 ){

            if ((mPatternsInLoop % 5) == 0){

                if (!((mPatternsInLoop == 0) && (!mFirstRow))){

                    mRow = new LinearLayout(this);
                    mNumRows++;
                    mRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    mRow.setId(0);
                    mRow.setTag("" + mNumRows);
                    mRow.setFocusable(true);
                    mRow.setFocusableInTouchMode(true);
                    mLLPatternOrder.addView(mRow);
                    mRow.requestFocus();

                }

                if (mPatternsOnLastRow == 5)
                    mPatternsOnLastRow = 0;
            }

            int currPatternNum = mLoop.getSelectedPatternNum();

            Button btnPattern = new Button(this);
            mFirstRow = false;
            mPatternsInLoop++;
            btnPattern.setLayoutParams(new LinearLayout.LayoutParams(pixels, pixels));
            btnPattern.setText("" + currPatternNum);
            btnPattern.setId(mPatternsInLoop);

            if(!mLoop.getWasSaved())
                mLoop.addPatternToLoop();

            // remove a pattern from the loop

            btnPattern.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    int idNum = v.getId();

                    LinearLayout parentRow = (LinearLayout) v.getParent();
                    int rowNum = Integer.parseInt(parentRow.getTag().toString());

                    parentRow.removeView(v);

                    if (!mCascading)
                        mLoop.removePattern(idNum - 1);

                    if ((idNum < mPatternsInLoop) && !mCascading){

                        if ((idNum % 5) == 0){
                            idNum = 5;
                        } else {
                            idNum = idNum % 5;
                        }

                        for(int i = (rowNum - 1); i < mNumRows; i++){

                            LinearLayout tempRow = (LinearLayout) mLLPatternOrder.getChildAt(i);

                            if (i == (rowNum - 1)){

                                for (int j = (idNum - 1); j < tempRow.getChildCount(); j++){

                                    Button tempBtn = (Button) tempRow.getChildAt(j);
                                    int newId = tempBtn.getId();
                                    tempBtn.setId(newId - 1);
                                    int patternNum = Integer.parseInt(tempBtn.getText().toString());
                                    mLoop.setPatternAtPosition((newId - 2), patternNum);
                                }
                            }  else {

                                for (int j = 0; j < tempRow.getChildCount(); j++){

                                    Button tempBtn = (Button) tempRow.getChildAt(j);
                                    int newId = tempBtn.getId();
                                    tempBtn.setId(newId - 1);
                                    int patternNum = Integer.parseInt(tempBtn.getText().toString());
                                    mLoop.setPatternAtPosition((newId - 2), patternNum);
                                }
                            }
                        }
                    }

                    if (rowNum == mNumRows){

                        int numPatternsOnRow = parentRow.getId();
                        numPatternsOnRow--;
                        mPatternsOnLastRow--;
                        mPatternsInLoop--;
                        parentRow.setId(numPatternsOnRow);
                    }

                    if ((mPatternsOnLastRow == 0) && (mNumRows != 1)){

                        int delRowNum = mNumRows - 1;
                        LinearLayout nextRow = (LinearLayout) mLLPatternOrder.getChildAt(delRowNum);
                        nextRow.setId(0);
                        mLLPatternOrder.removeView(nextRow);
                        mNumRows--;
                        mPatternsOnLastRow = mLLPatternOrder.getChildAt(mNumRows - 1).getId();
                        mRow = (LinearLayout) mLLPatternOrder.getChildAt(mNumRows - 1);
                    }

                    if (rowNum < mNumRows){

                        mCascading = true;
                        LinearLayout nextRow = (LinearLayout) mLLPatternOrder.getChildAt(rowNum);
                        Button oldButton = (Button) nextRow.getChildAt(0);
                        nextRow.getChildAt(0).performLongClick();
                        parentRow.addView(oldButton);
                        mCascading = false;
                    }

                    return true;
                }
            });

            // swap patterns

            btnPattern.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Button tempBtn = (Button) v;

                    if (mFirstBtnSwapId == 0){

                        mFirstBtnSwapId = tempBtn.getId();
                        mFirstPatternNum = Integer.parseInt(tempBtn.getText().toString());
                    } else {

                        int secondBtnSwapId = tempBtn.getId();
                        mLoop.swapPatterns(mFirstBtnSwapId - 1, secondBtnSwapId - 1);

                        for (int i = 0; i < mNumRows; i++){

                            LinearLayout tempRow = (LinearLayout) mLLPatternOrder.getChildAt(i);

                            for (int j = 0; j < tempRow.getChildCount(); j++){

                                Button tempBtn2 = (Button) tempRow.getChildAt(j);

                                if (tempBtn2.getId() == mFirstBtnSwapId){

                                    tempBtn2.setText(tempBtn.getText());
                                    tempBtn.setText("" + mFirstPatternNum);
                                    mFirstBtnSwapId = 0;
                                    mPatternsSwapped = true;
                                    break;
                                }
                            }

                            if (mPatternsSwapped){
                                break;
                            }
                        }

                        mPatternsSwapped = false;
                    }
                }
            });

            mRow.addView(btnPattern);
            mPatternsOnLastRow++;
            mRow.setId(mPatternsOnLastRow);

        } else {
            Toast.makeText(getBaseContext(), "Create a pattern first!", Toast.LENGTH_SHORT).show();
        }

    }

    public void editPattern(View v){

        stopLoop();

        mFirstBtnSwapId = 0;

        if (mLoop.getSelectedPatternNum() == 0){

            mLoop.createPattern(new Pattern());
            mLoop.setSelectedPatternNum(mLoop.getNumPatterns());
            Intent activity1 = new Intent(LoopActivity.this, PatternActivity.class);
            startActivity(activity1);

        } else {

            Intent activity1 = new Intent(LoopActivity.this, PatternActivity.class);
            startActivity(activity1);
        }
    }

    public void newPattern(View v){

        stopLoop();

        mFirstBtnSwapId = 0;

        mLoop.createPattern(new Pattern());
        mLoop.setSelectedPatternNum(mLoop.getNumPatterns());
        Intent activity1 = new Intent(LoopActivity.this, PatternActivity.class);
        startActivity(activity1);
    }

    public void playLoop(View view){

        if (mLoop.getNumPatternsInLoop() == 0){

            Toast.makeText(getBaseContext(), "Add a pattern to the loop first!", Toast.LENGTH_SHORT).show();
        } else {

            if (mClick.isPlaying()){
                mClick.stop();
                mPlayLoop.setText("play");
                rewind();
            } else{

                mPlayLoop.setText("stop");

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

    public void doneLoop(View v){

        stopLoop();
        onPause();
        finish();
    }

    public void loadLoop(){

        for (int i = 0; i < mLoop.getNumPatternsInLoop(); i++){

            mLoop.setSelectedPatternNum(mLoop.getPatternNum(i));

            mAddToLoop.performClick();
        }

        mLoop.setWasSaved(false);

    }

    public void stopLoop(){

        if (mClick.isPlaying()){
            mClick.stop();
            mPlayLoop.setText("play");
        }
    }

    public void rewind(){
        mLoop.rewindLoop();
        mClick.setPattern(mLoop.getPattern(mLoop.getPatternNum(0)));
        mSpnrPatternNum.setSelection(0);
    }

}