library(ggplot2)
library(grid)
source("summarySE.R")

path <- "../agents/Hex/04/csv/"; limits=c(0,1.0); errWidth=3000;

filenames=c("multiTrain-withLearnFromRM-01.csv","multiTrain-noLearnFromRM-01.csv") 
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

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="evalQ", groupvars=c("gameNum","learnFromRM"))
tgc1 <- cbind(tgc1,evalMode=rep(0,nrow(tgc1)))
names(tgc1)[4] <- "eval"  # rename "evalQ"
tgc2 <- summarySE(dfBoth, measurevar="evalT", groupvars=c("gameNum","learnFromRM"))
tgc2 <- cbind(tgc2,evalMode=rep(10,nrow(tgc1)))
names(tgc2)[4] <- "eval"  # rename "evalT"
tgc <- rbind(tgc1,tgc2)
tgc$learnFromRM <- as.factor(tgc$learnFromRM)
tgc$evalMode <- as.factor(tgc$evalMode)

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(1000) # move them 300 to the left and right

q <- ggplot(tgc,aes(x=gameNum,y=eval,colour=learnFromRM,linetype=evalMode))
q <- q+geom_errorbar(aes(ymin=eval-se, ymax=eval+se), width=errWidth, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+scale_y_continuous(limits=limits) 
#q <- q+guides(colour = guide_legend(reverse = TRUE))
plot(q)

