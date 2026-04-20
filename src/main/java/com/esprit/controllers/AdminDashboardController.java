package com.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AdminDashboardController {

    @FXML
    private VBox contentArea;

    // ================= DASHBOARD =================
    @FXML
    public void showDashboard() {
        contentArea.getChildren().clear();

        Label title = new Label("Bienvenue sur PackTrack Admin");
        title.getStyleClass().add("title");

        Label sub = new Label("Sélectionnez une section dans le menu.");
        sub.getStyleClass().add("subtitle");

        contentArea.getChildren().addAll(title, sub);
    }

    // ================= FACTURES =================
    @FXML
    public void showFactures() {
        Node view = loadView("facture-table-view.fxml");
        if (view != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    // ================= RECOMPENSES =================
    @FXML
    public void showRecompenses() {
        Node view = loadView("recompense-table-view.fxml");
        if (view != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    // ================= STATS FACTURES =================
    @FXML
    public void showFactureStats() {
        Node view = loadView("facture-stats.fxml");
        if (view != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    // ================= STATS RECOMPENSES =================
    @FXML
    public void showRecompenseStats() {
        Node view = loadView("recompense-stats.fxml");
        if (view != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    // ================= AJOUT FACTURE =================
    @FXML
    public void showAjoutFacture() {
        Node view = loadView("facture-view.fxml");
        if (view != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    // ================= AJOUT RECOMPENSE =================
    @FXML
    public void showAjoutRecompense() {
        Node view = loadView("recompense-view.fxml");
        if (view != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    // ================= LOADER CENTRAL =================
    private Node loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/esprit/" + fxml)
            );

            Node node = loader.load();

            return node;

        } catch (Exception e) {
            System.out.println("❌ Erreur chargement FXML : " + fxml);
            e.printStackTrace();

            Label error = new Label("Erreur de chargement : " + fxml);
            error.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            return error;
        }
    }
}