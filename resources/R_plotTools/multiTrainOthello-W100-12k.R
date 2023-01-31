#
# **** These are April-Aug'2022 results with MCTS-wrapped TDNTuple4Agt on Othello. 
#      In contrast to former runs we have MCTS in the training loop.
#      To keep runtimes managable, we run only 12.000 training episodes for each agent:
#         W-0:       no MCTS iterations during training
#         W-100:    100 MCTS iterations during training
#         W-1000:  1000 MCTS iterations during training
#         W-10000:10000 MCTS iterations during training
#      When evaluating against Edax, we use always 10.000 MCTS iterations.
# ****
#
# This script shows that MCTS-in-the-training-loop does not reach in 12.000 episodes results
# comparable to MCTS-not-in-training-loop, but 250.000 episodes
#
# With PART = 1,2 or 3 we select between 4,6 or 8 agents for W-10000 (each run costs 11.5 
# days (!!) on lwivs48)
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

EX0_STRING = "-EX0"  # either "-EX0" or ""
# if EX0_STRING=="", reproduce ToG-paper results
# if EX0_STRING=="EX0", take the repeated results, which show statistical fluctuations for the W100 case

PART=3    # 1,2 or 3
numAgents=c("04agents","06agents","08agents")
filesW10000=c(
  "multiCompeteOthello-W10000-12k-part0.csv",    #  4 agents
  "multiCompeteOthello-W10000-12k-part0-1.csv",  #  6 agents
  "multiCompeteOthello-W10000-12k-part0-2.csv"   #  8 agents
)
filenames=c(
            filesW10000[PART],
            "multiCompeteOthello-W1000-12k.csv", # 10 agents
            paste0("multiCompeteOthello",EX0_STRING,"-W100.csv"),      # 10 agents
            "multiCompeteOthello-W0-12k.csv"     # 10 agents
            # generated with >GBGBatch Othello 6 ...
)
agroup = c("W10000","W1000","W100","W0")   
pdffile=paste0("MCTSWrap",EX0_STRING,"-W100-12k-",numAgents[PART],".pdf")
        
  
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
q <- q+labs(title=paste("same training episodes (12,000)",EX0_STRING))
#q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+geom_line(size=1.0) + geom_point(size=3.0)
q <- q+scale_x_discrete(limits=Xlimits) 
q <- q+scale_y_continuous(limits=Ylimits) + xlab(xlabel) + ylab(ylabel)
q <- q+guides(colour = guide_legend(reverse = F))
q <- q+guides(shape = guide_legend(reverse = F))
q <- q+guides(linetype = guide_legend(reverse = F))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.5)))    # bigger title text  

plot(q)
ggsave(pdffile, width = 8.04, height = 4.95, units = "in")
cat(paste("Plot saved to",pdffile))
