# 🚀 RemX — Your Visual Second Brain & Instagram Reels AI Assistant

![RemX Banner](https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=1200&q=80)

> **"Ne perdez plus jamais une bonne idée, une recette ou un conseil partagé sur Instagram."**

**RemX** est une application Android moderne développée avec **Jetpack Compose** et propulsée par l'intelligence artificielle **Google Gemini 1.5/2.0**. Elle transforme le flux éphémère de vos Instagram Reels en un second cerveau visuel interactif, consultable et recherchable instantanément.

> 👤 **Conçu et développé avec passion par :** **GBAGUIDI Exaucé alias Gandxo**

---

## ✨ Fonctionnalités Principales

- 📲 **Partage Direct depuis Instagram** : Partagez n'importe quel lien Reel vers **RemX** directement via le menu système d'Android.
- ⚡ **Analyse Automatique Gemini AI** : Extraie automatiquement l'auteur, une légende explicite, un résumé concis en 2 phrases et des mots-clés thématiques.
- 💬 **Assistant Chat Conversationnel (Façon WhatsApp)** : Discutez avec votre mémoire visuelle pour retrouver instantanément la recette de la semaine ou les astuces de voyage enregistrées.
- 🔔 **Notifications Locales Android** : Recevez une alerte Push dès que l'analyse en arrière-plan d'un Reel partagé est finalisée.
- 🔒 **Compte Privé & Sécurisé (Room Database)** : Système d'authentification local avec pseudo et mot de passe chiffré. Vos souvenirs ne quittent jamais votre téléphone !
- 🎨 **Interface Moderne Material 3 & Thème Sombre/Clair** : Expérience fluide avec onboarding immersif, animations soignées et support multi-écrans.
- 📂 **Exportation & Importation JSON** : Sauvegardez et restaurez l'intégralité de vos données en un clic.
- 📜 **Pages Légales Intégrées** : Accès direct aux Mentions Légales, Politique de Confidentialité et À Propos.

---

## 🛠️ Stack Technique & Architecture

- **Langage** : Kotlin 100%
- **UI Framework** : Jetpack Compose (Material Design 3)
- **Architecture** : MVVM (Model-View-ViewModel) + Clean Architecture
- **Base de Données** : Room Database (SQLite avec TypeConverters & Flow reactive streams)
- **Moteur IA** : Google Gemini API via Retrofit2 & Moshi JSON adapter
- **Image Loading** : Coil Compose
- **Asynchronisme** : Kotlin Coroutines & StateFlow

---

## 📱 Aperçu de l'Application

| Onboarding & Slides | Authentification Sécurisée | Fil d'Actualité Reels | Assistant Chat Gemini |
| :---: | :---: | :---: | :---: |
| 🎬 Slides interactifs avec hooks | 🔑 Inscription par Pseudo + Mot de passe | 📺 Résumés Flash et thèmes auto | 🤖 Recherche conversationnelle |

---

## 🚀 Installation & Lancement

### Prérequis
- Android Studio Ladybug (2024.2.1) ou plus récent
- JDK 17+
- Un appareil Android (API 24+ / Android 7.0+) ou un Émulateur

### Étapes
1. **Cloner le projet** :
   ```bash
   git clone https://github.com/votre-username/RemX.git
   cd RemX
   ```

2. **Configuration de la Clé API Gemini** :
   Créez un fichier `.env` à la racine du projet ou ajoutez dans votre environnement :
   ```env
   GEMINI_API_KEY=votre_cle_api_gemini
   ```

3. **Compiler & Exécuter** :
   Ouvrez le projet dans Android Studio et lancez la compilation via Gradle (`:app:assembleDebug`).

---

## 📄 Licence & Crédits

- **Auteur & Développeur Principal** : **GBAGUIDI Exaucé alias Gandxo**
- **Framework & Outils** : Android Jetpack Compose, Google Gemini AI, Room DB, Retrofit, Coil.

---
*Fait avec ❤️ par GBAGUIDI Exaucé (Gandxo) pour simplifier le quotidien.*
