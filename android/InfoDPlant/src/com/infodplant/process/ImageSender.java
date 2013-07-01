package com.infodplant.process;

import android.os.AsyncTask;

/**
 * Sends the processed picture to the server, returns the callback from the server (the result).
 * Created by marto on 6/30/13.
 */
public class ImageSender extends AsyncTask<String, Void, String>{
    //TODO Use a ProgressValue

    @Override
    protected String doInBackground(String... strings) {
        //TODO send compressed image to server, wait for a answer.
        return null;
    }
}
