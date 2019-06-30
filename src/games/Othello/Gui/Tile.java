package games.Othello.Gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import games.Arena;
import games.Othello.ConfigOthello;
import games.Othello.GameBoardOthello;
import games.Othello.StateObserverOthello;
import tools.Types;

/**
 * Representation of a single Tile of the game board.
 */
public class Tile extends JButton{
	
	private GameBoardOthello gb;
	private int index;
	private boolean isMarked = false;
	
	public Tile(GameBoardOthello gb, int i, int j) {
		setBackground(ConfigOthello.BOARDCOLOR);
		setMargin(new Insets(0,0,0,0));
		index = i * ConfigOthello.BOARD_SIZE + j;
		this.gb = gb;
		this.setPreferredSize(ConfigOthello.DIMENSIONTILE);
		addListener(i,j);
	}

	
	/**
	 * Setting the border to red if this tile is the last played move.
	 * @param isLastMove boolean if the {@link StateObserverOthello#getLastMove()()} last
	 *  index is this tile's {@code index}
	 */
	public void setBorder(boolean isLastMove) {
		if(isLastMove) this.setBorder(BorderFactory.createLineBorder(ConfigOthello.LASTMOVECOLOR, 4));
		else this.setBorder(BorderFactory.createLineBorder(ConfigOthello.NORMALBORDERCOLOR, 2));
	}
	
	/**
	 * Setting the border color to green if this tile is a valid placement for a disc this turn.
	 * @param b	boolean if the {@link StateObserverOthello#getAvailableActions()}
	 *  contains this tile's {@code index}.
	 */
	public void markAsPossiblePlacement(boolean b) {
		if(b) this.setBorder(BorderFactory.createLineBorder(ConfigOthello.POSSIBLEMOVECOLOR, 4));
		else this.setBorder(BorderFactory.createLineBorder(ConfigOthello.NORMALBORDERCOLOR, 2));
		this.setEnabled(b);
		isMarked = b;
	}
	
	
	public boolean isMarked() { return isMarked;}
	
	/**
	 * Adding actionlistener to each tile
	 * @param i	index of board
	 * @param j index of the board
	 */
	private void addListener(int i, int j)
	{
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Arena.Task aTaskState = gb.m_Arena.taskState;
				if(aTaskState == Arena.Task.PLAY) {
					gb.hGameMove(i,j); // Human play
				}else if( aTaskState == Arena.Task.INSPECTV) {
					gb.inspectMove(i,j); // Inspect
				}
			}
		});
	}
	

	
	
	
}
