package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Personne;
import tn.esprit.services.ServicePersonne;

import java.io.IOException;

public class AdminAddPersonController {

    @FXML private TextField emailField;
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private TextField paysField;
    @FXML private TextField idTypeField;
    @FXML private TextField idNumberField;
    @FXML private CheckBox chkGoogleUser;
    @FXML private TextField txtGoogleUserId;
    @FXML private Label statusLabel;
    @FXML private Button addButton;
    @FXML private Button backButton;

    private final ServicePersonne servicePersonne = new ServicePersonne();

    @FXML
    public void initialize() {
        // Bind Google User ID field to checkbox
        if (chkGoogleUser != null && txtGoogleUserId != null) {
            txtGoogleUserId.disableProperty().bind(chkGoogleUser.selectedProperty().not());
        }
    }

    @FXML
    private void adminAddPerson() {
        try {
            // Validate input
            if (emailField.getText().isEmpty() || prenomField.getText().isEmpty() || nomField.getText().isEmpty() ||
                    passwordField.getText().isEmpty()) {
                statusLabel.setText("Please fill in all required fields.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            // Convert the telephone field to int
            int telephone = 0;
            if (!telephoneField.getText().isEmpty()) {
                try {
                    telephone = Integer.parseInt(telephoneField.getText());
                } catch (NumberFormatException e) {
                    statusLabel.setText("Invalid phone number.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
            }

            // Create new Personne object
            Personne personne = new Personne();
            personne.setEmail(emailField.getText());
            personne.setPrenom(prenomField.getText());
            personne.setNom(nomField.getText());
            personne.setTelephone(telephone); // Store the integer value of phone number
            personne.setMotDePasse(passwordField.getText());
            personne.setPays(paysField.getText());
            personne.setIdType(idTypeField.getText());
            personne.setIdNumber(idNumberField.getText());
            personne.setGoogleUser(chkGoogleUser.isSelected());
            personne.setGoogleUserId(chkGoogleUser.isSelected() ? txtGoogleUserId.getText() : null);

            // Add person using ServicePersonne
            servicePersonne.add(personne);

            statusLabel.setText("Person added successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");

            // Clear fields
            emailField.clear();
            prenomField.clear();
            nomField.clear();
            telephoneField.clear();
            passwordField.clear();
            paysField.clear();
            idTypeField.clear();
            idNumberField.clear();
            chkGoogleUser.setSelected(false);
            txtGoogleUserId.clear();

        } catch (Exception e) {
            statusLabel.setText("Error adding person: " + e.getMessage());
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
