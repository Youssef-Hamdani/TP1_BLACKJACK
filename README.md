# TP1 Blackjack Android

Application locale en Kotlin et Jetpack Compose pour le cours 420-C56-JO.

## Démarrer

1. Cloner le dépôt et l'ouvrir dans Android Studio.
2. Synchroniser Gradle avec le SDK Android et un JDK compatibles.
3. Lancer la configuration `app` sur un Pixel 8 en portrait et en paysage.

Vérification locale sous Windows :

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

Il faut relancer les tests après chaque changement important : la présence de fichiers de test ne prouve pas leur réussite sur le dernier commit.

## Architecture

- `model/cards` : cartes, rangs, couleurs et sabot de 52 × N cartes.
- `model/game` : état, actions, règles et Split.
- `model/betting` et `model/results` : mises, paiements, journal des tirages et résultats.
- `viewmodel` : `StateFlow`, validation, coordination des sauvegardes et nom du joueur.
- `repository` : transactions vers les DAO et stockage local du nom du joueur.
- `database` : Room, DAO, Entity et statistiques SQL.
- `ui` : Compose, Navigation, Scaffold/FAB, animations et écrans.

Au premier lancement, le joueur choisit un nom enregistré localement. Le capital initial reste de 1 000 jetons. Le parcours est préparation → partie → résultat → nouvelle manche ou revue de la table.

**Interruption :** le jeu en cours ne reprend pas après la mort du processus. Au démarrage suivant, les mises engagées sur les manches abandonnées sont remboursées une seule fois et les tirages de ces manches retirés des statistiques, dans une transaction Room.

**Documents Scrum :** l'énoncé exige le Product Backlog, les Sprints et les Sprint Reviews. Le dossier `docs/` a été retiré de `main` au commit `41c5f2e`. Ces documents doivent accompagner la remise séparément ou être réintégrés après confirmation.

Cette branche attend une compilation, les tests instrumentés sur Pixel 8 et la décision de fusion.
