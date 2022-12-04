import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EScooterServiceTest {

    static final EScooterId SCOOTER = EScooterId.newOne();
    static final RiderId RIDER = RiderId.newOne();
    static final RiderId RIDER2 = RiderId.newOne();

    ReservationRepository reservationRepository = new ReservationRepository();
    EScooterRepository eScooterRepository = new EScooterRepository();

    DemandRepository demandRepository = new DemandRepository();

    DemandService demandService = new DemandService(demandRepository);
    EScooterService eScooterService = new EScooterService(reservationRepository, eScooterRepository, demandService);

    @Test
    void canReserveAvailableScooter() {
        //given
        thereIsScooter(SCOOTER);
        //when
        boolean result = eScooterService.reserve(SCOOTER, RIDER);

        //then
        assertTrue(result);
        assertTrue(reservationRepository.findByEscooterId(SCOOTER).ownedBy(RIDER));

    }

    @Test
    void cantReserveAlreadyTakenScooter() {
        //given
        thereIsScooter(SCOOTER);
        eScooterService.reserve(SCOOTER, RIDER);

        //when
        boolean result = eScooterService.reserve(SCOOTER, RIDER2);

        //then
        assertFalse(result);
        assertTrue(reservationRepository.findByEscooterId(SCOOTER).ownedBy(RIDER));

    }

    @Test
    void cantReserveWhenInMaintenance() {
        //given
        thereIsScooterInMaintance(SCOOTER);
        eScooterService.reserve(SCOOTER, RIDER);

        //when
        boolean result = eScooterService.reserve(SCOOTER, RIDER2);

        //then
        assertFalse(result);
        assertNull(reservationRepository.findByEscooterId(SCOOTER));

    }

    @Test
    void cantReserveWhenThereIsDemand() {
        //given
        thereIsScooter(SCOOTER);
        thereIsDemand(SCOOTER);

        //when
        boolean result = eScooterService.reserve(SCOOTER, RIDER2);

        //then
        assertFalse(result);
    }

    private void thereIsDemand(EScooterId scooter) {
        demandService.save(scooter, Instant.now().plusSeconds(600));
    }


    private void thereIsScooter(EScooterId scooter) {
        eScooterRepository.save(new EScooter(scooter));
    }

    private void thereIsScooterInMaintance(EScooterId scooter) {
        eScooterRepository.save(new EScooter(scooter, true));
    }


}