#
# **** These are Nov'2021 results with TDNTuple4Agt on Othello. 
#      TCL-wrap: 10 wrapped agents   
#         multiTrain/TCL4-100_7_250k-lam05_P4_nPly2-FAm_A_0*.agt.zip with *=0,1,...,9
#      with MCTSWrapper and 10.000 iterations in each move
#      TCL-base: the same 10 agents, but no wrapper
#      MCTS-i10k: MCTS with 10.000 iterations, but no TCL
# ****
#
# This script shows that TCL-wrap is clearly better than either TCL-base or MCTS-i10k.
# 
# TCL-wrap results are obtained with EPS=+1e-8.
# 
library(ggplot2)
library(grid)
source("~/GitHub/GBG/resources/R_plotTools/summarySE.R")

wfac = 1000;
errWidth=0.2;
xlabel = "Edax level"
ylabel = "win rate"
path <- "~/GitHub/GBG/agents/Othello/csv/"; 
Ylimits=c(0.0,1.0); 
Xlimits=factor(1:9); 

filenames=c("multiTrainOthello-10Agents.csv" 
            # generated with >GBGBatch Othello 5 ...
)

  
dfAll = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, skip=2, dec=".",sep=";")
  dfAll <- rbind(dfAll,df)
}
dfAll = cbind(dfAll,agentGroup=rep("TCL-base",nrow(dfAll)))
dfAll$agentGroup = as.character(dfAll$agentGroup)
dfAll[dfAll$iterMWrap==10000,c("agentGroup")] <- "TCL-wrap"
dfAll[dfAll$agtFile=="MCTS-10k",c("agentGroup")] <- "MCTS-10k"



tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfAll, measurevar="winrate", groupvars=c("dEdax","EPS","agentGroup")) 
tgc$EPS <- as.factor(tgc$EPS)
tgc <- tgc[tgc$EPS==1e-8,]
#tgc <- tgc[tgc$EPS==0,]
#tgc <- tgc[tgc$EPS==-1e-8,]

# If errorbars overlap, use position_dodge to move them horizontally
#pd <- position_dodge(1000/wfac) # move them 1000/wfac to the left and right


q <- ggplot(tgc,aes(x=dEdax,y=winrate,colour=agentGroup,shape=agentGroup)) #,linetype=agentGroup))
q <- q+geom_errorbar(aes(ymin=winrate-se, ymax=winrate+se), width=errWidth) #, position=pd)
#q <- q+labs(title="MCTSWrap(TCL) vs. Edax")
#q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+geom_line(size=1.0) + geom_point(size=3.0)
q <- q+scale_x_discrete(limits=Xlimits) 
q <- q+scale_y_continuous(limits=Ylimits) + xlab(xlabel) + ylab(ylabel)
q <- q+guides(colour = guide_legend(reverse = TRUE))
q <- q+guides(shape = guide_legend(reverse = TRUE))
q <- q+guides(linetype = guide_legend(reverse = TRUE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.5)))    # bigger title text  

plot(q)
pdffile="MCTSWrap-TCL10.pdf"
ggsave(pdffile, width = 8.04, height = 4.95, units = "in")
cat(paste("Plot saved to",pdffile))
