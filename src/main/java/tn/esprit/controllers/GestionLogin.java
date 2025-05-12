package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import tn.esprit.models.Personne;
import tn.esprit.services.GoogleAuthService;
import tn.esprit.services.GoogleAuthService.GoogleUser;
import tn.esprit.services.ServicePersonne;
import tn.esprit.utils.MyDataBase;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;

public class GestionLogin implements Initializable {

    @FXML
    private ImageView logoImageView;

    @FXML
    private Label descriptionLabel;

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private Button btnShowPassword;

    @FXML
    private CheckBox cbRememberMe;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnGoogleLogin;

    @FXML
    private Hyperlink signupLink;

    private boolean passwordVisible = false;
    private TextField visiblePasswordField;

    private final ServicePersonne servicePersonne = new ServicePersonne();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();
    private Connection connection;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialiser la connexion à la base de données
            connection = MyDataBase.getInstance().getCnx();

            // Charger le logo de l'entreprise
            try {
                Image logoImage = new Image(getClass().getResourceAsStream("/images/Logo.png"));
                logoImageView.setImage(logoImage);
            } catch (Exception e) {
                System.err.println("Impossible de charger le logo: " + e.getMessage());
            }

            // Créer un champ de texte pour afficher le mot de passe en clair
            visiblePasswordField = new TextField();
            visiblePasswordField.setPromptText("Mot de passe");
            visiblePasswordField.setStyle("-fx-background-color: transparent;");
            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);

            // Ajouter le champ de texte visible à côté du PasswordField
            HBox passwordContainer = (HBox) pfPassword.getParent();
            passwordContainer.getChildren().add(visiblePasswordField);

            // Définir la description de REHELTY
            descriptionLabel.setText("Votre partenaire immobilier de confiance. Trouvez, achetez et gérez facilement vos biens immobiliers avec notre plateforme intuitive.");

        } catch (Exception e) {
            showError("Erreur d'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void onLogin(ActionEvent event) {
        String email = tfEmail.getText().trim();
        String password = passwordVisible ? visiblePasswordField.getText() : pfPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        try {
            FXMLLoader loader;
            Parent root;

            if (email.equals("admin") && password.equals("admin")) {
                // Connexion admin
                loader = new FXMLLoader(getClass().getResource("/GestionAdmin.fxml"));
                root = loader.load();
                showSuccess("Connexion admin réussie!");
            } else {
                // Vérifie l'utilisateur en base
                Personne user = servicePersonne.getByEmailAndPassword(email, password);

                if (user == null) {
                    showError("Email ou mot de passe incorrect");
                    return;
                }

                if (cbRememberMe.isSelected()) {
                    saveLoginInfo(email);
                }

                // Connexion utilisateur normale
                loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
                root = loader.load();
                showSuccess("Connexion réussie!");
            }

            // Changer la scène
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showError("Erreur lors du chargement de la page: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onGoogleLogin(ActionEvent event) {
        showInfo("Connexion avec Google en cours...");

        googleAuthService.authenticate()
                .thenAccept(googleUser -> {
                    Platform.runLater(() -> {
                        try {
                            // Vérifier si l'utilisateur existe déjà
                            Personne existingUser = servicePersonne.findByGoogleId(googleUser.getId());

                            if (existingUser != null) {
                                // L'utilisateur existe déjà, connectez-le directement
                                showSuccess("Connexion réussie! Redirection vers l'accueil...");
                                navigateToHome(event);
                            } else {
                                // Nouvel utilisateur, rediriger vers la page d'inscription
                                // avec les informations pré-remplies
                                navigateToSignup(event, googleUser);
                            }
                        } catch (Exception e) {
                            showError("Erreur lors de la connexion avec Google: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showError("Erreur d'authentification Google: " + ex.getMessage());
                        ex.printStackTrace();
                    });
                    return null;
                });
    }

    @FXML
    private void onShowPassword(ActionEvent event) {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            // Afficher le mot de passe
            visiblePasswordField.setText(pfPassword.getText());
            visiblePasswordField.setLayoutX(pfPassword.getLayoutX());
            visiblePasswordField.setLayoutY(pfPassword.getLayoutY());
            visiblePasswordField.setPrefWidth(pfPassword.getWidth());
            visiblePasswordField.setPrefHeight(pfPassword.getHeight());

            pfPassword.setVisible(false);
            pfPassword.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);

            btnShowPassword.setText("Masquer");
        } else {
            // Masquer le mot de passe
            pfPassword.setText(visiblePasswordField.getText());

            pfPassword.setVisible(true);
            pfPassword.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            btnShowPassword.setText("Afficher");
        }
    }

    @FXML
    private void onForgotPassword(ActionEvent event) {
        // Implémenter la fonctionnalité de récupération de mot de passe
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionResetPassword.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page de récupération: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onSignupLink(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionSignup.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page d'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToSignup(ActionEvent event, GoogleUser googleUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionSignup.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur d'inscription
            GestionSignup signupController = loader.getController();

            // Appeler la méthode pour définir les données Google
            signupController.setGoogleUserData(googleUser);

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page d'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void saveLoginInfo(String email) {
        // Implémenter la sauvegarde des informations de connexion
        // Par exemple, utiliser les préférences utilisateur
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(GestionLogin.class);
        prefs.put("savedEmail", email);
        prefs.putBoolean("rememberMe", true);
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
