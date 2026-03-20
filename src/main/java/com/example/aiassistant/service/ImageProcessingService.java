package com.example.aiassistant.service;

import org.imgscalr.Scalr;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ImageProcessingService {

    private static final int TARGET_SIZE = 512;   // resize target in px
    private static final int JPEG_QUALITY = 75;   // compression quality

    static {
        nu.pattern.OpenCV.loadLocally();           // loads OpenCV native libs
    }

    // Main method: resize + detect + crop → returns base64 image
    public String processImage(MultipartFile file) throws IOException {
        System.out.println("Original size: " + file.getSize() / 1024 + " KB");

        // Step 1: Resize image to 512px max dimension
        BufferedImage original = ImageIO.read(file.getInputStream());
        BufferedImage resized = resizeImage(original);
        System.out.println("After resize: " + resized.getWidth() + "x" + resized.getHeight());

        // Step 2: Try to detect damage region with OpenCV
        byte[] resizedBytes = toJpegBytes(resized);
        byte[] cropped = detectAndCrop(resizedBytes);

        // Step 3: Convert final image to base64
        String base64 = Base64.getEncoder().encodeToString(cropped);
        System.out.println("Final image size: " + cropped.length / 1024 + " KB");
        return base64;
    }

    // Resize to max 512px while keeping aspect ratio
    private BufferedImage resizeImage(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= TARGET_SIZE && height <= TARGET_SIZE) {
            return original; // already small enough
        }

        return Scalr.resize(original, Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC, TARGET_SIZE, TARGET_SIZE);
    }

    // Convert BufferedImage to compressed JPEG bytes
    private byte[] toJpegBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    // Use OpenCV to find damage region and crop to it
    private byte[] detectAndCrop(byte[] imageBytes) {
        try {
            // Load image into OpenCV Mat
            Mat mat = Imgcodecs.imdecode(
                    new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR
            );

            if (mat.empty()) {
                System.out.println("OpenCV: could not read image, using full image");
                return imageBytes;
            }

            // Convert to grayscale + find edges
            Mat gray = new Mat();
            Mat edges = new Mat();
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.Canny(gray, edges, 50, 150);

            // Find contours (damaged areas have lots of edges)
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(edges, contours, hierarchy,
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            if (contours.isEmpty()) {
                System.out.println("OpenCV: no contours found, using full image");
                return imageBytes;
            }

            // Find the largest contour (most likely the damage area)
            double maxArea = 0;
            Rect bestRect = null;

            for (MatOfPoint contour : contours) {
                Rect rect = Imgproc.boundingRect(contour);
                double area = rect.area();
                if (area > maxArea && area > 500) { // ignore tiny contours
                    maxArea = area;
                    bestRect = rect;
                }
            }

            if (bestRect == null) {
                System.out.println("OpenCV: no significant region, using full image");
                return imageBytes;
            }

            // Add 20% padding around the detected region
            int padding = (int) (Math.max(bestRect.width, bestRect.height) * 0.2);
            int x = Math.max(0, bestRect.x - padding);
            int y = Math.max(0, bestRect.y - padding);
            int w = Math.min(mat.cols() - x, bestRect.width + 2 * padding);
            int h = Math.min(mat.rows() - y, bestRect.height + 2 * padding);

            Rect paddedRect = new Rect(x, y, w, h);
            Mat cropped = new Mat(mat, paddedRect);

            System.out.println("OpenCV: cropped to region " + w + "x" + h +
                    " (was " + mat.cols() + "x" + mat.rows() + ")");

            // Convert cropped Mat back to bytes
            MatOfByte result = new MatOfByte();
            Imgcodecs.imencode(".jpg", cropped, result);
            return result.toArray();

        } catch (Exception e) {
            System.out.println("OpenCV error: " + e.getMessage() + " — using full image");
            return imageBytes;
        }
    }
}