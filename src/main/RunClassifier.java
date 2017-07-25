package main;

import main.cifar10.Cifar10BinaryReader;
import main.classifier.Classifiers;
import main.classifier.KNNClassifier;
import main.data.ClassifierImage;
import org.apache.commons.collections4.KeyValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RunClassifier {

    public static void main(String[] args) throws IOException {
        runKNNClassifier();
    }

    /**
     * Trains the K nearest neighbor classifier on the training image set, labels the test images,
     * and finds how accurately the classifier labelled the test images compared to the known correct labels.
     *
     * @throws IOException
     */
    private static void runKNNClassifier() throws IOException {
        Logger.setToConsoleLogger();

        // load CIFAR-10 training and test images
        Logger.log("Loading training images...");
        List<KeyValue<String, ClassifierImage>> labeledTrainingImages = Cifar10BinaryReader.loadTrainingData();
        Logger.log("Loaded " + labeledTrainingImages.size() + " training images");

        Logger.log("Loading test images");
        List<KeyValue<String, ClassifierImage>> labeledTestImages = Cifar10BinaryReader.loadTestData();
        Logger.log("Loaded " + labeledTestImages.size() + " test images");


        // set aside part of the training set for validation
        // 1 out of every numFolds images will be used for validation
        List<KeyValue<String, ClassifierImage>> labeledValidationImages
                = Classifiers.extractValidationSet(labeledTrainingImages, 50);


        // train the k-nearest neighbor classifier on the training images
        KNNClassifier knnClassifier = new KNNClassifier();
        knnClassifier.train(labeledTrainingImages);


        // find k-value for k nearest neighbor with highest accuracy on validation set
        Logger.log("Tuning k-value:");
        int maxAccuracyKValue = KNNClassifier.findMaxAccuracyKValue(labeledValidationImages, knnClassifier);
        Logger.log("Using " + maxAccuracyKValue + " nearest neighbor");


        // evaluation
        Logger.log("\nPredicting...");
        List<ClassifierImage> testImages = new ArrayList<>();
        for(KeyValue<String,ClassifierImage> kv : labeledTestImages) {
            testImages.add(kv.getValue());
        }

        // classify images in test set using k nearest neighbor classifier
        List<KeyValue<String, ClassifierImage>> predictedLabeledImages = knnClassifier.predict(testImages, maxAccuracyKValue);

        // display accuracy of classifier-attributed labels
        double accuracyPercentage = Classifiers.calculateAccuracy(labeledTestImages, predictedLabeledImages)*100;
        Logger.log("Accuracy: " + accuracyPercentage + "%");
    }

}
