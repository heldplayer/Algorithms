package blue.heldplayer.images;

public class SimpleColor {

    private int red, green, blue, full;

    public SimpleColor(int red, int green, int blue) {
        this.red = red & 0xFF;
        this.green = green & 0xFF;
        this.blue = blue & 0xFF;
        this.calculateFull();
    }

    public SimpleColor(int full) {
        this.full = full & 0xFFFFFF;
        this.calculateParts();
    }

    private void calculateFull() {
        this.full = (this.red << 16) | (this.green << 8) | this.blue;
    }

    private void calculateParts() {
        this.red = (this.full >> 16) & 0xFF;
        this.green = (this.full >> 8) & 0xFF;
        this.blue = this.full & 0xFF;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getFull() {
        return full;
    }

    public void add(int red, int green, int blue) {
        this.red = Math.max(0, Math.min(this.red + red, 0xFF));
        this.green = Math.max(0, Math.min(this.green + green, 0xFF));
        this.blue = Math.max(0, Math.min(this.blue + blue, 0xFF));
        this.calculateFull();
    }

    public void add(int full) {
        this.add((full >> 16) & 0xFF, (full >> 8) & 0xFF, full & 0xFF);
    }

    public void subtract(int red, int green, int blue) {
        this.red = Math.max(0, Math.min(this.red - red, 0xFF));
        this.green = Math.max(0, Math.min(this.green - green, 0xFF));
        this.blue = Math.max(0, Math.min(this.blue - blue, 0xFF));
        this.calculateFull();
    }

    public void subtract(int full) {
        this.subtract((full >> 16) & 0xFF, (full >> 8) & 0xFF, full & 0xFF);
    }
}
