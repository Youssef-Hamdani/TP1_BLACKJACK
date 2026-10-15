# Scope Change Log — changements après la baseline v0.1

Baseline de 40 cartes / 30,75 h approuvée le 13 octobre 2026. Voir `docs/baseline/Blackjack_Backlog_PROPOSITION_v0.1.csv`.

Ce journal a été **reconstitué a posteriori** le 14 octobre à partir des demandes de l'étudiant et de commits Git existants; il ne prétend pas avoir été écrit avant chaque décision. Les statuts Trello restent la source de vérité des cartes.

| Date (Québec) | Sujet | Avant | Après / preuve | Justification et impact |
|---|---|---|---|---|
| 14 oct. 2026 | Présentation du résultat | Mise et résultat dans le même écran de partie | Écran de résultat distinct, révision de la table et bouton nouvelle manche; commit `fc02805` | Améliorer lisibilité et satisfaction; modification UI/navigation au-delà de la baseline |
| 14 oct. 2026 | Animation et packages du modèle | Affichage final immédiat; modèles à plat | Révélation progressive des cartes et `model/cards`, `game`, `betting`, `results`; commit `60dfb98` | UX et apprentissage du code; tests supplémentaires nécessaires |
| 14 oct. 2026 | Progression des mains après Split | Passage à la seconde main imparfait | Correction de `PlayerHands.finishActiveHand`, commit `6915b8d` | Bug bloquant constaté puis corrigé |
| 14 oct. 2026 | Mort du processus pendant une manche | Mise perdue mais manche non reprenable (DEC-11) | `main` intègre le remboursement transactionnel et la suppression de la manche abandonnée; commit d'origine `2921dbe` | Correction de cohérence solde/historique, ensuite révisée le 15 octobre |
| 14 oct. 2026 | Création d'un joueur | Aucun nom obligatoire au démarrage | Présente dans `main` via `PlayerCreationScreen` et SharedPreferences | Fonction additionnelle; capital initial fixe à 1 000 jetons |
| 15 oct. 2026 | Manches interrompues | La récupération supprimait leurs cartes | Branche finale : base v2, état abandonné et cartes observées conservées | Les statistiques doivent représenter ce que le joueur a réellement vu; remboursement toujours unique |
| 15 oct. 2026 | Deux mains dépassent 21 | Le croupier ne jouait pas (DEC-12 d'origine) | Branche finale : révélation et tirages sous 17 | Conforme au déroulement explicitement décrit dans l'énoncé |
| 15 oct. 2026 | Solde à zéro | Aucun redémarrage possible dans l'interface | Réinitialisation confirmée à 1 000 jetons, historique conservé | Permet de poursuivre la démonstration sans effacer les données |

**Interprétations conservées :** voir `05 - Decisions.md`. DEC-12 et DEC-14 ont été révisées explicitement; la baseline approuvée reste intacte.

**À faire avant remise :** ne déclarer que les tests réellement exécutés sur la branche finale et actualiser Trello séparément.
