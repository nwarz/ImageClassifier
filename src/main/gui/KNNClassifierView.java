package main.gui;

import main.Logger;
import main.cifar10.Cifar10BinaryReader;
import main.classifier.Classifiers;
import main.classifier.KNNClassifier;
import main.data.ClassifierImage;
import org.apache.commons.collections4.KeyValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class KNNClassifierView {

    private KNNClassifier knnClassifier;


    private JFrame rootFrame;

    private JSpinner numFoldsSpinner;

    private JRadioButton autoKValueButton;
    private JRadioButton setKValueButton;
    private JSpinner kValueSpinner;

    private JScrollPane statusScrollPane;
    private JScrollBar statusScrollBar;
    private JTextArea statusArea;

    private JButton startButton;


    KNNClassifierView() {
        init();
        Logger.setToSwingLogger(this);
    }

    private void init() {
        rootFrame = new JFrame("Image Classifier");
        rootFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        rootFrame.setResizable(false);

        JComponent mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        Box titleBox = Box.createHorizontalBox();
        titleBox.add(new JLabel("K-nearest-neighbor classifier"));
        titleBox.add(Box.createGlue());
        mainPanel.add(titleBox);
        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

        Box numFoldsSelectionBox = initAndCreateNumFoldsSelectionBox();
        mainPanel.add(numFoldsSelectionBox);

        Box kValueSelectorBox = initAndCreateKValueSelectorBox();
        mainPanel.add(kValueSelectorBox);
        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

        initStatusArea();
        mainPanel.add(statusScrollPane);
        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

        Box startButtonBox = initAndCreateStartButtonBox();
        mainPanel.add(startButtonBox);

        rootFrame.add(mainPanel);
        rootFrame.pack();
    }

    private Box initAndCreateNumFoldsSelectionBox() {
        Box numFoldsSelectionBox = Box.createHorizontalBox();
        numFoldsSelectionBox.add(new JLabel("set aside 1 in "));
        numFoldsSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 1000, 1));
        numFoldsSpinner.setMaximumSize(new Dimension(20, 50));
        numFoldsSelectionBox.add(numFoldsSpinner);
        numFoldsSelectionBox.add(new JLabel(" training images for validation"));
        numFoldsSelectionBox.add(Box.createGlue());
        return numFoldsSelectionBox;
    }

    private Box initAndCreateStartButtonBox() {
        Box startButtonBox = Box.createHorizontalBox();
        startButtonBox.add(Box.createGlue());
        startButton = new JButton("Start");
        startButton.addActionListener((ActionEvent e) -> new ClassifierTrainer().execute());
        startButtonBox.add(startButton);
        startButtonBox.add(Box.createGlue());
        return startButtonBox;
    }

    private void initStatusArea() {
        statusArea = new JTextArea("ready\n", 10, 40);
        statusArea.setLineWrap(true);
        statusArea.setEditable(false);
        statusScrollPane = new JScrollPane(
                statusArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        statusScrollBar = statusScrollPane.getVerticalScrollBar();
    }

    private Box initAndCreateKValueSelectorBox() {
        Box kValueSelectorBox = Box.createHorizontalBox();

        kValueSelectorBox.add(new JLabel("K-value:"));

        autoKValueButton = new JRadioButton("auto", true);
        kValueSelectorBox.add(autoKValueButton);

        setKValueButton = new JRadioButton("set value: ");
        setKValueButton.addChangeListener(e -> kValueSpinner.setEnabled(setKValueButton.isSelected()));
        setKValueButton.setSelected(false);
        kValueSelectorBox.add(setKValueButton);

        kValueSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 25, 1));
        kValueSpinner.setMaximumSize(new Dimension(20, 50));
        kValueSpinner.setEnabled(false);
        kValueSelectorBox.add(kValueSpinner);
        kValueSelectorBox.add(Box.createGlue());

        ButtonGroup kValueSelectorGroup = new ButtonGroup();
        kValueSelectorGroup.add(autoKValueButton);
        kValueSelectorGroup.add(setKValueButton);

        return kValueSelectorBox;
    }


    void show() {
        rootFrame.setVisible(true);
    }


    /**
     * Trains the k-nearest neighbor classifier and shows the results
     */
    private void startTraining() {
        try {
            startButton.setEnabled(false);

            // train classifier
            Logger.log("loading training images...");
            List<KeyValue<String, ClassifierImage>> labeledTrainingImages = Cifar10BinaryReader.loadTrainingData();
            Logger.log("loaded " + labeledTrainingImages.size() + " training images");

            Logger.log("loading test images...");
            List<KeyValue<String, ClassifierImage>> labeledTestImages = Cifar10BinaryReader.loadTestData();
            Logger.log("loaded " + labeledTestImages.size() + " test images");


            int numFolds = (int) numFoldsSpinner.getValue();
            List<KeyValue<String, ClassifierImage>> labeledValidationImages
                    = Classifiers.extractValidationSet(labeledTrainingImages, numFolds);
            Logger.log("extracted " + labeledValidationImages.size() + " validation images "
                    + "(1/" + numFolds + " of training set)");


            Logger.log("training k-nearest-neighbor classifier on "
                    + labeledTrainingImages.size() + " images...");
            knnClassifier = new KNNClassifier();
            knnClassifier.train(labeledTrainingImages);
            Logger.log("trained k-nearest-neighbor classifier");

            // pick k-value - use preselected value if set, otherwise auto-select
            int kValue = selectKValue(labeledValidationImages);


            List<ClassifierImage> unlabeledTestImages
                    = labeledTestImages.stream().map(KeyValue::getValue).collect(Collectors.toList());
            Logger.log("classifying " + unlabeledTestImages.size()
                    + " test images using " + kValue + "-nearest-neighbor...");

            // predict labels
            List<KeyValue<String, ClassifierImage>> predictedLabelTestImages
                    = knnClassifier.predict(unlabeledTestImages, kValue);


            Logger.log("done");
            double accuracy = Classifiers.calculateAccuracy(labeledTestImages, predictedLabelTestImages);
            String accuracyPercentage = String.format("%.2f", accuracy*100);
            Logger.log(accuracyPercentage + "% classification accuracy");


            // show labelled image results window
            PredictedResultsView resultsView
                    = new PredictedResultsView(predictedLabelTestImages, labeledTestImages);
            resultsView.show();

        } catch (IOException e) {
            Logger.log("ERROR - IOException while training");
            Logger.log(e.getMessage());
            e.printStackTrace(System.err);
        } catch (Exception e) {
            Logger.log("ERROR - Exception while training");
            Logger.log(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /**
     * Selects the k-value parameter for k-nearest-neighbor classification.
     * Uses set value if that option is selected, otherwise automatically selects a k-value based on
     * accuracy on the validation set.
     * @param labeledValidationImages
     * @return
     */
    private int selectKValue(List<KeyValue<String, ClassifierImage>> labeledValidationImages) {
        if(setKValueButton.isSelected()) {
            Logger.log("using set k-value");
            return (int) kValueSpinner.getValue();
        } else {
            Logger.log("automatically selecting k-value from highest accuracy candidate...");
            return KNNClassifier.findMaxAccuracyKValue(labeledValidationImages, knnClassifier);
        }
    }

    /**
     * Logs a message to the classifier GUI status area
     * @param message
     */
    public void writeToStatusArea(String message) {
        statusArea.append(message + "\n");
        statusArea.repaint();
        statusScrollBar.setValue(statusScrollBar.getMaximum() - statusScrollBar.getVisibleAmount());
    }


    /**
     * Runs the k-nearest-neighbor classifier in a separate thread
     */
    class ClassifierTrainer extends SwingWorker<Object, Integer> {
        @Override
        protected Object doInBackground() throws Exception {
            startTraining();
            // no return value necessary
            return null;
        }
    }
}