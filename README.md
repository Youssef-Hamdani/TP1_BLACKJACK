# TP1 Blackjack Android

Application locale en Kotlin/Jetpack Compose pour le cours 420-C56-JO.

## Démarrer et compiler

1. Cloner le dépôt et l'ouvrir dans Android Studio.
2. Synchroniser Gradle avec le JDK/SDK Android compatibles.
3. Utiliser un émulateur Pixel 8, en portrait et en paysage.
4. Lancer la configuration `app`.

Vérification locale sous Windows (à exécuter après les changements récents) :

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

`local.properties` n'est pas versionné, car chaque installation a son chemin SDK.

## Architecture

- `model/cards` : valeurs, couleurs, cartes et sabot local.
- `model/game` : états, règles, tours du joueur et Split.
- `model/betting` et `model/results` : mises, paiements et résultats.
- `viewmodel` : état observable `StateFlow`, validation des entrées et coordination des écritures; profil local du joueur.
- `repository/GameRepository.kt` : accès transactionnel aux données, entre ViewModel et DAO.
- `database/dao` et `database/entity` : Room/SQLite, historique, journal des cartes et statistiques SQL.
- `ui/screens`, `ui/navigation`, `ui/scaffold`, `ui/components` : écrans Compose, navigation, FAB et composants.
- `app/schemas/` : export du schéma Room.
- `docs/` : Scrum et choix de conception.

Flux principal : Compose → ViewModel → Model pour les règles; ViewModel → Repository → DAO → Room/SQLite pour la persistance. Les statistiques Room remontent par Flow → RecordsViewModel/stateIn → Compose/collectAsStateWithLifecycle.

## Déroulement

Au premier lancement, un écran de création du joueur enregistre son nom dans les préférences locales de l'application (sans compte en ligne, sans modification du schéma Room et sans remise à zéro du solde existant).

Préparation (mise et sabot) → table de jeu → animation de révélation → résultat → nouvelle manche ou révision de la table. Historique et Statistiques sont accessibles dans la navigation principale.

Un seul processus possède la base Room. Les manches achevées, la mise par défaut, le solde et les statistiques persistent. **La manche en cours n'est pas reprise après mort du processus** : dans la branche d'audit, les mises des manches abandonnées sont remboursées au prochain démarrage et leurs cartes sont retirées des statistiques en transaction (voir DEC-14). Ce correctif doit être testé sur émulateur avant fusion.

## Validation et remise

Les campagnes de tests antérieures, observations Pixel 8 et limites sont consignées dans `docs/06 - Sprint Reviews.md`. Le code introduit par la branche d'audit n'a **pas encore** été recompilé ni exécuté en émulateur par l'assistant; voir la PR pour la checklist finale. Ne pas confondre tests existants et exécution récente.
