package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Reglements{

    @FXML private WebView wvPdf;
    private GestionSignup parentController;

    public void setParentController(GestionSignup parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        WebEngine eng = wvPdf.getEngine();
        // Chargement du PDF avec pdf.js
        try {
            String viewer = getClass()
                    .getResource("/pdfjs/web/viewer.html").toExternalForm();
            String pdf = getClass()
                    .getResource("/pdf/reglement.pdf").toExternalForm();
            eng.load(viewer + "?file=" + pdf);
        } catch (Exception e) {
            // Fallback en cas d'erreur - afficher un message HTML simple
            eng.loadContent(
                    "<html><body style='font-family: Arial; padding: 20px;'>" +
                            "<h1>Conditions d'utilisation</h1>" +
                            "<p>Le document PDF n'a pas pu être chargé. Voici un résumé des conditions:</p>" +
                            "<ul>" +
                            "<li>Vous acceptez de fournir des informations exactes et complètes.</li>" +
                            "<li>Vous êtes responsable de maintenir la confidentialité de votre compte.</li>" +
                            "<li>Vous acceptez de ne pas utiliser ce service à des fins illégales.</li>" +
                            "<li>Nous nous réservons le droit de modifier ces conditions à tout moment.</li>" +
                            "</ul>" +
                            "</body></html>",
                    "text/html"
            );
        }
    }

    @FXML
    private void onAccepter() {
        if (parentController != null) {
            parentController.accepterConditions();
        }
        fermerFenetre();
    }

    @FXML
    private void onFermer() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) wvPdf.getScene().getWindow();
        stage.close();
    }
}
