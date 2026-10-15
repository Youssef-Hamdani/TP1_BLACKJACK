# Plan de sprints — approuvé le 2026-10-13

## S1 · Fondations · mardi 13 octobre
**Objectif** : première version jouable et correctement structurée. 14 items BJ-01..14, **11 h estimées**. Produit visé : une Activity, Compose/Scaffold/Navigation, ViewModel/StateFlow, modèles, sabot et règles Hit/Stand, visuels de cartes, tests moteur et revue. Room non exigé dans ce sprint.

**Condition de sortie** : le programme compile réellement et exécute une manche simple, actions invalides indisponibles; résultats de test consignés. Aucun Sprint Review rempli avant d'avoir des preuves.

## S2 · Intégration · mercredi 14 octobre
**Objectif** : règles complètes et données persistées. 19 items BJ-15..33, **14,75 h estimées**, dont BJ-29 améliorations conditionnelles (1 h). Mises et jetons, Split, sabot configurable, Room/DAO/Repository, journalisation de chaque carte et partie, Flow/StateFlow/stateIn, historique, statistiques minimales, portrait/paysage, vérifications et revue.

**Condition de sortie** : tests observés du Split, Room persistante, SQL comme vérité, navigation fonctionnelle, Pixel 8 et deux orientations.

## S3 · Stabilisation · jeudi 15 octobre AVANT 14 h 45
**Objectif** : zéro nouvelle fonctionnalité substantielle. 7 items BJ-34..40, **5 h estimées** dont 1 h réserve. Recette réelle Pixel 8, fixes ciblés, revue architecture, push Git complet, documents Scrum conformes au travail réalisé, répétition démo.

**Condition de sortie** : build et démo constatés, repo partagé, journal des erreurs honnête, présentateur peut justifier chaque couche.

## Décisions de gestion du temps
- **Lancement réel : mardi 13 octobre 2026**, aucune avance fictive.
- Les 30,75 h estimées dépassent vraisemblablement la disponibilité réaliste; le plan initial est volontairement transparent.
- Dépendances et ordre de la baseline priment. Ne pas démarrer l'intégration UI de Split avant son modèle, ni l'historique sans persistance.
- Toute réduction, déplacement de sprint ou ajustement d'estimation après le gel : [[09 - Scope Change Log]].

## Definition of Ready
Exigence et source connues; dépendances achevées ou remplacées par un stub documenté; critère testable; hypothèse explicitée; tâche assez ciblée pour lancer Codex sans choix implicite.

## Definition of Done
Code réellement créé; build et tests pertinents exécutés si environnement disponible; résultats précis (pass/fail/non vérifié); critères d'acceptation observés; fichiers/commits cités; revue de code comprise par étudiant; carte Trello déplacée dans Done **après** vérification.
