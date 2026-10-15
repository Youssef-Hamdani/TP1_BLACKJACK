# Scope Change Log — changements après la baseline v0.1

Baseline de 40 cartes / 30,75 h approuvée le 13 octobre 2026. Voir `docs/baseline/Blackjack_Backlog_PROPOSITION_v0.1.csv`.

Ce journal a été **reconstitué a posteriori** le 14 octobre à partir des demandes de l'étudiant et de commits Git existants; il ne prétend pas avoir été écrit avant chaque décision. Les statuts Trello restent la source de vérité des cartes.

| Date (Québec) | Sujet | Avant | Après / preuve | Justification et impact |
|---|---|---|---|---|
| 14 oct. 2026 | Présentation du résultat | Mise et résultat dans le même écran de partie | Écran de résultat distinct, révision de la table et bouton nouvelle manche; commit `fc02805` | Améliorer lisibilité et satisfaction; modification UI/navigation au-delà de la baseline |
| 14 oct. 2026 | Animation et packages du modèle | Affichage final immédiat; modèles à plat | Révélation progressive des cartes et `model/cards`, `game`, `betting`, `results`; commit `60dfb98` | UX et apprentissage du code; tests supplémentaires nécessaires |
| 14 oct. 2026 | Progression des mains après Split | Passage à la seconde main imparfait | Correction de `PlayerHands.finishActiveHand`, commit `6915b8d` | Bug bloquant constaté puis corrigé |
| 14 oct. 2026 | Mort du processus pendant une manche | Mise perdue mais manche non reprenable (DEC-11) | **Proposition en branche** `fix/blackjack-audit-20261014` : remboursement transactionnel et suppression de la manche abandonnée; commit `2921dbe` | Correction de cohérence solde/historique; non fusionné et non validé en émulateur |
| 14 oct. 2026 | Création d'un joueur | Aucun nom obligatoire au démarrage | **Demandée par l'étudiant, non présente dans `main` lors de l'audit** | Feature additionnelle; persistance du profil à concevoir et tester avant livraison |

**Interprétations conservées :** voir DEC-01 à DEC-13 dans `05 - Decisions.md`, notamment le choix de ne pas faire jouer le croupier si toutes les mains dépassent 21 (DEC-12). Le professeur peut demander une lecture littérale différente de l'énoncé.

**À faire avant remise :** refléter uniquement les validations réellement exécutées après les derniers commits, enregistrer la décision de fusion/abandon de la branche et actualiser Trello.
