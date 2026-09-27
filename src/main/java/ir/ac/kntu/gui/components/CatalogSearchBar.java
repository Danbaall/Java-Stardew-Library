package ir.ac.kntu.gui.components;

import ir.ac.kntu.entities.enums.ItemType;
import ir.ac.kntu.entities.module.FilterFactor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.function.Consumer;

public class CatalogSearchBar extends HBox {

    private final TextField searchField = new TextField();
    private final ComboBox<String> typeBox = new ComboBox<>();
    private final ComboBox<String> categoryBox = new ComboBox<>();
    private final CheckBox availableCheck = new CheckBox("Available only");

    private final Consumer<FilterFactor> onFilter;

    public CatalogSearchBar(Consumer<FilterFactor> onFilter) {
        this.onFilter = onFilter;

        getStyleClass().add("catalog-search-bar");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(10, 20, 10, 20));

        // Search field
        searchField.setPromptText("Search by title or author…");
        searchField.setPrefWidth(240);
        searchField.getStyleClass().add("catalog-search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, old, val) -> fire());

        // Type filter
        typeBox.getItems().addAll("All Types", "Book", "Magazine", "E-Book", "Audiobook");
        typeBox.setValue("All Types");
        typeBox.getStyleClass().add("catalog-filter-combo");
        typeBox.setOnAction(e -> fire());

        // Category filter
        categoryBox.getItems().add("All Categories");
        categoryBox.setValue("All Categories");
        categoryBox.getStyleClass().add("catalog-filter-combo");
        categoryBox.setOnAction(e -> fire());

        // Availability toggle
        availableCheck.getStyleClass().add("catalog-available-check");
        availableCheck.setOnAction(e -> fire());

        getChildren().addAll(searchField, typeBox, categoryBox, availableCheck);
    }

    public void setCategories(List<String> sortedCategories) {
        String current = categoryBox.getValue();
        categoryBox.getItems().setAll("All Categories");
        categoryBox.getItems().addAll(sortedCategories);
        categoryBox.setValue(sortedCategories.contains(current) ? current : "All Categories");
    }

    private void fire() {
        String kw = searchField.getText().trim();

        ItemType type = switch (typeBox.getValue()) {
            case "Book" -> ItemType.BOOK;
            case "Magazine" -> ItemType.MAGAZINE;
            case "E-Book" -> ItemType.EBOOK;
            case "Audiobook" -> ItemType.AUDIOBOOK;
            default -> null;
        };

        String cat = "All Categories".equals(categoryBox.getValue()) ? null : categoryBox.getValue();
        Boolean avail = availableCheck.isSelected() ? Boolean.TRUE : null;

        onFilter.accept(new FilterFactor(kw.isEmpty() ? null : kw, type, cat, avail));
    }
}
