package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.module.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.util.*;
import ir.ac.kntu.services.NotificationService;
import ir.ac.kntu.gui.components.NotificationBell;

public class MainLayout extends BorderPane {

    private final User currentUser;
    private final Runnable onLogout;
    private final Stack<Node> viewStack = new Stack<>();

    // Views that need refresh
    private Node catalogView;
    private MyBorrowsView myBorrowsView;
    private WalletView walletView;
    private TicketListView ticketListView;
    private ProfileView profileView;
    private MyReservationsView reservationsView;

    private final NotificationService notificationService;
    private final List<Button> navButtons = new ArrayList<>();
    private Button catalogNavBtn, myBorrowsBtn, walletBtn, ticketsBtn;

    public MainLayout(User user, Node catalogView, MyBorrowsView myBorrowsView,
            WalletView walletView, TicketListView ticketListView, ProfileView profileView,
            MyReservationsView reservationsView, Runnable onLogout, NotificationService notificationService) {
        this.currentUser = user;
        this.catalogView = catalogView;
        this.myBorrowsView = myBorrowsView;
        this.walletView = walletView;
        this.ticketListView = ticketListView;
        this.profileView = profileView;
        this.reservationsView = reservationsView;
        this.onLogout = onLogout;
        this.notificationService = notificationService;

        setTop(createTopBar());
        setLeft(createRightSidebar());

        switchMainView(catalogView, catalogNavBtn);
    }

    public void switchMainView(Node view, Button sourceBtn) {
        viewStack.clear();
        viewStack.push(view);
        setCenter(view);

        // Refresh
        if (view == myBorrowsView) {
            myBorrowsView.refreshData();
        } else if (view == walletView) {
            walletView.refresh();
        }

        if (view == reservationsView) {
            reservationsView.loadReservations();
        } else if (view == profileView) {
            // no refresh needed
        }

        // Update active button style
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("active-nav");
        }
        sourceBtn.getStyleClass().add("active-nav");
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

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label logoLabel = new Label("Stardew Library");
        logoLabel.getStyleClass().add("logo-text");

        Button themeButton = new Button("Toggle Mode");
        themeButton.getStyleClass().add("theme-toggle-button");
        themeButton.setOnAction(e -> toggleTheme());

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        NotificationBell notifBell = new NotificationBell(currentUser, notificationService);

        topBar.getChildren().addAll(logoLabel, topSpacer, notifBell, themeButton);
        return topBar;
    }

    private VBox createRightSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("right-sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);

        Label userGreeting = new Label("Hi, " + currentUser.getUserName());
        userGreeting.getStyleClass().add("sidebar-greeting");

        catalogNavBtn = new Button("Catalog");
        catalogNavBtn.getStyleClass().addAll("sidebar-nav-btn");
        catalogNavBtn.setMaxWidth(Double.MAX_VALUE);
        catalogNavBtn.setOnAction(e -> switchMainView(catalogView, catalogNavBtn));

        myBorrowsBtn = new Button("My Borrows");
        myBorrowsBtn.getStyleClass().add("sidebar-nav-btn");
        myBorrowsBtn.setMaxWidth(Double.MAX_VALUE);
        myBorrowsBtn.setOnAction(e -> switchMainView(myBorrowsView, myBorrowsBtn));

        walletBtn = new Button("Wallet & Fines");
        walletBtn.getStyleClass().add("sidebar-nav-btn");
        walletBtn.setMaxWidth(Double.MAX_VALUE);
        walletBtn.setOnAction(e -> switchMainView(walletView, walletBtn));

        ticketsBtn = new Button("Support Tickets");
        ticketsBtn.getStyleClass().add("sidebar-nav-btn");
        ticketsBtn.setMaxWidth(Double.MAX_VALUE);
        ticketsBtn.setOnAction(e -> switchMainView(ticketListView, ticketsBtn));
        navButtons.add(ticketsBtn);

        Button profileBtn = new Button("Profile Settings");
        profileBtn.getStyleClass().add("sidebar-nav-btn");
        profileBtn.setMaxWidth(Double.MAX_VALUE);
        profileBtn.setOnAction(e -> switchMainView(profileView, profileBtn));

        Button reservationsBtn = new Button("My Reservations");
        reservationsBtn.getStyleClass().add("sidebar-nav-btn");
        reservationsBtn.setMaxWidth(Double.MAX_VALUE);
        reservationsBtn.setOnAction(e -> switchMainView(reservationsView, reservationsBtn));

        navButtons
                .addAll(Arrays.asList(catalogNavBtn, myBorrowsBtn, walletBtn, ticketsBtn, profileBtn, reservationsBtn));

        VBox navList = new VBox(10, userGreeting, catalogNavBtn, myBorrowsBtn, walletBtn, ticketsBtn, reservationsBtn,
                profileBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Log Out");
        logoutBtn.getStyleClass().add("oval-logout-button");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> onLogout.run());

        sidebar.getChildren().addAll(navList, spacer, logoutBtn);
        return sidebar;
    }

    private void toggleTheme() {
        if (getScene() != null) {
            boolean isDark = getScene().getRoot().getStyleClass().contains("dark-mode");
            if (isDark) {
                getScene().getRoot().getStyleClass().remove("dark-mode");
            } else {
                getScene().getRoot().getStyleClass().add("dark-mode");
            }
        }
    }
}