package ir.ac.kntu.cli;

import ir.ac.kntu.entities.enums.Format;
import ir.ac.kntu.entities.enums.ItemType;
import ir.ac.kntu.entities.enums.Period;
import ir.ac.kntu.entities.module.FilterFactor;
import ir.ac.kntu.entities.module.Item;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.services.ItemService;
import ir.ac.kntu.util.ServiceResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ItemUI {

    private static final String RESET = "\033[0m";
    private static final String CYAN = "\033[0;36m";
    private static final String RED = "\033[0;31m";
    private static final String YELLOW = "\033[0;33m";

    private final ItemRepo itemRepo;
    private final ItemService itemService;
    private final Scanner scanner;

    public ItemUI(ItemRepo itemRepo, ItemService itemService, Scanner scanner) {
        this.itemRepo = itemRepo;
        this.itemService = itemService;
        this.scanner = scanner;
    }

    public void displayCatalog() {
        boolean running = true;
        while (running) {
            Item selected = displayCatalogBrowser();
            if (selected == null) {
                running = false;
            } else {
                System.out.println(
                    YELLOW + "\nYou must be logged in to select items." + RESET
                );
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    public void displayAddItem(User user) {
        try {
            collectAndAddItem(user);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");
        }
        scanner.nextLine();
    }

    private void collectAndAddItem(User user) {
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.print("Author: ");
        String author = scanner.nextLine();
        System.out.print("Category: ");
        String category = scanner.nextLine();
        System.out.print(
            "Item Type [b]ook / [m]agazine / [e]book / [a]udiobook: "
        );
        String itemType = scanner.nextLine();
        ItemType type = parseItemType(itemType);
        int copies = 0;
        if (type == ItemType.BOOK || type == ItemType.MAGAZINE) {
            System.out.print("Number of copies: ");
            copies = Integer.parseInt(scanner.nextLine());
        }
        System.out.print("Publish year: ");
        String year = scanner.nextLine();
        String baseData = title + "::" + author + "::" + category + "::" + year;
        dispatchAddItem(type, baseData, copies, user);
    }

    private ItemType parseItemType(String code) {
        return switch (code) {
            case "m" -> ItemType.MAGAZINE;
            case "a" -> ItemType.AUDIOBOOK;
            case "e" -> ItemType.EBOOK;
            default -> ItemType.BOOK;
        };
    }

    private void dispatchAddItem(
        ItemType type,
        String baseData,
        int copies,
        User user
    ) {
        switch (type) {
            case BOOK -> addBookItem(baseData, copies, user);
            case MAGAZINE -> addMagazineItem(baseData, copies, user);
            case EBOOK -> addEbookItem(baseData, copies, user);
            case AUDIOBOOK -> addAudiobookItem(baseData, copies, user);
            default -> {}
        }
    }

    public Item displayCatalogBrowser() {
        int width = 100;
        while (true) {
            ConsoleUtils.clearScreen();
            System.out.println(
                CYAN + "╔" + ConsoleUtils.repeat('═', width) + "╗" + RESET
            );
            System.out.println(
                CYAN +
                    "║" +
                    ConsoleUtils.centerText("CATALOG", width) +
                    RESET +
                    CYAN +
                    "║" +
                    RESET
            );
            System.out.println(
                CYAN + "╚" + ConsoleUtils.repeat('═', width) + "╝" + RESET
            );
            System.out.println("1. View all items");
            System.out.println("2. Search for books or authors");
            System.out.println(
                "3. Filter by item type, category or availability"
            );
            System.out.println("4. Return");
            System.out.print("> ");
            String choice = scanner.nextLine();
            if (choice.equals("4")) {
                return null;
            }
            List<Item> items = fetchCatalogItems(choice);
            int idx = displayPagedList(items, "Catalog Results");
            if (idx != -1) {
                return items.get(idx);
            }
        }
    }

    private List<Item> fetchCatalogItems(String choice) {
        return switch (choice) {
            case "1" -> itemRepo.findAll();
            case "2" -> searchItemsByKeyword();
            case "3" -> filterItems();
            default -> new ArrayList<>();
        };
    }

    private List<Item> searchItemsByKeyword() {
        System.out.print("Enter Book title or full Author name: ");
        String keyword = scanner.nextLine();
        ServiceResult<List<Item>> result = itemService.search(
            new FilterFactor(keyword, null, null, null)
        );
        if (result.isSuccess()) {
            return result.getData();
        }
        System.out.println("No matches.");
        scanner.nextLine();
        return new ArrayList<>();
    }

    private List<Item> filterItems() {
        System.out.print(
            "Item type: [b]ook / [m]agazine / [e]book / [a]udiobook / [-] skip: "
        );
        String itemType = scanner.nextLine();
        ItemType type = itemType.equals("-") ? null : parseItemType(itemType);
        System.out.print("Category name / [-] skip: ");
        String category = scanner.nextLine();
        System.out.print("[a] available only / [-] skip: ");
        String isAvailable = scanner.nextLine();
        ServiceResult<List<Item>> result = itemService.search(
            new FilterFactor(
                null,
                type,
                category.equals("-") ? null : category,
                isAvailable.equals("a") ? Boolean.TRUE : null
            )
        );
        if (result.isSuccess()) {
            return result.getData();
        }
        System.out.println("No matches.");
        scanner.nextLine();
        return new ArrayList<>();
    }

    private void addBookItem(String baseData, int copies, User user) {
        try {
            System.out.print("Publisher: ");
            String publisher = scanner.nextLine();
            System.out.print("Page count: ");
            int pageCount = Integer.parseInt(scanner.nextLine());
            System.out.print("ISBN: ");
            String isbn = scanner.nextLine();
            String[] parts = baseData.split("::");
            String bookData =
                parts[0] +
                "::" +
                parts[1] +
                "::" +
                parts[2] +
                "::" +
                parts[3] +
                "::" +
                publisher +
                "::" +
                pageCount +
                "::" +
                isbn;
            ServiceResult<Item> result = itemService.addBook(
                bookData,
                ItemType.BOOK,
                copies,
                user
            );
            System.out.println(result.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid page count.");
        }
    }

    private void addMagazineItem(String baseData, int copies, User user) {
        System.out.print("ISSN: ");
        String issn = scanner.nextLine();
        System.out.print("Period: [w]eekly / [m]onthly / [s]easonal: ");
        String period = scanner.nextLine();
        Period peri = switch (period) {
            case "m" -> Period.MONTHLY;
            case "s" -> Period.SEASONAL;
            default -> Period.WEEKLY;
        };
        String[] parts = baseData.split("::");
        String magData =
            parts[0] +
            "::" +
            parts[1] +
            "::" +
            parts[2] +
            "::" +
            parts[3] +
            "::" +
            issn +
            "::" +
            peri;
        ServiceResult<Item> result = itemService.addMagazine(
            magData,
            ItemType.MAGAZINE,
            copies,
            user
        );
        System.out.println(result.getMessage());
    }

    private void addEbookItem(String baseData, int copies, User user) {
        try {
            System.out.print("File size in MB: ");
            double fileSize = Double.parseDouble(scanner.nextLine());
            System.out.print("Format: [p]df / [e]pub: ");
            String format = scanner.nextLine();
            Format fmt = format.equals("e") ? Format.EPUB : Format.PDF;
            System.out.print("URL: ");
            String url = scanner.nextLine();
            String[] parts = baseData.split("::");
            String ebookData =
                parts[0] +
                "::" +
                parts[1] +
                "::" +
                parts[2] +
                "::" +
                parts[3] +
                "::" +
                fileSize +
                "::" +
                fmt +
                "::" +
                url;
            ServiceResult<Item> result = itemService.addEbook(
                ebookData,
                ItemType.EBOOK,
                copies,
                user
            );
            System.out.println(result.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid file size.");
        }
    }

    private void addAudiobookItem(String baseData, int copies, User user) {
        try {
            System.out.print("File size in MB: ");
            double fileSize = Double.parseDouble(scanner.nextLine());
            System.out.print("Format: [A]A3 / [M]P3: ");
            String format = scanner.nextLine();
            Format fmt = format.equals("A") ? Format.AA3 : Format.MP3;
            System.out.print("URL: ");
            String url = scanner.nextLine();
            System.out.print("Duration: ");
            String dur = scanner.nextLine();
            String[] parts = baseData.split("::");
            String audioData =
                parts[0] +
                "::" +
                parts[1] +
                "::" +
                parts[2] +
                "::" +
                parts[3] +
                "::" +
                fileSize +
                "::" +
                fmt +
                "::" +
                url +
                "::" +
                dur;
            ServiceResult<Item> result = itemService.addAudioBook(
                audioData,
                ItemType.AUDIOBOOK,
                copies,
                user
            );
            System.out.println(result.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid file size.");
        }
    }

    private void printPagedHeader(
        String title,
        int currentPage,
        int totalPages
    ) {
        int width = 100;
        System.out.println(
            CYAN + "╔" + ConsoleUtils.repeat('═', width) + "╗" + RESET
        );
        String header =
            title + "  Page " + (currentPage + 1) + "/" + totalPages;
        System.out.println(
            CYAN +
                "║" +
                ConsoleUtils.centerText(header, width) +
                RESET +
                CYAN +
                "║" +
                RESET
        );
        System.out.println(
            CYAN + "╚" + ConsoleUtils.repeat('═', width) + "╝" + RESET
        );
    }

    public int displayPagedList(List<?> items, String title) {
        int pageSize = 10;
        int currentPage = 0;
        int width = 100;
        int totalPages = Math.max((items.size() + pageSize - 1) / pageSize, 1);
        while (true) {
            ConsoleUtils.clearScreen();
            printPagedHeader(title, currentPage, totalPages);
            if (items.isEmpty()) {
                System.out.println("No items found.");
            } else {
                int start = currentPage * pageSize;
                int end = Math.min(start + pageSize, items.size());
                for (int i = start; i < end; i++) {
                    System.out.println(
                        RED +
                            (i - start + 1) +
                            ". " +
                            items.get(i).toString() +
                            RESET
                    );
                    System.out.println(
                        CYAN + ConsoleUtils.repeat('═', width) + RESET
                    );
                }
            }
            int navResult = handlePageNavigation(
                currentPage,
                pageSize,
                items.size()
            );
            if (navResult == -2) {
                currentPage = Math.min(currentPage + 1, totalPages - 1);
            } else if (navResult == -3) {
                currentPage = Math.max(currentPage - 1, 0);
            } else if (navResult >= 0) {
                return navResult;
            } else if (navResult == -1) {
                return -1;
            }
        }
    }

    private int handlePageNavigation(
        int currentPage,
        int pageSize,
        int totalItems
    ) {
        System.out.println(
            "\n[N]ext Page | [P]rev Page | [B]ack | Enter number to select"
        );
        System.out.print("> ");
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("n")) {
            return -2;
        }
        if (input.equalsIgnoreCase("p")) {
            return -3;
        }
        if (input.equalsIgnoreCase("b")) {
            return -1;
        }
        try {
            int num = Integer.parseInt(input);
            int start = currentPage * pageSize;
            int index = start + num - 1;
            int end = Math.min(start + pageSize, totalItems);
            if (index >= start && index < end) {
                return index;
            }
            System.out.println("Invalid selection for this page.");
            scanner.nextLine();
        } catch (NumberFormatException e) {
            System.out.println("Please enter a number, N, P, or B.");
            scanner.nextLine();
        }
        return -4;
    }
}
