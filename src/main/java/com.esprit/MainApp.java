package com.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        VBox menu = new VBox(20);
        menu.setStyle("-fx-padding: 50; -fx-alignment: center;");

        Button facturesBtn = new Button("Gestion Factures");
        Button recompensesBtn = new Button("Gestion Récompenses");

        // ⭐ STAT RECOMPENSE
        Button statsRecompenseBtn = new Button("Statistiques Récompenses");

        // ⭐ NEW : STAT FACTURES
        Button statsFactureBtn = new Button("Statistiques Factures");

        // ================= FACTURES =================
        facturesBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/esprit/facture-view.fxml")
                );
                Scene scene = new Scene(loader.load());
                Stage moduleStage = new Stage();
                moduleStage.setTitle("Gestion Factures");
                moduleStage.setScene(scene);
                moduleStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ================= RECOMPENSES =================
        recompensesBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/esprit/recompense-view.fxml")
                );
                Scene scene = new Scene(loader.load());
                Stage moduleStage = new Stage();
                moduleStage.setTitle("Gestion Récompenses");
                moduleStage.setScene(scene);
                moduleStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ================= STAT RECOMPENSE =================
        statsRecompenseBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/esprit/recompense-stats.fxml")
                );
                Scene scene = new Scene(loader.load());
                Stage moduleStage = new Stage();
                moduleStage.setTitle("Statistiques Récompenses");
                moduleStage.setScene(scene);
                moduleStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ================= ⭐ STAT FACTURE =================
        statsFactureBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/esprit/facture-stats.fxml")
                );
                Scene scene = new Scene(loader.load());
                Stage moduleStage = new Stage();
                moduleStage.setTitle("Statistiques Factures");
                moduleStage.setScene(scene);
                moduleStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ⭐ AJOUT DES BOUTONS
        menu.getChildren().addAll(
                facturesBtn,
                recompensesBtn,
                statsRecompenseBtn,
                statsFactureBtn
        );

        Scene scene = new Scene(menu, 300, 300);
        stage.setTitle("Menu Principal");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}