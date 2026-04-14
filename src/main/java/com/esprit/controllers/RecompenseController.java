package com.esprit.controllers;

import com.esprit.entities.Recompense;
import com.esprit.services.RecompenseService;
import com.esprit.utils.MyDataBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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
    @FXML private ComboBox<String> livreurComboBox;
    @FXML private ComboBox<String> FactureComboBox;
    @FXML private Label messageLabel;

    private final Map<String, Integer> livreurMap = new HashMap<>();
    private final Map<String, Integer> factureMap = new HashMap<>();

    @FXML
    public void initialize() {
        typeField.getItems().addAll(
                "bonus", "réduction", "cadeau", "prime", "remise"
        );

        chargerFactures();
        dateObtentionPicker.setValue(LocalDate.now());

        // Empêcher la saisie non numérique
        valeurField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                valeurField.setText(oldVal);
            }
        });

        seuilField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                seuilField.setText(oldVal);
            }
        });

        // Quand facture choisie → livreur automatique
        FactureComboBox.setOnAction(e -> {
            String factureNum = FactureComboBox.getValue();
            if (factureNum != null) {
                chargerLivreurParFacture(factureMap.get(factureNum));
            }
        });
    }

    private void chargerFactures() {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT ID_Facture, numero FROM factures");

            while (rs.next()) {
                String numero = rs.getString("numero");
                int id = rs.getInt("ID_Facture");
                FactureComboBox.getItems().add(numero);
                factureMap.put(numero, id);
            }
        } catch (Exception e) {
            messageLabel.setText("Erreur factures : " + e.getMessage());
        }
    }

    private void chargerLivreurParFacture(int factureId) {
        try {
            Connection cnx = MyDataBase.getInstance().getConnection();
            String sql = "SELECT u.id_utilisateur, u.nom, u.prenom " +
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
                String nomComplet = rs.getString("nom") + " " + rs.getString("prenom");
                int id = rs.getInt("id_utilisateur");

                livreurComboBox.getItems().add(nomComplet);
                livreurComboBox.setValue(nomComplet);
                livreurMap.put(nomComplet, id);
                messageLabel.setText("");
            } else {
                livreurComboBox.setValue(null);
                messageLabel.setText("Aucun livreur trouvé pour cette facture !");
            }
        } catch (Exception e) {
            messageLabel.setText("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void ajouterRecompense() {
        try {
            String type = typeField.getValue();
            String description = descriptionField.getText().trim();
            String valeurText = valeurField.getText().trim();
            String seuilText = seuilField.getText().trim();
            String livreurNom = livreurComboBox.getValue();
            String factureNum = FactureComboBox.getValue();
            LocalDate date = dateObtentionPicker.getValue();

            // Validation champs obligatoires
            if (type == null || type.isEmpty()
                    || description.isEmpty()
                    || valeurText.isEmpty()
                    || seuilText.isEmpty()
                    || livreurNom == null
                    || factureNum == null
                    || date == null) {

                showAlert(Alert.AlertType.WARNING,
                        "Champs manquants",
                        "Veuillez remplir tous les champs !");
                return;
            }

            double valeur = Double.parseDouble(valeurText);
            int seuil = Integer.parseInt(seuilText);

            // Validation valeurs positives
            if (valeur <= 0 || seuil <= 0) {
                showAlert(Alert.AlertType.WARNING,
                        "Valeurs invalides",
                        "La valeur et le seuil doivent être positifs !");
                return;
            }

            int livreurId = livreurMap.get(livreurNom);
            Integer factureId = factureMap.get(factureNum);

            Date dateObtention = Date.from(
                    date.atStartOfDay(ZoneId.systemDefault()).toInstant()
            );

            Recompense recompense = new Recompense(
                    type,
                    valeur,
                    description,
                    seuil,
                    dateObtention,
                    livreurId,
                    factureId
            );

            recompenseService.ajouter(recompense);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "Récompense ajoutée avec succès !");

            clearFields();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de saisie",
                    "Valeur et seuil doivent être numériques !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    e.getMessage());
        }
    }

    @FXML
    private void ouvrirTableViewRecompense() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/esprit/recompense-table-view.fxml")
            );
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Liste des Récompenses");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            messageLabel.setText("Erreur ouverture tableau : " + e.getMessage());
        }
    }

    private void clearFields() {
        typeField.getSelectionModel().clearSelection();
        valeurField.clear();
        seuilField.clear();
        descriptionField.clear();
        dateObtentionPicker.setValue(LocalDate.now());
        livreurComboBox.getItems().clear();
        livreurComboBox.setValue(null);
        FactureComboBox.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
