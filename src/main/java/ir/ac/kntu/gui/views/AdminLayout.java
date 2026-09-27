package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.module.Admin;
import ir.ac.kntu.gui.components.NotificationBell;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.services.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AdminLayout extends BorderPane {

    private final Admin admin;
    private final Runnable onLogout;
    private final NotificationService notificationService;

    private final Stack<Node> viewStack = new Stack<>();
    private final List<Button> navButtons = new ArrayList<>();

    // lazy views
    private AdminDashboardView dashboardView;
    private CatalogView catalogView;
    private AdminAddItemView addItemView;
    private AdminTicketsView ticketsView;
    private AdminDebtorsView debtorsView;

    private final ItemRepo itemRepo;
    private final ItemService itemService;
    private final TicketService ticketService;
    private final WalletService walletService;
    private final DebtService debtService;
    private final UserService userService;
    private final ReservationService reservationService;

    private Button dashboardBtn, catalogBtn, addItemBtn, ticketsBtn, debtorsBtn;

    public AdminLayout(Admin admin,
            ItemRepo itemRepo,
            ItemService itemService,
            TicketService ticketService,
            WalletService walletService,
            DebtService debtService,
            UserService userService,
            ReservationService reservationService,
            NotificationService notificationService,
            Runnable onLogout) {
        this.admin = admin;
        this.itemRepo = itemRepo;
        this.itemService = itemService;
        this.ticketService = ticketService;
        this.walletService = walletService;
        this.debtService = debtService;
        this.userService = userService;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.onLogout = onLogout;

        buildUI();
    }

    private void buildUI() {
        setTop(buildTopBar());
        setLeft(buildSidebar());
        showDashboard();
    }

    public void pushView(Node view) {
        viewStack.push(view);
        setCenter(view);
    }

    public void popView() {
        if (viewStack.size() > 1) {
            viewStack.pop();
            setCenter(viewStack.peek());
        }
    }

    private void switchTo(Node view, Button btn) {
        viewStack.clear();
        viewStack.push(view);
        setCenter(view);
        navButtons.forEach(b -> b.getStyleClass().remove("active-nav"));
        btn.getStyleClass().add("active-nav");
    }

    private void showDashboard() {
        if (dashboardView == null) {
            dashboardView = new AdminDashboardView(admin, ticketService);
        }
        switchTo(dashboardView, dashboardBtn);
    }

    private void showCatalog() {
        if (catalogView == null) {
            catalogView = new CatalogView(itemRepo, null);
        }
        switchTo(catalogView, catalogBtn);
    }

    private void showAddItem() {
        if (addItemView == null) {
            addItemView = new AdminAddItemView(admin, itemService, itemRepo, () -> {
                if (catalogView != null)
                    catalogView.reload();
            });
        }
        switchTo(addItemView, addItemBtn);
    }

    private void showTickets() {
        if (ticketsView == null) {
            ticketsView = new AdminTicketsView(admin, ticketService, walletService,
                    debtService, userService, reservationService,
                    ticket -> pushView(new TicketChatView(ticket, admin, ticketService, this::popView)));
        }
        switchTo(ticketsView, ticketsBtn);
    }

    private void showDebtors() {
        if (debtorsView == null) {
            debtorsView = new AdminDebtorsView(admin, debtService, userService);
        }
        switchTo(debtorsView, debtorsBtn);
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(20);
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("Stardew Library — Admin");
        logo.getStyleClass().add("logo-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        NotificationBell bell = new NotificationBell(admin, notificationService);

        Button themeBtn = new Button("Toggle Mode");
        themeBtn.getStyleClass().add("theme-toggle-button");
        themeBtn.setOnAction(e -> toggleTheme());

        bar.getChildren().addAll(logo, spacer, bell, themeBtn);
        return bar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("right-sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);

        Label greeting = new Label("Admin: " + admin.getUserName());
        greeting.getStyleClass().add("sidebar-greeting");

        dashboardBtn = navBtn("Dashboard", e -> showDashboard());
        catalogBtn = navBtn("Catalog", e -> showCatalog());
        addItemBtn = navBtn("Add Item", e -> showAddItem());
        ticketsBtn = navBtn("Tickets", e -> showTickets());
        debtorsBtn = navBtn("Debtors", e -> showDebtors());

        navButtons.addAll(List.of(dashboardBtn, catalogBtn, addItemBtn, ticketsBtn, debtorsBtn));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Log Out");
        logoutBtn.getStyleClass().add("oval-logout-button");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> onLogout.run());

        sidebar.getChildren().addAll(
                greeting, dashboardBtn, catalogBtn, addItemBtn, ticketsBtn, debtorsBtn,
                spacer, logoutBtn);
        return sidebar;
    }

    private Button navBtn(String label, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(label);
        btn.getStyleClass().add("sidebar-nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(handler);
        return btn;
    }

    private void toggleTheme() {
        if (getScene() != null) {
            boolean isDark = getScene().getRoot().getStyleClass().contains("dark-mode");
            if (isDark)
                getScene().getRoot().getStyleClass().remove("dark-mode");
            else
                getScene().getRoot().getStyleClass().add("dark-mode");
        }
    }
}
