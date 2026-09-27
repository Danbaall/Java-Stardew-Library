package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.enums.Role;
import ir.ac.kntu.entities.enums.TicketType;
import ir.ac.kntu.entities.module.*;
import ir.ac.kntu.repo.*;
import ir.ac.kntu.services.ManagerService;
import ir.ac.kntu.util.AsyncExecutor;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ManagerLayout extends BorderPane {

    private final User manager;
    private final UserRepo userRepo;
    private final ItemRepo itemRepo;
    private final BorrowHistoryRepo borrowHistoryRepo;
    private final TransactionRepo transactionRepo;
    private final ManagerService managerService;
    private final Runnable onLogout;

    private final StackPane content = new StackPane();
    private final Map<String, Button> navigationButtons = new LinkedHashMap<>();
    private Label totalUsersLabel;
    private Label totalItemsLabel;
    private Label totalIncomeLabel;
    private Label totalBorrowsLabel;
    private BarChart<String, Number> borrowedItemsChart;
    private BarChart<String, Number> incomeChart;
    private ListView<User> userListView;
    private ListView<Admin> adminListView;

    private TextField fineRateField, pickupDaysField, maxReserveDaysField;
    private TextField studentBorrowDaysField, professorBorrowDaysField, guestBorrowDaysField;
    private TextField studentBorrowLimitField, professorBorrowLimitField, guestBorrowLimitField;
    private TextField studentReservationsField, professorReservationsField, guestReservationsField;

    public ManagerLayout(
            User manager,
            UserRepo userRepo,
            ItemRepo itemRepo,
            BorrowHistoryRepo borrowHistoryRepo,
            TransactionRepo transactionRepo,
            ManagerService managerService,
            Runnable onLogout) {
        this.manager = manager;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.borrowHistoryRepo = borrowHistoryRepo;
        this.transactionRepo = transactionRepo;
        this.managerService = managerService;
        this.onLogout = onLogout;

        getStyleClass().add("content-view");
        setTop(createTopBar());
        setLeft(createSidebar());
        setCenter(content);
        showPage("Overview", createOverviewPage());
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");
        Label logo = new Label("Stardew Library");
        logo.getStyleClass().add("logo-text");
        Label workspace = new Label("Manager workspace");
        workspace.getStyleClass().add("manager-topbar-subtitle");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button refresh = new Button("Refresh data");
        refresh.getStyleClass().add("manager-secondary-button");
        refresh.setOnAction(event -> refreshStats());
        Button theme = new Button("Toggle mode");
        theme.getStyleClass().add("theme-toggle-button");
        theme.setOnAction(event -> toggleTheme());
        topBar.getChildren().addAll(logo, workspace, spacer, refresh, theme);
        return topBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("right-sidebar");
        sidebar.setPadding(new Insets(22, 14, 18, 14));
        sidebar.setPrefWidth(220);
        Label greeting = new Label("Hi, " + manager.getUserName());
        greeting.getStyleClass().add("sidebar-greeting");
        Label role = new Label("Library manager");
        role.getStyleClass().add("manager-sidebar-role");
        VBox navigation = new VBox(5);
        navigation.setPadding(new Insets(18, 0, 0, 0));
        addNavigation(navigation, "Overview", this::createOverviewPage);
        addNavigation(navigation, "Users", this::createUserTab);
        addNavigation(navigation, "Administrators", this::createAdminTab);
        addNavigation(navigation, "Library policy", this::createPolicyTab);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button logout = new Button("Log out");
        logout.getStyleClass().add("oval-logout-button");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(event -> onLogout.run());
        sidebar.getChildren().addAll(greeting, role, navigation, spacer, logout);
        return sidebar;
    }

    private void addNavigation(VBox navigation, String name, Supplier<Node> pageFactory) {
        Button button = new Button(name);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("sidebar-nav-btn");
        button.setOnAction(event -> showPage(name, pageFactory.get()));
        navigationButtons.put(name, button);
        navigation.getChildren().add(button);
    }

    private void showPage(String name, Node page) {
        navigationButtons.values().forEach(button -> button.getStyleClass().remove("active-nav"));
        navigationButtons.get(name).getStyleClass().add("active-nav");
        content.getChildren().setAll(page);
    }

    private VBox page(String title, String description) {
        Label heading = new Label(title);
        heading.getStyleClass().add("manager-page-title");
        Label subtitle = new Label(description);
        subtitle.getStyleClass().add("manager-page-subtitle");
        VBox page = new VBox(18, heading, subtitle);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("manager-page");
        return page;
    }

    private Node createOverviewPage() {
        VBox page = page("Overview", "A snapshot of library activity and revenue.");
        totalUsersLabel = statValue();
        totalItemsLabel = statValue();
        totalIncomeLabel = statValue();
        totalBorrowsLabel = statValue();
        HBox cards = new HBox(16,
                statCard("Registered users", totalUsersLabel),
                statCard("Catalog items", totalItemsLabel),
                statCard("Charge income", totalIncomeLabel),
                statCard("All-time borrows", totalBorrowsLabel));
        borrowedItemsChart = createChart("Most borrowed items", "Borrows");
        incomeChart = createChart("Charge income — last six months", "Amount ($)");
        VBox borrowedCard = chartCard(borrowedItemsChart);
        VBox incomeCard = chartCard(incomeChart);
        HBox.setHgrow(borrowedCard, Priority.ALWAYS);
        HBox.setHgrow(incomeCard, Priority.ALWAYS);
        HBox charts = new HBox(16, borrowedCard, incomeCard);
        VBox.setVgrow(charts, Priority.ALWAYS);
        page.getChildren().addAll(cards, charts);
        refreshStats();
        return page;
    }

    private Label statValue() {
        Label label = new Label("—");
        label.getStyleClass().add("manager-stat-value");
        return label;
    }

    private VBox statCard(String caption, Label value) {
        Label label = new Label(caption);
        label.getStyleClass().add("manager-stat-caption");
        VBox card = new VBox(7, value, label);
        card.setPrefWidth(190);
        card.setMinHeight(105);
        card.getStyleClass().add("manager-stat-card");
        return card;
    }

    private BarChart<String, Number> createChart(String title, String axisLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(axisLabel);
        yAxis.setMinorTickVisible(false);
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(320);
        chart.setMinWidth(360);
        chart.getStyleClass().add("manager-chart");
        return chart;
    }

    private VBox chartCard(BarChart<String, Number> chart) {
        VBox card = new VBox(chart);
        card.getStyleClass().add("manager-chart-card");
        VBox.setVgrow(chart, Priority.ALWAYS);
        return card;
    }

    private VBox createUserTab() {
        VBox box = page("User management", "Review account details and manage access.");
        userListView = new ListView<>();
        userListView.setPlaceholder(new Label("No users found."));
        userListView.getStyleClass().add("borrow-list");
        userListView.setCellFactory(lv -> new UserCell());
        VBox.setVgrow(userListView, Priority.ALWAYS);
        box.getChildren().add(userListView);
        refreshUserList();
        return box;
    }

    private void refreshUserList() {
        AsyncExecutor.execute(
                () -> managerService.listUsers(
                        manager,
                        new UserFilter(null, null, null)),
                users -> {
                    users = users
                            .stream()
                            .filter(u -> !u.getId().equals(manager.getId()))
                            .collect(Collectors.toList());
                    userListView.setItems(FXCollections.observableArrayList(users));
                },
                error -> showAlert("Failed to load users: " + error.getMessage()));
    }

    private class UserCell extends ListCell<User> {

        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);
            if (empty || user == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5));
            row.getStyleClass().add("borrow-row");

            Label idLabel = new Label(user.getId());
            idLabel.getStyleClass().add("borrow-row-label");
            Label nameLabel = new Label(user.getUserName());
            nameLabel.getStyleClass().add("borrow-row-label");
            Label roleLabel = new Label(user.getRole().toString());
            roleLabel.getStyleClass().add("borrow-row-label");
            Label activeLabel = new Label(
                    user.isActive() ? "Active" : "Disabled");
            activeLabel.getStyleClass().add("borrow-row-label");

            row.getChildren().addAll(
                    idLabel,
                    nameLabel,
                    roleLabel,
                    activeLabel);

            // Action buttons
            Button editBtn = new Button("Edit");
            editBtn.setStyle(
                    "-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
            editBtn.setOnAction(e -> showEditUserDialog(user));
            Button disableBtn = new Button(
                    user.isActive() ? "Disable" : "Enable");
            disableBtn.setStyle(
                    "-fx-background-color: #e67e22; -fx-text-fill: white; -fx-cursor: hand;");
            disableBtn.setOnAction(e -> toggleUserActive(user, !user.isActive()));
            Button passwordBtn = new Button("Reset PW");
            passwordBtn.setStyle(
                    "-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-cursor: hand;");
            passwordBtn.setOnAction(e -> showPasswordResetDialog(user));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(spacer, editBtn, disableBtn, passwordBtn);

            setGraphic(row);
            setText(null);
        }
    }

    private void showEditUserDialog(User user) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        styleFormDialog(dialog);
        dialog.setHeaderText(
                "Editing " + user.getUserName() + " (" + user.getId() + ")");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField(user.getUserName());
        usernameField.setPromptText("Username");
        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Email address");
        TextField phoneField = new TextField(user.getPhoneNumber());
        phoneField.setPromptText("Phone number");
        usernameField.setPrefWidth(300);

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog
                .getDialogPane()
                .getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    managerService.editUserField(
                            manager,
                            user.getId(),
                            "username",
                            usernameField.getText().trim());
                    managerService.editUserField(
                            manager,
                            user.getId(),
                            "email",
                            emailField.getText().trim());
                    managerService.editUserField(
                            manager,
                            user.getId(),
                            "phone",
                            phoneField.getText().trim());
                    refreshUserList();
                } catch (Exception ex) {
                    showAlert(ex.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void toggleUserActive(User user, boolean newActive) {
        managerService.setUserActive(manager, user.getId(), newActive);
        refreshUserList();
    }

    private void showPasswordResetDialog(User user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.getDialogPane().setMinWidth(480);
        dialog.getEditor().setPromptText("Strong password");
        dialog.setHeaderText("Enter new password for " + user.getUserName());
        dialog.setContentText("New password:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            try {
                managerService.recoverPassword(manager, user.getId(), password);
                showAlert("Password reset successfully.");
            } catch (Exception ex) {
                showAlert(ex.getMessage());
            }
        });
    }

    private VBox createAdminTab() {
        VBox box = page("Administrator management", "Create administrators and assign ticket responsibilities.");
        Button createAdminBtn = new Button("Create administrator");
        createAdminBtn.getStyleClass().add("auth-submit-btn");
        createAdminBtn.setOnAction(e -> showCreateAdminDialog());
        adminListView = new ListView<>();
        adminListView.setPlaceholder(new Label("No administrators found."));
        adminListView.getStyleClass().add("borrow-list");
        adminListView.setCellFactory(lv -> new AdminCell());
        VBox.setVgrow(adminListView, Priority.ALWAYS);
        box.getChildren().addAll(createAdminBtn, adminListView);
        refreshAdminList();
        return box;
    }

    private void refreshAdminList() {
        AsyncExecutor.execute(
                () -> managerService.listUsers(
                        manager,
                        new UserFilter(null, null, Role.ADMIN)),
                users -> {
                    List<Admin> admins = users
                            .stream()
                            .filter(u -> u.getRole() == Role.ADMIN)
                            .map(u -> (Admin) u)
                            .collect(Collectors.toList());
                    adminListView.setItems(
                            FXCollections.observableArrayList(admins));
                },
                error -> showAlert("Failed to load admins: " + error.getMessage()));
    }

    private class AdminCell extends ListCell<Admin> {

        @Override
        protected void updateItem(Admin admin, boolean empty) {
            super.updateItem(admin, empty);
            if (empty || admin == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5));
            row.getStyleClass().add("borrow-row");

            Label idLabel = new Label(admin.getId());
            idLabel.getStyleClass().add("borrow-row-label");
            Label nameLabel = new Label(admin.getUserName());
            nameLabel.getStyleClass().add("borrow-row-label");
            Label typesLabel = new Label("Types: " + admin.getAssignedTypes());
            typesLabel.getStyleClass().add("borrow-row-label");

            row.getChildren().addAll(idLabel, nameLabel, typesLabel);

            Button removeBtn = new Button("Remove");
            removeBtn.setStyle(
                    "-fx-background-color: #e74c3c; -fx-text-fill: white;");
            removeBtn.setOnAction(e -> removeAdmin(admin));
            Button assignTypesBtn = new Button("Assign Types");
            assignTypesBtn.setStyle(
                    "-fx-background-color: #3498db; -fx-text-fill: white;");
            assignTypesBtn.setOnAction(e -> showAssignTicketTypesDialog(admin));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(spacer, removeBtn, assignTypesBtn);

            setGraphic(row);
            setText(null);
        }
    }

    private void showCreateAdminDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Create Admin");
        styleFormDialog(dialog);
        dialog.setHeaderText("Enter admin details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Strong password");
        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone number");
        usernameField.setPrefWidth(300);

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog
                .getDialogPane()
                .getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String credentials = passwordField.getText() +
                        "::" +
                        emailField.getText() +
                        "::" +
                        phoneField.getText();
                try {
                    managerService.createAdmin(
                            manager,
                            usernameField.getText().trim(),
                            credentials);
                    refreshAdminList();
                } catch (Exception ex) {
                    showAlert(ex.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void removeAdmin(Admin admin) {
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Remove admin " + admin.getUserName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                managerService.removeAdmin(manager, admin.getId());
                refreshAdminList();
            }
        });
    }

    private void showAssignTicketTypesDialog(Admin admin) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assign Ticket Types");
        dialog.setHeaderText("Select types for " + admin.getUserName());

        VBox checkBoxContainer = new VBox(5);
        Map<TicketType, CheckBox> checkBoxMap = new HashMap<>();
        for (TicketType type : TicketType.values()) {
            CheckBox cb = new CheckBox(type.name());
            cb.setSelected(admin.getAssignedTypes().contains(type));
            checkBoxMap.put(type, cb);
            checkBoxContainer.getChildren().add(cb);
        }

        dialog.getDialogPane().setContent(checkBoxContainer);
        dialog
                .getDialogPane()
                .getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                List<TicketType> selected = new ArrayList<>();
                for (Map.Entry<TicketType, CheckBox> entry : checkBoxMap.entrySet()) {
                    if (entry.getValue().isSelected())
                        selected.add(
                                entry.getKey());
                }
                managerService.assignTicketTypes(
                        manager,
                        admin.getId(),
                        selected);
                refreshAdminList();
            }
            return null;
        });
        dialog.showAndWait();
    }

    private VBox createPolicyTab() {
        VBox box = page("Library policy", "Configure lending and reservation rules. Values cannot be negative.");
        LibraryPolicy policy = managerService.getPolicy(manager);
        fineRateField = policyField(policy.getFineRatePerDay());
        pickupDaysField = policyField(policy.getReservationPickupDays());
        maxReserveDaysField = policyField(policy.getMaxReserveDays());
        studentBorrowDaysField = policyField(policy.getStudentMaxBorrowDays());
        professorBorrowDaysField = policyField(policy.getProfessorMaxBorrowDays());
        guestBorrowDaysField = policyField(policy.getGuestMaxBorrowDays());
        studentBorrowLimitField = policyField(policy.getStudentMaxBorrowLimit());
        professorBorrowLimitField = policyField(policy.getProfessorMaxBorrowLimit());
        guestBorrowLimitField = policyField(policy.getGuestMaxBorrowLimit());
        studentReservationsField = policyField(policy.getStudentMaxReservations());
        professorReservationsField = policyField(policy.getProfessorMaxReservations());
        guestReservationsField = policyField(policy.getGuestMaxReservations());

        GridPane form = new GridPane();
        form.setHgap(18);
        form.setVgap(12);
        form.setMaxWidth(760);
        ColumnConstraints labels = new ColumnConstraints(280);
        ColumnConstraints fields = new ColumnConstraints();
        fields.setHgrow(Priority.ALWAYS);
        fields.setFillWidth(true);
        form.getColumnConstraints().addAll(labels, fields);
        int row = 0;
        row = addPolicySection(form, row, "General settings");
        addPolicyField(form, row++, "Fine per day ($)", fineRateField);
        addPolicyField(form, row++, "Reservation pickup window (days)", pickupDaysField);
        addPolicyField(form, row++, "Maximum reservation length (days)", maxReserveDaysField);
        row = addPolicySection(form, row, "Loan periods");
        addPolicyField(form, row++, "Student loan period", studentBorrowDaysField);
        addPolicyField(form, row++, "Professor loan period", professorBorrowDaysField);
        addPolicyField(form, row++, "Guest loan period", guestBorrowDaysField);
        row = addPolicySection(form, row, "Borrowing limits");
        addPolicyField(form, row++, "Student item limit", studentBorrowLimitField);
        addPolicyField(form, row++, "Professor item limit", professorBorrowLimitField);
        addPolicyField(form, row++, "Guest item limit", guestBorrowLimitField);
        row = addPolicySection(form, row, "Reservation limits");
        addPolicyField(form, row++, "Student reservations", studentReservationsField);
        addPolicyField(form, row++, "Professor reservations", professorReservationsField);
        addPolicyField(form, row++, "Guest reservations", guestReservationsField);

        VBox formCard = new VBox(form);
        formCard.getStyleClass().add("manager-form-card");
        formCard.setMaxWidth(800);
        ScrollPane scroll = new ScrollPane(formCard);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("edge-to-edge");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        Button save = new Button("Save policy changes");
        save.getStyleClass().add("auth-submit-btn");
        save.setOnAction(event -> savePolicy());
        HBox actions = new HBox(save);
        actions.setAlignment(Pos.CENTER_RIGHT);
        box.getChildren().addAll(scroll, actions);
        return box;
    }

    private TextField policyField(Number value) {
        TextField field = new TextField(String.valueOf(value));
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private int addPolicySection(GridPane form, int row, String title) {
        Label heading = new Label(title);
        heading.getStyleClass().add("manager-form-section");
        form.add(heading, 0, row, 2, 1);
        return row + 1;
    }

    private void addPolicyField(GridPane form, int row, String title, TextField field) {
        Label label = new Label(title);
        label.getStyleClass().add("item-detail-label");
        form.add(label, 0, row);
        form.add(field, 1, row);
    }

    private void savePolicy() {
        LibraryPolicy updated = new LibraryPolicy();
        try {
            updated.setFineRatePerDay(parseNonNegativeDouble(fineRateField, "Fine per day"));
            updated.setReservationPickupDays(parseNonNegativeInt(pickupDaysField, "Reservation pickup window"));
            updated.setMaxReserveDays(parseNonNegativeInt(maxReserveDaysField, "Maximum reservation length"));
            updated.setStudentMaxBorrowDays(parseNonNegativeInt(studentBorrowDaysField, "Student loan period"));
            updated.setProfessorMaxBorrowDays(parseNonNegativeInt(professorBorrowDaysField, "Professor loan period"));
            updated.setGuestMaxBorrowDays(parseNonNegativeInt(guestBorrowDaysField, "Guest loan period"));
            updated.setStudentMaxBorrowLimit(parseNonNegativeInt(studentBorrowLimitField, "Student item limit"));
            updated.setProfessorMaxBorrowLimit(parseNonNegativeInt(professorBorrowLimitField, "Professor item limit"));
            updated.setGuestMaxBorrowLimit(parseNonNegativeInt(guestBorrowLimitField, "Guest item limit"));
            updated.setStudentMaxReservations(parseNonNegativeInt(studentReservationsField, "Student reservations"));
            updated.setProfessorMaxReservations(
                    parseNonNegativeInt(professorReservationsField, "Professor reservations"));
            updated.setGuestMaxReservations(parseNonNegativeInt(guestReservationsField, "Guest reservations"));
            managerService.updatePolicy(manager, updated);
            showAlert("Policy updated successfully.");
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private int parseNonNegativeInt(TextField field, String name) {
        int value = Integer.parseInt(field.getText().trim());
        if (value < 0)
            throw new IllegalArgumentException(name + " cannot be negative.");
        return value;
    }

    private double parseNonNegativeDouble(TextField field, String name) {
        double value = Double.parseDouble(field.getText().trim());
        if (value < 0)
            throw new IllegalArgumentException(name + " cannot be negative.");
        return value;
    }

    private void refreshStats() {
        if (borrowedItemsChart == null || incomeChart == null)
            return;
        AsyncExecutor.execute(() -> {
            List<BorrowRecord> borrows = borrowHistoryRepo.findAll();
            List<Transaction> transactions = transactionRepo.findAll();
            Map<String, Long> counts = borrows.stream().collect(
                    Collectors.groupingBy(BorrowRecord::getItemId, Collectors.counting()));
            List<ChartEntry> popular = counts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(entry -> new ChartEntry(itemTitle(entry.getKey()), entry.getValue().doubleValue()))
                    .toList();
            double totalIncome = transactions.stream()
                    .filter(transaction -> transaction.type() == ir.ac.kntu.entities.enums.TransactionType.CHARGE)
                    .mapToDouble(Transaction::amount).sum();
            Map<YearMonth, Double> incomeByMonth = transactions.stream()
                    .filter(transaction -> transaction.type() == ir.ac.kntu.entities.enums.TransactionType.CHARGE)
                    .filter(transaction -> transaction.timestamp() != null)
                    .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.timestamp()),
                            Collectors.summingDouble(Transaction::amount)));
            List<ChartEntry> monthlyIncome = new ArrayList<>();
            YearMonth currentMonth = YearMonth.now();
            for (int offset = 5; offset >= 0; offset--) {
                YearMonth month = currentMonth.minusMonths(offset);
                monthlyIncome.add(new ChartEntry(month.format(DateTimeFormatter.ofPattern("MMM yy")),
                        incomeByMonth.getOrDefault(month, 0.0)));
            }
            return new Stats(userRepo.findAll().size(), itemRepo.findAll().size(), totalIncome,
                    borrows.size(), popular, monthlyIncome);
        }, stats -> {
            totalUsersLabel.setText(String.valueOf(stats.totalUsers()));
            totalItemsLabel.setText(String.valueOf(stats.totalItems()));
            totalIncomeLabel.setText(String.format("$%,.2f", stats.totalIncome()));
            totalBorrowsLabel.setText(String.valueOf(stats.totalBorrows()));
            populateChart(borrowedItemsChart, "Borrow count", stats.popularItems());
            populateChart(incomeChart, "Charge income", stats.monthlyIncome());
        }, error -> showAlert("Failed to refresh dashboard: " + error.getMessage()));
    }

    private String itemTitle(String itemId) {
        Item item = itemRepo.findById(itemId);
        String title = item == null ? itemId : item.getTitle();
        if (title == null || title.isBlank())
            return "Untitled";
        return title.length() <= 18 ? title : title.substring(0, 17) + "…";
    }

    private void populateChart(BarChart<String, Number> chart, String name, List<ChartEntry> entries) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        entries.forEach(entry -> series.getData().add(new XYChart.Data<>(entry.label(), entry.value())));
        chart.getData().add(series);
    }

    private void styleFormDialog(Dialog<?> dialog) {
        dialog.getDialogPane().setMinWidth(500);
        dialog.getDialogPane().setPadding(new Insets(18));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                msg,
                ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void toggleTheme() {
        if (getScene() != null) {
            boolean isDark = getScene()
                    .getRoot()
                    .getStyleClass()
                    .contains("dark-mode");
            if (isDark)
                getScene()
                        .getRoot()
                        .getStyleClass()
                        .remove("dark-mode");
            else
                getScene().getRoot().getStyleClass().add("dark-mode");
        }
    }

    private record ChartEntry(String label, double value) {
    }

    private record Stats(int totalUsers, int totalItems, double totalIncome,
            int totalBorrows, List<ChartEntry> popularItems,
            List<ChartEntry> monthlyIncome) {
    }
}
