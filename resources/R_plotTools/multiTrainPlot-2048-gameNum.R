# 
# This plot shows for 2048 different TD-n-tuple results for 100k, 200k, ... training episodes.
# 'actionNum' is the number of n-tuple weight updates (#episodes * avg. episode length).
#
# The first result is, that after 200k training games there is only little further
# increase. 
# The second result is that V2 and V3 are nearly identical: Doubling the learning rate
# from 0.2-->0.1 to 0.4-->0.2 does not have any effect. 
# The third result (not known if statistically significant) is that in 3 cases (200k V3, 
# 300k and 400k) there is a clear drop in performance in the last 3 measuruments (last 
# 30.000 games). Why?
#
# More details are found in TR-TDNTuple.tex.
# 
library(ggplot2)
library(grid)
source("summarySE.R")

path <- "../../agents/2048/csv/"
filenames=c("multiTrain-5run-200k_V2.csv"        
           #,"multiTrain-1run-200k_V3.csv"
           ,"multiTrain-5run-200k_V5.csv"
           #,"multiTrain-1run-300k_V2.csv"
           #,"multiTrain-1run-400k_V2.csv"
           #,"multiTrain-RewardGameScore.csv"#,"multiTrain-RewardGameScore-OLD.csv"
           #,"multiTrain-RewardGameSc-3P.csv"
           ) 
# Param settings: epsilon = 0.0, ChooseStart01=F, LearnFromRM=F, Reward=Score,
#     5 runs, lambda=0.0, gamma=1.0, VER_3P=true, MODE_3P=1 (all modes are equivalent for 1-player game 2048).
# V2: alpha=0.2-->0.1
# V3: alpha=0.4-->0.2 --> same result as V2
# V4: alpha=0.2-->0.2 --> slightly  worse than V2,V3
# V5: V2+lambda=0.5   --> slightly better than V2


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
                    ,rep("200k",nrow(df))
                    #,rep("200k V3",nrow(df))
                    ,rep("200k V5",nrow(df))
                    #,rep("300k",nrow(df))
                    #,rep("400k",nrow(df))
                    #,rep("game score OLD",nrow(df))
                    ,rep("game sc 3P",nrow(df))
                    )
  dfBoth = rbind(dfBoth,cbind(df,REWARD=afterCol))
}

# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfBoth, measurevar="evalQ", groupvars=c("gameNum","REWARD"))
tgc$REWARD <- as.factor(tgc$REWARD)

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(1000) # move them 100 to the left and right

q <- ggplot(tgc,aes(x=gameNum,y=evalQ,colour=REWARD))
q <- q+geom_errorbar(aes(ymin=evalQ-se, ymax=evalQ+se), width=3000, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
#q <- q+geom_line()
q <- q+scale_y_continuous(limits=c(0,120000))    
q <- q+scale_x_continuous(limits=c(0,201000))     # for x=gameNum
q <- q+guides(colour = guide_legend(reverse = TRUE))
plot(q)

