package ir.ac.kntu.cli;

import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.enums.TicketType;
import ir.ac.kntu.entities.module.Admin;
import ir.ac.kntu.entities.module.BorrowRecord;
import ir.ac.kntu.entities.module.BorrowRecordView;
import ir.ac.kntu.entities.module.Client;
import ir.ac.kntu.entities.module.Item;
import ir.ac.kntu.entities.module.PhysicalItem;
import ir.ac.kntu.entities.module.Reservation;
import ir.ac.kntu.entities.module.Ticket;
import ir.ac.kntu.entities.module.Transaction;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.exceptions.AccountDisabledException;
import ir.ac.kntu.repo.BorrowHistoryRepo;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.repo.TicketRepo;
import ir.ac.kntu.repo.TransactionRepo;
import ir.ac.kntu.repo.UserRepo;
import ir.ac.kntu.services.AuthService;
import ir.ac.kntu.services.BorrowService;
import ir.ac.kntu.services.DebtService;
import ir.ac.kntu.services.ItemService;
import ir.ac.kntu.services.ManagerService;
import ir.ac.kntu.services.ReservationService;
import ir.ac.kntu.services.TicketService;
import ir.ac.kntu.services.UserService;
import ir.ac.kntu.services.WalletService;
import ir.ac.kntu.util.ServiceResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Supplier;

public class MenuHandler {

    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String CYAN = "\033[0;36m";
    private static final String YELLOW = "\033[0;33m";
    private static final String GREEN = "\033[0;32m";
    private static final String DONE_PREFIX = "done, ";
    private static final String FAILED_PREFIX = "failed, ";

    private final BorrowHistoryRepo borrowRepo;
    private final TransactionRepo txnRepo;
    private final TicketRepo ticketRepo;
    private final ItemRepo itemRepo;
    private final UserRepo userRepo;

    private final AuthService authService;
    private final BorrowService borrowService;
    private final WalletService walletService;
    private final DebtService debtService;
    private final TicketService ticketService;
    private final ReservationService reservationService;

    private final ItemUI itemUI;
    private final ProfileUI profileUI;
    private final TicketUI ticketUI;
    private final ManagerUI managerUI;
    private final Scanner scanner;

    public MenuHandler(
            ItemRepo itemRepo,
            BorrowHistoryRepo borrowRepo,
            TransactionRepo txnRepo,
            TicketRepo ticketRepo,
            AuthService authService,
            UserService userService,
            ItemService itemService,
            BorrowService borrowService,
            WalletService walletService,
            DebtService debtService,
            TicketService ticketService,
            UserRepo userRepo,
            ManagerService managerService,
            ReservationService reservationService) {
        this.borrowRepo = borrowRepo;
        this.txnRepo = txnRepo;
        this.ticketRepo = ticketRepo;
        this.authService = authService;
        this.borrowService = borrowService;
        this.walletService = walletService;
        this.debtService = debtService;
        this.ticketService = ticketService;
        this.scanner = new Scanner(System.in);
        this.itemUI = new ItemUI(itemRepo, itemService, scanner);
        this.profileUI = new ProfileUI(authService, userService, scanner);
        this.ticketUI = new TicketUI(scanner, ticketService, userService, itemUI, itemRepo, txnRepo,
                reservationService);
        this.managerUI = new ManagerUI(managerService, itemUI, scanner);
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.reservationService = reservationService;
    }

    public void run() {
        boolean running = true;
        while (running) {
            User currentUser = displayMainMenu();
            if (currentUser == null) {
                running = false;
            } else if (currentUser.getRole() == Role.MANAGER) {
                managerUI.displayManagerDashboard(currentUser);
            } else if (currentUser.getRole() == Role.ADMIN) {
                displayAdminDashboard(currentUser);
            } else {
                displayUserDashboard(currentUser);
            }
        }
    }

    private void printBanner(String text, int width) {
        System.out.println(
                CYAN + "╔" + ConsoleUtils.repeat('═', width) + "╗" + RESET);
        System.out.println(
                CYAN +
                        "║" +
                        BOLD +
                        YELLOW +
                        ConsoleUtils.centerText(text, width) +
                        RESET +
                        CYAN +
                        "║" +
                        RESET);
        System.out.println(
                CYAN + "╚" + ConsoleUtils.repeat('═', width) + "╝" + RESET);
    }

