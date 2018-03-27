package com.company.app.gui.image;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;

public class TfProcessor {
    private Session session;

    public Session getSession() {
        if(session==null){
            SavedModelBundle smb = SavedModelBundle.load(".", "serve");
            session = smb.session();
        }
        return session;
    }


}
