import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


//mozemy skladac rezerwacje czasowa na hulajnoge (escooter), ona musza wygsnac, moge ja anulowac i moze nalezac do konkretne uzytkownika (rider)

class Reservation {

    enum Status {
        Active, Cancelled, Expired
    }

    private RiderId riderId;
    private EScooterId escooterId;
    private Instant validTill;
    private Status status = Status.Active;

    Reservation(RiderId riderId, EScooterId eScooterId, Instant validTill) {
        this.riderId = riderId;
        this.escooterId = eScooterId;
        this.validTill = validTill;
    }

    EScooterId scooterId() {
        return escooterId;
    }

    void expire() {
        status = Status.Expired;
    }

    void cancel() {
        status = Status.Cancelled;
    }

    boolean isActive(Instant now) {
        return status == Status.Active && now.isBefore(validTill);
    }

    boolean ownedBy(RiderId riderId) {
        return this.riderId.equals(riderId);
    }

}

class DemandService {

    final DemandRepository demandRepository;
    final EScooterAvailabilityService eScooterAvailabilityService;

    DemandService(DemandRepository demandRepository, EScooterAvailabilityService eScooterAvailabilityService) {
        this.demandRepository = demandRepository;
        this.eScooterAvailabilityService = eScooterAvailabilityService;
    }

    void addDemand(EScooterId eScooterId, Instant to) {
        if (eScooterAvailabilityService.take(eScooterId, Instant.now().plusSeconds(600), to)) {
            demandRepository.save(new Demand(eScooterId, to));
        }
    }

    boolean isDemandFor(EScooterId eScooterId, Instant when) {
        Demand demand = demandRepository.findByEScooterId(eScooterId);
        return demand != null && !demand.expired(when);
    }
}

class MaintenanceService {

    final EScooterAvailabilityService escooterAvailabilityService;

    MaintenanceService(EScooterAvailabilityService escooterAvailabilityService) {
        this.escooterAvailabilityService = escooterAvailabilityService;
    }

    boolean putIntoMaintenance(EScooterId eScooterId, Instant when) {
        if (escooterAvailabilityService.take(eScooterId, when.plusSeconds(600), when)) {
            //rob swoje
            return true;
        }
        return false;
    }
}

class ReservationService {

    final EScooterAvailabilityService escooterAvailabilityService;
    final ReservationRepository reservationRepository;

    ReservationService(EScooterAvailabilityService escooterAvailabilityService, ReservationRepository reservationRepository) {
        this.escooterAvailabilityService = escooterAvailabilityService;
        this.reservationRepository = reservationRepository;
    }

    boolean reserve(EScooterId eScooterId, RiderId riderId) {
        if (escooterAvailabilityService.take(eScooterId, tenMinutes(), Instant.now())) {
            reservationRepository.save(new Reservation(riderId, eScooterId, tenMinutes()));
            return true;
        }
        return false;
    }

    private Instant tenMinutes() {
        return Instant.now().plus(Duration.ofMinutes(10));
    }

}


class ReservationRepository {

    private Map<EScooterId, Reservation> reservations = new HashMap<>();

    void save(Reservation reservation) {
        reservations.put(reservation.scooterId(), reservation);
    }

    Reservation findByEscooterId(EScooterId eScooterId) {
        return reservations.get(eScooterId);
    }
}


class Demand {

    private EScooterId eScooterId;
    private Instant to;

    Demand(EScooterId eScooterId, Instant to) {
        this.eScooterId = eScooterId;
        this.to = to;
    }

    EScooterId scooterId() {
        return eScooterId;
    }

    boolean expired(Instant when) {
        return when.isAfter(to);
    }
}


class DemandRepository {

    private Map<EScooterId, Demand> demands = new HashMap<>();

    void save(Demand demand) {
        demands.put(demand.scooterId(), demand);
    }

    Demand findByEScooterId(EScooterId eScooterId) {
        return demands.get(eScooterId);
    }
}


//----///
class EScooterAvailability {

    private EScooterId eScooterId;
    private Instant till;

    EScooterAvailability(EScooterId scooter) {
        this.eScooterId = scooter;
    }

    boolean take(Instant when, Instant till) {
        if (isAvailable(when)) {
            this.till = till;
            return true;
        }
        return false;
    }

    private boolean isAvailable(Instant when) {
        return till == null || till.isBefore(when);
    }

    boolean release() {
        till = null;
        return true;
    }

    EScooterId id() {
        return eScooterId;
    }
}

class EScooterAvailabilityRepository {

    private Map<EScooterId, EScooterAvailability> scooters = new HashMap<>();

    void save(EScooterAvailability eScooter) {
        scooters.put(eScooter.id(), eScooter);
    }

    EScooterAvailability findByEScooterId(EScooterId eScooterId) {
        return scooters.get(eScooterId);
    }
}

//nie moze byc 2 rezeeweacji na hulajngoe w tym samym czasie

class EScooterAvailabilityService {

    final EScooterAvailabilityRepository eScooterRepository;

    EScooterAvailabilityService(EScooterAvailabilityRepository eScooterRepository) {
        this.eScooterRepository = eScooterRepository;
    }

    boolean take(EScooterId eScooterId, Instant till, Instant when) {
        EScooterAvailability availability = eScooterRepository.findByEScooterId(eScooterId);
        return availability.take(when, till);
    }

}


///-------///








