/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.company.app.gui.image;

//import Image;

import com.haulmont.cuba.core.app.FileStorageService;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Embedded;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kirill on 22.11.2017.
 */

public class ImageEdit extends AbstractEditor<com.company.app.entity.Image> {

    private Logger log = LoggerFactory.getLogger(ImageEdit.class);

    @Inject
    private DataSupplier dataSupplier;
    @Inject
    private FileStorageService fileStorageService;
    @Inject
    private FileUploadingAPI fileUploadingAPI;
    @Inject
    private ExportDisplay exportDisplay;

    @Inject
    private Embedded embeddedImage;
    @Inject
    private FileUploadField uploadField;
    @Inject
    private Button downloadImageBtn;
    @Inject
    private Button clearImageBtn;
    @Inject
    private Datasource<com.company.app.entity.Image> imageDs;
    @Inject
    private UserSessionSource uss;

    private static final int IMG_HEIGHT = 600;
    private static final int IMG_WIDTH = 800;


    @Override
    public void init(Map<String, Object> params) {
        uploadField.addFileUploadSucceedListener(event -> {

            AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(TfConfig.class);

            TfProcessor processor = context.getBean("tfProcessorService", TfProcessor.class);

            Session session = processor.getSession();


            File file = fileUploadingAPI.getFile(uploadField.getFileId());
            if (file != null) {
                try{
                    BufferedImage image = ImageIO.read(file);
                    int resolvedClass = predict(image, session);
                    if(resolvedClass!=-1){
                        String className = Labels.getLabels().get(resolvedClass);
                        showNotification("Predicted class: " + resolvedClass + "\nClass name: " + className, NotificationType.HUMANIZED);
                    } else {
                        showNotification("Not a sign", NotificationType.HUMANIZED);
                    }
                } catch (IOException e){
                    showNotification("Cannot read image", NotificationType.HUMANIZED);
                }
            } else
                showNotification("Cannot find file in temporary storage", NotificationType.HUMANIZED);

            FileDescriptor fd = uploadField.getFileDescriptor();
            try {
                fileUploadingAPI.putFileIntoStorage(uploadField.getFileId(), fd);
            } catch (FileStorageException e) {
                throw new RuntimeException("Error saving file to FileStorage", e);
            }
            getItem().setImageFile(dataSupplier.commit(fd));
            displayImage();
        });

        uploadField.addFileUploadErrorListener(event ->
                showNotification("File upload error", NotificationType.HUMANIZED));

        imageDs.addItemPropertyChangeListener(event -> {
            if ("imageFile".equals(event.getProperty()))
                updateImageButtons(event.getValue() != null);
        });
    }

    @Override
    protected void postInit() {
        displayImage();
        updateImageButtons(getItem().getImageFile() != null);
    }

    public void onDownloadImageBtnClick() {
        if (getItem().getImageFile() != null)
            exportDisplay.show(getItem().getImageFile(), ExportFormat.OCTET_STREAM);
    }

    public void onClearImageBtnClick() {
        getItem().setImageFile(null);
        displayImage();
    }

    private void updateImageButtons(boolean enable) {
        downloadImageBtn.setEnabled(enable);
        clearImageBtn.setEnabled(enable);
    }

    private void displayImage() {
        byte[] bytes = null;
        if (getItem().getImageFile() != null) {
            try {
                bytes = fileStorageService.loadFile(getItem().getImageFile());
            } catch (FileStorageException e) {
                log.error("Unable to load image file", e);
                showNotification("Unable to load image file", NotificationType.HUMANIZED);
            }
        }
        if (bytes != null) {
            embeddedImage.setSource(getItem().getImageFile().getName(), new ByteArrayInputStream(bytes));
            embeddedImage.setType(Embedded.Type.IMAGE);
            BufferedImage image;
            try {
                image = ImageIO.read(new ByteArrayInputStream(bytes));
                int width = image.getWidth();
                int height = image.getHeight();

                if (((double) height / (double) width) > ((double) IMG_HEIGHT / (double) IMG_WIDTH)) {
                    embeddedImage.setHeight(String.valueOf(IMG_HEIGHT));
                    embeddedImage.setWidth(String.valueOf(width * IMG_HEIGHT / height));
                } else {
                    embeddedImage.setWidth(String.valueOf(IMG_WIDTH));
                    embeddedImage.setHeight(String.valueOf(height * IMG_WIDTH / width));
                }
            } catch (IOException e) {
                log.error("Unable to resize image", e);
            }
            // refresh image
            embeddedImage.setVisible(false);
            embeddedImage.setVisible(true);
        } else {
            embeddedImage.setVisible(false);
        }
    }

