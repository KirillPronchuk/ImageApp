/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.company.app.gui.image;

//import Image;

import com.haulmont.cuba.core.app.FileStorageService;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Embedded;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.*;
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
    private FileDescriptor imageDescriptor;

    private static final int IMG_HEIGHT = 600;
    private static final int IMG_WIDTH = 800;

//    private static final int IMG_HEIGHT_MOD = 32;
//    private static final int IMG_WIDTH_MOD = 32;
//
//    private static final int NB_LABELS = 43;
//
//    private static int conv_1_size = 9;
//    private static int conv_1_nb = 256;
//    private static int conv_2_size = 6;
//    private static int conv_2_nb = 64;


    @Override
    public void init(Map<String, Object> params) {
        uploadField.addFileUploadSucceedListener(event -> {

            File file = fileUploadingAPI.getFile(uploadField.getFileId());
            if (file != null) {
                int resolvedClass = predict(file.getAbsolutePath());
                if(resolvedClass!=-1){
                    String className = Labels.getLabels().get(resolvedClass);
                    showNotification("Predicted class: " + resolvedClass + "\nClass name: " + className, NotificationType.HUMANIZED);
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

    private int predict(String fileName) {
        int result = -1;
        String prefix = "C:\\Users\\Kirill\\studio-projects\\PredictionScript\\";
        try {
            ProcessBuilder pb = new ProcessBuilder("python", prefix + "script\\test_single.py", prefix + "script\\outputs\\checkpoints\\c1s_9_c1n_256_c2s_6_c2n_64_c2d_0.7_c1vl_16_c1s_5_c1nf_16_c2vl_32_lr_0.0001_rs_1--TrafficSign--1511246130.7228131", fileName);
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String ret = in.readLine();
            if(StringUtils.isNotBlank(ret)){
                try {
                    result = new Integer(ret).intValue();
                } catch (NumberFormatException e){
                    result = -1;
                }
            }

            BufferedReader in_err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line = "";
            while (true) {
                String tempLine = in_err.readLine();
                if (tempLine == null) { break; }
                line = line + tempLine + "\n";
            }
            if(StringUtils.isNotBlank(line)){
                System.out.println("Error: " + line);
            }
        } catch (IOException e) {
            showNotification("Cannot perform prediction script", NotificationType.HUMANIZED);
        }
        return result;
    }

    private static class Labels{
        private static Map<Integer, String> labels = new HashMap<>();
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

//    private static BufferedImage resize(BufferedImage origin, int newWidth, int newHeight) {
//        java.awt.Image scaledInstance = origin.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH);
//        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
//
//        Graphics2D g2d = image.createGraphics();
//        g2d.drawImage(scaledInstance, 0, 0, null);
//        g2d.dispose();
//
//        return image;
//    }

//    private static float[][][] createArrayFromImage (BufferedImage image){
//        BufferedImage resizedImage = resize(image, IMG_WIDTH_MOD, IMG_HEIGHT_MOD);
////        int width = resizedImage.getWidth();
////        int height = resizedImage.getHeight();
//        float [][][] result = new float[IMG_HEIGHT_MOD][IMG_WIDTH_MOD][3];
//        Color color;
//        for(int i = 0; i<IMG_HEIGHT_MOD; i++){
//            for (int j = 0; j<IMG_WIDTH_MOD; j++){
//                color = new Color(image.getRGB(i, j));
//                result[i][j][0] = color.getRed()/255;
//                result[i][j][1] = color.getGreen()/255;
//                result[i][j][2] = color.getBlue()/255;
//            }
//        }
//        return result;
//    }
//
//    private static int maxIndex(float[] probabilities) {
//        int best = 0;
//        for (int i = 1; i < probabilities.length; ++i) {
//            if (probabilities[i] > probabilities[best]) {
//                best = i;
//            }
//        }
//        return best;
//    }
//
//    private void initGraph(){
//        try (Graph g = new Graph();
//             Session s = new Session(g)) {
//
//            Output tf_conv_2_dropout = g.opBuilder("Placeholder", "conv_2_dropout")
//                    .setAttr("dtype", DataType.FLOAT)
//                    .setAttr("shape", Shape.unknown())
//                    .build().output(0);
//
//            Output  tf_images = g.opBuilder("Placeholder", "images")
//                    .setAttr("dtype", DataType.FLOAT)
//                    .setAttr("shape", Shape.make(-1, 32, 32, 3))
//                    .build().output(0);
//
//            Output  tf_labels = g.opBuilder("Placeholder", "labels")
//                    .setAttr("dtype", DataType.INT64)
//                    .setAttr("shape", Shape.make(-1))
//                    .build().output(0);
//        }
//    }
//
//    private Tensor buildMainNetwork(Output images, Output conv_2_dropout){
//        Shape shape = Shape.make(conv_1_size, conv_1_size, 3, conv_1_nb);
//
//    }
//
//    private Tensor create_conv(Output prev, Shape shape){
//
//    }
}
