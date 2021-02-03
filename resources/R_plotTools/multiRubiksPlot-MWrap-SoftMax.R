#
# **** These are new results with MCTSWrapper[TCL4-EXP] from Jan 2021 ****
#
# **** see TDNTupleAgt.docx, Sec 'MCTSWrapper Results', 'Rubik's Cube'.
# 
library(ggplot2)
library(grid)
source("summarySE.R")

PLOTALLLINES=F    # if =T: make a plot for each filename, with one line for each run
CUBEW=3

cubeww =paste0( CUBEW,"x",CUBEW)
cubewww=paste0(cubeww,"x",CUBEW)
wfac = 1;
errWidth=20/wfac;
titleStr = paste("RubiksCube ",cubeww," with MCTSWrap(TCL), no SoftMax",sep="");
evalStr = "perc solved"
path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_AT/csv/",sep=""); 
Ylimits=c(-0.01,1.01); 
#Xlimits=c(0,100000); # c(400,6100) (-/+100 to grab position-dodge-moved points)

# files generated via MCTSWrapperAgentTest.rubiksCube2x2Test or rubiksCube3x3Test:
filenames=c(paste0("mRubiks",cubeww,"-MWrap-noSoftMax.csv")
            #paste0("mRubiks",cubeww,"-MWrap-noSoftMax-noLast.csv") 
            #paste0("mRubiks",cubeww,"-MWrap-SoftMax.csv")    
)           
# -noSoftMax/-SoftMax: with USESOFTMAX=false/true in ConfigWrapper.
# -noLast: with USELASTMCTS=false in ConfigWrapper.
# other settings: maxDepth=50, c_puct=1.0, STICKER2, all twists
# agtFile: 
#   2x2: "TCL4-p13-3000k-60-7t.agt.zip"
#   3x3: "TCL4-p9-2000k-120-7t.agt.zip"

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename <- paste0(path,filenames[k])
  df <- read.csv(file=filename, dec=".",sep=",",skip=2)
  df$run <- as.factor(df$run)
  print(unique(df$run))
  df <- df[,setdiff(names(df),c("dEdax", "user1","user2"))]

  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=iterMWrap,y=winrate))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(0,1)) #+
      #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  targetModeCol = switch(k
                         ,rep("TD",nrow(df))
                         ,rep("TD",nrow(df))
                         ,rep("TERNA",nrow(df))
                         ,rep("TERNA",nrow(df))
  )
  #browser()
  dfBoth <- rbind(dfBoth,cbind(df,targetMode=targetModeCol))
}

tgc <- data.frame()
# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc1 <- summarySE(dfBoth, measurevar="winrate", groupvars=c("iterMWrap","EPS","targetMode"))
#qeval <- "MaxN"   #ifelse(length(grep("-ALm",filename)==1),"AB","MCTS")
#tgc1 <- cbind(tgc1,evalMode=rep(qeval,nrow(tgc1)))
tgc <- tgc1
#tgcT <- summarySE(dfBoth, measurevar="totalTrainSec", groupvars=c("iterMWrap","EPS","targetMode"))
tgc$targetMode <- as.factor(tgc$targetMode)
tgc$EPS <- as.factor(tgc$EPS)

# The errorbars may overlap, so use position_dodge to move them horizontally
#pd <- position_dodge(1000/wfac) # move them 1000/wfac to the left and right


q <- ggplot(tgc,aes(x=iterMWrap,y=winrate,colour=EPS,linetype=EPS))
q <- q+labs(title=titleStr)
q <- q+geom_errorbar(aes(ymin=winrate-se, ymax=winrate+se), width=errWidth) #, position=pd)
#q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+geom_line(size=1.0) + geom_point(size=2.0)
q <- q+scale_y_continuous(limits=Ylimits) + ylab(evalStr)
q <- q+guides(colour = guide_legend(reverse = TRUE))
q <- q+guides(linetype = guide_legend(reverse = TRUE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.5)))    # bigger title text  

plot(q)

