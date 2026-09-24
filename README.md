# Isax — Launcher Android « SAO × Solo Leveling »

Launcher Android natif (Kotlin + Jetpack Compose) qui combine :
- **Gestes Sword Art Online** : glisser vers le bas (menu SAO Hub), glisser vers
  le haut (tableau de quêtes), tracé du « **S** » (fenêtre système).
- **HUD Solo Leveling** : « fiche de personnage » (HP = batterie, MP = RAM,
  AGI = CPU) + un vrai **système de quêtes** programmables et notifiées.
- **Fenêtres déplaçables** : lancement des apps en mode *freeform* (multi-window),
  dispositions côte à côte / mosaïque.
- **Hôte de widgets** : Isax accueille les widgets d'autres apps (AppWidgetHost)
  et fournit son propre widget « Quêtes ».
- **Compétences (Skills)** : packs ajoutables (`.isaxskill` / manifest JSON) pour
  renforcer le launcher.
- **Accès direct** aux réglages Android (apps installées, launcher par défaut,
  accès notifications, affichage, etc.).

Terminal intégré : binaire natif `minishell` compilé par le NDK/CMake.

---

## ⚠️ Ce que ce dépôt est — et n'est pas

C'est un **projet source complet et structuré** (code Kotlin + C, Gradle,
workflow CI). Il **ne contient pas d'APK** : la compilation se fait dans le
cloud (GitHub Actions) ou sur ta machine, jamais dans cet environnement.
Les briques systèmes qui dépendent du matériel/OS (freeform réel, exécution du
binaire natif) s'exécutent correctement au runtime mais ne peuvent pas être
testées ici.

---

## Compiler l'APK **depuis le téléphone, sans ordinateur** (GitHub Actions)

1. Crée un dépôt **vide** sur GitHub (ex. `isax-launcher`).
2. Depuis **Termux** :

```bash
pkg install git gh -y
gh auth login                       # choisit HTTPS + token
cd /sdcard/Download                 # là où tu as mis le zip
unzip isax-launcher.zip -d isax && cd isax
git init && git add . && git commit -m "Isax v0.2"
git branch -M main
git remote add origin https://github.com/<TON_USER>/isax-launcher.git
git push -u origin main             # déclenche le workflow automatiquement
```

3. Suivre / relancer le build et récupérer l'APK :

```bash
gh run list --workflow=build-apk.yml
gh run watch                        # suivi en direct
gh workflow run build-apk.yml           # relance manuelle (workflow_dispatch)
gh run download -n Isax-debug-apk         # télécharge l'APK dans ./isax-apk
termux-open ./Isax-debug-apk/Isax-debug.apk   # lance l'installeur Android
```

4. **Définir Isax comme launcher par défaut** :
   `Paramètres → Applications → Applications par défaut → Accueil → Isax`.

> Le workflow (`.github/workflows/build.yml`) est déjà présent : il installe
> JDK 17, le SDK Android, le NDK 26.1 et CMake 3.22, compile `assembleDebug`
> puis publie l'APK comme artefact téléchargeable (`isax-apk`).

---

## Alternative : compiler sur PC (Flutter installé mais ici on est en Kotlin)

Android Studio (Hedgehog+) → ouvre le dossier `isax/`.
SDK Manager → coche **NDK** + **CMake**.
Puis :

```bash
./gradlew assembleDebug     # ou gradle assembleDebug si le wrapper n'est pas généré
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Pourquoi Kotlin plutôt que Flutter ou HTML→APK ?

Un launcher doit **être l'écran d'accueil** (`HOME` + `LAUNCHER`), **héberger des
widgets** (`AppWidgetHost`) et **lancer des apps en fenêtres** (`ActivityOptions
.setLaunchBounds`) : ce sont des API natives que Flutter ne peut atteindre qu'au
prix de ponts fragiles, et qu'un simple HTML→APK ne peut pas exposer du tout.
Kotlin **est** la plateforme Android : accès direct, performances, zéro couche
intermédiaire. Flutter reste excellent pour un portage iOS futur ; on l'écarte
ici pour la fidélité à l'OS. Détails : `docs/ARCHITECTURE.md`.

## Arborescence

```
isax/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/isax/launcher/
│   │   ├── MainActivity.kt / SettingsActivity.kt / IsaxApplication.kt
│   │   ├── core/       Prefs, chemins, JSON, BootReceiver
│   │   ├── gesture/    SGestureDetector (swipe + « S »)
│   │   ├── home/       AppRepository (apps installées)
│   │   ├── window/     FreeformLauncher, WindowSession
│   │   ├── quest/      Quest, Repository, Scheduler, PomodoroService
│   │   ├── system/     Stats, NotificationListener, DeviceSettings
│   │   ├── skills/     IsaxSkill, Registry, Installer + builtin/
│   │   ├── widget/     IsaxWidgetProvider, WidgetHostController
│   │   └── ui/         HomeScreen, overlays (Hub, Status, Terminal, Files…)
│   ├── res/            icône, thème, layout widget
│   └── cpp/            minishell.c, minigit.c, editor.c (CMakeLists.txt)
└── .github/workflows/build.yml
```
