package com.esprit.gui;

import com.esprit.entities.Recompense;
import com.esprit.services.RecompenseService;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class RecompenseDialog extends JDialog {

    private JTextField typeField, valeurField, seuilField, livreurField, factureField;
    private JTextArea descArea;

    private Recompense recompense;
    private RecompenseService service;

    public RecompenseDialog(Frame parent, Recompense recompense, RecompenseService service) {
        super(parent, true);

        this.recompense = recompense;
        this.service = service;

        setTitle(recompense == null ? "Ajouter Récompense" : "Modifier Récompense");
        setSize(450, 420);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(7, 2, 10, 10));

        // ================= UI =================
        add(new JLabel("Type"));
        typeField = new JTextField();
        add(typeField);

        add(new JLabel("Valeur"));
        valeurField = new JTextField();
        add(valeurField);

        add(new JLabel("Description"));
        descArea = new JTextArea(3, 20);
        add(new JScrollPane(descArea));

        add(new JLabel("Seuil"));
        seuilField = new JTextField();
        add(seuilField);

        add(new JLabel("Livreur ID"));
        livreurField = new JTextField();
        add(livreurField);

        add(new JLabel("Facture ID"));
        factureField = new JTextField();
        add(factureField);

        JButton saveBtn = new JButton("Enregistrer");
        JButton cancelBtn = new JButton("Annuler");

        add(saveBtn);
        add(cancelBtn);

        // ================= PREFILL =================
        if (recompense != null) {
            typeField.setText(recompense.getType());
            valeurField.setText(String.valueOf(recompense.getValeur()));
            descArea.setText(recompense.getDescription());
            seuilField.setText(String.valueOf(recompense.getSeuil()));
            livreurField.setText(String.valueOf(recompense.getIdLivreur()));
            factureField.setText(String.valueOf(recompense.getIdFacture()));
        }

        // ================= ACTIONS =================
        saveBtn.addActionListener(e -> saveRecompense());
        cancelBtn.addActionListener(e -> dispose());
    }

    // ================= SAVE =================
    private void saveRecompense() {
        try {
            String type = typeField.getText();
            double valeur = Double.parseDouble(valeurField.getText());
            String desc = descArea.getText();
            int seuil = Integer.parseInt(seuilField.getText());
            int livreur = Integer.parseInt(livreurField.getText());
            int facture = Integer.parseInt(factureField.getText());

            if (type.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Type obligatoire !");
                return;
            }

            if (recompense == null) {
                service.ajouter(new Recompense(
                        type, valeur, desc, seuil, new Date(), livreur, facture
                ));
            } else {
                recompense.setType(type);
                recompense.setValeur(valeur);
                recompense.setDescription(desc);
                recompense.setSeuil(seuil);
                recompense.setIdLivreur(livreur);
                recompense.setIdFacture(facture);

                service.modifier(recompense);
            }

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur : vérifiez les champs numériques !");
        }
    }
}