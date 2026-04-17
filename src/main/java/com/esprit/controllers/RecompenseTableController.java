package com.esprit.controllers;

import com.esprit.entities.Recompense;
import com.esprit.services.RecompenseService;
import com.esprit.utils.MyDataBase;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Collectors;

public class RecompenseTableController {

    // ================= TABLE =================
    @FXML private TableView<Recompense> recompenseTable;

    @FXML private TableColumn<Recompense, String> typeColumn;
    @FXML private TableColumn<Recompense, Double> valeurColumn;
    @FXML private TableColumn<Recompense, String> descriptionColumn;
    @FXML private TableColumn<Recompense, Integer> seuilColumn;
    @FXML private TableColumn<Recompense, String> dateColumn;
    @FXML private TableColumn<Recompense, String> livreurColumn;
    @FXML private TableColumn<Recompense, String> factureColumn;

    @FXML private TableColumn<Recompense, Void> actionsColumn;

    // ================= UI =================
    @FXML private TextField searchField;
    @FXML private ComboBox<String> triCombo;
    @FXML private Label messageLabel;

    // ================= SERVICE =================
    private final RecompenseService service = new RecompenseService();
    private List<Recompense> allRecompenses;

    // ================= INIT =================
    @FXML
    public void initialize() {

        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        valeurColumn.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        seuilColumn.setCellValueFactory(new PropertyValueFactory<>("seuil"));

        dateColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDateObtention() != null
                                ? data.getValue().getDateObtention().toString()
                                : "-"
                )
        );

        livreurColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        getNomLivreur(data.getValue().getIdLivreur())
                )
        );

        factureColumn.setCellValueFactory(data -> {
            Integer id = data.getValue().getIdFacture();
            return new javafx.beans.property.SimpleStringProperty(
                    id == null ? "-" : getNumeroFacture(id)
            );
        });

        // ⭐ TRI OPTIONS
        triCombo.setItems(FXCollections.observableArrayList(
                "Valeur décroissante",
                "Valeur croissante",
                "Date récente",
                "Date ancienne",
                "Type A-Z"
        ));

        addActionButtons();
        actualiserTableau();
    }

    // ================= LOAD =================
    @FXML
    public void actualiserTableau() {
        allRecompenses = service.afficherTous();
        recompenseTable.setItems(FXCollections.observableArrayList(allRecompenses));
        triCombo.setValue(null);
        messageLabel.setText("");
    }

    // ================= TRI =================
    @FXML
    public void trier() {

        String choix = triCombo.getValue();
        if (choix == null) return;

        List<Recompense> sorted = allRecompenses.stream()
                .sorted((r1, r2) -> {
                    switch (choix) {

                        case "Valeur décroissante":
                            return Double.compare(r2.getValeur(), r1.getValeur());

                        case "Valeur croissante":
                            return Double.compare(r1.getValeur(), r2.getValeur());

                        case "Date récente":
                            return r2.getDateObtention().compareTo(r1.getDateObtention());

                        case "Date ancienne":
                            return r1.getDateObtention().compareTo(r2.getDateObtention());

                        case "Type A-Z":
                            return r1.getType().compareToIgnoreCase(r2.getType());

                        default:
                            return 0;
                    }
                })
                .collect(Collectors.toList());

        recompenseTable.setItems(FXCollections.observableArrayList(sorted));
    }

    // ================= TOP 5 =================
    @FXML
    public void topRecompenses() {

        List<Recompense> top = allRecompenses.stream()
                .sorted((r1, r2) -> Double.compare(r2.getValeur(), r1.getValeur()))
                .limit(5)
                .collect(Collectors.toList());

        recompenseTable.setItems(FXCollections.observableArrayList(top));
        messageLabel.setText("🔥 Top 5 récompenses");
    }

    // ================= ACTION BUTTONS =================
    private void addActionButtons() {

        actionsColumn.setCellFactory(param -> new TableCell<>() {

            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");

            {
                btnEdit.setStyle("-fx-background-color:#f59e0b; -fx-text-fill:white;");
                btnDelete.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white;");

                btnEdit.setOnAction(e -> {
                    Recompense r = getTableView().getItems().get(getIndex());
                    modifierRecompense(r);
                });

                btnDelete.setOnAction(e -> {
                    Recompense r = getTableView().getItems().get(getIndex());
                    supprimerRecompense(r);
                });
            }

            private final HBox box = new HBox(10, btnEdit, btnDelete);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ================= DELETE =================
    private void supprimerRecompense(Recompense r) {
        service.supprimer(r.getIdRecompense());
        actualiserTableau();
        messageLabel.setText("✅ Supprimé !");
    }

    // ================= MODIFY =================
    private void modifierRecompense(Recompense r) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/esprit/recompense-dialog.fxml")
            );

            Parent root = loader.load();
            RecompenseDialogController controller = loader.getController();
            controller.setRecompense(r);

            Stage stage = new Stage();
            stage.setTitle("Modifier Récompense");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            actualiserTableau();
            messageLabel.setText("✅ Modifié !");

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("❌ Erreur modification");
        }
    }

    // ================= SEARCH =================
    @FXML
    public void rechercher() {

        if (allRecompenses == null) return;

        String text = searchField.getText().toLowerCase().trim();

        if (text.isEmpty()) {
            recompenseTable.setItems(FXCollections.observableArrayList(allRecompenses));
            return;
        }

        List<Recompense> filtered = allRecompenses.stream()
                .filter(r ->
                        (r.getType() != null && r.getType().toLowerCase().contains(text)) ||
                                (r.getDescription() != null && r.getDescription().toLowerCase().contains(text))
                )
                .collect(Collectors.toList());

        recompenseTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // ================= DB HELPERS =================
    private String getNomLivreur(int idLivreur) {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT nom, prenom FROM utilisateurs WHERE id_utilisateur=?"
            );
            ps.setInt(1, idLivreur);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
                return rs.getString("nom") + " " + rs.getString("prenom");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "-";
    }

    private String getNumeroFacture(Integer id) {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT numero FROM factures WHERE ID_Facture=?"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
                return rs.getString("numero");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "-";
    }
}