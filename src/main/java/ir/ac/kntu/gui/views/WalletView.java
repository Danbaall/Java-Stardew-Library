package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.enums.TransactionType;
import ir.ac.kntu.entities.module.*;
import ir.ac.kntu.services.DebtService;
import ir.ac.kntu.services.WalletService;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class WalletView extends BorderPane {

    private final Client client;
    private final WalletService walletService;
    private final DebtService debtService;

    private Label balanceLabel;
    private ListView<BorrowRecord> finesListView;
    private ListView<Transaction> txListView;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public WalletView(User user, WalletService walletService, DebtService debtService) {
        if (!(user instanceof Client))
            throw new IllegalArgumentException("Must be a client");
        this.client = (Client) user;
        this.walletService = walletService;
        this.debtService = debtService;

        getStyleClass().add("content-view");
        setupUI();
        refresh();
    }

    private void setupUI() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.getStyleClass().add("top-bar");
        Label title = new Label("Wallet & Fines");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("item-detail-label");
        topBar.getChildren().add(title);
        setTop(topBar);

        // Balance + add funds
        VBox header = new VBox(10);
        header.setPadding(new Insets(20, 20, 10, 20));
        header.setAlignment(Pos.TOP_LEFT);

        balanceLabel = new Label();
        balanceLabel.setStyle("-fx-font-size: 18px;");
        balanceLabel.getStyleClass().add("item-detail-label");

        HBox addFundsBox = new HBox(10);
        addFundsBox.setAlignment(Pos.CENTER_LEFT);
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setPrefWidth(100);
        Button addBtn = new Button("Add Funds");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        addBtn.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(amountField.getText());
                handleAddFunds(amt);
                amountField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Invalid amount.");
            }
        });
        addFundsBox.getChildren().addAll(new Label("Add funds: $"), amountField, addBtn);
        header.getChildren().addAll(balanceLabel, addFundsBox);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // fines
        finesListView = new ListView<>();
        finesListView.setCellFactory(lv -> new FineCell());
        finesListView.getStyleClass().add("borrow-list");
        Tab finesTab = new Tab("Outstanding Fines", finesListView);

        // history
        txListView = new ListView<>();
        txListView.setCellFactory(lv -> new TxCell());
        txListView.getStyleClass().add("borrow-list");
        Tab histTab = new Tab("Transaction History", txListView);

        tabs.getTabs().addAll(finesTab, histTab);

        VBox center = new VBox(0, header, tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        setCenter(center);
    }

    public void refresh() {
        balanceLabel.setText("Wallet Balance: $" + String.format("%.2f", client.getWallet().getBalance()));

        AsyncExecutor.execute(
                () -> debtService.getUnpaidDebtRecords(client.getId()),
                list -> finesListView.setItems(FXCollections.observableArrayList(list)),
                error -> showAlert("Error loading fines: " + error.getMessage()));

        AsyncExecutor.execute(
                () -> walletService.getTransactionHistory(client.getId()),
                list -> txListView.setItems(FXCollections.observableArrayList(list)),
                error -> showAlert("Error loading transactions: " + error.getMessage()));
    }

    private void handleAddFunds(double amount) {
        AsyncExecutor.execute(
                () -> walletService.chargeWallet(client, amount),
                result -> {
                    showAlert(result.getMessage());
                    refresh();
                },
                error -> showAlert("Error adding funds: " + error.getMessage()));
    }

    private void handlePayDebt(BorrowRecord record) {
        double finePerDay = LibraryPolicy.getInstance().getFineRatePerDay();
        long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
        double amount = Math.max(0, daysOverdue * finePerDay);

        AsyncExecutor.execute(
                () -> walletService.payDebt(client, amount),
                result -> {
                    showAlert(result.getMessage());
                    refresh();
                },
                error -> showAlert("Payment failed: " + error.getMessage()));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private class FineCell extends ListCell<BorrowRecord> {
        @Override
        protected void updateItem(BorrowRecord record, boolean empty) {
            super.updateItem(record, empty);
            if (empty || record == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            double fineRate = LibraryPolicy.getInstance().getFineRatePerDay();
            long days = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
            double debt = Math.max(0, days * fineRate);

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5));
            row.getStyleClass().add("borrow-row");

            Label itemLabel = new Label("Item: " + record.getItemId());
            itemLabel.getStyleClass().add("borrow-row-label");
            Label dueLabel = new Label("Due: " + record.getDueDate());
            dueLabel.getStyleClass().add("borrow-row-label");
            Label amountLabel = new Label(String.format("Fine: $%.2f", debt));
            amountLabel.getStyleClass().add("borrow-row-label");

            Button payBtn = new Button("Pay");
            payBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-cursor: hand;");
            payBtn.setOnAction(e -> handlePayDebt(record));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(itemLabel, dueLabel, amountLabel, spacer, payBtn);
            setGraphic(row);
            setText(null);
        }
    }

    private class TxCell extends ListCell<Transaction> {
        @Override
        protected void updateItem(Transaction tx, boolean empty) {
            super.updateItem(tx, empty);
            if (empty || tx == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5));
            row.getStyleClass().add("borrow-row");

            Label dateLabel = new Label(tx.timestamp().format(DT_FMT));
            dateLabel.getStyleClass().add("borrow-row-label");
            dateLabel.setPrefWidth(130);

            boolean isCharge = tx.type() == TransactionType.CHARGE;
            Label typeLabel = new Label(isCharge ? "Deposit" : "Payment");
            typeLabel.setStyle(isCharge
                    ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                    : "-fx-text-fill: #e67e22; -fx-font-weight: bold;");
            typeLabel.setPrefWidth(70);

            String sign = isCharge ? "+" : "-";
            Label amtLabel = new Label(sign + String.format("$%.2f", tx.amount()));
            amtLabel.getStyleClass().add("borrow-row-label");
            amtLabel.setPrefWidth(80);

            Label descLabel = new Label(tx.description() != null ? tx.description() : "");
            descLabel.getStyleClass().add("borrow-row-label");

            row.getChildren().addAll(dateLabel, typeLabel, amtLabel, descLabel);
            setGraphic(row);
            setText(null);
        }
    }
}
