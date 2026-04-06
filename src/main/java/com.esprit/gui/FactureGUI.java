package com.esprit.gui;

import com.esprit.entities.Facture;
import com.esprit.entities.Recompense;
import com.esprit.services.FactureService;
import com.esprit.services.RecompenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FactureGUI extends JFrame {

    private FactureService factureService;
    private RecompenseService recompenseService;

    private JTable factureTable;
    private JTable recompenseTable;

    private DefaultTableModel factureModel;
    private DefaultTableModel recompenseModel;

    public FactureGUI() {
        factureService = new FactureService();
        recompenseService = new RecompenseService();

        setTitle("Gestion Factures & Récompenses");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Onglets pour Factures et Récompenses
        JTabbedPane tabs = new JTabbedPane();

        // ----- Onglet Factures -----
        JPanel facturePanel = new JPanel(new BorderLayout());
        factureModel = new DefaultTableModel(new String[]{"ID", "Numéro", "Date", "MontantHT", "MontantTTC", "TVA", "Statut", "Livraison ID"}, 0);
        factureTable = new JTable(factureModel);
        factureTable.setRowHeight(25);
        facturePanel.add(new JScrollPane(factureTable), BorderLayout.CENTER);

        JPanel factureButtons = new JPanel();
        JButton addFactureBtn = new JButton("Ajouter");
        JButton editFactureBtn = new JButton("Modifier");
        JButton deleteFactureBtn = new JButton("Supprimer");
        JButton refreshFactureBtn = new JButton("Actualiser");
        factureButtons.add(addFactureBtn);
        factureButtons.add(editFactureBtn);
        factureButtons.add(deleteFactureBtn);
        factureButtons.add(refreshFactureBtn);
        facturePanel.add(factureButtons, BorderLayout.SOUTH);

        tabs.addTab("Factures", facturePanel);

        // ----- Onglet Récompenses -----
        JPanel recompensePanel = new JPanel(new BorderLayout());
        recompenseModel = new DefaultTableModel(new String[]{"ID", "Type", "Valeur", "Description", "Seuil", "Date", "Livreur ID", "Facture ID"}, 0);
        recompenseTable = new JTable(recompenseModel);
        recompenseTable.setRowHeight(25);
        recompensePanel.add(new JScrollPane(recompenseTable), BorderLayout.CENTER);

        JPanel recompenseButtons = new JPanel();
        JButton addRecompBtn = new JButton("Ajouter");
        JButton editRecompBtn = new JButton("Modifier");
        JButton deleteRecompBtn = new JButton("Supprimer");
        JButton refreshRecompBtn = new JButton("Actualiser");
        recompenseButtons.add(addRecompBtn);
        recompenseButtons.add(editRecompBtn);
        recompenseButtons.add(deleteRecompBtn);
        recompenseButtons.add(refreshRecompBtn);
        recompensePanel.add(recompenseButtons, BorderLayout.SOUTH);

        tabs.addTab("Récompenses", recompensePanel);

        add(tabs);

        // ----- Action Listeners -----
        refreshFactureBtn.addActionListener(e -> loadFactures());
        refreshRecompBtn.addActionListener(e -> loadRecompenses());

        addFactureBtn.addActionListener(e -> openFactureDialog(null));
        editFactureBtn.addActionListener(e -> editSelectedFacture());
        deleteFactureBtn.addActionListener(e -> deleteSelectedFacture());

        addRecompBtn.addActionListener(e -> openRecompenseDialog(null));
        editRecompBtn.addActionListener(e -> editSelectedRecompense());
        deleteRecompBtn.addActionListener(e -> deleteSelectedRecompense());

        // Charger les données au démarrage
        loadFactures();
        loadRecompenses();
    }

    // -------------------- FACTURES --------------------
    private void loadFactures() {
        factureModel.setRowCount(0);
        List<Facture> factures = factureService.afficherTous();
        for (Facture f : factures) {
            factureModel.addRow(new Object[]{
                    f.getIdFacture(),
                    f.getNumero(),
                    f.getDateEmission(),
                    f.getMontantHT(),
                    f.getMontantTTC(),
                    f.getTva(),
                    f.getStatut(),
                    f.getIdLivraison()
            });
        }
    }

    private void openFactureDialog(Facture f) {
        FactureDialog dialog = new FactureDialog(this, f, factureService);
        dialog.setVisible(true);
        loadFactures();
    }

    private void editSelectedFacture() {
        int row = factureTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) factureModel.getValueAt(row, 0);
            Facture f = factureService.rechercherParId(id);
            openFactureDialog(f);
        } else JOptionPane.showMessageDialog(this, "Sélectionnez une facture à modifier !");
    }

    private void deleteSelectedFacture() {
        int row = factureTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) factureModel.getValueAt(row, 0);
            factureService.supprimer(id);
            loadFactures();
        } else JOptionPane.showMessageDialog(this, "Sélectionnez une facture à supprimer !");
    }

    // -------------------- RECOMPENSES --------------------
    private void loadRecompenses() {
        recompenseModel.setRowCount(0);
        List<Recompense> recompenses = recompenseService.afficherTous();
        for (Recompense r : recompenses) {
            recompenseModel.addRow(new Object[]{
                    r.getIdRecompense(),
                    r.getType(),
                    r.getValeur(),
                    r.getDescription(),
                    r.getSeuil(),
                    r.getDateObtention(),
                    r.getIdLivreur(),
                    r.getIdFacture()
            });
        }
    }

    private void openRecompenseDialog(Recompense r) {
        RecompenseDialog dialog = new RecompenseDialog(this, r, recompenseService);
        dialog.setVisible(true);
        loadRecompenses();
    }

    private void editSelectedRecompense() {
        int row = recompenseTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) recompenseModel.getValueAt(row, 0);
            Recompense r = recompenseService.rechercherParId(id);
            openRecompenseDialog(r);
        } else JOptionPane.showMessageDialog(this, "Sélectionnez une récompense à modifier !");
    }

    private void deleteSelectedRecompense() {
        int row = recompenseTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) recompenseModel.getValueAt(row, 0);
            recompenseService.supprimer(id);
            loadRecompenses();
        } else JOptionPane.showMessageDialog(this, "Sélectionnez une récompense à supprimer !");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new com.esprit.gui.FactureGUI().setVisible(true));
    }
}