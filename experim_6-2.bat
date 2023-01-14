:: This script repeats the experiment leading to Figure 12 (Sec. 6.2) in [Konen23a] "Towards Learning Rubik’s Cube with N-tuple-based Reinforcement 
:: Learning". Results might slightly differ due to random fluctuations, but should be statistically similar to Figure 12.
::
:: In the properties file props_batch.txt the property trainOutDir should be %train_out_dir% and this directory should be initially empty.
:: Estimated computation time: 9h, if run on a standalone standard CPU. Of course, some of the calls can be parallelized on different cores.
:: Results (in directory agents\RubiksCube\***_STICKER2_§§\ with *** = 2x2x2 or 3x3x3 and §§ = AT or QT): 
::    - evaluation results (solved rates) in the resp. game-dependent subdirectories in files csv\-$csv_***_?TM.

set train_out_dir=multiTrain2
set csv_2x2_HTM=mRubiks2x2-MWrap[TCL4-p13-ET16]-Jan23-p1-13-EE50.csv
set csv_2x2_QTM=mRubiks2x2-MWrap[TCL4-p16-ET20]-Jan23-p1-16-EE50.csv
set csv_3x3_HTM=mRubiks3x3-MWrap[TCL4-p9-ET13]-Sep22-p1-9-EE50.csv
set csv_3x3_QTM=mRubiks3x3-MWrap[TCL4-p13-ET16]-Sep22-p1-13-EE50.csv

@echo %cd%
@echo %train_out_dir%
@echo %csv_2x2_HTM%
@echo %csv_2x2_QTM%
@echo %csv_3x3_HTM%
@echo %csv_3x3_QTM%

:: --- train the agents from stubs ---
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 1 TCL4-p13-ET16-3000k-60-7t-stub.agt.zip 1 3000000 experim_04_02.csv 2x2x2 STICKER2 HTM
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 1 TCL4-p16-ET20-3000k-60-7t-stub.agt.zip 1 3000000 experim_04_02.csv 2x2x2 STICKER2 QTM
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 1 TCL4-p9-ET13-3000k-120-7t-stub.agt.zip 1 3000000 experim_04_02.csv 3x3x3 STICKER2 HTM
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 1 TCL4-p13-ET16-3000k-120-7t-stub.agt.zip 1 3000000 experim_04_02.csv 3x3x3 STICKER2 QTM

:: --- evaluate the trained agents ---
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 10 %train_out_dir%\TCL4-p13-ET16-3000k-60-7t_00.agt.zip 3 300 %csv_2x2_HTM% 2x2x2 STICKER2 HTM
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 10 %train_out_dir%\TCL4-p16-ET20-3000k-60-7t_00.agt.zip 3 300 %csv_2x2_QTM% 2x2x2 STICKER2 QTM
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 10 %train_out_dir%\TCL4-p9-ET13-3000k-120-7t_00.agt.zip 3 300 %csv_3x3_HTM% 3x3x3 STICKER2 HTM
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 10 %train_out_dir%\TCL4-p13-ET16-3000k-120-7t_02.agt.zip 3 300 %csv_3x3_QTM% 3x3x3 STICKER2 QTM

