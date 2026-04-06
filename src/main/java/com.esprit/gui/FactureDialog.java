package com.esprit.gui;

import com.esprit.entities.Facture;
import com.esprit.services.FactureService;

import javax.swing.*;
import java.awt.*;

public class FactureDialog extends JDialog {

    private JTextField numeroField, htField, ttcField, tvaField, statutField, livraisonField;
    private Facture facture;
    private FactureService service;

    public FactureDialog(Frame parent, Facture facture, FactureService service) {
        super(parent, true);

        this.facture = facture;
        this.service = service;

        setTitle(facture == null ? "Ajouter Facture" : "Modifier Facture");
        setSize(400, 300);
        setLayout(new GridLayout(7,2,10,10));
        setLocationRelativeTo(parent);

        add(new JLabel("Numéro"));
        numeroField = new JTextField();
        add(numeroField);

        add(new JLabel("HT"));
        htField = new JTextField();
        add(htField);

        add(new JLabel("TTC"));
        ttcField = new JTextField();
        add(ttcField);

        add(new JLabel("TVA"));
        tvaField = new JTextField();
        add(tvaField);

        add(new JLabel("Statut"));
        statutField = new JTextField();
        add(statutField);

        add(new JLabel("Livraison ID"));
        livraisonField = new JTextField();
        add(livraisonField);

        JButton saveBtn = new JButton("Enregistrer");
        add(saveBtn);

        JButton cancelBtn = new JButton("Annuler");
        add(cancelBtn);

        if (facture != null) {
            numeroField.setText(facture.getNumero());
            htField.setText(String.valueOf(facture.getMontantHT()));
            ttcField.setText(String.valueOf(facture.getMontantTTC()));
            tvaField.setText(String.valueOf(facture.getTva()));
            statutField.setText(facture.getStatut());
            livraisonField.setText(String.valueOf(facture.getIdLivraison()));
        }

        saveBtn.addActionListener(e -> saveFacture());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void saveFacture() {
        try {
            String num = numeroField.getText();
            float ht = Float.parseFloat(htField.getText());
            float ttc = Float.parseFloat(ttcField.getText());
            float tva = Float.parseFloat(tvaField.getText());
            String statut = statutField.getText();
            int idLiv = Integer.parseInt(livraisonField.getText());

            if (facture == null) {
                service.ajouter(new Facture(num, ht, ttc, tva, statut, idLiv));
            } else {
                facture.setNumero(num);
                facture.setMontantHT(ht);
                facture.setMontantTTC(ttc);
                facture.setTva(tva);
                facture.setStatut(statut);
                facture.setIdLivraison(idLiv);
                service.modifier(facture);
            }

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur !");
        }
    }
}