    private int predict(BufferedImage image, Session session) {
        int result = -1;
        float[] array = resizeAndConvert2Grayscale(image);
        Tensor inputTensor = createInputTensor(array);
        Tensor outputTensor = performModel(session, inputTensor);
        result = predictClass(outputTensor);

        return result;
    }

    private float[] resizeAndConvert2Grayscale(BufferedImage source){
        float[] array = new float[1024];
        try{
            int IMG_WIDTH = 32;
            int IMG_HEIGHT = 32;
            BufferedImage gray = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, source.getType());
            Graphics2D g2d = gray.createGraphics();
            g2d.drawImage(source, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
            g2d.dispose();
            for (int i = 0; i < gray.getHeight(); i++) {
                for (int j = 0; j < gray.getWidth(); j++) {
                    int rgb = gray.getRGB(i, j);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = (rgb & 0xFF);
                    int grayPixel = (r + g + b) / 3;
                    float floatPixel = (float)grayPixel;
                    array[j * IMG_WIDTH + i] =  floatPixel/255*2-1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }

    private int predictClass(Tensor resultTensor){
        float[][] m = new float[1][43];
        m[0] = new float[43];
        Arrays.fill(m[0], 0);


        float[][] matrix = (float[][]) resultTensor.copyTo(m);
        float maxVal = 0;
        int inc = 0;
        int predict = -1;
        for (float val : matrix[0]) {
            if (val > maxVal) {
                predict = inc;
                maxVal = val;
            }
            inc++;
        }
        return predict;
    }

    private Tensor createInputTensor(float[] array){
        Tensor inputTensor = Tensor.create(new long[]{1, 32, 32, 1});
        if(array!=null && array.length==1024){
            FloatBuffer fb = FloatBuffer.allocate(1024);
            fb.put(array);
            fb.rewind();
            inputTensor = Tensor.create(new long[]{1, 32, 32, 1}, fb);
        }
        return inputTensor;
    }

    private Tensor performModel(Session sess, Tensor inputTensor){
        Tensor result = sess.runner()
                .feed("input_tensor", inputTensor)
                .fetch("output_tensor")
                .run().get(0);
        return result;
    }

    private static class Labels{
        private static final Map<Integer, String> labels = new HashMap<>();
        static {
            labels.put(0, "Speed limit (20km/h)");
            labels.put(1, "Speed limit (30km/h)");
            labels.put(2, "Speed limit (50km/h)");
            labels.put(3, "Speed limit (60km/h)");
            labels.put(4, "Speed limit (70km/h)");
            labels.put(5, "Speed limit (80km/h)");
            labels.put(6, "End of speed limit (80km/h)");
            labels.put(7, "Speed limit (100km/h)");
            labels.put(8, "Speed limit (120km/h)");
            labels.put(9, "No passing");
            labels.put(10, "No passing for vehicles over 3.5 metric tons");
            labels.put(11, "Right-of-way at the next intersection");
            labels.put(12, "Priority road");
            labels.put(13, "Yield");
            labels.put(14, "Stop");
            labels.put(15, "No vehicles");
            labels.put(16, "Vehicles over 3.5 metric tons prohibited");
            labels.put(17, "No entry");
            labels.put(18, "General caution");
            labels.put(19, "Dangerous curve to the left");
            labels.put(20, "Dangerous curve to the right");
            labels.put(21, "Double curve");
            labels.put(22, "Bumpy road");
            labels.put(23, "Slippery road");
            labels.put(24, "Road narrows on the right");
            labels.put(25, "Road work");
            labels.put(26, "Traffic signals");
            labels.put(27, "Pedestrians");
            labels.put(28, "Children crossing");
            labels.put(29, "Bicycles crossing");
            labels.put(30, "Beware of ice/snow");
            labels.put(31, "Wild animals crossing");
            labels.put(32, "End of all speed and passing limits");
            labels.put(33, "Turn right ahead");
            labels.put(34, "Turn left ahead");
            labels.put(35, "Ahead only");
            labels.put(36, "Go straight or right");
            labels.put(37, "Go straight or left");
            labels.put(38, "Keep right");
            labels.put(39, "Keep left");
            labels.put(40, "Roundabout mandatory");
            labels.put(41, "End of no passing");
            labels.put(42, "End of no passing by vehicles over 3.5 metric tons");
        }

        public static Map<Integer, String> getLabels(){
            return labels;
        }
    }


}
