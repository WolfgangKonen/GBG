package TournamentSystem;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.*;
import games.TicTacToe.LaunchTrainTTT;

public class notes
{
    public static void main(String[] args)
    {
        /**
         *
         * meine wichtigen Klassen
         *
         * Kette der Vererbung/Aufrufe:
         * @see LaunchTrainTTT // Startpunkt TTT
         * @see games.TicTacToe.ArenaTrainTTT
         * @see games.ArenaTrain
         * @see Arena // Arenacode und Gameloop
         *
         * @see XArenaMenu // TopMenu der GUI
         *
         * @see AgentBase // abstrakte Grundlagenklasse aller Agenten
         *
         * @see tools.Types.GUI_SCALING_FACTOR // Faktor zur GUI skalierung | hier noch nicht verfügbar da noch nicht gemergt
         *
         * @see games.LogManagerGUI // bsp gui im eigenen Fenster - vorlage TS GUI?
         *
         * @see tools.Types.GUI_AGENT_LIST // Liste der verfügbaren Agenten
         *
         * Kette Aufrufe Wettkampf
         * @see Arena#run() -> while() taskState==COMPETE
         * @see games.XArenaFuncs#singleCompete(XArenaButtons, GameBoard)
         * @see games.XArenaFuncs#competeBase(boolean, XArenaButtons, GameBoard)
         * @see games.XArenaFuncs#compete(PlayAgent, PlayAgent, StateObservation, int, int)  -> while()
         *
         * @see PlayAgent#getNextAction2(StateObservation, boolean, boolean) // berechnung des nächsten zuges des agenten
         *
         * ++++++++++++++++++
         *
         * Einsatzpunkt
         *      {@link Arena#PlayGame()} oder
         *      {@link XArenaFuncs#singleCompete(XArenaButtons, GameBoard)} oder
         *      {@link XArenaFuncs#multiCompete(boolean, XArenaButtons, GameBoard)} ?
         *      + neuen Taskstate in {@link Arena}
         *
         * Unterschied numGames und numCompetitions in {@link XArenaFuncs#multiCompete(boolean, XArenaButtons, GameBoard)} ?
         *
         */
    }
}
