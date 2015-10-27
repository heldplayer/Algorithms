package blue.heldplayer.images;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class FeatureDetect {

    public static void main(String[] args) throws IOException {
        Stream<Path> inputs = Files.list(Paths.get(".", "input"));

        inputs.parallel().map(path -> {
            try {
                return ImageIO.read(path.toFile());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(image -> image != null).forEach(FeatureDetect::processImage);
    }

    public static void processImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ColorConvertOp converter = new ColorConvertOp(null);
        converter.filter(image, rgb);
        DataBufferInt dataBuffer = (DataBufferInt) rgb.getData().getDataBuffer();
        JFrame frame = new JFrame(image.toString());
        JPanel canvas = new JPanel() {
            @Override
            public void paint(Graphics g) {
                int[] index = { 0, 0 };
                int[] data = dataBuffer.getData();
                Arrays.stream(data).map(operand -> transformPixel(data, width, height, index[0] % width, index[0]++ / width)).forEach(value -> {
                    g.setColor(new Color(value));
                    g.fillRect(index[1] % width, index[1]++ / width, 1, 1);
                });
            }
        };
        canvas.setPreferredSize(new Dimension(width, height));
        frame.add(canvas);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    public static int transformPixel(int[] imageData, int width, int height, int x, int y) {
        int r = 5;
        //for (int dx = -r; dx <= r; dx++) {
        //for (int dy = -r; dy <= r; dy++) {
        //}
        //}
        int pixel = getPixel(imageData, width, height, x, y);
        int average = (((pixel >> 16) & 0xFF) + ((pixel >> 8) & 0xFF) + (pixel & 0xFF)) / 3;
        int red = threshold(pixel, 0x40, 0xFF, 16);
        int green = threshold(pixel, 0x40, 0xFF, 8);
        int blue = threshold(pixel, 0x40, 0xFF, 0);
        return red | green | blue;
    }

    private static int threshold(int val, int threshold, int out, int shift) {
        return ((val >> shift) & 0xFF) > threshold ? out << shift : 0;
    }

    private static int kernel(int[] imageData, int width, int height, int x, int y, int[][] kernel) {
        return 0;
    }

    private static int getPixel(int[] imageData, int width, int height, int x, int y) {
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        if (x >= width)
            x = width - 1;
        if (y >= height)
            y = height - 1;
        return imageData[x + y * width];
    }
}
