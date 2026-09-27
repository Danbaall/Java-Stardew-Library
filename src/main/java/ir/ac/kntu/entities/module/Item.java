package ir.ac.kntu.entities.module;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ir.ac.kntu.entities.enums.ItemType;
import ir.ac.kntu.util.IdGenerator;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = Book.class, visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Book.class, name = "BOOK"),
        @JsonSubTypes.Type(value = Magazine.class, name = "MAGAZINE"),
        @JsonSubTypes.Type(value = Ebook.class, name = "EBOOK"),
        @JsonSubTypes.Type(value = AudioBook.class, name = "AUDIOBOOK")
})
public class Item {

    private String id;
    private String title;
    private String author;
    private String category;
    private String publishYear;
    private ItemType type;
    private String coverImageUrl;

    public Item() {
        // Jackson
    }

    public Item(String title, String author, String category,
            ItemType type, String publishYear) {
        this.id = IdGenerator.generateItemId(type);
        this.title = title;
        this.author = author;
        this.category = category;
        this.publishYear = publishYear;
        this.type = type;
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean checkout() {
        return true;
    }

    public boolean returnItem() {
        return true;
    }

    public int getTotalCopies() {
        return -1;
    }

    public int getAvailableCopies() {
        return -1;
    }

    public String getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(String publishYear) {
        this.publishYear = publishYear;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCoverImageUrl() {
        return this.coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    @Override
    public String toString() {
        return "id=" + id + ", title=" + title + ", author=" + author
                + ", category=" + category + ", publishYear=" + publishYear
                + ", type=" + type;
    }
}