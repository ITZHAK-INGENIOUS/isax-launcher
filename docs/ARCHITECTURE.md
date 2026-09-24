# Isax — Architecture & choix techniques

## 1. Framework : Kotlin vs Flutter vs Java vs HTML→APK

| Besoin launcher | Kotlin natif | Flutter | HTML→APK (WebView) |
|---|---|---|---|
| Être l'écran d'accueil (`HOME`/`LAUNCHER`) | Oui | Oui (fragile) | Non |
| Lister **toutes** les apps (`QUERY_ALL_PACKAGES`) | Oui | Pont nécessaire | Non |
| **Héberger** les widgets d'autres apps (`AppWidgetHost`) | Oui (natif) | Impossible en Flutter pur | Non |
| Lancer des apps en **fenêtres** (`ActivityOptions`) | Oui | Pont nécessaire | Non |
| Lire le flux de **notifications** (`NotificationListenerService`) | Oui | Pont nécessaire | Non |
| Terminal natif (NDK/JNI) | Oui | Pont FFI | Non |
| Poids du binaire / latence | Minimal | Moteur embarqué | WebView |

**Décision** : Kotlin natif + Jetpack Compose. C'est la seule voie qui couvre
100 % des fonctionnalités demandées sans couche intermédiaire. Java conviendrait
techniquement mais Compose (déclaratif) accélère l'UI « néon » et la gestion
d'état. Flutter est réservé à un éventuel portage multiplateforme ultérieur.

## 2. Gestes (correctif v0.2)

Le scaffold v0.1 empilait **deux** `pointerInput` sur la même `Box`
(`detectVerticalDragGestures` **et** `detectDragGestures`). Le premier consommait
l'événement → le détecteur de « S » ne recevait jamais rien : **c'était la cause
racine** du « ça ne marche pas ». v0.2 n'a **qu'un seul** `pointerInput`, qui
alimente un accumulateur unique ; la classification (SWIPE_UP / SWIPE_DOWN / S /
TAP) se fait au relâchement dans `SGestureDetector.classify()`.

## 3. Fenêtres déplaçables (limite honnête)

Sans root, un launcher **ne peut pas** afficher le contenu d'une autre app dans
sa propre hiérarchie de vues (réservé au système / à l'`ActivityManager`). La
voie officielle est le **multi-window freeform** : `ActivityOptions.makeBasic()
.setLaunchBounds(rect)` + `FLAG_ACTIVITY_LAUNCH_ADJACENT`. Ces appels ne
prennent effet que si le système autorise le freeform (sur beaucoup d'appareils :
Options développeur → « Forcer le redimensionnement des activités »). Sinon,
l'app s'ouvre en plein écran — **sans crash**. Isax gère donc la géométrie, la
mosaïque, et déclenche le lancement ; l'affichage simultané réel est décidé par
Android. Deux apps côte à côte fonctionnent nativement (split-screen système).

## 4. Quêtes (Solo Leveling)

`QuestRepository` (StateFlow + persistance JSON) est la source de vérité.
`QuestScheduler` programme via `AlarmManager.setExactAndAllowWhileIdle`
(reprogrammé au boot par `BootReceiver`). `QuestAlarmReceiver` notifie et
reprogramme les quêtes quotidiennes. `PomodoroService` = donjon chronométré en
service de premier plan.

## 5. Compétences (Skills)

`IsaxSkill` définit un contrat (id, catégorie, `@Composable Render`).
- **Embarquées** : ClockSkill, SystemPulseSkill.
- **Installées** : pack `.isaxskill` (zip + manifest.json) ou JSON collé.
**Sécurité** : on n'exécute **pas** de code tiers non signé (pas de
`DexClassLoader` automatique) — les packs installés sont **déclaratifs**
(thème, icônes, métadonnées). Un store signé pourra être ajouté plus tard.

## 6. Accès réglages

`DeviceSettings` centralise les Intents officielles (apps installées, launcher
par défaut, accès notifications, usage stats, affichage, batterie, etc.),
ouvertes depuis l'écran Réglages.
