# ✅ Collaborative To-Do Lists - Backend

Ce backend fournit une API REST sécurisée permettant d'accéder à différentes applications telle qu'une gestion de listes de tâches collaboratives.  
Les utilisateurs peuvent créer des listes, y ajouter des tâches, inviter d'autres utilisateurs, et gérer les droits d'accès.

## 🚀 Fonctionnalités

- **Authentification & Autorisation**
  - Inscription et connexion sécurisées avec Spring Security + JWT
  - Gestion des rôles et des permissions

- **Gestion des listes**
  - Création, modification, suppression de listes
  - Ajout et suppression de membres
  - Mise à jour du titre

- **Gestion des tâches**
  - Ajout, modification, suppression et complétion des tâches
  - Attribution des tâches à un membre d'une liste

- **Sécurité**
  - Accès restreint aux listes et tâches en fonction des droits
  - Gestion des erreurs 403 (Accès refusé)

---

## 🛠️ Technologies utilisées

- **Java 21**
- **Spring Boot 3**
- Spring Security (JWT)
- Spring Data JPA (Hibernate)
- Base de données : H2 (dev test) / PostgreSQL (prod)
- Maven
- JUnit & Cucumber pour les tests

---

## 📂 Structure du projet

backend/  
│── src/main/java/com/simon/code_lab/  
│ ├── config/ # Configurations (sécurité, CORS, JWT, etc.)  
│ ├── controller/ # Endpoints REST  
│ ├── dto/ # Objets de transfert de données  
│ ├── exception # Les erreurs levées  
│ ├── model/ # Entités JPA  
│ ├── repository/ # DAO (accès DB)  
│ ├── response/ # Reponse de connexion  
│ ├── service/ # Logique métier  
│ ├── util/ # JWT et filtres de sécurité  
│── src/test/java/... # Tests unitaires et BDD (Cucumber)  
│── pom.xml  

---

## ⚙️ Installation

### 1️⃣ Cloner le dépôt
git clone https://github.com/simon-btr/code-lab-back
cd code-lab-back

### 2️⃣ Configurer la base de données
Dans src/main/resources/application.properties :

spring.datasource.url=jdbc:postgresql://localhost:5432/
spring.datasource.username=postgres
spring.datasource.password=monmotdepasse

### 3️⃣ Lancer le backend
mvn spring-boot:run
Le backend sera disponible sur :
http://localhost:8080

🧪 Lancer les tests

mvn test  
Les tests incluent :

Tests unitaires (JUnit)

Tests BDD avec Cucumber

📡 API Principales  
Méthode	Endpoint  
POST	/auth/signup	                Créer un compte utilisateur  
POST	/auth/login	                    Se connecter (JWT)  
GET	/todolists	                        Lister les listes de l'utilisateur  
POST	/todolists	                    Créer une liste  
PUT	/todolists/{id}	                    Modifier le titre d'une liste  
DELETE	/todolists/{id}	                Supprimer une liste  
POST	/todolists/{id}/members	        Ajouter un membre  
DELETE	/todolists/{id}/members/{email}	Supprimer un membre  
POST	/todolists/{id}/tasks	        Ajouter une tâche  
PUT	/tasks/{id}                         Mettre à jour une tâche  
DELETE	/tasks/{id}	                    Supprimer une tâche  

📄 Licence
Ce projet est sous licence MIT.
Vous êtes libre de l'utiliser, le modifier et le redistribuer à des fins personnelles ou professionnelles, avec attribution.

💡 Ce backend a été conçu dans un cadre de démonstration et de portfolio.