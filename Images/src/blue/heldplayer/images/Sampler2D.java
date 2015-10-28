package blue.heldplayer.images;

public class Sampler2D {

    private int[] data;
    public final int width, height;

    public Sampler2D(int[] data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
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

    public void setPixel(int x, int y, int value) {
        if (x < 0 || y < 0 || x >= width || y >= height)
            return;
        data[x + y * width] = value;
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
