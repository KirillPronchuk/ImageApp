package com.company.app.gui.image;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;

public class TfProcessor {
    private Session session;

    public Session getSession() {
        if(session==null){
            SavedModelBundle smb = SavedModelBundle.load("/home/kirill/IdeaProjects/tfloader/src/main/resources", "serve");
            session = smb.session();
        }
        return session;
    }


}
