#
# **** These are April'2022 results with MCTS-wrapped TDNTuple4Agt on Othello. 
#      In contrast to former runs we have MCTS in the training loop.
#      To keep runtimes managable, we run only 12.000 training episodes for each agent:
#         W-0:      no MCTS iterations during training
#         W-100:   100 MCTS iterations during training
#      When evaluating against Edax, we use always 10.000 MCTS iterations
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

filenames=c(
            "multiCompeteOthello-W100.csv",
            "multiCompeteOthello-W0-12k.csv",
            "multiCompeteOthello-W1000-12k.csv"
            # generated with >GBGBatch Othello 6 ...
)
agroup = c("W100","W0","W1000")   #"W10000","W1000",
pdffile="MCTSWrap-W100-12k.pdf"
        
  
dfAll = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, skip=2, dec=".",sep=";")
  df <- cbind(df,agentGroup=rep(agroup[k],nrow(df)))
  dfAll <- rbind(dfAll,df)
}
#dfAll = cbind(dfAll,agentGroup=rep("TCL-base",nrow(dfAll)))
#dfAll$agentGroup = as.character(dfAll$agentGroup)
#dfAll[dfAll$iterMWrap==10000,c("agentGroup")] <- "TCL-wrap"
#dfAll[dfAll$agtFile=="MCTS-10k",c("agentGroup")] <- "MCTS-10k"
dfAll <- dfAll[dfAll$iterMWrap==10000,]
dfAll <- dfAll[dfAll$agtFile!="MCTS-10k",]



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
#q <- q+geom_errorbar(aes(ymin=winrate-se, ymax=winrate+se), width=errWidth) #, position=pd)
q <- q+labs(title="same training episodes (12000)")
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
ggsave(pdffile, width = 8.04, height = 4.95, units = "in")
cat(paste("Plot saved to",pdffile))
