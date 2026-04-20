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

    private static final String PATTERN = "^FAC-\\d{4}-\\d{3}$";

    // ✅ Facture en cours de modification (null = mode ajout)
    private Facture factureEnEdition = null;

    // =====================================================
    // INIT
    // =====================================================
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
    // ✅ SETTER POUR MODE MODIFICATION
    // =====================================================
    public void setFactureToEdit(Facture facture) {
        this.factureEnEdition = facture;

        // Remplir les champs avec les donnees de la facture
        numeroField.setText(facture.getNumero());
        numeroField.setEditable(false); // numero non modifiable

        montantHTField.setText(String.valueOf(facture.getMontantHT()));
        tvaField.setText(String.valueOf(facture.getTva()));
        montantTTCField.setText(String.valueOf(facture.getMontantTTC()));
        statutCombo.setValue(facture.getStatut());
        cbLivraison.setValue(facture.getIdLivraison());
    }

    // =====================================================
    // ALERT SYSTEM
    // =====================================================
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // =====================================================
    // GENERER NUMERO
    // =====================================================
    @FXML
    private void genererNumeroFacture() {
        // Desactiver en mode modification
        if (factureEnEdition != null) return;

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

            showAlert(Alert.AlertType.INFORMATION, "Succes", "Numero genere avec succes");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur generation : " + e.getMessage());
        }
    }

    // =====================================================
    // CALCUL TTC
    // =====================================================
    private void calculTTC() {
        try {
            String htText  = montantHTField.getText().replace(',', '.');
            String tvaText = tvaField.getText().replace(',', '.');

            if (htText.isEmpty() || tvaText.isEmpty()) {
                montantTTCField.clear();
                return;
            }

            float ht  = Float.parseFloat(htText);
            float tva = Float.parseFloat(tvaText);
            float ttc = ht + (ht * tva / 100);

            montantTTCField.setText(String.format(Locale.US, "%.2f", ttc));

        } catch (Exception e) {
            montantTTCField.clear();
        }
    }

    // =====================================================
    // AJOUT OU MODIFICATION FACTURE
    // =====================================================
    @FXML
    private void ajouterFacture() {
        clearAllErrors();
        boolean hasError = false;

        String numero      = numeroField.getText().trim();
        String statut      = statutCombo.getValue();
        Integer idLivraison = cbLivraison.getValue();
        String htText      = montantHTField.getText().trim();
        String tvaText     = tvaField.getText().trim();

        // Validation numero seulement en mode ajout
        if (factureEnEdition == null) {
            if (numero.isEmpty() || !numero.matches(PATTERN)) {
                setError(numeroField, "Numero invalide FAC-2026-001");
                hasError = true;
            }
        }

        if (htText.isEmpty()) {
            setError(montantHTField, "Montant HT obligatoire");
            hasError = true;
        }

        if (tvaText.isEmpty()) {
            setError(tvaField, "TVA obligatoire");
            hasError = true;
        }

        if (statut == null) {
            setComboError(statutCombo);
            hasError = true;
        }

        if (idLivraison == null) {
            setComboError(cbLivraison);
            hasError = true;
        }

        if (hasError) {
            showAlert(Alert.AlertType.ERROR, "Formulaire invalide",
                    "Veuillez corriger les champs en rouge.");
            return;
        }

        try {
            float ht  = Float.parseFloat(htText.replace(',', '.'));
            float tva = Float.parseFloat(tvaText.replace(',', '.'));
            float ttc = ht + (ht * tva / 100);

            if (factureEnEdition != null) {
                // ✅ MODE MODIFICATION
                factureEnEdition.setMontantHT(ht);
                factureEnEdition.setTva(tva);
                factureEnEdition.setMontantTTC(ttc);
                factureEnEdition.setStatut(statut);
                factureEnEdition.setIdLivraison(idLivraison);

                factureService.modifier(factureEnEdition);

                showAlert(Alert.AlertType.INFORMATION, "Succes",
                        "Facture modifiee avec succes");

                // Fermer la fenetre apres modification
                numeroField.getScene().getWindow().hide();

            } else {
                // ✅ MODE AJOUT
                Facture f = new Facture(numero, ht, ttc, tva, statut, idLivraison);
                factureService.ajouter(f);

                showAlert(Alert.AlertType.INFORMATION, "Succes",
                        "Facture ajoutee avec succes");

                clearForm();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // =====================================================
    // TABLE VIEW
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

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur ouverture table view");
        }
    }

    // =====================================================
    // HELPERS UI
    // =====================================================
    private void setError(TextField field, String msg) {
        field.getStyleClass().add("text-field-error");
        field.setTooltip(new Tooltip(msg));
    }

    private void setComboError(ComboBox<?> combo) {
        combo.setStyle("-fx-border-color: #ef4444; -fx-background-color: #fef2f2;");
    }

    private void clearAllErrors() {
        numeroField.getStyleClass().removeAll("text-field-error");
        montantHTField.getStyleClass().removeAll("text-field-error");
        tvaField.getStyleClass().removeAll("text-field-error");
        statutCombo.setStyle("");
        cbLivraison.setStyle("");
    }

    private void clearForm() {
        numeroField.clear();
        montantHTField.clear();
        tvaField.clear();
        montantTTCField.clear();
        statutCombo.setValue(null);
        cbLivraison.setValue(null);
    }

    // =====================================================
    // LIVRAISONS
    // =====================================================
    private void chargerLivraisons() {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            Statement st   = cnx.createStatement();
            ResultSet rs   = st.executeQuery("SELECT ID_Livraison FROM livraisons");

            while (rs.next()) {
                cbLivraison.getItems().add(rs.getInt("ID_Livraison"));
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur chargement livraisons");
        }
    }
}