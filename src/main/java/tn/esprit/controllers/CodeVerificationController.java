package tn.esprit.controllers;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.controllers.GestionLogin;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import tn.esprit.models.Personne;
import tn.esprit.services.ServicePersonne;

import java.io.IOException;

public class CodeVerificationController {

    @FXML
    private TextField verificationCodeField;

    private final ServicePersonne servicePersonne = new ServicePersonne();

    @FXML
    private void onVerifyCode(ActionEvent event) {
        System.out.println("La méthode onVerifyCode a été appelée !");

        String enteredCode = verificationCodeField.getText().trim();

        if (enteredCode.isEmpty()) {
            showError("Veuillez entrer le code de vérification.");
            return;
        }

        boolean isCodeValid = servicePersonne.verifyCode(user.getEmail(), enteredCode);

        if (isCodeValid) {
            showSuccess("Code de vérification correct !");
            redirectToHome(event, user);

        } else {
            showError("Code incorrect. Essayez à nouveau.");
        }
    }
    private void redirectToHome(ActionEvent event, Personne user) {
        try {
            // Charger le fichier FXML pour la page d'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur au contrôleur de la page d'accueil
            Home homeController = loader.getController();
            homeController.setPersonne(user);  // Passer l'objet Personne au contrôleur de la page d'accueil

            // Changer de scène et afficher la page d'accueil
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Personne user; // To store the current user

    // This method will allow you to set the user from the previous screen
    public void setUser(Personne user) {
        this.user = user;
    }
}
