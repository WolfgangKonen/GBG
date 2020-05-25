package games.Sim;

import games.BoardVector;
import games.StateObservation;

public class BoardVectorSim extends BoardVector {

	StateObserverSim sim; 
	
	public BoardVectorSim(int[] bvec, StateObserverSim sim) {
		super(bvec);
		this.sim = sim;
	}

	public BoardVectorSim(int[] bvec, int[] aux, StateObserverSim sim) {
		super(bvec, aux);
		this.sim = sim;
	}
	
	public String toString() {
		String sout = "|";
		String str[] = new String[4]; 
		str[0] = "_"; str[1]="0"; str[2]="1";str[3]="2" ;
		
		for(int i=0, k=0; i < sim.getNumNodes() -1 ; i++) {
			for(int j = i+1; j < sim.getNumNodes(); j++) {
				sout = sout + str[bvec[k++]];
			}
			sout = sout + "|";
		}
		
 		return sout;		
	}

}
