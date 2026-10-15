# Préparation de la démonstration

Ce plan est une aide à la présentation, pas le compte rendu d'une répétition. Le passage oral reste à faire par l'étudiant.

## Parcours court

1. Créer le joueur si nécessaire. Montrer la mise (1 à 100), le solde initial de 1 000 jetons et le choix 1/2/4/6 paquets.
2. Commencer, Piger, Arrêter. Montrer l'ordre des cartes, la carte cachée et le tirage du croupier sous 17.
3. Expliquer Split avec une paire de même rang : deuxième mise, main 1 puis main 2, deux résultats contre le même croupier. Une main à 21 issue du Split n'est pas un naturel.
4. Ouvrir Résultat, Revoir la table, Nouvelle manche, Historique et Statistiques. Revenir à la partie active depuis un onglet sans perdre les cartes.
5. Tourner le Pixel 8 en paysage. Si possible, fermer puis relancer après une manche sauvegardée pour montrer Room.

## Fichiers à savoir retrouver

- `ui/BlackjackApp.kt`, `ui/navigation/BlackjackNavHost.kt`, `ui/scaffold/BlackjackScaffold.kt` : ViewModels partagés, destinations, barre et action principale.
- `viewmodel/GameViewModel.kt`, `viewmodel/GameUiState.kt` : événements, validation, StateFlow et sauvegarde asynchrone.
- `model/game/BlackjackEngine.kt`, `PlayerHands.kt`, `Hand.kt` : progression des mains, tour du croupier, As et règles.
- `model/cards/Shoe.kt` : paquet local mélangé.
- `repository/GameRepository.kt`, `database/dao/*` : transactions Room et requête SQL des cartes observées.
- `viewmodel/RecordsViewModel.kt` : Flow Room transformés en état affichable.
- `docs/architecture.md` : diagrammes des dépendances réelles.

## Trace à expliquer

Piger → Compose appelle `GameViewModel.onGameAction(HIT)` → le moteur ajoute une carte à la main active et publie un nouveau `GameState` → le ViewModel construit `GameUiState` et demande au Repository d'enregistrer l'instantané dans Room → l'animation Compose révèle la carte → après la sauvegarde, le ViewModel réactive les actions légales. La requête SQL sur les cartes observées réémet son `Flow`, puis l'écran Statistiques se met à jour.

## Limites honnêtes

Une manche active survit à la rotation grâce au ViewModel, mais pas à la mort du processus. Au redémarrage, le Repository marque cette manche interrompue et rembourse les mises; les cartes déjà observées restent comptées. Les tests compilés ne prouvent pas à eux seuls qu'un émulateur a exécuté les tests instrumentés. La répétition orale doit encore être chronométrée par l'étudiant.
