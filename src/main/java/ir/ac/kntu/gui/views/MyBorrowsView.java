package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.enums.BorrowStatus;
import ir.ac.kntu.entities.module.*;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.services.BorrowService;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import ir.ac.kntu.services.NotificationService;
import ir.ac.kntu.entities.enums.NotificationType;

public class MyBorrowsView extends BorderPane {

    private final User currentUser;
    private final BorrowService borrowService;
    private final ItemRepo itemRepo;

    private final NotificationService notificationService;
    private TabPane tabPane;
    private ListView<BorrowRecord> activeListView;
    private ListView<BorrowRecord> historyListView;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MyBorrowsView(User user, BorrowService borrowService, ItemRepo itemRepo,
                         NotificationService notificationService) {
        this.currentUser = user;
        this.borrowService = borrowService;
        this.itemRepo = itemRepo;
        this.notificationService = notificationService;

        getStyleClass().add("content-view");
        setupUI();
        refreshData();
    }

    private void setupUI() {
        // Top bar with title
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.getStyleClass().add("top-bar");
        Label title = new Label("My Borrows");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("item-detail-label"); // reuse existing class
        topBar.getChildren().add(title);

        // Tabs
        tabPane = new TabPane();
        Tab activeTab = new Tab("Active Borrows");
        activeTab.setClosable(false);
        activeListView = new ListView<>();
        activeListView.setCellFactory(lv -> new BorrowCell(true));
        activeListView.getStyleClass().add("borrow-list");
        activeTab.setContent(activeListView);

        Tab historyTab = new Tab("History");
        historyTab.setClosable(false);
        historyListView = new ListView<>();
        historyListView.setCellFactory(lv -> new BorrowCell(false));
        historyListView.getStyleClass().add("borrow-list");
        historyTab.setContent(historyListView);

        tabPane.getTabs().addAll(activeTab, historyTab);

        setTop(topBar);
        setCenter(tabPane);
    }

    public void refreshData() {
        if (!(currentUser instanceof Client client)) {
            showAlert("Only clients have borrows.");
            return;
        }

        AsyncExecutor.execute(
                () -> borrowService.getUserBorrowHistory(client).getData(),
                allRecords -> {
                    List<BorrowRecord> active = allRecords.stream()
                            .filter(r -> r.getStatus() != BorrowStatus.RETURNED)
                            .collect(Collectors.toList());
                    List<BorrowRecord> history = allRecords.stream()
                            .filter(r -> r.getStatus() == BorrowStatus.RETURNED)
                            .collect(Collectors.toList());

                    activeListView.setItems(FXCollections.observableArrayList(active));
                    historyListView.setItems(FXCollections.observableArrayList(history));
                },
                error -> showAlert("Failed to load borrows: " + error.getMessage()));
    }

    private class BorrowCell extends ListCell<BorrowRecord> {
        private final boolean showActions;

        public BorrowCell(boolean showActions) {
            this.showActions = showActions;
        }

        @Override
        protected void updateItem(BorrowRecord record, boolean empty) {
            super.updateItem(record, empty);
            if (empty || record == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            HBox row = new HBox(20);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8));
            row.getStyleClass().add("borrow-row");

            String itemTitle = getItemTitle(record.getItemId());
            Label titleLabel = new Label("Item: " + itemTitle);
            titleLabel.setPrefWidth(200);
            titleLabel.getStyleClass().add("borrow-row-label");

            Label dueLabel = new Label("Due: " + record.getDueDate().format(DATE_FMT));
            dueLabel.getStyleClass().add("borrow-row-label");

            Label statusLabel = new Label("Status: " + record.getStatus());
            statusLabel.getStyleClass().add("borrow-row-label");

            row.getChildren().addAll(titleLabel, dueLabel, statusLabel);

            if (showActions) {
                Button returnBtn = new Button("Return");
                returnBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                returnBtn.setOnAction(e -> handleReturn(record));

                Button extendBtn = new Button("Extend (7d)");
                extendBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
                extendBtn.setDisable(record.getExtensionCount() >= 1);
                extendBtn.setOnAction(e -> handleExtend(record));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                row.getChildren().addAll(spacer, returnBtn, extendBtn);
            }

            setGraphic(row);
            setText(null);
        }
    }

    private String getItemTitle(String itemId) {
        Item item = itemRepo.findById(itemId);
        return item != null ? item.getTitle() : "Unknown item";
    }

    private void handleReturn(BorrowRecord record) {
        AsyncExecutor.execute(
                () -> borrowService.returnItem(record.getRecordId(), currentUser),
                result -> {
                    showAlert(result.getMessage());
                    refreshData();
                    if (result.isSuccess()) {
                        notificationService.push(currentUser.getId(),
                                "Item Returned",
                                "You returned the item successfully.",
                                NotificationType.SUCCESS);
                    } else {
                        notificationService.push(currentUser.getId(),
                                "Return Failed",
                                result.getMessage(),
                                NotificationType.ERROR);
                    }
                },
                error -> {
                    showAlert("Return failed: " + error.getMessage());
                    notificationService.push(currentUser.getId(),
                            "Return Failed",
                            "An error occurred: " + error.getMessage(),
                            NotificationType.ERROR);
                });
    }

    private void handleExtend(BorrowRecord record) {
        AsyncExecutor.execute(
                () -> borrowService.requestExtension(record.getRecordId(), currentUser),
                result -> {
                    showAlert(result.getMessage());
                    refreshData();
                    if (result.isSuccess()) {
                        notificationService.push(currentUser.getId(),
                                "Due Date Extended",
                                "Your borrow period has been extended by 7 days.",
                                NotificationType.INFO);
                    } else {
                        notificationService.push(currentUser.getId(),
                                "Extension Failed",
                                result.getMessage(),
                                NotificationType.ERROR);
                    }
                },
                error -> {
                    showAlert("Extension failed: " + error.getMessage());
                    notificationService.push(currentUser.getId(),
                            "Extension Failed",
                            "An error occurred: " + error.getMessage(),
                            NotificationType.ERROR);
                });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}