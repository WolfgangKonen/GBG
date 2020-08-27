package games.RubiksCube;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import games.RubiksCube.ColorTrafoMap.ColMapType;
import games.RubiksCube.CubeState.Twist;
import games.RubiksCube.CubeStateMap.CsMapType;

/**
 * This is a class for generating and representing <b>distance sets</b> for the cube.
 * <p>
 * We use an {@link ArrayList} for the representation of distance sets (and not a {@link HashSet},
 * which may seem more natural for a distance <em>set</em> D), since only with an {@link ArrayList}
 * we can easily pick a random element via D.get(index).
 */
public class CSArrayList extends ArrayList<CubeState> {
	public enum CSAListType {GenerateD0, GenerateD1, GenerateNext, GenerateNextColSymm};
	
	public CSArrayList() {
		super();
	}
	private CubeStateFactory csFactory = new CubeStateFactory();

	/**
	 * Generate either distance set D0 or D1
	 * 
	 * @param csaType either GenerateD0 or GenerateD1
	 */
	public CSArrayList(CSAListType csaType) {
		super();
		switch(csaType) {
		case GenerateD0:
			CubeState cS0 = csFactory.makeCubeState();
			this.add(cS0);
			break;
		case GenerateD1:
			for (int i=1; i<=3; i++) {
				for (int act1=0; act1<3; act1++) {
					CubeState cS1 = csFactory.makeCubeState();
					switch(act1) {
					case 0: cS1.UTw(i); break;
					case 1: cS1.LTw(i); break;
					case 2: cS1.FTw(i); break;
					}
					if (!this.contains(cS1)) 
						this.add(cS1);
				}
			}
			break;
		default: 
			throw new RuntimeException("CSAListType "+csaType+" not allowed!");
		}
	}

	
	/**
	 * Given the CSArrayList objects D and Dprev, which are the distance sets
	 * from stage p-1 and p-2, create in {@code this} the distance set for stage p.<br>
	 * The object constructed is an CSArrayList covering (part of) the next distance set for p 
	 * ("Dnext").
	 * <p>
	 * In more detail: We pick N random elements from D, for each of it we perform a random twist.
	 * If the resulting new state is not in D or Dprev, we add it to the distance set of stage p.
	 * If csaType==GenerateNext, we add only it; if csaType==GenerateNextColSymm, we add it and 
	 * all its color-symmetric states.
	 * <p>
	 * NOTE: If D or Dprev are not the complete distance sets from stage p-1 and p-2, the operation
	 * may not detect every twin, so that some of the states in the constructed object CSArrayList
	 * may be actually not in stage p, but in stage p-1 or p-2. 
	 * 
	 * @param csaType see 'Detail' above
	 * @param D		the distance set we use as base
	 * @param Dprev the distance set below (previous to) D
	 * @param N		how many distinct elements to pick from D (N may not be larger than D.size())
	 * @param tintList	additional output information, see {@link TupleInt} 
	 * @param silent
	 * @param doAssert	do assertions (time consuming), if true 
	 * 
	 * @see TupleInt 
	 */
	public CSArrayList(CSAListType csaType, CSArrayList D, CSArrayList Dprev, int N,
			ArrayList tintList, boolean silent, boolean doAssert, Random rand) {
		super();
		// Dcopy is just a copy of D from which we can safely remove the elements 
		// we already picked (in the for-n-loop below) without altering D:
		CSArrayList Dcopy = (CSArrayList) D.clone();
		Twist[] twist = {Twist.U,Twist.L,Twist.F};
		CubeStateMap hmRots = new CubeStateMap(CsMapType.AllWholeCubeRotTrafos);
		ColorTrafoMap hmCols = new ColorTrafoMap(ColMapType.AllColorTrafos);
		int twinCounter=0;
		int prevCounter=0;
		int currCounter=0;
		assert(N<=D.size()) : "Oops, N too big!";
		TupleInt tint;
		for (int n=0; n<N; n++) {
			if (n%200==0) System.out.print(".");
			if (n%(200*80)==0) System.out.println();
			// We use an ArrayList for the representation of D and Dcopy (and not a HashSet,
			// which may seem more natural for a distance set D), since only with an ArrayList 
			// we can easily pick a random element via Dcopy.get(index):
			int index = rand.nextInt(Dcopy.size());
			CubeState cS0 = (CubeState)Dcopy.get(index);
			// Each n shall pick a random, but different element from Dcopy. 
			// Therefore we remove from Dcopy, which starts as a clone of D, 
			// every element which has already been picked:
			Dcopy.remove(cS0);
//			System.out.println(cS0.twistSeq+"  "+n);
			int count=0;
			for (int i=1; i<=3; i++) {
				for (int act1=0; act1<3; act1++) {
					// do not twist again in the direction of lastTwist, this is redundant (stays 
					// in D or goes to Dprev)
					// (If cS0 is from a color transformation, we do not have the twist sequence, 
					// instead we have cS0.lastTwist==ID for 'not known'. Then we run through 
					// all 3 twists.)
					if (twist[act1]!=cS0.lastTwist) {
						CubeState cS1 = csFactory.makeCubeState(cS0);
						switch(twist[act1]) {
						case U: cS1.UTw(i); break;
						case L: cS1.LTw(i); break;
						case F: cS1.FTw(i); break;
						}
						HashSet set = new HashSet();
						switch(csaType) {
						case GenerateNextColSymm: 
							//
							// calculate all color-symmetric states for cS1, collect
							// in 'set' only the truly different CubeStates  
							CubeStateMap mapColSymm = hmCols.applyColSymm(cS1,hmRots);
							if (doAssert) assert(mapColSymm.countYgrHomeStates()==mapColSymm.size()) : 
								"not all color-symmetric states have ygr 'home'!";
							Iterator it1 = mapColSymm.entrySet().iterator(); 
						    while (it1.hasNext()) {
							    Map.Entry entry = (Map.Entry)it1.next();
							    set.add((CubeState)entry.getValue());	
					        } 	
							break;
						case GenerateNext: 
							//
							// add only cS1 to HashSet set. This is slower (we need a larger n), 
							// but useful, if we want to have the twistSeq for each state in this
							// (better characterization of duplicates)
							set.add(cS1);
							break;
						default: 
							throw new RuntimeException("CSAListType "+csaType+" not allowed!");
						}
						//
						// add the states in HashSet set 
						// -- if not yet present in Dnext, Dprev or D -- to Dnext (=this):
						if (this.contains(cS1)) {
							twinCounter++;
							if (csaType == CSAListType.GenerateNext) {
								CubeState twin = this.findTwin(cS1);
								if (!silent) {
									System.out.print("Duplicate paths to state "+cS1.toString()+" ! ");
									System.out.println(" cS1: "+cS1.twistSeq+", twin(Dnext): "+twin.twistSeq);
								}
								if (doAssert) {
									assert (cS1.assertTwistSequence()==true) : "Wrong twist sequence in cS1";
									assert (twin.assertTwistSequence()==true) : "Wrong twist sequence in twin";
								}
							}
							if (doAssert) this.assertSetInD(set,"Dnext");
							continue; // next for-loop
						}
						if (Dprev.contains(cS1)) { // add cS1-family only to Dnext, if it is NOT in Dprev
							// cS1 can happen to be in Dprev, if the base cS0 from D was a twin, and the 
							// lastTwist in the other twin was, say U1, and we have selected the action
							// U3, which yields in summary the twist U4 = Id, and thus the last twist 
							// vanishes and we have a state with only p-2 twists, i.e. from Dprev.
							prevCounter++;
							if (!silent) {
								System.out.print("This is an element of previous distance set: "+cS1.toString()+" ! ");
								System.out.println(" cS1: "+cS1.twistSeq);
							}
						    CubeState twin = Dprev.findTwin(cS1);
						    if (doAssert) Dprev.assertSetInD(set,"Dprev");
							continue; // next for-loop
						}
						if (D.contains(cS1)) {  // add cS1-family only to Dnext, if it is NOT in D
							// cS1 can happen to be in D, if the base cS0 from D was a twin, and we have
							// selected the action which was the lastTwist in the other twin. 
							currCounter++;
							if (doAssert) D.assertSetInD(set,"D");
							continue; // next for-loop
						}
					    Iterator it2 = set.iterator();
					    while (it2.hasNext()) {
						    CubeState cset = (CubeState)it2.next();
						    if (doAssert) assert(!this.contains(cset)) : "Oops, CubeState cset already present in Dnext";
						    this.add(cset);	
				        } 
					    tint = new TupleInt(6*n+count,set.size(),this.size(), prevCounter,currCounter,twinCounter);
					    tintList.add(tint);
					    count++;
					} // if (twist...)
				} // for (act1)
			} // for (i)
			
		} // for (n)
		
	} // CSArrayList(GenerateNext)
	
