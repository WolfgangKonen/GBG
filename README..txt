Starting the framework:
-----------------------
(e.g. in Eclipse)  Make a new Eclipse project with name SourceGBG and with build path SourceGBG/GBG (the directory containing src/ and lib/)

1) Project Properties - Java Build Path - Libraries - Add Library... - JCommon & JFreeChart
2) Project Properties - Java Build Path - Libraries - Add JARs... - SourcGBG/lib/commons-compress-1.9
3) Start the GUI: LaunchTrainTTT.java - Run - Run As... - Java Application
4) To locate help files: Edit .classpath and add line
	<classpathentry excluding="**/*.java" kind="src" path="resources"/>
5) Optional: Edit Run Configuration: Arguments - VM args = "-ea" (enable assertions, if you want them)


Tipps and tricks: 
-----------------

If the help files in GBG/resources change: Go to project root (e.g. SourceGBG), F5, Build project. Then the help files will be automatically copied from GBG/resources to GBG/bin, and there they will be found by the program.

When training a big net, there can be a heap-memory crash.
How to cure: Set this option in "Run Dialog - Arguments - VM-Arguments"
	-Xmx512M
then the program gets 512 MB heap space and the error is gone.

If the JAR file commons-compress-1.9.jar is not on your system, download it from 
	 https://commons.apache.org/proper/commons-compress/download_compress.cgi
This is needed for Add JARs... above and for the imports in agentIO/LoadSaveTD.java:
	import org.apache.commons.compress.archivers.ArchiveException;
and similar

JFreeChart is under Eclipse normally available as User Library (see Add Library... above). 
If not, follow the tipps in 
	C:\installs\JFreeChart\jfreechart-1.0.17-install.pdf, p. 31-35.
If this PDF is not available locally, download it from
	http://download2.polytechnic.edu.na/pub4/sourceforge/j/jf/jfreechart/2. Documentation/1.0.17/jfreechart-1.0.17-install.pdf
See also hints in notes_java.docx


Recommended settings for TDSPlayer, backprop net:
	alpha init 0.5, alpha final 0.001, lambda 0.9, w/o sigmoid, epsilon init 0.1, epsilon final 0.0, NumEval 1000, Train games 20000, 10 runs:
		 Feature set 3, lambda 0.0: 	S(Minimax) = -0.285 +- 0.19
		 Feature set 3, lambda 0.8: 	S(Minimax) = -0.160 +- 0.14
		 Feature set 3, lambda 0.9: 	S(Minimax) = -0.070 +- 0.11	(* best lambda)
		 Feature set 3, lambda 0.95: 	S(Minimax) = -0.445 +- 0.17
		 Feature set 4, lambda 0.0: 	S(Minimax) = -0.070 +- 0.12
		 Feature set 4, lambda 0.8: 	S(Minimax) = -0.030 +- 0.03
		 Feature set 4, lambda 0.9: 	S(Minimax) = -0.025 +- 0.06	(** best ever)
		 Feature set 4, lambda 0.95: 	S(Minimax) = -0.330 +- 0.30
	so the best setting is Feature set 4, lambda = 0.8 or 0.9


RPROP recommended : LIN, with sigmoid
	eta_init 0.5, eta_minus 0.8, lambda 0.7, epsilon init 0.3, epsilon final 0.0, train games 10.000
often a sudden drop in performance on the last evaluation

