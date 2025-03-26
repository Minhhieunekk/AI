// ModelAI/src/main/java/com/example/modelai/model/ImageModel.java
package com.example.modelai.model;

import lombok.Data;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

@Data
public class ImageModel {
    private MultiLayerNetwork network;
    private static final int HEIGHT = 256;
    private static final int WIDTH = 256;
    private static final int CHANNELS = 3;
}
