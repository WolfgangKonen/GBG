# GBG 

## Introduction
GBG is a framework for **General Board Game** playing. GBG is written in Java. GBG is suitable for arbitrary 
1-player, 2-player and n-player board games. It provides a set of agents (**AI**’s) which can be applied to any such game. 
New games can be coded following certain interfaces and then all agents of the GBG framework are available for that game. 
New AI agents can be coded following certain other interfaces. Then, the new agent can be tested on all 
games implemented in the GBG framework. It can be compared with all other AI agents in the GBG framework. 

For an in-depth description of classes and interfaces in GBG see the [technical report on GBG](resources/TR-GBG.pdf). This tutorial describes 
the set of interfaces, abstract and non-abstract classes which help to standardize and implement
those parts of board game playing and learning that otherwise would be tedious and repetitive parts in coding. 
Alternatively, you can reach the technical report after starting the GBG framework via  `Help - Show TR-GBG.pdf`


## Starting the framework

(e.g. in Eclipse)  Make a new Eclipse project with build path GBG (this directory, containing src/ and lib/)

1. Project Properties - Java Build Path - Libraries - Add Library... - JCommon & JFreeChart
2. Project Properties - Java Build Path - Libraries - Add JARs... - SourceGBG/lib/commons-compress-1.9
3. Start the GUI: Right mouse on LaunchTrainTTT.java - Run - Run As... - Java Application
4. Optional, to locate help files: Edit .classpath and add line
```
	<classpathentry excluding="**/*.java" kind="src" path="resources"/>
```
5. Optional: Edit Run Configuration: Arguments - VM args = "-ea" (enable assertions, if you want them)


## Tipps and tricks: 

If the help files in GBG/resources change: Right mouse on project root, F5, Build project. Then the help files will be automatically copied from GBG/resources to GBG/bin, and there the program will find them.

When training a big net, there can be a heap-memory crash.
How to cure: Set this option in "Run Dialog - Arguments - VM-Arguments"
```
	-Xmx512M
```
then the program gets 512 MB heap space and the error is gone.

If the JAR file commons-compress-1.9.jar is not on your system, locate it from [lib/commons-compress-1.9](lib/commons-compress-1.9) or download it from 
	 [https://commons.apache.org/proper/commons-compress/download_compress.cgi](https://commons.apache.org/proper/commons-compress/download_compress.cgi).
This is needed for Add JARs... above and for the imports in `agentIO/LoadSaveTD.java`:
```
	import org.apache.commons.compress.archivers.ArchiveException;
```
and similar.

JFreeChart is under Eclipse normally available as User Library (see Add Library... above). 
If not, follow the tipps in [jfreechart-1.0.17-install.pdf](lib/jfreechart-1.0.19/jfreechart-1.0.17-install.pdf), p. 31-35.

If this PDF is not available locally, download it from [https://sourceforge.net/projects/jfreechart/files/2.%20Documentation/1.0.17/jfreechart-1.0.17-install.pdf](https://sourceforge.net/projects/jfreechart/files/2.%20Documentation/1.0.17/jfreechart-1.0.17-install.pdf)


