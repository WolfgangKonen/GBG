#
# **** These are Jan'2023 results with MCTS-wrapped TDNTuple4Agt on Othello. 
#      In contrast to former runs we have MCTS in the training loop.
#      To keep runtimes managable, we run only with 0 or 100 iterations, but with
#      250.000 training episodes for each agent:
#         W-0:       no MCTS iterations during training (avg over 20 agents)
#         W-100:    100 MCTS iterations during training (avg over 10 agents)
#      When evaluating against Edax, we use always 10.000 MCTS iterations
# ****
#
# This script shows that 100 iterations with MCTS-in-the-training-loop are somewhat better
# for most levels, with a big jump upwards for level 8 (from 34% to 65%).
# Overall, the improvement is not dramatic, given that the computation time (from 2h to 2.5d) 
# has increased by a factor of 30.
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

EX0_STRING = "-EX0"  # either "-EX0", "-EX0-TEST", "-EX0-TST2", "-EX1", "-EX2" or ""
# if EX0_STRING=="", reproduce ToG-paper results
# if           =="EX0", take the repeated results, which show statistical fluctuations for the W100 case
# if           =="EX1", take the results for EXPLORATION_MODE==1 (sample proportional visit counts)
# if           =="EX2", take the results for EXPLORATION_MODE==2 (eps-greedy with eps=0.15)
# ("-EX0-TEST" & "-EX0-TST2" are just software checks, should be statistically the same as
# "-EX0" and as "")
pdffile=paste0("MCTSWrap",EX0_STRING,"-W100-250k.Jan23.pdf")


if (EX0_STRING=="") {
  filesW100="multiCompeteOthello-W100-250k-part0-3.csv"  # 18 agents
} else {
  filesW100=paste0("multiCompeteOthello",EX0_STRING,"-W100-250k.csv")
}
filesW100 = "multiOthello-Fig10.csv"               # 10 W100-EX0 agents, Jan'23
filenames=c(
            filesW100,
            #"multiTrainOthello-20Agents.csv"      # no iter during training (W0) for comparison
            "multiOthello-Fig04.csv"               # the same, but new results Jan'23  
            # generated with >GBGBatch Othello 6 ...
)
agroup = c("W100","W0")   

dfAll = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, skip=2, dec=".",sep=";")
  df <- cbind(df,agentGroup=rep(agroup[k],nrow(df)))
  dfAll <- rbind(dfAll,df)
}

dfAll <- dfAll[dfAll$iterMWrap==10000,]



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
q <- q+labs(title=paste("250,000 training episodes ",EX0_STRING)) 
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
