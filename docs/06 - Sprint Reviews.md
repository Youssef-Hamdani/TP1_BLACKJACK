# Sprint Reviews — résultats réellement constatés seulement

> Les bilans ci-dessous ne rapportent que des faits vérifiés. Aucun état Trello ni réunion non observée n'est supposé; S1 reste à documenter séparément.

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
- **Travail vérifié :** BJ-15–28, BJ-30–32. BJ-29 est optionnel et non réalisé. Les statuts officiels sont à mettre à jour par l'étudiant dans Trello. Le dépôt Git local a ensuite été créé pour BJ-37; cela ne change pas le bilan technique de S2.
- **Builds et tests constatés :** validation antérieure BJ-25–28 : 71 tests JVM et 24 Android réussis, build réussi, lint 0 erreur/12 avertissements. Validation BJ-30–31 : 23 JVM et 5 Android ciblés réussis, build réussi. Validation BJ-32/34 : 1 test de navigation réussi et 1 test Split/Room réussi avec orientation paysage imposée et vérifiée; `:app:assembleDebug` réussi. Le premier essai Split sans défilement explicite a échoué (solde attendu 900, lu 950); le test a été corrigé pour faire défiler la commande, puis a réussi. Aucun résultat de test non exécuté n'est présumé.
- **Recette Pixel 8 émulé :** en portrait, mise 75, départ (solde 925), cartes et manche inchangées après Historique → Statistiques → retour système. Rotation en paysage pendant la manche, commande Arrêter accessible après défilement, perte réglée à 925; Historique affiche mise 75/variation -75/solde final 925, Statistiques 4 cartes observées. Split paysage est couvert par le test instrumenté ciblé. Captures réelles : [[evidence/bj34/bj34_round_portrait.png]], [[evidence/bj34/bj34_active_landscape_bottom.png]], [[evidence/bj34/bj34_history_landscape.png]], [[evidence/bj34/bj34_stats_landscape.png]]. La recette BJ-30–31 avait aussi vérifié les dix valeurs statistiques en paysage et la restauration solde/mise/historique/statistiques après `force-stop`.
- **Incidents et reste à faire :** aucun blocker critique de jeu observé; pas de correction de production BJ-35 nécessaire. BJ-14 (revue S1) et BJ-29 restent non faits. Aucun appel Trello pendant ces validations.

## Sprint 3 — 15 octobre 2026
**Bilan partiel factuel rédigé le 13 octobre 2026; aucune réunion du 15 octobre n'est prétendue.**

- **Objectif prévu :** stabiliser, préparer la remise Git et pouvoir démontrer/expliquer l'application.
- **Git :** dépôt local `main` créé. Commit initial `29a8440` (`feat: complete Android Blackjack assignment`). Aucun remote n'est configuré; aucun push ni URL partagée n'est donc revendiqué et BJ-37 reste incomplet.
- **Build propre :** `clean testDebugUnitTest assembleDebug` réussi en 35 s; 71 tests JVM, 0 échec, 0 erreur, 0 ignoré; APK debug assemblé. Les recettes Pixel 8 et tests Android antérieurs restent consignés dans S2 et n'ont pas été répétés pour des changements documentaires/Git.
- **Documents :** README, backlog approuvé exporté sans modifier la baseline, Sprint Plan, décisions, Sprint Reviews, Scope Change Log et préparation de présentation inclus dans le dépôt. BJ-38 est vérifié.
- **Démonstration :** script oral de sept minutes et trace d'exécution préparés dans [[07 - Presentation Prep]]. La répétition orale chronométrée par l'étudiant n'a pas été observée; BJ-39 reste incomplet jusqu'à cette répétition.
- **Défauts connus / éléments non remis :** aucun défaut applicatif critique connu. BJ-29 optionnel et BJ-14 non faits; remote Git et répétition humaine restent à fournir.
