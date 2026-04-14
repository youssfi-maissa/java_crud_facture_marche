package com.esprit.controllers;

import com.esprit.entities.Facture;
import com.esprit.services.FactureService;
import com.esprit.utils.MyDataBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.Year;
import java.util.Locale;

public class FactureController {

    private final FactureService factureService = new FactureService();

    @FXML private TextField numeroField, montantHTField, montantTTCField, tvaField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private ComboBox<Integer> cbLivraison;
    @FXML private Label messageLabel;

    private static final String PATTERN = "^FAC-\\d{4}-\\d{3}$";

    @FXML
    public void initialize() {

        statutCombo.getItems().addAll("payee", "en attente", "annulee");

        chargerLivraisons();

        montantTTCField.setEditable(false);
        montantTTCField.setMouseTransparent(true);
        montantTTCField.setFocusTraversable(false);

        montantHTField.textProperty().addListener((o, a, b) -> calculTTC());
        tvaField.textProperty().addListener((o, a, b) -> calculTTC());
    }

    // =====================================================
    // GENERATE NUMBER
    // =====================================================
    @FXML
    private void genererNumeroFacture() {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();

            String sql = "SELECT numero FROM factures WHERE numero LIKE ? ORDER BY id_facture DESC LIMIT 1";
            PreparedStatement ps = cnx.prepareStatement(sql);

            String year = String.valueOf(Year.now().getValue());
            ps.setString(1, "FAC-" + year + "%");

            ResultSet rs = ps.executeQuery();

            int next = 1;

            if (rs.next()) {
                String last = rs.getString("numero");
                String[] parts = last.split("-");
                next = Integer.parseInt(parts[2]) + 1;
            }

            String newNumero = String.format("FAC-%s-%03d", year, next);

            numeroField.setText(newNumero);
            messageLabel.setText("Numéro généré ✔");

        } catch (Exception e) {
            messageLabel.setText("Erreur génération : " + e.getMessage());
        }
    }

    // =====================================================
    private void calculTTC() {
        try {
            String htText = montantHTField.getText().replace(',', '.');
            String tvaText = tvaField.getText().replace(',', '.');

            if (htText.isEmpty() || tvaText.isEmpty()) {
                montantTTCField.clear();
                return;
            }

            float ht = Float.parseFloat(htText);
            float tva = Float.parseFloat(tvaText);

            float ttc = ht + (ht * tva / 100);

            montantTTCField.setText(String.format(Locale.US, "%.2f", ttc));

        } catch (Exception e) {
            montantTTCField.clear();
        }
    }

    // =====================================================
    @FXML
    private void ajouterFacture() {
        try {
            String numero = numeroField.getText();
            String statut = statutCombo.getValue();
            Integer idLivraison = cbLivraison.getValue();

            if (numero.isEmpty() || statut == null || idLivraison == null) {
                messageLabel.setText("Veuillez remplir tous les champs !");
                return;
            }

            if (!numero.matches(PATTERN)) {
                messageLabel.setText("Format invalide FAC-2026-001");
                return;
            }

            float ht = Float.parseFloat(montantHTField.getText().replace(',', '.'));
            float tva = Float.parseFloat(tvaField.getText().replace(',', '.'));

            if (ht <= 0 || tva <= 0) {
                messageLabel.setText("Montants doivent être > 0");
                return;
            }

            float ttc = ht + (ht * tva / 100);

            Facture f = new Facture(numero, ht, ttc, tva, statut, idLivraison);

            factureService.ajouter(f);

            messageLabel.setText("Facture ajoutée ✔");

            numeroField.clear();
            montantHTField.clear();
            tvaField.clear();
            montantTTCField.clear();

        } catch (Exception e) {
            messageLabel.setText("Erreur : " + e.getMessage());
        }
    }

    // =====================================================
    private void chargerLivraisons() {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT ID_Livraison FROM livraisons");

            while (rs.next()) {
                cbLivraison.getItems().add(rs.getInt("ID_Livraison"));
            }

        } catch (SQLException e) {
            messageLabel.setText("Erreur livraisons : " + e.getMessage());
        }
    }

    // =====================================================
    @FXML
    private void ouvrirTableView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/esprit/facture-table-view.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Liste des Factures");
            stage.show();

            // ❌ AUCUN message ici

        } catch (IOException e) {
            e.printStackTrace();

            // seulement en cas d'erreur
            messageLabel.setText("❌ Erreur ouverture table view");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}