library(ggplot2)
library(grid)
source("summarySE.R")

path <- "../agents/TicTacToe/csv/"; limits=c(-0.5,0); errWidth=300;
path <- "../agents/Hex/04/csv/"; limits=c(0,1.0); errWidth=3000;

filenames=c("multiTrain-withLearnFromRM.csv","multiTrain-noLearnFromRM.csv") 
PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, na.string="-1", dec=".",skip=2)
  df$run <- as.factor(df$run)
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(-1,0)) #+
    #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  learnCol = switch(k
                    ,rep("true",nrow(df))
                    ,rep("false",nrow(df))
                    )
  dfBoth = rbind(dfBoth,cbind(df,learnFromRM=learnCol))
}

# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfBoth, measurevar="evalQ", groupvars=c("gameNum","learnFromRM"))
tgc$learnFromRM <- as.factor(tgc$learnFromRM)

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(150) # move them 100 to the left and right

q <- ggplot(tgc,aes(x=gameNum,y=evalQ,colour=learnFromRM))
q <- q+geom_errorbar(aes(ymin=evalQ-se, ymax=evalQ+se), width=errWidth, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+scale_y_continuous(limits=limits) 
#q <- q+guides(colour = guide_legend(reverse = TRUE))
plot(q)

