package thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageResizerPipelineApp {

    public static void main(String[] args) {
        // Paths for input and output folders
    	File inputFolder = new File("input_images");
        File outputFolder = new File("output_images");
        
    	
        //Ensure the output directory exists
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            System.err.println("Failed to create the output folder.");
            return;
        }

        // Set up the pipeline queues
        BlockingQueue<File> loadQueue = new ArrayBlockingQueue<>(10);
        BlockingQueue<ImageTask> resizeQueue = new ArrayBlockingQueue<>(10);

        // Set up the thread pool
        ExecutorService executor = Executors.newFixedThreadPool(4);

        //Start pipeline stages
        executor.submit(() -> loadImages(inputFolder, loadQueue));
        executor.submit(() -> resizeImages(loadQueue, resizeQueue));
        executor.submit(() -> saveImages(resizeQueue, outputFolder));

        // Shut down the executor +wait for completion
        executor.shutdown();
        try {
            if (executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("Image resizing completed.");
            } else {
                System.err.println("Timeout reached before all tasks could complete.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execution interrupted: " + e.getMessage());
        }
    }

    private static void loadImages(File inputFolder, BlockingQueue<File> queue) {
        File[] imageFiles = ImageUtils.findImageFiles(inputFolder);
        for (File file : imageFiles) {
            try {
                queue.put(file); // Add file to the queue
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Image loading interrupted.");
            }
        }
        // Signal that no more files will be added
        try {
            queue.put(new File("EOF"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void resizeImages(BlockingQueue<File> inputQueue, BlockingQueue<ImageTask> outputQueue) {
        try {
            while (true) {
                File file = inputQueue.take();
                if ("EOF".equals(file.getName())) {
                    // Signal downstream stages
                    outputQueue.put(new ImageTask(null, null));
                    break;
                }
                BufferedImage originalImage = ImageUtils.loadImage(file);
                if (originalImage != null) {
                    BufferedImage resizedImage = ImageUtils.resizeImage(originalImage);
                    outputQueue.put(new ImageTask(resizedImage, file.getName()));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Image resizing interrupted.");
        }
    }

    private static void saveImages(BlockingQueue<ImageTask> queue, File outputFolder) {
        try {
            while (true) {
                ImageTask task = queue.take();
                if (task.image == null) { // EOF signal
                    break;
                }
                File outputFile = new File(outputFolder, task.fileName);
                ImageUtils.saveImage(task.image, outputFile);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Image saving interrupted.");
        }
    }

    // Helper class to hold resized image and file name
    private static class ImageTask {
        BufferedImage image;
        String fileName;

        public ImageTask(BufferedImage image, String fileName) {
            this.image = image;
            this.fileName = fileName;
        }
    }
}
