import cifar10.Cifar10BinaryReader;
import classifier.Calculations;
import classifier.KNNClassifier;
import data.ClassifierImage;
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

        // load CIFAR-10 training and test images
        List<KeyValue<String, ClassifierImage>> labeledTrainingImages = Cifar10BinaryReader.loadTrainingData();
        List<KeyValue<String, ClassifierImage>> labeledTestImages = Cifar10BinaryReader.loadTestData();

        //set aside part of the training set for validation
        // 1 out of every numFolds images will be used for validation
        List<KeyValue<String, ClassifierImage>> labeledValidationImages = new ArrayList<>();
        int numFolds = 50;
        for(int i = 0; i < labeledTrainingImages.size(); i+=numFolds) {
            labeledValidationImages.add(labeledTrainingImages.remove(i));
        }


        // train the k-nearest neighbor classifier on the training images
        KNNClassifier knnClassifier = new KNNClassifier();
        knnClassifier.train(labeledTrainingImages);

        // find k-value for k nearest neighbor with highest accuracy on validation set
        System.out.println("Tuning k-value:");
        int maxAccuracyKValue = KNNClassifier.findMaxAccuracyKValue(labeledValidationImages, knnClassifier);
        System.out.println("Using " + maxAccuracyKValue + " nearest neighbor");


        // evaluation
        System.out.println("\nPredicting...");
        List<ClassifierImage> testImages = new ArrayList<>();
        for(KeyValue<String,ClassifierImage> kv : labeledTestImages) {
            testImages.add(kv.getValue());
        }

        // classify images in test set using k nearest neighbor classifier
        List<KeyValue<String, ClassifierImage>> predictedLabelledImages = knnClassifier.predict(testImages, maxAccuracyKValue);

        // display accuracy of classifier-attributed labels
        System.out.println("Accuracy: " + Calculations.calculateAccuracy(labeledTestImages, predictedLabelledImages)*100+"%");
    }
}
