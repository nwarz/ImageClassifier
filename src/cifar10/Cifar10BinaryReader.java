package cifar10;

import data.ClassifierImage;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import config.Config;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;

/**
 * Reads in CIFAR-10 dataset images from binary format
 *
 * CIFAR-10 binary images are structured, without delimiters, as follows:
 * 1 byte label, 1024 bytes red pixels, 1024 bytes green pixels, 1024 bytes blue pixels
 *
 * Subsequent images have no delimiters between them
 */
public class Cifar10BinaryReader {

    /**
     *  Reads in labeled CIFAR-10 images from the given binary image batch
     *
     * @param imagesPath filepath to a batch of binary images in the CIFAR-10 dataset
     * @return labeled CIFAR-10 images
     * @throws IOException
     */
    public static List<KeyValue<String, ClassifierImage>> readCifar10Dataset(String imagesPath) throws IOException {
        return readClassifiedImages(imagesPath, readClassNames(Config.CLASS_NAMES_PATH));
    }

    /**
     *  Reads the list of class names from the CIFAR-10 dataset
     *
     * @param path filepath to the list of class names in the CIFAR-10 dataset
     * @return map from numerical values per label from CIFAR-10 binary data to string name of label
     * @throws IOException
     */
    private static Map<Integer,String> readClassNames(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), Charset.availableCharsets().get("US-ASCII")));
        Map<Integer,String> classNames = new HashMap<>();
        for(int i=0; reader.ready(); i++) {
            classNames.put(i, reader.readLine());
        }

        return classNames;
    }

    /**
     *  Reads in a batch of binary CIFAR-10 images and outputs labeled images in digestible format
     *
     * @param path filepath to a batch of CIFAR-10 images
     * @param classNames map from CIFAR-10 binary class value to class name
     * @return list containing labeled images from the CIFAR-10 set
     * @throws IOException
     */
    private static List<KeyValue<String,ClassifierImage>> readClassifiedImages(String path, Map<Integer,String> classNames) throws IOException {

        List<KeyValue<String,ClassifierImage>> classifiedImages = new ArrayList<>();
        FileInputStream binaryDataStream = new FileInputStream(path);

        int labelSize = 1;
        int imageSize = Config.RGB_FLAT_IMAGE_SIZE;
        byte[] imageBuffer = new byte[imageSize];
        int imagesRead = 0;

        assert binaryDataStream.available() > 0;

        // read each binary image in the file
        while (binaryDataStream.available() >= labelSize + imageSize) {
            int label = binaryDataStream.read();

            int imageBytesRead = binaryDataStream.read(imageBuffer);
            if (imageBytesRead != imageSize) {
                throw new IOException("expected image to contain " + imageSize + "bytes, " + "actually read " + imageBytesRead);
            }

            byte[] reds = Arrays.copyOfRange(imageBuffer, 0, Config.ONE_COLOR_FLAT_IMAGE_SIZE);
            byte[] greens = Arrays.copyOfRange(imageBuffer, Config.ONE_COLOR_FLAT_IMAGE_SIZE, 2*Config.ONE_COLOR_FLAT_IMAGE_SIZE);
            byte[] blues = Arrays.copyOfRange(imageBuffer, 2*Config.ONE_COLOR_FLAT_IMAGE_SIZE, 3*Config.ONE_COLOR_FLAT_IMAGE_SIZE);

            assert imageBuffer.length == Config.RGB_FLAT_IMAGE_SIZE;
            assert imageBuffer[Config.RGB_FLAT_IMAGE_SIZE -1] != 0;

            int i = 0;
            int[][][] rawImage = new int[Config.IMAGE_WIDTH][Config.IMAGE_WIDTH][3];
            for(int y = 0; y < Config.IMAGE_WIDTH; y++) {
                for (int x = 0; x < Config.IMAGE_WIDTH; x++) {
                    rawImage[x][y][0] = reds[i];
                    rawImage[x][y][1] = greens[i];
                    rawImage[x][y][2] = blues[i];
                    i++;
                }
            }
            ClassifierImage image = new ClassifierImage(rawImage);
            classifiedImages.add(new DefaultKeyValue<>(classNames.get(label), image));
            ++imagesRead;
        }

        System.out.println(imagesRead + " images read");
        return classifiedImages;
    }

    /**
     * Load in and aggregate all the labeled training images from the CIFAR-10 dataset
     *
     * @return
     * @throws IOException
     */
    public static List<KeyValue<String,ClassifierImage>> loadTrainingData() throws IOException {
        System.out.println("Loading training images:");

        List<KeyValue<String,ClassifierImage>> trainingImages = new ArrayList<>();
        for(String trainingImagesPath : Config.TRAINING_IMAGES_PATHS) {
            trainingImages.addAll(readCifar10Dataset(trainingImagesPath));
        }

        return trainingImages;
    }

    /**
     * Load in labeled test images from the CIFAR-10 dataset
     *
     * @return
     * @throws IOException
     */
    public static List<KeyValue<String, ClassifierImage>> loadTestData() throws IOException {
        System.out.println("Loading test images:");

        return readCifar10Dataset(Config.TEST_IMAGES_PATH);
    }
}
