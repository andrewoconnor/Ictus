package com.andrewoconnor.Ictus;

import android.app.Application;
import android.os.PowerManager;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew
 * Date: 2/26/13
 * Time: 6:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Loop extends Application implements Serializable {

    private ArrayList<Pattern> patterns = new ArrayList<Pattern>();
    private ArrayList<Integer> loop = new ArrayList<Integer>();
    private int selectedPatternNum;
    private int currLoopPosition;
    private boolean wasSaved;
    private String loopName;

    public Loop(){
        setSelectedPatternNum(1);
        setCurrLoopPosition(0);
        setLoopName("untitled");
        Pattern p = new Pattern();
        createPattern(p);
    }

    public void setLoop(Loop _l){

        ArrayList<Pattern> tempPat = _l.getPatterns();
        patterns.clear();
        for(int i = 0; i < tempPat.size(); i++){
            patterns.add(tempPat.get(i));
        }

        ArrayList<Integer> tempLoop = _l.getLoop();
        loop.clear();
        for(int i = 0; i < tempLoop.size(); i++){
            loop.add(tempLoop.get(i));
        }

        selectedPatternNum = _l.getSelectedPatternNum();

        currLoopPosition = _l.getCurrLoopPosition();

        wasSaved = _l.getWasSaved();

        loopName = _l.getLoopName();

    }

    public void setSelectedPatternNum(int _selectedPatternNum){
        selectedPatternNum = _selectedPatternNum;
    }

    public void setCurrLoopPosition(int _currLoopPosition){
        currLoopPosition = _currLoopPosition;
    }

    public void setWasSaved(boolean _wasSaved){
        wasSaved = _wasSaved;
    }

    public void setLoopName(String _loopName){
        loopName = _loopName;
    }

    public void createPattern(Pattern _pattern){
        patterns.add(_pattern);
    }

    public void addPatternToLoop(){
        loop.add(selectedPatternNum);
    }

    public void setPatternAtPosition(int _index, int _patternNum){
        loop.set(_index, _patternNum);
    }

    public void removePattern(int _index){
        loop.remove(_index);
    }

    public void swapPatterns(int _index1, int _index2){
        int temp = loop.set(_index1, loop.get(_index2)) ;
        loop.set(_index2, temp) ;
    }

    public Pattern getNextPatternInLoop(){
        currLoopPosition++;
        if(currLoopPosition > getNumPatternsInLoop()){
            currLoopPosition = 1;
        }
        return getPattern(getPatternNum(currLoopPosition - 1));
    }

    public int getSelectedPatternNum(){
        return selectedPatternNum;
    }

    public int getCurrLoopPosition(){
        return currLoopPosition;
    }

    public int getNumPatterns(){
        return patterns.size();
    }

    public int getNumPatternsInLoop(){
        return loop.size();
    }

    public int getPatternNum(int _index){
        return loop.get(_index);
    }

    public boolean getWasSaved(){
        return wasSaved;
    }

    public String getLoopName(){
        return loopName;
    }

    public Pattern getCurrentPattern(){
        return patterns.get(selectedPatternNum - 1);
    }

    public Pattern getPattern(int _index){
        return patterns.get(_index - 1);
    }

    public ArrayList<Pattern> getPatterns(){
        return patterns;
    }

    public ArrayList<Integer> getLoop(){
        return loop;
    }

    public void rewindLoop(){

        for(int i = 1; i <= getNumPatterns(); i++){

            getPattern(i).rewindPattern();
        }

        setCurrLoopPosition(1);
        setSelectedPatternNum(1);
    }

}
