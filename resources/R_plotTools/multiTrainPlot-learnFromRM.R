# 
# This plot shows for 4x4 Hex that the 2P-form has better results than the 3P-form with OLD_3P=T. 
# More details why are found in TDNTupleAgt.docx, Sec. "Debugging VER_3P=OLD_3P=true"
# and in TR-TDNTuple.tex.
# 
library(ggplot2)
library(grid)
source("summarySE.R")

path <- "../../agents/TicTacToe/csv/"; limits=c(-0.5,0); errWidth=300;
path <- "../../agents/Hex/04/csv/"; limits=c(0,1.0); errWidth=3000;

# 25 runs, epsilon 1.3 --> 0, lambda=0, 20 5-tuples. "-01" means 'Choose Start 01' checked.
# Files with "-3P" are with VER_3P=true. 
# Files with "-2P" are with VER_3P=false, NEW_2P=true (new 2P-form).
# Files with neither "-2P" nor "-3P" are with VER_3P=false, NEW_2P=false (old 2P-form). There results 
# should be equivalent to the "-2P" results.
filenames=c( "multiTrain-withLearnFromRM-01-al050.csv"
            #,"multiTrain-withLearnFromRM-01-al050-2P.csv"
            ,"multiTrain-withLearnFromRM-01-al050-3P.csv"
            ,"multiTrain-noLearnFromRM-01-al050.csv"
            #,"multiTrain-noLearnFromRM-01-al050-2P.csv"
            ,"multiTrain-noLearnFromRM-01-al050-3P.csv"
            ) 
PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
MEASUREVAR="evalT"  # "evalQ" , "evalT"
  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, na.string="-1", dec=".",skip=2)
  df$run <- as.factor(df$run)
  cat(c(unique(df$run)),"\n")
  # we remove here two columns since the older multiTrain files do not have it:
  df <- df[,setdiff(names(df),c("actionNum","trnMoves"))]
  
  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=gameNum,y=evalQ))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(-1,0)) #+
    #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  learnCol = switch(k
                    ,rep("true",nrow(df))
                    #,rep("true 2P",nrow(df))
                    ,rep("true 3P",nrow(df))
                    ,rep("false",nrow(df))
                    #,rep("false 2P",nrow(df))
                    ,rep("false 3P",nrow(df))
                    )
  dfBoth = rbind(dfBoth,cbind(df,learnFromRM=learnCol))
}

# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfBoth, measurevar=MEASUREVAR, groupvars=c("gameNum","learnFromRM"))
tgc$learnFromRM <- as.factor(tgc$learnFromRM)

# The errorbars may overlap, so use position_dodge to move them horizontally
pd <- position_dodge(150) # move them 100 to the left and right

if (MEASUREVAR=="evalT") {
  q <- ggplot(tgc,aes(x=gameNum,y=evalT,colour=learnFromRM))
  q <- q+geom_errorbar(aes(ymin=evalT-se, ymax=evalT+se), width=errWidth, position=pd)
} else {
  q <- ggplot(tgc,aes(x=gameNum,y=evalQ,colour=learnFromRM))
  q <- q+geom_errorbar(aes(ymin=evalQ-se, ymax=evalQ+se), width=errWidth, position=pd)
}
q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+scale_y_continuous(limits=limits) 
#q <- q+guides(colour = guide_legend(reverse = TRUE))
plot(q)

