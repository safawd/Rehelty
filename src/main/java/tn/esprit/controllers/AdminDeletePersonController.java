package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Personne;
import tn.esprit.services.ServicePersonne;

import java.io.IOException;

public class AdminDeletePersonController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private VBox personCard;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label countryLabel;
    @FXML private Button deleteButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;

    private final ServicePersonne servicePersonne = new ServicePersonne();
    private Personne currentPerson;

    @FXML
    private void searchPerson() {
        try {
            int id = Integer.parseInt(searchField.getText());
            currentPerson = servicePersonne.getAll().stream()
                    .filter(p -> p.getIdPersonne() == id)
                    .findFirst()
                    .orElse(null);

            if (currentPerson != null) {
                // Populate card with person's details
                nameLabel.setText("Name: " + currentPerson.getNom() + " " + currentPerson.getPrenom());
                emailLabel.setText("Email: " + currentPerson.getEmail());
                countryLabel.setText("Country: " + currentPerson.getPays());

                personCard.setVisible(true);
                deleteButton.setVisible(true);
                statusLabel.setText("Person found!");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                personCard.setVisible(false);
                deleteButton.setVisible(false);
                statusLabel.setText("Person not found.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid ID.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void adminDelete() {
        try {
            if (currentPerson == null) {
                statusLabel.setText("No person selected.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            // Delete person from database
            servicePersonne.delete(currentPerson);

            statusLabel.setText("Person deleted successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");

            // Clear form
            personCard.setVisible(false);
            deleteButton.setVisible(false);
            searchField.clear();
            currentPerson = null;

        } catch (Exception e) {
            statusLabel.setText("Error deleting person: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionAdmin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error returning to dashboard: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}