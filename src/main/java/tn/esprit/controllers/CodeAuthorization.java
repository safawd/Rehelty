package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.services.GoogleAuthService;
import tn.esprit.services.GoogleAuthService.GoogleUser;
import tn.esprit.models.Personne;
import tn.esprit.services.ServicePersonne;

import java.io.IOException;

public class CodeAuthorization {

    @FXML
    private TextField authCodeField;

    private GoogleAuthService googleAuthService;
    private final ServicePersonne servicePersonne = new ServicePersonne();
    private GoogleUser currentGoogleUser;

    public CodeAuthorization() {
        googleAuthService = new GoogleAuthService();  // Initialiser le service GoogleAuth
    }
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    @FXML
    private void onSubmitAuthCode() {
        String authCode = authCodeField.getText().trim();

        if (authCode.isEmpty()) {
            showError("Le code d'autorisation ne peut pas être vide.");
            return;
        }

        // Échanger le code d'autorisation contre un token
        googleAuthService.exchangeCodeForToken(authCode)
                .thenAccept(googleUser -> {
                    if (googleUser != null) {
                        // Créer un objet Personne avec les informations de l'utilisateur Google
                        Personne personne = new Personne();
                        personne.setNom(googleUser.getGivenName());
                        personne.setPrenom(googleUser.getFamilyName());
                        personne.setEmail(googleUser.getEmail());
                        personne.setGoogleUser(true); // Marquer cet utilisateur comme venant de Google
                        personne.setGoogleUserId(googleUser.getId());

                        // Générer un mot de passe aléatoire pour l'utilisateur
                        String generatedPassword = generateRandomPassword();
                        personne.setMotDePasse(generatedPassword);  // Affecter le mot de passe généré

                        // Enregistrer l'utilisateur dans la base de données
                        try {
                            servicePersonne.add(personne);  // Méthode pour ajouter l'utilisateur dans la base

                            showSuccess("Inscription réussie avec Google!");
                            // Rediriger vers la page d'accueil ou une autre page après l'inscription
                            openHomePage(personne);

                        } catch (Exception e) {
                            showError("Erreur lors de l'enregistrement de l'utilisateur.");
                            e.printStackTrace();
                        }
                    } else {
                        showError("Erreur lors de l'échange du code d'autorisation.");
                    }
                })
                .exceptionally(ex -> {
                    showError("Erreur lors de l'échange du code: " + ex.getMessage());
                    return null;
                });
    }


    private void handleSuccessfulLogin(GoogleUser googleUser) {
        // Créer un objet Personne avec les informations de l'utilisateur Google
        Personne personne = new Personne();
        personne.setNom(googleUser.getGivenName());
        personne.setPrenom(googleUser.getFamilyName());
        personne.setEmail(googleUser.getEmail());
        personne.setGoogleUser(true);  // Marquer cet utilisateur comme un utilisateur Google
        personne.setGoogleUserId(googleUser.getId());  // Sauvegarder l'ID Google

        // Si le numéro de téléphone est vide, affecter une valeur par défaut
        if (personne.getTelephone() == 0) {
            personne.setTelephone(0);  // Exemple de numéro par défaut (si c'est un entier)
        } else {
            personne.setTelephone(0); // Si c'est une chaîne, la convertir en entier
        }

        // Si le pays est vide, affecter une valeur par défaut
        if (personne.getPays() == null || personne.getPays().isEmpty()) {
            personne.setPays("Inconnu");  // Exemple de pays par défaut
        }

        // Enregistrer l'utilisateur dans la base de données
        try {
            // Ajouter l'utilisateur dans la base de données
            servicePersonne.add(personne);  // Remplacer par votre méthode d'ajout

            // Si l'utilisateur a été ajouté avec succès, fermer la fenêtre actuelle de saisie du code
            Stage stage = (Stage) authCodeField.getScene().getWindow();
            stage.close();

            // Ouvrir la page d'accueil avec l'utilisateur qui vient d'être ajouté
            openHomePage(personne);

        } catch (Exception e) {
            showError("Erreur lors de l'enregistrement de l'utilisateur : " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void openHomePage(Personne personne) {
        try {
            // Charger la vue de la page d'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la page d'accueil
            Home controller = loader.getController();
            controller.setUtilisateur(personne);  // Passer l'utilisateur à la page d'accueil

            // Créer une nouvelle fenêtre pour afficher la page d'accueil
            Stage homeStage = new Stage();
            homeStage.setScene(new Scene(root));
            homeStage.show();  // Afficher la page d'accueil

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture de la page d'accueil.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        // Crée une alerte de type information pour le succès
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("Opération réussie");
        alert.setContentText(message);

        // Affiche l'alerte
        alert.showAndWait();
    }
    public void setGoogleUser(GoogleUser googleUser) {
        this.currentGoogleUser = googleUser; // Ici, nous définissons l'utilisateur Google actuel
    }
}
