package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.enums.TicketType;
import ir.ac.kntu.entities.module.*;
import ir.ac.kntu.services.*;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class AdminTicketsView extends BorderPane {

    private final Admin admin;
    private final TicketService ticketService;
    private final WalletService walletService;
    private final DebtService debtService;
    private final UserService userService;
    private final ReservationService reservationService;
    private final Consumer<Ticket> onOpenChat;

    private ListView<Ticket> ticketList;
    private VBox detailPane;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AdminTicketsView(Admin admin, TicketService ticketService,
            WalletService walletService, DebtService debtService,
            UserService userService, ReservationService reservationService,
            Consumer<Ticket> onOpenChat) {
        this.admin = admin;
        this.ticketService = ticketService;
        this.walletService = walletService;
        this.debtService = debtService;
        this.userService = userService;
        this.reservationService = reservationService;
        this.onOpenChat = onOpenChat;
        getStyleClass().add("content-view");
        buildUI();
        loadTickets();
    }

    private void buildUI() {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15));
        topBar.getStyleClass().add("top-bar");
        Label title = new Label("Ticket Management");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("item-detail-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #6a89a7; -fx-text-fill: white; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadTickets());

        topBar.getChildren().addAll(title, spacer, refreshBtn);
        setTop(topBar);

        // Left: ticket list
        ticketList = new ListView<>();
        ticketList.setCellFactory(lv -> new TicketCell());
        ticketList.getStyleClass().add("borrow-list");
        ticketList.setPrefWidth(320);
        ticketList.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null)
                        showDetail(selected);
                });

        // Right: detail pane
        detailPane = new VBox(12);
        detailPane.setPadding(new Insets(20));
        detailPane.setAlignment(Pos.TOP_LEFT);
        Label placeholder = new Label("Select a ticket to view details.");
        placeholder.getStyleClass().addAll("item-detail-label", "admin-hint-label");
        detailPane.getChildren().add(placeholder);

        ScrollPane detailScroll = new ScrollPane(detailPane);
        detailScroll.setFitToWidth(true);
        detailScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        detailScroll.getStyleClass().add("edge-to-edge");

        SplitPane split = new SplitPane(ticketList, detailScroll);
        split.setDividerPositions(0.35);
        setCenter(split);
    }

    private void loadTickets() {
        AsyncExecutor.execute(
                () -> ticketService.getTicketsForAdmin(admin).getData(),
                list -> ticketList.setItems(FXCollections.observableArrayList(list)),
                err -> showAlert("Error: " + err.getMessage()));
    }

    private void showDetail(Ticket ticket) {
        detailPane.getChildren().clear();

        // Header
        Label subjectLbl = new Label(ticket.getSubject());
        subjectLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        subjectLbl.getStyleClass().add("item-detail-label");
        subjectLbl.setWrapText(true);

        Label metaLbl = new Label(
                "[" + ticket.getTicketType() + "]  •  " + ticket.getStatus()
                        + "  •  " + (ticket.getCreatedAt() != null ? ticket.getCreatedAt().format(DT) : ""));
        metaLbl.getStyleClass().addAll("item-detail-label", "admin-meta-label");

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button chatBtn = new Button("Open Chat");
        chatBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        chatBtn.setOnAction(e -> {
            if (onOpenChat != null)
                onOpenChat.accept(ticket);
        });

        Button closeBtn = new Button("Close Ticket");
        closeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> AsyncExecutor.execute(
                () -> ticketService.closeTicket(ticket.getTicketId(), admin),
                r -> {
                    showAlert(r.getMessage());
                    loadTickets();
                    detailPane.getChildren().clear();
                },
                err -> showAlert("Error: " + err.getMessage())));

        actions.getChildren().addAll(chatBtn, closeBtn);

        detailPane.getChildren().addAll(subjectLbl, metaLbl, new Separator(), actions);

        // Type sections
        if (ticket.getTicketType() == TicketType.FINANCIAL) {
            detailPane.getChildren().add(buildFinancialSection(ticket));
        } else if (ticket.getTicketType() == TicketType.RESERVATION) {
            detailPane.getChildren().add(buildReservationSection(ticket));
        }
    }

    private VBox buildFinancialSection(Ticket ticket) {
        VBox box = new VBox(8);

        Label sectionTitle = new Label("User Wallet & Transactions");
        sectionTitle.getStyleClass().add("item-detail-label");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label loadingLbl = new Label("Loading…");
        loadingLbl.getStyleClass().add("item-detail-label");
        box.getChildren().addAll(new Separator(), sectionTitle, loadingLbl);

        AsyncExecutor.execute(
                () -> {
                    User u = userService.findUserById(ticket.getUserId());
                    double debt = debtService.calculateUserDebtRaw(ticket.getUserId());
                    var txHistory = walletService.getTransactionHistory(ticket.getUserId());
                    return new Object[] { u, debt, txHistory };
                },
                data -> {
                    box.getChildren().remove(loadingLbl);
                    User u = (User) data[0];
                    double debt = (double) data[1];
                    @SuppressWarnings("unchecked")
                    var txList = (java.util.List<ir.ac.kntu.entities.module.Transaction>) data[2];

                    String name = u != null ? u.getUserName() : ticket.getUserId();
                    double balance = (u instanceof Client c) ? c.getWallet().getBalance() : 0;

                    Label userInfo = new Label("User: " + name + "  |  Balance: $"
                            + String.format("%.2f", balance) + "  |  Outstanding debt: $"
                            + String.format("%.2f", debt));
                    userInfo.getStyleClass().add("item-detail-label");
                    userInfo.setWrapText(true);
                    box.getChildren().add(userInfo);

                    if (!txList.isEmpty()) {
                        Label txTitle = new Label("Recent Transactions:");
                        txTitle.getStyleClass().add("item-detail-label");
                        txTitle.setStyle("-fx-font-weight: bold;");

                        ListView<ir.ac.kntu.entities.module.Transaction> txView = new ListView<>();
                        txView.setItems(FXCollections.observableArrayList(txList));
                        txView.setPrefHeight(Math.min(180, txList.size() * 36 + 10));
                        txView.getStyleClass().add("borrow-list");
                        txView.setCellFactory(lv -> new ListCell<>() {
                            @Override
                            protected void updateItem(ir.ac.kntu.entities.module.Transaction tx, boolean empty) {
                                super.updateItem(tx, empty);
                                if (empty || tx == null) {
                                    setText(null);
                                    return;
                                }
                                setText(tx.timestamp().format(DT) + "  |  "
                                        + tx.type() + "  |  $" + String.format("%.2f", tx.amount())
                                        + (tx.description() != null ? "  – " + tx.description() : ""));
                            }
                        });
                        box.getChildren().addAll(txTitle, txView);
                    } else {
                        Label noTx = new Label("No transactions on record.");
                        noTx.getStyleClass().add("item-detail-label");
                        noTx.setStyle("-fx-text-fill: #aaa;");
                        box.getChildren().add(noTx);
                    }
                },
                err -> {
                    box.getChildren().remove(loadingLbl);
                    Label errLbl = new Label("Could not load wallet data.");
                    errLbl.setStyle("-fx-text-fill: #e74c3c;");
                    box.getChildren().add(errLbl);
                });

        return box;
    }

    private VBox buildReservationSection(Ticket ticket) {
        VBox box = new VBox(8);

        Label sectionTitle = new Label("User Reservations");
        sectionTitle.getStyleClass().add("item-detail-label");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label loadingLbl = new Label("Loading…");
        loadingLbl.getStyleClass().add("item-detail-label");
        box.getChildren().addAll(new Separator(), sectionTitle, loadingLbl);

        AsyncExecutor.execute(
                () -> {
                    User u = userService.findUserById(ticket.getUserId());
                    return u != null ? reservationService.getUserReservations(u) : List.of();
                },
                reservations -> {
                    box.getChildren().remove(loadingLbl);

                    @SuppressWarnings("unchecked")
                    List<Reservation> resList = (List<Reservation>) reservations;
                    List<Reservation> active = resList.stream().filter(Reservation::isActive).toList();

                    if (active.isEmpty()) {
                        Label none = new Label("No active reservations.");
                        none.getStyleClass().addAll("item-detail-label", "admin-hint-label");
                        box.getChildren().add(none);
                    } else {
                        for (Reservation res : active) {
                            HBox row = new HBox(12);
                            row.setAlignment(Pos.CENTER_LEFT);
                            row.setPadding(new Insets(4));
                            row.getStyleClass().add("borrow-row");

                            Label resInfo = new Label("Item: " + res.getItemId()
                                    + "  |  Status: " + res.getStatus());
                            resInfo.getStyleClass().add("borrow-row-label");

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            Button cancelBtn = new Button("Cancel");
                            cancelBtn
                                    .setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                            cancelBtn.setOnAction(e -> AsyncExecutor.execute(
                                    () -> reservationService.adminCancelReservation(res.getReservationId()),
                                    r -> {
                                        showAlert(r.getMessage());
                                        showDetail(ticket); // refresh
                                    },
                                    err -> showAlert("Error: " + err.getMessage())));

                            row.getChildren().addAll(resInfo, spacer, cancelBtn);
                            box.getChildren().add(row);
                        }
                    }

                    // Reserve item for user
                    Button reserveForUser = new Button("Reserve Item for User…");
                    reserveForUser.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
                    reserveForUser.setOnAction(e -> showReserveDialog(ticket.getUserId()));
                    box.getChildren().add(reserveForUser);
                },
                err -> {
                    box.getChildren().remove(loadingLbl);
                    Label errLbl = new Label("Could not load reservations.");
                    errLbl.setStyle("-fx-text-fill: #e74c3c;");
                    box.getChildren().add(errLbl);
                });

        return box;
    }

    private void showReserveDialog(String userId) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reserve Item");
        dialog.setHeaderText("Reserve an item for user " + userId);
        dialog.setContentText("Item ID:");
        dialog.showAndWait().ifPresent(itemId -> {
            if (itemId.isBlank())
                return;
            AsyncExecutor.execute(
                    () -> {
                        User u = userService.findUserById(userId);
                        if (u == null)
                            return ir.ac.kntu.util.ServiceResult.failure("User not found.");
                        return reservationService.reserveItem(u, itemId.trim());
                    },
                    r -> showAlert(r.getMessage()),
                    err -> showAlert("Error: " + err.getMessage()));
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private class TicketCell extends ListCell<Ticket> {
        @Override
        protected void updateItem(Ticket t, boolean empty) {
            super.updateItem(t, empty);
            if (empty || t == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            VBox box = new VBox(3);
            box.setPadding(new Insets(6));
            box.getStyleClass().add("borrow-row");

            Label subject = new Label(t.getSubject());
            subject.getStyleClass().add("borrow-row-label");
            subject.setStyle("-fx-font-weight: bold;");
            subject.setWrapText(true);

            Label meta = new Label("[" + t.getTicketType() + "]  " + t.getStatus());
            meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

            box.getChildren().addAll(subject, meta);
            setGraphic(box);
            setText(null);
        }
    }
}
