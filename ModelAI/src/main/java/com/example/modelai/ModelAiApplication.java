package com.example.modelai;

import com.example.modelai.model.Datamodel;
import com.example.modelai.service.AIservice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ModelAiApplication {

    public static void main(String[] args) {

//        AIservice aiService = new AIservice();
//
//        // Creating training data
//        List<Datamodel> trainingData = new ArrayList<>();
//        trainingData.add(new Datamodel(0.1, 0.2, 0.3));
//        trainingData.add(new Datamodel(0.2, 0.3, 0.5));
//        trainingData.add(new Datamodel(0.3, 0.4, 0.7));
//        trainingData.add(new Datamodel(0.4, 0.5, 0.9));
//
//        aiService.trainModel(trainingData);
//
//        // Evaluating the model with test data
//        Datamodel testData = new Datamodel(0.5, 0.5, 1.0);
//        aiService.evaluateModel(testData);

        SpringApplication.run(ModelAiApplication.class, args);
    }

}
