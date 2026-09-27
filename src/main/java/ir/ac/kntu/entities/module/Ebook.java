package ir.ac.kntu.entities.module;

import com.fasterxml.jackson.annotation.JsonTypeName;

import ir.ac.kntu.entities.enums.Format;
import ir.ac.kntu.entities.enums.ItemType;

@JsonTypeName("EBOOK")
public class Ebook extends DigitalItem {
    public Ebook() {
        //this is for jackson
    }
    public Ebook(String title, String author, String category, ItemType type, String publishYear,
            double fileSizeMB, Format format, String url) {
        super(title, author, category, type, publishYear, fileSizeMB, format, url);
    }
    @Override
    public String toString() {
        return super.toString() + " [Ebook]";
    }
}
