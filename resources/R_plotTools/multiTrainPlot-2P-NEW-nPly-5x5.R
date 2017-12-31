# 
# This plot shows for 5x5 Hex that different nPly wrappers (MaxN) yield better results with
# increasing nPly.
#
# Needs more than 5 runs (e.g. 20) to be statistically reliable.
#
# More details are found in TR-TDNTuple.tex.
# 
library(ggplot2)
library(grid)
source("summarySE.R")

path <- "../../agents/Hex/05/csv/"; 
limits=c(0.0,1.0); errWidth=3000;

filenames=c ( "TDNT2_NEW_2P-0ply.csv"    # 5 runs: 20 min (_PAR version)
             #,"TDNT2_NEW_2P-1ply.csv"   # 5 runs: 27 min (no _PAR version)
             ,"TDNT2_NEW_2P-3ply.csv"    # 5 runs: 42 min, elapsed 63 min (no _PAR version)
             ,"TDNT2_NEW_2P-5ply.csv"    # 5 runs: 155 min (no _PAR version)
            )
# other pars: alpha=0.2, eps = 1.0 ... 0.2, ChooseStart01=T, LearnFromRM=F, VER_3P=F, NEW_2P=T
# 20 runs, 150.000 training episodes. 
# evalMode= 0 (evalQ) is from default start state against MCTS, 
# evalMode=10 (evalT) is from 6 different start states against MCTS. 

PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, na.string="-1", dec=".",skip=2)
  df$run <- as.factor(df$run)
  df <- df[,setdiff(names(df),"trnMoves")]
  #if (k==1) df <- cbind(df,actionNum=rep(0,nrow(df)))
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(-1,0)) #+
      #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  lambdaCol = switch(k
                    ,rep(0,nrow(df))
                    #,rep(1,nrow(df))
                    ,rep(3,nrow(df))
                    ,rep(5,nrow(df))
  )
  #browser()
  dfBoth <- rbind(dfBoth,cbind(df,nPly=lambdaCol))
                  
}

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="evalQ", groupvars=c("gameNum","nPly"))
tgc1 <- cbind(tgc1,evalMode=rep(0,nrow(tgc1)))
names(tgc1)[4] <- "eval"  # rename "evalQ"
tgc2 <- summarySE(dfBoth, measurevar="evalT", groupvars=c("gameNum","nPly"))
tgc2 <- cbind(tgc2,evalMode=rep(10,nrow(tgc2)))
names(tgc2)[4] <- "eval"  # rename "evalT"
#tgc <- rbind(tgc1,tgc2)
#tgc <- tgc1
tgc <- tgc2
tgc$nPly <- as.factor(tgc$nPly)
tgc$evalMode <- as.factor(tgc$evalMode)

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(3000) # move them 300 to the left and right

q <- ggplot(tgc,aes(x=gameNum,y=eval,colour=nPly,linetype=evalMode))
q <- q+geom_errorbar(aes(ymin=eval-se, ymax=eval+se), width=errWidth, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+scale_y_continuous(limits=limits) 
q <- q+labs(title="5x5 Hex, TD-Ntuple-2")
#q <- q+guides(colour = guide_legend(reverse = TRUE))
plot(q)

