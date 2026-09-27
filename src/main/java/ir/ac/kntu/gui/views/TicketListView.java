package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.enums.TicketType;
import ir.ac.kntu.entities.module.Ticket;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.services.TicketService;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class TicketListView extends BorderPane {

    private final User currentUser;
    private final TicketService ticketService;
    private final java.util.function.Consumer<Ticket> onTicketSelect;

    private ListView<Ticket> ticketListView;

    public TicketListView(User currentUser, TicketService ticketService,
            java.util.function.Consumer<Ticket> onTicketSelect) {
        this.currentUser = currentUser;
        this.ticketService = ticketService;
        this.onTicketSelect = onTicketSelect;

        getStyleClass().add("content-view");
        setupUI();
        refreshTickets();
    }

    private void setupUI() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.getStyleClass().add("top-bar");

        Label title = new Label("Support Tickets");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("item-detail-label");
        topBar.getChildren().add(title);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newTicketBtn = new Button("New Ticket");
        newTicketBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        newTicketBtn.setOnAction(e -> showNewTicketDialog());
        topBar.getChildren().addAll(spacer, newTicketBtn);

        setTop(topBar);

        ticketListView = new ListView<>();
        ticketListView.setCellFactory(lv -> new TicketCell());
        ticketListView.getStyleClass().add("borrow-list");
        ticketListView.setOnMouseClicked(e -> {
            Ticket selected = ticketListView.getSelectionModel().getSelectedItem();
            if (selected != null && onTicketSelect != null) {
                onTicketSelect.accept(selected);
            }
        });
        setCenter(ticketListView);
    }

    private void refreshTickets() {
        AsyncExecutor.execute(
                () -> ticketService.getUserTickets(currentUser).getData(),
                list -> ticketListView.setItems(FXCollections.observableArrayList(list)),
                error -> showAlert("Error loading tickets: " + error.getMessage()));
    }

    private void showNewTicketDialog() {
        Dialog<Ticket> dialog = new Dialog<>();
        dialog.setTitle("New Support Ticket");
        dialog.setHeaderText("Create a new ticket");

        ButtonType submitBtn = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<TicketType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(TicketType.values());
        typeCombo.setValue(TicketType.REQUEST);

        TextField subjectField = new TextField();
        subjectField.setPromptText("Subject");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Describe your issue...");
        messageArea.setPrefRowCount(5);

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Subject:"), 0, 1);
        grid.add(subjectField, 1, 1);
        grid.add(new Label("Message:"), 0, 2);
        grid.add(messageArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitBtn) {
                return null;
            }
            return null;
        });

        final Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitBtn);
        submitButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String subject = subjectField.getText().trim();
            String msg = messageArea.getText().trim();
            TicketType type = typeCombo.getValue();
            if (subject.isEmpty() || msg.isEmpty()) {
                showAlert("Subject and message are required.");
                event.consume();
                return;
            }
            AsyncExecutor.execute(
                    () -> ticketService.openTicket(currentUser, type, subject, msg),
                    result -> {
                        if (result.isSuccess()) {
                            refreshTickets();
                        } else {
                            showAlert(result.getMessage());
                        }
                    },
                    error -> showAlert("Failed to create ticket: " + error.getMessage()));
            event.consume();
            dialog.close();
        });

        dialog.showAndWait();
        refreshTickets();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private class TicketCell extends ListCell<Ticket> {
        @Override
        protected void updateItem(Ticket ticket, boolean empty) {
            super.updateItem(ticket, empty);
            if (empty || ticket == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5));
            row.getStyleClass().add("borrow-row");

            Label idLabel = new Label("#" + ticket.getTicketId().substring(0, 8));
            idLabel.getStyleClass().add("borrow-row-label");
            Label subjectLabel = new Label(ticket.getSubject());
            subjectLabel.getStyleClass().add("borrow-row-label");
            Label typeLabel = new Label("[" + ticket.getTicketType() + "]");
            typeLabel.getStyleClass().add("borrow-row-label");
            Label statusLabel = new Label(ticket.getStatus().toString());
            statusLabel.getStyleClass().add("borrow-row-label");

            row.getChildren().addAll(idLabel, subjectLabel, typeLabel, statusLabel);
            setGraphic(row);
            setText(null);
        }
    }
}