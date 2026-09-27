package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.enums.TicketStatus;
import ir.ac.kntu.entities.enums.TicketType;
import ir.ac.kntu.entities.module.Admin;
import ir.ac.kntu.entities.module.Ticket;
import ir.ac.kntu.services.TicketService;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AdminDashboardView extends BorderPane {

    private final Admin admin;
    private final TicketService ticketService;

    private Label openCountLabel;
    private Label closedCountLabel;
    private Label totalCountLabel;
    private BarChart<String, Number> chart;

    public AdminDashboardView(Admin admin, TicketService ticketService) {
        this.admin = admin;
        this.ticketService = ticketService;
        getStyleClass().add("content-view");
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Top bar
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15));
        topBar.getStyleClass().add("top-bar");
        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("item-detail-label");
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #6a89a7; -fx-text-fill: white; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadData());
        topBar.getChildren().addAll(title, topSpacer, refreshBtn);
        setTop(topBar);

        // Stat cards
        openCountLabel = new Label("—");
        closedCountLabel = new Label("—");
        totalCountLabel = new Label("—");

        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(20, 20, 10, 20));
        statsRow.setAlignment(Pos.TOP_LEFT);
        statsRow.getChildren().addAll(
                statCard("Open / In Progress", openCountLabel, "#3498db"),
                statCard("Closed Tickets", closedCountLabel, "#3498db"),
                statCard("My Queue (Total)", totalCountLabel, "#3498db"));

        // Bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day");
        yAxis.setLabel("Tickets Created");
        yAxis.setMinorTickVisible(false);

        chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Ticket Activity — Last 7 Days");
        chart.setAnimated(false);
        chart.getStyleClass().add("admin-chart");
        chart.setLegendVisible(true);
        chart.setPrefHeight(340);
        VBox.setVgrow(chart, Priority.ALWAYS);

        VBox center = new VBox(0, statsRow, chart);
        center.setPadding(new Insets(0, 20, 20, 20));
        VBox.setVgrow(chart, Priority.ALWAYS);
        setCenter(center);
    }

    private VBox statCard(String label, Label valueLabel, String accentColor) {
        VBox card = new VBox(6);
        card.getStyleClass().add("admin-stat-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.setPrefHeight(90);

        valueLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("item-detail-label");
        lbl.setStyle("-fx-font-size: 12px;");
        card.getChildren().addAll(valueLabel, lbl);
        return card;
    }

    public void loadData() {
        AsyncExecutor.execute(
                () -> ticketService.getTicketsForAdmin(admin).getData(),
                tickets -> {
                    // Stats
                    long open = tickets.stream()
                            .filter(t -> t.getStatus() == TicketStatus.OPEN
                                    || t.getStatus() == TicketStatus.RESOLVING)
                            .count();
                    long closed = tickets.stream()
                            .filter(t -> t.getStatus() == TicketStatus.CLOSED)
                            .count();
                    openCountLabel.setText(String.valueOf(open));
                    closedCountLabel.setText(String.valueOf(closed));
                    totalCountLabel.setText(String.valueOf(tickets.size()));

                    // Chart
                    buildChart(tickets);
                },
                err -> {
                    /* nothing */ });
    }

    private void buildChart(List<Ticket> tickets) {
        chart.getData().clear();

        LocalDate today = LocalDate.now();
        List<LocalDate> days = IntStream.range(0, 7)
                .mapToObj(i -> today.minusDays(6 - i))
                .collect(Collectors.toList());
        List<String> dayLabels = days.stream()
                .map(d -> d.format(DateTimeFormatter.ofPattern("MMM d")))
                .collect(Collectors.toList());

        // count by date last 7 days
        Map<LocalDate, Map<TicketType, Long>> byDayType = tickets.stream()
                .filter(t -> t.getCreatedAt() != null)
                .filter(t -> !t.getCreatedAt().toLocalDate().isBefore(today.minusDays(6)))
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toLocalDate(),
                        Collectors.groupingBy(Ticket::getTicketType, Collectors.counting())));

        for (TicketType type : TicketType.values()) {
            if (!admin.canHandle(type))
                continue;

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(capitalize(type.name()));

            for (int i = 0; i < 7; i++) {
                long count = byDayType
                        .getOrDefault(days.get(i), Map.of())
                        .getOrDefault(type, 0L);
                series.getData().add(new XYChart.Data<>(dayLabels.get(i), count));
            }
            chart.getData().add(series);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
