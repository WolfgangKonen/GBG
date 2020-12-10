package games.RubiksCube;

import controllers.PlayAgent;

public class EvalCubeParams {
    public int pMin=1;
    public int pMax=CubeConfig.pMax;
    public int epiLength=10;
    public int evalNmax=CubeConfig.EvalNmax;

    public EvalCubeParams(int pMin, int pMax, int epiLength, int evalNmax) {
        this.pMin=pMin;
        this.pMax=pMax;
        this.epiLength=epiLength;
        this.evalNmax=evalNmax;
    }

    public EvalCubeParams(PlayAgent pa) {
        //		epiLength = CubeConfig.EVAL_EPILENGTH; //50, 2*p; //(2*p>10) ? 2*p : 10;
        if (pa!=null) epiLength = pa.getParOther().getStopEval();
    }

    public EvalCubeParams(EvalCubeParams other) {
        this.pMin=other.pMin;
        this.pMax=other.pMax;
        this.epiLength=other.epiLength;
        this.evalNmax=other.evalNmax;
    }
}
