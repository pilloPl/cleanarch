import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}

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

    public boolean ownedBy(RiderId riderId) {
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

    final ReservationRepository reservationRepository;

    EScooterService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    boolean reserve(EScooterId eScooterId, RiderId riderid) {
        if(reservedBySomeoneElse(eScooterId, riderid)) {
            return false;
        }
        reservationRepository.save(new Reservation(riderid, eScooterId, tenMinutes()));
        return true;
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