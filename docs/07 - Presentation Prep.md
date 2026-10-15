# Préparation de la démonstration — plan, pas résultats

## Script BJ-39 — 7 minutes, fondé sur la recette réelle

Ce minutage est le scénario à répéter par l'étudiant; la recette technique sous-jacente a été exécutée sur Pixel 8, mais le passage oral humain reste à chronométrer une fois avant la présentation.

| Temps | Action de démonstration | Explication à donner |
|---|---|---|
| 0:00–0:40 | Lancer en portrait, montrer Jouer/Historique/Statistiques | Une seule Activity; Navigation Compose change la destination, pas le ViewModel de partie. |
| 0:40–1:30 | Choisir mise 75 et nombre de paquets, commencer | Compose envoie l'événement; le ViewModel délègue au moteur. La mise 1–100 et le changement de sabot entre manches sont validés par le Model. |
| 1:30–2:30 | Piger puis Arrêter; commenter carte cachée et résultat | `BlackjackEngine` produit un nouveau `GameState`; le dealer révèle/joue, `PayoutCalculator` règle une seule fois. |
| 2:30–3:20 | Montrer ou expliquer Split avec le scénario testé 8/8 | Split débite une seconde mise, crée deux mains jouées successivement et garde un seul dealer; un 21 issu du Split n'est pas un naturel. |
| 3:20–4:05 | Naviguer pendant une manche puis Retour | Les mêmes cartes et le même solde restent : les ViewModels vivent au-dessus des destinations du NavHost. |
| 4:05–4:50 | Ouvrir Historique puis Statistiques | Room est la vérité persistée. Les requêtes DAO retournent des `Flow`; `RecordsViewModel.stateIn` publie et Compose collecte selon le lifecycle. |
| 4:50–5:20 | Tourner en paysage et faire défiler | Les écrans et commandes restent accessibles; Pixel 8 portrait/paysage a été inspecté. |
| 5:20–6:30 | Ouvrir les fichiers ci-dessous et tracer un clic | Expliquer clairement les responsabilités et la transaction Room. |
| 6:30–7:00 | Résumer vérification et limites | Tests JVM/Android, `force-stop` vérifié; BJ-29 optionnel non fait et dépôt distant à confirmer au moment de la remise. |

### Sept repères de code à savoir expliquer

1. `ui/screens/GameScreen.kt` — affiche uniquement `GameUiState`; montre les actions présentes dans `availableActions`.
2. `ui/BlackjackApp.kt` — collecte les deux `StateFlow` avec `collectAsStateWithLifecycle` et raccorde navigation/UI.
3. `viewmodel/GameViewModel.kt` — reçoit les événements, appelle le moteur, publie l'état et sauvegarde un snapshot sans rejouer l'action.
4. `model/BlackjackEngine.kt` — règles Hit/Stand/Split, phases, dealer et transitions; pur Kotlin.
5. `repository/GameRepository.kt` — seul point d'accès applicatif aux DAO; une action/règlement est transactionnel et idempotent.
6. `database/dao/CardDrawDao.kt` — SQL des cartes observées et statistiques réactives; `GameDao`/`WalletDao` complètent la persistance.
7. `viewmodel/RecordsViewModel.kt` — combine les `Flow` Room, les transforme en état partageable avec `stateIn`.

### Trace à réciter

Arrêter → `GameScreen` appelle `GameViewModel.onGameAction(STAND)` → `BlackjackEngine.stand()` révèle et joue le dealer → le ViewModel publie le nouvel état et demande `GameRepository.saveRound()` → transaction Repository/DAO écrit cartes, résultats et solde dans Room → les `Flow` historique/statistiques réémettent → `RecordsViewModel` met son `StateFlow` à jour → Compose recompose les écrans.

### Réponses courtes probables

- **Pourquoi pas les règles dans Compose ?** Compose peut être recréé souvent; il doit rendre un état, pas devenir la source de vérité. Le Model reste testable sans Android.
- **Pourquoi le ViewModel ne requête pas les DAO ?** Le Repository masque le schéma et garantit les transactions; le ViewModel coordonne l'UI et les coroutines.
- **Flow vs StateFlow ?** Room émet un `Flow` quand SQL est invalidé; le ViewModel le transforme en `StateFlow` possédant toujours une valeur UI courante.
- **Rotation vs mort du processus ?** Le ViewModel conserve la manche pendant une recréation d'écran; Room conserve solde, mise, historique et statistiques après `force-stop`. Une manche interrompue n'est volontairement pas reprise.
- **Pourquoi la carte cachée n'est-elle pas comptée ?** Elle n'est observée qu'à sa révélation; la requête SQL filtre `isObserved`, y compris dans le dénominateur.
- **Que garantit la transaction ?** Si une écriture échoue, résultats, cartes et solde restent cohérents; aucun état partiellement réglé n'est visible.

**Date** : jeudi 15 octobre 2026 à partir de 14 h 45.

