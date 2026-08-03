<div align="center">

  <img src="app/src/main/res/drawable/img_remx_app_icon_1785717561059.jpg" alt="RemX Logo" width="140" height="140" style="border-radius: 28px; box-shadow: 0 10px 25px rgba(0,0,0,0.3);" />

  # 🚀 RemX — Your Visual Second Brain & Instagram Reels AI Assistant

  [![Android](https://img.shields.io/badge/Platform-Android_8.0%2B-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/)
  [![Kotlin](https://img.shields.io/badge/Language-Kotlin_100%25-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
  [![Google Gemini](https://img.shields.io/badge/AI-Google_Gemini_2.0-FF6F00.svg?style=for-the-badge&logo=google)](https://ai.google.dev/)
  [![Author](https://img.shields.io/badge/Developer-GBAGUIDI_Exauc%C3%A9_(Gandxo)-blueviolet.svg?style=for-the-badge)](https://github.com/)

  <p align="center">
    <strong>"Ne perdez plus jamais une bonne idée, une recette ou un conseil partagé sur Instagram."</strong>
  </p>

</div>

---

### 🌟 Présentation de l'Application

**RemX** est une application Android de nouvelle génération conçue pour capturer, organiser et interroger l'ensemble de vos **Instagram Reels** sauvegardés. 

Grâce à l'intégration poussée de **Google Gemini AI**, l'application analyse automatiquement chaque Reel partagé, extrait les étapes clés et crée un résumé structuré sous forme de fiche de connaissance. Vous pouvez ensuite interroger votre mémoire visuelle grâce à un **Assistant Chat conversationnel** sécurisé.

> 👤 **Conçu & Développé avec passion par :** **GBAGUIDI Exaucé alias Gandxo**

---

## ✨ Fonctionnalités Clés

- 📱 **Partage Direct via Intent Android** : Partagez un Reel depuis l'application Instagram vers **RemX** en 1 clic.
- ⚡ **Analyse Automatique Gemini 2.0** : Extraction de l'auteur, titre, légende, résumé en 1-2 phrases et tags thématiques.
- 💬 **Assistant Chat Conversationnel (Façon WhatsApp)** : Posez des questions naturelles (*"Retrouve la recette de pâtes de la semaine dernière"*) et laissez l'IA vous répondre avec le lien du Reel.
- 🔔 **Notifications Push Locales** : Notification automatique dès que l'analyse du Reel partagé est terminée.
- 🔒 **Compte Sécurisé & Stockage 100% Local (Room DB)** : Authentification privée par Pseudo et Mot de Passe. Vos données restent sur votre smartphone.
- 🎬 **Onboarding Carrousel Interactif** : 4 slides guidés expliquant le concept et le tutoriel de partage Instagram avec bouton pulsant d'achèvement.
- 🎨 **Design Material You (M3)** : Thème moderne avec palette néon/violette, animations fluides et support tablette/foldable.
- 📂 **Sauvegarde & Restauration (Export/Import JSON)** : Exportez ou restaurez votre mémoire visuelle en un clin d'œil.

---

## 🛠️ Stack Technique & Architecture

```
RemX App
 ├── 🎨 Presentation (Jetpack Compose, Material 3, Navigation)
 ├── 🧠 ViewModel Layer (StateFlow, Coroutines)
 ├── 🤖 AI Service (Google Gemini API via Retrofit & Moshi)
 ├── 💾 Local Data Layer (Room Database, SQLite, Encrypted Credentials)
 └── 🔔 System Integration (Android Share Intent & Local Notifications)
```

| Composant | Technologie Utilisée |
| :--- | :--- |
| **Langage** | Kotlin 1.9+ |
| **Interface Utilisateur** | Jetpack Compose (Material 3) |
| **Base de Données** | Room Database (SQLite) |
| **Intelligence Artificielle** | Google Gemini 2.0 / 1.5 Flash API |
| **Réseau & Parsing** | Retrofit 2, OkHttp 3, Moshi JSON |
| **Gestion des Images** | Coil Compose |
| **Système de Build** | Gradle (Kotlin DSL `.gradle.kts`) |

---

## 📱 Onboarding & Expérience Utilisateur

| 1. Second Cerveau | 2. Partage Instagram | 3. Analyse Gemini | 4. Assistant Chat |
| :---: | :---: | :---: | :---: |
| 🧠 Concept RemX | 📲 Menu Partage ↗ | ⚡ Résumé Automatique | 💬 Recherche Interactive |

---

## 🚀 Installation & Démarrage

### Prérequis
- **Android Studio Ladybug** (2024.2.1+) ou version récente
- **JDK 17**
- Smartphone Android (Android 8.0+ / API 26+) ou Émulateur

### Étapes
1. **Cloner le repository GitHub** :
   ```bash
   git clone https://github.com/votre-compte/RemX.git
   cd RemX
   ```

2. **Ajouter la Clé API Gemini** :
   Créez un fichier `.env` à la racine ou renseignez votre clé dans `BuildConfig` :
   ```env
   GEMINI_API_KEY="VOTRE_CLE_API_GEMINI"
   ```

3. **Compiler le projet** :
   Lancez la compilation via Android Studio ou Gradle :
   ```bash
   ./gradlew assembleDebug
   ```

---

## 👑 Crédits & Auteur

- **Développeur & Créateur** : **GBAGUIDI Exaucé alias Gandxo**
- **Contact & Portfolio** : Application conçue dans le cadre du projet RemX Studio.

---

<div align="center">
  <sub>Fait avec ❤️ et passion par <strong>GBAGUIDI Exaucé (Gandxo)</strong> • RemX © 2026</sub>
</div>
