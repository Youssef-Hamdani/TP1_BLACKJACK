# TP1 — Blackjack Android

Application Android locale en Kotlin et Jetpack Compose pour le cours **420-C56-JO**.

## Fonctionnalités

- Jouer une manche de Blackjack contre le croupier (Piger, Arrêter, Blackjack naturel, égalité).
- Miser des jetons et consulter le solde.
- Séparer une paire en deux mains avec **Split**.
- Choisir le nombre de paquets utilisés pour le sabot.
- Consulter l'historique des manches et les statistiques sur les valeurs des cartes observées.
- Conserver le nom du joueur, les préférences de mise et les données de jeu localement.

## Lancer le projet

1. Cloner ce dépôt et l'ouvrir dans Android Studio.
2. Synchroniser Gradle et installer le SDK Android demandé par le projet.
3. Démarrer la configuration `app` sur un émulateur compatible (par exemple Pixel 8).
4. Vérifier les écrans en portrait et en paysage.

Sous Windows, depuis la racine du projet :

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug assembleDebugAndroidTest
```

Pour **exécuter** les tests instrumentés Android, démarrer un émulateur ou brancher un appareil, puis lancer :

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

La tâche `assembleDebugAndroidTest` compile les tests instrumentés, mais ne les exécute pas.

## Organisation du code

| Dossier | Responsabilité |
| --- | --- |
| `model/cards` | Cartes, rangs, couleurs et sabot |
| `model/game` | État, phases, actions et règles du Blackjack |
| `model/betting`, `model/results` | Mises, gains, résultats et journal des cartes tirées |
| `viewmodel` | État affiché par les écrans et coordination des actions |
| `repository` | Accès aux données et transactions |
| `database` | Base Room, entités et requêtes DAO |
| `ui` | Écrans Jetpack Compose, navigation et présentation |

Une action suit généralement ce chemin :

```text
Écran Compose → ViewModel → Moteur Blackjack
                         → Repository → DAO → Room
```

L'interface observe ensuite les changements d'état du ViewModel. L'historique et les statistiques sont alimentés par des requêtes Room observées via Flow.

## Sauvegarde

Le nom du joueur est enregistré localement. Le portefeuille débute à **1 000 jetons** lors d'une première utilisation.

Les manches sont enregistrées au fil du jeu. La version actuelle ne reprend pas une manche interrompue après la mort du processus : lors du redémarrage, une opération Room rembourse les mises engagées une seule fois et retire les manches inachevées des résultats et statistiques.

## Suivi Scrum

Le [tableau Trello du projet](https://trello.com/b/cG4pTP0E/blackjack-android-420-c56-jo) contient le Product Backlog, les trois sprints et leurs revues. Les tâches non terminées restent indiquées dans le backlog.

Les tests doivent être relancés après chaque modification importante; leur présence dans le dépôt ne garantit pas qu'ils passent sur le dernier commit.
