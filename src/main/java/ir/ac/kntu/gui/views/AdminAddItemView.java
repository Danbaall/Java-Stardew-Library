package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.enums.Format;
import ir.ac.kntu.entities.enums.ItemType;
import ir.ac.kntu.entities.enums.Period;
import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.services.ItemService;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminAddItemView extends BorderPane {

    private final User admin;
    private final ItemService itemService;
    private final ItemRepo itemRepo;
    private final Runnable onItemAdded;

    private Label feedbackLabel;

    public AdminAddItemView(User admin, ItemService itemService,
            ItemRepo itemRepo, Runnable onItemAdded) {
        this.admin = admin;
        this.itemService = itemService;
        this.itemRepo = itemRepo;
        this.onItemAdded = onItemAdded;
        getStyleClass().add("content-view");
        buildUI();
    }

    private void buildUI() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(15));
        topBar.getStyleClass().add("top-bar");
        Label title = new Label("Add Item to Catalog");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("item-detail-label");
        topBar.getChildren().add(title);
        setTop(topBar);

        feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                new Tab("Book", buildBookForm()),
                new Tab("Magazine", buildMagazineForm()),
                new Tab("E-Book", buildEbookForm()),
                new Tab("Audiobook", buildAudioBookForm()));

        VBox center = new VBox(10, tabs, feedbackLabel);
        center.setPadding(new Insets(15));
        VBox.setVgrow(tabs, Priority.ALWAYS);
        setCenter(center);
    }

    private ScrollPane buildBookForm() {
        GridPane g = grid();
        TextField title = field(g, "Title *", 0);
        TextField author = field(g, "Author *", 1);
        TextField category = field(g, "Category *", 2);
        TextField year = field(g, "Publish Year *", 3);
        TextField publisher = field(g, "Publisher *", 4);
        TextField pages = field(g, "Page Count *", 5);
        TextField isbn = field(g, "ISBN-13 *", 6);
        TextField copies = field(g, "Total Copies *", 7);
        TextField coverUrl = field(g, "Cover Image URL", 8);

        Button submit = submitBtn();
        g.add(submit, 1, 9);

        submit.setOnAction(e -> {
            try {
                String data = join(title, author, category, year, publisher, pages, isbn);
                int n = Integer.parseInt(copies.getText().trim());
                String cover = coverUrl.getText().trim();
                AsyncExecutor.execute(
                        () -> itemService.addBook(data, ItemType.BOOK, n, admin),
                        r -> {
                            if (r.isSuccess()) {
                                if (!cover.isBlank()) {
                                    r.getData().setCoverImageUrl(cover);
                                    itemRepo.save(r.getData());
                                }
                                showFeedback(r.getMessage(), true);
                                clearFields(title, author, category, year, publisher, pages, isbn, copies, coverUrl);
                                if (onItemAdded != null)
                                    onItemAdded.run();
                            } else {
                                showFeedback(r.getMessage(), false);
                            }
                        },
                        err -> showFeedback("Error: " + err.getMessage(), false));
            } catch (Exception ex) {
                showFeedback("Invalid input: " + ex.getMessage(), false);
            }
        });
        return scroll(g);
    }

    private ScrollPane buildMagazineForm() {
        GridPane g = grid();
        TextField title = field(g, "Title *", 0);
        TextField author = field(g, "Author *", 1);
        TextField category = field(g, "Category *", 2);
        TextField year = field(g, "Publish Year *", 3);
        TextField issn = field(g, "ISSN *", 4);

        ComboBox<Period> periodBox = new ComboBox<>();
        periodBox.getItems().addAll(Period.values());
        periodBox.setValue(Period.MONTHLY);
        addComboRow(g, "Period *", periodBox, 5);

        TextField copies = field(g, "Total Copies *", 6);
        TextField coverUrl = field(g, "Cover Image URL", 7);

        Button submit = submitBtn();
        g.add(submit, 1, 8);
        submit.setOnAction(e -> {
            try {
                String data = join(title, author, category, year, issn)
                        + "::" + periodBox.getValue().name();
                int n = Integer.parseInt(copies.getText().trim());
                String cover = coverUrl.getText().trim();
                AsyncExecutor.execute(
                        () -> itemService.addMagazine(data, ItemType.MAGAZINE, n, admin),
                        r -> {
                            if (r.isSuccess()) {
                                if (!cover.isBlank()) {
                                    r.getData().setCoverImageUrl(cover);
                                    itemRepo.save(r.getData());
                                }
                                showFeedback(r.getMessage(), true);
                                clearFields(title, author, category, year, issn, copies, coverUrl);
                                if (onItemAdded != null)
                                    onItemAdded.run();
                            } else {
                                showFeedback(r.getMessage(), false);
                            }
                        },
                        err -> showFeedback("Error: " + err.getMessage(), false));
            } catch (Exception ex) {
                showFeedback("Invalid input: " + ex.getMessage(), false);
            }
        });
        return scroll(g);
    }

    private ScrollPane buildEbookForm() {
        GridPane g = grid();
        TextField title = field(g, "Title *", 0);
        TextField author = field(g, "Author *", 1);
        TextField category = field(g, "Category *", 2);
        TextField year = field(g, "Publish Year *", 3);
        TextField fileSize = field(g, "File Size (MB) *", 4);

        ComboBox<Format> formatBox = new ComboBox<>();
        formatBox.getItems().addAll(Format.EPUB, Format.PDF);
        formatBox.setValue(Format.EPUB);
        addComboRow(g, "Format *", formatBox, 5);

        TextField url = field(g, "Download URL *", 6);
        TextField coverUrl = field(g, "Cover Image URL", 7);

        Button submit = submitBtn();
        g.add(submit, 1, 8);
        submit.setOnAction(e -> {
            try {
                String data = join(title, author, category, year, fileSize)
                        + "::" + formatBox.getValue().name()
                        + "::" + url.getText().trim();
                String cover = coverUrl.getText().trim();
                AsyncExecutor.execute(
                        () -> itemService.addEbook(data, ItemType.EBOOK, 0, admin),
                        r -> {
                            if (r.isSuccess()) {
                                if (!cover.isBlank()) {
                                    r.getData().setCoverImageUrl(cover);
                                    itemRepo.save(r.getData());
                                }
                                showFeedback(r.getMessage(), true);
                                clearFields(title, author, category, year, fileSize, url, coverUrl);
                                if (onItemAdded != null)
                                    onItemAdded.run();
                            } else {
                                showFeedback(r.getMessage(), false);
                            }
                        },
                        err -> showFeedback("Error: " + err.getMessage(), false));
            } catch (Exception ex) {
                showFeedback("Invalid input: " + ex.getMessage(), false);
            }
        });
        return scroll(g);
    }

    private ScrollPane buildAudioBookForm() {
        GridPane g = grid();
        TextField title = field(g, "Title *", 0);
        TextField author = field(g, "Author *", 1);
        TextField category = field(g, "Category *", 2);
        TextField year = field(g, "Publish Year *", 3);
        TextField fileSize = field(g, "File Size (MB) *", 4);

        ComboBox<Format> formatBox = new ComboBox<>();
        formatBox.getItems().addAll(Format.AA3, Format.MP3);
        formatBox.setValue(Format.MP3);
        addComboRow(g, "Format *", formatBox, 5);

        TextField url = field(g, "Download URL *", 6);
        TextField duration = field(g, "Duration *", 7);
        TextField coverUrl = field(g, "Cover Image URL", 8);

        Button submit = submitBtn();
        g.add(submit, 1, 9);
        submit.setOnAction(e -> {
            try {
                String data = join(title, author, category, year, fileSize)
                        + "::" + formatBox.getValue().name()
                        + "::" + url.getText().trim()
                        + "::" + duration.getText().trim();
                String cover = coverUrl.getText().trim();
                AsyncExecutor.execute(
                        () -> itemService.addAudioBook(data, ItemType.AUDIOBOOK, 0, admin),
                        r -> {
                            if (r.isSuccess()) {
                                if (!cover.isBlank()) {
                                    r.getData().setCoverImageUrl(cover);
                                    itemRepo.save(r.getData());
                                }
                                showFeedback(r.getMessage(), true);
                                clearFields(title, author, category, year, fileSize, url, duration, coverUrl);
                                if (onItemAdded != null)
                                    onItemAdded.run();
                            } else {
                                showFeedback(r.getMessage(), false);
                            }
                        },
                        err -> showFeedback("Error: " + err.getMessage(), false));
            } catch (Exception ex) {
                showFeedback("Invalid input: " + ex.getMessage(), false);
            }
        });
        return scroll(g);
    }

    private GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(12);
        g.setPadding(new Insets(20));
        ColumnConstraints c0 = new ColumnConstraints(150);
        c0.setHalignment(HPos.RIGHT);
        ColumnConstraints c1 = new ColumnConstraints(300);
        g.getColumnConstraints().addAll(c0, c1);
        return g;
    }

    private TextField field(GridPane g, String labelText, int row) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("item-detail-label");
        TextField tf = new TextField();
        g.add(lbl, 0, row);
        g.add(tf, 1, row);
        return tf;
    }

    private <T> void addComboRow(GridPane g, String labelText, ComboBox<T> box, int row) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("item-detail-label");
        g.add(lbl, 0, row);
        g.add(box, 1, row);
    }

    private Button submitBtn() {
        Button b = new Button("Add to Catalog");
        b.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8px 20px; "
                + "-fx-background-radius: 6px;");
        return b;
    }

    private String join(TextField... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0)
                sb.append("::");
            sb.append(fields[i].getText().trim());
        }
        return sb.toString();
    }

    private void clearFields(TextField... fields) {
        for (TextField f : fields)
            f.clear();
    }

    private ScrollPane scroll(GridPane g) {
        ScrollPane sp = new ScrollPane(g);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.getStyleClass().add("edge-to-edge");
        return sp;
    }

    private void showFeedback(String msg, boolean success) {
        feedbackLabel.setText(msg);
        feedbackLabel.setStyle(success
                ? "-fx-text-fill: #27ae60; -fx-font-size: 13px;"
                : "-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
    }
}
