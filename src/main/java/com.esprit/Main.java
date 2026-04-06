
package com.esprit;
import com.esprit.services.FactureService;
import com.esprit.entities.Facture;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        FactureService service = new FactureService();
        Scanner scanner = new Scanner(System.in);
        int choix;

        do {
            System.out.println("\n========== GESTION FACTURES ==========");
            System.out.println("1. Ajouter une facture");
            System.out.println("2. Afficher toutes les factures");
            System.out.println("3. Modifier une facture");
            System.out.println("4. Supprimer une facture");
            System.out.println("5. Rechercher une facture par ID");
            System.out.println("0. Quitter");
            System.out.print("Votre choix : ");
            choix = scanner.nextInt();

            switch (choix) {

                case 1:
                    System.out.print("Numéro : "); String num = scanner.next();
                    System.out.print("MontantHT : "); float ht = scanner.nextFloat();
                    System.out.print("MontantTTC : "); float ttc = scanner.nextFloat();
                    System.out.print("TVA : "); float tva = scanner.nextFloat();
                    System.out.print("Statut : "); String statut = scanner.next();
                    System.out.print("ID Livraison : "); int idLiv = scanner.nextInt();
                    service.ajouter(new Facture(num, ht, ttc, tva, statut, idLiv));
                    break;

                case 2:
                    List<Facture> factures = service.afficherTous();
                    if (factures.isEmpty()) {
                        System.out.println("Aucune facture trouvée.");
                    } else {
                        factures.forEach(System.out::println);
                    }
                    break;

                case 3:
                    System.out.print("ID Facture à modifier : "); int idMod = scanner.nextInt();
                    System.out.print("Nouveau numéro : "); String newNum = scanner.next();
                    System.out.print("Nouveau MontantHT : "); float newHT = scanner.nextFloat();
                    System.out.print("Nouveau MontantTTC : "); float newTTC = scanner.nextFloat();
                    System.out.print("Nouvelle TVA : "); float newTVA = scanner.nextFloat();
                    System.out.print("Nouveau Statut : "); String newStatut = scanner.next();
                    System.out.print("Nouveau ID Livraison : "); int newIdLiv = scanner.nextInt();
                    service.modifier(new Facture(idMod, newNum, null, newHT, newTTC, newTVA, newStatut, newIdLiv));
                    break;

                case 4:
                    System.out.print("ID Facture à supprimer : "); int idSup = scanner.nextInt();
                    service.supprimer(idSup);
                    break;

                case 5:
                    System.out.print("ID Facture à rechercher : "); int idRech = scanner.nextInt();
                    Facture f = service.rechercherParId(idRech);
                    System.out.println(f != null ? f : "Facture non trouvée.");
                    break;

                case 0:
                    System.out.println("Au revoir !");
                    break;

                default:
                    System.out.println("Choix invalide !");
            }

        } while (choix != 0);
    }
}