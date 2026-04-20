package com.esprit.controllers;

import com.esprit.entities.Facture;
import com.esprit.services.FactureService;
import com.esprit.services.PdfService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FactureTableController {

    private final FactureService factureService = new FactureService();
    private final PdfService pdfService = new PdfService();

    @FXML private TableView<Facture> factureTable;

    @FXML private TableColumn<Facture, String>  numeroColumn;
    @FXML private TableColumn<Facture, Float>   montantHTColumn;
    @FXML private TableColumn<Facture, Float>   montantTTCColumn;
    @FXML private TableColumn<Facture, Float>   tvaColumn;
    @FXML private TableColumn<Facture, String>  statutColumn;
    @FXML private TableColumn<Facture, Integer> livraisonColumn;
    @FXML private TableColumn<Facture, Void>    actionsColumn;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> triCombo;
    @FXML private Label            messageLabel;

    private ObservableList<Facture> sourceList;
    private FilteredList<Facture>   filteredData;

    // ================= INIT =================
    @FXML
    public void initialize() {
        initColumns();
        initCombo();
        loadFactures();
        addActions();
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
        sourceList  = FXCollections.observableArrayList(
                new ArrayList<>(factureService.afficherTous())
        );
        filteredData = new FilteredList<>(sourceList, p -> true);
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

    // ================= TRI =================
    @FXML
    private void trierFactures() {
        String choix = triCombo.getValue();
        if (choix == null) return;

        switch (choix) {
            case "Numéro A-Z" ->
                    sourceList.sort(Comparator.comparing(
                            Facture::getNumero,
                            Comparator.nullsLast(String::compareToIgnoreCase)
                    ));
            case "Numéro Z-A" ->
                    sourceList.sort(Comparator.comparing(
                            Facture::getNumero,
                            Comparator.nullsLast(String::compareToIgnoreCase)
                    ).reversed());
            case "HT croissant" ->
                    sourceList.sort(Comparator.comparingDouble(Facture::getMontantHT));
            case "HT décroissant" ->
                    sourceList.sort(Comparator.comparingDouble(Facture::getMontantHT).reversed());
            case "TTC croissant" ->
                    sourceList.sort(Comparator.comparingDouble(Facture::getMontantTTC));
            case "TTC décroissant" ->
                    sourceList.sort(Comparator.comparingDouble(Facture::getMontantTTC).reversed());
            case "Statut" ->
                    sourceList.sort(Comparator.comparing(
                            Facture::getStatut,
                            Comparator.nullsLast(String::compareToIgnoreCase)
                    ));
            case "Livraison" ->
                    sourceList.sort(Comparator.comparingInt(Facture::getIdLivraison));
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

    // ================= ACTIONS (MODIFIER + DELETE + PDF) =================
    private void addActions() {

        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button btnModifier = new Button("Modifier");
            private final Button btnDelete   = new Button("Supprimer");
            private final Button btnPDF      = new Button("PDF");
            private final HBox   box         = new HBox(8, btnModifier, btnDelete, btnPDF);

            {
                box.setAlignment(Pos.CENTER);

                // ================= MODIFIER =================
                btnModifier.setOnAction(e -> {
                    Facture f = getTableRow().getItem();
                    if (f == null) return;

                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/com/esprit/facture-view.fxml")
                        );
                        Stage stage = new Stage();
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setTitle("Modifier Facture");
                        stage.setScene(new Scene(loader.load()));

                        // Passer la facture au controller du formulaire
                        FactureController controller = loader.getController();
                        controller.setFactureToEdit(f);

                        stage.showAndWait();

                        // Rafraichir la table apres modification
                        loadFactures();
                        messageLabel.setText("Facture modifiee avec succes");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        messageLabel.setText("Erreur ouverture formulaire modification");
                    }
                });

                // ================= DELETE =================
                btnDelete.setOnAction(e -> {
                    Facture f = getTableRow().getItem();
                    if (f == null) return;

                    factureService.supprimer(f.getIdFacture());
                    loadFactures();
                    messageLabel.setText("Facture supprimee");
                });

                // ================= PDF =================
                btnPDF.setOnAction(e -> {
                    Facture f = getTableRow().getItem();
                    if (f == null) return;

                    pdfService.generateFacturePdf(f);
                    messageLabel.setText("PDF genere avec succes");
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