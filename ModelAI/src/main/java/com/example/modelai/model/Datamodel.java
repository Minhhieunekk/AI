package com.example.modelai.model;

import lombok.Data;

@Data
public class Datamodel {
    private double feature1;
    private double feature2;
    private double label;

    public Datamodel(double feature1, double feature2, double label) {
        this.feature1 = feature1;
        this.feature2 = feature2;
        this.label = label;
    }
}
