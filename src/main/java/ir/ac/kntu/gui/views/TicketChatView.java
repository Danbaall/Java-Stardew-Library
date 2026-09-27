package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.module.Message;
import ir.ac.kntu.entities.module.Ticket;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.services.TicketService;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.format.DateTimeFormatter;

public class TicketChatView extends BorderPane {

    private final Ticket ticket;
    private final User currentUser;
    private final TicketService ticketService;
    private final Runnable onBack;

    private VBox messageContainer;
    private TextField replyInput;
    private Label statusLabel;

    public TicketChatView(Ticket ticket, User currentUser, TicketService ticketService, Runnable onBack) {
        this.ticket = ticket;
        this.currentUser = currentUser;
        this.ticketService = ticketService;
        this.onBack = onBack;

        getStyleClass().add("content-view");
        setupUI();
        loadMessages();
    }

    private void setupUI() {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(10, 15, 10, 15));
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("theme-toggle-button");
        backBtn.setOnAction(e -> onBack.run());

        Label titleLabel = new Label(ticket.getSubject());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.getStyleClass().add("item-detail-label");

        statusLabel = new Label("Status: " + ticket.getStatus());
        statusLabel.getStyleClass().add("item-detail-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, titleLabel, spacer, statusLabel);
        setTop(topBar);

        messageContainer = new VBox(10);
        messageContainer.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("edge-to-edge");
        setCenter(scrollPane);

        HBox replyBox = new HBox(10);
        replyBox.setPadding(new Insets(10));
        replyBox.setAlignment(Pos.CENTER);
        replyInput = new TextField();
        replyInput.setPromptText("Type your reply...");
        HBox.setHgrow(replyInput, Priority.ALWAYS);
        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        sendBtn.setOnAction(e -> sendReply());
        replyBox.getChildren().addAll(replyInput, sendBtn);
        setBottom(replyBox);
    }

    private void loadMessages() {
        refreshMessageDisplay();
    }

    private void refreshMessageDisplay() {
        messageContainer.getChildren().clear();
        if (ticket.getMessages() == null)
            return;

        for (Message msg : ticket.getMessages()) {
            HBox msgBox = new HBox();
            msgBox.setPadding(new Insets(5));
            msgBox.setAlignment(msg.senderId().equals(currentUser.getId()) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            VBox bubble = new VBox(5);
            bubble.setPadding(new Insets(8));
            bubble.getStyleClass().add(msg.senderId().equals(currentUser.getId()) ? "ticket-chat-bubble-self"
                    : "ticket-chat-bubble-other");

            Label senderLabel = new Label(msg.senderId().equals(currentUser.getId()) ? "You" : "Support");
            senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

            Label textLabel = new Label(msg.msg());
            textLabel.setWrapText(true);

            Label timeLabel = new Label(msg.timestamp()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

            bubble.getChildren().addAll(senderLabel, textLabel, timeLabel);
            msgBox.getChildren().add(bubble);
            messageContainer.getChildren().add(msgBox);
        }
    }

    private void sendReply() {
        String text = replyInput.getText().trim();
        if (text.isEmpty())
            return;

        AsyncExecutor.execute(
                () -> ticketService.replyToTicket(ticket.getTicketId(), currentUser, text),
                result -> {
                    if (result.isSuccess()) {
                        replyInput.clear();
                        loadMessages();
                    } else {
                        showAlert(result.getMessage());
                    }
                },
                error -> showAlert("Failed to send reply: " + error.getMessage()));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}