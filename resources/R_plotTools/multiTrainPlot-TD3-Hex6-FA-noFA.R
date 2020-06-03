#
# **** These are new results with TDNTuple3Agt from May 2020 ****
# **** same as before, but for runs with different JARs
#
# This script shows results for Hex 6x6 in the TCL-case with various TD-settings:
#   
# It compares mainly FARL with no-FARL. 
# 
library(ggplot2)
library(grid)
source("summarySE.R")

PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
USEGAMESK=T       # if =T: use x-axis variable 'gamesK' instead of 'gameNum'  
USEEVALT=F        # if =T: use evalT measure; if =F: use evalQ measure for y-label
MAPWINRATE=T      # if =T: map y-axis to win rate (range [0,1]); if =F: range [-1,1]

wfac = ifelse(USEGAMESK,1000,1);
gamesVar = ifelse(USEGAMESK,"gamesK","gameNum")
#evalVar = ifelse(USEEVALT,"evalT","evalQ")
evalStr = ifelse(USEEVALT,"eval AlphaBeta","eval MCTS")
evalStr = ifelse(MAPWINRATE,"win rate", evalStr)
path <- "../../agents/Hex/06/csv/"; 
Ylimits=c(ifelse(MAPWINRATE,0.0,-1.0),1.0); errWidth=30000/wfac;
Xlimits=c(0,310); # c(400,6100) (-/+100 to grab position-dodge-moved points)

filenames=c(#"multiTrain-OLD.csv"  # GBGBatch-OLD.jar,branch master as of 2020-03-10
            # "multiTest-25-6-TCLid-FA-10m.csv"      # GBGBatch.jar
            #,"multiTest-25-6-TCLid-noFA-10m.csv"    # GBGBatch-noFA.jar
             "multiTest-20.csv"      # GBGBatch.jar
            ,"multiTest-noFA-20.csv"    # GBGBatch-noFA.jar
)
# evalQ is MCTS (0), evalT is MCTS with diff. starts (10),
# in both cases TD starts from positions which are theoretical wins --> ideal winrate is 1.0.
# suffix 'm' means that runs were performed on maanbs05.
# suffix -noFA: no final adaptation RL (FARL) step.
#
# other pars: alpha = 1.0->0.5, eps = 0.2->0.2, lambda=0.0, gamma = 1.0, ChooseStart01=T, 
# NORMALIZE=F, SIGMOID=tanh, LEARNFROMRM=T, random ntuples: 25 6-tuples. 
# TC_INIT=1e-4, TC_id, rec.weight-change accumulation. 
# 300.000 training games, 10 runs.

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, dec=".",skip=2)
  df$run <- as.factor(df$run)
  names(df)[7] <- "totalTrainSec"
  print(unique(df$run))
  df <- df[,setdiff(names(df),c("trnMoves", "movesSecond","lambda","null","X","X.1"))]
  #,"elapsedTime"
  #if (k==1) df <- cbind(df,actionNum=rep(0,nrow(df)))
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(-1,1)) #+
      #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  algoCol = switch(k
                   ,rep("  FA",nrow(df))   
                   ,rep("noFA",nrow(df))   
                   ,rep("SOWB",nrow(df))   
  )
  targetModeCol = switch(k
                         ,rep("TD",nrow(df))
                         ,rep("TD",nrow(df))
                         ,rep("TERNA",nrow(df))
                         ,rep("TERNA",nrow(df))
  )
  #browser()
  dfBoth <- rbind(dfBoth,cbind(df,algo=algoCol,targetMode=targetModeCol))
}

# This defines a new grouping variable  'gamesK':
#       games                                     gamesK
#       100000,200000,300000,400000, 500000 -->    500
#       600000,700000,800000,900000,1000000 -->   1000
#       ...                                 -->   ....
# If USEGAMESK==T, then the subsequent summarySE calls will group 'along the x-axis'
# as well (all measurements with the same gamesK-value in the same bucket)
dfBoth <- cbind(dfBoth,gamesK=10*(ceiling(dfBoth$gameNum/10000)))

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="evalQ", groupvars=c(gamesVar,"algo","targetMode"))
qeval <- "Mdiff"   #ifelse(length(grep("-ALm",filename)==1),"AB","MCTS")
tgc1 <- cbind(tgc1,evalMode=rep(qeval,nrow(tgc1)))
names(tgc1)[5] <- "eval"  # rename "evalQ"
tgc2 <- summarySE(dfBoth, measurevar="evalT", groupvars=c(gamesVar,"algo","targetMode"))
tgc2 <- cbind(tgc2,evalMode=rep("MdiffOLD",nrow(tgc2)))
names(tgc2)[5] <- "eval"  # rename "evalT"
#tgc <- rbind(tgc1,tgc2) # AB & MCTS
tgc <- tgc1              # AB only
tgcT <- summarySE(dfBoth, measurevar="totalTrainSec", groupvars=c(gamesVar,"algo","targetMode"))
z=aggregate(dfBoth$evalT,dfBoth[,c(gamesVar,"algo","targetMode")],mean)
tgc$algo <- as.factor(tgc$algo)
tgc$targetMode <- as.factor(tgc$targetMode)
if (MAPWINRATE) tgc$eval = (tgc$eval+1)/2   # map y-axis to win rate (range [0,1])

# The errorbars may overlap, so use position_dodge to move them horizontally
#pd <- position_dodge(1000/wfac) # move them 100000/wfac to the left and right

if (USEGAMESK) {
  q <- ggplot(tgc,aes(x=gamesK,y=eval,shape=evalMode,linetype=algo,color=evalMode))
  q <- q + xlab(bquote(paste("episodes [*",10^3,"]", sep=""))) + ylab(evalStr)
  q <- q+scale_x_continuous(limits=Xlimits) 
} else {
  q <- ggplot(tgc,aes(x=gameNum,y=eval,colour=algo,linetype=algo))
}
q <- q+geom_errorbar(aes(ymin=eval-se, ymax=eval+se), width=errWidth, position=pd)
#q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+geom_line(size=1.0) + geom_point(size=2.0)
q <- q+scale_y_continuous(limits=Ylimits) 
q <- q+guides(colour = guide_legend(reverse = FALSE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  

plot(q)

