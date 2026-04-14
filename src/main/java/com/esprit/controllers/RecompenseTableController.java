package com.esprit.controllers;

import com.esprit.entities.Recompense;
import com.esprit.services.RecompenseService;
import com.esprit.utils.MyDataBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecompenseTableController {

    @FXML private TableView<Recompense>            recompenseTable;
    @FXML private TableColumn<Recompense, String>  typeColumn;
    @FXML private TableColumn<Recompense, Double>  valeurColumn;
    @FXML private TableColumn<Recompense, String>  descriptionColumn;
    @FXML private TableColumn<Recompense, Integer> seuilColumn;
    @FXML private TableColumn<Recompense, String>  dateColumn;
    @FXML private TableColumn<Recompense, String>  livreurColumn;
    @FXML private TableColumn<Recompense, String>  factureColumn;
    @FXML private Label                            messageLabel;

    private final RecompenseService recompenseService = new RecompenseService();

    @FXML
    public void initialize() {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        valeurColumn.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        seuilColumn.setCellValueFactory(new PropertyValueFactory<>("seuil"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateObtention"));

        livreurColumn.setCellValueFactory(cellData -> {
            int livreurId = cellData.getValue().getIdLivreur();
            String nom = getNomLivreur(livreurId);
            return new javafx.beans.property.SimpleStringProperty(nom);
        });

        factureColumn.setCellValueFactory(cellData -> {
            Integer factureId = cellData.getValue().getIdFacture();
            if (factureId == null)
                return new javafx.beans.property.SimpleStringProperty("-");
            return new javafx.beans.property.SimpleStringProperty(
                    getNumeroFacture(factureId)
            );
        });

        actualiserTableau();
    }

    @FXML
    public void actualiserTableau() {
        List<Recompense> list = recompenseService.afficherTous();
        ObservableList<Recompense> data = FXCollections.observableArrayList(list);
        recompenseTable.setItems(data);
        messageLabel.setText("");
    }

    @FXML
    public void supprimerRecompense() {
        Recompense selected = recompenseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("⚠️ Sélectionnez une récompense à supprimer !");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette récompense ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                recompenseService.supprimer(selected.getIdRecompense());
                actualiserTableau();
                messageLabel.setText("✅ Récompense supprimée !");
            }
        });
    }

    @FXML
    public void modifierRecompense() {
        Recompense selected = recompenseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("⚠️ Sélectionnez une récompense à modifier !");
            return;
        }

        // Fenêtre popup de modification
        Stage popupStage = new Stage();
        popupStage.setTitle("Modifier Récompense");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // Type
        ComboBox<String> typeField = new ComboBox<>();
        typeField.getItems().addAll("bonus", "réduction", "cadeau", "prime", "remise");
        typeField.setValue(selected.getType());

        // Valeur
        TextField valeurField = new TextField(String.valueOf(selected.getValeur()));

        // Description
        TextArea descriptionField = new TextArea(selected.getDescription());
        descriptionField.setPrefHeight(80);

        // Seuil
        TextField seuilField = new TextField(String.valueOf(selected.getSeuil()));

        // Date
        DatePicker datePicker = new DatePicker();
        if (selected.getDateObtention() != null) {
            datePicker.setValue(selected.getDateObtention()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        // Livreur — chargé automatiquement
        ComboBox<String> livreurCombo = new ComboBox<>();
        Map<String, Integer> livreurMap = new HashMap<>();
        chargerLivreurs(livreurCombo, livreurMap);
        String nomLivreur = getNomLivreur(selected.getIdLivreur());
        livreurCombo.setValue(nomLivreur);

        // Facture
        ComboBox<String> factureCombo = new ComboBox<>();
        Map<String, Integer> factureMap = new HashMap<>();
        chargerFactures(factureCombo, factureMap);
        if (selected.getIdFacture() != null) {
            factureCombo.setValue(getNumeroFacture(selected.getIdFacture()));
        }

        // Label message
        Label msg = new Label();

        // Bouton Sauvegarder
        Button saveBtn = new Button("Sauvegarder");
        saveBtn.setOnAction(e -> {
            try {
                selected.setType(typeField.getValue());
                selected.setValeur(Double.parseDouble(valeurField.getText().trim()));
                selected.setDescription(descriptionField.getText().trim());
                selected.setSeuil(Integer.parseInt(seuilField.getText().trim()));

                LocalDate date = datePicker.getValue();
                if (date != null) {
                    selected.setDateObtention(Date.from(
                            date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                    ));
                }

                String livreurNom = livreurCombo.getValue();
                if (livreurNom != null) {
                    selected.setIdLivreur(livreurMap.get(livreurNom));
                }

                String factureNum = factureCombo.getValue();
                selected.setIdFacture(
                        factureNum != null ? factureMap.get(factureNum) : null
                );

                recompenseService.modifier(selected);
                actualiserTableau();
                messageLabel.setText("✅ Récompense modifiée !");
                popupStage.close();

            } catch (NumberFormatException ex) {
                msg.setText("⚠️ Valeur et seuil doivent être numériques !");
                msg.setStyle("-fx-text-fill: red;");
            } catch (Exception ex) {
                msg.setText("❌ Erreur : " + ex.getMessage());
                msg.setStyle("-fx-text-fill: red;");
            }
        });

        // Bouton Annuler
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setOnAction(e -> popupStage.close());

        // Ajouter composants au grid
        grid.add(new Label("Type :"),        0, 0); grid.add(typeField,        1, 0);
        grid.add(new Label("Valeur :"),      0, 1); grid.add(valeurField,      1, 1);
        grid.add(new Label("Description :"), 0, 2); grid.add(descriptionField, 1, 2);
        grid.add(new Label("Seuil :"),       0, 3); grid.add(seuilField,       1, 3);
        grid.add(new Label("Date :"),        0, 4); grid.add(datePicker,       1, 4);
        grid.add(new Label("Livreur :"),     0, 5); grid.add(livreurCombo,     1, 5);
        grid.add(new Label("Facture :"),     0, 6); grid.add(factureCombo,     1, 6);
        grid.add(saveBtn,                    0, 7); grid.add(cancelBtn,        1, 7);
        grid.add(msg,                        0, 8, 2, 1);

        Scene scene = new Scene(grid, 450, 450);
        popupStage.setScene(scene);
        popupStage.show();
    }

    // ===== Méthodes utilitaires =====

    private void chargerLivreurs(ComboBox<String> combo, Map<String, Integer> map) {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT id_utilisateur, nom, prenom FROM utilisateurs WHERE role = 'Livreur'"
            );
            while (rs.next()) {
                String nomComplet = rs.getString("nom") + " " + rs.getString("prenom");
                int id = rs.getInt("id_utilisateur");
                combo.getItems().add(nomComplet);
                map.put(nomComplet, id);
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur livreurs : " + e.getMessage());
        }
    }

    private void chargerFactures(ComboBox<String> combo, Map<String, Integer> map) {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT ID_Facture, numero FROM factures");
            while (rs.next()) {
                String numero = rs.getString("numero");
                int id = rs.getInt("ID_Facture");
                combo.getItems().add(numero);
                map.put(numero, id);
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur factures : " + e.getMessage());
        }
    }

    private String getNomLivreur(int livreurId) {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT nom, prenom FROM utilisateurs WHERE id_utilisateur = ?"
            );
            ps.setInt(1, livreurId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("nom") + " " + rs.getString("prenom");
        } catch (Exception e) {
            System.out.println("❌ Erreur getNomLivreur : " + e.getMessage());
        }
        return "Inconnu";
    }

    private String getNumeroFacture(int factureId) {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT numero FROM factures WHERE ID_Facture = ?"
            );
            ps.setInt(1, factureId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("numero");
        } catch (Exception e) {
            System.out.println("❌ Erreur getNumeroFacture : " + e.getMessage());
        }
        return "-";
    }
}