#
# **** These are Aug'2021 results with TDNTuple3Agt on C4. The wrapped agent is  ****
#         TCL-EXP-NT3-al37-lam000-6000k-epsfin0.agt.zip
#      with MCTSWrapper and different #iterations.
#      MCTS params: 10.000 iterations, tree depth 40, useNormalize=false (stronger MCTS)
# ****
#
# This script shows results from a competition where MCTS_10000 (1st) vs. opponent (2nd)
# Opponent = MCTSWrapper[TCL3] or = AB-DL, where the AB-DL results were added to csv by hand
#   
# [If either of the opponents plays first, it always wins, this is uninteresting.]
# 
library(ggplot2)
library(grid)
source("~/GitHub/GBG/resources/R_plotTools/summarySE.R")

PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run

wfac = 1;
errWidth=20/wfac;
evalStr = "agent win rate"
path <- "~/GitHub/GBG/agents/ConnectFour/csv/"; 
Ylimits=c(0.0,1.05); 
#Xlimits=c(0,100000); # c(400,6100) (-/+100 to grab position-dodge-moved points)

filenames=c("mCompeteMCTS-vs-MWrap-25runs-normF.csv"  # MCTSWrapperAgentTest.C4_vs_MCTS_Test, Aug'2021, useNormalize=false
)

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, dec=".",sep=";",skip=2)
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
  
  #browser()
  dfBoth <- rbind(dfBoth,df)
}
names(dfBoth)[8] <- "agent"

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfBoth, measurevar="winrate", groupvars=c("iterMWrap","EPS","agent"))
tgc$EPS <- as.factor(tgc$EPS)

# The errorbars may overlap, so use position_dodge to move them horizontally
#pd <- position_dodge(1000/wfac) # move them 1000/wfac to the left and right


q <- ggplot(tgc,aes(x=iterMWrap,y=winrate,colour=agent,shape=agent))
q <- q+labs(title="MCTS vs. various agents")
q <- q+geom_errorbar(aes(ymin=winrate-se, ymax=winrate+se), width=errWidth) #, position=pd)
#q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+geom_line(size=1.0) + geom_point(size=3.0)
q <- q+scale_y_continuous(limits=Ylimits) + ylab(evalStr)
q <- q+guides(colour = guide_legend(reverse = TRUE))
q <- q+guides(shape = guide_legend(reverse = TRUE))
q <- q+guides(linetype = guide_legend(reverse = TRUE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.5)))    # bigger title text  

plot(q)
pdffile="C4-MCTS-vs-Opponent-NEW.pdf"
ggsave(pdffile, width = 8.04, height = 4.95, units = "in")
cat(paste("Plot saved to",pdffile))
