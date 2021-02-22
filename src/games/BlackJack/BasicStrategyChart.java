package games.BlackJack;



public class BasicStrategyChart {
    public enum Move{
        /*
            H	Hit
            S	Stand
            P	Split
            Dh	Double Down if possible, otherwise Hit
            Ds	Double Down if possible, otherwise Stand
            Rh 	Surrender if possible, otherwise Hit
        */
        H, S, P, Dh, Ds, Rh;
    }

    // chart for hard hands
    public static final Move hardHand[][] = {
            /*dealers upcard
                   :  2        3        4        5         6        7        8        9       10       Ace */
            /*hard*/
            {/*5*/	Move.H  ,Move.H	 ,Move.H  ,Move.H   ,Move.H  ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*6*/	Move.H	,Move.H	 ,Move.H  ,Move.H	,Move.H	 ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*7*/	Move.H	,Move.H	 ,Move.H  ,Move.H	,Move.H	 ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*8*/	Move.H	,Move.H	 ,Move.H  ,Move.H	,Move.H	 ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*9*/	Move.H	,Move.Dh ,Move.Dh ,Move.Dh	,Move.Dh ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*10*/	Move.Dh	,Move.Dh ,Move.Dh ,Move.Dh	,Move.Dh ,Move.Dh ,Move.Dh ,Move.Dh	,Move.H	 ,Move.H},
            {/*11*/	Move.Dh	,Move.Dh ,Move.Dh ,Move.Dh	,Move.Dh ,Move.Dh ,Move.Dh ,Move.Dh	,Move.Dh ,Move.H},
            {/*12*/	Move.H	,Move.H	 ,Move.S  ,Move.S	,Move.S	 ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*13*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*14*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*15*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.H  ,Move.H  ,Move.H	,Move.Rh ,Move.H},
            {/*16*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.H  ,Move.H  ,Move.Rh	,Move.Rh ,Move.Rh},
            {/*17*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.S  ,Move.S  ,Move.S	,Move.S	 ,Move.S},
            {/*18*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.S  ,Move.S  ,Move.S	,Move.S	 ,Move.S},
            {/*19*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.S  ,Move.S  ,Move.S	,Move.S	 ,Move.S},
            {/*20*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.S  ,Move.S  ,Move.S	,Move.S	 ,Move.S},
            {/*21*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.S  ,Move.S  ,Move.S	,Move.S	 ,Move.S},
    } ;

    // chart for soft hands
    public static final Move softHand[][] = {

            /*dealers upcard
                   :  2        3        4        5         6        7        8        9       10       Ace */
            /*soft*/
            {/*13*/	Move.H	,Move.H	 ,Move.H  ,Move.Dh	,Move.Dh ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*14*/	Move.H	,Move.H	 ,Move.H  ,Move.Dh	,Move.Dh ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*15*/	Move.H	,Move.H	 ,Move.Dh ,Move.Dh	,Move.Dh ,Move.H  ,Move.H  ,Move.H	,Move.H  ,Move.H},
            {/*16*/	Move.H	,Move.H	 ,Move.Dh ,Move.Dh	,Move.Dh ,Move.H  ,Move.H  ,Move.H	,Move.H  ,Move.H},
            {/*17*/	Move.H	,Move.Dh ,Move.Dh ,Move.Dh	,Move.Dh ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*18*/	Move.S	,Move.Ds ,Move.Ds ,Move.Ds	,Move.Ds ,Move.S  ,Move.S  ,Move.H	,Move.H	 ,Move.H},
            {/*19*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.S  ,Move.S  ,Move.S	,Move.S	 ,Move.S},
            {/*20*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.S  ,Move.S  ,Move.S	,Move.S	 ,Move.S},
            {/*21*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.S  ,Move.S  ,Move.S	,Move.S	 ,Move.S},
    } ;

    // chart for pairs
    public static final Move pairHand[][] = {

            /*dealers upcard
                   :  2        3        4        5         6        7        8        9       10       Ace */
            /*pair of*/
            {/*2*/	Move.P	,Move.P	 ,Move.P  ,Move.P	,Move.P  ,Move.P  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*3*/	Move.P	,Move.P	 ,Move.P  ,Move.P	,Move.P  ,Move.P  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*4*/	Move.H	,Move.H	 ,Move.H  ,Move.P	,Move.P  ,Move.H  ,Move.H  ,Move.H	,Move.H  ,Move.H},
            {/*5*/	Move.Dh	,Move.Dh ,Move.Dh ,Move.Dh	,Move.Dh ,Move.Dh ,Move.Dh ,Move.Dh	,Move.H  ,Move.H},
            {/*6*/	Move.P	,Move.P  ,Move.P  ,Move.P	,Move.P  ,Move.H  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*7*/	Move.P	,Move.P  ,Move.P  ,Move.P	,Move.P  ,Move.P  ,Move.H  ,Move.H	,Move.H	 ,Move.H},
            {/*8*/	Move.P	,Move.P	 ,Move.P  ,Move.P	,Move.P	 ,Move.P  ,Move.P  ,Move.P	,Move.P	 ,Move.P},
            {/*9*/	Move.P	,Move.P	 ,Move.P  ,Move.P	,Move.P	 ,Move.S  ,Move.P  ,Move.P	,Move.S	 ,Move.S},
            {/*10*/	Move.S	,Move.S	 ,Move.S  ,Move.S	,Move.S	 ,Move.S  ,Move.S  ,Move.S	,Move.S	 ,Move.S},
            {/*A*/	Move.P	,Move.P	 ,Move.P  ,Move.P	,Move.P	 ,Move.P  ,Move.P  ,Move.P	,Move.P	 ,Move.P},

    } ;

}
