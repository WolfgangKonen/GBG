package TournamentSystem;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.*;

import javax.swing.*;

/**
 * This class is used to store some dev notes of the GBG-TS.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
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
         *
         * TimeMeasurement:
         * {@link System#currentTimeMillis()} - Zeit seit 1.1.1970 in mS. Abhängig von Zeitzone usw - WallClockTime?
         * {@link System#nanoTime()} - Zeitstempel im System in nS
         *
         */
    }
}
