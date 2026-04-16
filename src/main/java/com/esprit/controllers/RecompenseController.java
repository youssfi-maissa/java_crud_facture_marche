package com.esprit.controllers;

import com.esprit.entities.Recompense;
import com.esprit.services.RecompenseService;
import com.esprit.utils.MyDataBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RecompenseController {

    private final RecompenseService recompenseService = new RecompenseService();

    @FXML private ComboBox<String> typeField;
    @FXML private TextField valeurField;
    @FXML private TextField seuilField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dateObtentionPicker;
    @FXML private ComboBox<String> FactureComboBox;
    @FXML private ComboBox<String> livreurComboBox;

    private final Map<String, Integer> factureMap = new HashMap<>();
    private final Map<String, Integer> livreurMap = new HashMap<>();

    // ================= INIT =================
    @FXML
    public void initialize() {

        typeField.getItems().addAll(
                "bonus", "réduction", "cadeau", "prime", "remise"
        );

        chargerFactures();
        dateObtentionPicker.setValue(LocalDate.now());

        // IMPORTANT : déclenchement facture → livreur
        FactureComboBox.setOnAction(e -> {
            String factureNum = FactureComboBox.getValue();
            if (factureNum != null && factureMap.containsKey(factureNum)) {
                chargerLivreurParFacture(factureMap.get(factureNum));
            }
        });

        // validation numérique
        valeurField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*(\\.\\d*)?")) {
                valeurField.setText(o);
            }
        });

        seuilField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) {
                seuilField.setText(o);
            }
        });
    }

    // ================= ALERT =================
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ================= ERROR STYLE =================
    private void markError(Control c, boolean error) {
        if (error) {
            if (!c.getStyleClass().contains("error-field"))
                c.getStyleClass().add("error-field");
        } else {
            c.getStyleClass().remove("error-field");
        }
    }

    // ================= FACTURES =================
    private void chargerFactures() {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            Statement st = cnx.createStatement();

            ResultSet rs = st.executeQuery("SELECT ID_Facture, numero FROM factures");

            FactureComboBox.getItems().clear();
            factureMap.clear();

            while (rs.next()) {
                int id = rs.getInt("ID_Facture");
                String numero = rs.getString("numero");

                FactureComboBox.getItems().add(numero);
                factureMap.put(numero, id);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur factures", e.getMessage());
        }
    }

    // ================= LIVREUR BY FACTURE =================
    private void chargerLivreurParFacture(int factureId) {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();

            String sql =
                    "SELECT u.id_utilisateur, u.nom, u.prenom " +
                            "FROM factures f " +
                            "JOIN livraisons l ON l.ID_Livraison = f.livraison_id " +
                            "JOIN utilisateurs u ON u.id_utilisateur = l.livreur_id " +
                            "WHERE f.ID_Facture = ?";

            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, factureId);

            ResultSet rs = ps.executeQuery();

            livreurComboBox.getItems().clear();
            livreurMap.clear();

            if (rs.next()) {
                String nom = rs.getString("nom") + " " + rs.getString("prenom");
                int id = rs.getInt("id_utilisateur");

                livreurComboBox.getItems().add(nom);
                livreurComboBox.setValue(nom);
                livreurMap.put(nom, id);
            } else {
                showAlert(Alert.AlertType.WARNING,
                        "Info",
                        "Aucun livreur trouvé pour cette facture");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    // ================= AJOUT =================
    @FXML
    private void ajouterRecompense() {

        Control[] fields = {
                typeField, valeurField, seuilField,
                descriptionField, dateObtentionPicker,
                FactureComboBox, livreurComboBox
        };

        for (Control c : fields) markError(c, false);

        boolean error = false;

        String type = typeField.getValue();
        String valeur = valeurField.getText();
        String seuil = seuilField.getText();
        String desc = descriptionField.getText();
        LocalDate date = dateObtentionPicker.getValue();
        String facture = FactureComboBox.getValue();
        String livreur = livreurComboBox.getValue();

        if (type == null) { markError(typeField, true); error = true; }
        if (valeur == null || valeur.isEmpty()) { markError(valeurField, true); error = true; }
        if (seuil == null || seuil.isEmpty()) { markError(seuilField, true); error = true; }
        if (desc == null || desc.isEmpty()) { markError(descriptionField, true); error = true; }
        if (date == null) { markError(dateObtentionPicker, true); error = true; }
        if (facture == null) { markError(FactureComboBox, true); error = true; }
        if (livreur == null) { markError(livreurComboBox, true); error = true; }

        if (error) {
            showAlert(Alert.AlertType.ERROR,
                    "Formulaire incomplet",
                    "Veuillez remplir tous les champs");
            return;
        }

        try {
            double v = Double.parseDouble(valeur);
            int s = Integer.parseInt(seuil);

            Date d = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Recompense r = new Recompense(
                    type,
                    v,
                    desc,
                    s,
                    d,
                    livreurMap.get(livreur),
                    factureMap.get(facture)
            );

            recompenseService.ajouter(r);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "Récompense ajoutée avec succès");

            clearFields();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ================= TABLE VIEW =================
    @FXML
    private void ouvrirTableViewRecompense() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/esprit/recompense-table-view.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Récompenses");
            stage.show();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ================= RESET =================
    private void clearFields() {
        typeField.setValue(null);
        valeurField.clear();
        seuilField.clear();
        descriptionField.clear();
        FactureComboBox.setValue(null);
        livreurComboBox.setValue(null);
        dateObtentionPicker.setValue(LocalDate.now());
    }
}