    private User displayMainMenu() {
        int width = 40;
        Map<String, Supplier<User>> actions = buildMainMenuActions();
        while (true) {
            ConsoleUtils.clearScreen();
            printBanner("STARDEW LIBRARY", width);
            System.out.println(GREEN + "1. View Catalog\n2. Log in\n3. Sign up\n4. Quit" + RESET);
            System.out.print("> ");
            String choice = scanner.nextLine();
            if (choice.equals("4")) {
                return null;
            }
            User user = actions.getOrDefault(choice, () -> null).get();
            if (user != null) {
                return user;
            }
        }
    }

    private Map<String, Supplier<User>> buildMainMenuActions() {
        Map<String, Supplier<User>> actions = new HashMap<>();
        actions.put("1", () -> {
            itemUI.displayCatalog();
            return null;
        });
        actions.put("2", this::handleLoginAndConfirm);
        actions.put("3", profileUI::displayRegistration);
        return actions;
    }

    private User handleLoginAndConfirm() {
        User user = handleLogin();
        if (user != null) {
            scanner.nextLine();
        }
        return user;
    }

    private User handleLogin() {
        System.out.print("Enter Username/Email/Phone: ");
        String id = scanner.nextLine();
        System.out.print("Enter Password: ");
        String pass = scanner.nextLine();
        try {
            ServiceResult<User> result = authService.signIn(id, pass);
            System.out.println(result.getMessage());
            if (result.isSuccess()) {
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
                return result.getData();
            }
        } catch (AccountDisabledException e) {
            System.out.println(e.getMessage());
        }
        scanner.nextLine();
        return null;
    }

    private void displayUserDashboard(User user) {
        String menu = GREEN + "1. Browse & Borrow Catalog\n2. Show Active Borrows\n3. Borrow History\n"
                + "4. Wallet & Fines\n5. Support Tickets\n6. Update Profile\n7. My Reservations\n8. Logout" + RESET;
        runMenu("User Dashboard", () -> menu, "8", buildUserDashboardActions(user));
    }

    private Map<String, Runnable> buildUserDashboardActions(User user) {
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("1", () -> handleBrowseAndBorrow(user));
        actions.put("2", () -> displayActiveBorrows(user));
        actions.put("3", () -> displayBorrowHistory(user));
        actions.put("4", () -> displayWallet(user));
        actions.put("5", () -> displayTickets(user));
        actions.put("6", () -> profileUI.displayProfile(user));
        actions.put("7", () -> displayMyReservations(user));
        return actions;
    }

    private void runMenu(String title, Supplier<String> menuTextSupplier, String exitChoice,
            Map<String, Runnable> actions) {
        boolean running = true;
        while (running) {
            ConsoleUtils.clearScreen();
            printBanner(title, 50);
            System.out.println(menuTextSupplier.get());
            System.out.print("> ");
            String choice = scanner.nextLine();
            if (choice.equals(exitChoice)) {
                running = false;
            } else {
                Runnable action = actions.get(choice);
                if (action != null) {
                    action.run();
                }
            }
        }
    }

    private void handleBrowseAndBorrow(User user) {
        Item selected = itemUI.displayCatalogBrowser();
        if (selected == null) {
            return;
        }
        System.out.println("\n--- " + selected.getTitle() + " ---");
        System.out.println("ID: " + selected.getId());
        int copies = selected.getAvailableCopies();
        System.out.println("Available copies: " + (copies == -1 ? "Unlimited" : copies));
        System.out.println("1. Borrow  2. Back");
        System.out.print("> ");
        if (scanner.nextLine().equals("1")) {
            ServiceResult<Void> result = borrowService.checkOut((Client) user, selected.getId());
            System.out.println(result.getMessage());
            if (!result.isSuccess() && selected instanceof PhysicalItem) {
                System.out.print("Would you like to reserve this item? (y/n): ");
                if (scanner.nextLine().equalsIgnoreCase("y")) {
                    ServiceResult<Reservation> resResult = reservationService.reserveItem(user, selected.getId());
                    System.out.println(resResult.getMessage());
                }
            }
            scanner.nextLine();
        }
    }

