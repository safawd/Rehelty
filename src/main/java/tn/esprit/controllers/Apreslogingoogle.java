package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class Apreslogingoogle {

    @FXML
    private TextField codeField;  // Champ pour saisir le code d'autorisation

    // Méthode appelée lorsque l'utilisateur clique sur le bouton "Soumettre"
    @FXML
    private void onSubmitCode(ActionEvent event) {
        String authorizationCode = codeField.getText();

        // Vérifier si le code n'est pas vide
        if (authorizationCode != null && !authorizationCode.isEmpty()) {
            // Appeler une méthode pour échanger le code d'autorisation contre un token
            exchangeAuthorizationCodeForToken(authorizationCode);
        } else {
            // Afficher un message d'erreur si le champ est vide
            showError("Veuillez entrer le code d'autorisation.");
        }
    }

    // Méthode pour afficher des messages d'erreur
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour échanger le code d'autorisation
    private void exchangeAuthorizationCodeForToken(String authorizationCode) {
        // Ici vous implémentez l'échange du code contre un token d'accès
        // Cela dépend de la bibliothèque Google OAuth que vous utilisez
        // Nous allons simuler une authentification réussie

        boolean isAuthenticated = simulateGoogleAuthentication(authorizationCode);

        if (isAuthenticated) {
            // Si l'authentification est réussie, rediriger vers la page d'accueil
            navigateToHome();
        } else {
            showError("Échec de l'authentification. Le code peut être incorrect.");
        }
    }

    // Méthode simulant une authentification réussie
    private boolean simulateGoogleAuthentication(String authorizationCode) {
        // Simulation d'une authentification réussie
        return true;  // Remplacez ceci par l'authentification réelle
    }

    // Méthode pour rediriger vers la page d'accueil
    private void navigateToHome() {
        System.out.println("Redirection vers la page d'accueil...");
        // Implémentez ici la logique pour rediriger vers la page d'accueil
    }
}
