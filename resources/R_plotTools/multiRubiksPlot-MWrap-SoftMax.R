#
# **** These are new results with TDNTuple3Agt from June 2020 ****
# **** same as before, but for runs with different JARs
#
# This script shows results for Nim3P with extra rule in the TCL-case:
#   
# It compares mainly FARL with no-FARL. 
# 
library(ggplot2)
library(grid)
source("summarySE.R")

PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run

wfac = 1;
errWidth=20/wfac;
titleStr = "RubiksCube 2x2 with MCTSWrap(TCL), no SoftMax";
evalStr = "perc solved"
path <- "../../agents/RubiksCube/2x2x2_STICKER2_AT/csv/"; 
Ylimits=c(0.0,1.01); 
#Xlimits=c(0,100000); # c(400,6100) (-/+100 to grab position-dodge-moved points)

filenames=c(#"mRubiks2x2-MWrap-SoftMax.csv"    # MCTSWrapperAgentTest.rubiksCube2x2Test
            #"mRubiks2x2-MWrap-noSoftMax.csv" # MCTSWrapperAgentTest.rubiksCube2x2Test
            "mRubiks2x2-MWrap-noSoftMax-noLast.csv" # MCTSWrapperAgentTest.rubiksCube2x2Test
)
# w/o suffix: only the relevant cases EPS=1e-8, 0.0
# with suffix "-allEPS": EPS=[-1e-8,0,1e-8]. Are cases -1e-8 and 0 statistically equivalent? - Yes.
# with suffix "-allEPS-R": in reverse order [1e-8,0,-1e-8]. Are results statistically the same? - Yes.

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, dec=".",sep=",",skip=2)
  df$run <- as.factor(df$run)
  print(unique(df$run))
  df <- df[,setdiff(names(df),c("dEdax", "user1","user2"))]

  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=iterMWrap,y=winrate))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(0,1)) #+
      #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  targetModeCol = switch(k
                         ,rep("TD",nrow(df))
                         ,rep("TD",nrow(df))
                         ,rep("TERNA",nrow(df))
                         ,rep("TERNA",nrow(df))
  )
  #browser()
  dfBoth <- rbind(dfBoth,cbind(df,targetMode=targetModeCol))
}

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="winrate", groupvars=c("iterMWrap","EPS","targetMode"))
#qeval <- "MaxN"   #ifelse(length(grep("-ALm",filename)==1),"AB","MCTS")
#tgc1 <- cbind(tgc1,evalMode=rep(qeval,nrow(tgc1)))
tgc <- tgc1
#tgcT <- summarySE(dfBoth, measurevar="totalTrainSec", groupvars=c("iterMWrap","EPS","targetMode"))
tgc$targetMode <- as.factor(tgc$targetMode)
tgc$EPS <- as.factor(tgc$EPS)

# The errorbars may overlap, so use position_dodge to move them horizontally
#pd <- position_dodge(1000/wfac) # move them 1000/wfac to the left and right


q <- ggplot(tgc,aes(x=iterMWrap,y=winrate,colour=EPS,linetype=EPS))
q <- q+labs(title=titleStr)
q <- q+geom_errorbar(aes(ymin=winrate-se, ymax=winrate+se), width=errWidth) #, position=pd)
#q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+geom_line(size=1.0) + geom_point(size=2.0)
q <- q+scale_y_continuous(limits=Ylimits) + ylab(evalStr)
q <- q+guides(colour = guide_legend(reverse = TRUE))
q <- q+guides(linetype = guide_legend(reverse = TRUE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.5)))    # bigger title text  

plot(q)

