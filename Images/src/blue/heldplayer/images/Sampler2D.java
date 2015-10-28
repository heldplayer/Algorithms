package blue.heldplayer.images;

import java.util.Arrays;

public class Sampler2D {

    private int[] data;
    public final int width, height;
    private boolean grayscale;

    public Sampler2D(int[] data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public Sampler2D(Sampler2D ref) {
        this.data = Arrays.copyOf(ref.data, ref.data.length);
        this.width = ref.width;
        this.height = ref.height;
    }

    public void setGrayscale() {
        this.grayscale = true;
    }

    public int getPixel(int x, int y) {
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        if (x >= width)
            x = width - 1;
        if (y >= height)
            y = height - 1;
        return data[x + y * width];
    }

    public int getColorPixel(int x, int y) {
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        if (x >= width)
            x = width - 1;
        if (y >= height)
            y = height - 1;
        if (this.grayscale) {
            int gray = data[x + y * width] & 0xFF;
            return gray << 16 | gray << 8 | gray;
        }
        return data[x + y * width];
    }

    public void setPixel(int x, int y, int value) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            return;
        if (this.grayscale) {
            int red = (value >> 16) & 0xFF;
            int green = (value >> 8) & 0xFF;
            int blue = value & 0xFF;
            data[x + y * width] = ((red + green + blue) / 3) & 0xFF;
        } else {
            data[x + y * width] = value;
        }
    }

    public void swap(Sampler2D other) {
        if (this.width != other.width || this.height != other.height)
            throw new IllegalArgumentException("Cannot swap samplers with different sizes");

        int[] temp = other.data;
        other.data = this.data;
        this.data = temp;
    }

    public Iterator iterator() {
        return new Iterator();
    }

    public class Iterator {

        public void iterate(Sampler2D source, Transform transform) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    setPixel(x, y, transform.transform(source, x, y));
                }
            }
        }
    }

    public interface Transform {

        int transform(Sampler2D source, int x, int y);
    }
}
