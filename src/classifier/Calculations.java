package classifier;

import data.ClassifierImage;
import org.apache.commons.collections4.KeyValue;

import java.util.List;

public class Calculations {

    /**
     * Calculates the accuracy of the predicted classes on the test data by comparing against the actual test data classes
     *
     * @param actualClassifications
     * @param predictedClassifications
     * @return
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
}
