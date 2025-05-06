# 👋 Bienvenue sur Smart Meet

## 💡 C’est quoi Smart Meet ?

**Smart Meet**, c’est notre réponse à une question simple :  
Combien d’opportunités avons-nous manquées lors d’événements scientifiques, simplement parce qu’on ne savait pas qui rencontrer ?

Notre application vise à **connecter les bonnes personnes au bon moment**, lors de conférences ou de séminaires. Grâce à l’intelligence artificielle, **Smart Meet organise automatiquement des rencontres** (en présentiel ou virtuelles) entre les participants qui partagent les mêmes centres d’intérêt ou des compétences complémentaires.

---

## 🧰 Ce que l'application peut faire

Voici un aperçu des fonctionnalités que nous développons :

- 🎤 **Organisation de conférences** : gestion des lieux, thèmes, plannings, notifications.
- 🧾 **Appel à contributions** : formulaires d’inscription, dépôt de papiers, posters ou présentations.
- 🧠 **Analyse intelligente des fichiers** : traitement semi-automatisé par IA.
- 🗓️ **Planification des sessions** : création de sessions, affectation de salles et de ressources.
- 💬 **Smart Meetings** : rencontres ciblées entre participants (principe du speed-dating).
- 🌍 **Espace interactif** : forums, publications, likes, commentaires.
- 📸 **Catalogue multimédia** : galerie photos liée à chaque événement.
- 👤 **Gestion des utilisateurs** : profils, rôles, permissions, authentification sécurisée.
- 📊 **Statistiques et analyses** : tableau de bord, prédictions, aide à la décision.

---

## 🛠️ Technologies utilisées (ou prévues)

- Backend : **Spring Boot**
- Frontend : **Angular**
- Base de données : **PostgreSQL**
- IA : matching intelligent & recommandations
- Intégrations prévues : **Jitsi Meet**, services cloud, etc.

---

## 🚀 Comment l’installer (exemple de setup local)

```bash
# Cloner le projet
git clone https://github.com/ton-utilisateur/smart-meet.git

# Lancer le backend
cd smart-meet/backend
./mvnw spring-boot:run

# Lancer le frontend
cd ../frontend
npm install
ng serve
