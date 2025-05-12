package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Personne;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Home {

    @FXML private Label welcomeLabel;
    @FXML private ImageView profileImage;
    private Personne personne;

    // Méthode pour afficher les informations de l'utilisateur authentifié
    public void setUtilisateur(Personne personne) {
        this.personne = personne;
        welcomeLabel.setText("Bienvenue, " + personne.getPrenom() + " !");

        // Si l'utilisateur a une photo de profil, on l'affiche
        if (personne.isGoogleUser() && personne.getGoogleUserId() != null) {
            // Si c'est un utilisateur Google, on peut utiliser l'image Google si disponible
            // Ici on suppose que le GoogleUserId est utilisé pour charger l'image depuis une source externe.
            profileImage.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm()));
        } else {
            // Sinon on utilise une image par défaut pour les utilisateurs manuels
            profileImage.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm()));
        }
    }

    // Méthode pour changer la photo de profil
    @FXML
    private void onChangePhotoClick() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            profileImage.setImage(new Image(file.toURI().toString()));
        }
    }

    // Méthode pour éditer le profil de l'utilisateur
    @FXML
    private void onEditProfileClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifyAccount.fxml"));
            Parent root = loader.load();
            ModifyAccount controller = loader.getController();
            Stage stage = new Stage();
            controller.setPersonne(personne, stage);
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier le Profil");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour supprimer le compte de l'utilisateur
    @FXML
    private void onDeleteAccountClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le compte ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        result.ifPresent(response -> {
            if (response == ButtonType.YES) {
                showInfo("Compte supprimé.");
            }
        });
    }

    // Méthode pour afficher un message d'information
    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
