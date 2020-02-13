#
# **** These are new results with TDNTuple3Agt from January 2020 ****
#
# This script shows results for ConnectFour in the TCL-case with various 
# learning rates alpha \in {0.0, 2.5, 3.7, 5.0, 7.5, 10.0}.
# 
library(ggplot2)
library(grid)
source("summarySE.R")

PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
USEGAMESK=T       # if =T: use x-axis variable 'gamesK' instead of 'gameNum'  
USEEVALT=T        # if =T: use evalT measure; if =F: use evalQ measure
MAPWINRATE=T      # if =T: map y-axis to win rate (range [0,1]); if =F: range [-1,1]

wfac = ifelse(USEGAMESK,1000,1);
gamesVar = ifelse(USEGAMESK,"gamesK","gameNum")
evalStr = ifelse(USEEVALT,"eval AlphaBeta","eval MCTS")
evalStr = ifelse(MAPWINRATE,"win rate", evalStr)
path <- "../../agents/ConnectFour/csv/"; 
Ylimits=c(ifelse(MAPWINRATE,0.0,-1.0),1.0); errWidth=300000/wfac;
Xlimits=c(400,6100); # c(400,6100) (-/+100 to grab position-dodge-moved points)

filenames=c("multiTrainLambda-6000.csv"
            #"multiTrain_TCL-EXP-NT3-al37-lamSweep-750k-epsfin0-V12m.csv"
           )
# suffix -DLm: evalQ is MCTS (0), evalT is AB with Distant Losses DL (5),
# suffix -ALm: both evaluators AB, 1st one w/o DL (3), 2nd one with DL (5),
# in both cases the evaluators MCTS or AlphaBeta (AB) play 2nd --> ideal winrate is 1.0
# in both cases the 'm' means that runs were performed on maanbs05.
# suffix -noFA: no final adaptation RL (FARL) step.
#
# other pars: alpha = 3.7->3.7m eps = 0.1->0.0, gamma = 1.0, ChooseStart01=F, 
# NORMALIZE=F, SIGMOID=tanh, LEARNFROMRM=T, fixed ntuple mode 1: 70 8-tuples. 
# TC_INIT=1e-4, TC_EXP with TC beta =2.7, rec.weight-change accumulation. 
# 6.000.000 training games, 10 runs.
# 
# 10 runs for each lambda \in {0.00, 0.04, 0.09, 0.16, 0.25}.

  
dfBoth = data.frame()
k=1 #for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, dec=".",skip=2)
  df$run <- as.factor(df$run)
  df <- df[,setdiff(names(df),c("trnMoves","elapsedTime", "movesSecond","null","X","X.1"))]

  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(-1,1)) #+
      #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  lambdaCol = switch(k
                    ,rep("0.00",nrow(df))   
                    )
  targetModeCol = switch(k
                       ,rep("TD",nrow(df))
                      )
  #browser()
  dfBoth <- rbind(dfBoth,cbind(df,targetMode=targetModeCol))
#} // for (k)

# This defines a new grouping variable  'gamesK':
#       games                                     gamesK
#       100000,200000,300000,400000, 500000 -->    500
#       600000,700000,800000,900000,1000000 -->   1000
#       ...                                 -->   ....
# If USEGAMESK==T, then the subsequent summarySE calls will group 'along the x-axis'
# as well (all measurements with the same gamesK-value in the same bucket)
dfBoth <- cbind(dfBoth,gamesK=500*(ceiling(dfBoth$gameNum/500000)))

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="evalQ", groupvars=c(gamesVar,"lambda","targetMode"))
tgc1 <- cbind(tgc1,evalMode=rep("AB",nrow(tgc1)))
names(tgc1)[5] <- "eval"  # rename "evalQ"
tgc2 <- summarySE(dfBoth, measurevar="evalT", groupvars=c(gamesVar,"lambda","targetMode"))
tgc2 <- cbind(tgc2,evalMode=rep("AB-DL",nrow(tgc2)))
names(tgc2)[5] <- "eval"  # rename "evalT"
#tgc <- rbind(tgc1,tgc2)  # AB & AB-DL
tgc <- tgc2               # AB-DL only
tgc$lambda <- as.factor(tgc$lambda)
#tgc$alpha <- as.factor(tgc$alpha)
tgc$targetMode <- as.factor(tgc$targetMode)
if (MAPWINRATE) {
  tgc$eval = (tgc$eval+1)/2   # map y-axis to win rate (range [0,1]
  # z is just a control (manual comparison) that summarySE worked correctly
  z=aggregate(dfBoth$evalT,dfBoth[,c(gamesVar,"lambda","targetMode")],mean)
  z$x = (z$x+1)/2
}

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(100000/wfac) # move them 100000/wfac to the left and right

if (USEGAMESK) {
  q <- ggplot(tgc,aes(x=gamesK,y=eval,shape=evalMode,linetype=evalMode,color=lambda))
  q <- q + xlab(bquote(paste("games [*",10^3,"]", sep=""))) + ylab(evalStr)
  q <- q+scale_x_continuous(limits=Xlimits) 
} else {
  q <- ggplot(tgc,aes(x=gameNum,y=eval,colour=lambda,linetype=lambda))
}
q <- q+geom_errorbar(aes(ymin=eval-se, ymax=eval+se), width=3*errWidth, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+scale_y_continuous(limits=Ylimits) 
q <- q+guides(colour = guide_legend(reverse = TRUE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  

plot(q)

