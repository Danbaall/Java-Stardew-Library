package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.services.UserService;
import ir.ac.kntu.util.AsyncExecutor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ProfileView extends BorderPane {

    private final User currentUser;
    private final UserService userService;

    private TextField usernameField, emailField, phoneField;
    private PasswordField oldPasswordField, newPasswordField;
    private Label feedbackLabel;

    public ProfileView(User user, UserService userService) {
        this.currentUser = user;
        this.userService = userService;
        getStyleClass().add("content-view");
        setupUI();
    }

    private void setupUI() {
        // Top bar
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.getStyleClass().add("top-bar");
        Label title = new Label("Profile Settings");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("item-detail-label");
        topBar.getChildren().add(title);
        setTop(topBar);

        // Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(15);
        form.setPadding(new Insets(30));
        form.setAlignment(Pos.CENTER);

        usernameField = new TextField(currentUser.getUserName());
        emailField = new TextField(currentUser.getEmail());
        phoneField = new TextField(currentUser.getPhoneNumber());
        oldPasswordField = new PasswordField();
        newPasswordField = new PasswordField();
        feedbackLabel = new Label();
        feedbackLabel.getStyleClass().add("feedback-label");

        // Labels and inputs
        form.add(new Label("Username:"), 0, 0);
        form.add(usernameField, 1, 0);
        form.add(new Label("Email:"), 0, 1);
        form.add(emailField, 1, 1);
        form.add(new Label("Phone:"), 0, 2);
        form.add(phoneField, 1, 2);

        form.add(new Separator(), 0, 3, 2, 1);

        form.add(new Label("Old Password:"), 0, 4);
        form.add(oldPasswordField, 1, 4);
        form.add(new Label("New Password:"), 0, 5);
        form.add(newPasswordField, 1, 5);

        // Buttons
        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER);
        Button saveProfileBtn = new Button("Save Profile");
        saveProfileBtn.setStyle("-fx-background-color: #6a89a7; -fx-text-fill: white;");
        saveProfileBtn.setOnAction(e -> saveProfile());

        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        changePasswordBtn.setOnAction(e -> changePassword());

        buttonRow.getChildren().addAll(saveProfileBtn, changePasswordBtn);
        form.add(buttonRow, 0, 6, 2, 1);
        form.add(feedbackLabel, 0, 7, 2, 1);

        // Center wrapper
        VBox centerBox = new VBox(20, form);
        centerBox.setAlignment(Pos.TOP_CENTER);
        setCenter(centerBox);
    }

    private void saveProfile() {
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPhone = phoneField.getText().trim();

        if (!newUsername.equals(currentUser.getUserName())) {
            AsyncExecutor.execute(
                    () -> userService.updateUsername(currentUser, newUsername),
                    result -> {
                        if (result.isSuccess()) {
                            showFeedback(result.getMessage(), true);
                            currentUser.setUserName(newUsername);
                        } else {
                            showFeedback(result.getMessage(), false);
                        }
                    },
                    error -> showFeedback("Error: " + error.getMessage(), false));
        }
        if (!newEmail.equals(currentUser.getEmail())) {
            AsyncExecutor.execute(
                    () -> userService.updateEmail(currentUser, newEmail),
                    result -> showFeedback(result.getMessage(), result.isSuccess()),
                    error -> showFeedback("Error: " + error.getMessage(), false));
        }
        if (!newPhone.equals(currentUser.getPhoneNumber())) {
            AsyncExecutor.execute(
                    () -> userService.updatePhone(currentUser, newPhone),
                    result -> showFeedback(result.getMessage(), result.isSuccess()),
                    error -> showFeedback("Error: " + error.getMessage(), false));
        }
    }

    private void changePassword() {
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        if (oldPass.isEmpty() || newPass.isEmpty()) {
            showFeedback("Both password fields required.", false);
            return;
        }
        AsyncExecutor.execute(
                () -> userService.updatePassword(currentUser, oldPass, newPass),
                result -> showFeedback(result.getMessage(), result.isSuccess()),
                error -> showFeedback("Error: " + error.getMessage(), false));
    }

    private void showFeedback(String msg, boolean success) {
        feedbackLabel.setText(msg);
        feedbackLabel.getStyleClass().removeAll("error-text", "success-text");
        feedbackLabel.getStyleClass().add(success ? "success-text" : "error-text");
    }
}