package com.esprit.controllers;

import com.esprit.entities.Facture;
import com.esprit.services.FactureService;
import com.esprit.services.PdfService;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;

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

    // ============================================================
    //  NOTIFICATION TOAST
    // ============================================================
    private enum NotifType { SUCCESS, ERROR, WARNING, INFO }

    private void showNotification(NotifType type, String title, String message) {
        Stage owner = (Stage) factureTable.getScene().getWindow();

        String accent, bgColor, iconText;
        switch (type) {
            case SUCCESS -> { accent = "#1D9E75"; bgColor = "#E1F5EE"; iconText = "✓"; }
            case ERROR   -> { accent = "#E24B4A"; bgColor = "#FCEBEB"; iconText = "✕"; }
            case WARNING -> { accent = "#BA7517"; bgColor = "#FAEEDA"; iconText = "!"; }
            default      -> { accent = "#378ADD"; bgColor = "#E6F1FB"; iconText = "i"; }
        }

        Label icon = new Label(iconText);
        icon.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 26px; -fx-min-height: 26px;" +
                        "-fx-max-width: 26px;  -fx-max-height: 26px;" +
                        "-fx-alignment: center;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: " + accent + ";"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1a1a1a;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(240);

        VBox textBox = new VBox(3, titleLabel, msgLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Popup popup = new Popup();
        Label closeBtn = new Label("×");
        closeBtn.setStyle("-fx-font-size: 18px; -fx-text-fill: #aaaaaa; -fx-cursor: hand;");
        closeBtn.setOnMouseClicked(e -> popup.hide());

        HBox body = new HBox(12, icon, textBox, closeBtn);
        body.setAlignment(Pos.TOP_LEFT);
        body.setPadding(new Insets(12, 14, 12, 12));

        Rectangle strip = new Rectangle(4, 1);
        strip.setFill(Color.web(accent));
        strip.heightProperty().bind(body.heightProperty());

        HBox root = new HBox(strip, body);
        root.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0.5;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.13), 14, 0, 0, 4);"
        );
        root.setPrefWidth(320);

        popup.getContent().add(root);
        popup.setAutoHide(true);

        double x = owner.getX() + owner.getWidth()  - 340;
        double y = owner.getY() + owner.getHeight()  - 120;
        popup.show(owner, x, y);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(3.5));
        pause.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> popup.hide());
            fadeOut.play();
        });
        pause.play();
    }

    // ============================================================
    //  CONFIRMATION DIALOG
    // ============================================================
    private boolean showConfirmDialog(String ligne1, String ligne2) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(false);

        Label iconLabel = new Label("!");
        iconLabel.setStyle(
                "-fx-background-color: #FAEEDA;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 44px; -fx-min-height: 44px;" +
                        "-fx-max-width: 44px;  -fx-max-height: 44px;" +
                        "-fx-alignment: center;" +
                        "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #BA7517;"
        );

        Label titleLbl = new Label("Confirmer la suppression");
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        Label line1Lbl = new Label(ligne1);
        line1Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label line2Lbl = new Label(ligne2);
        line2Lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        VBox textBox = new VBox(6, titleLbl, line1Lbl, line2Lbl);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(16, iconLabel, textBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 12, 24));

        Separator sep = new Separator();

        final boolean[] confirmed = {false};

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #d0d0d0;" +
                        "-fx-border-width: 0.5;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-cursor: hand;" +
                        "-fx-pref-width: 110px;" +
                        "-fx-pref-height: 36px;"
        );
        btnAnnuler.setOnAction(e -> dialog.close());

        Button btnSupprimer = new Button("Oui, supprimer");
        btnSupprimer.setStyle(
                "-fx-background-color: #E24B4A;" +
                        "-fx-background-radius: 6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: white;" +
                        "-fx-cursor: hand;" +
                        "-fx-pref-width: 130px;" +
                        "-fx-pref-height: 36px;"
        );
        btnSupprimer.setOnAction(e -> { confirmed[0] = true; dialog.close(); });

        btnAnnuler.setOnMouseEntered(e ->
                btnAnnuler.setStyle(btnAnnuler.getStyle().replace("-fx-background-color: white;", "-fx-background-color: #f5f5f5;"))
        );
        btnAnnuler.setOnMouseExited(e ->
                btnAnnuler.setStyle(btnAnnuler.getStyle().replace("-fx-background-color: #f5f5f5;", "-fx-background-color: white;"))
        );
        btnSupprimer.setOnMouseEntered(e ->
                btnSupprimer.setStyle(btnSupprimer.getStyle().replace("#E24B4A", "#C03939"))
        );
        btnSupprimer.setOnMouseExited(e ->
                btnSupprimer.setStyle(btnSupprimer.getStyle().replace("#C03939", "#E24B4A"))
        );

        HBox buttons = new HBox(12, btnAnnuler, btnSupprimer);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(14, 24, 20, 24));

        VBox layout = new VBox(header, sep, buttons);
        layout.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0.5;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0, 0, 6);"
        );
        layout.setPrefWidth(380);

        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        Stage owner = (Stage) factureTable.getScene().getWindow();
        dialog.setX(owner.getX() + (owner.getWidth()  - 380) / 2);
        dialog.setY(owner.getY() + (owner.getHeight() - 160) / 2);

        dialog.showAndWait();
        return confirmed[0];
    }

    // ============================================================
    //  INIT
    // ============================================================
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
                "Numéro A-Z", "Numéro Z-A", "HT croissant", "HT décroissant",
                "TTC croissant", "TTC décroissant", "Statut", "Livraison"
        ));
    }

    // ============================================================
    //  LOAD
    // ============================================================
    private void loadFactures() {
        sourceList   = FXCollections.observableArrayList(new ArrayList<>(factureService.afficherTous()));
        filteredData = new FilteredList<>(sourceList, p -> true);
        factureTable.setItems(filteredData);
    }

    // ============================================================
    //  SEARCH
    // ============================================================
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

    // ============================================================
    //  TRI
    // ============================================================
    @FXML
    private void trierFactures() {
        String choix = triCombo.getValue();
        if (choix == null) return;

        switch (choix) {
            case "Numéro A-Z"      -> sourceList.sort(Comparator.comparing(Facture::getNumero, Comparator.nullsLast(String::compareToIgnoreCase)));
            case "Numéro Z-A"      -> sourceList.sort(Comparator.comparing(Facture::getNumero, Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
            case "HT croissant"    -> sourceList.sort(Comparator.comparingDouble(Facture::getMontantHT));
            case "HT décroissant"  -> sourceList.sort(Comparator.comparingDouble(Facture::getMontantHT).reversed());
            case "TTC croissant"   -> sourceList.sort(Comparator.comparingDouble(Facture::getMontantTTC));
            case "TTC décroissant" -> sourceList.sort(Comparator.comparingDouble(Facture::getMontantTTC).reversed());
            case "Statut"          -> sourceList.sort(Comparator.comparing(Facture::getStatut, Comparator.nullsLast(String::compareToIgnoreCase)));
            case "Livraison"       -> sourceList.sort(Comparator.comparingInt(Facture::getIdLivraison));
        }
        factureTable.refresh();
    }

    // ============================================================
    //  RESET
    // ============================================================
    @FXML
    private void resetAll() {
        searchField.clear();
        triCombo.setValue(null);
        loadFactures();
    }

    // ============================================================
    //  ACTIONS
    // ============================================================
    private void addActions() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button btnModifier = new Button("Modifier");
            private final Button btnDelete   = new Button("Supprimer");
            private final Button btnPDF      = new Button("PDF");
            private final HBox   box         = new HBox(8, btnModifier, btnDelete, btnPDF);

            {
                box.setAlignment(Pos.CENTER);

                btnModifier.setStyle(
                        "-fx-background-color: #f59e0b; -fx-text-fill: white;" +
                                "-fx-background-radius: 5; -fx-cursor: hand;"
                );
                btnDelete.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white;" +
                                "-fx-background-radius: 5; -fx-cursor: hand;"
                );
                btnPDF.setStyle(
                        "-fx-background-color: #378ADD; -fx-text-fill: white;" +
                                "-fx-background-radius: 5; -fx-cursor: hand;"
                );

                // ===== MODIFIER =====
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

                        FactureController controller = loader.getController();
                        controller.setFactureToEdit(f);

                        stage.showAndWait();
                        loadFactures();
                        showNotification(NotifType.SUCCESS, "Modifié", "Facture modifiée avec succès.");

                    } catch (Exception ex) {
                        showNotification(NotifType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire.");
                    }
                });

                // ===== DELETE =====
                btnDelete.setOnAction(e -> {
                    Facture f = getTableRow().getItem();
                    if (f == null) return;

                    boolean confirmed = showConfirmDialog(
                            "Voulez-vous supprimer cette facture ?",
                            "Numéro : " + f.getNumero() + "  |  TTC : " + f.getMontantTTC()
                    );

                    if (!confirmed) return;

                    factureService.supprimer(f.getIdFacture());
                    loadFactures();
                    showNotification(NotifType.SUCCESS, "Supprimé", "La facture a été supprimée.");
                });

                // ===== PDF =====
                btnPDF.setOnAction(e -> {
                    Facture f = getTableRow().getItem();
                    if (f == null) return;
                    String url = pdfService.generateFacturePdf(f);
                    showNotification(NotifType.INFO, "PDF généré", url != null ? url : "Fichier créé.");
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