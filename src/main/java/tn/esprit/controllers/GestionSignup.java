package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Personne;
import tn.esprit.services.GoogleAuthService;
import tn.esprit.services.GoogleAuthService.GoogleUser;
import tn.esprit.services.ServicePersonne;

import java.io.IOException;
import java.util.regex.Pattern;

public class GestionSignup {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private TextField tfTelephone;
    @FXML private PasswordField pfPassword;
    @FXML private PasswordField pfConfirmPassword;
    @FXML private ComboBox<String> cbPays;
    @FXML private ComboBox<String> cbIdType;
    @FXML private TextField tfIdNumber;
    @FXML private CheckBox cbAccept;
    @FXML private Label lblMessage;
    @FXML private Button btnGoogleSignup;
private Personne personne;
    private final ServicePersonne servicePersonne = new ServicePersonne();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();
    private GoogleUser currentGoogleUser = null;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(gmail\\.com|outlook\\.com|yahoo\\.com)$");

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur...");

        // Initialiser les listes déroulantes
        cbPays.getItems().addAll("Tunisie", "France", "Maroc", "Algérie", "Canada");
        cbIdType.getItems().addAll("CIN", "Passeport");

        // Vérifier si le bouton Google existe
        if (btnGoogleSignup == null) {
            System.err.println("ERREUR: Le bouton Google est null!");
        } else {
            System.out.println("Bouton Google trouvé");
        }
    }

    @FXML
    public void handleGoogleSignup() {
        System.out.println("Démarrage de l'authentification Google...");
        showInfo("Connexion avec Google en cours...");

        // Ouvrir la page de saisie du code immédiatement après la demande d'authentification
        Platform.runLater(() -> {
            openCodeAuthorizationPage();  // Afficher la page pour saisir le code d'autorisation
        });

        // Lancer l'authentification Google
        googleAuthService.authenticate()
                .thenAccept(googleUser -> {
                    Platform.runLater(() -> {
                        System.out.println("Authentification Google réussie: " + googleUser);
                        currentGoogleUser = googleUser;

                        try {
                            // Vérifier si l'utilisateur existe déjà
                            Personne existingUser = servicePersonne.findByGoogleId(googleUser.getId());

                            if (existingUser != null) {
                                // L'utilisateur existe déjà, connectez-le directement
                                showSuccess("Connexion réussie! Redirection vers l'accueil...");
                                // TODO: Rediriger vers la page d'accueil
                            } else {
                                // Nouvel utilisateur, remplir les champs du formulaire d'inscription
                                tfNom.setText(googleUser.getFamilyName());
                                tfPrenom.setText(googleUser.getGivenName());
                                tfEmail.setText(googleUser.getEmail());

                                // Générer un mot de passe aléatoire
                                String randomPassword = generateRandomPassword();
                                pfPassword.setText(randomPassword);
                                pfConfirmPassword.setText(randomPassword);

                                // Cocher automatiquement l'acceptation des conditions
                                cbAccept.setSelected(true);

                                showSuccess("Connexion avec Google réussie! Complétez votre profil et cliquez sur 'Sign Up'.");
                            }
                        } catch (Exception e) {
                            showError("Erreur: " + e.getMessage());
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

    private void openCodeAuthorizationPage() {
        try {
            // Créer une nouvelle fenêtre pour saisir le code
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CodeAuthorization.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur Google au contrôleur de la page CodeAuthorization
            CodeAuthorization controller = loader.getController();
            controller.setGoogleUser(currentGoogleUser); // Passer l'utilisateur à CodeAuthorization

            // Créer un stage (fenêtre) et afficher la page de saisie du code
            Stage codeAuthStage = new Stage();
            codeAuthStage.setScene(new Scene(root));
            codeAuthStage.setTitle("Saisir le Code d'Autorisation");
            codeAuthStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture de la page pour saisir le code.");
        }
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
    private void onSignup() {
        clearMessage();

        // Validation des champs
        if (!validateFields()) {
            return;
        }

        // Création de l'utilisateur
        Personne personne = new Personne();
        personne.setNom(tfNom.getText().trim());
        personne.setPrenom(tfPrenom.getText().trim());
        personne.setEmail(tfEmail.getText().trim());

        // Convertir le téléphone en int
        String telephoneStr = tfTelephone.getText().trim();
        if (!telephoneStr.isEmpty()) {
            try {
                // Convertir la chaîne en entier
                int telephone = Integer.parseInt(telephoneStr);
                personne.setTelephone(telephone);  // Assigner l'entier au téléphone
            } catch (NumberFormatException e) {
                showError("Le numéro de téléphone doit être un entier valide.");
                return;  // Si la conversion échoue, arrêter l'exécution
            }
        } else {
            personne.setTelephone(0);  // Si le champ téléphone est vide, mettre 0 ou une valeur par défaut
        }

        personne.setMotDePasse(pfPassword.getText());
        personne.setPays(cbPays.getValue());
        personne.setIdType(cbIdType.getValue());
        personne.setIdNumber(tfIdNumber.getText().trim());

        // Si l'inscription vient de Google
        if (currentGoogleUser != null) {
            personne.setGoogleUser(true);
            personne.setGoogleUserId(currentGoogleUser.getId());
        }

        // Enregistrement
        try {
            servicePersonne.add(personne);

            if (personne.getIdPersonne() > 0) {
                showSuccess("Inscription réussie! Vous pouvez maintenant vous connecter.");
                clearFields();
                currentGoogleUser = null;
            } else {
                showError("Échec de l'inscription. Veuillez réessayer.");
            }
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private boolean validateFields() {
        // Vérification des champs obligatoires
        if (isEmpty(tfNom.getText()) || isEmpty(tfPrenom.getText()) ||
                isEmpty(tfEmail.getText()) || isEmpty(pfPassword.getText())) {
            showError("Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        // Validation de l'email
        if (!EMAIL_PATTERN.matcher(tfEmail.getText().trim()).matches()) {
            showError("Format d'email invalide. Utilisez une adresse Gmail, Outlook ou Yahoo.");
            return false;
        }

        // Vérification de la confirmation du mot de passe
        if (!pfPassword.getText().equals(pfConfirmPassword.getText())) {
            showError("Les mots de passe ne correspondent pas.");
            return false;
        }

        // Vérification de l'acceptation des conditions
        if (!cbAccept.isSelected()) {
            showError("Vous devez accepter les conditions d'utilisation.");
            return false;
        }

        return true;
    }

    private boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    private void showError(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #FFAB91;");
    }

    private void showSuccess(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #A5D6A7;");
    }

    private void showInfo(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #90CAF9;");
    }

    private void clearMessage() {
        lblMessage.setText("");
    }

    private void clearFields() {
        tfNom.clear();
        tfPrenom.clear();
        tfEmail.clear();
        tfTelephone.clear();
        pfPassword.clear();
        pfConfirmPassword.clear();
        cbPays.getSelectionModel().clearSelection();
        cbIdType.getSelectionModel().clearSelection();
        tfIdNumber.clear();
        cbAccept.setSelected(false);
    }

    public void accepterConditions() {
        cbAccept.setSelected(true);
    }

    public void setGoogleUserData(GoogleUser googleUser) {
        if (googleUser == null) {
            return;
        }

        // Stocker l'utilisateur Google courant
        this.currentGoogleUser = googleUser;

        // Pré-remplir les champs
        tfNom.setText(googleUser.getFamilyName());
        tfPrenom.setText(googleUser.getGivenName());
        tfEmail.setText(googleUser.getEmail());

        // Générer un mot de passe aléatoire
        String randomPassword = generateRandomPassword();
        pfPassword.setText(randomPassword);
        pfConfirmPassword.setText(randomPassword);

        // Cocher automatiquement l'acceptation des conditions
        cbAccept.setSelected(true);

        showSuccess("Connexion avec Google réussie! Complétez votre profil et cliquez sur 'Sign Up'.");
    }

    @FXML
    private void onLoginLink(ActionEvent event) {
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

}