	/**
	 * Assert for each CubeState in {@code this}, that its twist sequence applied to the 
	 * default cube leads to its state.
	 * 
	 * @see CubeState#assertTwistSequence() 
	 */
	public void assertTwistSeqInArrayList() {
	    Iterator it2 = this.iterator();
	    while (it2.hasNext()) {
		    CubeState cset = (CubeState)it2.next();
		    boolean pred = cset.assertTwistSequence();
		    assert (pred==true) : "Error: sequence "+cset.twistSeq+" differs for CubeState "+cset.print();
        } 
	}
	
	/**
	 * Check if there is a twin to {@code other} in {@code this}. (A twin is another element in
	 * {@code this} having the same state).
	 * @param other
	 * @return the twin in {@code this}, if it is found, else {@code null}
	 */
	public CubeState findTwin(CubeState other) {
	    Iterator it2 = this.iterator();
	    while (it2.hasNext()) {
		    CubeState twin = (CubeState)it2.next();
		    if (twin.equals(other)) return twin;
        } 
	    System.err.println("Warning: findTwin could not get a match! Returning null.");
	    return null;	
	}
	
	/**
	 * just a sanity check: we assume that {@code set} is a set with all color-symmetric 
	 * states to some CubeState cS, and we know that cS is present in {@code this}.
	 * Then all other elements of {@code set} should be in {@code this} as well.
	 * @param set
	 * @param Dname 	a name for this (needed for assertion message)
	 */
	private void assertSetInD(HashSet set, String Dname) {
	    Iterator it2 = set.iterator();
	    while (it2.hasNext()) {
		    CubeState cset = (CubeState)it2.next();
		    assert(this.contains(cset)) : "Oops, CubeState cset not present in "+Dname+"!";
        } 

	}
	
