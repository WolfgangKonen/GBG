library(ggplot2)
library(grid)
source("summarySE.R")

PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
USEGAMESK=T       # if =T: use x-axis variable 'gamesK' instead of 'gameNum'  

wfac = ifelse(USEGAMESK,1000,1);
gamesVar = ifelse(USEGAMESK,"gamesK","gameNum")
limits=c(-1.0,0.1);   # there might fall some data out of this y-range, leading
                      # to the warning "Removed N rows containing missing values"
errWidth=300/wfac;
path <- "../../agents/TicTacToe/csv/"; 

filenames=c("multiTrainSarsa.csv"
           ,"multiTrainSarsaNoLearnRM.csv"
           ,"multiTrainSarsaNoFinalAdapt.csv"
           #,"multiTrainSarsaInitNull.csv"
           #,"multiTrain_TCL-EXP-al10-lam06-500k-eps0025.csv"
           )
# other pars: alpha=1.0 .. 0.5, eps=0.1 ... 0.0, ChooseStart01=F
# lambda=0.0, learnFromRM=true, NORM=F, sigmoid=tanh

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, dec=".",skip=2)
  df$run <- as.factor(df$run)
  df <- df[,setdiff(names(df),c("trnMoves","elapsedTime","movesSecond"))]
  #if (k==1) df <- cbind(df,actionNum=rep(0,nrow(df)))
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(-1,0)) #+
      #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  algoCol = switch(k
                    ,rep("Sarsa",nrow(df))
                    ,rep("no learnRM",nrow(df))
                    ,rep("no FARL",nrow(df))   # no finalAdaptAgents
                   #,rep("init null",nrow(df)) # 
                   #,rep("al50",nrow(df))
                    #,rep("eps0.025",nrow(df))
                    #,rep(0.80,nrow(df))
                    #,rep(0.90,nrow(df))
                    #,rep(0.2,nrow(df))
                    #,rep(0.205,nrow(df))
  )
  #browser()
  dfBoth <- rbind(dfBoth,cbind(df,algorithm=algoCol))
                  
}

# This defines a new grouping variable 'gamesK':
#       games                           gamesK
#       10000,20000,30000,40000,50000   50
#       60000,70000,80000,90000,100000  100
#       ...                             ...
# If USEGAMESK==T, then the subsequent summarySE calls will group 'along the x-axis'
# as well (all measurements with the same gamesK-value in the same bucket)
dfBoth <- cbind(dfBoth,gamesK=1*(ceiling(dfBoth$gameNum/1000)))

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="evalQ", groupvars=c(gamesVar,"algorithm"))
tgc1 <- cbind(tgc1,evalMode=rep(2,nrow(tgc1)))
names(tgc1)[4] <- "eval"  # rename "evalQ"
# tgc2 <- summarySE(dfBoth, measurevar="evalT", groupvars=c(gamesVar,"algorithm"))
# tgc2 <- cbind(tgc2,evalMode=rep(10,nrow(tgc2)))
# names(tgc2)[4] <- "eval"  # rename "evalT"
tgc <- tgc1
#tgc <- tgc2
#tgc <- rbind(tgc1,tgc2)
tgc$algorithm <- as.factor(tgc$algorithm)
tgc$evalMode <- as.factor(tgc$evalMode)

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(3/wfac) # move them 3000 to the left and right

if (USEGAMESK) {
  q <- ggplot(tgc,aes(x=gamesK,y=eval,colour=algorithm))  #,linetype=evalMode
  q <- q + xlab(bquote(paste("episodes [*",10^3,"]", sep=""))) + ylab(evalStr)
} else {
  q <- ggplot(tgc,aes(x=gameNum,y=eval,colour=algorithm)) #,linetype=evalMode
}
q <- q+geom_errorbar(aes(ymin=eval-se, ymax=eval+se), width=errWidth) #, position=pd)
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+scale_y_continuous(limits=limits) 
#q <- q+guides(colour = guide_legend(reverse = TRUE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  

plot(q)

