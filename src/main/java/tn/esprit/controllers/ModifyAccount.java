package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Personne;
import tn.esprit.services.ServicePersonne;

import java.io.File;
import java.io.IOException;

public class ModifyAccount {

    @FXML private ImageView profileImage;
    @FXML private TextField nomField, prenomField, emailField, paysField;

    private Personne personne;
    private final ServicePersonne service = new ServicePersonne();
    private Stage stage;

    public void setPersonne(Personne p, Stage s) {
        this.personne = p;
        this.stage = s;
        if (p != null) {
            nomField.setText(p.getNom());
            prenomField.setText(p.getPrenom());
            emailField.setText(p.getEmail());
            paysField.setText(p.getPays());
            profileImage.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm()));
        }
    }

    @FXML
    private void changeProfilePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            profileImage.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void generateLoyaltyCard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoyaltyCard.fxml"));
        Parent root = loader.load();
        LoyaltyCard controller = loader.getController();
        controller.setDetails(nomField.getText(), prenomField.getText(), paysField.getText());
        Stage st = new Stage();
        st.setScene(new Scene(root));
        st.setTitle("Carte Fidélité");
        st.show();
    }

    @FXML
    private void saveChanges() {
        if (personne != null) {
            personne.setNom(nomField.getText());
            personne.setPrenom(prenomField.getText());
            personne.setEmail(emailField.getText());
            personne.setPays(paysField.getText());
            service.update(personne);
            stage.close();
        }
    }
}
