package com.esprit.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.esprit.entities.Facture;
import com.esprit.utils.CloudinaryConfig;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

public class PdfService {

    private final Cloudinary cloudinary = CloudinaryConfig.getInstance();

    public String generateFacturePdf(Facture f) {
        String cloudinaryUrl = null;

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

            // Fichier temporaire local
            File pdfDir = new File("pdfs");
            pdfDir.mkdirs(); // crée le dossier s'il n'existe pas
            File pdfFile = new File(pdfDir, "facture_" + f.getNumero() + ".pdf");

            // Génération du PDF
            try (OutputStream os = new FileOutputStream(pdfFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(html, null);
                builder.toStream(os);
                builder.run();
            }

            System.out.println("PDF généré localement : " + pdfFile.getAbsolutePath());

            // ─── Upload vers Cloudinary ───────────────────────────────────────
            String publicId = "factures/facture_" + f.getNumero();

            Map<?, ?> result = cloudinary.uploader().upload(pdfFile, ObjectUtils.asMap(
                    "resource_type", "raw",       // obligatoire pour les PDF
                    "public_id",     publicId,
                    "overwrite",     true
            ));

            cloudinaryUrl = (String) result.get("secure_url");
            System.out.println("PDF uploadé sur Cloudinary : " + cloudinaryUrl);
            // ─────────────────────────────────────────────────────────────────

            // Ouverture automatique du PDF local après génération
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cloudinaryUrl; // null si l'upload a échoué
    }

    private String loadTemplate() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/esprit/templates/facture.html");

        if (is == null) {
            throw new RuntimeException("Template facture.html introuvable !");
        }

        return new String(is.readAllBytes());
    }
}