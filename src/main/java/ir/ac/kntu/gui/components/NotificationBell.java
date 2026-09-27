package ir.ac.kntu.gui.components;

import ir.ac.kntu.entities.enums.NotificationType;
import ir.ac.kntu.entities.module.Notification;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.services.NotificationService;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Popup;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationBell extends StackPane {

    private static final int POPUP_WIDTH = 360;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    private final User user;
    private final NotificationService notificationService;

    private final Label badgeLabel = new Label();
    private final Popup popup = new Popup();
    private final VBox notifList = new VBox(6);
    private VBox popupBox;

    public NotificationBell(User user, NotificationService notificationService) {
        this.user = user;
        this.notificationService = notificationService;
        buildBell();
        buildPopup();
        bindBadge();
    }

    private void buildBell() {
        Button bellBtn = new Button("\uD83D\uDD14"); // bell
        bellBtn.getStyleClass().add("notif-bell-button");
        bellBtn.setOnAction(e -> togglePopup(bellBtn));

        badgeLabel.getStyleClass().add("notif-badge");
        badgeLabel.setMouseTransparent(true);
        badgeLabel.setVisible(false);
        StackPane.setAlignment(badgeLabel, Pos.TOP_RIGHT);

        getChildren().addAll(bellBtn, badgeLabel);
    }

    private void buildPopup() {
        popup.setAutoHide(true);

        popupBox = new VBox();
        popupBox.getStyleClass().add("notif-popup-box");
        popupBox.setPrefWidth(POPUP_WIDTH);
        popupBox.setMaxWidth(POPUP_WIDTH);

        // Header row
        HBox header = new HBox(8);
        header.getStyleClass().add("notif-popup-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Notifications");
        title.getStyleClass().add("notif-popup-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markReadBtn = new Button("Mark all read");
        markReadBtn.getStyleClass().add("notif-action-btn");
        markReadBtn.setOnAction(e -> {
            notificationService.markAllRead(user.getId());
            refreshList();
        });

        Button clearBtn = new Button("Clear all");
        clearBtn.getStyleClass().add("notif-action-btn");
        clearBtn.setOnAction(e -> {
            notificationService.clearAll(user.getId());
            refreshList();
        });

        header.getChildren().addAll(title, spacer, markReadBtn, clearBtn);

        // Scroll part
        notifList.getStyleClass().add("notif-list");
        notifList.setPadding(new Insets(8));

        ScrollPane scroll = new ScrollPane(notifList);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("notif-scroll");
        scroll.setPrefHeight(380);
        scroll.setMaxHeight(380);

        popupBox.getChildren().addAll(header, scroll);
        popup.getContent().add(popupBox);
    }

    private void bindBadge() {
        notificationService.getCurrentUserNotifications()
                .addListener((ListChangeListener<Notification>) change -> updateBadge());
        updateBadge();
    }

    private void updateBadge() {
        long unread = notificationService.getUnreadCount(user.getId());
        if (unread > 0) {
            badgeLabel.setText(unread > 99 ? "99+" : String.valueOf(unread));
            badgeLabel.setVisible(true);
        } else {
            badgeLabel.setVisible(false);
        }
    }

    private void togglePopup(Button anchor) {
        if (popup.isShowing()) {
            popup.hide();
            return;
        }

        refreshList();

        try {
            if (popupBox.getStylesheets().isEmpty()) {
                var res = getClass().getResource("/css/theme.css");
                if (res != null) {
                    popupBox.getStylesheets().add(res.toExternalForm());
                }
            }
            if (getScene() != null
                    && getScene().getRoot().getStyleClass().contains("dark-mode")) {
                if (!popupBox.getStyleClass().contains("dark-mode")) {
                    popupBox.getStyleClass().add("dark-mode");
                }
            } else {
                popupBox.getStyleClass().remove("dark-mode");
            }
        } catch (Exception ignored) {
        }

        Bounds screenBounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (screenBounds != null) {
            double x = screenBounds.getMaxX() - POPUP_WIDTH;
            double y = screenBounds.getMaxY() + 6;
            popup.show(anchor.getScene().getWindow(), x, y);
        }

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
        pause.setOnFinished(e -> {
            notificationService.markAllRead(user.getId());
            refreshList();
        });
        pause.play();
    }

    private void refreshList() {
        notifList.getChildren().clear();
        List<Notification> notifications = notificationService.getForUser(user.getId());

        if (notifications.isEmpty()) {
            Label empty = new Label("No notifications yet");
            empty.getStyleClass().add("notif-empty-label");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(30, 0, 30, 0));
            notifList.getChildren().add(empty);
            return;
        }

        for (Notification n : notifications) {
            notifList.getChildren().add(buildRow(n));
        }
    }

    private HBox buildRow(Notification n) {
        HBox row = new HBox(10);
        row.getStyleClass().add("notif-item");
        if (!n.isRead()) {
            row.getStyleClass().add("notif-item-unread");
        }
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(8, 12, 8, 0));

        // Colored left accent bar
        Region accent = new Region();
        accent.getStyleClass().addAll("notif-accent", accentStyle(n.getType()));
        accent.setPrefWidth(4);
        accent.setMinHeight(44);

        // Text content
        VBox textBox = new VBox(3);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        textBox.setPadding(new Insets(0, 8, 0, 8));

        Label titleLbl = new Label(n.getTitle());
        titleLbl.getStyleClass().add("notif-item-title");

        Label msgLbl = new Label(n.getMessage());
        msgLbl.getStyleClass().add("notif-item-message");
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(POPUP_WIDTH - 60.0);

        Label timeLbl = new Label(n.getTimestamp().format(TIME_FMT));
        timeLbl.getStyleClass().add("notif-item-time");

        textBox.getChildren().addAll(titleLbl, msgLbl, timeLbl);
        row.getChildren().addAll(accent, textBox);
        return row;
    }

    private String accentStyle(NotificationType type) {
        return switch (type) {
            case SUCCESS -> "notif-accent-success";
            case ERROR -> "notif-accent-error";
            case WARNING -> "notif-accent-warning";
            case INFO -> "notif-accent-info";
        };
    }
}
