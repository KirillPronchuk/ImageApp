package com.company.app.gui.image;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.opencv.core.Core;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

public class CcProcessor {

    private CascadeClassifier classifier;

    public CascadeClassifier getClassifier() throws IOException {
        if (classifier == null) {
//            System.setProperty("java.library.path", "/home/kirill/Documents/Localizer/OpenCV/build/lib");
//            System.loadLibrary("opencv_java341");
//            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

//            InputStream inputStream = new ClassPathResource("com/company/app/gui/image/saved_model.pbtxt").getInputStream();

//            StringWriter writer = new StringWriter();
//            IOUtils.copy(inputStream, writer, "UTF-8");
//            String theString = writer.toString();


//            String libName = "";
//            if (SystemUtils.IS_OS_WINDOWS) {
//                libName = "opencv_java341.dll";
//            } else if (SystemUtils.IS_OS_LINUX) {
//                libName = "libopencv_java341.so";
//            }
//            InputStream inputStream  = new ClassPathResource("com/company/app/gui/image/" + libName).getInputStream();
//
//            File lib = new File("libopencv_java341.so");
//            FileUtils.copyInputStreamToFile(inputStream, lib);
//            System.load(lib.getAbsolutePath());
//            String absolutePath = lib.getAbsolutePath();
//            String filePath = absolutePath.
//                    substring(0,absolutePath.lastIndexOf(File.separator));
//
//            System.setProperty("java.library.path", filePath);
            System.loadLibrary("opencv_java341");

            classifier = new CascadeClassifier("/home/kirill/IdeaProjects/ImageApp/modules/gui/src/com/company/app/gui/image/cascade.xml");
        }
        return classifier;
    }

}
