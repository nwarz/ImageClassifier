package main.config;

import java.util.ArrayList;
import java.util.List;

/**
    Configuration settings for the image classifier
 */
public class Config {

    /** Root file directory for the CIFAR-10 dataset */
    private static String CIFAR10_ROOT_PATH = "data/cifar-10-batches-bin/";

    /** Filepath for the list of class names for the CIFAR-10 dataset */
    public static String CLASS_NAMES_PATH = CIFAR10_ROOT_PATH + "batches.meta.txt";

    /** Filepaths for each batch of CIFAR-10 training images */
    public static List<String> TRAINING_IMAGES_PATHS = new ArrayList<>();
    static {
        TRAINING_IMAGES_PATHS.add(CIFAR10_ROOT_PATH + "data_batch_1.bin");
        TRAINING_IMAGES_PATHS.add(CIFAR10_ROOT_PATH + "data_batch_2.bin");
        TRAINING_IMAGES_PATHS.add(CIFAR10_ROOT_PATH + "data_batch_3.bin");
        TRAINING_IMAGES_PATHS.add(CIFAR10_ROOT_PATH + "data_batch_4.bin");
        TRAINING_IMAGES_PATHS.add(CIFAR10_ROOT_PATH + "data_batch_5.bin");
    }

    /** Filepath for the batch of CIFAR-10 test set images */
    public static String TEST_IMAGES_PATH = CIFAR10_ROOT_PATH + "test_batch.bin";


    /** Width (and height) of dataset images in pixels */
    public static int IMAGE_WIDTH = 32;

    /** Number of pixels in image for an individual RGB color */
    public static int PER_COLOR_FLAT_IMAGE_SIZE = IMAGE_WIDTH * IMAGE_WIDTH;

    /** Total number of single-color pixels in image, with each individual RGB color counted separately */
    public static int RGB_FLAT_IMAGE_SIZE = PER_COLOR_FLAT_IMAGE_SIZE * 3;
}
