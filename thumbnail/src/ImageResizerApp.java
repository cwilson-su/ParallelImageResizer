package thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageResizerApp {

    public static void main(String[] args) {
    	
        // Hardcoded input and output folders
    	File inputFolder = new File("input_images");
        File outputFolder = new File("output_images");

        // Ensure the output directory exists
        if (!outputFolder.exists()) {
            if (!outputFolder.mkdirs()) {
                System.err.println("Failed to create the output folder.");
                return;
            }
        }

        // Find image files in the input folder
        File[] imageFiles = ImageUtils.findImageFiles(inputFolder);

        treatImages(outputFolder, imageFiles);

        System.out.println("Image resizing completed.");
    }

    /*
     * concurrent version with executor Service
     * */
    public static void treatImages(File outputFolder, File[] imageFiles) {
        // Process each image
        for (File file : imageFiles) {
            // Load the image
            BufferedImage originalImage = ImageUtils.loadImage(file);
            if (originalImage != null) {
                // Resize the image
                BufferedImage resizedImage = ImageUtils.resizeImage(originalImage);

                // Create the output file
                File outputFile = new File(outputFolder, file.getName());

                // Save the resized image
                ImageUtils.saveImage(resizedImage, outputFile);
            }
        }
    }
    
    /*
     * Version 2 (OK)
     */
    public static void treatImages2(File outputFolder, File[] imageFiles) {
        ExecutorService pool = Executors.newFixedThreadPool(4);

        // Process each image
        for (File file : imageFiles) {
            pool.submit(() -> {
                // Load the image inside the task
                BufferedImage originalImage = ImageUtils.loadImage(file);
                if (originalImage != null) {
                    // Resize the image
                    BufferedImage resizedImage = ImageUtils.resizeImage(originalImage);

                    // Create the output file
                    File outputFile = new File(outputFolder, file.getName());

                    // Save the resized image
                    ImageUtils.saveImage(resizedImage, outputFile);
                }
            });
        }

        // Shutdown the executor service and wait for tasks to complete
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Some tasks did not finish within the timeout.");
            }
        } catch (InterruptedException e) {
            System.err.println("Task interrupted: " + e.getMessage());
        }
    }
    
    /*
     * version 3 (OK)
     */
    public static void treatImages3(File outputFolder, File[] imageFiles) {
        /*
         *     The try-with-resources syntax makes it easier to ensure the executor shuts down properly 
         *     without explicitly calling shutdown() and awaitTermination() in the main code.
         *     This approach is cleaner and ensures the ExecutorService is properly shut down when treatImages completes.
         */
    	try (AutoCloseableExecutorService autoPool = new AutoCloseableExecutorService(4)) {
            ExecutorService pool = autoPool.getExecutorService();

            // Process each image concurrently
            for (File file : imageFiles) {
                pool.submit(() -> {
                    // Load the image inside the task
                    BufferedImage originalImage = ImageUtils.loadImage(file);
                    if (originalImage != null) {
                        // Resize the image
                        BufferedImage resizedImage = ImageUtils.resizeImage(originalImage);

                        // Create the output file
                        File outputFile = new File(outputFolder, file.getName());

                        // Save the resized image
                        ImageUtils.saveImage(resizedImage, outputFile);
                    }
                });
            }
        }
        // No need for explicit shutdown or awaitTermination here – the AutoCloseableExecutorService takes care of it
    }

    
}

