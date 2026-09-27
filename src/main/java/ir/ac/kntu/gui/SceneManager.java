package ir.ac.kntu.gui;

import ir.ac.kntu.entities.module.Item;
import ir.ac.kntu.entities.module.Ticket;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.gui.views.*;
import ir.ac.kntu.repo.BorrowHistoryRepo;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.repo.TransactionRepo;
import ir.ac.kntu.repo.UserRepo;
import ir.ac.kntu.services.AuthService;
import ir.ac.kntu.services.BorrowService;
import ir.ac.kntu.services.DebtService;
import ir.ac.kntu.services.ReservationService;
import ir.ac.kntu.services.TicketService;
import ir.ac.kntu.services.UserService;
import ir.ac.kntu.services.WalletService;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ir.ac.kntu.services.NotificationService;
import ir.ac.kntu.services.ItemService;
import ir.ac.kntu.services.ManagerService;
import ir.ac.kntu.entities.module.Admin;
import ir.ac.kntu.entities.enums.Role;

public class SceneManager {

    private Stage primaryStage;
    private final StackPane rootNode;

    private final UserRepo userRepo;
    private final ItemRepo itemRepo;
    private final AuthService authService;
    private final BorrowService borrowService;
    private final BorrowHistoryRepo borrowHistoryRepo;
    private final TransactionRepo transactionRepo;
    private WalletService walletService;
    private DebtService debtService;
    private TicketService ticketService;
    private UserService userService;
    private ReservationService reservationService;
    private ManagerService managerService;

    private NotificationService notificationService;
    private ItemService itemService;
    private MainLayout mainLayout;

    public SceneManager(UserRepo userRepo, ItemRepo itemRepo, AuthService authService,
            BorrowService borrowService, BorrowHistoryRepo borrowHistoryRepo, TransactionRepo transactionRepo,
            WalletService walletService,
            DebtService debtService, TicketService ticketService, UserService userService,
            ReservationService reservationService, NotificationService notificationService,
            ItemService itemService, ManagerService managerService) {
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.authService = authService;
        this.borrowService = borrowService;
        this.borrowHistoryRepo = borrowHistoryRepo;
        this.transactionRepo = transactionRepo;
        this.walletService = walletService;
        this.debtService = debtService;
        this.ticketService = ticketService;
        this.userService = userService;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.itemService = itemService;
        this.managerService = managerService;
        this.rootNode = new StackPane();
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth() * 0.8;
        double height = screenBounds.getHeight() * 0.8;

        Scene scene = new Scene(rootNode, width, height);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load theme.css.");
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle("Library Management System");
        primaryStage.centerOnScreen();
        primaryStage.show();

        showPublicCatalog();
    }

    public void showPublicCatalog() {
        rootNode.getChildren().setAll(new PublicCatalogView(itemRepo, this::showAuthView));
    }

    private void showAuthView() {
        AuthView authView = new AuthView(authService, this::showDashboard,
                this::showPublicCatalog);
        rootNode.getChildren().setAll(authView);
    }

    private void showDashboard(User user) {
        notificationService.setCurrentUser(user.getId());
        ir.ac.kntu.util.AsyncExecutor.execute(
                () -> {
                    notificationService.performLoginScan(user.getId());
                    return null;
                },
                result -> {
                }, error -> {
                });

        if (user.getRole() == Role.ADMIN) {
            Admin admin = (Admin) user;
            AdminLayout adminLayout = new AdminLayout(admin, itemRepo, itemService,
                    ticketService, walletService, debtService, userService,
                    reservationService, notificationService, this::showPublicCatalog);
            rootNode.getChildren().setAll(adminLayout);
            return;
        }

        if (user.getRole() == Role.MANAGER) {
            ManagerLayout managerLayout = new ManagerLayout(user, userRepo, itemRepo, borrowHistoryRepo,
                    transactionRepo, managerService, this::showPublicCatalog);
            rootNode.getChildren().setAll(managerLayout);
            return;
        }

        CatalogView catalog = new CatalogView(itemRepo, item -> pushItemDetail(item, user));
        MyBorrowsView myBorrows = new MyBorrowsView(user, borrowService, itemRepo, notificationService);
        WalletView wallet = new WalletView(user, walletService, debtService);
        TicketListView ticketList = new TicketListView(user, ticketService, ticket -> pushTicketChat(ticket, user));
        ProfileView profile = new ProfileView(user, userService);
        MyReservationsView reservations = new MyReservationsView(user, reservationService, itemRepo, borrowService,
                notificationService);

        mainLayout = new MainLayout(user, catalog, myBorrows, wallet, ticketList, profile, reservations,
                this::showPublicCatalog, notificationService);
        rootNode.getChildren().setAll(mainLayout);
    }

    private void pushItemDetail(Item item, User user) {
        ItemDetailView detail = new ItemDetailView(item, user, borrowService, borrowHistoryRepo,
                reservationService, notificationService, () -> mainLayout.popView());
        mainLayout.pushView(detail);
    }

    private void pushTicketChat(Ticket ticket, User user) {
        TicketChatView chat = new TicketChatView(ticket, user, ticketService, () -> mainLayout.popView());
        mainLayout.pushView(chat);
    }
}