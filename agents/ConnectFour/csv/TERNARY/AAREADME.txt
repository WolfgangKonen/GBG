The "-single" files: both evaluators, AB (AlphaBeta) and MCTS run an evaluation with singleCompete where TDNTuple2Agt plays always 1st.

All with setting TERNARY==true in TERNARY/:

multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T.csv: 	first run, still with g3_EvalNPly: AB has 0.81 in the end
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T2.csv: 	same run, other random numbers: AB has 0.85 in the end
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T3.csv: 	new version with ZValue*: AB has only 0.70 in the end (!)
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T4.csv: 	again with g3_EvalNPly: AB has again 0.84 in the end (!)
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T5.csv: 	again with ZValue*, but keeping old randomSelect: AB has only 0.70 in the end (!)
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T6.csv: 	again with g3_EvalNPly, ZValue* in parallel calculated, but not used: AB has 0.63 in the end (was non-ternary by accident)
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T7.csv: 	again with g3_EvalNPly, ZValue* not calculated: AB has 0.87 in the end 
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T8.csv: 	new version with ZValue*, but old randomSelect: AB has 0.89 in the end 
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T9.csv: 	new ZValue*, new randomAgent, no ZValueSingle: AB has only 0.74 in the end (!) 
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T9a.csv: 	new ZValue*, new RandomAgent randomAgent, no ZValueSingle: AB has only 0.77 in the end (!) 
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-T9b.csv: 	new ZValue*, old randomSelect: AB has 0.83 in the end (!) 

All with setting TERNARY==false in nonTERNA/:

multiTrain_TCL-EXP-al50-lam05-500k-HOR001-single-9b.csv: 	new ZValue*, old randomSelect: AB has 0.66 in the end  
multiTrain_TCL-EXP-al20-lam05-500k-HOR001-single-9b.csv: 	new ZValue*, old randomSelect: AB has 0.35 in the end  


in comparison: 
multiTrain_TCL-EXP-al50-lam05-500k-HOR001-T.csv: 		former run, with g3_EvalNPly, single only for AB: AB has 0.90 in the end (!)