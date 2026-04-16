package com.esprit.controllers;

import com.esprit.entities.Facture;
import com.esprit.entities.Recompense;
import com.esprit.services.RecompenseService;
import com.esprit.utils.MyDataBase;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RecompenseDialogController {

    @FXML private TextField typeField;
    @FXML private TextField valeurField;
    @FXML private TextField descriptionField;
    @FXML private TextField seuilField;
    @FXML private TextField livreurField;

    // ✅ UTILISE TA CLASSE FACTURE
    @FXML private ComboBox<Facture> factureCombo;

    private Recompense recompense;

    // ================= INIT =================
    @FXML
    public void initialize() {
        loadFacturesFromDB();
    }

    // ================= LOAD FACTURES =================
    private void loadFacturesFromDB() {

        List<Facture> factures = new ArrayList<>();

        try {
            Connection cnx = MyDataBase.getInstance().getConnection();

            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT * FROM factures"
            );

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

            // ⭐ AFFICHAGE PROPRE (numero seulement)
            factureCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Facture item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNumero());
                }
            });

            factureCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Facture item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNumero());
                }
            });

        } catch (Exception e) {
            System.out.println("❌ Erreur load factures: " + e.getMessage());
        }
    }

    // ================= SET DATA =================
    public void setRecompense(Recompense r) {

        this.recompense = r;

        typeField.setText(r.getType());
        valeurField.setText(String.valueOf(r.getValeur()));
        descriptionField.setText(r.getDescription());
        seuilField.setText(String.valueOf(r.getSeuil()));
        livreurField.setText(String.valueOf(r.getIdLivreur()));

        // ✅ FIX IMPORTANT : sélection facture correcte
        if (r.getIdFacture() != null) {

            factureCombo.getItems().stream()
                    .filter(f -> f.getIdFacture() == r.getIdFacture())
                    .findFirst()
                    .ifPresent(factureCombo::setValue);
        }
    }

    // ================= SAVE =================
    @FXML
    public void enregistrer() {

        recompense.setType(typeField.getText());
        recompense.setValeur(Double.parseDouble(valeurField.getText()));
        recompense.setDescription(descriptionField.getText());
        recompense.setSeuil(Integer.parseInt(seuilField.getText()));
        recompense.setIdLivreur(Integer.parseInt(livreurField.getText()));

        // ✅ FIX FINAL
        Facture selected = factureCombo.getValue();

        if (selected != null) {
            recompense.setIdFacture(selected.getIdFacture());
        } else {
            recompense.setIdFacture(null);
        }

        new RecompenseService().modifier(recompense);

        ((Stage) typeField.getScene().getWindow()).close();
    }
}