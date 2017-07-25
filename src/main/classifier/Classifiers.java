package main.classifier;

import main.data.ClassifierImage;
import org.apache.commons.collections4.KeyValue;

import java.util.ArrayList;
import java.util.List;

public class Classifiers {

    /**
     * Calculates the accuracy of the predicted classes on the test data by comparing against the actual test data classes
     */
    public static double calculateAccuracy(List<KeyValue<String,ClassifierImage>> actualClassifications,
                                           List<KeyValue<String,ClassifierImage>> predictedClassifications) {
        int totalImages = 0;
        int successes = 0;

        for(KeyValue<String,ClassifierImage> predictedClassification : predictedClassifications) {
            if(actualClassifications.contains(predictedClassification)) {
                successes++;
            }
            totalImages++;
        }

        assert predictedClassifications.size() == totalImages;
        return successes/(double)totalImages;
    }

    /**
     * Removes 1 out of every <code>numFolds</code> images from a list of labeled images,
     * and returns the list of images removed
     */
    public static List<KeyValue<String, ClassifierImage>> extractValidationSet(
            List<KeyValue<String, ClassifierImage>> labeledTrainingImages, int numFolds) {

        List<KeyValue<String, ClassifierImage>> labeledValidationImages = new ArrayList<>();
        for(int i = labeledTrainingImages.size()-1; i >= 0; i -= numFolds) {
            labeledValidationImages.add(labeledTrainingImages.remove(i));
        }
        return labeledValidationImages;
    }
}
