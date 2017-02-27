package data;

import java.util.Arrays;

/**
 * Represents an image digestible by a classifier
 */
public class ClassifierImage {
    /**
     * Bitmap image array in format [x][y][rgbColor]
     */
    int[][][] image;

    public ClassifierImage(int[][][] image) {
        this.image = image;
    }

    public int[] getPixelAt(int x, int y) {
        return image[x][y];
    }

    public int getWidth() {
        return image.length;
    }

    public int getHeight() {
        return image[0].length;
    }

    private int getTotalSize() {
        if(image==null) {
            return 0;
        }
        return (image.length)*(image[0].length)*(image[0][0].length);
    }

    /**
     * @return the image as a linear array
     */
    public int[] toFlatImage() {
        int[] flattenedImage = new int[getTotalSize()];
        int i = 0;
        for(int[][] row : image) {
            for(int[] pixel : row) {
                for(int colorValue : pixel) {
                    flattenedImage[i++] = colorValue;
                }
            }
        }
        return flattenedImage;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof ClassifierImage)) {
            return false;
        }
        return Arrays.equals(image, ((ClassifierImage)other).image);
    }
}
