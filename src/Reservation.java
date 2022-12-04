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
    final EScooterRepository eScooterRepository;

    final DemandService demandService;

    EScooterService(ReservationRepository reservationRepository, EScooterRepository eScooterRepository, DemandService demandService) {
        this.reservationRepository = reservationRepository;
        this.eScooterRepository = eScooterRepository;
        this.demandService = demandService;
    }

    boolean reserve(EScooterId eScooterId, RiderId riderId) {
        if (reservedBySomeoneElse(eScooterId, riderId, Instant.now())) {
            return false;
        }
        if (isInMaintenance(eScooterId)) {
            return false;
        }
        if (isThereDemand(eScooterId, Instant.now())) {
            return false;
        }
        reservationRepository.save(new Reservation(riderId, eScooterId, tenMinutes()));
        return true;
    }

    boolean putIntoMaintenance(EScooterId eScooterId, Instant when) {
        if (isReserved(eScooterId, when)) {
            return false;
        }
        if (isThereDemand(eScooterId, when)) {
            return false;
        }
        EScooter eScooter = eScooterRepository.findByEScooterId(eScooterId);
        eScooter.putIntoMaintenance();
        eScooterRepository.save(eScooter);
        return true;
    }

    private boolean isInMaintenance(EScooterId eScooterId) {
        return eScooterRepository.findByEScooterId(eScooterId).isInMaintenance();
    }

    private boolean isReserved(EScooterId eScooterId, Instant when) {
        Reservation reservation = reservationRepository.findByEscooterId(eScooterId);
        return reservation != null && reservation.isActive(when);
    }

    private Instant tenMinutes() {
        return Instant.now().plus(Duration.ofMinutes(10));
    }

    private boolean reservedBySomeoneElse(EScooterId eScooterId, RiderId riderId, Instant when) {
        Reservation reservation = reservationRepository.findByEscooterId(eScooterId);
        return reservation != null && !reservation.ownedBy(riderId) && reservation.isActive(when);
    }

    private boolean isThereDemand(EScooterId eScooterId, Instant when) {
        return demandService.isDemandFor(eScooterId, when);
    }

    boolean addDemand(EScooterId eScooterId, Instant when) {
        if (isReserved(eScooterId, when)) {
            return false;
        }

        if (isInMaintenance(eScooterId)) {
            return false;
        }
        demandService.save(eScooterId, when);
        return true;
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

class EScooter {

    private EScooterId eScooterId;
    private boolean maintenance;

    EScooter(EScooterId scooter, boolean maintenance) {
        this.eScooterId = scooter;
        this.maintenance = maintenance;
    }

    EScooter(EScooterId scooter) {
        this(scooter, false);
    }

    boolean isInMaintenance() {
        return maintenance;
    }

    void putIntoMaintenance() {
        maintenance = true;
    }

    EScooterId id() {
        return eScooterId;
    }
}

class EScooterRepository {

    private Map<EScooterId, EScooter> scooters = new HashMap<>();

    void save(EScooter eScooter) {
        scooters.put(eScooter.id(), eScooter);
    }

    EScooter findByEScooterId(EScooterId eScooterId) {
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
