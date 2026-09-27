package ir.ac.kntu.gui;

import ir.ac.kntu.repo.*;
import ir.ac.kntu.services.*;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {

    private ItemRepo itemRepo;
    private UserRepo userRepo;
    private BorrowHistoryRepo borrowHistoryRepo;
    private ReservationRepo reservationRepo;
    private TicketRepo ticketRepo;
    private TransactionRepo transactionRepo;

    private ir.ac.kntu.repo.NotificationRepo notificationRepo;
    private ir.ac.kntu.services.NotificationService notificationService;

    private AuthService authService;
    private DebtService debtService;
    private ReservationService reservationService;
    private BorrowService borrowService;
    private WalletService walletService;
    private TicketService ticketService;
    private UserService userService;
    private ir.ac.kntu.services.ItemService itemService;
    private ManagerService managerService;

    @Override
    public void init() {
        itemRepo = new ItemRepo();
        userRepo = new UserRepo();
        borrowHistoryRepo = new BorrowHistoryRepo();
        reservationRepo = new ReservationRepo();
        ticketRepo = new TicketRepo();
        transactionRepo = new TransactionRepo();

        authService = new AuthService(userRepo);
        debtService = new DebtService(borrowHistoryRepo, userRepo);
        reservationService = new ReservationService(reservationRepo, itemRepo);
        borrowService = new BorrowService(borrowHistoryRepo, itemRepo, debtService, reservationService);
        walletService = new WalletService(userRepo, transactionRepo, debtService, borrowHistoryRepo);
        ticketService = new TicketService(ticketRepo);
        userService = new UserService(userRepo);
        itemService = new ir.ac.kntu.services.ItemService(itemRepo);
        notificationRepo = new ir.ac.kntu.repo.NotificationRepo();
        notificationService = new ir.ac.kntu.services.NotificationService(
                notificationRepo, borrowHistoryRepo, reservationRepo, itemRepo);
        reservationService.setNotificationService(notificationService);
        managerService = new ManagerService(userRepo, new PolicyRepo());
    }

    @Override
    public void start(Stage primaryStage) {
        SceneManager sceneManager = new SceneManager(userRepo, itemRepo, authService, borrowService, borrowHistoryRepo,
                transactionRepo, walletService, debtService, ticketService, userService, reservationService,
                notificationService, itemService, managerService);
        sceneManager.init(primaryStage);
    }

    @Override
    public void stop() {
        AsyncExecutor.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}