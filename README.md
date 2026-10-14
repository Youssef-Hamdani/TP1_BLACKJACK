# TP1 Blackjack Android

Projet du cours 420-C56-JO : Blackjack hors ligne en Kotlin avec Jetpack Compose, ViewModel/StateFlow, Navigation Compose, Room, Flow et coroutines.

## Ouvrir et exécuter

1. Ouvrir ce dossier dans Android Studio.
2. Utiliser un Pixel 8 virtuel ou physique.
3. Synchroniser Gradle, puis lancer la configuration `app`.

Validation en ligne de commande sous Windows :

```powershell
.\gradlew.bat testDebugUnitTest connectedDebugAndroidTest assembleDebug lintDebug
```

Le SDK Android reste une configuration locale : `local.properties` n'est volontairement pas versionné.

## Structure utile pour la présentation

- `model/` : règles Blackjack pures, sabot, mises, Split et calcul des résultats.
- `viewmodel/` : événements de l'interface, `StateFlow` et coordination des sauvegardes.
- `repository/GameRepository.kt` : frontière transactionnelle vers les DAO.
- `database/` : Room, entités, DAO et requêtes SQL réactives.
- `ui/` : écrans Compose, navigation et collecte lifecycle-aware.
- `app/schemas/` : schéma Room exporté.
- `docs/` : backlog approuvé, plan/revue de sprint, décisions et journal de portée.

Les règles ne sont pas calculées dans Compose et les écrans n'accèdent jamais directement aux DAO.

## État vérifié

La recette Pixel 8 couvre portrait/paysage, mises, Hit/Stand, Split, navigation et retour système, résultats, persistance Room, historique et statistiques réactives. Les résultats détaillés et limites sont consignés dans `docs/06 - Sprint Reviews.md`.
