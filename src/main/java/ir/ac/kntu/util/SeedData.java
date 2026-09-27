package ir.ac.kntu.util;

import java.util.ArrayList;
import java.util.List;

import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.module.Book;
import ir.ac.kntu.entities.module.Item;
import ir.ac.kntu.entities.module.Manager;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.repo.UserRepo;

public class SeedData {

    private static final String FANTASY = "Fantasy";
    private static final String TECH = "Tech";
    private static final String CLASSIC = "Classic";
    private static final String SELF_HELP = "Self-Help";
    private static final String YR_2011 = "2011";

    private final UserRepo userRepo;
    private final ItemRepo itemRepo;

    public SeedData(UserRepo userRepo, ItemRepo itemRepo) {
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
    }

    public void seed() {
        seedManager();
        seedItems();
    }

    private void seedManager() {
        if (userRepo.findAll().stream()
                .anyMatch(user -> user.getRole() == Role.MANAGER)) {
            return;
        }
        userRepo.save(new Manager("manager", "Manager@1234!", "manager@library.com", "09000000000"));
        System.out.println("[Initializer] Manager created. username=manager  password=Manager@1234!");
    }

    private void seedItems() {
        if (!itemRepo.findAll().isEmpty()) {
            return;
        }
        System.out.println("[Initializer] Seeding dummy books...");
        List<Item> books = new ArrayList<>();
        books.addAll(batchA());
        books.addAll(batchB());
        books.addAll(batchC());
        books.addAll(batchD());
        itemRepo.saveAll(books);
    }

    private static List<Item> batchA() {
        return List.of(
                new Book("The Hobbit", "J.R.R. Tolkien", FANTASY, 1000, "1937", "Allen & Unwin", 310, "9780261102217"),
                new Book("1984", "George Orwell", "Dystopian", 1000, "1949", "Secker & Warburg", 328, "9780451524935"),
                new Book("Dune", "Frank Herbert", "Sci-Fi", 1000, "1965", "Chilton Books", 688, "9780441013593"),
                new Book("Clean Code", "Robert Martin", TECH, 1000, "2008", "Prentice Hall", 464, "9780132350884"),
                new Book("The Pragmatic Programmer", "David Thomas", TECH, 1000, "1999", "Addison-Wesley", 352,
                        "9780135957059"),
                new Book("Harry Potter", "J.K. Rowling", FANTASY, 1000, "1997", "Bloomsbury", 223, "9780747532699"));
    }

    private static List<Item> batchB() {
        return List.of(
                new Book("The Great Gatsby", "F. Scott Fitzgerald", CLASSIC, 1000, "1925", "Scribner", 180,
                        "9780743273565"),
                new Book("To Kill a Mockingbird", "Harper Lee", CLASSIC, 1000, "1960", "J.B. Lippincott", 281,
                        "9780061935466"),
                new Book("Sapiens", "Yuval Noah Harari", "History", 1000, YR_2011, "Harvill Secker", 443,
                        "9780062316097"),
                new Book("Atomic Habits", "James Clear", SELF_HELP, 1000, "2018", "Avery", 320, "9780735211292"),
                new Book("The Alchemist", "Paulo Coelho", "Fiction", 1000, "1988", "HarperCollins", 208,
                        "9780062315007"),
                new Book("Steve Jobs", "Walter Isaacson", "Biography", 1000, YR_2011, "Simon & Schuster", 656,
                        "9781451648539"));
    }

    private static List<Item> batchC() {
        return List.of(
                new Book("The Lord of the Rings", "J.R.R. Tolkien", FANTASY, 1000, "1954", "Allen & Unwin", 1178,
                        "9780618640157"),
                new Book("Brave New World", "Aldous Huxley", "Dystopian", 1000, "1932", "Chatto & Windus", 311,
                        "9780060850524"),
                new Book("The Hitchhiker's Guide", "Douglas Adams", "Sci-Fi", 1000, "1979", "Pan Books", 224,
                        "9780345391803"),
                new Book("Design Patterns", "Gang of Four", TECH, 1000, "1994", "Addison-Wesley", 395, "9780201633610"),
                new Book("The Catcher in the Rye", "J.D. Salinger", CLASSIC, 1000, "1951", "Little Brown", 277,
                        "9780316769174"),
                new Book("Pride and Prejudice", "Jane Austen", CLASSIC, 1000, "1813", "T. Egerton", 432,
                        "9780141439518"));
    }

    private static List<Item> batchD() {
        return List.of(
                new Book("Thinking Fast and Slow", "Daniel Kahneman", "Psychology", 1000, YR_2011, "Farrar Straus", 499,
                        "9780374533557"),
                new Book("The Power of Habit", "Charles Duhigg", SELF_HELP, 1000, "2012", "Random House", 371,
                        "9780812981605"),
                new Book("Educated", "Tara Westover", "Biography", 1000, "2018", "Random House", 334, "9780399590504"),
                new Book("The Subtle Art", "Mark Manson", SELF_HELP, 1000, "2016", "HarperOne", 224, "9780062457714"),
                new Book("Meditations", "Marcus Aurelius", "Philosophy", 1000, "180", "Penguin Classics", 254,
                        "9780140449334"),
                new Book("A Brief History of Time", "Stephen Hawking", "Science", 1000, "1988", "Bantam Books", 212,
                        "9780553380163"));
    }
}
