package blue.heldplayer.images;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.*;
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
        BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        ColorConvertOp converter = new ColorConvertOp(null);
        converter.filter(image, rgb);
        rgb = transformImage(rgb);
        //DataBufferInt dataBuffer = (DataBufferInt) rgb.getData().getDataBuffer();
        //int[] data = dataBuffer.getData();
        //Sampler2D inputBuffer = new Sampler2D(data, width, height);
        //Sampler2D outputBuffer = new Sampler2D(new int[data.length], width, height);
        //transformBuffer(inputBuffer, outputBuffer);
        final BufferedImage draw = rgb;
        JFrame frame = new JFrame(image.toString());
        JPanel canvas = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.drawImage(draw, 0, 0, getWidth(), getHeight(), null);
                /*
                for (int x = 0; x < this.getWidth(); x++) {
                    for (int y = 0; y < this.getHeight(); y++) {
                        g.setColor(new Color(outputBuffer.getColorPixel(x, y)));
                        g.fillRect(x, y, 1, 1);
                    }
                }
                */
            }
        };
        canvas.setPreferredSize(new Dimension(width, height));
        frame.add(canvas);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private static BufferedImage transformImage(BufferedImage src) {
        BufferedImage result = new ConvolveOp(new Kernel(3, 3, new float[] { 1F / 9F, 1F / 9F, 1F / 9F, 1F / 9F, 1F / 9F, 1F / 9F, 1F / 9F, 1F / 9F, 1F / 9F }), ConvolveOp.EDGE_NO_OP, null).filter(src, null);
        result = new ConvolveOp(new Kernel(3, 3, new float[] { -1, -1, -1, -1, 8, -1, -1, -1, -1 })).filter(result, null);
        return result;
    }

    private static void transformBuffer(Sampler2D in, Sampler2D out) {
        Sampler2D interim = new Sampler2D(out);
        interim.setGrayscale();
        //interim.iterator().iterate(in, FeatureDetect::blurGaussian);
        out.iterator().iterate(in, Sampler2D::getColorPixel);
        //out.iterator().iterate(interim, FeatureDetect::blurHorizontal);
        //out.swap(interim);
        //out.iterator().iterate(in, FeatureDetect::kernelPixel);
    }

    public static int kernelPixel(Sampler2D in, int x, int y) {
        return kernel(in, x, y, new int[][] { { -1, -1, -1 }, { -1, 8, -1 }, { -1, -1, -1 } }, 16);
    }

    public static int blurHorizontal(Sampler2D in, int x, int y) {
        int r = 3;
        int red = 0;
        int green = 0;
        int blue = 0;
        int count = 0;
        for (int dx = -r; dx <= r; dx++) {
            int pixel = in.getColorPixel(x + dx, y);
            red += (pixel >> 16) & 0xFF;
            green += (pixel >> 8) & 0xFF;
            blue += pixel & 0xFF;
            count++;
        }
        red = (red / count) & 0xFF;
        green = (green / count) & 0xFF;
        blue = (blue / count) & 0xFF;
        return (red << 16) | (green << 8) | blue;
    }

    public static int blurVertical(Sampler2D in, int x, int y) {
        int r = 3;
        int red = 0;
        int green = 0;
        int blue = 0;
        int count = 0;
        for (int dy = -r; dy <= r; dy++) {
            int pixel = in.getColorPixel(x, y + dy);
            red += (pixel >> 16) & 0xFF;
            green += (pixel >> 8) & 0xFF;
            blue += pixel & 0xFF;
            count++;
        }
        red = (red / count) & 0xFF;
        green = (green / count) & 0xFF;
        blue = (blue / count) & 0xFF;
        return (red << 16) | (green << 8) | blue;
    }

    public static int blur(Sampler2D in, int x, int y) {
        int r = 3;
        int red = 0;
        int green = 0;
        int blue = 0;
        int count = 0;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                int pixel = in.getColorPixel(x + dx, y + dy);
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

    private static int kernel(Sampler2D in, int x, int y, int[][] kernel, int factor) {
        int offsetY = kernel.length / 2;
        int offsetX = kernel[0].length / 2;
        int red = 0, green = 0, blue = 0;
        int sum = 0;
        for (int row = 0; row < kernel.length; row++) {
            for (int column = 0; column < kernel[row].length; column++) {
                int pixel = in.getColorPixel(x - offsetX + column, y - offsetY + row);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                int val = kernel[row][column];
                red += val * r / factor;
                green += val * g / factor;
                blue += val * b / factor;
                sum += val;
            }
        }
        //red /= factor;
        //green /= factor;
        //blue /= factor;
        return new SimpleColor(red, green, blue).getFull();
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
