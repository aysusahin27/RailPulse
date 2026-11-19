package com.basespringboot.config;

import com.basespringboot.entity.StationEntity;
import com.basespringboot.entity.TripEntity;
import com.basespringboot.entity.UserEntity;
import com.basespringboot.repository.StationRepository;
import com.basespringboot.repository.TripRepository;
import com.basespringboot.repository.UserRepository;
import com.basespringboot.security.PasswordService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.init-data", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final TripRepository tripRepository;
    private final PasswordService passwordService;

    @Override
    @Transactional
    public void run(String... args) {
        initUsers();
        initStationsAndTrips();
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            log.info("Initializing demo users (admin / voyageur)...");
            UserEntity admin = new UserEntity(
                    "admin",
                    "admin@railway.com",
                    passwordService.hashPassword("admin123"),
                    "Chef de Gare",
                    "ROLE_ADMIN"
            );
            UserEntity voyageur = new UserEntity(
                    "voyageur",
                    "voyageur@sncf.fr",
                    passwordService.hashPassword("password123"),
                    "Aysu & Manal",
                    "ROLE_USER"
            );
            userRepository.saveAll(List.of(admin, voyageur));
            log.info("Users initialized: admin (admin123) / voyageur (password123)");
        }
    }

    private void initStationsAndTrips() {
        if (stationRepository.count() == 0) {
            log.info("Initializing French railway stations and live train departures...");

            StationEntity parisLyon = new StationEntity("Paris Gare de Lyon", "Paris", "87271007");
            StationEntity parisMont = new StationEntity("Paris Montparnasse", "Paris", "87391003");
            StationEntity lyonPartDieu = new StationEntity("Lyon Part-Dieu", "Lyon", "87723197");
            StationEntity marseilleStCh = new StationEntity("Marseille Saint-Charles", "Marseille", "87751008");
            StationEntity bordeauxStJean = new StationEntity("Bordeaux Saint-Jean", "Bordeaux", "87581009");
            StationEntity lilleEurope = new StationEntity("Lille Europe", "Lille", "87286005");
            StationEntity strasbourg = new StationEntity("Strasbourg-Ville", "Strasbourg", "87212027");

            stationRepository.saveAll(List.of(
                    parisLyon, parisMont, lyonPartDieu, marseilleStCh, bordeauxStJean, lilleEurope, strasbourg
            ));

            List<TripEntity> trips = List.of(
                    // Paris Gare de Lyon
                    new TripEntity("TGV 6601", "TGV INOUI", "Marseille Saint-Charles", "07:15", "10:35", "Voie 1", "ON_TIME", "À l'heure", parisLyon),
                    new TripEntity("TGV 6615", "TGV INOUI", "Lyon Part-Dieu", "08:00", "09:56", "Voie 3", "ON_TIME", "À l'heure", parisLyon),
                    new TripEntity("OUIGO 7821", "OUIGO", "Nice-Ville", "08:45", "14:20", "Voie 7", "DELAYED", "Retard 15 min", parisLyon),
                    new TripEntity("TGV 9243", "TGV INOUI", "Genève Cornavin", "09:18", "12:30", "Voie 2", "BOARDING", "Embarquement", parisLyon),
                    new TripEntity("TER 8912", "TER", "Dijon-Ville", "10:05", "12:45", "Voie 9", "ON_TIME", "À l'heure", parisLyon),
                    new TripEntity("TGV 6633", "TGV INOUI", "Marseille Saint-Charles", "11:30", "14:48", "Voie 5", "ON_TIME", "À l'heure", parisLyon),

                    // Paris Montparnasse
                    new TripEntity("TGV 8405", "TGV INOUI", "Bordeaux Saint-Jean", "07:45", "09:50", "Voie 2", "ON_TIME", "À l'heure", parisMont),
                    new TripEntity("OUIGO 7611", "OUIGO", "Rennes", "08:20", "09:55", "Voie 4", "BOARDING", "Embarquement", parisMont),
                    new TripEntity("TGV 8719", "TGV INOUI", "Nantes", "09:05", "11:15", "Voie 1", "DELAYED", "Retard 10 min", parisMont),
                    new TripEntity("TGV 8521", "TGV INOUI", "Toulouse Matabiau", "10:12", "14:25", "Voie 6", "ON_TIME", "À l'heure", parisMont),

                    // Lyon Part-Dieu
                    new TripEntity("TGV 6602", "TGV INOUI", "Paris Gare de Lyon", "08:30", "10:28", "Voie B", "ON_TIME", "À l'heure", lyonPartDieu),
                    new TripEntity("TER 88120", "TER", "Grenoble", "09:15", "10:32", "Voie F", "ON_TIME", "À l'heure", lyonPartDieu),
                    new TripEntity("TGV 6835", "TGV INOUI", "Marseille Saint-Charles", "10:05", "11:45", "Voie D", "ON_TIME", "À l'heure", lyonPartDieu),
                    new TripEntity("TER 88204", "TER", "Genève", "11:20", "13:15", "Voie E", "ON_TIME", "À l'heure", lyonPartDieu),

                    // Marseille Saint-Charles
                    new TripEntity("TGV 6608", "TGV INOUI", "Paris Gare de Lyon", "08:15", "11:35", "Voie A", "ON_TIME", "À l'heure", marseilleStCh),
                    new TripEntity("TER 88510", "TER", "Nice-Ville", "09:30", "12:05", "Voie C", "ON_TIME", "À l'heure", marseilleStCh),
                    new TripEntity("OUIGO 7824", "OUIGO", "Lyon Part-Dieu", "10:45", "12:20", "Voie B", "BOARDING", "Embarquement", marseilleStCh),

                    // Bordeaux Saint-Jean
                    new TripEntity("TGV 8412", "TGV INOUI", "Paris Montparnasse", "08:05", "10:12", "Voie 1", "ON_TIME", "À l'heure", bordeauxStJean),
                    new TripEntity("TER 86602", "TER", "Arcachon", "08:50", "09:42", "Voie 3", "ON_TIME", "À l'heure", bordeauxStJean),
                    new TripEntity("INTERCITÉS 4652", "INTERCITÉS", "Marseille Saint-Charles", "10:25", "16:40", "Voie 2", "ON_TIME", "À l'heure", bordeauxStJean),

                    // Lille Europe
                    new TripEntity("EUROSTAR 9114", "EUROSTAR", "Bruxelles-Midi", "09:00", "09:35", "Voie 1", "ON_TIME", "À l'heure", lilleEurope),
                    new TripEntity("TGV 5210", "TGV INOUI", "Lyon Part-Dieu", "10:15", "13:20", "Voie 2", "ON_TIME", "À l'heure", lilleEurope),

                    // Strasbourg-Ville
                    new TripEntity("TGV 2410", "TGV INOUI", "Paris Est", "08:20", "10:08", "Voie 1", "ON_TIME", "À l'heure", strasbourg),
                    new TripEntity("TER 83030", "TER", "Bâle SBB (Suisse)", "09:21", "10:40", "Voie 4", "ON_TIME", "À l'heure", strasbourg)
            );

            tripRepository.saveAll(trips);
            log.info("Initialized {} stations and {} live train trips successfully!", stationRepository.count(), tripRepository.count());
        }
    }
}
