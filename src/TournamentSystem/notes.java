package TournamentSystem;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.*;
import games.TicTacToe.LaunchTrainTTT;

import javax.swing.*;

@Deprecated
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
         * Fragen:
         *
         * welcher Einsatzpunkt Competition?
         *      {@link Arena#PlayGame()} (erlaubt human und visualsiert spiel live) oder
         *      {@link XArenaFuncs#singleCompete(XArenaButtons, GameBoard)} oder
         *      {@link XArenaFuncs#multiCompete(boolean, XArenaButtons, GameBoard)} ?
         *      + neuen Taskstate in {@link Arena}
         *
         * Unterschied numGames und numCompetitions in {@link XArenaFuncs#multiCompete(boolean, XArenaButtons, GameBoard)} ?
         *
         * Wie komme ich in XArenaFuncs an meine GUI Eingaben?
         *      {@link XArenaButtons} braucht eine Instanz als public Object
         *      siehe {@link XArenaMenu} zeile 271
         *      GUI extends JFrame?
         *
         * Exakte System in dem die Agenten gegeneinander antreten
         *      alle gegen alle
         *          nur a vs b oder auch b vs a (http://www.turnier-editor.de/ nur obere hälfte der diagonale oder beide)
         *      oder paarweise turniersystem wo nur gewinner gegeneinander spielen?
         *          was passiert mit ungeradem letzten spieler
         *              disqualifikation
         *              automatisch auftsieg in nächste runde ohne sieg wertung
         *              automatisch auftsieg in nächste runde mit sieg wertung
         *
         * ++++++++++++++++++
         *
         * TimeMeasurement:
         * {@link System#currentTimeMillis()} - Zeit seit 1.1.1970 in mS. Abhängig von Zeitzone usw - WallClockTime?
         * {@link System#nanoTime()} - Zeitstempel im System in nS
         *
         * Code Edited to implement TS
         * {@link XArenaMenu#XArenaMenu(Arena, JFrame)}
         *
         *
         * Code Added to implement TS
         * {@link XArenaMenu#generateTournamentMenu()}
         * @see TournamentSystem.TournamentSystemGUI
         * @see TournamentSystem.TSAgent
         * @see TournamentsystemGUI2
         *
         */
    }
}
