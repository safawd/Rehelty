package tn.esprit.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.Personne;
import tn.esprit.services.ServicePersonne;

import java.io.IOException;
import java.util.List;

public class GestionAdmin {

    @FXML private Label lblTotalPeople;
    @FXML private Label lblDeletedAccounts;
    @FXML private Label lblTotalHotels;
    @FXML private Label lblGoogleUsers;
    @FXML private Label lblRecentUsers;
    @FXML private Label lblTotalPayments;
    @FXML private VBox statBoxPeople;
    @FXML private VBox statBoxDeleted;
    @FXML private VBox statBoxHotels;
    @FXML private VBox statBoxGoogleUsers;
    @FXML private VBox statBoxRecentUsers;
    @FXML private VBox statBoxPayments;
    @FXML private Button refreshButton;
    @FXML private Button displayButton;
    @FXML private Button logoutButton;
    @FXML private ScrollPane peopleScrollPane;
    @FXML private GridPane peopleGrid;
    @FXML private AnchorPane mainContent;
    @FXML private TextField searchField;
    @FXML private VBox personCardContainer;
    @FXML private Label searchStatusLabel;

    private final ServicePersonne servicePersonne = new ServicePersonne();

    @FXML
    public void initialize() {
        // Apply initial fade-in animation to stat boxes
        applyFadeAnimation(statBoxPeople, 0.0, 1.0, 1000);
        applyFadeAnimation(statBoxDeleted, 0.0, 1.0, 1200);
        applyFadeAnimation(statBoxHotels, 0.0, 1.0, 1400);
        applyFadeAnimation(statBoxGoogleUsers, 0.0, 1.0, 1600);
        applyFadeAnimation(statBoxRecentUsers, 0.0, 1.0, 1800);
        applyFadeAnimation(statBoxPayments, 0.0, 1.0, 2000);

        // Apply scale animation to buttons on load
        applyScaleAnimation(refreshButton, 0.9, 1.0, 800);
        applyScaleAnimation(displayButton, 0.9, 1.0, 800);
        applyScaleAnimation(logoutButton, 0.9, 1.0, 800);

        refreshStats();
    }

    @FXML
    private void handleSearchInput(KeyEvent event) {
        String input = searchField.getText().trim();
        personCardContainer.getChildren().clear();
        searchStatusLabel.setText("");

        if (input.isEmpty()) {
            return; // No input, clear card and message
        }

        try {
            int id = Integer.parseInt(input);
            Personne person = servicePersonne.getAll().stream()
                    .filter(p -> p.getIdPersonne() == id)
                    .findFirst()
                    .orElse(null);

            if (person != null) {
                // Create card for the person
                VBox card = new VBox(10);
                card.getStyleClass().add("person-card");

                Label nameLabel = new Label("Name: " + person.getNom() + " " + person.getPrenom());
                nameLabel.getStyleClass().add("person-label");
                Label emailLabel = new Label("Email: " + person.getEmail());
                emailLabel.getStyleClass().add("person-label");
                Label countryLabel = new Label("Country: " + person.getPays());
                countryLabel.getStyleClass().add("person-label");

                card.getChildren().addAll(nameLabel, emailLabel, countryLabel);
                card.setPrefWidth(300);
                card.setPrefHeight(100);

                // Add card to container
                personCardContainer.getChildren().add(card);
                applyFadeAnimation(card, 0.0, 1.0, 500);

                searchStatusLabel.setText("Person found!");
                searchStatusLabel.setStyle("-fx-text-fill: #28a745;");
            } else {
                searchStatusLabel.setText("Person not found.");
                searchStatusLabel.setStyle("-fx-text-fill: #FF4444;");
            }
        } catch (NumberFormatException e) {
            searchStatusLabel.setText("Please enter a valid ID.");
            searchStatusLabel.setStyle("-fx-text-fill: #FF4444;");
        }
    }

    @FXML
    private void refreshStats() {
        // Fetch total number of people
        List<Personne> people = servicePersonne.getAll();
        lblTotalPeople.setText(String.valueOf(people.size()));

        // Fetch deleted accounts (simulated as 0 for now)
        lblDeletedAccounts.setText("0");

        // Number of hotels (set to 0 as requested)
        lblTotalHotels.setText("0");

        // Fetch total Google users
        int googleUsers = servicePersonne.getCountGoogleUsers();
        lblGoogleUsers.setText(String.valueOf(googleUsers));

        // Fetch recently added users
        int recentUsers = servicePersonne.getCountRecentlyAdded();
        lblRecentUsers.setText(String.valueOf(recentUsers));

        // Total payments (placeholder)
        double totalPayments = servicePersonne.getTotalPaymentsAmount();
        lblTotalPayments.setText(String.format("$%.2f", totalPayments));

        // Re-apply fade animation on refresh
        applyFadeAnimation(statBoxPeople, 1.0, 0.0, 200);
        applyFadeAnimation(statBoxPeople, 0.0, 1.0, 400);
        applyFadeAnimation(statBoxDeleted, 1.0, 0.0, 200);
        applyFadeAnimation(statBoxDeleted, 0.0, 1.0, 500);
        applyFadeAnimation(statBoxHotels, 1.0, 0.0, 200);
        applyFadeAnimation(statBoxHotels, 0.0, 1.0, 600);
        applyFadeAnimation(statBoxGoogleUsers, 1.0, 0.0, 200);
        applyFadeAnimation(statBoxGoogleUsers, 0.0, 1.0, 700);
        applyFadeAnimation(statBoxRecentUsers, 1.0, 0.0, 200);
        applyFadeAnimation(statBoxRecentUsers, 0.0, 1.0, 800);
        applyFadeAnimation(statBoxPayments, 1.0, 0.0, 200);
        applyFadeAnimation(statBoxPayments, 0.0, 1.0, 900);

        // Apply scale animation to buttons on refresh
        applyScaleAnimation(refreshButton, 1.0, 1.1, 200);
        applyScaleAnimation(refreshButton, 1.1, 1.0, 200);
    }