## Démo projetée (à adapter au comportement réellement vérifié)
1. Ouvrir l'app Pixel 8 en portrait; montrer navigation Jouer / Historique / Statistiques.
2. Sélectionner une mise et afficher le solde; jouer Hit/Stand et expliquer As et croupier.
3. Si réellement implémenté, provoquer Split sur deux cartes de même rang et montrer deux mains.
4. Montrer cartes graphiques, état des actions impossibles et FAB contextuel.
5. Consulter historique/statistiques; fermer puis relancer pour vérifier persistance.
6. Basculer en paysage et vérifier accessibilité.
7. Ouvrir fichiers réels pour expliquer Compose → VM → Repository → DAO → Room et un Flow Room.

## Fiches d'explication — état réel après BJ-24
| Composant | Fichier réel | Responsabilité | Ce qui casserait s'il était ailleurs | Test réel |
|---|---|---|---|---|
| Compose/Scaffold/NavHost | `ui/BlackjackApp.kt`, `ui/scaffold/BlackjackScaffold.kt`, `ui/navigation/BlackjackNavHost.kt`, `ui/screens/GameScreen.kt` | Afficher table, mains et actions contextuelles; naviguer dans une Activity | UI et back stack couplés aux règles | `BlackjackNavigationTest`, `GameFlowTest` Pixel 8 |
| ViewModel/StateFlow | `viewmodel/GameViewModel.kt`, `viewmodel/GameUiState.kt` | Déléguer jeu/configuration au moteur et projeter un état immuable | UI muterait l'état; instances divergentes | 6 tests `GameViewModelTest`, dont Split/sabot |
| Blackjack Model | `model/game/Hand.kt`, `model/cards/Shoe.kt`, `model/game/GameState.kt`, `model/game/BlackjackEngine.kt`, `model/results/RoundResultResolver.kt` | Scores/As, sabot, phases, actions, dealer et résultats | règles dupliquées dans UI/VM | tests Model inclus dans les 65 tests JVM réussis |
| Split | `model/game/GameAction.kt`, `model/game/GameState.kt`, `model/game/BlackjackEngine.kt`, `ui/screens/GameScreen.kt` | Valider paire/fonds dans Model; afficher action et main active dans Compose | règles copiées dans l'écran ou deux dealers concurrents | 12 tests moteur + parcours Compose Pixel 8 |
| Nombre de paquets | `model/cards/Shoe.kt`, `model/game/GameState.kt`, `model/game/BlackjackEngine.kt`; projection VM; sélecteur dans `ui/screens/PreparationScreen.kt` | Construire 52×N cartes et permettre changement 1/2/4/6 entre manches | changement en cours de manche invaliderait le sabot | tests Shoe/moteur/VM + parcours Compose Pixel 8 |
| Mise et jetons | `model/betting/Bet.kt`, `model/betting/PayoutCalculator.kt`; état dans `model/game/GameState.kt`; projection dans `GameUiState` | Valider 1–100, engager la mise, payer une seule fois et publier le solde | paiement dans Compose/VM serait duplicable et difficile à tester | `BetTest`, `PayoutCalculatorTest`, `BlackjackEngineBalanceTest`, `GameFlowTest` |
| Cartes graphiques | `ui/components/CardView.kt`, `TableSurface.kt`; `res/drawable/card_back.xml`; `res/drawable-nodpi/blackjack_table.png` | Représenter faces/dos et table sans réseau | cartes textuelles seules, carte cachée révélée | `CardViewTest`; portrait/paysage inspectés |
| Repository | `repository/GameRepository.kt` | Frontière unique vers les DAO; conversions et transaction de règlement | VM dépendrait du schéma SQL; résultats/solde pourraient diverger | `GameRepositoryTest` : rollback, retry, historique |
| DAO et requêtes SQL | `database/dao/GameDao.kt`, `CardDrawDao.kt`, `WalletDao.kt` | Écritures suspend, lectures Flow, comptes/proportions SQL | statistiques officielles dupliquées en mémoire | `GameRepositoryTest` : valeurs/visibilité et émissions |
| Room/AppDatabase | `database/AppDatabase.kt`, `database/entity/*`, `BlackjackApplication.kt` | Un fichier SQLite partagé, clés/relations | données perdues ou instances divergentes | fermeture/réouverture fichier + singleton Repository |
| Flow/stateIn/lifecycle | `viewmodel/RecordsViewModel.kt`, `ui/BlackjackApp.kt` | Partager le dernier état observé et collecter selon lifecycle | affichage périmé ou abonnements inutiles | `RecordsFlowTest` : compteurs visibles suivent insertion/révélation/règlement |

## Traces réelles BJ-20–24

Dans `RecordsFlowTest`, le test insère via le Repository un 10 visible et un 7 caché. Room exécute la requête filtrée → Flow contient un seul compte → `RecordsViewModel.combine` → `stateIn` → collecte lifecycle → Compose affiche « 1 carte observée ». `revealCard` met ensuite `isObserved` à vrai sur la même ligne : la requête est invalidée puis réexécutée; le compteur devient 2 sans bouton Actualiser. Ce n'est pas encore le chemin automatique d'un Hit réel (BJ-25).

