package ir.ac.kntu.entities.module;

import com.fasterxml.jackson.annotation.JsonTypeName;

import ir.ac.kntu.entities.enums.Format;
import ir.ac.kntu.entities.enums.ItemType;

@JsonTypeName("AUDIOBOOK")
public class AudioBook extends DigitalItem {
    private String duration;

    public AudioBook() {
        //this is for jackson
    }

    public AudioBook(String title, String author, String category, ItemType type, String publishYear,
            double fileSizeMB, Format format, String url, String duration) {
        super(title, author, category, type, publishYear, fileSizeMB, format, url);
        this.duration = duration;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return super.toString() +"AudioBook duration= " + duration;
    }
}
