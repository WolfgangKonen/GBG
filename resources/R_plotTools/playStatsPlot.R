library(ggplot2)
library(grid)
source("summarySE.R")

path <- "../../agents/2048/csv/"; limits=c(0.0,1.0); errWidth=3000;

filenames=c("playStats-TDNTuple2.csv","playStats-MC.csv")
plottitles=c("TD-NTuple","MC")
PLOTALLLINES=T    # if =T: make a plot for each filename, with one line for each run
  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, na.string="-1", dec=".",skip=2)
  df$run <- as.factor(df$run)
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=moveNum,y=cumEmptyTl,colour=run))
    q <- q+geom_line(aes(colour=run))
    q <- q+ggtitle(plottitles[k])
    plot(q)
    q <- ggplot(df,aes(x=moveNum,y=gameScore,colour=run))
    q <- q+geom_line(aes(colour=run))
    q <- q+ggtitle(plottitles[k])
    plot(q)
  }
  #browser()
  
  agentCol = switch(k
                    ,rep("TD-NTuple",nrow(df))
                    ,rep("MC",nrow(df))
                    )
  dfBoth <- rbind(dfBoth,cbind(df,agent=agentCol))
  dfBoth$agent <- as.factor(dfBoth$agent)
                  
}

tgc <- data.frame()
## summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
## It summarizes a dataset, by grouping measurevar according to groupvars and calculating
## its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="gameScore", groupvars=c("moveNum","agent"))
tgc2 <- summarySE(dfBoth, measurevar="cumEmptyTl", groupvars=c("moveNum","agent"))

## The errorbars may overlap, so use position_dodge to move them horizontally
#pd <- position_dodge(1000) # move them 300 to the left and right

q1 <- ggplot(tgc1,aes(x=moveNum,y=gameScore))  
#q1 <- q1+geom_errorbar(aes(ymin=eval-se, ymax=eval+se), width=errWidth, position=pd)
q1 <- q1+geom_line(aes(colour=agent)) #+ geom_point(position=pd,size=2.0) 
#q1 <- q1+scale_y_continuous(limits=c(0,1000)) 
#q1 <- q1+guides(colour = guide_legend(reverse = TRUE))
plot(q1)

q2 <- ggplot(tgc2,aes(x=moveNum,y=cumEmptyTl))  #,linetype=evalMode
q2 <- q2+geom_line(aes(colour=agent)) #+ geom_point(position=pd,size=2.0) 
plot(q2)