	/**
	 *  TupleInt is just a class to store a tuple of int's with diagnostic information
	 *  about {@code this} (called by constructor CSArrayList(GenerateNext*,...)) <ul>
	 *  <li> <b>n</b> 			the for-loop counter
	 *  <li> <b>setSize</b> 	the size of the set just added
	 *  <li> <b>dSize</b>		the current size of {@code this}
	 *  <li> <b>prevCounter</b>	how many generated states belong to Dprev
	 *  <li> <b>currCounter</b>	how many generated states belong to D
	 *  <li> <b>twinCounter</b>	how many generated states have a twin in Dnext
	 *  </ul>
	 */
	public static class TupleInt {
		int n;
		int setSize;
		int dSize;
		int prevCounter;
		int currCounter;
		int twinCounter;
		
		public TupleInt(int n, int s, int d, int p, int c, int t) {
			this.n=n;
			this.setSize=s;
			this.dSize=d;
			this.prevCounter=p;
			this.currCounter=c;
			this.twinCounter=t;
		}		
	}
	
	public static void printTupleIntList(ArrayList<TupleInt> tintList) {
		Iterator it = tintList.iterator();
	    while (it.hasNext()) {
			DecimalFormat form = new DecimalFormat("000");
		    TupleInt tint = (TupleInt)it.next();
		    System.out.println(form.format(tint.n) + ", " + form.format(tint.setSize) + ", "+ tint.dSize 
		    		+ ", prevCounter="+tint.prevCounter+ ", currCounter="+tint.currCounter+", twinCounter="+tint.twinCounter );
        } 
		
	}

	public static void printLastTupleInt(ArrayList<TupleInt> tintList) {
		DecimalFormat form = new DecimalFormat("000");
		TupleInt tint = tintList.get(tintList.size()-1);
	    System.out.println(form.format(tint.n) + ", " + form.format(tint.setSize) + ", "+ tint.dSize 
	    		+ ", prevCounter="+tint.prevCounter+ ", currCounter="+tint.currCounter+", twinCounter="+tint.twinCounter );
	}

}
