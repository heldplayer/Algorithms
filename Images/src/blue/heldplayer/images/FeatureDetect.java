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
        int[] data = dataBuffer.getData();
        Sampler2D outputBuffer = new Sampler2D(new int[data.length], width, height);
        Sampler2D inputBuffer = new Sampler2D(data, width, height);
        transformBuffer(inputBuffer, outputBuffer);
        JFrame frame = new JFrame(image.toString());
        JPanel canvas = new JPanel() {
            @Override
            public void paint(Graphics g) {
                for (int x = 0; x < this.getWidth(); x++) {
                    for (int y = 0; y < this.getHeight(); y++) {
                        g.setColor(new Color(outputBuffer.getPixel(x, y)));
                        g.fillRect(x, y, 1, 1);
                    }
                }
            }
        };
        canvas.setPreferredSize(new Dimension(width, height));
        frame.add(canvas);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private static void transformBuffer(Sampler2D in, Sampler2D out) {
        out.iterator().iterate(in, FeatureDetect::transformPixel);
    }

    public static int transformPixel(Sampler2D in, int x, int y) {
        int r = 2;
        int red = 0;
        int green = 0;
        int blue = 0;
        int count = 0;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                int pixel = in.getPixel(x + dx, y + dy);
                red += (pixel >> 16) & 0xFF;
                green += (pixel >> 8) & 0xFF;
                blue += pixel & 0xFF;
                count++;
            }
        }
        red = (red / count) & 0xFF;
        green = (green / count) & 0xFF;
        blue = (blue / count) & 0xFF;
        return (red << 16) | (green << 8) | blue;
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
