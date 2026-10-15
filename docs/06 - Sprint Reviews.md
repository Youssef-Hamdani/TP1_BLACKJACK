# Sprint Reviews — faits vérifiables et limites

Révision documentaire du **14 octobre 2026** à partir du dépôt Git et des comptes rendus de tests préexistants. Les dates ci-dessous sont celles des sprints prévus; aucune réunion non observée n'est inventée. Les changements après la baseline figurent dans `09 - Scope Change Log.md`.

## Sprint 1 — 13 octobre (bilan rétrospectif)

**Objectif :** préparer le projet Android, l'architecture et une première manche jouable.

**Produit constatable dans le code remis :** une Activity, Scaffold/Navigation Compose, modèle de cartes et sabot, calcul d'As, moteur Hit/Stand et tests JVM. Ces éléments sont présents dans la base du dépôt; leur présence actuelle ne prouve pas qu'une revue Scrum autonome s'est réellement tenue le 13.

**Validation S1 indépendante :** aucune feuille de résultats datée et isolée à la clôture de S1 n'est disponible dans le dépôt. Ne pas attribuer les résultats globaux ultérieurs à une exécution S1 qui n'est pas documentée.

**Manque documentaire :** aucun compte rendu de réunion de Sprint Review S1 observé; ce bilan est reconstruit honnêtement après coup.

## Sprint 2 — 14 octobre (vérifications historiques)

**Objectif :** mises, jetons, Split, sabot, Room/DAO/Repository, historique, statistiques réactives, portrait/paysage.

**Travail constaté dans le code :** fonctionnalités BJ-15 à BJ-28 et BJ-30 à BJ-32, ainsi que tests associés. BJ-29 reste optionnel. L'état actuel de chaque carte reste à vérifier dans Trello.

**Résultats consignés dans les comptes rendus initiaux, antérieurs aux derniers correctifs :**
- BJ-25 à BJ-28 : 71 tests JVM et 24 tests Android annoncés réussis, APK compilé, lint 0 erreur / 12 avertissements.
- BJ-30 à BJ-31 : 23 tests JVM et 5 Android ciblés annoncés réussis; APK compilé.
- BJ-32/34 : scénario navigation et Split/Room en paysage instrumentés; échec initial du test Split sans défilement, corrigé dans le test, ensuite réussite.
- Recette Pixel 8 rapportée : portrait/paysage, solde et historique après force-stop, 10 valeurs statistiques, navigation retour, mises, Split.

Les preuves de ces exécutions se trouvent dans le compte rendu de travail original et/ou dans le vault local; tous les fichiers de capture cités initialement ne sont pas présents dans ce dépôt Git. Les tests ci-dessus ne valent **pas** vérification des commits d'animations et du correctif de redémarrage ajoutés ensuite.

## Sprint 3 — 15 octobre (préparation/état au 14 octobre)

**Objectif :** stabiliser, compiler, remettre dans Git, préparer les explications et effectuer une vraie répétition.

**Git vérifiable :** le dépôt [TP1_BLACKJACK](https://github.com/Youssef-Hamdani/TP1_BLACKJACK) a un remote accessible; `main` contient `29a8440` (version initiale), `fc02805` (résultats), `60dfb98` (animations/packages) et `6915b8d` (Split). La mention précédente « aucun remote/push » est périmée.

**Build initial connu :** `clean testDebugUnitTest assembleDebug` avait réussi avec 71 tests JVM au moment de `29a8440`, **pas** après toutes les modifications de navigation/animations.

**Branche d'audit en cours :** `fix/blackjack-audit-20261014` corrige la récupération d'une manche interrompue et la navigation de nouvelle manche; ses nouveaux tests restent à exécuter sur Pixel 8 avant fusion. Aucune nouvelle réussite de compilation n'est déclarée par anticipation.

**Points ouverts :** vérification réelle du build propre après derniers commits, tests d'interruption/navigation/rotation, décision sur le tour du croupier après dépassement, décision éventuelle sur la création du joueur, capture des preuves et répétition chronométrée par l'étudiant. BJ-39 ne peut pas être déclaré achevé tant que la répétition n'a pas eu lieu.

**Consigne de remise :** ne passer les cartes Trello à « Fini » qu'après vérification réelle. Les données et étapes non observées restent explicitement « non vérifiées ».
