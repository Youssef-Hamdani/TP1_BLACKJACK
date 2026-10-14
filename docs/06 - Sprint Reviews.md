# Sprint Reviews — résultats réellement constatés seulement

> Seul le bilan factuel S2 ci-dessous est rédigé. Aucun commit ni état Trello n'est supposé; S1 et S3 restent à documenter séparément.

## Sprint 1 — 13 octobre 2026
**Statut :** à compléter uniquement après travail réel.

- Objectif prévu :
- Cartes réellement terminées (IDs Trello + liens) :
- Builds exécutés (commande, environnement, résultat) :
- Tests exécutés (commande, résultats) :
- Démo / captures réellement réalisées :
- Non terminés, incidents, blocages :
- Adaptations prévues pour le prochain sprint (avec lien log) :

## Sprint 2 — 14 octobre 2026
**Bilan factuel rédigé le 13 octobre 2026 au soir, avant la date S2 prévue.** Le travail a été accéléré avec l'assistance IA; ceci ne prétend pas qu'une réunion de revue a eu lieu le 14 ni que Trello a été modifié.

- **Objectif prévu :** règles complètes (mises, Split, sabot), Room/DAO/Repository, historique/statistiques réactifs et navigation utilisable en portrait/paysage sur Pixel 8.
- **Travail vérifié :** BJ-15–28, BJ-30–32. BJ-29 est optionnel et non réalisé. Les statuts officiels sont à mettre à jour par l'étudiant dans Trello; aucun commit ni dépôt Git n'existe encore dans `TP1_BLACKJACK`.
- **Builds et tests constatés :** validation antérieure BJ-25–28 : 71 tests JVM et 24 Android réussis, build réussi, lint 0 erreur/12 avertissements. Validation BJ-30–31 : 23 JVM et 5 Android ciblés réussis, build réussi. Validation BJ-32/34 : 1 test de navigation réussi et 1 test Split/Room réussi avec orientation paysage imposée et vérifiée; `:app:assembleDebug` réussi. Le premier essai Split sans défilement explicite a échoué (solde attendu 900, lu 950); le test a été corrigé pour faire défiler la commande, puis a réussi. Aucun résultat de test non exécuté n'est présumé.
- **Recette Pixel 8 émulé :** en portrait, mise 75, départ (solde 925), cartes et manche inchangées après Historique → Statistiques → retour système. Rotation en paysage pendant la manche, commande Arrêter accessible après défilement, perte réglée à 925; Historique affiche mise 75/variation -75/solde final 925, Statistiques 4 cartes observées. Split paysage est couvert par le test instrumenté ciblé. Captures réelles : [[evidence/bj34/bj34_round_portrait.png]], [[evidence/bj34/bj34_active_landscape_bottom.png]], [[evidence/bj34/bj34_history_landscape.png]], [[evidence/bj34/bj34_stats_landscape.png]]. La recette BJ-30–31 avait aussi vérifié les dix valeurs statistiques en paysage et la restauration solde/mise/historique/statistiques après `force-stop`.
- **Incidents et reste à faire :** aucun blocker critique de jeu observé; pas de correction de production BJ-35 nécessaire. BJ-14 (revue S1) et BJ-29 restent non faits. Remise Git, documents Scrum finaux et répétition de présentation relèvent encore de S3; ne pas les déclarer terminés. Aucun appel Trello pendant ces validations.

## Sprint 3 — 15 octobre 2026
**Statut :** à compléter uniquement après travail réel, avant la remise / présentation.

- Objectif prévu :
- Dernier commit et URL dépôt partagée :
- Résultat recette Pixel 8 portrait et paysage :
- Build final (commande, résultat) :
- Démo et explications effectivement préparées :
- Défauts connus / éléments remis et non remis :
