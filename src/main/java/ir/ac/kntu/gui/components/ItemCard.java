package ir.ac.kntu.gui.components;

import ir.ac.kntu.entities.module.Item;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ItemCard extends VBox {

    public ItemCard(Item libraryItem) {
        this.getStyleClass().add("item-card");
        this.setPrefWidth(160);
        this.setPrefHeight(250);
        this.setAlignment(Pos.CENTER);

        ImageView coverView = this.createImageView(libraryItem.getCoverImageUrl());
        Label titleLabel = this.createTitleLabel(libraryItem);
        Label authorLabel = this.createAuthorLabel(libraryItem);

        this.getChildren().addAll(coverView, titleLabel, authorLabel);
    }

    private ImageView createImageView(String imageUrl) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(110);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                imageView.setImage(new Image(imageUrl, true));
            } else {
                imageView.setImage(new Image("file:src/main/resources/images/placeholder.png", true));
            }
        } catch (Exception exception) {
            System.err.println("Failed to load image: " + imageUrl);
        }

        return imageView;
    }

    private Label createTitleLabel(Item libraryItem) {
        Label titleLabel = new Label(libraryItem.getTitle());
        titleLabel.getStyleClass().add("item-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(140);
        return titleLabel;
    }

    private Label createAuthorLabel(Item libraryItem) {
        String authorName = "Unknown Author";
        try {
            authorName = libraryItem.getAuthor();
        } catch (Exception ignored) {
        }

        Label authorLabel = new Label(authorName);
        authorLabel.getStyleClass().add("item-author");
        authorLabel.setWrapText(true);
        authorLabel.setMaxWidth(140);
        return authorLabel;
    }
}