package thumbnail;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtils {

    public static BufferedImage resizeImage(BufferedImage originalImage) {
        int newWidth = originalImage.getWidth() / 2;
        int newHeight = originalImage.getHeight() / 2;
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return resizedImage;
    }

    public static File[] findImageFiles(File inputFolder) {
        // Get the list of image files from the input folder
        File[] imageFiles = inputFolder.listFiles((dir, name) -> {
            String lowercaseName = name.toLowerCase();
            // Use ImageIO to check supported formats
            String[] formats = ImageIO.getReaderFileSuffixes();
            for (String format : formats) {
                if (lowercaseName.endsWith("." + format.toLowerCase())) {
                    return true;
                }
            }
            return false;
        });

        // Check if any image files were found
        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No image files found in the input folder.");
            return new File[0];
        }
        return imageFiles;
    }

    public static BufferedImage loadImage(File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            // Handle files that cannot be read as images
            System.err.println("Could not read image: " + file.getAbsolutePath());
        }
        return image;
    }

    public static void saveImage(BufferedImage image, File file) {
        try {
            ImageIO.write(image, "jpg", file);
        } catch (IOException e) {
            // Handle files that cannot be written as images
            System.err.println("Could not write image: " + file.getAbsolutePath());
        }
    }
}

