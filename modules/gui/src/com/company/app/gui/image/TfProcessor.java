package com.company.app.gui.image;


import org.apache.commons.io.FileUtils;
import org.tensorflow.Graph;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;

import java.io.File;
import java.io.IOException;

public class TfProcessor {
    private Session session;

    public Session getSession() {
        if(session==null){

            try (Graph graph = new Graph()) {
                graph.importGraphDef(FileUtils.readFileToByteArray(new File("C:/Users/Kirill/Documents/TensorflowModel/saved_model.pbtxt")));
                session = new Session(graph);
            } catch (IOException e){

            }
//            SavedModelBundle smb = SavedModelBundle.load("C:/Users/Kirill/Documents/TensorflowModel", "serve");
//            session = smb.session();
        }
        return session;
    }


}
