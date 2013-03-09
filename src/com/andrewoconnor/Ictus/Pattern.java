package com.andrewoconnor.Ictus;

import android.app.Application;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew
 * Date: 2/10/13
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class Pattern implements Serializable {

    private int tempo;
    private int numBeats;
    private int selectedBeat;
    private int beatCount;
    private int subdivCount;
    private boolean beatAccent;
    private boolean[][] subdivisions;

    public Pattern(){
        setTempo(120);
        setNumBeats(4);
        setSelectedBeat(1);
        setBeatCount(1);
        setSubdivCount(1);
        setBeatAccent(true);

        for (int i = 1; i <= subdivisions.length; i++){
            setNumSubdivisions(i, 1);
            setBeatSubdivision(i, 1, true);
        }
    }

    public void setTempo(int _tempo){
        if (_tempo < 10)
            _tempo = 10;
        else if (_tempo > 240)
            _tempo = 240;

        tempo = _tempo;
    }

    public void setNumBeats(int _numBeats){
        if (_numBeats < 1)
            _numBeats = 1;
        else if (_numBeats > 16)
            _numBeats = 16;

        numBeats = _numBeats;
        subdivisions = new boolean[numBeats][1];

        for (int i = 1; i <= _numBeats; i++){
            setBeatSubdivision(i, 1, true);
        }
    }

    public void setSelectedBeat(int _selectedBeat){
        selectedBeat = _selectedBeat;
    }

    public void setBeatCount(int _beatCount){
        if (_beatCount < 1)
            _beatCount = 1;
        else if (_beatCount > numBeats)
            _beatCount = numBeats;

        beatCount = _beatCount;
    }

    public void setSubdivCount(int _subdivCount){
        if (_subdivCount < 1)
            _subdivCount = 1;
        else if (_subdivCount > getNumSubdivisions(beatCount))
            _subdivCount = getNumSubdivisions(beatCount);

        subdivCount = _subdivCount;
    }

    public void setBeatAccent(boolean _beatAccent){
        beatAccent = _beatAccent;
    }

    public void setNumSubdivisions(int _beatNum, int _numSubdivisions){
        if (_beatNum < 1)
            _beatNum = 1;
        else if (_beatNum > numBeats)
            _beatNum = numBeats;

        if (_numSubdivisions < 1)
            _numSubdivisions = 1;
        else if (_numSubdivisions > 16)
            _numSubdivisions = 16;

        subdivisions[(_beatNum - 1)] = new boolean[_numSubdivisions];

        for (int i = 1; i <= _numSubdivisions; i++){
            setBeatSubdivision(_beatNum, i, true);
        }
    }

    public void setBeatSubdivision(int _beatNum, int _subdivisionNum, boolean _flag){
        if (_beatNum < 1)
            _beatNum = 1;
        else if (_beatNum > numBeats)
            _beatNum = numBeats;

        if (_subdivisionNum < 1)
            _subdivisionNum = 1;
        else if (_subdivisionNum > subdivisions[(_beatNum - 1)].length)
            _subdivisionNum = subdivisions[(_beatNum - 1)].length;

        subdivisions[(_beatNum - 1)][(_subdivisionNum - 1)] = _flag;
    }


    public int getTempo(){
        return tempo;
    }

    public int getNumBeats(){
        return numBeats;
    }

    public int getSelectedBeat(){
        return selectedBeat;
    }

    public int getBeatCount(){
        return beatCount;
    }

    public int getSubdivCount(){
        return subdivCount;
    }

    public boolean getBeatAccent(){
        return beatAccent;
    }

    public boolean[][] getSubdivisions(){
        return subdivisions;
    }

    public boolean getSubdivisionActivated(int _beatNum, int _subdivisionNum){
        if (_beatNum < 1)
            _beatNum = 1;
        else if (_beatNum > numBeats)
            _beatNum = numBeats;

        if (_subdivisionNum < 1)
            _subdivisionNum = 1;
        else if (_subdivisionNum > subdivisions[(_beatNum - 1)].length)
            _subdivisionNum = subdivisions[(_beatNum - 1)].length;

        return subdivisions[(_beatNum - 1)][(_subdivisionNum - 1)];
    }

    public int getNumSubdivisions(int _beatNum){
        if (_beatNum < 1)
            _beatNum = 1;
        else if (_beatNum > numBeats)
            _beatNum = numBeats;

        return subdivisions[(_beatNum - 1)].length;
    }

    public void incrementBeatCount(){

        beatCount++;

        if (beatCount > numBeats)
            beatCount = 1;

    }

    public void incrementSubdivCount(){

        subdivCount++;

        if (subdivCount > getNumSubdivisions(beatCount)){
            subdivCount = 1;
            incrementBeatCount();
        }

    }

    public void applyPattern(){

        int subdivs = getNumSubdivisions(selectedBeat);

        for (int i = (selectedBeat + 1); i <= numBeats; i++){

            setNumSubdivisions(i, subdivs);

            for (int j = 1; j <= subdivs; j++){
                boolean isActive = getSubdivisionActivated(selectedBeat, j);

                setBeatSubdivision(i, j, isActive);
            }
        }
    }

    public void rewindPattern(){
        setBeatCount(1);
        setSubdivCount(1);
    }
}


