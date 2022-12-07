
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        System.out.println(Game.between(new Mean(), new Sucker()));
    }
}

enum Behavior {
    COOPERATE,
    BETRAY
}


class Game {

    static Result between(BehaviorStrategy player, BehaviorStrategy anotherPlayer) {
        Behavior cardOne = player.showBehavior();
        Behavior cardTwo = anotherPlayer.showBehavior();
        player.seenBehaviorWas(cardTwo);
        anotherPlayer.seenBehaviorWas(cardOne);
        Result result = calculatePoints(cardOne, cardTwo);
        sendEmail(result);
        save(result);
        return result;
    }

    static private Result calculatePoints(Behavior cardOne, Behavior cardTwo) {
        if (cardOne == Behavior.COOPERATE) {
            if (cardTwo == Behavior.COOPERATE) {
                return new Result(3, 3);
            }
            return new Result(0, 5);
        } else {
            if (cardTwo == Behavior.COOPERATE) {
                return new Result(5, 0);
            }
            return new Result(1, 1);
        }
    }

    private static void save(Result result) {

    }

    private static void sendEmail(Result result) {

    }


}


class Result {
    int one;
    int two;

    static Result zeroZero() {
        return new Result(0, 0);
    }

    Result(int one, int two) {
        this.one = one;
        this.two = two;
    }

    Result add(Result result) {
        return new Result(one + result.one, two + result.two);
    }

    @Override
    public String toString() {
        return "Result{" +
                "one=" + one +
                ", two=" + two +
                '}';
    }
}


interface BehaviorStrategy {
    Behavior showBehavior();

    void seenBehaviorWas(Behavior card);
}

class Sucker implements BehaviorStrategy {

    @Override
    public Behavior showBehavior() {
        return Behavior.COOPERATE;
    }

    @Override
    public void seenBehaviorWas(Behavior reaction) {
    }

    @Override
    public String toString() {
        return "Sucker";
    }
}

class Mean implements BehaviorStrategy {

    @Override
    public Behavior showBehavior() {
        return Behavior.BETRAY;
    }

    @Override
    public void seenBehaviorWas(Behavior reaction) {
    }

    @Override
    public String toString() {
        return "Mean";
    }
}















