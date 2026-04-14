package com.esprit.controllers;

import com.esprit.entities.Facture;
import com.esprit.services.FactureService;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;

import java.io.FileOutputStream;

// PDF iText
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

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

    @FXML private Label messageLabel;

    // 🔍 SEARCH
    @FXML private TextField searchField;
    private FilteredList<Facture> filteredData;

    @FXML
    public void initialize() {
        initColumns();
        addActionsColumn();
        loadFactures();
    }

    // ================= TABLE INIT =================
    private void initColumns() {
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
        montantHTColumn.setCellValueFactory(new PropertyValueFactory<>("montantHT"));
        montantTTCColumn.setCellValueFactory(new PropertyValueFactory<>("montantTTC"));
        tvaColumn.setCellValueFactory(new PropertyValueFactory<>("tva"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        livraisonColumn.setCellValueFactory(new PropertyValueFactory<>("idLivraison"));
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

            if (keyword == null || keyword.isEmpty()) {
                return true;
            }

            String lower = keyword.toLowerCase();

            return f.getNumero().toLowerCase().contains(lower)
                    || f.getStatut().toLowerCase().contains(lower)
                    || String.valueOf(f.getMontantHT()).contains(lower)
                    || String.valueOf(f.getMontantTTC()).contains(lower)
                    || String.valueOf(f.getIdLivraison()).contains(lower);
        });
    }

    // ================= ACTIONS =================
    private void addActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final Button btnPDF = new Button("PDF");

            private final HBox box = new HBox(8, btnModifier, btnSupprimer, btnPDF);

            {
                box.setAlignment(Pos.CENTER);

                // MODIFIER
                btnModifier.setOnAction(e -> {
                    Facture f = getTableRow().getItem();
                    if (f == null) return;

                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.setTitle("Modifier Facture");

                    TextField tfNumero = new TextField(f.getNumero());
                    TextField tfHT = new TextField(String.valueOf(f.getMontantHT()));
                    TextField tfTVA = new TextField(String.valueOf(f.getTva()));

                    ComboBox<String> cbStatut = new ComboBox<>();
                    cbStatut.getItems().addAll("payee", "en attente", "annulee");
                    cbStatut.setValue(f.getStatut());

                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);

                    grid.add(new Label("Numéro"), 0, 0);
                    grid.add(tfNumero, 1, 0);

                    grid.add(new Label("HT"), 0, 1);
                    grid.add(tfHT, 1, 1);

                    grid.add(new Label("TVA"), 0, 2);
                    grid.add(tfTVA, 1, 2);

                    grid.add(new Label("Statut"), 0, 3);
                    grid.add(cbStatut, 1, 3);

                    dialog.getDialogPane().setContent(grid);
                    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                    dialog.showAndWait().ifPresent(res -> {
                        if (res == ButtonType.OK) {
                            try {
                                f.setNumero(tfNumero.getText());
                                f.setMontantHT(Float.parseFloat(tfHT.getText()));
                                f.setTva(Float.parseFloat(tfTVA.getText()));
                                f.setStatut(cbStatut.getValue());

                                float ttc = f.getMontantHT() + (f.getMontantHT() * f.getTva() / 100);
                                f.setMontantTTC(ttc);

                                factureService.modifier(f);
                                loadFactures();

                                messageLabel.setText("✔ Facture modifiée");

                            } catch (Exception ex) {
                                messageLabel.setText("❌ Erreur modification");
                            }
                        }
                    });
                });

                // SUPPRIMER
                btnSupprimer.setOnAction(e -> {
                    Facture f = getTableRow().getItem();
                    if (f == null) return;

                    factureService.supprimer(f.getIdFacture());
                    loadFactures();
                    messageLabel.setText("✔ Supprimée");
                });

                // PDF
                btnPDF.setOnAction(e -> {
                    Facture f = getTableRow().getItem();
                    if (f == null) return;
                    exportPDF(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ================= PDF =================
    private void exportPDF(Facture f) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document,
                    new FileOutputStream("facture_" + f.getNumero() + ".pdf"));

            document.open();

            Font title = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph p = new Paragraph("FACTURE OFFICIELLE\n\n", title);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            document.add(new Paragraph("Numéro : " + f.getNumero()));
            document.add(new Paragraph("HT : " + f.getMontantHT()));
            document.add(new Paragraph("TVA : " + f.getTva()));
            document.add(new Paragraph("TTC : " + f.getMontantTTC()));
            document.add(new Paragraph("Statut : " + f.getStatut()));
            document.add(new Paragraph("Livraison : " + f.getIdLivraison()));

            document.close();

            messageLabel.setText("📄 PDF généré");

        } catch (Exception e) {
            messageLabel.setText("❌ Erreur PDF");
        }
    }
}