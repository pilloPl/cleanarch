import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class Reservation {


    enum Status {
        Active, Cancelled, Expired
    }

    private RiderId riderId;
    private EScooterId escooterId;

    public Reservation(RiderId riderId, EScooterId escooterId, Instant validTill) {
        this.riderId = riderId;
        this.escooterId = escooterId;
        this.validTill = validTill;
    }

    private Instant validTill;
    private Status status = Status.Active;

    EScooterId scooterId() {
        return escooterId;
    }

    boolean isActive(Instant now) {
        return status == Status.Active && now.isBefore(validTill);
    }

    boolean ownedBy(RiderId riderId) {
        return this.riderId.equals(riderId);
    }

    void expire() {
        status = Status.Expired;
    }

    void cancel() {
        status = Status.Cancelled;
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

class EScooterService {

    final ReservationRepository reservationRepository;
    final EScooterAvailabilityRepository availabilityRepo;
    final DemandService demandService;

    EScooterService(ReservationRepository reservationRepository, EScooterAvailabilityRepository eScooterRepository, DemandService demandService) {
        this.reservationRepository = reservationRepository;
        this.availabilityRepo = eScooterRepository;
        this.demandService = demandService;
    }

    boolean reserve(EScooterId eScooterId, RiderId riderId) {
        if (take(eScooterId, Instant.now(), tenMinutes())) {
            reservationRepository.save(new Reservation(riderId, eScooterId, tenMinutes()));
            return true;
        }
        return false;
    }

    boolean addDemand(EScooterId eScooterId, Instant when) {
        if (take(eScooterId, when, tenMinutes())) {
            demandService.save(eScooterId, when);
        }
        return true;
    }

    boolean putIntoMaintenance(EScooterId eScooterId, Instant when) {
        return take(eScooterId, when, Instant.MAX);
    }

    boolean take(EScooterId eScooterId, Instant till, Instant when) {
        EscooterAvailability availability = availabilityRepo.findByEScooterId(eScooterId);
        return availability.take(till, when);
    }

    private Instant tenMinutes() {
        return Instant.now().plus(Duration.ofMinutes(10));
    }


}

class EScooterId {

    static EScooterId newOne() {
        return new EScooterId(UUID.randomUUID());
    }

    private final UUID no;

    private EScooterId(UUID no) {
        this.no = no;
    }
}

class RiderId {

    static RiderId newOne() {
        return new RiderId(UUID.randomUUID());
    }

    private final UUID no;

    private RiderId(UUID no) {
        this.no = no;
    }
}

class EscooterAvailability {

    private EScooterId eScooterId;
    private Instant till;

    EscooterAvailability(EScooterId scooter) {
        this.eScooterId = scooter;
        this.till = null;
    }


    boolean take(Instant till, Instant when) {
        if (isAvailable(when)) {
            this.till = till;
            return true;
        }
        return false;
    }

    boolean isAvailable(Instant when) {
        return till == null || till.isAfter(when);
    }

    void release() {
        till = null;
    }

    EScooterId id() {
        return eScooterId;
    }
}

class EScooterAvailabilityRepository {

    private Map<EScooterId, EscooterAvailability> scooters = new HashMap<>();

    void save(EscooterAvailability eScooter) {
        scooters.put(eScooter.id(), eScooter);
    }

    EscooterAvailability findByEScooterId(EScooterId eScooterId) {
        return scooters.get(eScooterId);
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

class DemandService {

    final DemandRepository demandRepository;

    DemandService(DemandRepository demandRepository) {
        this.demandRepository = demandRepository;
    }

    void save(EScooterId eScooterId, Instant to) {
        demandRepository.save(new Demand(eScooterId, to));
    }

    boolean isDemandFor(EScooterId eScooterId, Instant when) {
        Demand demand = demandRepository.findByEScooterId(eScooterId);
        return demand != null && !demand.expired(when);
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
