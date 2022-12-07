import java.util.UUID;

class EScooterId {

    static EScooterId newOne() {
        return new EScooterId(UUID.randomUUID());
    }

    private final UUID no;

    private EScooterId(UUID no) {
        this.no = no;
    }
}
