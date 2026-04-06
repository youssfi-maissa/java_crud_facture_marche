package com.esprit;

import com.esprit.entities.Facture;
import com.esprit.services.FactureService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FactureGUI extends JFrame {

    private FactureService service = new FactureService();
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtNumero, txtMontantHT, txtMontantTTC, txtTVA, txtStatut, txtIdLivraison;

    public FactureGUI() {
        setTitle("Gestion des Factures");
        setSize(1000, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ======= Layout principal =======
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(panel);

        // ======= Formulaire =======
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Facture"));
        formPanel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        txtNumero = addLabelAndField(formPanel, "Numéro:", 0, gbc);
        txtMontantHT = addLabelAndField(formPanel, "Montant HT:", 1, gbc);
        txtMontantTTC = addLabelAndField(formPanel, "Montant TTC:", 2, gbc);
        txtTVA = addLabelAndField(formPanel, "TVA:", 3, gbc);
        txtStatut = addLabelAndField(formPanel, "Statut:", 4, gbc);
        txtIdLivraison = addLabelAndField(formPanel, "ID Livraison:", 5, gbc);

        JButton btnAjouter = new JButton("Ajouter");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnActualiser = new JButton("Actualiser");

        btnAjouter.setBackground(new Color(76, 175, 80));
        btnAjouter.setForeground(Color.WHITE);
        btnModifier.setBackground(new Color(33, 150, 243));
        btnModifier.setForeground(Color.WHITE);
        btnSupprimer.setBackground(new Color(244, 67, 54));
        btnSupprimer.setForeground(Color.WHITE);
        btnActualiser.setBackground(new Color(255, 193, 7));

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);
        btnPanel.add(btnActualiser);
        formPanel.add(btnPanel, gbc);

        panel.add(formPanel, BorderLayout.WEST);

        // ======= Table =======
        tableModel = new DefaultTableModel(new String[]{"ID", "Numéro", "Date", "MontantHT", "MontantTTC", "TVA", "Statut", "Livraison ID"}, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Liste des factures"));
        panel.add(scrollPane, BorderLayout.CENTER);

        // ======= Actions =======
        btnActualiser.addActionListener(e -> chargerFactures());

        btnAjouter.addActionListener(e -> ajouterFacture());
        btnModifier.addActionListener(e -> modifierFacture());
        btnSupprimer.addActionListener(e -> supprimerFacture());

        // Charger au démarrage
        chargerFactures();
    }

    private JTextField addLabelAndField(JPanel panel, String label, int y, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        JTextField field = new JTextField(15);
        panel.add(field, gbc);
        return field;
    }

    private void chargerFactures() {
        tableModel.setRowCount(0);
        List<Facture> factures = service.afficherTous();
        for (Facture f : factures) {
            tableModel.addRow(new Object[]{
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

    private void ajouterFacture() {
        try {
            String numero = txtNumero.getText();
            float ht = Float.parseFloat(txtMontantHT.getText());
            float ttc = Float.parseFloat(txtMontantTTC.getText());
            float tva = Float.parseFloat(txtTVA.getText());
            String statut = txtStatut.getText();
            int idLiv = Integer.parseInt(txtIdLivraison.getText());

            service.ajouter(new Facture(numero, ht, ttc, tva, statut, idLiv));
            chargerFactures();
            JOptionPane.showMessageDialog(this, "Facture ajoutée avec succès !");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir des valeurs numériques valides pour Montants et TVA !");
        }
    }

    private void modifierFacture() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une facture à modifier !");
            return;
        }
        try {
            int id = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
            String numero = txtNumero.getText();
            float ht = Float.parseFloat(txtMontantHT.getText());
            float ttc = Float.parseFloat(txtMontantTTC.getText());
            float tva = Float.parseFloat(txtTVA.getText());
            String statut = txtStatut.getText();
            int idLiv = Integer.parseInt(txtIdLivraison.getText());

            service.modifier(new Facture(id, numero, null, ht, ttc, tva, statut, idLiv));
            chargerFactures();
            JOptionPane.showMessageDialog(this, "Facture modifiée avec succès !");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir des valeurs numériques valides !");
        }
    }

    private void supprimerFacture() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une facture à supprimer !");
            return;
        }
        int id = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
        service.supprimer(id);
        chargerFactures();
        JOptionPane.showMessageDialog(this, "Facture supprimée !");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FactureGUI().setVisible(true));
    }
}