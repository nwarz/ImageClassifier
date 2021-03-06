package main.classifier;

import main.data.ClassifierImage;
import main.Logger;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class KNNClassifier {

    private List<KeyValue<String, byte[]>> classifiedTrainingImages = new ArrayList<>();

    /**
     * "Trains" the k nearest neighbor classifier by adding all the training images to it
     * @param trainingSet
     */
    public void train(List<KeyValue<String,ClassifierImage>> trainingSet) {
        for(KeyValue<String,ClassifierImage> trainingImage : trainingSet) {
            classifiedTrainingImages.add(
                    new DefaultKeyValue<>(
                            trainingImage.getKey(),
                            trainingImage.getValue().toFlatImage()));
        }
    }

    /**
     * Picks the k-value for k-nearest-neighbor with the highest accuracy on the labeled validation images,
     * from an arbitrary list of k value candidates.
     *
     * @param labeledValidationImages
     * @param knnClassifier
     * @return k-value with the highest accuracy on validation set
     */
    public static int findMaxAccuracyKValue(
            List<KeyValue<String, ClassifierImage>> labeledValidationImages,
            KNNClassifier knnClassifier) {

        // arbitrary list of likely good k-values
        int kValueCandidates[] = {1,2,3,4,5,6,7,8,9};

        double kAccuracies[] = new double[kValueCandidates.length];
        Arrays.fill(kAccuracies, 0.);

        List<ClassifierImage> validationImages = new ArrayList<>();
        for (KeyValue<String, ClassifierImage> kv : labeledValidationImages) {
            validationImages.add(kv.getValue());
        }

        // check the accuracy of k-nearest neighbor on the validation set for each k-value candidate
        for (int i = 0; i < kValueCandidates.length; i++) {
            int k = kValueCandidates[i];
            List<KeyValue<String, ClassifierImage>> predictedValidationImages
                    = knnClassifier.predict(validationImages, k);

            kAccuracies[i] = Classifiers.calculateAccuracy(
                                    labeledValidationImages,
                                    predictedValidationImages);

            Logger.log("k-value " + kValueCandidates[i] + ": " + kAccuracies[i]*100 + "%");
        }

        double maxAccuracy = 0.;
        int maxAccuracyKValue = 1;
        for (int i = 0; i < kAccuracies.length; i++) {
            if (kAccuracies[i] > maxAccuracy) {
                maxAccuracy = kAccuracies[i];
                maxAccuracyKValue = kValueCandidates[i];
            }
        }
        return maxAccuracyKValue;
    }

    /**
     * Predicts classes for unlabeled images using k-nearest neighbor
     *
     * Compares unlabeled image inputs to the labeled training images stored in the classifier,
     * picks k quantity of labeled training images whose pixel values are closest to the unlabeled test image,
     * and selects a label for the test image based on the most frequent label for k closest training images.
     *
     * @param predictImages unlabeled images
     * @param k number of closest training images to consider
     * @return images labeled by k-nearest neighbor classification
     */
    public List<KeyValue<String,ClassifierImage>> predict(List<ClassifierImage> predictImages, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("invalid k value: " + k);
        }

        List<KeyValue<String, ClassifierImage>> testedImages = new ArrayList<>();

        for (ClassifierImage predictImage : predictImages) {

            byte[] flatPredictImage = predictImage.toFlatImage();

            List<KeyValue<Integer,String>> minDistances = new ArrayList<>();
            minDistances.add(new DefaultKeyValue<>(Integer.MAX_VALUE, "ERR_NO_CLASS"));

            // picks k quantity of the images in the training set closest to the unlabeled image
            for(KeyValue<String,byte[]> trainingImage : classifiedTrainingImages) {
                int distance = calculateImageDistance(flatPredictImage, trainingImage.getValue());

                if(distance < minDistances.get(minDistances.size()-1).getKey()) {
                    KeyValue<Integer,String> labelledDistance = new DefaultKeyValue<>(distance, trainingImage.getKey());
                    if(minDistances.size() >= k ) {
                        minDistances.set(minDistances.size()-1, labelledDistance);
                    } else {
                        minDistances.add(labelledDistance);
                    }

                    minDistances.sort(Comparator.comparing(KeyValue::getKey));
                }
            }

            Bag<String> nearestNeighbors = new HashBag<>();
            for(KeyValue<Integer,String> kv : minDistances) {
                nearestNeighbors.add(kv.getValue());
            }

            // pick a label for the unlabeled image
            // most frequent label for the k closest neighboring images is selected
            String label = "ERR_NO_LABEL";
            int maxVotes = 0;
            for(String vote : nearestNeighbors.uniqueSet()) {
                int count = nearestNeighbors.getCount(vote);
                if(count > maxVotes) {
                    maxVotes = count;
                    label = vote;
                }
            }
            testedImages.add(new DefaultKeyValue<>(label, predictImage));
        }

        return testedImages;
    }

    /**
     * Predicts classes for unlabeled images using 1-nearest neighbor
     *
     * @param predictImages unlabeled images
     * @return images labeled by 1-nearest neighbor classification
     */
    public List<KeyValue<String,ClassifierImage>> predict(List<ClassifierImage> predictImages) {
        return predict(predictImages,1);
    }

    /**
     * Calculates the sum of the differences between individual single-color pixel values between two flattened images
     *
     * @param flatImageA
     * @param flatImageB
     * @return
     */
    private int calculateImageDistance(byte[] flatImageA, byte[] flatImageB) {
        if(flatImageA.length != flatImageB.length) {
            throw new ArrayIndexOutOfBoundsException(
                    "unequal image sizes " + flatImageA.length + ", " + flatImageB.length);
        }

        int sum = 0;
        for(int i = 0; i < flatImageA.length; i++) {
            sum += Math.abs(flatImageA[i] - flatImageB[i]);
        }
        return sum;
    }
}