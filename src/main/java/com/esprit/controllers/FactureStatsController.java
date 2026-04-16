package com.esprit.controllers;

import com.esprit.utils.MyDataBase;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FactureStatsController {

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    public void initialize() {
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false); // ⭐ enlève les points rouges
        loadCAParMois();
    }

    private void loadCAParMois() {

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("CA mensuel");

        try {
            Connection cnx = MyDataBase.getInstance().getConnection();

            String sql =
                    "SELECT MONTH(dateEmission) AS mois, SUM(montantTTC) AS ca " +
                            "FROM factures " +
                            "GROUP BY MONTH(dateEmission) " +
                            "ORDER BY mois";

            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                int mois = rs.getInt("mois");
                double ca = rs.getDouble("ca");

                series.getData().add(
                        new XYChart.Data<>(getMonthName(mois), ca)
                );
            }

            lineChart.getData().clear(); // ⭐ évite duplication
            lineChart.getData().add(series);

        } catch (Exception e) {
            System.out.println("Erreur stats facture: " + e.getMessage());
        }
    }

    private String getMonthName(int m) {
        return switch (m) {
            case 1 -> "Jan";
            case 2 -> "Fév";
            case 3 -> "Mar";
            case 4 -> "Avr";
            case 5 -> "Mai";
            case 6 -> "Juin";
            case 7 -> "Juil";
            case 8 -> "Août";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Déc";
            default -> "";
        };
    }
}