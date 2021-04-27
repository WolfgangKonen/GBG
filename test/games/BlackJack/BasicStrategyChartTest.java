package games.BlackJack;


import games.BoardVector;
import games.XNTupleFuncs;
import org.junit.Test;
import tools.Types;

import java.util.Random;

// Tests the validity of Basic Strategy
public class BasicStrategyChartTest {


    public static final int NUM_ITERATIONS = 100000;

    public StateObserverBlackJack initSo(Card firstCardPlayer, Card secondCardPlayer, Card upCardDealer){
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITERATIONS);
        so.getCurrentPlayer().bet(10);
        so.getCurrentPlayer().addCardToActiveHand(firstCardPlayer);
        so.getCurrentPlayer().addCardToActiveHand(secondCardPlayer);
        // Players Turn
        so.setgPhase(StateObserverBlackJack.gamePhase.PLAYERONACTION);
        so.getDealer().addCardToActiveHand(upCardDealer);
        so.getDealer().addCardToActiveHand(new Card(Card.Rank.X, Card.Suit.X));
        so.setPartialState(true);
        so.setAvailableActions();
        return so;
    }

    public StateObserverBlackJack init3So(Card first, Card second, Card third, Card upCardDealer){
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITERATIONS);
        so.getCurrentPlayer().bet(10);
        so.getCurrentPlayer().addCardToActiveHand(first);
        so.getCurrentPlayer().addCardToActiveHand(second);
        so.getCurrentPlayer().addCardToActiveHand(third);
        // Players Turn
        so.setgPhase(StateObserverBlackJack.gamePhase.PLAYERONACTION);
        so.getDealer().addCardToActiveHand(upCardDealer);
        so.getDealer().addCardToActiveHand(new Card(Card.Rank.X, Card.Suit.X));
        so.setPartialState(true);
        so.setAvailableActions();
        return so;
    }

    // just a little check: look with debugger at the constructed BoardVector bv if it is as expected
    @Test
    public void testBoardVector() {
        StateObserverBlackJack so = init3So( new Card(Card.Rank.TEN, Card.Suit.SPADE),
                                            new Card(Card.Rank.QUEEN, Card.Suit.SPADE),
                                            new Card(Card.Rank.SEVEN, Card.Suit.HEART),
                new Card(Card.Rank.FIVE, Card.Suit.CLUB));
        ArenaBlackJackTrain ar = new ArenaBlackJackTrain("tit",false);
        XNTupleFuncs xf = ar.makeXNTupleFuncs();
        BoardVector bv = xf.getBoardVector(so);
        System.out.println(so.stringDescr());
        System.out.println(so.getRewardTuple(true));
        int dummy = 1;
    }

    //expected best move Stand
    @Test
    public void testHardTwelveAgainstDealersFive(){

        StateObserverBlackJack so = initSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.SEVEN, Card.Suit.SPADE),
                new Card(Card.Rank.FIVE, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[0] > values[1]): "avgPayOffStand should be greater than avgPayOffSurrender";
        assert (values[0] > values[2]): "avgPayOffStand should be greater than avgPayOffDoubleDown";
        assert (values[0] > values[3]): "avgPayOffStand should be greater than avgPayOffHit";


    }

    //expected best move Stand
    @Test
    public void testHardTwelveAgainstDealersSix(){

        StateObserverBlackJack so = initSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.SEVEN, Card.Suit.SPADE),
                new Card(Card.Rank.SIX, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[0] > values[1]): "avgPayOffStand should be greater than avgPayOffSurrender";
        assert (values[0] > values[2]): "avgPayOffStand should be greater than avgPayOffDoubleDown";
        assert (values[0] > values[3]): "avgPayOffStand should be greater than avgPayOffHit";
    }

    //expected best move Hit
    @Test
    public void testHardFiveAgainstDealersSix(){

        StateObserverBlackJack so = initSo(new Card(Card.Rank.TWO, Card.Suit.SPADE), new Card(Card.Rank.THREE, Card.Suit.SPADE),
                new Card(Card.Rank.SIX, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);
        System.out.println("BA example {2,3} against dealer 6");
        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);
        System.out.println("Hit with random moves: " + simulationHitWithRandomMove(so));
        System.out.println("Hit with random rollout fixed depth 10 : " + simulationWithRandomMovesFixedRollout(so, Types.ACTIONS.fromInt(
                StateObserverBlackJack.BlackJackActionDet.HIT.getAction()
        )));
        System.out.println("Stand with random rollout fixed depth 10 : " + simulationWithRandomMovesFixedRollout(so, Types.ACTIONS.fromInt(
                StateObserverBlackJack.BlackJackActionDet.STAND.getAction()
        )));

        assert (values[3] > values[1]): "avgPayOffStand should be greater than avgPayOffSurrender";
        assert (values[3] > values[2]): "avgPayOffStand should be greater than avgPayOffDoubleDown";
        assert (values[3] > values[0]): "avgPayOffStand should be greater than avgPayOffStand";
    }

    //expected best move Hit
    @Test
    public void testHardTwelveAgainstDealersSeven(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.SEVEN, Card.Suit.SPADE),
                new Card(Card.Rank.SEVEN, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[3] > values[0]): "avgPayOffHit should be greater than avgPayOffStand";
        assert (values[3] > values[1]): "avgPayOffHit should be greater than avgPayOffSurrender";
        assert (values[3] > values[2]): "avgPayOffHit should be greater than avgPayOffDoubleDown";

    }

    //expected best move Surrender
    @Test
    public void testHardSixteenAgainstDealersNine(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.TEN, Card.Suit.SPADE), new Card(Card.Rank.SIX, Card.Suit.SPADE),
                new Card(Card.Rank.NINE, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[1] > values[0]): "avgPayOffSurrender should be greater than avgPayOffStand";
        assert (values[1] > values[2]): "avgPayOffSurrender should be greater than avgPayOffDoubleDown";
        assert (values[1] > values[3]): "avgPayOffSurreder should be greater than avgPayOffHit";
    }

    //expected best move DoubleDown
    @Test
    public void testHardNineAgainstDealersSix(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.FOUR, Card.Suit.SPADE), new Card(Card.Rank.FIVE, Card.Suit.SPADE),
                new Card(Card.Rank.SIX, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[2] > values[0]): "avgPayOffDoubleDown should be greater than avgPayOffStand";
        assert (values[2] > values[1]): "avgPayOffDoubleDown should be greater than avgPayOffSurrender";
        assert (values[2] > values[3]): "avgPayOffDoubleDown should be greater than avgPayOffHit";

    }

    //expected best move Hit
    @Test
    public void testHardNineAgainstDealersSeven(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.FOUR, Card.Suit.SPADE), new Card(Card.Rank.FIVE, Card.Suit.SPADE),
                new Card(Card.Rank.SEVEN, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[3] > values[0]): "avgPayOffHit should be greater than avgPayOffStand";
        assert (values[3] > values[1]): "avgPayOffHit should be greater than avgPayOffSurrender";
        assert (values[3] > values[2]): "avgPayOffHit should be greater than avgPayOffDoubleDown";


    }

    //expected best move DoubleDown
    @Test
    public void testSoftSixteenAgainstDealersFive(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.FIVE, Card.Suit.SPADE),
                new Card(Card.Rank.FIVE, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[2] > values[0]): "avgPayOffDoubleDown should be greater than avgPayOffStand";
        assert (values[2] > values[1]): "avgPayOffDoubleDown should be greater than avgPayOffSurrender";
        assert (values[2] > values[3]): "avgPayOffDoubleDown should be greater than avgPayOffHit";
    }

    //expected best move DoubleDown
    @Test
    public void testSoftSixteenAgainstDealersSix(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.FIVE, Card.Suit.SPADE),
                new Card(Card.Rank.SIX, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[2] > values[0]): "avgPayOffDoubleDown should be greater than avgPayOffStand";
        assert (values[2] > values[1]): "avgPayOffDoubleDown should be greater than avgPayOffSurrender";
        assert (values[2] > values[3]): "avgPayOffDoubleDown should be greater than avgPayOffHit";
    }

    //expected best move Hit
    @Test
    public void testSoftSixteenAgainstDealersSeven(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.FIVE, Card.Suit.SPADE),
                new Card(Card.Rank.SEVEN, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[3] > values[0]): "avgPayOffHit should be greater than avgPayOffStand";
        assert (values[3] > values[1]): "avgPayOffHit should be greater than avgPayOffSurrender";
        assert (values[3] > values[2]): "avgPayOffHit should be greater than avgPayOffDoubleDown";
    }

    //expected best move Stand
    @Test
    public void testSoftEighteenAgainstDealersEight(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.SEVEN, Card.Suit.SPADE),
                new Card(Card.Rank.EIGHT, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[0] > values[1]): "avgPayOffStand should be greater than avgPayOffSurrender";
        assert (values[0] > values[2]): "avgPayOffStand should be greater than avgPayOffDoubleDown";
        assert (values[0] > values[3]): "avgPayOffStand should be greater than avgPayOffHit";
    }

    //expected best move Hit
    @Test
    public void testSoftEighteenAgainstDealersNine(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.SEVEN, Card.Suit.SPADE),
                new Card(Card.Rank.NINE, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 4) : "AvailableActions.size should return 4";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";

        double []values = simulation(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);

        assert (values[3] > values[0]): "avgPayOffHit should be greater than avgPayOffStand";
        assert (values[3] > values[1]): "avgPayOffHit should be greater than avgPayOffSurrender";
        assert (values[3] > values[2]): "avgPayOffHit should be greater than avgPayOffDoubleDown";
    }

    //expected best move Split
    @Test
    public void testPairOfThreesAgainstDealersSeven(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.THREE, Card.Suit.SPADE), new Card(Card.Rank.THREE, Card.Suit.SPADE),
                new Card(Card.Rank.SEVEN, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 5) : "AvailableActions.size should return 5";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SPLIT.getAction()))): "SPLIT should be in Available Actions";

        double []values = simulationWithSplit(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);
        System.out.println("Split: " + values[4]);



        assert (values[4] > values[0]): "avgPayOffSplit should be greater than avgPayOffStand";
        assert (values[4] > values[1]): "avgPayOffSplit should be greater than avgPayOffSurrender";
        assert (values[4] > values[2]): "avgPayOffSplit should be greater than avgPayOffDoubleDown";
        assert (values[4] > values[3]): "avgPayOffSplit should be greater than avgPayOffHit";
    }

    //expected best move Hit
    //this case sometimes fails with NUM_ITERATIONS around 100.000 This Test will pass for NUM_ITERATIONS around 1.000.000
    @Test
    public void testPairOfThreesAgainstDealersEight(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.THREE, Card.Suit.SPADE), new Card(Card.Rank.THREE, Card.Suit.SPADE),
                new Card(Card.Rank.EIGHT, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 5) : "AvailableActions.size should return 5";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SPLIT.getAction()))): "SPLIT should be in Available Actions";

        double []values = simulationWithSplit(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);
        System.out.println("Split: " + values[4]);



        assert (values[3] > values[0]): "avgPayOffHit should be greater than avgPayOffStand";
        assert (values[3] > values[1]): "avgPayOffHit should be greater than avgPayOffSurrender";
        assert (values[3] > values[2]): "avgPayOffHit should be greater than avgPayOffDoubleDown";
        assert (values[3] > values[4]): "avgPayOffHit should be greater than avgPayOffSplit";
    }

    //expected best move DoubleDown
    @Test
    public void testPairOfFivesAgainstDealersNine(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.FIVE, Card.Suit.SPADE),
                new Card(Card.Rank.NINE, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 5) : "AvailableActions.size should return 5";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SPLIT.getAction()))): "SPLIT should be in Available Actions";

        double []values = simulationWithSplit(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);
        System.out.println("Split: " + values[4]);



        assert (values[2] > values[0]): "avgPayOffDoubleDown should be greater than avgPayOffStand";
        assert (values[2] > values[1]): "avgPayOffDoubleDown should be greater than avgPayOffSurrender";
        assert (values[2] > values[3]): "avgPayOffDoubleDown should be greater than avgPayOffHit";
        assert (values[2] > values[4]): "avgPayOffDoubleDown should be greater than avgPayOffSplit";
    }

    //expected best move Hit
    @Test
    public void testPairOfFivesAgainstDealersTen(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.FIVE, Card.Suit.SPADE),
                new Card(Card.Rank.TEN, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 5) : "AvailableActions.size should return 5";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SPLIT.getAction()))): "SPLIT should be in Available Actions";

        double []values = simulationWithSplit(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);
        System.out.println("Split: " + values[4]);



        assert (values[3] > values[0]): "avgPayOffHit should be greater than avgPayOffStand";
        assert (values[3] > values[1]): "avgPayOffHit should be greater than avgPayOffSurrender";
        assert (values[3] > values[2]): "avgPayOffHit should be greater than avgPayOffDoubleDown";
        assert (values[3] > values[4]): "avgPayOffHit should be greater than avgPayOffSplit";
    }

    //expected best move Hit
    @Test
    public void testPairOfEightsAgainstDealersAce(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.EIGHT, Card.Suit.SPADE), new Card(Card.Rank.EIGHT, Card.Suit.SPADE),
                new Card(Card.Rank.ACE, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 5) : "AvailableActions.size should return 5";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SPLIT.getAction()))): "SPLIT should be in Available Actions";

        double []values = simulationWithSplit(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);
        System.out.println("Split: " + values[4]);



        assert (values[4] > values[0]): "avgPayOffSplit should be greater than avgPayOffStand";
        assert (values[4] > values[1]): "avgPayOffSplit should be greater than avgPayOffSurrender";
        assert (values[4] > values[2]): "avgPayOffSplit should be greater than avgPayOffDoubleDown";
        assert (values[4] > values[3]): "avgPayOffSplit should be greater than avgPayOffHit";
    }

    //expected best move Stand
    @Test
    public void testPairOfNinesAgainstDealersSeven(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.NINE, Card.Suit.SPADE), new Card(Card.Rank.NINE, Card.Suit.SPADE),
                new Card(Card.Rank.SEVEN, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 5) : "AvailableActions.size should return 5";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SPLIT.getAction()))): "SPLIT should be in Available Actions";

        double []values = simulationWithSplit(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);
        System.out.println("Split: " + values[4]);



        assert (values[0] > values[1]): "avgPayOffStand should be greater than avgPayOffSurrender";
        assert (values[0] > values[2]): "avgPayOffStand should be greater than avgPayOffDoubleDown";
        assert (values[0] > values[3]): "avgPayOffStand should be greater than avgPayOffHit";
        assert (values[0] > values[4]): "avgPayOffStand should be greater than avgPayOffSplit";
    }

    //expected best move Stand
    @Test
    public void testPairOfNinesAgainstDealersEight(){
        StateObserverBlackJack so = initSo(new Card(Card.Rank.NINE, Card.Suit.SPADE), new Card(Card.Rank.NINE, Card.Suit.SPADE),
                new Card(Card.Rank.EIGHT, Card.Suit.CLUB));

        assert (so.getAvailableActions().size() == 5) : "AvailableActions.size should return 5";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()))): "Stand should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))): "Surrender should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))): "DoubleDown should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()))): "HIT should be in Available Actions";
        assert (so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SPLIT.getAction()))): "SPLIT should be in Available Actions";

        double []values = simulationWithSplit(so);

        System.out.println("Stand: " + values[0]);
        System.out.println("Sur: " + values[1]);
        System.out.println("dd: " + values[2]);
        System.out.println("Hit: " + values[3]);
        System.out.println("Split: " + values[4]);



        assert (values[4] > values[0]): "avgPayOffSplit should be greater than avgPayOffStand";
        assert (values[4] > values[1]): "avgPayOffSplit should be greater than avgPayOffSurrender";
        assert (values[4] > values[2]): "avgPayOffSplit should be greater than avgPayOffDoubleDown";
        assert (values[4] > values[3]): "avgPayOffSplit should be greater than avgPayOffHit";
    }





    public double[] simulationWithSplit(StateObserverBlackJack  so){
        double [] tmpValues = simulation(so);
        double [] values = new double[5];
        //copy the valuse from simulation
        for(int i = 0; i < tmpValues.length; i++){
            values[i] = tmpValues[i];
        }

        double avgPayOffSplit = 0;
        for(int i = 0; i < NUM_ITERATIONS; i++){
            StateObserverBlackJack newSo = (StateObserverBlackJack) so.copy();
            newSo.advance(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SPLIT.getAction()));
            BasicStrategyBlackJackAgent ba = new BasicStrategyBlackJackAgent();
            while(!newSo.isRoundOver()){
                if(newSo.getCurrentPlayer().getActiveHand().isPair())
                    newSo.advance(Types.ACTIONS.fromInt(ba.lookUpBestMovePair(newSo)));
                else if(newSo.getCurrentPlayer().getActiveHand().isSoft())
                    newSo.advance(Types.ACTIONS.fromInt(ba.lookUpBestMoveSoft(newSo)));
                else
                    newSo.advance(Types.ACTIONS.fromInt(ba.lookUpBestMoveHard(newSo)));
            }
            avgPayOffSplit += newSo.getCurrentPlayer().getRoundPayoff();
        }
        avgPayOffSplit /= NUM_ITERATIONS;

        values[4] = avgPayOffSplit;
        return values;


    }

    public double simulationHitWithRandomMove(StateObserverBlackJack  so){
        Random r = new Random();
        double avgPayOffHit = 0;
        for(int i = 0; i < NUM_ITERATIONS; i++){
            StateObserverBlackJack newSo = (StateObserverBlackJack) so.copy();
            newSo.advance(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()));
            while(!newSo.isRoundOver()){
                int action = r.nextInt(newSo.getNumAvailableActions());
                newSo.advance(newSo.getAction(action));
            }
            avgPayOffHit += newSo.getCurrentPlayer().getRoundPayoff();
        }
        avgPayOffHit /= NUM_ITERATIONS;

        return avgPayOffHit;
    }

    public double simulationWithRandomMovesFixedRollout(StateObserverBlackJack  so, Types.ACTIONS a){
        Random r = new Random();
        double avgFinalReward = 0;
        for(int i = 0; i < NUM_ITERATIONS; i++){
            StateObserverBlackJack newSo = (StateObserverBlackJack) so.copy();
            newSo.advance(a);
            if(newSo.isRoundOver())
                newSo.initRound();
            for(int f = 0; f < 5; f++){
                newSo.advance(newSo.getAction(r.nextInt(newSo.getNumAvailableActions())));
                if(newSo.isRoundOver())
                    newSo.initRound();
            }
            avgFinalReward += newSo.getGameScore(0);
        }
        avgFinalReward /= NUM_ITERATIONS;

        return avgFinalReward-200;
    }


    public double[] simulation(StateObserverBlackJack  so){
        double avgPayOffStand = 0;
        for(int i = 0; i < NUM_ITERATIONS; i++){
            StateObserverBlackJack newSo = (StateObserverBlackJack) so.copy();
            newSo.advance(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.STAND.getAction()));
            avgPayOffStand += newSo.getCurrentPlayer().getRoundPayoff();
        }
        avgPayOffStand /= NUM_ITERATIONS;

        double avgPayOffSurrender = 0;
        for(int i = 0; i < NUM_ITERATIONS; i++){
            StateObserverBlackJack newSo = (StateObserverBlackJack) so.copy();
            newSo.advance(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()));
            avgPayOffSurrender += newSo.getCurrentPlayer().getRoundPayoff();
        }
        avgPayOffSurrender /= NUM_ITERATIONS;

        double avgPayOffDoubleDown = 0;
        for(int i = 0; i < NUM_ITERATIONS; i++){
            StateObserverBlackJack newSo = (StateObserverBlackJack) so.copy();
            newSo.advance(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()));
            avgPayOffDoubleDown += newSo.getCurrentPlayer().getRoundPayoff();
        }
        avgPayOffDoubleDown /= NUM_ITERATIONS;

        double avgPayOffHit = 0;
        for(int i = 0; i < NUM_ITERATIONS; i++){
            StateObserverBlackJack newSo = (StateObserverBlackJack) so.copy();
            newSo.advance(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.HIT.getAction()));
            BasicStrategyBlackJackAgent ba = new BasicStrategyBlackJackAgent();
            while(!newSo.isRoundOver()){
                if(newSo.getCurrentPlayer().getActiveHand().isSoft())
                    newSo.advance(Types.ACTIONS.fromInt(ba.lookUpBestMoveSoft(newSo)));
                else
                    newSo.advance(Types.ACTIONS.fromInt(ba.lookUpBestMoveHard(newSo)));
            }
            avgPayOffHit += newSo.getCurrentPlayer().getRoundPayoff();
        }
        avgPayOffHit /= NUM_ITERATIONS;

        double values[] = {avgPayOffStand, avgPayOffSurrender, avgPayOffDoubleDown, avgPayOffHit};
        return values;
    }
}
