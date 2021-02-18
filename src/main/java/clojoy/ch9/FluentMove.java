package clojoy.ch9;

/**
 * @author zjjfly[https://github.com/zjjfly] on 2020/11/7
 */
public class FluentMove {

    private String from;

    private String to;

    private String promotion = "";

    private boolean castlep;

    public static MoveBuilder desc() {
        return new MoveBuilder();
    }

    public static final class MoveBuilder {
        FluentMove move = new FluentMove();

        public MoveBuilder from(String from) {
            move.from = from;
            return this;
        }

        public MoveBuilder to(String to) {
            move.to = to;
            return this;
        }

        public MoveBuilder castle() {
            move.castlep = true;
            return this;
        }

        public MoveBuilder promoteTo(String promotion) {
            move.promotion = promotion;
            return this;
        }

        public FluentMove build() {
            return move;
        }

    }

    @Override
    public String toString() {
        return "Move " + from + " to " + to + (castlep ? " castle" : "") +
                (promotion.length() != 0 ? " promote to " + promotion : "");
    }

    public static void main(String[] args) {
        FluentMove move = FluentMove.desc()
                                    .from("e2")
                                    .to("e4")
                                    .build();
        System.out.println(move);
        move = FluentMove.desc()
                         .from("a1")
                         .to("c1")
                         .castle()
                         .build();
        System.out.println(move);
        move = FluentMove.desc()
                         .from("a7")
                         .to("a8")
                         .promoteTo("Q")
                         .build();
        System.out.println(move);
    }
}
