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

## Sprint 3 — 15 octobre (bilan technique, sans réunion simulée)

**Objectif :** stabiliser, compiler, remettre dans Git, préparer les explications et effectuer une vraie répétition.

**Git vérifiable :** `main` est à `b1ff9e6` au début de ce correctif; la branche de finalisation est `fix/blackjack-final-polish`. La baseline du backlog n'a pas été modifiée.

**Vérification finale exécutée le 15 octobre :** `testDebugUnitTest assembleDebug lintDebug assembleDebugAndroidTest connectedDebugAndroidTest` a réussi après correction des attentes de tests et de la navigation. Rapports Gradle : 73 tests JVM réussis avant l'intégration du dernier `main` et 35 tests instrumentés réussis sur Pixel_8(AVD); lint : 0 erreur, 24 avertissements. Après cette intégration, `testDebugUnitTest assembleDebug` a réussi avec 72 tests JVM : `main` avait retiré un exemple de test arithmétique sans lien avec Blackjack. `assembleDebugAndroidTest` a compilé l'APK de test; `connectedDebugAndroidTest` l'a réellement exécuté.

**Travail observable :** retour Back sans préparation modifiable pendant une manche, saisie de mise conservée et resynchronisée pour la prochaine manche, retry Room sans rejouer Hit, tour du croupier conforme au PDF même après deux dépassements, résultats Split expliqués correctement, migration Room v1→v2 et remboursement unique des manches interrompues en conservant leurs cartes observées. Les tests de migration, navigation, Split, paysage, persistance et retry sont inclus dans les 35 tests exécutés.

**Points ouverts :** répétition orale chronométrée par l'étudiant et éventuelle capture de démonstration. Les tests d'émulateur vérifient les scénarios automatisés, pas chaque détail visuel observé à l'œil. BJ-39 ne peut pas être déclaré achevé par ce seul bilan technique.

**Consigne de remise :** ne passer les cartes Trello à « Fini » qu'après vérification réelle. Les données et étapes non observées restent explicitement « non vérifiées ».
