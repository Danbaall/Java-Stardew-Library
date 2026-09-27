package ir.ac.kntu.entities.module;

import ir.ac.kntu.entities.enums.Format;
import ir.ac.kntu.entities.enums.ItemType;

public class DigitalItem extends Item {

    private double fileSizeMB;
    private Format format;
    private String url;

    public DigitalItem() {
        // Jackson
    }

    public DigitalItem(String title, String author, String category,
            ItemType type, String publishYear,
            double fileSizeMB, Format format, String url) {
        super(title, author, category, type, publishYear);
        this.fileSizeMB = fileSizeMB;
        this.format = format;
        this.url = url;
    }

    public double getFileSizeMB() {
        return fileSizeMB;
    }

    public void setFileSizeMB(double fileSizeMB) {
        this.fileSizeMB = fileSizeMB;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return super.toString()
                + " [fileSizeMB=" + fileSizeMB + ", format=" + format + ", url=" + url + "]";
    }
}