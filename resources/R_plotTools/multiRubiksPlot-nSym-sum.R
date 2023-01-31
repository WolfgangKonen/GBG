#
# **** These are new results with MCTSWrapper[TCL4-EXP], nSym, and QTM from June 2022 ****
#
# **** see MCTSWrapperResults-2021-01-16.docx, Sec. 'With Symmetries'.
# 
library(ggplot2)
library(grid)
library(scales)     # needed for 'labels=percent'
library(ggpubr)     # needed for 'ggarrange'
source("summarySE.R")

TWISTTYPE="QTM"
LEGVAR="NSYM"     # "ITER" or "NSYM" (legend variable)
PLOTTWO = T       # whether to make two stacked plots (winrate, time) or only one plot (winrate) 
twostr <- ifelse(PLOTTWO,"2","")

filenames=c()
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
                    ,paste0("symmIterBatch-nSym0-16.csv")   # CUBEW=3, QTM
    )
  } 
  filename <- paste0(path,fname)
  filenames = c(filenames,filename)
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

  # add column 'cubeWidth' which is either "2x2" or "3x3"
  cubeWidthCol = switch(k
                        ,"3x3x3"
                        ,"3x3x3"
  )
  #
  dfBoth <- rbind(dfBoth,cbind(df,cubeWidth=cubeWidthCol))
}

dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(0,50,100,200,400,800),]
#-- if only a subset of iterMWrap shall be shown: --
#dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(0,200),]
#dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(50,400),]
#dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(100,800),]

colnames(dfBoth)[colnames(dfBoth) %in% c("evalQ")] <- c("winrate")

#dfBoth <- dfBoth[dfBoth$pTwist > 9,]



# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfBoth, measurevar="winrate", groupvars=c("iterMWrap","nSym"))
tgt <- summarySE(dfBoth, measurevar="totalSec", groupvars=c("iterMWrap","nSym"))

if (LEGVAR=="NSYM") {
  tgc$nSym <- as.factor(tgc$nSym)
  xlabStr <- "iterations"
  q <- ggplot(tgc,aes(x=iterMWrap,y=winrate,color=nSym,shape=nSym)) 
} else {
  tgc$iterMWrap <- as.factor(tgc$iterMWrap)
  xlabStr <- "number symmetries"
  q <- ggplot(tgc,aes(x=nSym,y=winrate,color=iterMWrap,shape=iterMWrap)) 
}

#q <- q+labs(title=titleStr)
#q <- q+geom_errorbar(aes(ymin=winrate-se, ymax=winrate+se), width=0.3) #, position=pd)
q <- q+geom_line(size=1.0) + geom_point(size=3.0)
q <- q+scale_y_continuous(limits=Ylimits, labels=percent) 
q <- q+ylab(evalStr)
#q <- q+scale_x_continuous(limits=c(1,ifelse(TWISTTYPE=="HTM",13,14)), breaks=c(1,3,5,7,9,11,13)) 
q <- q+xlab(xlabStr) +
  annotate("text",x=ifelse(TWISTTYPE=="HTM",11,11),y=0.05,label=TWISTTYPE, size=5) 
q <- q+guides(colour = guide_legend(reverse = TRUE))
q <- q+guides(shape = guide_legend(reverse = TRUE))
if (PLOTTWO) {
  q <- q+theme(axis.title.x = element_blank())    # no x-axis label
} else {
  q <- q+theme(axis.title = element_text(size = rel(1.2)))    # bigger axis labels 
}
q <- q+theme(axis.text = element_text(size = rel(1.2)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.2)))    # bigger title text  

if (LEGVAR=="NSYM") {
  tgt$nSym <- as.factor(tgt$nSym)
  xlabStr <- "iterations"
  q2 <- ggplot(tgt,aes(x=iterMWrap,y=totalSec,color=nSym,shape=nSym)) 
} else {
  tgt$iterMWrap <- as.factor(tgt$iterMWrap)
  xlabStr <- "number symmetries"
  q2 <- ggplot(tgt,aes(x=nSym,y=totalSec,color=iterMWrap,shape=iterMWrap)) 
}

#q2 <- q2+labs(title=titleStr)
#q2 <- q2+geom_errorbar(aes(ymin=winrate-se, ymax=winrate+se), width=0.3) #, position=pd)
q2 <- q2+geom_line(size=1.0) + geom_point(size=3.0)
q2 <- q2+ylab("time [sec]")
q2 <- q2+  xlab(xlabStr)  
q2 <- q2+guides(colour = guide_legend(reverse = TRUE))
q2 <- q2+guides(shape = guide_legend(reverse = TRUE))
q2 <- q2+theme(axis.title = element_text(size = rel(1.2)))    # bigger axis labels 
q2 <- q2+theme(axis.text = element_text(size = rel(1.2)))     # bigger tick mark text  
q2 <- q2+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q2 <- q2+theme(plot.title = element_text(size = rel(1.2)))    # bigger title text  

if (PLOTTWO) {
  fig <- ggarrange(q,q2,nrow=2,ncol=1)
} else {
  fig <- q
}
plot(fig)

pdffile=paste0("Rubiks-",tolower(LEGVAR),"-summary",twostr,"-",TWISTTYPE,".pdf")
ggsave(pdffile, width = 8.04, height = 4.95, units = "in")
cat(paste("Plot saved to",pdffile,"\n"))

