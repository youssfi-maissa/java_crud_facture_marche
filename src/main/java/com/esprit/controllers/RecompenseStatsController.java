package com.esprit.controllers;

import com.esprit.utils.MyDataBase;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RecompenseStatsController {

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    public void initialize() {
        loadStats();
    }

    private void loadStats() {

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Récompenses par livreur");

        try {
            Connection cnx = MyDataBase.getInstance().getConnection();

            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT u.nom, u.prenom, COUNT(*) AS total " +
                            "FROM recompenses r " +
                            "JOIN utilisateurs u ON r.livreur_id = u.id_utilisateur " +
                            "GROUP BY u.nom, u.prenom"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String livreur = rs.getString("nom") + " " + rs.getString("prenom");
                int total = rs.getInt("total");

                series.getData().add(new XYChart.Data<>(livreur, total));
            }

            barChart.getData().add(series);

        } catch (Exception e) {
            System.out.println(" Erreur stats: " + e.getMessage());
        }
    }
}