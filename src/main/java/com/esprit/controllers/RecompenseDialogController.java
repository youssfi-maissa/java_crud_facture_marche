package com.esprit.controllers;

import com.esprit.entities.Facture;
import com.esprit.entities.Livreur;
import com.esprit.entities.Recompense;
import com.esprit.services.RecompenseService;
import com.esprit.utils.MyDataBase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecompenseDialogController {

    @FXML private ComboBox<String> typeField;
    @FXML private TextField valeurField;
    @FXML private TextArea descriptionField;
    @FXML private TextField seuilField;

    @FXML private ComboBox<Facture> factureCombo;
    @FXML private ComboBox<Livreur> livreurComboBox;

    private Recompense recompense;

    // ================= INIT =================
    @FXML
    public void initialize() {
        loadFacturesFromDB();
        loadLivreursFromDB();
    }

    // ================= FACTURES =================
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
            System.out.println("❌ Erreur factures: " + e.getMessage());
        }
    }

    // ================= LIVREURS =================
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
            System.out.println("❌ Erreur livreurs: " + e.getMessage());
        }
    }

    // ================= SET RECOMPENSE =================
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

        if (r.getIdLivreur() >0) {
            livreurComboBox.getItems().stream()
                    .filter(l -> l.getId() == r.getIdLivreur())
                    .findFirst()
                    .ifPresent(livreurComboBox::setValue);
        }
    }

    // ================= SAVE =================
    @FXML
    public void enregistrer() {

        if (recompense == null) {
            recompense = new Recompense();
        }

        recompense.setType(typeField.getValue());
        recompense.setValeur(Double.parseDouble(valeurField.getText()));
        recompense.setDescription(descriptionField.getText());
        recompense.setSeuil(Integer.parseInt(seuilField.getText()));

        Facture f = factureCombo.getValue();
        recompense.setIdFacture(f != null ? f.getIdFacture() : null);

        Livreur l = livreurComboBox.getValue();
        recompense.setIdLivreur(l != null ? l.getId() : null);

        new RecompenseService().modifier(recompense);

        ((Stage) typeField.getScene().getWindow()).close();
    }
}