    private void displayMyReservations(User user) {
        List<Reservation> reservations = reservationService.getUserReservations(user);
        List<String> lines = new ArrayList<>();
        for (Reservation r : reservations) {
            Item item = itemRepo.findById(r.getItemId());
            String itemName = (item != null) ? item.getTitle() : r.getItemId();
            String deadline = (r.getClaimDeadline() != null) ? r.getClaimDeadline().toString() : "N/A";
            lines.add("ID: " + r.getReservationId() + " | Item: " + itemName
                    + " | Status: " + r.getStatus() + " | Deadline: " + deadline);
        }
        int idx = itemUI.displayPagedList(lines, "MY RESERVATIONS");
        if (idx < 0) {
            return;
        }

        Reservation selected = reservations.get(idx);
        if (selected.isActive()) {
            System.out.print("1. Cancel this reservation  2. Back: ");
            if (scanner.nextLine().equals("1")) {
                ServiceResult<Void> res = reservationService.cancelReservation(user,
                        selected.getReservationId());
                System.out.println(res.getMessage());
                scanner.nextLine();
            }
        } else {
            System.out.println("This reservation is no longer active. (Press Enter)");
            scanner.nextLine();
        }
    }

    private void displayActiveBorrows(User user) {
        List<BorrowRecordView> active = buildBorrowViews(
                borrowRepo.findActiveByUserId(user.getId()));
        int selectedIndex = itemUI.displayPagedList(active, "ACTIVE BORROWS");
        if (selectedIndex == -1) {
            return;
        }
        BorrowRecordView selected = active.get(selectedIndex);
        System.out.println("1. Return Item  2. Request Extension  3. Cancel");
        System.out.print("> ");
        String choice = scanner.nextLine();
        ServiceResult<Void> result = switch (choice) {
            case "1" -> borrowService.returnItem(selected.recordId(), user);
            case "2" -> borrowService.requestExtension(
                    selected.recordId(),
                    user);
            default -> null;
        };
        if (result != null) {
            System.out.println(
                    (result.isSuccess() ? DONE_PREFIX : "error, ") +
                            result.getMessage());
            scanner.nextLine();
        }
    }

    private List<BorrowRecordView> buildBorrowViews(
            List<BorrowRecord> records) {
        List<BorrowRecordView> views = new ArrayList<>();
        for (BorrowRecord raw : records) {
            User cUser = userRepo.findById(raw.getUserId());
            Item item = itemRepo.findById(raw.getItemId());
            views.add(
                    new BorrowRecordView(
                            raw.getRecordId(),
                            cUser != null ? cUser.getUserName() : raw.getUserId(),
                            item != null ? item.getTitle() : raw.getItemId(),
                            raw.getBorrowDate(),
                            raw.getDueDate(),
                            raw.getStatus()));
        }
        return views;
    }

    private void displayBorrowHistory(User user) {
        List<BorrowRecordView> views = buildBorrowViews(
                borrowRepo.findByUserId(user.getId()));
        itemUI.displayPagedList(views, "BORROWING HISTORY");
    }

    private void displayWallet(User user) {
        if (!(user instanceof Client)) {
            System.out.println("Only regular users have wallets.");
            scanner.nextLine();
            return;
        }
        Client client = (Client) user;
        runMenu("WALLET", () -> buildWalletMenuText(client), "4", buildWalletActions(client, user));
    }

    private String buildWalletMenuText(Client client) {
        double debt = debtService.calculateUserDebt(client.getId()).getData();
        return "Balance: $" + client.getWallet().getBalance() + " | Debt: $" + debt
                + "\n1. Add Funds  2. Pay Debt  3. Transaction History  4. Back";
    }

    private Map<String, Runnable> buildWalletActions(Client client, User user) {
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("1", () -> handleAddFunds(client));
        actions.put("2", () -> handlePayDebt(client));
        actions.put("3", () -> {
            List<Transaction> txns = txnRepo.findByUserId(user.getId());
            itemUI.displayPagedList(txns, "Transactions");
        });
        return actions;
    }

