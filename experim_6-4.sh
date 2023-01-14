#!/bin/sh
# This script repeats the experiment leading to Figure 15 (Sec. 6.4) in [Konen23a] "Towards Learning Rubik’s Cube with N-tuple-based Reinforcement 
# Learning". Results might slightly differ due to random fluctuations, but should be statistically similar to Figure 15.
#
# In the properties file props_batch.txt the property trainOutDir should be $train_out_dir and this directory should be initially empty.
# Estimated computation time: several days, if run on a standalone standard CPU. Of course, some of the calls can be parallelized on different cores.
# Results are in directory agents\RubiksCube\3x3x3_STICKER2_QT\: 
#    - trained agents $train_out_dir,
#    - evaluation results (solved rates) in csv\$csv_out_file.

csv_out_file=symmIterSingle-batch.csv
train_out_dir=multiTrain2
echo $cd
echo $csv_out_file
echo $train_out_dir

# --- train the agents from stubs ---
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 1 TCL4-p13-ET16-3000k-120-7t-stub.agt.zip 2 3000000 experim_04_00.csv 3x3x3 STICKER2 QTM
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 1 TCL4-p13-ET16-3000k-120-7t-nsym08-stub.agt.zip 2 3000000 experim_04_08.csv 3x3x3 STICKER2 QTM
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 1 TCL4-p13-ET16-3000k-120-7t-nsym16-stub.agt.zip 2 3000000 experim_04_16.csv 3x3x3 STICKER2 QTM
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 1 TCL4-p13-ET16-3000k-120-7t-nsym24-stub.agt.zip 2 3000000 experim_04_24.csv 3x3x3 STICKER2 QTM

# --- evaluate the trained agents ---
java -ea -Xmx12096M -jar jartools/GBGBatch.jar RubiksCube 8 $train_out_dir 1 0 $csv_out_file 3x3x3 STICKER2 QTM