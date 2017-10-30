library(ggplot2)
library(grid)
source("summarySE.R")

path <- "../../agents/2048/csv/"
#filenames=c("multiTrain-2048-withAFTERSTATE.csv","multiTrain-2048-noAFTERSTATE.csv") 
filenames=c("multiTrain-1run-100k.csv"
            #,"multiTrain-1run-200k.csv"
            #,"multiTrain-1run-200k-eTile.csv"
            , "multiTrain-1run-100k-al02.csv"
            , "multiTrain-1run-100k-eTile-3P.csv"
            , "multiTrain-1run-100k-score-3P.csv"
) 
titnames=c("100k","200k","eTile")
PLOTALLLINES=T    # if =T: make a plot for each filename, with one line for each run
xquant="gameNum"  # "actionNum"|"gameNum": the quantity to be displayed along the x-axis
  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, na.string="-1", dec=".",skip=2)
  df$run <- as.factor(df$run)
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + ggtitle(titnames[k]) +
      scale_y_continuous(limits=c(0,100000)) #+
    #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  afterCol = switch(k
                    ,rep("100k",nrow(df))
                    #,rep("200k",nrow(df))
                    #,rep("eTile",nrow(df))
                    ,rep("al=0.2",nrow(df))
                    ,rep("eTile 3P",nrow(df))
                    ,rep("score 3P",nrow(df))
  )
  dfBoth = rbind(dfBoth,cbind(df,NGAMES=afterCol))
}

# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfBoth, measurevar="evalQ", groupvars=c(xquant,"NGAMES"))
tgc$NGAMES <- as.factor(tgc$NGAMES)

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(100) # move them 100 to the left and right

if (xquant=="actionNum") {
  q <- ggplot(tgc,aes(x=actionNum,y=evalQ,colour=NGAMES))
} else {
  q <- ggplot(tgc,aes(x=gameNum,y=evalQ,colour=NGAMES))
}
#q <- q+geom_errorbar(aes(ymin=evalQ-se, ymax=evalQ+se), width=300, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
#q <- q+geom_line()
q <- q+scale_y_continuous(limits=c(0,120000)) 
#q <- q+guides(colour = guide_legend(reverse = TRUE))
plot(q)

