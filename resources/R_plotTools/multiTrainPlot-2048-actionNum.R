library(ggplot2)
library(grid)
source("summarySE.R")

path <- "../../agents/2048/csv/"
filenames=c("multiTrain-1run-100k_V2.csv"
           ,"multiTrain-1run-200k_V2.csv"
           #,"multiTrain-RewardGameScore.csv"#,"multiTrain-RewardGameScore-OLD.csv"
           #,"multiTrain-RewardGameSc-3P.csv"
           ) 
titnames=c("Reward CumTiles","Reward GameScore")
PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, na.string="-1", dec=".",skip=2)
  df$run <- as.factor(df$run)
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + ggtitle(titnames[k]) +
      scale_y_continuous(limits=c(0,37000)) 
    plot(q)
  }
  
  afterCol = switch(k
                    ,rep("100k",nrow(df))
                    ,rep("200k",nrow(df))
                    ,rep("game score",nrow(df))
                    #,rep("game score OLD",nrow(df))
                    ,rep("game sc 3P",nrow(df))
                    )
  dfBoth = rbind(dfBoth,cbind(df,REWARD=afterCol))
}

# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfBoth, measurevar="evalQ", groupvars=c("gameNum","REWARD","actionNum"))
tgc$REWARD <- as.factor(tgc$REWARD)

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(100) # move them 100 to the left and right

q <- ggplot(tgc,aes(x=actionNum,y=evalQ,colour=REWARD))
#q <- q+geom_errorbar(aes(ymin=evalQ-se, ymax=evalQ+se), width=300, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
#q <- q+geom_line()
q <- q+scale_y_continuous(limits=c(0,120000)) 
#q <- q+guides(colour = guide_legend(reverse = TRUE))
plot(q)

