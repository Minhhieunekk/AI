package com.example.modelai.service;

import com.example.modelai.model.Datamodel;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIservice {
    private MultiLayerNetwork model;

    public AIservice() {
        MultiLayerConfiguration conf=new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Sgd(0.1))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(2).nOut(3).activation(Activation.RELU).build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).nIn(3).nOut(1).activation(Activation.IDENTITY).build())
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

    }

    public void trainModel(List<Datamodel> datamodelList)
    {
        int dataSize=datamodelList.size();
        INDArray input= Nd4j.create(dataSize, 2);
        INDArray labels=Nd4j.create(dataSize, 1);

        for(int i=0; i<dataSize; i++)
        {
            Datamodel datamodel=datamodelList.get(i);
            input.putRow(i, Nd4j.create(new double[]{datamodel.getFeature1(), datamodel.getFeature2()}));
            labels.putRow(i, Nd4j.create(new double[]{datamodel.getLabel()}));
        }

        DataSet dataSet=new DataSet(input, labels);
        DataSetIterator dataSetIterator=new ListDataSetIterator<>(dataSet.asList(), 10);

        int numEpochs=1000;
        for (int i=0; i<numEpochs; i++)
        {
            model.fit(dataSetIterator);
            if(i%100==0)
            {
                System.out.println("Score at iteration " + i + " is " + model.score());
            }
        }
    }
public void evaluateModel(Datamodel datamodel) {
    INDArray input = Nd4j.create(new double[][]{{datamodel.getFeature1(), datamodel.getFeature2()}});
    INDArray output = model.output(input);
    System.out.println("Predicted value is " + output.getDouble(0));
}
}
