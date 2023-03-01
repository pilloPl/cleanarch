import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EScooterServiceTest {

    EScooterAvailabilityRepository eScooterRepository = new EScooterAvailabilityRepository();
    EScooterAvailabilityService eScooterService = new EScooterAvailabilityService(eScooterRepository);

    ReservationRepository reservationRepository = new ReservationRepository();
    ReservationService reservationService = new ReservationService(eScooterService, reservationRepository);
    //---//


    static final EScooterId SCOOTER = EScooterId.newOne();
    static final RiderId RIDER = RiderId.newOne();
    static final RiderId RIDER2 = RiderId.newOne();


    @Test
    void canReserveAvailableScooter() {
        //given
        thereIsScooter(SCOOTER);
        //when
        boolean result = reservationService.reserve(SCOOTER, RIDER);

        //then
        assertTrue(result);
        assertTrue(reservationRepository.findByEscooterId(SCOOTER).ownedBy(RIDER));
    }

    @Test
    void cantReserveAlreadyTakenScooter() {
        //given
        thereIsScooter(SCOOTER);
        reservationService.reserve(SCOOTER, RIDER);

        //when
        boolean result = reservationService.reserve(SCOOTER, RIDER2);

        //then
        assertFalse(result);
        assertTrue(reservationRepository.findByEscooterId(SCOOTER).ownedBy(RIDER));
    }


    private void thereIsScooter(EScooterId scooter) {
        eScooterRepository.save(new EScooterAvailability(scooter));
    }

}


