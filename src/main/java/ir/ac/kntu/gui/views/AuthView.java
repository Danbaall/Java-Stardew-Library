package ir.ac.kntu.gui.views;

import ir.ac.kntu.entities.module.User;
import ir.ac.kntu.exceptions.AccountDisabledException;
import ir.ac.kntu.services.AuthService;
import ir.ac.kntu.util.AsyncExecutor;
import ir.ac.kntu.util.ServiceResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;

public class AuthView extends VBox {

    private final AuthService authService;
    private final Consumer<User> onAuthSuccess;
    private final Runnable onBack;

    private Label feedbackLabel;
    private ComboBox<String> roleComboBox;
    private TextField extraField;

    public AuthView(AuthService authService, Consumer<User> onAuthSuccess, Runnable onBack) {
        this.authService = authService;
        this.onAuthSuccess = onAuthSuccess;
        this.onBack = onBack;
        setupUI();
    }

    private void setupUI() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(30));
        getStyleClass().add("auth-container");

        Button backBtn = new Button("← Back to Catalog");
        backBtn.getStyleClass().add("theme-toggle-button");
        backBtn.setOnAction(e -> onBack.run());
        HBox backBox = new HBox(backBtn);
        backBox.setAlignment(Pos.CENTER_LEFT);
        backBox.setMaxWidth(Double.MAX_VALUE);
        backBox.setPadding(new Insets(0, 0, 20, 0)); // space below

        VBox authCard = new VBox(20);
        authCard.setMaxWidth(400);
        authCard.getStyleClass().add("auth-card");
        authCard.setAlignment(Pos.CENTER);
        authCard.setMinHeight(550);
        authCard.setMaxHeight(VBox.USE_PREF_SIZE);

        feedbackLabel = new Label();
        feedbackLabel.getStyleClass().add("feedback-label");

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("auth-tab-pane");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMinHeight(500);
        tabPane.setPrefHeight(500);

        Tab signInTab = new Tab("Sign In", createSignInForm());
        Tab signUpTab = new Tab("Sign Up", createSignUpForm());
        tabPane.getTabs().addAll(signInTab, signUpTab);

        authCard.getChildren().addAll(tabPane, feedbackLabel);

        getChildren().addAll(backBox, authCard);
        setAlignment(Pos.TOP_CENTER);
    }

    private VBox createSignInForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20, 10, 10, 10));

        TextField identifierInput = new TextField();
        identifierInput.setPromptText("Email, Phone, or Username");

        VBox passwordBox = createPasswordFieldWithToggle("Password");

        Button submitBtn = new Button("Sign In");
        submitBtn.getStyleClass().add("auth-submit-btn");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            PasswordField pf = (PasswordField) passwordBox.lookup(".password-field");
            if (pf == null) {
                StackPane stack = (StackPane) passwordBox.getChildren().get(0);
                pf = (PasswordField) stack.getChildren().get(0);
            }
            handleSignIn(identifierInput.getText(), pf.getText());
        });

        form.getChildren().addAll(identifierInput, passwordBox, submitBtn);
        return form;
    }

    private VBox createSignUpForm() {
        VBox form = new VBox(12);
        form.setPadding(new Insets(20, 10, 10, 10));

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");

        TextField emailInput = new TextField();
        emailInput.setPromptText("Email");

        TextField phoneInput = new TextField();
        phoneInput.setPromptText("Phone Number");

        VBox passwordBox = createPasswordFieldWithToggle("Password");

        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Guest", "Student", "Professor");
        roleComboBox.setValue("Guest");
        roleComboBox.setMaxWidth(Double.MAX_VALUE);

        StackPane extraFieldWrapper = new StackPane();
        extraFieldWrapper.setMinHeight(45);
        extraFieldWrapper.setMaxHeight(45);
        extraField = new TextField();
        extraField.setVisible(false);
        extraFieldWrapper.getChildren().add(extraField);
        roleComboBox.setOnAction(e -> updateRoleFields());

        Button submitBtn = new Button("Register Account");
        submitBtn.getStyleClass().add("auth-submit-btn");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            PasswordField pf = (PasswordField) passwordBox.lookup(".password-field");
            if (pf == null) {
                StackPane stack = (StackPane) passwordBox.getChildren().get(0);
                pf = (PasswordField) stack.getChildren().get(0);
            }
            handleSignUp(usernameInput.getText(), emailInput.getText(),
                    phoneInput.getText(), pf.getText());
        });

        form.getChildren().addAll(
                usernameInput, emailInput, phoneInput,
                passwordBox, roleComboBox, extraFieldWrapper, submitBtn);
        return form;
    }

    private VBox createPasswordFieldWithToggle(String promptText) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(promptText);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.getStyleClass().add("password-field");

        TextField visibleField = new TextField();
        visibleField.setPromptText(promptText);
        visibleField.setMaxWidth(Double.MAX_VALUE);
        visibleField.setVisible(false);
        visibleField.setManaged(false);

        StackPane fieldStack = new StackPane(passwordField, visibleField);

        CheckBox showCheck = new CheckBox("Show password");
        showCheck.getStyleClass().add("show-password-checkbox");
        showCheck.selectedProperty().addListener((obs, old, selected) -> {
            if (selected) {
                visibleField.setText(passwordField.getText());
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                visibleField.setVisible(true);
                visibleField.setManaged(true);
            } else {
                passwordField.setText(visibleField.getText());
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                visibleField.setVisible(false);
                visibleField.setManaged(false);
            }
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!showCheck.isSelected())
                visibleField.setText(newVal);
        });
        visibleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (showCheck.isSelected())
                passwordField.setText(newVal);
        });

        VBox box = new VBox(5, fieldStack, showCheck);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void updateRoleFields() {
        String selectedRole = roleComboBox.getValue();
        if ("Student".equals(selectedRole)) {
            extraField.setPromptText("Student ID");
            extraField.setVisible(true);
        } else if ("Professor".equals(selectedRole)) {
            extraField.setPromptText("Department");
            extraField.setVisible(true);
        } else {
            extraField.setVisible(false);
            extraField.clear();
        }
    }

    private void handleSignIn(String identifier, String password) {
        AsyncExecutor.execute(
                () -> {
                    try {
                        return authService.signIn(identifier, password);
                    } catch (AccountDisabledException ex) {
                        return ServiceResult.<User>failure("Account is disabled: " + ex.getMessage());
                    }
                },
                result -> {
                    if (result.isSuccess()) {
                        onAuthSuccess.accept(result.getData());
                    } else {
                        showError(result.getMessage());
                    }
                },
                error -> showError("Sign in failed: " + error.getMessage()));
    }

    private void handleSignUp(String username, String email, String phone, String password) {
        String role = roleComboBox.getValue();
        String combined = phone + "::" + extraField.getText();

        AsyncExecutor.execute(
                () -> switch (role) {
                    case "Student" -> authService.registerStudent(username, email, password, combined);
                    case "Professor" -> authService.registerProfessor(username, email, password, combined);
                    default -> authService.registerGuest(username, email, phone, password);
                },
                result -> {
                    if (result.isSuccess()) {
                        onAuthSuccess.accept(result.getData());
                    } else {
                        showError(result.getMessage());
                    }
                },
                error -> showError("Registration failed: " + error.getMessage()));
    }

    private void showError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().removeAll("success-text");
        if (!feedbackLabel.getStyleClass().contains("error-text")) {
            feedbackLabel.getStyleClass().add("error-text");
        }
    }
}