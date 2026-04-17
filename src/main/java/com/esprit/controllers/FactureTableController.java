package com.esprit.controllers;

import com.esprit.entities.Facture;
import com.esprit.services.FactureService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.io.FileOutputStream;
import java.util.Comparator;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class FactureTableController {

    private final FactureService factureService = new FactureService();

    @FXML private TableView<Facture> factureTable;

    @FXML private TableColumn<Facture, String> numeroColumn;
    @FXML private TableColumn<Facture, Float> montantHTColumn;
    @FXML private TableColumn<Facture, Float> montantTTCColumn;
    @FXML private TableColumn<Facture, Float> tvaColumn;
    @FXML private TableColumn<Facture, String> statutColumn;
    @FXML private TableColumn<Facture, Integer> livraisonColumn;
    @FXML private TableColumn<Facture, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> triCombo;
    @FXML private Label messageLabel;

    private FilteredList<Facture> filteredData;

    // ================= INIT =================
    @FXML
    public void initialize() {

        initColumns();
        initCombo();
        addActions();
        loadFactures();
    }

    private void initColumns() {

        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
        montantHTColumn.setCellValueFactory(new PropertyValueFactory<>("montantHT"));
        montantTTCColumn.setCellValueFactory(new PropertyValueFactory<>("montantTTC"));
        tvaColumn.setCellValueFactory(new PropertyValueFactory<>("tva"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        livraisonColumn.setCellValueFactory(new PropertyValueFactory<>("idLivraison"));
    }

    private void initCombo() {

        triCombo.setItems(FXCollections.observableArrayList(
                "Numéro A-Z",
                "Numéro Z-A",
                "HT croissant",
                "HT décroissant",
                "TTC croissant",
                "TTC décroissant",
                "Statut",
                "Livraison"
        ));
    }

    // ================= LOAD =================
    private void loadFactures() {

        filteredData = new FilteredList<>(
                FXCollections.observableArrayList(factureService.afficherTous()),
                p -> true
        );

        factureTable.setItems(filteredData);
    }

    // ================= SEARCH =================
    @FXML
    private void filterFactures() {

        String keyword = searchField.getText();

        filteredData.setPredicate(f -> {

            if (keyword == null || keyword.isEmpty()) return true;

            String lower = keyword.toLowerCase();

            return (f.getNumero() != null && f.getNumero().toLowerCase().contains(lower))
                    || (f.getStatut() != null && f.getStatut().toLowerCase().contains(lower))
                    || String.valueOf(f.getMontantHT()).contains(lower)
                    || String.valueOf(f.getMontantTTC()).contains(lower)
                    || String.valueOf(f.getIdLivraison()).contains(lower);
        });
    }

    // ================= TRI FIX FINAL =================
    @FXML
    private void trierFactures() {

        String choix = triCombo.getValue();
        if (choix == null) return;

        switch (choix) {

            case "Numéro A-Z" ->
                    filteredData.getSource().sort(
                            Comparator.comparing(Facture::getNumero,
                                    Comparator.nullsLast(String::compareToIgnoreCase))
                    );

            case "Numéro Z-A" ->
                    filteredData.getSource().sort(
                            Comparator.comparing(Facture::getNumero,
                                    Comparator.nullsLast(String::compareToIgnoreCase)).reversed()
                    );

            case "HT croissant" ->
                    filteredData.getSource().sort(
                            Comparator.comparingDouble(Facture::getMontantHT)
                    );

            case "HT décroissant" ->
                    filteredData.getSource().sort(
                            Comparator.comparingDouble(Facture::getMontantHT).reversed()
                    );

            case "TTC croissant" ->
                    filteredData.getSource().sort(
                            Comparator.comparingDouble(Facture::getMontantTTC)
                    );

            case "TTC décroissant" ->
                    filteredData.getSource().sort(
                            Comparator.comparingDouble(Facture::getMontantTTC).reversed()
                    );

            case "Statut" ->
                    filteredData.getSource().sort(
                            Comparator.comparing(Facture::getStatut,
                                    Comparator.nullsLast(String::compareToIgnoreCase))
                    );

            case "Livraison" ->
                    filteredData.getSource().sort(
                            Comparator.comparingInt(Facture::getIdLivraison)
                    );
        }

        factureTable.refresh();
    }

    // ================= RESET =================
    @FXML
    private void resetAll() {
        searchField.clear();
        triCombo.setValue(null);
        loadFactures();
    }

    // ================= ACTIONS =================
    private void addActions() {

        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button btnDelete = new Button("Supprimer");

            private final HBox box = new HBox(10, btnDelete);

            {
                box.setAlignment(Pos.CENTER);

                btnDelete.setOnAction(e -> {
                    Facture f = getTableRow().getItem();
                    if (f == null) return;

                    factureService.supprimer(f.getIdFacture());
                    loadFactures();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }
}