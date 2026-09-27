package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.module.BorrowRecord;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.services.DebtService;
import ir.ac.kntu.services.UserService;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminDebtorsView extends BorderPane {

    private final User admin;
    private final DebtService debtService;

    private ListView<DebtorInfo> listView;
    private Label summaryLabel;

    private List<DebtorInfo> allDebtors;

    // Filter controls
    private TextField minDebtField;
    private TextField maxDebtField;
    private DatePicker overdueSincePicker;
    private ComboBox<String> sortBox;

    public AdminDebtorsView(User admin, DebtService debtService, UserService userService) {
        this.admin = admin;
        this.debtService = debtService;
        getStyleClass().add("content-view");
        buildUI();
        loadDebtors();
    }

    private void buildUI() {
        // Top bar
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15));
        topBar.getStyleClass().add("top-bar");

        Label title = new Label("Debtors");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("item-detail-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        summaryLabel = new Label();
        summaryLabel.getStyleClass().add("item-detail-label");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #6a89a7; -fx-text-fill: white; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadDebtors());

        topBar.getChildren().addAll(title, spacer, summaryLabel, refreshBtn);
        setTop(topBar);

        // Filter bar
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(10, 15, 10, 15));
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("catalog-search-bar");

        minDebtField = filterField("Min debt ($)", 90);
        minDebtField.textProperty().addListener((obs, o, n) -> applyFilter());

        maxDebtField = filterField("Max debt ($)", 90);
        maxDebtField.textProperty().addListener((obs, o, n) -> applyFilter());

        overdueSincePicker = new DatePicker();
        overdueSincePicker.setPromptText("Overdue since before…");
        overdueSincePicker.setPrefWidth(170);
        overdueSincePicker.setOnAction(e -> applyFilter());

        sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Highest Debt", "Lowest Debt", "Name A-Z", "Oldest Overdue");
        sortBox.setValue("Highest Debt");
        sortBox.getStyleClass().add("catalog-filter-combo");
        sortBox.setOnAction(e -> applyFilter());

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("notif-action-btn");
        clearBtn.setOnAction(e -> {
            minDebtField.clear();
            maxDebtField.clear();
            overdueSincePicker.setValue(null);
            sortBox.setValue("Highest Debt");
        });

        filterBar.getChildren().addAll(
                filterLabel("Debt:"), minDebtField, filterLabel("-"), maxDebtField,
                filterLabel("  Overdue since:"), overdueSincePicker,
                filterLabel("  Sort:"), sortBox,
                clearBtn);

        listView = new ListView<>();
        listView.setCellFactory(lv -> new DebtorCell());
        listView.getStyleClass().add("borrow-list");

        VBox center = new VBox(filterBar, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        setCenter(center);
    }

    private TextField filterField(String prompt, double width) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(width);
        tf.getStyleClass().add("catalog-search-field");
        return tf;
    }

    private Label filterLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("item-detail-label");
        lbl.setStyle("-fx-font-size: 12px;");
        return lbl;
    }

    private void loadDebtors() {
        AsyncExecutor.execute(
                () -> {
                    List<User> users = debtService.getUsersWithDebt(admin).getData();
                    return users.stream()
                            .map(u -> {
                                double debt = debtService.calculateUserDebtRaw(u.getId());
                                List<BorrowRecord> unpaid = debtService.getUnpaidDebtRecords(u.getId());
                                LocalDate oldest = unpaid.stream()
                                        .map(BorrowRecord::getDueDate)
                                        .min(LocalDate::compareTo)
                                        .orElse(null);
                                return new DebtorInfo(u, debt, oldest);
                            })
                            .collect(Collectors.toList());
                },
                infos -> {
                    allDebtors = infos;
                    applyFilter();
                },
                err -> showAlert("Error loading debtors: " + err.getMessage()));
    }

    private void applyFilter() {
        if (allDebtors == null)
            return;

        double minDebt = parseDouble(minDebtField.getText(), 0.0);
        double maxDebt = parseDouble(maxDebtField.getText(), Double.MAX_VALUE);
        LocalDate overdueBefore = overdueSincePicker.getValue();

        Comparator<DebtorInfo> comparator = switch (sortBox.getValue()) {
            case "Lowest Debt" -> Comparator.comparingDouble(DebtorInfo::debtAmount);
            case "Name A-Z" -> Comparator.comparing(d -> d.user().getUserName(),
                    String.CASE_INSENSITIVE_ORDER);
            case "Oldest Overdue" -> Comparator.comparing(
                    d -> d.oldestDueDate() != null ? d.oldestDueDate() : LocalDate.MAX,
                    Comparator.naturalOrder());
            default -> Comparator.comparingDouble(DebtorInfo::debtAmount).reversed();
        };

        List<DebtorInfo> filtered = allDebtors.stream()
                .filter(d -> d.debtAmount() >= minDebt)
                .filter(d -> d.debtAmount() <= maxDebt)
                .filter(d -> overdueBefore == null
                        || (d.oldestDueDate() != null && d.oldestDueDate().isBefore(overdueBefore)))
                .sorted(comparator)
                .collect(Collectors.toList());

        listView.setItems(FXCollections.observableArrayList(filtered));
        summaryLabel.setText(filtered.size() + " / " + allDebtors.size() + " debtor(s)");
    }

    private double parseDouble(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private record DebtorInfo(User user, double debtAmount, LocalDate oldestDueDate) {
    }

    private class DebtorCell extends ListCell<DebtorInfo> {
        @Override
        protected void updateItem(DebtorInfo info, boolean empty) {
            super.updateItem(info, empty);
            if (empty || info == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            User u = info.user();
            HBox row = new HBox(20);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8));
            row.getStyleClass().add("borrow-row");

            Label nameLabel = new Label(u.getUserName());
            nameLabel.getStyleClass().add("borrow-row-label");
            nameLabel.setPrefWidth(150);

            Label emailLabel = new Label(u.getEmail() != null ? u.getEmail() : "—");
            emailLabel.getStyleClass().add("borrow-row-label");
            emailLabel.setPrefWidth(190);

            Label roleLabel = new Label(u.getRole().toString());
            roleLabel.getStyleClass().add("borrow-row-label");
            roleLabel.setPrefWidth(90);

            Label debtLabel = new Label(String.format("$%.2f", info.debtAmount()));
            debtLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            debtLabel.setPrefWidth(80);

            String sinceText = info.oldestDueDate() != null
                    ? "overdue since " + info.oldestDueDate()
                    : "";
            Label sinceLabel = new Label(sinceText);
            sinceLabel.getStyleClass().add("admin-meta-label");

            row.getChildren().addAll(nameLabel, emailLabel, roleLabel, debtLabel, sinceLabel);
            setGraphic(row);
            setText(null);
        }
    }
}
