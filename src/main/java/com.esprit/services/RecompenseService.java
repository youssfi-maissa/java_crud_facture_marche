package com.esprit.services;

import com.esprit.entities.Recompense;
import com.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecompenseService {

    private Connection connection;

    public RecompenseService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public void ajouter(Recompense r) {
        String req = "INSERT INTO recompenses (type, valeur, description, seuil, dateObtention, livreur_id, facture_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, r.getType());
            ps.setDouble(2, r.getValeur());
            ps.setString(3, r.getDescription());
            ps.setInt(4, r.getSeuil());
            if (r.getDateObtention() != null)
                ps.setTimestamp(5, new Timestamp(r.getDateObtention().getTime()));
            else
                ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.setInt(6, r.getIdLivreur());
            if (r.getIdFacture() != null)
                ps.setInt(7, r.getIdFacture());
            else
                ps.setNull(7, Types.INTEGER);
            ps.executeUpdate();
            System.out.println("✅ Recompense ajoutée !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur ajout : " + e.getMessage());
        }
    }

    public List<Recompense> afficherTous() {
        List<Recompense> list = new ArrayList<>();
        String req = "SELECT * FROM recompenses";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                list.add(new Recompense(
                        rs.getInt("ID_Recompense"),
                        rs.getString("type"),
                        rs.getDouble("valeur"),
                        rs.getString("description"),
                        rs.getInt("seuil"),
                        rs.getTimestamp("dateObtention"),
                        rs.getInt("livreur_id"),
                        rs.getObject("facture_id") != null ? rs.getInt("facture_id") : null
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur affichage : " + e.getMessage());
        }
        return list;
    }

    public void modifier(Recompense r) {
        String req = "UPDATE recompenses SET type=?, valeur=?, description=?, seuil=?, dateObtention=?, livreur_id=?, facture_id=? WHERE ID_Recompense=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, r.getType());
            ps.setDouble(2, r.getValeur());
            ps.setString(3, r.getDescription());
            ps.setInt(4, r.getSeuil());
            ps.setTimestamp(5, new Timestamp(r.getDateObtention().getTime()));
            ps.setInt(6, r.getIdLivreur());
            if (r.getIdFacture() != null)
                ps.setInt(7, r.getIdFacture());
            else
                ps.setNull(7, Types.INTEGER);
            ps.setInt(8, r.getIdRecompense());
            ps.executeUpdate();
            System.out.println("✅ Recompense modifiée !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur modification : " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String req = "DELETE FROM recompenses WHERE ID_Recompense=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Recompense supprimée !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur suppression : " + e.getMessage());
        }
    }

    public Recompense rechercherParId(int id) {
        String req = "SELECT * FROM recompenses WHERE ID_Recompense=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Recompense(
                        rs.getInt("ID_Recompense"),
                        rs.getString("type"),
                        rs.getDouble("valeur"),
                        rs.getString("description"),
                        rs.getInt("seuil"),
                        rs.getTimestamp("dateObtention"),
                        rs.getInt("livreur_id"),
                        rs.getObject("facture_id") != null ? rs.getInt("facture_id") : null
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur recherche : " + e.getMessage());
        }
        return null;
    }
}