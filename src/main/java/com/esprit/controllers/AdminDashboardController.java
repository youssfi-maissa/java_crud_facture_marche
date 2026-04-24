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
            var url = getClass().getResource("/com/esprit/" + fxml);

            System.out.println("=== CHARGEMENT : " + fxml + " ===");
            System.out.println("URL = " + url);

            if (url == null) {
                System.out.println("FICHIER INTROUVABLE");
                Label error = new Label("Fichier introuvable : " + fxml);
                error.setStyle("-fx-text-fill: red;");
                return error;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Node node = loader.load();

            if (node instanceof javafx.scene.layout.Region region) {
                region.getStylesheets().clear();
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
                VBox.setVgrow(region, javafx.scene.layout.Priority.ALWAYS);
            }

            return node;

        } catch (Exception e) {
            System.out.println("=== ERREUR : " + fxml + " ===");
            System.out.println("Message     : " + e.getMessage());
            System.out.println("Cause       : " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
            e.printStackTrace();

            Label error = new Label("Erreur de chargement : " + fxml);
            error.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            return error;
        }
    }}