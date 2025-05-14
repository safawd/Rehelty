package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import tn.esprit.services.ServicePersonne;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Home implements Initializable {

    @FXML
    private Label welcomeLabel;  // Label pour afficher le message de bienvenue
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userEmailLabel;
    @FXML
    private Label userPhoneLabel;
    @FXML
    private Label userCountryLabel;
    @FXML
    private ImageView userProfileImage;
    @FXML
    private Button btnEditProfile;

    private Personne personne; // L'utilisateur connecté

    private final ServicePersonne servicePersonne = new ServicePersonne(); // Service pour accéder à la base de données

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation de l'interface
        try {
            // Supposons que l'email de l'utilisateur connecté soit dans la session ou ailleurs
            String currentUserEmail = "email_connecte@example.com"; // Remplacer par la logique de récupération de l'email connecté

            // Récupérer l'utilisateur connecté
            Personne currentUser = servicePersonne.getCurrentUser(currentUserEmail);
            if (currentUser != null) {
                setPersonne(currentUser); // Mettre à jour les informations utilisateur
            } else {
                showError("Utilisateur non trouvé.");
            }
        } catch (Exception e) {
            showError("Erreur lors de la récupération des informations de l'utilisateur.");
        }


    }
    @FXML private Label nomutilisateurpagehome;
    @FXML private Label nomUtilisateurPageHome;
    // Méthode pour initialiser les données de l'utilisateur
    public void setPersonneModify(Personne personne) {
        this.personne = personne;

        // Récupérer l'utilisateur de la base de données en utilisant l'email
        ServicePersonne servicePersonne = new ServicePersonne();
        Personne userFromDb = servicePersonne.findByEmail(personne.getEmail());

        // Vérifier que l'utilisateur existe
        if (userFromDb != null) {
            // Mettre à jour les éléments de l'interface avec les informations de l'utilisateur
            welcomeLabel.setText("Bienvenue, " + userFromDb.getPrenom() + " !"); // Affiche le prénom dans le label
            userNameLabel.setText(userFromDb.getPrenom() + " " + userFromDb.getNom());
            userEmailLabel.setText(userFromDb.getEmail());
            userPhoneLabel.setText(userFromDb.getTelephone() == 0 ? "Non renseigné" : String.valueOf(userFromDb.getTelephone()));
            userCountryLabel.setText(userFromDb.getPays() != null ? userFromDb.getPays() : "Non renseigné");

            // Charger l'image de profil
            try {
                if (userFromDb.isGoogleUser() && userFromDb.getGoogleUserId() != null) {
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

            // Mettre à jour le label avec le nom de l'utilisateur
            nomUtilisateurPageHome.setText("Bienvenue, " + userFromDb.getPrenom() + " " + userFromDb.getNom());
        } else {
            // Gérer le cas où l'utilisateur n'est pas trouvé
            System.out.println("Utilisateur non trouvé dans la base de données.");
        }
    }

    //etheya ki l utilisateur yaml login fl home page esmou yothor
    public void setPersonne(Personne personne) {
        this.personne = personne;

        // Mise à jour du label avec le nom de l'utilisateur
        if (personne != null) {
            nomutilisateurpagehome.setText("Bienvenue, " + personne.getPrenom() + " " + personne.getNom());
        }
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void onEditProfileClick() {
        // Ton code pour modifier le profil, ouvrir la page de modification, etc.
        System.out.println("Modifier le profil");
    }
    @FXML
    private void Hometologin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionLogin.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onEditProfileClick(ActionEvent event) {
        try {
            // Charger la page ModifyAccount
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifyAccount.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de ModifyAccount
            ModifyAccount modifyAccountController = loader.getController();

            // Passer l'objet personne (l'utilisateur actuellement connecté)
            modifyAccountController.setPersonne(personne, (Stage) ((Node) event.getSource()).getScene().getWindow());

            // Changer de scène pour la page de modification
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showError("Erreur lors de la redirection vers la page de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }


}
