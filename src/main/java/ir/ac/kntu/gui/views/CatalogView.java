package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.module.FilterFactor;
import ir.ac.kntu.entities.module.Item;
import ir.ac.kntu.gui.components.CatalogSearchBar;
import ir.ac.kntu.gui.components.ItemCard;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CatalogView extends BorderPane {

    private final ItemRepo itemRepo;
    private final Consumer<Item> onItemClick;

    private List<Item> allItems;
    private List<Item> cachedItems;

    private static final int ITEMS_PER_PAGE = 15;

    private final CatalogSearchBar searchBar;

    public CatalogView(ItemRepo itemRepo, Consumer<Item> onItemClick) {
        this.itemRepo = itemRepo;
        this.onItemClick = onItemClick;
        this.searchBar = new CatalogSearchBar(this::applyFilter);
        setupUI();
    }

    private void setupUI() {
        setTop(searchBar);
        setCenter(new Label("Loading Catalog…"));

        AsyncExecutor.execute(
                () -> itemRepo.findAll(),
                items -> {
                    allItems = items;
                    List<String> categories = allItems.stream()
                            .map(Item::getCategory)
                            .filter(c -> c != null && !c.isBlank())
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());
                    searchBar.setCategories(categories);
                    cachedItems = allItems;
                    rebuildPagination();
                },
                error -> System.err.println("Error fetching items: " + error.getMessage()));
    }

    private void applyFilter(FilterFactor filter) {
        if (allItems == null)
            return;
        cachedItems = allItems.stream()
                .filter(filter::matches)
                .collect(Collectors.toList());
        rebuildPagination();
    }

    public void reload() {
        allItems = null;
        cachedItems = null;
        setCenter(new Label("Refreshing Catalog…"));
        AsyncExecutor.execute(
                () -> itemRepo.findAll(),
                items -> {
                    allItems = items;
                    List<String> categories = allItems.stream()
                            .map(Item::getCategory)
                            .filter(c -> c != null && !c.isBlank())
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());
                    searchBar.setCategories(categories);
                    cachedItems = allItems;
                    rebuildPagination();
                },
                error -> System.err.println("Error reloading items: " + error.getMessage()));
    }

    private void rebuildPagination() {
        int pageCount = (cachedItems == null || cachedItems.isEmpty())
                ? 1
                : (int) Math.ceil((double) cachedItems.size() / ITEMS_PER_PAGE);
        Pagination pagination = new Pagination(pageCount, 0);
        pagination.setPageFactory(this::createPage);
        setCenter(pagination);
    }

    private Node createPage(Integer pageIndex) {
        TilePane grid = new TilePane();
        grid.setPadding(new Insets(20));
        grid.setHgap(30);
        grid.setVgap(30);
        grid.setAlignment(Pos.TOP_CENTER);

        if (cachedItems != null && !cachedItems.isEmpty()) {
            int start = pageIndex * ITEMS_PER_PAGE;
            int end = Math.min(start + ITEMS_PER_PAGE, cachedItems.size());
            for (Item item : cachedItems.subList(start, end)) {
                ItemCard card = new ItemCard(item);
                card.setOnMouseClicked(e -> {
                    if (onItemClick != null)
                        onItemClick.accept(item);
                });
                grid.getChildren().add(card);
            }
        } else {
            Label empty = new Label("No items match your search.");
            empty.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px;");
            grid.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }
}
