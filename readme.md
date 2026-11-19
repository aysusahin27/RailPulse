# RailPulse — Affichage & Gestion des Trains en Direct 🚆

Une application full-stack moderne développée avec **Spring Boot (Java 21)**, **PostgreSQL** et une interface **Web temps réel**.
Elle permet de rechercher n'importe quelle ville ou gare française pour afficher en direct le tableau des départs de trains (TGV, OUIGO, TER, Eurostar), avec une authentification JWT sécurisée et une collection d'API Bruno complète.

---

## 👥 Équipe du Projet

- **Aysu SAHIN** : [@aysusahin27](https://github.com/aysusahin27)
- **Manal EL BAZTAMI** : [@Menny206](https://github.com/Menny206)

---

## ✨ Fonctionnalités Principales

### 🌐 Interface Utilisateur & Expérience (UI/UX)
- **Tableau des Départs en Direct** : Style gare ferroviaire (SNCF / European Rail) avec horloge digitale temps réel à la seconde.
- **Recherche Instantanée par Ville ou Gare** : Recherche dynamique avec autocomplétion et filtres rapides (Paris, Lyon, Marseille, Bordeaux, Lille, Strasbourg...).
- **Badges Branded & Statuts Animés** : TGV INOUI, OUIGO, TER, Eurostar, Intercités. Statuts "À l'heure", "Retardé" (clignotement ambré), "Embarquement", "Supprimé".
- **Statistiques en Temps Réel** : Nombre de trains et taux de ponctualité calculés instantanément.
- **Filtres par Type** : Tous les trains, TGV & Ouigo, TER Régionaux, ou trains à l'heure uniquement.
- **Gestion Complète (CRUD)** : Ajout, modification, consultation et suppression des trains et gares via des modales ergonomiques avec notifications toast.
- **100% Responsive** : Adapté pour smartphones, tablettes et grands écrans.

### 🛡️ Sécurité & Authentification JWT
- Authentification par **Jetons JWT (JSON Web Tokens)** standard RFC 7519 (HMAC-SHA256).
- Mots de passe hachés avec sel cryptographique (SHA-256 + Salt).
- Rôles utilisateurs : `ROLE_USER` (Voyageur) et `ROLE_ADMIN` (Chef de Gare).
- Consultation publique (lecture libre de toutes les gares et trains).
- Opérations d'écriture (création, mise à jour, suppression) protégées par `@RequireAuth`.

---

## 🚀 Démarrage Rapide

### 1. Avec Docker Compose (Recommandé)

Lance l'application, PostgreSQL et Adminer en une seule commande :

```bash
docker compose up -d --build
```

- **Application Web & API** : <http://localhost:8088>
- **PostgreSQL** : `localhost:5433` (Base : `base_db`, Utilisateur : `postgres`, Mot de passe : `postgrespassword`)
- **Adminer (Gestionnaire BDD Web)** : <http://localhost:8081>

Pour arrêter les conteneurs :
```bash
docker compose down
```

Pour réinitialiser complètement la base de données :
```bash
docker compose down -v
docker compose up -d
```

---

### 2. Démarrage en local (Développement)

1. Lancez PostgreSQL (par exemple avec `docker compose up -d postgres`).
2. Démarrez l'application avec Maven :
```bash
./mvnw spring-boot:run
```
L'application démarre sur le port `8088`.

---

## 🔑 Comptes de Démonstration Préconfigurés

Au premier démarrage, la base de données s'initialise automatiquement avec deux comptes et un jeu réaliste de gares et trains :

| Rôle | Nom d'utilisateur | Mot de passe | Permissions |
| :--- | :--- | :--- | :--- |
| **Chef de Gare (Admin)** | `admin` | `admin123` | Création, modification et suppression de gares et trains |
| **Voyageur (User)** | `voyageur` | `password123` | Consultation, réservation et tests |

Des boutons de **connexion rapide** sont directement disponibles dans la fenêtre de connexion de l'interface !

---

## 📡 Documentation des Endpoints REST

### 1. Authentification (`/auth`)

| Méthode | Endpoint | Description | Accès |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/register` | Création d'un nouveau compte | Public |
| `POST` | `/auth/login` | Connexion et génération du jeton JWT | Public |
| `GET` | `/auth/me` | Récupération du profil connecté | Connecté (`Bearer Token`) |

### 2. Gares Ferroviaires (`/stations`)

| Méthode | Endpoint | Description | Accès |
| :--- | :--- | :--- | :--- |
| `GET` | `/stations` | Liste de toutes les gares | Public |
| `GET` | `/stations/search?q={ville}` | Recherche de gares par ville ou nom | Public |
| `GET` | `/stations/{id}` | Détails d'une gare | Public |
| `GET` | `/stations/{id}/trips` | Trains au départ de cette gare | Public |
| `POST` | `/stations` | Ajout d'une nouvelle gare | Protégé (`Bearer Token`) |
| `PUT` | `/stations/{id}` | Mise à jour d'une gare | Protégé (`Bearer Token`) |
| `DELETE` | `/stations/{id}` | Suppression d'une gare | Protégé (`Bearer Token`) |

### 3. Trains & Départs (`/trips`)

| Méthode | Endpoint | Description | Accès |
| :--- | :--- | :--- | :--- |
| `GET` | `/trips` | Liste de tous les trains programmés | Public |
| `GET` | `/trips/search?q={ville}` | Recherche de trains par ville ou destination | Public |
| `GET` | `/trips/{id}` | Détails d'un train | Public |
| `POST` | `/trips` | Programmation d'un train | Protégé (`Bearer Token`) |
| `PUT` | `/trips/{id}` | Mise à jour des horaires / voie / statut | Protégé (`Bearer Token`) |
| `DELETE` | `/trips/{id}` | Suppression d'un train | Protégé (`Bearer Token`) |

---

## 🧪 Collection d'API Bruno

Une collection **Bruno** prête à l'emploi est fournie dans le dossier [`baseSpringBoot-API/`](./baseSpringBoot-API) :

- **Environnement** : `Local` (`baseUrl = http://localhost:8088`)
- **Dossier 1. Auth** : `Register`, `Login Admin`, `Login Voyageur`, `Get Current User Profile` (le token JWT est capturé automatiquement lors du Login et réutilisé pour les requêtes protégées).
- **Dossier 2. Stations** : Recherche, consultation, ajout, mise à jour et suppression de gares.
- **Dossier 3. Trains & Trips** : Recherche par ville, consultation des départs, création et mise à jour des voies et statuts.

---

## 🏗️ Architecture & Bonnes Pratiques

- **Architecture en Couches** : Séparation stricte Controller / Service / Repository / Entity / DTO.
- **Performance JPA** : Utilisation de `@EntityGraph(attributePaths = {"departureStation"})` sur les requêtes pour éradiquer le problème N+1 requêtes SQL.
- **Gestion Globale des Erreurs** : `@RestControllerAdvice` retournant des réponses d'erreur normalisées et prévisibles avec horodatage, code HTTP et détails de validation des champs.
- **Transactions Managées** : `@Transactional(readOnly = true)` par défaut et `@Transactional` sur les écritures.
- **Validation Jakarta** : `@NotBlank`, `@Size`, `@Email`, etc. avec messages clairs.
- **Tests Automatisés** : 47 tests unitaires et d'intégration validant le comportement des contrôleurs, services, sécurité et gestionnaires d'exceptions.
