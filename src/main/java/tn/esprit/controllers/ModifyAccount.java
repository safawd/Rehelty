package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Personne;
import tn.esprit.services.ServicePersonne;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ModifyAccount implements Initializable {

    @FXML private ImageView profileImageView;
    @FXML private Label userNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label countryLabel;
    @FXML private Label idTypeLabel;
    @FXML private Label idNumberLabel;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private ComboBox<String> idTypeComboBox;
    @FXML private TextField idNumberField;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Button btnChangePhoto;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;
    @FXML private Button btnClose;

    private Personne personne;
    private Stage stage;
    private ServicePersonne servicePersonne = new ServicePersonne();
    private boolean photoChanged = false;
    private String newPhotoPath = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les listes déroulantes
        List<String> countries = Arrays.asList(
                "France", "Tunisie", "Allemagne", "Espagne", "Italie", "Royaume-Uni",
                "États-Unis", "Canada", "Japon", "Chine", "Australie", "Brésil", "Maroc"
        );
        countryComboBox.setItems(FXCollections.observableArrayList(countries));

        List<String> idTypes = Arrays.asList(
                "Carte d'identité", "Passeport", "Permis de conduire", "Visa", "Carte de séjour"
        );
        idTypeComboBox.setItems(FXCollections.observableArrayList(idTypes));
    }

    public void setPersonne(Personne personne, Stage stage) {
        this.personne = personne;
        this.stage = stage;

        // Remplir les informations de l'utilisateur dans la carte
        userNameLabel.setText(personne.getPrenom() + " " + personne.getNom());
        emailLabel.setText(personne.getEmail());
        phoneLabel.setText(personne.getTelephone() == 0 ? "Non renseigné" : String.valueOf(personne.getTelephone()));
        countryLabel.setText(personne.getPays() != null ? personne.getPays() : "Non renseigné");
        idTypeLabel.setText(personne.getIdType() != null ? personne.getIdType() : "Non renseigné");
        idNumberLabel.setText(personne.getIdNumber() != null ? personne.getIdNumber() : "Non renseigné");

        // Remplir les champs du formulaire
        firstNameField.setText(personne.getPrenom());
        lastNameField.setText(personne.getNom());
        emailField.setText(personne.getEmail());
        phoneField.setText(personne.getTelephone() == 0 ? "" : String.valueOf(personne.getTelephone()));

        if (personne.getPays() != null) {
            countryComboBox.setValue(personne.getPays());
        }

        if (personne.getIdType() != null) {
            idTypeComboBox.setValue(personne.getIdType());
        }

        idNumberField.setText(personne.getIdNumber() != null ? personne.getIdNumber() : "");

        // Charger l'image de profil
        try {
            if (personne.isGoogleUser() && personne.getGoogleUserId() != null) {
                // Pour les utilisateurs Google, on pourrait charger leur photo de profil Google
                profileImageView.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm()));
            } else {
                // Image par défaut pour les autres utilisateurs
                profileImageView.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image de profil: " + e.getMessage());
            profileImageView.setImage(new Image(getClass().getResource("/images/Logo.png").toExternalForm()));
        }
    }

    @FXML
    private void onChangePhotoClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
                photoChanged = true;
                newPhotoPath = selectedFile.getAbsolutePath();
            } catch (Exception e) {
                showError("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onSaveClick(ActionEvent event) {
        // Validation des champs
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showError("Les champs Prénom, Nom et Email sont obligatoires.");
            return;
        }

        // Validation du format de l'email
        String email = emailField.getText();
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Format d'email invalide.");
            return;
        }

        // Validation du numéro de téléphone
        String phone = phoneField.getText();
        int phoneNumber = 0;
        if (!phone.isEmpty()) {
            try {
                phoneNumber = Integer.parseInt(phone);
                if (phone.length() < 8) {
                    showError("Le numéro de téléphone doit contenir au moins 8 chiffres.");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Le numéro de téléphone doit contenir uniquement des chiffres.");
                return;
            }
        }

        // Validation du changement de mot de passe
        if (!currentPasswordField.getText().isEmpty() || !newPasswordField.getText().isEmpty() || !confirmPasswordField.getText().isEmpty()) {
            // Si l'un des champs de mot de passe est rempli, tous doivent être remplis
            if (currentPasswordField.getText().isEmpty() || newPasswordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty()) {
                showError("Tous les champs de mot de passe doivent être remplis pour changer le mot de passe.");
                return;
            }

            // Vérifier que le mot de passe actuel est correct
            if (!currentPasswordField.getText().equals(personne.getMotDePasse())) {
                showError("Le mot de passe actuel est incorrect.");
                return;
            }

            // Vérifier que les nouveaux mots de passe correspondent
            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showError("Les nouveaux mots de passe ne correspondent pas.");
                return;
            }

            // Vérifier la complexité du nouveau mot de passe
            if (newPasswordField.getText().length() < 8) {
                showError("Le nouveau mot de passe doit contenir au moins 8 caractères.");
                return;
            }
        }

        // Mettre à jour les données de l'utilisateur
        personne.setPrenom(firstNameField.getText());
        personne.setNom(lastNameField.getText());
        personne.setEmail(emailField.getText());
        personne.setTelephone(phoneNumber);
        personne.setPays(countryComboBox.getValue());
        personne.setIdType(idTypeComboBox.getValue());
        personne.setIdNumber(idNumberField.getText());

        // Mettre à jour le mot de passe si nécessaire
        if (!newPasswordField.getText().isEmpty()) {
            personne.setMotDePasse(newPasswordField.getText());
        }

        // Enregistrer les modifications dans la base de données
        try {
            servicePersonne.update(personne);
            showSuccess("Vos informations ont été mises à jour avec succès !");

            // Mettre à jour les labels de la carte utilisateur
            userNameLabel.setText(personne.getPrenom() + " " + personne.getNom());
            emailLabel.setText(personne.getEmail());
            phoneLabel.setText(personne.getTelephone() == 0 ? "Non renseigné" : String.valueOf(personne.getTelephone()));
            countryLabel.setText(personne.getPays() != null ? personne.getPays() : "Non renseigné");
            idTypeLabel.setText(personne.getIdType() != null ? personne.getIdType() : "Non renseigné");
            idNumberLabel.setText(personne.getIdNumber() != null ? personne.getIdNumber() : "Non renseigné");

            // Réinitialiser les champs de mot de passe
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

        } catch (Exception e) {
            showError("Erreur lors de la mise à jour des informations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancelClick(ActionEvent event) {
        // Demander confirmation avant d'annuler les modifications
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler les modifications");
        alert.setContentText("Êtes-vous sûr de vouloir annuler les modifications ? Toutes les modifications non enregistrées seront perdues.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Réinitialiser les champs avec les valeurs actuelles de l'utilisateur
            firstNameField.setText(personne.getPrenom());
            lastNameField.setText(personne.getNom());
            emailField.setText(personne.getEmail());
            phoneField.setText(personne.getTelephone() == 0 ? "" : String.valueOf(personne.getTelephone()));

            if (personne.getPays() != null) {
                countryComboBox.setValue(personne.getPays());
            } else {
                countryComboBox.getSelectionModel().clearSelection();
            }

            if (personne.getIdType() != null) {
                idTypeComboBox.setValue(personne.getIdType());
            } else {
                idTypeComboBox.getSelectionModel().clearSelection();
            }

            idNumberField.setText(personne.getIdNumber() != null ? personne.getIdNumber() : "");

            // Réinitialiser les champs de mot de passe
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

            // Réinitialiser l'image de profil si elle a été modifiée
            if (photoChanged) {
                try {
                    if (personne.isGoogleUser() && personne.getGoogleUserId() != null) {
                        profileImageView.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm()));
                    } else {
                        profileImageView.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm()));
                    }
                    photoChanged = false;
                    newPhotoPath = null;
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image de profil: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void onCloseClick(ActionEvent event) {
        // Vérifier s'il y a des modifications non enregistrées
        boolean hasChanges = !firstNameField.getText().equals(personne.getPrenom()) ||
                !lastNameField.getText().equals(personne.getNom()) ||
                !emailField.getText().equals(personne.getEmail()) ||
                !phoneField.getText().equals(personne.getTelephone() == 0 ? "" : String.valueOf(personne.getTelephone())) ||
                (countryComboBox.getValue() != null && !countryComboBox.getValue().equals(personne.getPays())) ||
                (idTypeComboBox.getValue() != null && !idTypeComboBox.getValue().equals(personne.getIdType())) ||
                !idNumberField.getText().equals(personne.getIdNumber() != null ? personne.getIdNumber() : "") ||
                !currentPasswordField.getText().isEmpty() ||
                !newPasswordField.getText().isEmpty() ||
                !confirmPasswordField.getText().isEmpty() ||
                photoChanged;

        if (hasChanges) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Fermer sans enregistrer");
            alert.setContentText("Vous avez des modifications non enregistrées. Êtes-vous sûr de vouloir fermer sans enregistrer ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return;
            }
        }

        // Fermer la fenêtre
        stage.close();
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
}
