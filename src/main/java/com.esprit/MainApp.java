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
        // Menu principal avec boutons pour chaque module
        VBox menu = new VBox(20);
        menu.setStyle("-fx-padding: 50; -fx-alignment: center;");

        Button facturesBtn = new Button("Gestion Factures");
        Button recompensesBtn = new Button("Gestion Récompenses");

        // Action pour ouvrir Factures
        facturesBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/facture-view.fxml"));
                Scene scene = new Scene(loader.load());
                Stage moduleStage = new Stage();
                moduleStage.setTitle("Gestion Factures");
                moduleStage.setScene(scene);
                moduleStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Action pour ouvrir Récompenses
        recompensesBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/recompense-view.fxml"));
                Scene scene = new Scene(loader.load());
                Stage moduleStage = new Stage();
                moduleStage.setTitle("Gestion Récompenses");
                moduleStage.setScene(scene);
                moduleStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        menu.getChildren().addAll(facturesBtn, recompensesBtn);

        Scene scene = new Scene(menu, 300, 200);
        stage.setTitle("Menu Principal");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}