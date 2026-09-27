package ir.ac.kntu.entities.module;

import ir.ac.kntu.entities.enums.ItemType;

public class PhysicalItem extends Item {

    private int totalCopies;
    private int availableCopies;

    public PhysicalItem() {
        // Jackson
    }

    public PhysicalItem(String title, String author, String category,
            ItemType type, int totalCopies, String publishYear) {
        super(title, author, category, type, publishYear);
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    @Override
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public boolean checkout() {
        if (!isAvailable()) {
            return false;
        }
        availableCopies--;
        return true;
    }

    @Override
    public boolean returnItem() {
        if (availableCopies >= totalCopies) {
            return false;
        }
        availableCopies++;
        return true;
    }

    @Override
    public int getTotalCopies() {
        return totalCopies;
    }

    @Override
    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    @Override
    public String toString() {
        return "[PhysicalItem] " + super.toString()
                + ", totalCopies=" + totalCopies
                + ", availableCopies=" + availableCopies;
    }
}