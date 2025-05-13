package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.Personne;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Home implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label userPhoneLabel;
    @FXML private Label userCountryLabel;
    @FXML private ImageView userProfileImage;
    @FXML private Button btnEditProfile;

    private Personne personne; // L'utilisateur connecté

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation de l'interface
    }

    // Méthode pour initialiser les données de l'utilisateur
    public void setPersonne(Personne personne) {
        this.personne = personne;

        // Mettre à jour les éléments de l'interface avec les informations de l'utilisateur
        welcomeLabel.setText("Bienvenue, " + personne.getPrenom() + " !");
        userNameLabel.setText(personne.getPrenom() + " " + personne.getNom());
        userEmailLabel.setText(personne.getEmail());
        userPhoneLabel.setText(personne.getTelephone() == 0 ? "Non renseigné" : String.valueOf(personne.getTelephone()));
        userCountryLabel.setText(personne.getPays() != null ? personne.getPays() : "Non renseigné");

        // Charger l'image de profil
        try {
            if (personne.isGoogleUser() && personne.getGoogleUserId() != null) {
                // Pour les utilisateurs Google, on pourrait charger leur photo de profil Google
                userProfileImage.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm()));
            } else {
                // Image par défaut pour les autres utilisateurs
                userProfileImage.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image de profil: " + e.getMessage());
            userProfileImage.setImage(new Image(getClass().getResource("/images/Logo.png").toExternalForm()));
        }
    }

    @FXML
    private void onEditProfileClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifyAccount.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et lui passer l'utilisateur
            ModifyAccount controller = loader.getController();

            // Créer une nouvelle scène dans une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Modifier mon profil");
            stage.setScene(new Scene(root));

            // Configurer la fenêtre
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloque l'interaction avec la fenêtre parent

            // Passer l'utilisateur et la référence de la fenêtre au contrôleur
            controller.setPersonne(personne, stage);

            // Afficher la fenêtre
            stage.showAndWait();

            // Mettre à jour l'affichage après la fermeture de la fenêtre (au cas où l'utilisateur a modifié ses informations)
            setPersonne(personne); // Rafraîchir l'affichage avec les données mises à jour

        } catch (IOException e) {
            showError("Erreur lors du chargement de la page de modification du profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);}}
