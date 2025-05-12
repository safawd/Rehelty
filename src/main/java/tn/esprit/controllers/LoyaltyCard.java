package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoyaltyCard {

    @FXML private Label nomLabel, prenomLabel, paysLabel;
    @FXML private ImageView barcodeImage, photoProfil;

    public void setDetails(String nom, String prenom, String pays) {
        nomLabel.setText("Nom : " + nom);
        prenomLabel.setText("Prénom : " + prenom);
        paysLabel.setText("Pays : " + pays);
        barcodeImage.setImage(new Image(getClass().getResource("/images/fake-barcode.png").toExternalForm()));
        photoProfil.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm()));
    }
}
