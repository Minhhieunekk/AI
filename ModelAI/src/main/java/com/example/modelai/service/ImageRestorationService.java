// ModelAI/src/main/java/com/example/modelai/service/ImageRestorationService.java
package com.example.modelai.service;

import com.example.modelai.model.ImageModel;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class ImageRestorationService {
    private final ImageModel imageModel;
    private static final int HEIGHT = 128;
    private static final int WIDTH = 128;
    private static final int CHANNELS = 3;
    private static final String MODEL_PATH = "image_restoration_model.zip";

    public ImageRestorationService() {
        this.imageModel = new ImageModel();
        loadModel();
    }

    private void loadModel() {
        try {
            File modelFile = new File(MODEL_PATH);
            if (modelFile.exists()) {
                imageModel.setNetwork(ModelSerializer.restoreMultiLayerNetwork(modelFile));
                System.out.println("Model loaded successfully.");
            } else {
                System.out.println("Model not found, please train first.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

public String restoreImage(MultipartFile file) throws IOException {
    if (imageModel.getNetwork() == null) {
        return "Model not trained. Please train first.";
    }

    BufferedImage image = ImageIO.read(file.getInputStream());
    NativeImageLoader loader = new NativeImageLoader(HEIGHT, WIDTH, CHANNELS);
    INDArray input = loader.asMatrix(image);

    input = input.div(255.0);
    INDArray output = imageModel.getNetwork().output(input);
    output = output.mul(255.0).reshape(HEIGHT, WIDTH, CHANNELS);

    // Convert INDArray to BufferedImage
    long[] outputShape = output.shape();
    BufferedImage restoredImage = new BufferedImage((int) outputShape[1], (int) outputShape[0], BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < outputShape[0]; y++) {
        for (int x = 0; x < outputShape[1]; x++) {
            int r = Math.min(Math.max((int) output.getDouble(y, x, 0), 0), 255);
            int g = Math.min(Math.max((int) output.getDouble(y, x, 1), 0), 255);
            int b = Math.min(Math.max((int) output.getDouble(y, x, 2), 0), 255);
            restoredImage.setRGB(x, y, (r << 16) | (g << 8) | b);
        }
    }

    String outputPath = "restored_" + System.currentTimeMillis() + ".jpg";
    ImageIO.write(restoredImage, "jpg", new File(outputPath));

    return "Image restored and saved at: " + outputPath;
}

   private MultiLayerConfiguration createModelConfiguration() {
        return new NeuralNetConfiguration.Builder()
            .seed(123)
            .updater(new Adam(0.001))
            .l2(0.00001)
            .miniBatch(false)  // Process one sample at a time
            .list()
            .layer(0, new ConvolutionLayer.Builder()
                .kernelSize(3, 3)
                .stride(2, 2)  // Reduce spatial dimensions
                .nIn(CHANNELS)
                .nOut(16)      // Further reduced number of filters
                .activation(Activation.RELU)
                .build())
            .layer(1, new ConvolutionLayer.Builder()
                .kernelSize(3, 3)
                .stride(1, 1)
                .nOut(16)      // Further reduced number of filters
                .activation(Activation.RELU)
                .build())
            .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nOut(HEIGHT * WIDTH * CHANNELS)
                .activation(Activation.IDENTITY)
                .build())
            .setInputType(InputType.convolutional(HEIGHT, WIDTH, CHANNELS))
            .build();
    }

    // Method to resize the image
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

    // Modified trainModel method
    public String trainModel(String datasetPath) {
        try {
            File datasetDir = new File(datasetPath);
            File degradedDir = new File(datasetDir, "degraded");
            File originalDir = new File(datasetDir, "original");

            if (!originalDir.exists() || !degradedDir.exists()) {
                throw new IllegalStateException("Missing required directories");
            }

            MultiLayerConfiguration config = createModelConfiguration();
            MultiLayerNetwork network = new MultiLayerNetwork(config);
            network.init();

            NativeImageLoader loader = new NativeImageLoader(HEIGHT, WIDTH, CHANNELS);
            File[] degradedFiles = degradedDir.listFiles();
            File[] originalFiles = originalDir.listFiles();

            if (degradedFiles == null || originalFiles == null ||
                degradedFiles.length == 0 || originalFiles.length == 0) {
                throw new IllegalStateException("No images found");
            }

            System.gc();  // Request garbage collection before training

            // Train on first image pair
            BufferedImage degradedImage = ImageIO.read(degradedFiles[0]);
            BufferedImage originalImage = ImageIO.read(originalFiles[0]);

            // Resize images
            BufferedImage resizedDegradedImage = resizeImage(degradedImage, HEIGHT, WIDTH);
            BufferedImage resizedOriginalImage = resizeImage(originalImage, HEIGHT, WIDTH);

            INDArray input = loader.asMatrix(resizedDegradedImage);
            INDArray label = loader.asMatrix(resizedOriginalImage);

            input.divi(255.0);
            label.divi(255.0);

            for (int epoch = 0; epoch < 5; epoch++) {  // Further reduced epochs
                network.fit(input, label);
                System.out.println("Completed epoch " + (epoch + 1));
            }

            imageModel.setNetwork(network);
            ModelSerializer.writeModel(network, new File(MODEL_PATH), true);
            return "Training completed with " + degradedFiles.length + " image pairs";
        } catch (Exception e) {
            throw new IllegalStateException("Failed to train model: " + e.getMessage());
        }
    }


private void trainNetwork(RecordReaderDataSetIterator iterator) {
    for (int epoch = 0; epoch < 50; epoch++) {
        System.out.println("Epoch " + (epoch + 1) + "/50");
        while (iterator.hasNext()) {
            imageModel.getNetwork().fit(iterator.next());
        }
        iterator.reset();
    }
}
}
