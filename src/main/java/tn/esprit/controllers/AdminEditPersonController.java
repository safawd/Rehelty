package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Personne;
import tn.esprit.services.ServicePersonne;

import java.io.IOException;

public class AdminEditPersonController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private VBox personCard;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label countryLabel;
    @FXML private GridPane editForm;
    @FXML private TextField emailField;
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private TextField paysField;
    @FXML private TextField idTypeField;
    @FXML private TextField idNumberField;
    @FXML private CheckBox chkEditGoogleUser;
    @FXML private TextField txtEditGoogleUserId;
    @FXML private Button updateButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;

    private final ServicePersonne servicePersonne = new ServicePersonne();
    private Personne currentPerson;

    @FXML
    public void initialize() {
        // Bind Google User ID field to checkbox
        if (chkEditGoogleUser != null && txtEditGoogleUserId != null) {
            txtEditGoogleUserId.disableProperty().bind(chkEditGoogleUser.selectedProperty().not());
        }
    }

    @FXML
    private void searchPerson() {
        try {
            int id = Integer.parseInt(searchField.getText());
            currentPerson = servicePersonne.getAll().stream()
                    .filter(p -> p.getIdPersonne() == id)
                    .findFirst()
                    .orElse(null);

            if (currentPerson != null) {
                // Populate card
                nameLabel.setText("Name: " + currentPerson.getNom() + " " + currentPerson.getPrenom());
                emailLabel.setText("Email: " + currentPerson.getEmail());
                countryLabel.setText("Country: " + currentPerson.getPays());

                // Populate form
                emailField.setText(currentPerson.getEmail());
                prenomField.setText(currentPerson.getPrenom());
                nomField.setText(currentPerson.getNom());
                telephoneField.setText(String.valueOf(currentPerson.getTelephone()));  // Ensure telephone is displayed as string
                passwordField.setText(currentPerson.getMotDePasse());
                paysField.setText(currentPerson.getPays());
                idTypeField.setText(currentPerson.getIdType());
                idNumberField.setText(currentPerson.getIdNumber());
                chkEditGoogleUser.setSelected(currentPerson.isGoogleUser());
                txtEditGoogleUserId.setText(currentPerson.getGoogleUserId() != null ? currentPerson.getGoogleUserId() : "");

                personCard.setVisible(true);
                editForm.setVisible(true);
                updateButton.setVisible(true);
                statusLabel.setText("Person found!");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                personCard.setVisible(false);
                editForm.setVisible(false);
                updateButton.setVisible(false);
                statusLabel.setText("Person not found.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid ID.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void adminEdit() {
        try {
            if (currentPerson == null) {
                statusLabel.setText("No person selected.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            // Convert telephone field to integer
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

            // Update person details
            currentPerson.setEmail(emailField.getText());
            currentPerson.setPrenom(prenomField.getText());
            currentPerson.setNom(nomField.getText());
            currentPerson.setTelephone(telephone);  // Set the integer value for telephone
            currentPerson.setMotDePasse(passwordField.getText());
            currentPerson.setPays(paysField.getText());
            currentPerson.setIdType(idTypeField.getText());
            currentPerson.setIdNumber(idNumberField.getText());
            currentPerson.setGoogleUser(chkEditGoogleUser.isSelected());
            currentPerson.setGoogleUserId(chkEditGoogleUser.isSelected() ? txtEditGoogleUserId.getText() : null);

            // Update in database
            servicePersonne.update(currentPerson);

            statusLabel.setText("Person updated successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");

            // Clear form
            personCard.setVisible(false);
            editForm.setVisible(false);
            updateButton.setVisible(false);
            searchField.clear();
            currentPerson = null;

        } catch (Exception e) {
            statusLabel.setText("Error updating person: " + e.getMessage());
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
