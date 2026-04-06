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
        setSize(400, 400);
        setLayout(new GridLayout(7,2,10,10));
        setLocationRelativeTo(parent);

        add(new JLabel("Type"));
        typeField = new JTextField();
        add(typeField);

        add(new JLabel("Valeur"));
        valeurField = new JTextField();
        add(valeurField);

        add(new JLabel("Description"));
        descArea = new JTextArea();
        add(descArea);

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
        add(saveBtn);

        JButton cancelBtn = new JButton("Annuler");
        add(cancelBtn);

        if (recompense != null) {
            typeField.setText(recompense.getType());
            valeurField.setText(String.valueOf(recompense.getValeur()));
            descArea.setText(recompense.getDescription());
            seuilField.setText(String.valueOf(recompense.getSeuil()));
            livreurField.setText(String.valueOf(recompense.getIdLivreur()));
            factureField.setText(String.valueOf(recompense.getIdFacture()));
        }

        saveBtn.addActionListener(e -> saveRecompense());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void saveRecompense() {
        try {
            String type = typeField.getText();
            double val = Double.parseDouble(valeurField.getText());
            String desc = descArea.getText();
            int seuil = Integer.parseInt(seuilField.getText());
            int livreur = Integer.parseInt(livreurField.getText());
            int facture = Integer.parseInt(factureField.getText());

            if (recompense == null) {
                service.ajouter(new Recompense(type, val, desc, seuil, new Date(), livreur, facture));
            } else {
                recompense.setType(type);
                recompense.setValeur(val);
                recompense.setDescription(desc);
                recompense.setSeuil(seuil);
                recompense.setIdLivreur(livreur);
                recompense.setIdFacture(facture);
                service.modifier(recompense);
            }

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur !");
        }
    }
}