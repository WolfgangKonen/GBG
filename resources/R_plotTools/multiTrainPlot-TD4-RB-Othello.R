#
# **** These are results for Replay Buffer on Othello from Feb 2022 ****
# 
# **** generated with GBGBatch Othello 5 TCL4-100_7_100k-RB10000c0100b.agt.zip 10 -1 
# **** and batch5 uses int[] batchSizeArr = {0,100,200,400};
# **** see <GBG>\myDocs\UnderstandReplayBuffer.docx
# 
library(ggplot2)
library(grid)
source("summarySE.R")

PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
USEGAMESK=T       # if =T: use x-axis variable 'gamesK' instead of 'gameNum'  
MAPWINRATE=T      # if =T: map y-axis to win rate (range [0,1]); if =F: range [-1,1] 
EVALBENCH=T       # if =T: show win rate vs. BenchPlayer, =F: vs. HeurPlayer

wgroup = 4000
wfac = ifelse(USEGAMESK,wgroup,1);
gamesVar = ifelse(USEGAMESK,"gamesK","gameNum")
evalStr = ifelse(MAPWINRATE,ifelse(EVALBENCH,"win rate Bench","win rate Heur"),"success")
Ylimits=c(ifelse(MAPWINRATE,0.0,-1.0),ifelse(MAPWINRATE,1.0,1.0));
errWidth=1000/wfac;
path <- "../../agents/Othello/multiTrain-RB/"; 

filenames=c(
              "TCL4-100_7_100k-RB10000c0100b.csv"       # RB with capacity 10000, batch size 100
            , "TCL4-100_7_100k-RB10000c0050b.csv"       # RB with capacity 10000, batch size  50
)
# other pars: alpha=0.2, eps=0.2 ... 0.1, ChooseStart01=T, LearnFromRM=T, 100000 train episodes,
# lambda=0.5, horCut=0.1, NORM=F, sigmoid=tanh. TCL-EXP, 100 7-tuples, UseSymmetry=T.
# Win rates are when playing in all roles against BenchPlayer (EVALBENCH=T) or against
# HeurPlayer (EVALBENCH=F). In both cases, best win rate is 1.0.

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, dec=".",skip=2)
  df$run <- as.factor(df$run)
  df <- df[,setdiff(names(df),c("trnMoves","elapsedTime","movesSecond"))]
  #if (k==1) df <- cbind(df,actionNum=rep(0,nrow(df)))
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(-1,1)) #+
      #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  algoCol = switch(k
                    ,rep("TD4 no RB",nrow(df))
                    ,rep("TD4 RB1000",nrow(df))   # replay buffer, 1000/100
  )
  #browser()
  #dfBoth <- rbind(dfBoth,cbind(df,algorithm=algoCol))
  dfBoth <- rbind(dfBoth,df)
                  
}

# This defines a new grouping variable 'gamesK':
#       games                           gamesK
#       2000,4000,                      1
#       6000,8000,                      2
#       ...                             ...
# If USEGAMESK==T, then the subsequent summarySE calls will group 'along the x-axis'
# as well (all measurements with the same gamesK-value are in the same bucket)
dfBoth <- cbind(dfBoth,gamesK=1*(ceiling(dfBoth$gameNum/wgroup)))

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="evalQ", groupvars=c(gamesVar,"RB_batch"))
tgc1 <- cbind(tgc1,evalMode=rep("Bench",nrow(tgc1)))
names(tgc1)[4] <- "eval"  # rename "evalQ"
tgc2 <- summarySE(dfBoth, measurevar="evalT", groupvars=c(gamesVar,"RB_batch"))
tgc2 <- cbind(tgc2,evalMode=rep("Heur",nrow(tgc2)))
names(tgc2)[4] <- "eval"  # rename "evalT"
if(EVALBENCH) tgc <- tgc1 else tgc <- tgc2
#tgc <- tgc2
#tgc <- rbind(tgc1,tgc2)
tgc$RB_batch <- as.factor(tgc$RB_batch)
tgc$evalMode <- as.factor(tgc$evalMode)

if (MAPWINRATE) {
  tgc$eval = (tgc$eval+1)/2   # map y-axis to win rate (range [0,1])
  tgc$se = (tgc$se)/2
}


# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(30/wfac) # move them 3000 to the left and right

#browser()
if (USEGAMESK) {
  q <- ggplot(tgc,aes(x=gamesK,y=eval,colour=RB_batch))   #,linetype=evalMode
  q <- q + xlab(bquote(paste("episodes [*4*",10^3,"]", sep=""))) + ylab(evalStr)
} else {
  q <- ggplot(tgc,aes(x=gameNum,y=eval,colour=RB_batch)) #,linetype=evalMode
}
q <- q+geom_errorbar(aes(ymin=eval-se, ymax=eval+se), width=errWidth) #, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+scale_y_continuous(limits=Ylimits) 
#q <- q+guides(colour = guide_legend(reverse = TRUE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  

plot(q)

