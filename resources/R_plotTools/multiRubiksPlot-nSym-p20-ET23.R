#
# **** These are new results with MCTSWrapper[TCL4-EXP], nSym, and QTM from Aug 2022 ****
#
# **** see MCTSWrapperResults-2021-01-16.docx, Sec. 'With Symmetries'.
# 
library(ggplot2)
library(grid)
library(scales)     # needed for 'labels=percent'
source("summarySE.R")

TWISTTYPE="QTM"
pdffile=paste0("Rubiks-nsym-ptwist-",TWISTTYPE,"-p20-ET23.pdf")

filenames=c()
cubeWidthCol=c()
for (CUBEW in c(3)) {        # cube width, either 2 or 3, currently only 3
  cubeww =paste0( CUBEW,"x",CUBEW)  # e.g. "3x3"
  cubewww=paste0(cubeww,"x",CUBEW)  # e.g. "3x3x3"
  if (TWISTTYPE=="HTM") {
    path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_AT/csv/",sep=""); 
    fname <- switch(CUBEW
                    ," "
                    ,paste0("TODO")   # CUBEW=2, HTM
                    ,paste0("TODO")   # CUBEW=3, HTM
    )
  } else {  # i.e. "QTM"
    path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_QT/csv/",sep=""); 
    fname <- switch(CUBEW
                    ," "
                    ,paste0("TODO")   # CUBEW=2, QTM
                    #,paste0("symmIterSingle-nSym0-16.csv")   # CUBEW=3, QTM (old: ET=12)
                    ,paste0("symmIterSingle-nSym0-24-P20-ET23.csv")   # CUBEW=3, QTM, cPUCT=1
                    #,paste0("symmIterSingle-nSym0-24-ET16-cPUCT10.csv")   # cPUCT=10, clearly worse
    )
  } 
  filename <- paste0(path,fname)
  filenames = c(filenames,filename)
  cubeWidthCol = c(cubeWidthCol,cubewww)
}


wfac = 1;
errWidth=400/wfac;
titleStr = substr(fname,12,nchar(fname))
evalStr = "percentage solved"
Ylimits=c(-0.01,1.01); 
#Xlimits=c(0,100000); # c(400,6100) (-/+100 to grab position-dodge-moved points)

# files generated via MCTSWrapperAgentTest.rubiksCube2x2Test or rubiksCube3x3Test:

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename = filenames[k]
  df <- read.csv(file=filename, dec=".",sep=";",skip=3)
  df$run <- as.factor(df$run)
  print(unique(df$run))

  dfBoth <- rbind(dfBoth,cbind(df,cubeWidth=cubeWidthCol[k]))
}

dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(0,50,100,200,400,800),]
#-- if only a subset of iterMWrap shall be shown: --
dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(0,100,800),]
#dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(0,200),]
#dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(50,400),]
#dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(100,800),]

colnames(dfBoth)[colnames(dfBoth) %in% c("pMaxEval", "evalQ")] <- c("pTwist", "winrate")

#dfBoth <- dfBoth[dfBoth$pTwist > 9,]

#dfBoth <- dfBoth[dfBoth$nSym %in% c(0,16,24),];  NSYM = "nSym=0,16,24"
dfBoth <- dfBoth[dfBoth$nSym %in% c(0,8,16),];  NSYM = "nSym=0,8,16"
NSYM = "3x3x3"

# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfBoth, measurevar="winrate", groupvars=c("iterMWrap","nSym","pTwist"))
#tgc$pTwist <- as.factor(tgc$pTwist)
tgc$nSym <- as.factor(tgc$nSym)
tgc$iterMWrap <- as.factor(tgc$iterMWrap)

q <- ggplot(tgc,aes(x=pTwist,y=winrate,color=iterMWrap,shape=nSym,linetype=nSym))
#q <- q+labs(title=titleStr)
q <- q+geom_errorbar(aes(ymin=winrate-se, ymax=winrate+se), width=0.3) #, position=pd)
q <- q+geom_line(size=1.0) + geom_point(size=3.0)
q <- q+scale_y_continuous(limits=Ylimits, labels=percent) 
q <- q+ylab(evalStr)
q <- q+scale_x_continuous(limits=c(1,ifelse(TWISTTYPE=="HTM",13,15)), breaks=c(1,3,5,7,9,11,13,15,17)) 
q <- q+xlab("scrambling twists") +
  annotate("text",x=ifelse(TWISTTYPE=="HTM",11,11),y=0.05,label=paste0(TWISTTYPE,""), size=5) +
  annotate("text",x=3,y=0.05,label=NSYM, size=5)
q <- q+guides(colour = guide_legend(reverse = FALSE))
q <- q+guides(linetype = guide_legend(reverse = FALSE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.5)))    # bigger title text  
q <- q+theme(legend.key.width = unit(1.5, "cm"))


plot(q)
ggsave(pdffile, width = 8.04, height = 5.95, units = "in")
cat(paste("Plot saved to",pdffile,"\n"))

