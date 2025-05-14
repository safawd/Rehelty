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
import tn.esprit.services.MailService;
import tn.esprit.services.ServicePersonne;
import tn.esprit.utils.MyDataBase;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
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
            connection = MyDataBase.getInstance().getCnx();

            try {
                Image logoImage = new Image(getClass().getResourceAsStream("/images/Logo.png"));
                logoImageView.setImage(logoImage);
            } catch (Exception e) {
                System.err.println("Impossible de charger le logo: " + e.getMessage());
            }

            visiblePasswordField = new TextField();
            visiblePasswordField.setPromptText("Mot de passe");
            visiblePasswordField.setStyle("-fx-background-color: transparent;");
            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);

            HBox passwordContainer = (HBox) pfPassword.getParent();
            passwordContainer.getChildren().add(visiblePasswordField);

            descriptionLabel.setText("Votre partenaire immobilier de confiance. Trouvez, achetez et gérez facilement vos biens immobiliers avec notre plateforme intuitive.");

        } catch (Exception e) {
            showError("Erreur d'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText();

        // Vérifie si les champs sont vides
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // Vérifier si l'utilisateur est l'admin
        if ("admin".equals(email) && "admin1234@".equals(password)) {
            // Rediriger vers la page GestionAdmin.fxml
            navigateToAdmin(event);
            return;
        }

        // Vérifie l'utilisateur dans la base de données
        Personne user = servicePersonne.getByEmailAndPassword(email, password);

        if (user == null) {
            showError("Email ou mot de passe incorrect");
            return;
        }

        // Vérifier si l'utilisateur a un code de vérification
        if (user.getVerificationCode() != null) {
            // Générer un code de vérification
            String verificationCode = servicePersonne.generateVerificationCode();

            // Mettre à jour le code de vérification dans la base de données
            servicePersonne.updateVerificationCode(user.getEmail(), verificationCode);

            // Envoyer le code de vérification par email
            sendVerificationEmail(user.getEmail(), verificationCode);

            // Ouvrir la fenêtre pour saisir le code
            openVerificationWindow(user);
        } else {
            // Si l'utilisateur est déjà vérifié, rediriger vers la page d'accueil
            redirectToHome(event, user);
        }
    }


    private void sendVerificationEmail(String email, String verificationCode) {
        MailService mailService = new MailService();
        mailService.sendVerificationEmail(email, verificationCode);
    }

    private void openVerificationWindow(Personne user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CodeVerificationController.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur à la fenêtre de vérification
            CodeVerificationController controller = loader.getController();
            controller.setUser(user);

            // Ouvrir la fenêtre
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showError("Erreur lors du chargement de la fenêtre de vérification: " + e.getMessage());
        }
    }



    private void redirectToHome(ActionEvent event, Personne user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            // Passer l'objet Personne au contrôleur de Home
            Home homeController = loader.getController();
            homeController.setPersonne(user);

            // Changer de scène vers Home
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page d'accueil: " + e.getMessage());
        }
    }

    @FXML
    private void onGoogleLogin(ActionEvent event) {
        showInfo("Connexion avec Google en cours...");

        googleAuthService.authenticate()
                .thenAccept(googleUser -> {
                    Platform.runLater(() -> {
                        try {
                            String userEmail = googleUser.getEmail();
                            Personne existingUser = servicePersonne.findByEmail(userEmail);

                            if (existingUser != null) {
                                showSuccess("Connexion réussie! Redirection vers l'accueil...");
                                navigateToHome(event);
                            } else {
                                showError("L'email associé à ce compte Google n'existe pas dans notre base de données.");
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
            pfPassword.setText(visiblePasswordField.getText());

            pfPassword.setVisible(true);
            pfPassword.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            btnShowPassword.setText("Afficher");
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

    private void navigateToAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionAdmin.fxml"));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }

    @FXML
    private void onForgotPassword(ActionEvent event) {
        String email = tfEmail.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez entrer votre email.");
            return;
        }

        Personne user = servicePersonne.findByEmail(email);
        if (user == null) {
            showError("Aucun utilisateur trouvé avec cet email.");
            return;
        }

        String verificationCode = servicePersonne.generateVerificationCode();

        servicePersonne.updateVerificationCode(email, verificationCode);

        sendVerificationEmail(email, verificationCode);

        openVerificationWindow(user);
    }
@FXML TextField verificationCodeField;
    @FXML
    private void verifyVerificationCode(ActionEvent event) {
        String enteredCode = verificationCodeField.getText().trim();  // Récupérer le code saisi par l'utilisateur

        if (enteredCode.isEmpty()) {
            showError("Veuillez entrer le code de vérification.");
            return;
        }

        // Récupérer l'email de l'utilisateur à partir du champ
        String email = tfEmail.getText().trim();

        // Vérifier le code dans la base de données
        boolean isCodeValid = servicePersonne.verifyCode(email, enteredCode);

        if (isCodeValid) {
            showSuccess("Code de vérification correct !");
            // Rediriger l'utilisateur vers la page d'accueil
            redirectToHome(event, servicePersonne.findByEmail(email));  // Assurez-vous d'utiliser l'objet Personne
        } else {
            showError("Code incorrect. Essayez à nouveau.");
        }
    }

}
