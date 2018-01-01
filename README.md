# GBG 

## Introduction
**General board game (GBG)** playing and learning is a fascinating area in the
intersection of machine learning, **artificial intelligence** and game playing. It is
about how computers can learn to play games not by being programmed but by
gathering experience and learning by themselves (self-play).

A common problem in game playing is the fact, that each time a new game is tackled,
the AI developer has to undergo the frustrating and tedious procedure to
write adaptations of this game for all agent algorithms. The motivation for GBG is to provide a framework for educational and research purposes which makes it relatively easy for students or researchers to code new board games and apply all agent algorithms to it. Likewise, AIs can be compared to each other in **game competitions** and they can be evaluated on a variety of games. 

GBG is a framework for **General Board Game** playing consisting of classes and interfaces which abstracts the common processes in board game playing and learning. GBG is written in Java. GBG is suitable for arbitrary 
1-player, 2-player and n-player board games. It provides a set of agents (**AI**’s) which can be applied to any such game. 
New games can be coded following certain interfaces and then all agents of the GBG framework are available for that game. 
New AI agents can be coded following certain other interfaces. Then, the new agent can be tested on all 
games implemented in the GBG framework. It can be compared with all other AI agents in the GBG framework. 

For a quick overview of GBG see the [GBG Wiki](https://github.com/WolfgangKonen/GBG/wiki).

For an in-depth description of classes and interfaces in GBG see the [technical report on GBG [Konen2017]](resources/TR-GBG.pdf). This technical report describes in tutorial form 
the set of interfaces, abstract and non-abstract classes which help to standardize and implement
those parts of board game playing and learning that otherwise would be tedious and repetitive parts in coding. 
Alternatively, you can reach the technical report after starting the GBG framework via  `Help - Show TR-GBG.pdf`

[[https://github.com/WolfgangKonen/GBG/resources/figArenaTrain.png|alt=octocat]]