    private void handleAddFunds(Client client) {
        try {
            System.out.print("Enter an Amount: ");
            double amt = Double.parseDouble(scanner.nextLine());
            ServiceResult<Void> res = walletService.chargeWallet(client, amt);
            System.out.println(
                    (res.isSuccess() ? DONE_PREFIX : FAILED_PREFIX) +
                            res.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
        }
        scanner.nextLine();
    }

    private void handlePayDebt(Client client) {
        try {
            System.out.print("Amount you want to pay: ");
            double amt = Double.parseDouble(scanner.nextLine());
            ServiceResult<Void> res = walletService.payDebt(client, amt);
            System.out.println(
                    (res.isSuccess() ? DONE_PREFIX : FAILED_PREFIX) +
                            res.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
        }
        scanner.nextLine();
    }

    private void displayTickets(User user) {
        String menu = GREEN + "1. Open New Ticket  \n2. View My Tickets  \n3. Back" + RESET;
        runMenu("SUPPORT TICKETS", () -> menu, "3", buildTicketsActions(user));
    }

    private Map<String, Runnable> buildTicketsActions(User user) {
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("1", () -> handleOpenTicket(user));
        actions.put("2", () -> viewMyTickets(user));
        return actions;
    }

    private void viewMyTickets(User user) {
        List<Ticket> tickets = ticketRepo.findByUserId(user.getId());
        int selectedIndex = itemUI.displayPagedList(tickets, "My Tickets");
        if (selectedIndex >= 0) {
            ticketUI.displayTicketDetail(tickets.get(selectedIndex), user);
        }
    }

    private void handleOpenTicket(User user) {
        System.out.print("Subject: ");
        String sub = scanner.nextLine();
        System.out.print("Message: ");
        String msg = scanner.nextLine();
        System.out.println("Type: [i]ssue / [r]equest / [f]inancial / [res]ervation");
        String type = scanner.nextLine();
        TicketType ticType = switch (type.toLowerCase()) {
            case "r", "request" -> TicketType.REQUEST;
            case "f", "financial" -> TicketType.FINANCIAL;
            case "res", "reservation" -> TicketType.RESERVATION;
            default -> TicketType.ISSUE;
        };
        ServiceResult<Ticket> res = ticketService.openTicket(user, ticType, sub, msg);
        System.out.println((res.isSuccess() ? DONE_PREFIX : FAILED_PREFIX) + res.getMessage());
        scanner.nextLine();
    }

    private void displayAdminDashboard(User user) {
        String menu = GREEN
                + "1. Search Catalog  \n2. Add New Item  \n3. Manage Tickets  \n4. View Debtors  \n5. Logout"
                + RESET;
        runMenu("Admin Dashboard", () -> menu, "5", buildAdminDashboardActions(user));
    }

    private Map<String, Runnable> buildAdminDashboardActions(User user) {
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("1", itemUI::displayCatalog);
        actions.put("2", () -> itemUI.displayAddItem(user));
        actions.put("3", () -> displayManageTickets(user));
        actions.put("4", () -> displayDebtors(user));
        return actions;
    }

    private void displayManageTickets(User user) {
        List<Ticket> tickets;
        if (user instanceof Admin) {
            tickets = ticketService.getTicketsForAdmin((Admin) user).getData();
        } else {
            tickets = ticketRepo.findAll();
        }
        int selectedIndex = itemUI.displayPagedList(tickets, "Support Tickets");
        if (selectedIndex >= 0) {
            ticketUI.displayTicketDetail(tickets.get(selectedIndex), user);
        }
    }

    private void displayDebtors(User user) {
        ServiceResult<List<User>> res = debtService.getUsersWithDebt(user);
        if (!res.isSuccess()) {
            System.out.println(FAILED_PREFIX + res.getMessage());
            scanner.nextLine();
            return;
        }
        List<String> debtorsToDisplay = new ArrayList<>();
        for (User debtor : res.getData()) {
            double debtAmount = debtService.calculateUserDebtRaw(
                    debtor.getId());
            debtorsToDisplay.add(
                    "id=" +
                            debtor.getId() +
                            ", userName=" +
                            debtor.getUserName() +
                            ", email=" +
                            debtor.getEmail() +
                            ", debt=" +
                            debtAmount);
        }
        itemUI.displayPagedList(debtorsToDisplay, "Users with Debt");
    }
}