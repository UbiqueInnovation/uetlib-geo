package ch.ubique.backend.geo.vectortile.models;

public class BinaryTile {

    private final String path;
    private final byte[] bytes;

    public BinaryTile(String path, byte[] bytes) {
        this.path = path;
        this.bytes = bytes;
    }

    public String getPath() {
        return path;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
