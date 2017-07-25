package main.gui;

public class RunGUIClassifier {
    public static void main(String[] args) {
        launchKNNClassifierView();
    }

    private static void launchKNNClassifierView() {
        KNNClassifierView knnClassifierView = new KNNClassifierView();
        knnClassifierView.show();
    }
}