Saisie « 75 » → `GameViewModel.onBetInputChanged` → validation `Bet`/mise moteur → coroutine `viewModelScope` → `Repository.setDefaultBet` → UPDATE de la colonne mise. Au prochain ViewModel de production, `loadWallet` précède l'activation de Jouer. Le test prouve le rechargement; une sauvegarde en cours n'est pas garantie si le processus est tué avant la fin de l'écriture.

Une transaction est un « tout ou rien » : si sauvegarder le solde échoue après avoir inséré les deux résultats de Split, les résultats et la fin de manche sont annulés aussi. Test réel avec déclencheur SQL d'échec, puis nouvelle tentative réussie. Une tentative après succès ne crédite rien de nouveau.

`suspend` autorise l'attente sans bloquer le thread UI; Room gère son travail DB. `Flow` réémet après modification; `stateIn` conserve et partage le dernier état dans la durée de vie du ViewModel. `WhileSubscribed(5000)` coupe l'amont après 5 secondes sans abonné. La collecte Compose s'arrête lorsque le lifecycle n'est plus actif.

Validation finale : 67 tests JVM, 18 Android Pixel 8, build réussi, lint 0 erreur/12 avertissements. Les 11 nouveaux tests Android couvrent Repository (9) et flux/VM (2). Pas encore de démonstration de mort du processus avec une vraie manche; solde du gameplay et journalisation restent BJ-25/BJ-26, vues détaillées BJ-27/BJ-28.

Questions : pourquoi le dénominateur SQL doit-il exclure lui aussi la carte cachée ? Que pourrait afficher l'historique si résultats et solde étaient écrits dans deux transactions indépendantes ?

## Traces réelles BJ-18/BJ-19
`availableActions` contient `SPLIT` → Compose affiche Séparer → clic → `onGameAction(SPLIT)` → moteur → snapshot `GameState` → projection `GameUiState` → `StateFlow` → recomposition avec deux mains et une seule marquée « À jouer ».

`clic 4 paquets` → `onDeckCountSelected(4)` → `engine.selectDeckCount(4)` → nouveau `Shoe(4)` de 208 cartes → état publié → bouton 4 sélectionné. Pendant `PLAYER_TURN`, le sélecteur n'est pas offert et le moteur refuserait quand même l'appel.

Exemple vérifié : solde 1 000, mise 50, paire de 8. Start débite 50 (950); Split débite 50 (900) et donne 8+10=18 puis 8+9=17, face au même dealer 10+7=17. Le premier Stand active la seconde main sans révéler le dealer. Le second déclenche le dealer, puis une victoire et une égalité créditent 100+50 : solde final 1 050.

`copy` construit un nouveau snapshot sans modifier les anciens; le ViewModel publie ce snapshot. `isFromSplit` conserve l'origine de la main : As+Roi après Split vaut 21, mais ne reçoit pas le bonus naturel. Les règles de disponibilité restent dans le Model, pas dans Compose.

Questions BJ-18/BJ-19 : pourquoi Compose consulte-t-il `availableActions` au lieu de comparer les deux rangs ? Que casserait le remplacement du sabot au milieu d'une manche ?

## Questions courtes pour s'exercer (ne pas inventer les réponses)
- Pourquoi le composable ne doit-il pas décider si un Hit est légal ?
- Qu'est-ce qu'une `MutableStateFlow`, et pourquoi l'UI n'y écrit-elle pas ?
- Pourquoi Room retourne-t-il un `Flow` pour des comptes statistiques ?
- Où se trouve le code des As et de Split, et comment est-il testé ?
- Quel chemin prend une carte nouvellement tirée jusqu'à son affichage dans les statistiques ?
- Qu'est-ce qui empêche le rechargement de l'écran de rejouer la distribution ?
- Quelles décisions de règles sont des hypothèses plutôt que des consignes du PDF ?

> Ne pas déclarer un scénario « testé » tant que des commandes, captures ou observations n'ont pas été consignées.


## Mise à jour après audit du 14 octobre (non remplacée par des tests)

Le jeu navigue désormais Préparation → Table → Résultat, avec une page facultative « Revoir la table ». L'animation retarde l'affichage du résultat, mais le moteur et la transaction Room calculent/sauvegardent d'abord la manche.

**Nouveau cas à justifier après validation de la PR :** une manche interrompue par mort du processus n'est pas reprise; la branche d'audit rembourse les mises engagées une seule fois dans une transaction Room et efface les cartes d'une manche abandonnée. Expliquer clairement ce compromis et vérifier physiquement sur Pixel 8 avant la présentation.

**Attention :** les précédents chemins `model/*.kt` ne sont plus valides; se référer à `model/cards`, `model/game`, `model/betting` et `model/results`. Les anciens nombres de tests dans cette fiche sont historiques et ne prouvent pas la validation des derniers commits.
