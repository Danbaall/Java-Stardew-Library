package ir.ac.kntu.services;

import ir.ac.kntu.entities.enums.Format;
import ir.ac.kntu.entities.enums.ItemType;
import ir.ac.kntu.entities.enums.Period;
import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.module.AudioBook;
import ir.ac.kntu.entities.module.Book;
import ir.ac.kntu.entities.module.Ebook;
import ir.ac.kntu.entities.module.FilterFactor;
import ir.ac.kntu.entities.module.Item;
import ir.ac.kntu.entities.module.Magazine;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.util.ServiceResult;
import ir.ac.kntu.util.Validator;
import java.util.List;

public class ItemService {

    private static final String ADMIN_ONLY = "Only admins can manage catalog items.";
    private static final String INVALID_YEAR = "Invalid publish year.";

    private final ItemRepo itemRepo;

    public ItemService(ItemRepo itemRepo) {
        this.itemRepo = itemRepo;
    }

    public ServiceResult<List<Item>> search(FilterFactor criteria) {
        List<Item> allItems = itemRepo.findAll();
        List<Item> matched = allItems.stream()
                .filter(criteria::matches)
                .toList();
        return ServiceResult.success(
                matched,
                "Found " + matched.size() + " items.");
    }

    public ServiceResult<Item> addBook(
            String data,
            ItemType type,
            int totalCopies,
            User currentUser) {
        if (!isAdmin(currentUser)) {
            return ServiceResult.failure(ADMIN_ONLY);
        }
        String[] parts = data.split("::");
        String title = parts[0],
                author = parts[1],
                category = parts[2],
                publishYear = parts[3],
                publisher = parts[4];
        int pageCount = Integer.parseInt(parts[5]);
        String isbn = parts[6];
        Validator validator = new Validator();
        if (!validator.isValidIsbn(isbn)) {
            return ServiceResult.failure("Invalid ISBN-13 checksum.");
        }
        if (!validator.isValidPublishYear(publishYear)) {
            return ServiceResult.failure(INVALID_YEAR);
        }
        Book book = new Book(
                title,
                author,
                category,
                totalCopies,
                publishYear,
                publisher,
                pageCount,
                isbn);
        itemRepo.save(book);
        return ServiceResult.success(book, "Book added successfully.");
    }

    public ServiceResult<Item> addMagazine(
            String data,
            ItemType type,
            int totalCopies,
            User currentUser) {
        if (!isAdmin(currentUser)) {
            return ServiceResult.failure(ADMIN_ONLY);
        }
        String[] parts = data.split("::");
        String title = parts[0],
                author = parts[1],
                category = parts[2],
                publishYear = parts[3],
                issn = parts[4];
        Period period = Period.valueOf(parts[5]);
        Validator validator = new Validator();
        if (!validator.isValidIssn(issn)) {
            return ServiceResult.failure("Invalid ISSN format.");
        }
        if (!validator.isValidPublishYear(publishYear)) {
            return ServiceResult.failure(INVALID_YEAR);
        }
        Magazine mag = new Magazine(
                title,
                author,
                category,
                type,
                totalCopies,
                publishYear,
                issn,
                period);
        itemRepo.save(mag);
        return ServiceResult.success(mag, "Magazine added successfully.");
    }

    public ServiceResult<Item> addEbook(
            String data,
            ItemType type,
            int totalCopies,
            User currentUser) {
        if (!isAdmin(currentUser)) {
            return ServiceResult.failure(ADMIN_ONLY);
        }
        String[] parts = data.split("::");
        String title = parts[0],
                author = parts[1],
                category = parts[2],
                publishYear = parts[3];
        double fileSizeMB = Double.parseDouble(parts[4]);
        Format format = Format.valueOf(parts[5]);
        String url = parts[6];
        Validator validator = new Validator();
        if (!validator.isValidPublishYear(publishYear)) {
            return ServiceResult.failure(INVALID_YEAR);
        }
        if (!validator.isValidUrl(url)) {
            return ServiceResult.failure("Invalid URL format.");
        }
        Ebook ebook = new Ebook(
                title,
                author,
                category,
                type,
                publishYear,
                fileSizeMB,
                format,
                url);
        itemRepo.save(ebook);
        return ServiceResult.success(ebook, "Ebook added successfully.");
    }

    public ServiceResult<Item> addAudioBook(
            String data,
            ItemType type,
            int totalCopies,
            User currentUser) {
        if (!isAdmin(currentUser)) {
            return ServiceResult.failure(ADMIN_ONLY);
        }
        String[] parts = data.split("::");
        String title = parts[0],
                author = parts[1],
                category = parts[2],
                publishYear = parts[3];
        double fileSizeMB = Double.parseDouble(parts[4]);
        Format format = Format.valueOf(parts[5]);
        String url = parts[6],
                duration = parts[7];
        Validator validator = new Validator();
        if (!validator.isValidPublishYear(publishYear)) {
            return ServiceResult.failure(INVALID_YEAR);
        }
        if (!validator.isValidUrl(url)) {
            return ServiceResult.failure("Invalid URL format.");
        }
        AudioBook audioBook = new AudioBook(
                title,
                author,
                category,
                type,
                publishYear,
                fileSizeMB,
                format,
                url,
                duration);
        itemRepo.save(audioBook);
        return ServiceResult.success(
                audioBook,
                "AudioBook added successfully.");
    }

    public ServiceResult<Void> removeItem(String itemId, User currentUser) {
        if (!isAdmin(currentUser)) {
            return ServiceResult.failure(ADMIN_ONLY);
        }
        Item item = itemRepo.findById(itemId);
        if (item == null) {
            return ServiceResult.failure("Item not found.");
        }
        itemRepo.delete(itemId);
        return ServiceResult.success("Item removed successfully.");
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }
}
