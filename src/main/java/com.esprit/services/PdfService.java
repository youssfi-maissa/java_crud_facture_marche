package com.esprit.services;

import com.esprit.entities.Facture;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class PdfService {

    public void generateFacturePdf(Facture f) {
        try {
            String html = loadTemplate();

            float tvaMontant = f.getMontantTTC() - f.getMontantHT();

            html = html.replace("${numero}",      f.getNumero())
                    .replace("${date}",        String.valueOf(f.getDateEmission()))
                    .replace("${statut}",      f.getStatut())
                    .replace("${ht}",          String.format("%.2f", f.getMontantHT()))
                    .replace("${tva}",         String.valueOf(f.getTva()))
                    .replace("${tva_montant}", String.format("%.2f", tvaMontant))
                    .replace("${ttc}",         String.format("%.2f", f.getMontantTTC()))
                    .replace("${livraison}",   String.valueOf(f.getIdLivraison()));

            // Sauvegarde dans le dossier utilisateur (C:\Users\maissa\)
            String userHome = System.getProperty("user.home");
            File pdfFile = new File(userHome, "facture_" + f.getNumero() + ".pdf");

            try (OutputStream os = new FileOutputStream(pdfFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(html, null);
                builder.toStream(os);
                builder.run();
            }

            // Ouverture automatique du PDF apres generation
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }

            System.out.println("PDF genere : " + pdfFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String loadTemplate() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/esprit/templates/facture.html");

        if (is == null) {
            throw new RuntimeException("Template facture.html introuvable !");
        }

        return new String(is.readAllBytes());
    }
}