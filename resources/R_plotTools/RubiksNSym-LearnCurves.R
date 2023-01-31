#
# **** These are new results with symmetries and QTM/HTM from Aug 2022 ****
#
# **** see notes-WK-RubiksCube.docx, Sec. '3x3x3 Results'/'With Symmetries'.
# 
library(ggplot2)
library(grid)
library(scales)     # needed for 'labels=percent'
source("summarySE.R")

PLOTALLLINES=F  # if =T: make a plot for each filename, with one line for each run
EE=20           # 20 or 50: eval epiLength
pdffile=paste0("Rubiks-nsym-learncurves",".pdf")

filenames=c()
twisttypes=c()
cubeWidthCol=c()
nSymCol=c()
for (TWISTTYPE in c("QTM")) {
  for (CUBEW in c(3)) { 
    for (NSYM in c("00","08","16","24")) {
      cubeww =paste0( CUBEW,"x",CUBEW)  # e.g. "3x3"
      cubewww=paste0(cubeww,"x",CUBEW)  # e.g. "3x3x3"
      nSym = as.numeric(NSYM)
      if (TWISTTYPE=="HTM") {
        path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_AT/csv/",sep=""); 
      } else {  # i.e. "QTM"
        path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_QT/csv/",sep=""); 
      } 
      filename <- paste0(path,"mRubiks-p13-ET16-nSym",NSYM,".csv")
      filenames = c(filenames,filename)
      twisttypes= c(twisttypes,TWISTTYPE)
      cubeWidthCol = c(cubeWidthCol,cubewww)
      nSymCol = c(nSymCol,nSym)
    }
  }
}

wfac = 1;
errWidth=20/wfac;
#titleStr = substr(fname,12,nchar(fname))
evalStr = "percentage solved"
evalStr = "percentage solved"
Ylimits=c(0.5,0.9); 
Xlimits=c(0,3000000);

# files generated via DistinctColorTrafos.countDistinct_Pocket or countDistinct_Rubiks:

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename = filenames[k]
  print(filename)
  df <- read.csv(file=filename, dec=".",sep=";",skip=3)
  # add column 'cubeWidth' which is either "2x2" or "3x3" and 'twistType' and 'nSym'
  dfBoth <- rbind(dfBoth,cbind(df,cubeWidth=cubeWidthCol[k],twistType=twisttypes[k],nSym=nSymCol[k]))
}

dfBoth <- dfBoth[,setdiff(names(dfBoth),c("evalT","actionNum","trnMoves","ucol1","ucol2"))]

dfBoth$nSym <- as.factor(dfBoth$nSym)
#browser()

# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfBoth, measurevar="evalQ", groupvars=c("gameNum","nSym","cubeWidth"))
tgc$nSym <- as.factor(tgc$nSym)


q <- ggplot(tgc,aes(x=gameNum,y=evalQ))
q <- q+ylab(evalStr)+xlab("episodes")
#q <- q+ geom_point(aes(colour=nSym), size=3.0)
q <- q+ geom_line(aes(colour=nSym), size=1.0)
q <- q+scale_y_continuous(limits=Ylimits, labels=percent) 
q <- q+scale_x_continuous(limits=Xlimits)
#q <- q+facet_grid(.~twistType, labeller = label_both)
q <- q+guides(colour = guide_legend(reverse = TRUE))
q <- q+guides(linetype = guide_legend(reverse = TRUE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.5)))    # bigger title text  

plot(q)
ggsave(pdffile, width = 8.04, height = 4.95, units = "in")
cat(paste("Plot saved to",pdffile,"\n"))

