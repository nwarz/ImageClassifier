package main.data;

import java.util.Arrays;

/**
 * Represents an image digestible by a classifier
 */
public class ClassifierImage {
    /**
     * Bitmap image array in format [x][y][rgbColor]
     */
    private byte[][][] image;

    public ClassifierImage(byte[][][] image) {
        this.image = image;
    }

    public byte[] getPixelAt(int x, int y) {
        return image[x][y];
    }

    public int getWidth() {
        return image.length;
    }

    public int getHeight() {
        return image[0].length;
    }

    private int getTotalSize() {
        if(image == null) {
            return 0;
        }
        return (image.length)*(image[0].length)*(image[0][0].length);
    }

    /**
     * @return the image as a linear array
     */
    public byte[] toFlatImage() {
        byte[] flattenedImage = new byte[getTotalSize()];
        int i = 0;
        for(byte[][] row : image) {
            for(byte[] pixel : row) {
                for(byte colorValue : pixel) {
                    flattenedImage[i] = colorValue;
                    ++i;
                }
            }
        }
        return flattenedImage;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ClassifierImage)
                && (Arrays.equals(image, ((ClassifierImage) other).image));
    }
}
