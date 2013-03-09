package com.andrewoconnor.Ictus;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.LinearLayout.*;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew
 * Date: 2/18/13
 * Time: 6:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubdivisionActivity  extends Activity {

    private Pattern mPattern;
    private Spinner mSpnrSubdiv;
    private Spinner mSpnrSelect;
    private int mSelectedBeat;
    private int mNumSubdiv;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.subdivision);

        mPattern = ((Loop) getApplicationContext()).getCurrentPattern();

        int patternNum = ((Loop) getApplicationContext()).getSelectedPatternNum();

        TextView tvPatternTitle = (TextView) findViewById(R.id.txtSubdivisionTitle);
        tvPatternTitle.setText("Edit pattern " + patternNum);

        final String[] beatArray = getResources().getStringArray(R.array.beats);

        mSpnrSelect = (Spinner) findViewById(R.id.spnrSubNumBeat);
        mSpnrSubdiv = (Spinner) findViewById(R.id.spnrSubNumSubdiv);

        mSelectedBeat = mPattern.getSelectedBeat();
        mNumSubdiv = mPattern.getNumSubdivisions(mSelectedBeat);

        String[] beats = Arrays.copyOfRange(beatArray, 0, mPattern.getNumBeats());

        ArrayAdapter<String> selectedAdapter =  new ArrayAdapter<String>(this, R.layout.spinner_text, beats);
        selectedAdapter.setDropDownViewResource(R.layout.spinner_text);

        final ArrayAdapter<CharSequence> subdivAdapter =
                ArrayAdapter.createFromResource(this, R.array.subdivisions, R.layout.spinner_text);

        subdivAdapter.setDropDownViewResource(R.layout.spinner_text);

        mSpnrSelect.setAdapter(selectedAdapter);
        mSpnrSubdiv.setAdapter(subdivAdapter);

        int pos = selectedAdapter.getPosition(mSelectedBeat + "");

        mSpnrSelect.setSelection(pos);

        pos = subdivAdapter.getPosition(mNumSubdiv + "");

        mSpnrSubdiv.setSelection(pos);

        makeSubdivButtons();

        mSpnrSubdiv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                mNumSubdiv = Integer.parseInt(parent.getItemAtPosition(pos).toString());

                if (mNumSubdiv != mPattern.getNumSubdivisions(mSelectedBeat))
                    mPattern.setNumSubdivisions(mSelectedBeat, mNumSubdiv);

                makeSubdivButtons();
            }

            public void onNothingSelected(AdapterView parent) {
                // Do nothing.
            }
        });

        mSpnrSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                mSelectedBeat = Integer.parseInt(parent.getItemAtPosition(pos).toString());

                mPattern.setSelectedBeat(mSelectedBeat);

                mNumSubdiv = mPattern.getNumSubdivisions(mSelectedBeat);

                mSpnrSubdiv.setSelection(mPattern.getNumSubdivisions(mSelectedBeat) - 1);

                makeSubdivButtons();
            }

            public void onNothingSelected(AdapterView parent) {
                // Do nothing.
            }
        });
    }

    public void makeSubdivButtons(){

        LinearLayout llSubdivPattern = (LinearLayout) findViewById(R.id.llSubdivPattern);

        llSubdivPattern.removeAllViews();
        llSubdivPattern.setOrientation(LinearLayout.VERTICAL);

        double helper = ((double) mNumSubdiv) / 4;
        int numRows = 0;

        if (helper <= 1.0)
            numRows = 1;
        else if (helper <= 2.0)
            numRows = 2;
        else if (helper <= 3.0)
            numRows = 3;
        else
            numRows = 4;

        final float scale = getBaseContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (60 * scale + 0.5f);

        for (int i = 0; i < numRows; i++) {


            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < 4; j++){

                int idNum = (j + 1 + (i * 4));
                if (idNum > mNumSubdiv)
                    break;
                ToggleButton tbtnSubdivision = new ToggleButton(this);
                tbtnSubdivision.setLayoutParams(new LayoutParams(pixels, pixels));
                tbtnSubdivision.setText("" + idNum);
                tbtnSubdivision.setTextOn("" + idNum);
                tbtnSubdivision.setTextOff("" + idNum);
                tbtnSubdivision.setId(idNum);

                boolean isActivated = mPattern.getSubdivisionActivated(mSelectedBeat, idNum);
                tbtnSubdivision.setChecked(isActivated);

                tbtnSubdivision.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mPattern.setBeatSubdivision(mSelectedBeat, buttonView.getId(), isChecked);
                    }
                });
                row.addView(tbtnSubdivision);

            }

            llSubdivPattern.addView(row);
        }
    }

    public void doneSubdivision(View view){
        finish();
    }
}
