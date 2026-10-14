# Décisions de conception

Le professeur ne définit pas tous les détails du Blackjack. DEC-01 à DEC-07 font partie de la proposition v0.1; DEC-08 à DEC-13 ont été approuvées par l'étudiant le 13 octobre 2026 après le gel. Ces choix **ne sont pas attribués au PDF**.

| ID | Décision / hypothèse approuvée | Raison | Références liées |
|---|---|---|---|
| DEC-01 | Débiter la mise au lancement; reverser capital et gains à la fin de sorte que le **delta net** = +mise victoire, −mise défaite, 0 push, +2×mise naturel | Cohérence avec p. 4 sans double débit | BJ-15, BJ-16 |
| DEC-02 | Split exige 2 cartes initiales du même rang, une mise supplémentaire égale à l'initiale, fonds suffisants; aucune reséparation en v0.1 | Clarifier l'ambiguïté financière et réduire complexité | BJ-17, BJ-18 |
| DEC-03 | Une main de Split atteignant 21 n'est pas un Blackjack naturel majoré | Règle choisie, PDF silencieux | BJ-17 |
| DEC-04 | Croupier reste au **soft 17**, puisque le texte impose arrêt à 17 ou plus | Lecture littérale de p. 5 | BJ-09 |
| DEC-05 | Proposer 1, 2, 4 ou 6 paquets et modifier le sabot **entre** manches | UI simple; valeurs non imposées | BJ-19 |
| DEC-06 | Toute carte distribuée est enregistrée en DB, même cachée; conserver information de visibilité/révélation pour distinguer les cartes « observées » selon interprétation | PDF demande log de toute carte et statistiques de cartes observées | BJ-25, BJ-28 |
| DEC-07 | Persistance requise pour historique/cartes/stats. Reprise d'une manche en cours après mort du processus non garantie | Ne pas inventer exigence | BJ-20..28 |
| DEC-08 | Pour les statistiques « par valeur », regrouper `10`, Valet, Dame et Roi sous la valeur Blackjack **10**; afficher l'As comme une catégorie distincte | Reflète la valeur utilisée par le jeu et évite quatre catégories équivalentes | BJ-22, BJ-28 |
| DEC-09 | Une carte cachée du croupier est enregistrée dès sa distribution, mais compte comme « observée » seulement après sa révélation | Distingue fidèlement carte pigée et information réellement vue par le joueur | BJ-25, BJ-28 |
| DEC-10 | Recréer et mélanger le sabot **avant** une nouvelle manche lorsqu'il reste moins de 20 cartes; ne jamais remplacer le sabot au milieu d'une manche | Seuil simple et prudent, sans réintroduire une carte pendant une manche | BJ-07, BJ-19 |
| DEC-11 | Le solde de jetons et la mise par défaut survivent à la mort du processus via Room; une manche interrompue n'est pas reprise | Expérience cohérente sans ajouter la complexité d'une sauvegarde complète de manche | BJ-15, BJ-16, BJ-20, BJ-26 |
| DEC-12 | Si toutes les mains du joueur dépassent 21, le croupier ne révèle pas sa carte cachée et ne pige pas; la manche se termine immédiatement | Évite un tour sans effet et garde cohérente la définition de carte « observée » | BJ-09, BJ-17, BJ-25 |
| DEC-13 | Direction visuelle : table de casino originale vue du dessus, feutre vert réaliste avec légère profondeur; ne pas reproduire de mentions incompatibles comme assurance ou paiement 3:2 | Suit la référence visuelle approuvée sans introduire de règles absentes du devoir | BJ-11, BJ-12, BJ-30 |

## Points précisés après le gel
Les ambiguïtés recensées pendant la planification ont été tranchées par l'étudiant le 13 octobre 2026 dans DEC-08 à DEC-13. Ces choix clarifient l'implémentation sans modifier les cartes, estimations ou sprints de la baseline.

**Révision de décision après baseline** : consigner dans [[09 - Scope Change Log]] si impacte périmètre, estimations, sprint ou critères; ne pas réécrire les hypothèses comme si elles avaient toujours été implicites.
