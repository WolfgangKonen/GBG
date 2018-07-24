package controllers.TD.ntuple2;

import games.StateObservation;
import tools.Types;

public interface ZValue {
	public double calculate(   StateObservation so, Types.ACTIONS act, 
							   StateObservation refer, boolean silent);
}
