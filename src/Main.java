import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}
//1 resevation
//2 expiration
//3 maintance
class Reservation {

    enum Status {
        Active, Cancelled, Expired
    }

    private RiderId riderId;
    private EScooterId escooterId;
    private Instant validTill;
    private Status status = Status.Active;

    Reservation(RiderId riderid, EScooterId eScooterId, Instant validTill) {
        this.riderId = riderid;
        this.escooterId = eScooterId;
        this.validTill = validTill;
    }

    void expire() {
        status = Status.Expired;
    }

    void cancel() {
        status = Status.Cancelled;
    }

    boolean ownedBy(RiderId riderId) {
        return this.riderId.equals(riderId);
    }
}

class ReservationRepository {
    void save(Reservation reservation) {

    }

    Reservation findByEscooterId(EScooterId eScooterId) {
        return null;
    }
}

class EScooterService {


    final EScooterRepository eScooterRepository;
    final ReservationRepository reservationRepository;

    EScooterService(EScooterRepository eScooterRepository, ReservationRepository reservationRepository) {
        this.eScooterRepository = eScooterRepository;
        this.reservationRepository = reservationRepository;
    }

    boolean putIntoMaintenance(EScooterId eScooterId) {
        if(isReserved(eScooterId)) {
            return false;
        }
        EScooter eScooter = eScooterRepository.findByEScooterId(eScooterId);
        eScooter.putIntoMaintenance();
        eScooterRepository.save(eScooter);
        return true;
    }

    private boolean isReserved(EScooterId eScooterId) {
        return reservationRepository.findByEscooterId(eScooterId) != null;
    }

    boolean reserve(EScooterId eScooterId, RiderId riderid) {
        if (reservedBySomeoneElse(eScooterId, riderid)) {
            return false;
        }
        if (isInMaintenance(eScooterId)) {
            return false;
        }

        reservationRepository.save(new Reservation(riderid, eScooterId, tenMinutes()));
        return true;
    }

    private boolean isInMaintenance(EScooterId eScooterId) {
        return eScooterRepository.findByEScooterId(eScooterId).isInMaintenance();
    }

    private Instant tenMinutes() {
        return Instant.now().plus(Duration.ofMinutes(10));
    }

    private boolean reservedBySomeoneElse(EScooterId eScooterId, RiderId riderId) {
        Reservation reservation = reservationRepository.findByEscooterId(eScooterId);
        return reservation != null && !reservation.ownedBy(riderId);
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

class EScooterId {

    static EScooterId newOne() {
        return new EScooterId(UUID.randomUUID());
    }

    private final UUID no;

    private EScooterId(UUID no) {
        this.no = no;
    }
}

class EScooter {

    private EScooterId eScooterId;
    private boolean maintanece;

    boolean isInMaintenance() {
        return maintanece;
    }

    void putIntoMaintenance() {
        maintanece = false;
    }
}

class EScooterRepository {
    void save(EScooter eScooter) {

    }

    EScooter findByEScooterId(EScooterId eScooterId) {
        return null;
    }
}