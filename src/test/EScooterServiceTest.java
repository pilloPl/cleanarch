import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EScooterServiceTest {

    EScooterAvailabilityService eScooterService = Mockito.mock(EScooterAvailabilityService.class);

    ReservationRepository reservationRepository = new ReservationRepository();
    ReservationService reservationService = new ReservationService(eScooterService, reservationRepository);

    static final EScooterId SCOOTER = EScooterId.newOne();
    static final RiderId RIDER = RiderId.newOne();
    static final RiderId RIDER2 = RiderId.newOne();

    @Test
    void canReserveAvailableScooter() {
        //given
        thereIsAvailableScooter(SCOOTER);
        //when
        boolean result = reservationService.reserve(SCOOTER, RIDER);

        //then
        assertTrue(result);
        assertTrue(reservationRepository.findByEscooterId(SCOOTER).ownedBy(RIDER));

    }

    private void thereIsAvailableScooter(EScooterId scooter) {
        Mockito.when(eScooterService.take(
                ArgumentMatchers.eq(SCOOTER),
                ArgumentMatchers.any(),
                ArgumentMatchers.any())).thenReturn(true);
    }

    @Test
    void cantReserveUnavailableScooter() {
        //given
        thereIsUnavaiableScooter(SCOOTER);

        //when
        boolean result = reservationService.reserve(SCOOTER, RIDER2);

        //then
        assertFalse(result);
    }

    private void thereIsUnavaiableScooter(EScooterId scooter) {
        Mockito.when(eScooterService.take(
                ArgumentMatchers.eq(SCOOTER),
                ArgumentMatchers.any(),
                ArgumentMatchers.any())).thenReturn(false);
    }

}


