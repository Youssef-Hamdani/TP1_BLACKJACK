# Architecture réelle du Blackjack

Les noms ci-dessous correspondent aux classes et fichiers du projet final. `BlackjackViewModelFactory` est une valeur Kotlin de type factory, pas une classe déclarée; son rectangle représente ce rôle. Les flèches « utilise » désignent une dépendance de code, pas un héritage.

```mermaid
classDiagram
    class MainActivity
    class BlackjackApplication
    class BlackjackApp
    class BlackjackNavHost
    class BlackjackScaffold
    class PreparationScreen
    class GameScreen
    class RoundResultScreen
    class GameViewModel
    class GameUiState
    class BlackjackViewModelFactory
    class BlackjackEngine
    class GameState
    class GameAction
    class GamePhase
    class PlayerHands
    class PlayerHandState
    class Hand
    class Card
    class Rank
    class Suit
    class CardSource {
        <<interface>>
        +draw() Card
        +prepareForNewRound() Boolean
    }
    class Shoe
    class Bet
    class PayoutCalculator
    class RoundResultResolver

    MainActivity --> BlackjackApp : affiche
    BlackjackApp --> BlackjackNavHost : configure
    BlackjackApp --> BlackjackScaffold : configure
    BlackjackApp --> GameViewModel : observe et envoie les actions
    BlackjackViewModelFactory ..> BlackjackApplication : obtient les repositories
    BlackjackViewModelFactory ..> GameViewModel : crée
    BlackjackNavHost --> PreparationScreen : destination
    BlackjackNavHost --> GameScreen : destination
    BlackjackNavHost --> RoundResultScreen : destination
    GameViewModel --> BlackjackEngine : applique les règles
    GameViewModel --> GameUiState : publie StateFlow
    BlackjackEngine --> GameState : détient
    BlackjackEngine --> CardSource : pige
    Shoe ..|> CardSource
    BlackjackEngine --> RoundResultResolver : résout
    BlackjackEngine --> PayoutCalculator : calcule le paiement
    GameState --> GamePhase : phase
    GameState --> GameAction : actions permises
    GameState --> PlayerHands : mains du joueur
    GameState --> Hand : main du croupier
    GameState --> Bet : mises engagées
    PlayerHands --> PlayerHandState : contient
    PlayerHandState --> Hand : contient
    Hand --> Card : contient
    Card --> Rank
    Card --> Suit
```

```mermaid
classDiagram
    class BlackjackApplication
    class BlackjackViewModelFactory
    class GameViewModel
    class RecordsViewModel
    class PlayerProfileViewModel
    class PlayerProfileRepository
    class GameRepository
    class AppDatabase
    class GameDao {
        <<interface>>
    }
    class CardDrawDao {
        <<interface>>
    }
    class WalletDao {
        <<interface>>
    }
    class GameEntity
    class CardDrawEntity
    class HandResultEntity
    class WalletEntity

    BlackjackApplication --> AppDatabase : une instance Room par processus
    BlackjackApplication --> GameRepository : partage
    BlackjackApplication --> PlayerProfileRepository : partage
    BlackjackViewModelFactory --> GameViewModel : crée
    BlackjackViewModelFactory --> RecordsViewModel : crée
    BlackjackViewModelFactory --> PlayerProfileViewModel : crée
    GameViewModel --> GameRepository : sauvegarde les actions
    RecordsViewModel --> GameRepository : observe les Flow
    PlayerProfileViewModel --> PlayerProfileRepository : nom local
    GameRepository --> AppDatabase : transactions
    AppDatabase --> GameDao
    AppDatabase --> CardDrawDao
    AppDatabase --> WalletDao
    GameDao --> GameEntity
    GameDao --> HandResultEntity
    CardDrawDao --> CardDrawEntity
    WalletDao --> WalletEntity
```

```mermaid
sequenceDiagram
    actor Joueur
    participant UI as PreparationScreen / BlackjackApp
    participant VM as GameViewModel
    participant M as BlackjackEngine
    participant R as GameRepository
    participant DB as DAO / Room
    Joueur->>UI: saisit 75, puis Commencer
    UI->>VM: onBetInputChanged("75")
    VM->>M: selectBet(75)
    VM-->>UI: StateFlow affiche 75, sans erreur
    UI->>VM: onGameAction(START_ROUND)
    VM->>M: startRound()
    M-->>VM: GameState (cartes et mise débitée)
    VM-->>UI: StateFlow GameUiState (table, sauvegarde en cours)
    VM->>R: saveRound(snapshot)
    R->>DB: transaction cartes, partie, portefeuille
    DB-->>R: succès
    R-->>VM: succès
    VM-->>UI: StateFlow (actions disponibles)
```

`MainActivity` installe Compose. `BlackjackApp` conserve les ViewModels partagés, collecte leurs `StateFlow` selon le cycle de vie et relie les destinations. Les écrans affichent `GameUiState` et transmettent les clics; ils ne décident pas qui gagne. `BlackjackScaffold` contient la barre supérieure, les onglets et le bouton Piger.

`GameViewModel` transforme la saisie en mise validée, appelle `BlackjackEngine`, puis demande au `GameRepository` d'enregistrer un instantané. `GameState` est la vérité des règles en mémoire; `GameUiState` est sa projection prête à afficher. Le moteur utilise `CardSource`, implémenté normalement par `Shoe`, et ne dépend ni de Compose ni de Room.

`GameRepository` est la frontière des données. Une action sauvegardée met à jour les cartes, la manche et le solde dans une transaction Room. Les DAO décrivent les requêtes SQLite. `CardDrawDao` calcule les statistiques des cartes réellement observées; Room émet un `Flow` après les changements. `RecordsViewModel` combine ces `Flow` avec `stateIn`, et Compose affiche le nouvel état.

La rotation recrée les Composables mais garde le ViewModel de la même Activity. Après une mort du processus, Room restitue le solde, la mise, l'historique et les cartes observées; la manche en cours est marquée interrompue et ses mises sont remboursées une seule fois. Le nom local est stocké séparément dans `PlayerProfileRepository` via SharedPreferences.
