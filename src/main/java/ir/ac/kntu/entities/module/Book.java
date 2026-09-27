package ir.ac.kntu.entities.module;

import com.fasterxml.jackson.annotation.JsonTypeName;

import ir.ac.kntu.entities.enums.ItemType;

@JsonTypeName("BOOK")
public class Book extends PhysicalItem {

    private String isbn;
    private String publisher;
    private int pageCount;

    public Book() {
        // this is for jackson
    }

    public Book(String title, String author, String category, int totalCopies,
            String publishYear, String publisher, int pageCount, String isbn) {
        super(title, author, category, ItemType.BOOK, totalCopies, publishYear);
        this.publisher = publisher;
        this.pageCount = pageCount;
        this.isbn = isbn;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return super.toString() + " publisher=" + publisher + ", pageCount=" + pageCount;
    }

}
