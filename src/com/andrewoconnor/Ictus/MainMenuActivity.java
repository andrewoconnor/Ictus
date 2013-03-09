package com.andrewoconnor.Ictus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew
 * Date: 2/23/13
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainMenuActivity extends Activity {

    private Loop mLoop;
    private Button btnNewLoop;
    private Button btnSave;

    private boolean mLoading;

    private AlertDialog mLoadAlert;

    private String mFileToLoad;

    private PowerManager.WakeLock mWakeLock;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        mLoading = false;

        mLoop = (Loop) getApplicationContext();

        btnNewLoop = (Button) findViewById(R.id.btnNewLoop);
        btnSave = (Button) findViewById(R.id.btnSaveLoop);

        PowerManager mgr = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        mWakeLock.acquire();

    }

    @Override
    public void onPause(){
        super.onPause();

        try{
            if(mWakeLock.isHeld())
                mWakeLock.release();
        } catch (Exception e){

        }
    }

    public void newLoop(View v){

        if (!mLoading){

            Loop loop = new Loop();
            ((Loop) getApplicationContext()).setLoop(loop);
        }

        mLoading = false;

        Intent activity1 = new Intent(MainMenuActivity.this, LoopActivity.class);
        startActivity(activity1);
    }

    public void saveLoop(View v){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Name save file");
        alert.setIcon(R.drawable.ic_launcher);

        // Set an EditText view to get user input
        final EditText saveNameInput = new EditText(this);

        saveNameInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if(source.charAt(i) == '-'){
                        return "";
                    }
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }

                }
                return null;
            }
        };

        saveNameInput.setFilters(new InputFilter[]{filter});

        saveNameInput.setText(mLoop.getLoopName());
        saveNameInput.selectAll();

        alert.setView(saveNameInput);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (saveNameInput.getText().toString().trim().length() > 0){

                    checkIfFileExists(saveNameInput.getText().toString());
                } else {
                    btnSave.performClick();
                }

                // Do something with value!
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }

    public void saveHelper(String _saveName, String _loopName){

        mLoop.rewindLoop();
        mLoop.setWasSaved(true);
        mLoop.setLoopName(_loopName);

        try {

            FileOutputStream fos = getBaseContext().openFileOutput(_saveName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(mLoop);
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }



        Toast.makeText(getApplicationContext(), "Loop saved as: " + _saveName, Toast.LENGTH_SHORT).show();

    }

    public void checkIfFileExists(String _loopName){

        final String loopName = _loopName;
        final String saveName = loopName + ".sav";

        final String[] files = getApplicationContext().fileList();
        boolean fileExists = false;

        for (int i = 0; i < files.length; i++){
            if (saveName.equals(files[i])){
                fileExists = true;
            }
        }

        if (fileExists){

            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Overwrite " + saveName + "?");
            alert.setIcon(R.drawable.ic_launcher);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    saveHelper(saveName, loopName);
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();

        } else {
            saveHelper(saveName, loopName);
        }
    }

    public void loadLoop(View v){

        final String[] files = getApplicationContext().fileList();

        if (files.length != 0){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select file to load\nLong click to delete");
            builder.setIcon(R.drawable.ic_launcher);
            builder.setItems(files, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    mFileToLoad = files[item];
                    mLoadAlert.hide();
                    loadHelper();
                }
            });

            mLoadAlert = builder.create();

            mLoadAlert.setOnShowListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface dialog)
                {
                    ListView lv = mLoadAlert.getListView(); //this is a ListView with your "buds" in it
                    lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
                    {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            mFileToLoad = files[position];
                            mLoadAlert.hide();
                            File file = new File(getBaseContext().getFilesDir().getAbsolutePath(), mFileToLoad);
                            confirmDelete(file);
                            return true;
                        }
                    });
                }
            });

            mLoadAlert.show();

        } else {
            Toast.makeText(getApplicationContext(), "No files found.", Toast.LENGTH_SHORT).show();
        }

    }

    public void loadHelper(){

        mLoading = true;

        Loop loop = new Loop();

        try {

            FileInputStream fis = getBaseContext().openFileInput(mFileToLoad);
            ObjectInputStream is = new ObjectInputStream(fis);
            loop = (Loop) is.readObject();
            ((Loop) getApplicationContext()).setLoop(loop);
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        btnNewLoop.performClick();
    }

    public void confirmDelete(File _file){

        final File file = _file;

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Delete " + _file.getName() + "?");
        alert.setIcon(R.drawable.ic_launcher);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                file.delete();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public void help(View v){

        AlertDialog.Builder help = new AlertDialog.Builder(this);
        help.setTitle("Help");
        help.setIcon(R.drawable.ic_launcher);

        ScrollView helpView = new ScrollView(this);

        TextView myMsg = new TextView(this);
        myMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        myMsg.setText(R.string.helpInfo);
        myMsg.setGravity(Gravity.CENTER);

        help.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        helpView.addView(myMsg);

        help.setView(helpView);
        help.show();

    }


    public void exitIctus(View v){
        onPause();
        finish();
    }

}
