package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.enums.ReservationStatus;
import ir.ac.kntu.entities.module.Client;
import ir.ac.kntu.entities.module.Reservation;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.services.BorrowService;
import ir.ac.kntu.services.ReservationService;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import ir.ac.kntu.services.NotificationService;
import ir.ac.kntu.entities.enums.NotificationType;

public class MyReservationsView extends BorderPane {

    private final User currentUser;
    private final ReservationService reservationService;
    private final ItemRepo itemRepo;
    private final BorrowService borrowService;

    private final NotificationService notificationService;
    private ListView<Reservation> listView;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MyReservationsView(User user, ReservationService reservationService, ItemRepo itemRepo,
            BorrowService borrowService, NotificationService notificationService) {
        this.currentUser = user;
        this.reservationService = reservationService;
        this.itemRepo = itemRepo;
        this.borrowService = borrowService;
        this.notificationService = notificationService;

        getStyleClass().add("content-view");
        setupUI();
        loadReservations();
    }

    private void setupUI() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.getStyleClass().add("top-bar");
        Label title = new Label("My Reservations");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("item-detail-label");
        topBar.getChildren().add(title);
        setTop(topBar);

        listView = new ListView<>();
        listView.setCellFactory(lv -> new ReservationCell());
        listView.getStyleClass().add("borrow-list");
        setCenter(listView);
    }

    public void loadReservations() {
        AsyncExecutor.execute(
                () -> reservationService.getUserReservations(currentUser),
                list -> listView.setItems(FXCollections.observableArrayList(list)),
                error -> showAlert("Error loading reservations: " + error.getMessage()));
    }

    private void cancelReservation(Reservation res) {
        AsyncExecutor.execute(
                () -> reservationService.cancelReservation(currentUser, res.getReservationId()),
                result -> {
                    showAlert(result.getMessage());
                    loadReservations();
                    if (result.isSuccess()) {
                        notificationService.push(currentUser.getId(),
                                "Reservation Cancelled",
                                "Your reservation has been cancelled.",
                                NotificationType.INFO);
                    } else {
                        notificationService.push(currentUser.getId(),
                                "Cancellation Failed",
                                result.getMessage(),
                                NotificationType.ERROR);
                    }
                },
                error -> {
                    showAlert("Cancel failed: " + error.getMessage());
                    notificationService.push(currentUser.getId(),
                            "Cancellation Failed",
                            "An error occurred: " + error.getMessage(),
                            NotificationType.ERROR);
                });
    }

    private void borrowNow(Reservation res) {
        if (!(currentUser instanceof Client client)) {
            showAlert("Only clients can borrow.");
            return;
        }
        String itemTitle = getItemTitle(res.getItemId());
        AsyncExecutor.execute(
                () -> borrowService.checkOut(client, res.getItemId()),
                result -> {
                    showAlert(result.getMessage());
                    loadReservations();
                    if (result.isSuccess()) {
                        notificationService.push(currentUser.getId(),
                                "Borrow Successful",
                                "You borrowed \"" + itemTitle + "\". "
                                        + result.getMessage(),
                                NotificationType.SUCCESS);
                    } else {
                        notificationService.push(currentUser.getId(),
                                "Borrow Failed",
                                result.getMessage(),
                                NotificationType.ERROR);
                    }
                },
                error -> {
                    showAlert("Borrow failed: " + error.getMessage());
                    notificationService.push(currentUser.getId(),
                            "Borrow Failed",
                            "An error occurred: " + error.getMessage(),
                            NotificationType.ERROR);
                });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private class ReservationCell extends ListCell<Reservation> {
        @Override
        protected void updateItem(Reservation res, boolean empty) {
            super.updateItem(res, empty);
            if (empty || res == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5));
            row.getStyleClass().add("borrow-row");

            String itemTitle = getItemTitle(res.getItemId());
            Label itemLabel = new Label("Item: " + itemTitle);
            itemLabel.getStyleClass().add("borrow-row-label");

            Label statusLabel = new Label("Status: " + res.getStatus());
            statusLabel.getStyleClass().add("borrow-row-label");

            Label dateLabel = new Label(
                    "Reserved: " + (res.getReservedAt() != null ? res.getReservedAt().format(DATE_FMT) : ""));
            dateLabel.getStyleClass().add("borrow-row-label");

            row.getChildren().addAll(itemLabel, statusLabel, dateLabel);

            if (res.getStatus() == ReservationStatus.NOTIFIED &&
                    res.getClaimDeadline() != null &&
                    res.getClaimDeadline().isAfter(LocalDateTime.now())) {
                Button borrowBtn = new Button("Borrow Now");
                borrowBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                borrowBtn.setOnAction(e -> borrowNow(res));
                row.getChildren().add(borrowBtn);
            }

            if (res.isActive()) {
                Button cancelBtn = new Button("Cancel");
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                cancelBtn.setOnAction(e -> cancelReservation(res));
                row.getChildren().add(cancelBtn);
            }

            setGraphic(row);
            setText(null);
        }
    }

    private String getItemTitle(String itemId) {
        var item = itemRepo.findById(itemId);
        return item != null ? item.getTitle() : "Unknown item";
    }
}