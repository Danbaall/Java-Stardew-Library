package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.module.*;
import ir.ac.kntu.repo.BorrowHistoryRepo;
import ir.ac.kntu.services.BorrowService;
import ir.ac.kntu.services.ReservationService;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;
import ir.ac.kntu.services.NotificationService;
import ir.ac.kntu.entities.enums.NotificationType;

public class ItemDetailView extends BorderPane {

    private final Item item;
    private final User currentUser;
    private final BorrowService borrowService;
    private final BorrowHistoryRepo borrowHistoryRepo;
    private final ReservationService reservationService;

    private final NotificationService notificationService;
    private final Runnable onBack;

    private Label statusLabel;
    private Button borrowButton, reserveButton;

    public ItemDetailView(Item item, User currentUser, BorrowService borrowService,
            BorrowHistoryRepo borrowHistoryRepo, ReservationService reservationService,
            NotificationService notificationService, Runnable onBack) {
        this.item = item;
        this.currentUser = currentUser;
        this.borrowService = borrowService;
        this.borrowHistoryRepo = borrowHistoryRepo;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.onBack = onBack;

        this.getStyleClass().add("content-view");
        setupUI();
        checkExistingBorrow();
    }

    private void setupUI() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(15));
        topBar.getStyleClass().add("top-bar");
        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("theme-toggle-button");
        backBtn.setOnAction(e -> onBack.run());
        topBar.getChildren().add(backBtn);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);

        ImageView coverView = createCoverImageView();
        content.getChildren().add(coverView);

        Label titleLabel = new Label(item.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titleLabel.getStyleClass().add("item-detail-label");

        Label authorLabel = new Label("Author: " + item.getAuthor());
        authorLabel.getStyleClass().add("item-detail-label");
        Label categoryLabel = new Label("Category: " + item.getCategory());
        categoryLabel.getStyleClass().add("item-detail-label");
        Label typeLabel = new Label("Type: " + item.getType());
        typeLabel.getStyleClass().add("item-detail-label");
        Label yearLabel = new Label("Published: " + item.getPublishYear());
        yearLabel.getStyleClass().add("item-detail-label");

        VBox extraInfo = new VBox(5);
        if (item instanceof Book book) {
            Label isbnLabel = new Label("ISBN: " + book.getIsbn());
            isbnLabel.getStyleClass().add("item-detail-label");
            Label pagesLabel = new Label("Pages: " + book.getPageCount());
            pagesLabel.getStyleClass().add("item-detail-label");
            Label pubLabel = new Label("Publisher: " + book.getPublisher());
            pubLabel.getStyleClass().add("item-detail-label");
            extraInfo.getChildren().addAll(isbnLabel, pagesLabel, pubLabel);
        } else if (item instanceof Magazine mag) {
            Label issnLabel = new Label("ISSN: " + mag.getIssn());
            issnLabel.getStyleClass().add("item-detail-label");
            Label periodLabel = new Label("Period: " + mag.getPeriod());
            periodLabel.getStyleClass().add("item-detail-label");
            extraInfo.getChildren().addAll(issnLabel, periodLabel);
        } else if (item instanceof AudioBook ab) {
            Label durLabel = new Label("Duration: " + ab.getDuration());
            durLabel.getStyleClass().add("item-detail-label");
            Label fmtLabel = new Label("Format: " + ab.getFormat());
            fmtLabel.getStyleClass().add("item-detail-label");
            Label sizeLabel = new Label("File size: " + ab.getFileSizeMB() + " MB");
            sizeLabel.getStyleClass().add("item-detail-label");
            extraInfo.getChildren().addAll(durLabel, fmtLabel, sizeLabel);
        } else if (item instanceof Ebook eb) {
            Label fmtLabel = new Label("Format: " + eb.getFormat());
            fmtLabel.getStyleClass().add("item-detail-label");
            Label sizeLabel = new Label("File size: " + eb.getFileSizeMB() + " MB");
            sizeLabel.getStyleClass().add("item-detail-label");
            extraInfo.getChildren().addAll(fmtLabel, sizeLabel);
        }

        Label availabilityLabel = new Label();
        if (item instanceof PhysicalItem physical) {
            availabilityLabel.setText(physical.getAvailableCopies() + " of " +
                    physical.getTotalCopies() + " available");
        } else {
            availabilityLabel.setText("Available (Digital)");
        }
        availabilityLabel.getStyleClass().add("item-detail-label");

        borrowButton = new Button("Borrow");
        borrowButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px 40px; " +
                "-fx-background-radius: 5px; -fx-cursor: hand;");
        borrowButton.setOnAction(e -> handleBorrow());

        reserveButton = new Button("Reserve");
        reserveButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px 40px; " +
                "-fx-background-radius: 5px; -fx-cursor: hand;");
        reserveButton.setOnAction(e -> handleReserve());
        reserveButton.setVisible(false);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #888;");

        VBox buttonBox = new VBox(10, borrowButton, reserveButton, statusLabel);
        buttonBox.setAlignment(Pos.CENTER);

        content.getChildren().addAll(titleLabel, authorLabel, categoryLabel, typeLabel,
                yearLabel, extraInfo, availabilityLabel, buttonBox);

        this.setTop(topBar);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("edge-to-edge");
        this.setCenter(scrollPane);
    }

    private ImageView createCoverImageView() {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(160);
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(true);

        String imageUrl = null;
        try {
            imageUrl = item.getCoverImageUrl();
        } catch (Exception ignored) {
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                imageView.setImage(new Image(imageUrl, true));
            } catch (Exception ex) {
                imageView.setImage(createPlaceholderImage());
            }
        } else {
            imageView.setImage(createPlaceholderImage());
        }

        return imageView;
    }

    private Image createPlaceholderImage() {
        Rectangle rect = new Rectangle(160, 220);
        rect.setArcWidth(12);
        rect.setArcHeight(12);
        rect.setStyle("-fx-fill: derive(#6a89a7, 20%);");

        String abbrev = getAbbreviation(item.getTitle());
        Text abbrevText = new Text(abbrev);
        abbrevText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        abbrevText.setFill(Color.WHITE);

        StackPane pane = new StackPane(rect, abbrevText);
        return pane.snapshot(null, null);
    }

    private String getAbbreviation(String title) {
        if (title == null || title.isEmpty())
            return "?";
        String[] words = title.split("\\s+");
        if (words.length >= 2) {
            return (words[0].charAt(0) + "" + words[1].charAt(0)).toUpperCase();
        }
        return title.substring(0, Math.min(2, title.length())).toUpperCase();
    }

    private void checkExistingBorrow() {
        if (!(currentUser instanceof Client)) {
            borrowButton.setDisable(true);
            reserveButton.setDisable(true);
            statusLabel.setText("Only clients can borrow/reserve.");
            return;
        }

        AsyncExecutor.execute(() -> {
            List<BorrowRecord> active = borrowHistoryRepo.findActiveByUserId(currentUser.getId());
            boolean alreadyBorrowed = active.stream().anyMatch(r -> r.getItemId().equals(item.getId()));
            return alreadyBorrowed;
        }, alreadyBorrowed -> {
            if (alreadyBorrowed) {
                borrowButton.setDisable(true);
                borrowButton.setText("Already Borrowed");
                borrowButton.setStyle("-fx-background-color: #aaa; ...");
                reserveButton.setVisible(false);
                statusLabel.setText("You already have this item.");
                return;
            }
            if (item instanceof PhysicalItem physical && physical.getAvailableCopies() == 0) {
                borrowButton.setVisible(false);
                reserveButton.setVisible(true);
                AsyncExecutor.execute(() -> {
                    List<Reservation> resList = reservationService.getUserReservations(currentUser);
                    return resList.stream().anyMatch(r -> r.getItemId().equals(item.getId()) && r.isActive());
                }, alreadyReserved -> {
                    if (alreadyReserved) {
                        reserveButton.setDisable(true);
                        reserveButton.setText("Already Reserved");
                        statusLabel.setText("You have an active reservation.");
                    } else {
                        reserveButton.setDisable(false);
                        reserveButton.setText("Reserve");
                        statusLabel.setText("Out of stock – reserve to be notified.");
                    }
                }, error -> {
                });
            } else {
                borrowButton.setVisible(true);
                reserveButton.setVisible(false);
            }
        }, error -> statusLabel.setText("Error checking status."));
    }

    private void handleBorrow() {
        if (!(currentUser instanceof Client client)) {
            statusLabel.setText("Only clients can borrow.");
            return;
        }

        borrowButton.setDisable(true);
        statusLabel.setText("Processing...");

        AsyncExecutor.execute(
                () -> borrowService.checkOut(client, item.getId()),
                result -> {
                    if (result.isSuccess()) {
                        statusLabel.setText(result.getMessage());
                        borrowButton.setDisable(true);
                        borrowButton.setText("Borrowed");
                        borrowButton.setStyle("-fx-background-color: #aaa; -fx-text-fill: white; " +
                                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px 40px; " +
                                "-fx-background-radius: 5px;");
                        notificationService.push(currentUser.getId(),
                                "Borrow Successful",
                                "You borrowed \"" + item.getTitle() + "\". "
                                        + result.getMessage(),
                                NotificationType.SUCCESS);
                    } else {
                        statusLabel.setText(result.getMessage());
                        borrowButton.setDisable(false);
                        notificationService.push(currentUser.getId(),
                                "Borrow Failed",
                                result.getMessage(),
                                NotificationType.ERROR);
                    }
                },
                error -> {
                    statusLabel.setText("Error: " + error.getMessage());
                    borrowButton.setDisable(false);
                    notificationService.push(currentUser.getId(),
                            "Borrow Failed",
                            "An error occurred: " + error.getMessage(),
                            NotificationType.ERROR);
                });
    }

    private void handleReserve() {
        AsyncExecutor.execute(
                () -> reservationService.reserveItem(currentUser, item.getId()),
                result -> {
                    if (result.isSuccess()) {
                        statusLabel.setText(result.getMessage());
                        reserveButton.setDisable(true);
                        reserveButton.setText("Reserved");
                        notificationService.push(currentUser.getId(),
                                "Reservation Placed",
                                "\"" + item.getTitle() + "\" has been reserved. "
                                        + "You'll be notified when it's available.",
                                NotificationType.SUCCESS);
                    } else {
                        statusLabel.setText(result.getMessage());
                        notificationService.push(currentUser.getId(),
                                "Reservation Failed",
                                result.getMessage(),
                                NotificationType.ERROR);
                    }
                },
                error -> {
                    statusLabel.setText("Reservation failed: " + error.getMessage());
                    notificationService.push(currentUser.getId(),
                            "Reservation Failed",
                            "An error occurred: " + error.getMessage(),
                            NotificationType.ERROR);
                });
    }
}