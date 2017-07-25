package main.gui;

import main.data.ClassifierImage;
import main.Logger;

import org.apache.commons.collections4.KeyValue;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.Hashtable;
import java.util.List;

/**
 * Swing GUI frame that displays images from the dataset with predicted and actual classifications
 */
class PredictedResultsView {

    private List<KeyValue<String, ClassifierImage>> predictedImages;
    private List<KeyValue<String, ClassifierImage>> actualImages;


    private JFrame rootFrame;
    private JComponent mainPanel;

    private JComponent navigationPanel;
    private JButton prevPageButton;
    private JButton nextPageButton;

    private int currentPage = 0;


    private final int CARD_ROWS = 3;
    private final int CARD_COLS = 4;
    private final int PAGE_SIZE = CARD_ROWS * CARD_COLS;

    private static final int IMAGE_SCALE_FACTOR = 3;


    PredictedResultsView(List<KeyValue<String, ClassifierImage>> predictedImages,
                                List<KeyValue<String, ClassifierImage>> actualImages) {
        this.predictedImages = predictedImages;
        this.actualImages = actualImages;
        init();
    }


    private void init() {
        rootFrame = new JFrame("Predicted classifications");
        rootFrame.setResizable(false);

        mainPanel = Box.createVerticalBox();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        rootFrame.add(mainPanel);

        navigationPanel = initAndCreateNavigationPanel();
        buildFrame();
    }

    private JComponent initAndCreateNavigationPanel() {
        JComponent panel = Box.createHorizontalBox();

        prevPageButton = new JButton("prev");
        prevPageButton.addActionListener(e -> showPrevPage());
        nextPageButton = new JButton("next");
        nextPageButton.addActionListener(e -> showNextPage());

        panel.add(Box.createGlue());
        panel.add(prevPageButton);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(nextPageButton);
        panel.add(Box.createGlue());

        updateNavButtons();
        return panel;
    }

    private JComponent createDisplayPanel(int page) {
        JPanel displayPanel = new JPanel(new GridLayout(CARD_ROWS, CARD_COLS, 10, 10));
        displayPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

        for (int i = page*PAGE_SIZE; i < (page+1)*PAGE_SIZE; i++) {
            if (i < predictedImages.size()-1) {
                displayPanel.add(createResultPane(i));
            }
        }
        return displayPanel;
    }

    private JComponent createResultPane(int imageIndex) {
        KeyValue<String, ClassifierImage> predictedImage = predictedImages.get(imageIndex);
        KeyValue<String, ClassifierImage> actualImage = actualImages.get(imageIndex);

        Box topBox = Box.createVerticalBox();
        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(10,10,10,10));
        topBox.setBorder(border);

        Image scaledImage = scaleImage(toBufferedImage(predictedImage.getValue()),
                32*IMAGE_SCALE_FACTOR, 32*IMAGE_SCALE_FACTOR);
        ImageIcon imageIcon = new ImageIcon(scaledImage);
        JLabel imageLabel = new JLabel(imageIcon);

        String predictedClass = predictedImage.getKey();
        String actualClass = actualImage.getKey();
        JLabel predictedLabel = new JLabel("predicted: " + predictedClass);
        JLabel actualLabel = new JLabel("actual: " + actualClass);

        topBox.add(imageLabel);
        topBox.add(predictedLabel);
        topBox.add(actualLabel);
        return topBox;
    }

    private void buildFrame() {
        mainPanel.removeAll();
        mainPanel.add(createDisplayPanel(currentPage));
        mainPanel.add(navigationPanel);
        rootFrame.pack();
    }

    void show() {
        rootFrame.setVisible(true);
    }


    private void showPrevPage() {
        if(currentPage > 0) {
            currentPage--;
        }
        updateNavButtons();
        buildFrame();
    }

    private void showNextPage() {
        if(!isOnLastPage()) {
            currentPage++;
        }
        updateNavButtons();
        buildFrame();
    }


    private void updateNavButtons() {
        if (currentPage < 0) {
            Logger.log("ERROR - negative currentPage value: " + currentPage);
            currentPage = 0;
        }

        boolean onFirstPage = (currentPage == 0);
        prevPageButton.setEnabled(!onFirstPage);

        nextPageButton.setEnabled(!isOnLastPage());
    }

    private boolean isOnLastPage() {
        if (currentPage > predictedImages.size() / PAGE_SIZE) {
            Logger.log("ERROR - currentPage {" + currentPage + "} out of bounds- " +
                    "last valid page {" + predictedImages.size() / PAGE_SIZE + "}");
        }
        return (currentPage == predictedImages.size() / PAGE_SIZE);
    }


    private static Image toBufferedImage(ClassifierImage classifierImage) {
        ColorModel colorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                false,
                false,
                ColorModel.OPAQUE,
                DataBuffer.TYPE_BYTE);
        byte[] flatImage = classifierImage.toFlatImage();
        DataBuffer dataBuffer = new DataBufferByte(flatImage, flatImage.length);
        int[] bandOffsets = {0, 1, 2};
        WritableRaster raster = Raster.createInterleavedRaster(
                dataBuffer,
                classifierImage.getWidth(),
                classifierImage.getHeight(),
                3 * classifierImage.getWidth(),
                3,
                bandOffsets,
                new Point(0, 0));

        return new BufferedImage(colorModel, raster, true, new Hashtable<>());
    }

    private static Image scaleImage(Image sourceImage, int w, int h) {
        BufferedImage scaledImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = scaledImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // scale image and rotate 90deg clockwise
        AffineTransform transform
                = AffineTransform.getScaleInstance(IMAGE_SCALE_FACTOR, IMAGE_SCALE_FACTOR);
        transform.quadrantRotate(1, 16,16);
        g2.drawImage(sourceImage, transform, null);
        g2.dispose();
        return scaledImage;
    }
}