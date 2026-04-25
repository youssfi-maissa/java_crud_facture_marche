package com.esprit.controllers;

import com.esprit.entities.Facture;
import com.esprit.entities.Livreur;
import com.esprit.entities.Recompense;
import com.esprit.services.AiService;
import com.esprit.services.RecompenseService;
import com.esprit.utils.MyDataBase;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecompenseDialogController {

    @FXML private ComboBox<String>  typeField;
    @FXML private TextField         valeurField;
    @FXML private TextArea          descriptionField;
    @FXML private TextField         seuilField;
    @FXML private ComboBox<Facture> factureCombo;
    @FXML private ComboBox<Livreur> livreurComboBox;

    // ── Nouveaux éléments IA ──────────────────────────────────────
    @FXML private Button btnGenererIA;
    @FXML private Label  labelIA;
    private final AiService aiService = new AiService();

    private Recompense recompense;

    // =====================================================
    // INIT
    // =====================================================
    @FXML
    public void initialize() {
        typeField.getItems().addAll(
                "Bon de réduction",
                "Livraison gratuite",
                "Cadeau",
                "Points bonus",
                "Remise fidélité"
        );
        loadFacturesFromDB();
        loadLivreursFromDB();
    }

    // =====================================================
    // FACTURES
    // =====================================================
    private void loadFacturesFromDB() {
        List<Facture> factures = new ArrayList<>();
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement("SELECT * FROM factures");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Facture f = new Facture(
                        rs.getInt("ID_Facture"),
                        rs.getString("numero"),
                        rs.getTimestamp("dateEmission"),
                        rs.getFloat("montantHT"),
                        rs.getFloat("montantTTC"),
                        rs.getFloat("tva"),
                        rs.getString("statut"),
                        rs.getInt("livraison_id")
                );
                factures.add(f);
            }
            factureCombo.getItems().setAll(factures);

        } catch (Exception e) {
            System.out.println("Erreur factures: " + e.getMessage());
        }
    }

    // =====================================================
    // LIVREURS
    // =====================================================
    private void loadLivreursFromDB() {
        List<Livreur> livreurs = new ArrayList<>();
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement("SELECT * FROM livreur");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Livreur l = new Livreur(
                        rs.getInt("id"),
                        rs.getString("nom")
                );
                livreurs.add(l);
            }
            livreurComboBox.getItems().setAll(livreurs);

        } catch (Exception e) {
            System.out.println("Erreur livreurs: " + e.getMessage());
        }
    }

    // =====================================================
    // SET RECOMPENSE (mode modification)
    // =====================================================
    public void setRecompense(Recompense r) {
        this.recompense = r;

        typeField.setValue(r.getType());
        valeurField.setText(String.valueOf(r.getValeur()));
        descriptionField.setText(r.getDescription());
        seuilField.setText(String.valueOf(r.getSeuil()));

        if (r.getIdFacture() != null) {
            factureCombo.getItems().stream()
                    .filter(f -> f.getIdFacture() == r.getIdFacture())
                    .findFirst()
                    .ifPresent(factureCombo::setValue);
        }

        if (r.getIdLivreur() >0  && r.getIdLivreur() > 0) {
            livreurComboBox.getItems().stream()
                    .filter(l -> l.getId() == r.getIdLivreur())
                    .findFirst()
                    .ifPresent(livreurComboBox::setValue);
        }
    }

    // =====================================================
    // GÉNÉRER DESCRIPTION AVEC GEMINI AI
    // =====================================================
    @FXML
    private void genererDescriptionIA() {
        String nom    = typeField.getValue() != null
                ? typeField.getValue().trim() : "";
        String points = seuilField.getText().trim();

        // Validation
        if (nom.isEmpty()) {
            setLabelIA("⚠ Choisissez le type d'abord.", "#ef4444");
            return;
        }
        if (points.isEmpty()) {
            setLabelIA("⚠ Remplissez le seuil d'abord.", "#ef4444");
            return;
        }

        int pointsInt;
        try {
            pointsInt = Integer.parseInt(points);
        } catch (NumberFormatException e) {
            setLabelIA("⚠ Seuil invalide.", "#ef4444");
            return;
        }

        // Désactiver bouton + afficher chargement
        btnGenererIA.setDisable(true);
        btnGenererIA.setText("⏳");
        setLabelIA("Gemini génère la description...", "#667eea");

        final int pointsFinal = pointsInt;

        // Thread séparé pour ne pas bloquer l'UI JavaFX
        new Thread(() -> {
            String description = aiService.genererDescriptionRecompense(nom, pointsFinal);

            Platform.runLater(() -> {
                descriptionField.setText(description);
                btnGenererIA.setDisable(false);
                btnGenererIA.setText("✨ IA");
                setLabelIA("✅ Description générée par Gemini !", "#16a34a");
            });
        }).start();
    }

    // =====================================================
    // HELPER LABEL IA
    // =====================================================
    private void setLabelIA(String message, String color) {
        labelIA.setText(message);
        labelIA.setStyle("-fx-text-fill: " + color + "; -fx-font-style: italic; -fx-font-size: 11px;");
    }

    // =====================================================
    // SAVE
    // =====================================================
    @FXML
    public void enregistrer() {
        // Validation basique
        if (typeField.getValue() == null || typeField.getValue().isEmpty()) {
            showAlert("Type obligatoire !");
            return;
        }
        if (valeurField.getText().trim().isEmpty()) {
            showAlert("Valeur obligatoire !");
            return;
        }
        if (seuilField.getText().trim().isEmpty()) {
            showAlert("Seuil obligatoire !");
            return;
        }

        try {
            if (recompense == null) {
                recompense = new Recompense();
            }

            recompense.setType(typeField.getValue());
            recompense.setValeur(Double.parseDouble(valeurField.getText().trim()));
            recompense.setDescription(descriptionField.getText().trim());
            recompense.setSeuil(Integer.parseInt(seuilField.getText().trim()));

            Facture f = factureCombo.getValue();
            recompense.setIdFacture(f != null ? f.getIdFacture() : null);

            Livreur l = livreurComboBox.getValue();
            recompense.setIdLivreur(l != null ? l.getId() : null);

            new RecompenseService().modifier(recompense);

            ((Stage) typeField.getScene().getWindow()).close();

        } catch (NumberFormatException e) {
            showAlert("Erreur : vérifiez les champs numériques !");
        }
    }

    // =====================================================
    // HELPER ALERT
    // =====================================================
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}