    @FXML
    private void displayPeople() {
        // Clear previous cards and search results
        peopleGrid.getChildren().clear();
        peopleGrid.getRowConstraints().clear();
        personCardContainer.getChildren().clear();
        searchStatusLabel.setText("");

        // Fetch all people
        List<Personne> people = servicePersonne.getAll();
        int row = 0;
        int col = 0;

        for (Personne person : people) {
            // Create a card for each person
            VBox card = new VBox(10);
            card.getStyleClass().add("person-card");

            Label nameLabel = new Label("Name: " + person.getNom());
            nameLabel.getStyleClass().add("person-label");
            Label emailLabel = new Label("Email: " + person.getEmail());
            emailLabel.getStyleClass().add("person-label");
            Label countryLabel = new Label("Country: " + person.getPays());
            countryLabel.getStyleClass().add("person-label");

            card.getChildren().addAll(nameLabel, emailLabel, countryLabel);
            card.setPrefWidth(200);
            card.setPrefHeight(100);

            // Add the card to the grid
            peopleGrid.add(card, col, row);
            applyFadeAnimation(card, 0.0, 1.0, 500);

            col++;
            if (col > 2) {
                col = 0;
                row++;
                peopleGrid.getRowConstraints().add(new RowConstraints());
            }
        }

        // Show the ScrollPane
        peopleScrollPane.setVisible(true);
    }

    @FXML
    private void adminAddPerson() {
        try {
            String path = "/AdminAddPerson.fxml";
            System.out.println("Loading FXML: " + path);
            System.out.println("Resource URL: " + getClass().getResource(path));
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load Add Person interface: " + e.getMessage());
        }
    }

    @FXML
    private void adminEditPerson() {
        try {
            String path = "/AdminEditPerson.fxml";
            System.out.println("Loading FXML: " + path);
            System.out.println("Resource URL: " + getClass().getResource(path));
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load Edit Person interface: " + e.getMessage());
        }
    }

    @FXML
    private void adminDeletePerson() {
        try {
            String path = "/AdminDeletePerson.fxml";
            System.out.println("Loading FXML: " + path);
            System.out.println("Resource URL: " + getClass().getResource(path));
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load Delete Person interface: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            // Apply fade-out animation before logout
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), mainContent);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionLogin.fxml"));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) mainContent.getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Failed to load Login interface: " + e.getMessage());
                }
            });
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error during logout: " + e.getMessage());
        }
    }

    // Helper method for fade animation
    private void applyFadeAnimation(Node node, double from, double to, int duration) {
        if (node != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(duration), node);
            fade.setFromValue(from);
            fade.setToValue(to);
            fade.play();
        }
    }

    // Helper method for scale animation
    private void applyScaleAnimation(Node node, double from, double to, int duration) {
        if (node != null) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(duration), node);
            scale.setFromX(from);
            scale.setFromY(from);
            scale.setToX(to);
            scale.setToY(to);
            scale.play();
        }
    }

    // Helper method to show error alerts
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private VBox sidebar;


    // ... autres variables FXML existantes ...

    private boolean isSidebarExpanded = true;

    @FXML
    public void toggleSidebar() {
        isSidebarExpanded = !isSidebarExpanded;

        // Créer une timeline pour l'animation
        Timeline timeline = new Timeline();

        if (isSidebarExpanded) {
            // Expansion de la sidebar
            KeyValue kvSidebar = new KeyValue(sidebar.prefWidthProperty(), 250, Interpolator.EASE_BOTH);
            KeyFrame kf = new KeyFrame(Duration.millis(300), kvSidebar);
            timeline.getKeyFrames().add(kf);

            timeline.setOnFinished(e -> {
                sidebar.getStyleClass().remove("compact");
                AnchorPane.setLeftAnchor(mainContent, 250.0);
            });
        } else {
            // Réduction de la sidebar
            sidebar.getStyleClass().add("compact");

            KeyValue kvSidebar = new KeyValue(sidebar.prefWidthProperty(), 70, Interpolator.EASE_BOTH);
            KeyFrame kf = new KeyFrame(Duration.millis(300), kvSidebar);
            timeline.getKeyFrames().add(kf);

            timeline.setOnFinished(e -> {
                AnchorPane.setLeftAnchor(mainContent, 70.0);
            });
        }

        timeline.play();
    }

}