#
# **** These are new results with TDNTuple3Agt from January 2019 ****
#
# This script shows results for ConnectFour in the TCL-case with various TD-settings:
#   H0.01:  horizon cut at 0.01 in the eligibility traces
#   HOR40:  horizon 40 (plies)
#   RESET: reset eligibility trace on random move instead of standard elig traces
# It compares the ternary version (target as ternary term finished?r:gamma*V) with the
# non-ternary TD-version (r + gamma*V) [Switch TERNARY in TDNTuple2Agt].
# 
library(ggplot2)
library(grid)
source("summarySE.R")

PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
USEGAMESK=T       # if =T: use x-axis variable 'gamesK' instead of 'gameNum'  
USEEVALT=T        # if =T: use evalT measure; if =F: use evalQ measure

wfac = ifelse(USEGAMESK,1000,1);
gamesVar = ifelse(USEGAMESK,"gamesK","gameNum")
#evalVar = ifelse(USEEVALT,"evalT","evalQ")
evalStr = ifelse(USEEVALT,"eval AlphaBeta","eval MCTS")
#evalStr = "eval score" #ifelse(USEEVALT,"eval AlphaBeta","eval MCTS")
path <- "../../agents/ConnectFour/csv/"; limits=c(-1.0,1.0); errWidth=20000/wfac;

filenames=c("multiTrain_TCL-EXP-NT3-al50-lam016-500k-HOR001-T-epsfin0.csv"
           ,"multiTrain_TCL-EXP-NT3-al50-lam025-500k-HOR001-T-epsfin0-V2.csv"
           #,"multiTrain_TCL-EXP-NT3-al50-lam036-500k-HOR001-T-epsfin0.csv"
           #,"TERNARY/multiTrain_TCL-EXP-al20-lam05-500k-HOR001-single-T9b.csv"
           #,"nonTERNA/multiTrain_TCL-EXP-al20-lam05-500k-HOR001-single-9b.csv"
           #,"TERNARY/multiTrain_TCL-EXP-al50-lam05-500k-HOR40-T.csv"
           #,"nonTERNA/multiTrain_TCL-EXP-al50-lam05-500k-HOR40.csv"
           #,,"TERNARY/multiTrain_TCL-EXP-al20-lam05-500k-HOR001-RESET-T.csv"
           #,"nonTERNA/multiTrain_TCL-EXP-al20-lam05-500k-HOR001-RESET.csv"
           #,"TERNARY/multiTrain_TCL-EXP-al20-lam05-500k-HOR010-T.csv"
           #,"multiTrain_TCL-EXP-al20-lam05-500k-HOR010.csv"
           #,"multiTrain_TCL-EXP-al20-lam05-500k-HOR001-RESET.csv"
           #,"multiTrain_TCL-EXP-al20-lam05-500k-HOR001-noSYM.csv"
           #,"multiTrain_TCL-EXP-al50-lam05-500k-HOR001.csv"
           #,"multiTrain_TCL-EXP-al10-lam06-500k-eps0025.csv"
           )
# other pars: eps = 0.1->0.0, gamma = 1.0, ChooseStart01=F, NORMALIZE=F, SIGMOID=tanh, 
# LEARNFROMRM=F, MODE_3P==2, fixed ntuple mode 1: 70 8-tuples. TC_INIT=1e-4, TC_EXP
# with TC beta =2.7, rec.weight-change accumulation.500.000 training games, 10 runs.
# evalMode= 0 (evalQ) is from default start state against MCTS, 
# evalMode= 3 (evalT) is from default start state against AlphaBeta. 

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, dec=".",skip=2)
  df$run <- as.factor(df$run)
  df <- df[,setdiff(names(df),c("trnMoves","elapsedTime", "movesSecond"))]
  #if (k==1) df <- cbind(df,actionNum=rep(0,nrow(df)))
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(-1,1)) #+
      #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  lambdaCol = switch(k
                    ,rep("0.16",nrow(df))   
                    ,rep("0.25",nrow(df))   
                    ,rep("0.36",nrow(df))   
                    ,rep("HOR40",nrow(df))
                    ,rep("HOR40",nrow(df))
                    ,rep("RESET",nrow(df))
                    ,rep("RESET",nrow(df))
                    ,rep("TERNA",nrow(df))
                    ,rep("TERNA",nrow(df))
                    ,rep("H0.10-T",nrow(df))
                    ,rep("H0.10-T",nrow(df))
                    ,rep("AL5.0-T",nrow(df))
                    #,rep("TERNA-RES",nrow(df))
                    #,rep("noSYM",nrow(df))
                    #,rep("al50",nrow(df))
                    #,rep("eps0.025",nrow(df))
                    ,rep(0.80,nrow(df))
                    ,rep(0.90,nrow(df))
                    #,rep(0.2,nrow(df))
                    #,rep(0.205,nrow(df))
                    )
  targetModeCol = switch(k
                       ,rep("TERNA",nrow(df))
                       ,rep("TERNA",nrow(df))
                       ,rep("TERNA",nrow(df))
                       ,rep("TD",nrow(df))
                       ,rep("new",nrow(df))
                       ,rep("old",nrow(df))
                       ,rep("new",nrow(df))
                       ,rep("old",nrow(df))
                       ,rep("new",nrow(df))
                      )
  #browser()
  dfBoth <- rbind(dfBoth,cbind(df,lambda=lambdaCol,targetMode=targetModeCol))
                  
}

# This defines a new grouping variable  'gamesK':
#       games                            gamesK
#       10000,20000,30000,40000,50000  -->   50
#       60000,70000,80000,90000,100000 -->  100
#       ...                            -->  ...
# If USEGAMESK==T, then the subsequent summarySE calls will group 'along the x-axis'
# as well (all measurements with the same gamesK-value in the same bucket)
dfBoth <- cbind(dfBoth,gamesK=50*(ceiling(dfBoth$gameNum/50000)))

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="evalQ", groupvars=c(gamesVar,"lambda","targetMode"))
tgc1 <- cbind(tgc1,evalMode=rep("MCTS",nrow(tgc1)))
names(tgc1)[5] <- "eval"  # rename "evalQ"
tgc2 <- summarySE(dfBoth, measurevar="evalT", groupvars=c(gamesVar,"lambda","targetMode"))
tgc2 <- cbind(tgc2,evalMode=rep("AB",nrow(tgc2)))
names(tgc2)[5] <- "eval"  # rename "evalT"
tgc <- rbind(tgc1,tgc2)
#tgc <- tgc2
tgc$lambda <- as.factor(tgc$lambda)
tgc$targetMode <- as.factor(tgc$targetMode)

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(3000/wfac) # move them 3000 to the left and right

if (USEGAMESK) {
  q <- ggplot(tgc,aes(x=gamesK,y=eval,shape=lambda,linetype=lambda,color=evalMode))
  q <- q + xlab(bquote(paste("games [*",10^3,"]", sep=""))) + ylab(evalStr)
} else {
  q <- ggplot(tgc,aes(x=gameNum,y=eval,colour=lambda,linetype=lambda))
}
q <- q+geom_errorbar(aes(ymin=eval-se, ymax=eval+se), width=errWidth, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+scale_y_continuous(limits=limits) 
q <- q+guides(colour = guide_legend(reverse = TRUE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  

plot(q)

