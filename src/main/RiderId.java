import java.util.UUID;

class RiderId {

    static RiderId newOne() {
        return new RiderId(UUID.randomUUID());
    }

    private final UUID no;

    private RiderId(UUID no) {
        this.no = no;
    }
}
