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
pdffile=paste0("Rubiks-nsym-states",".pdf")

filenames=c()
twisttypes=c()
cubeWidthCol=c()
for (TWISTTYPE in c("HTM","QTM")) {
  for (CUBEW in c(2,3)) {        # cube width, either 2 or 3
    cubeww =paste0( CUBEW,"x",CUBEW)  # e.g. "3x3"
    cubewww=paste0(cubeww,"x",CUBEW)  # e.g. "3x3x3"
    if (TWISTTYPE=="HTM") {
      path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_AT/csv/",sep=""); 
    } else {  # i.e. "QTM"
      path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_QT/csv/",sep=""); 
    } 
    filename <- paste0(path,"numTrulyDifferentSymStates.csv")
    filenames = c(filenames,filename)
    twisttypes= c(twisttypes,TWISTTYPE)
    cubeWidthCol = c(cubeWidthCol,cubewww)
  }
}

wfac = 1;
errWidth=20/wfac;
titleStr = substr(fname,12,nchar(fname))
evalStr = expression(N[symmetricStates])#"# symmetric states"
Ylimits=c(0,25); 

# files generated via DistinctColorTrafos.countDistinct_Pocket or countDistinct_Rubiks:

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename = filenames[k]
  print(filename)
  df <- read.csv(file=filename, dec=",",sep=";",skip=2)
  df$TWISTS <- as.factor(df$TWISTS)
  print(unique(df$TWISTS))
  # add column 'cubeWidth' which is either "2x2" or "3x3" and column 'twistType'
  dfBoth <- rbind(dfBoth,cbind(df,cubeWidth=cubeWidthCol[k],twistType=twisttypes[k]))
}

dfBoth$TWISTS = as.factor(dfBoth$TWISTS)

q <- ggplot(dfBoth,aes(x=TWISTS,y=NSYM_TRUE_DIFF))
q <- q+ylab(evalStr)+xlab("scrambling twists")
q <- q+ geom_point(aes(colour=cubeWidth), size=3.0)
#q <- q+ geom_line(aes(colour=cubeWidth), size=1.0)
q <- q+scale_y_continuous(limits=Ylimits) 
q <- q+scale_x_discrete(breaks=c(0,4,8,12,16,20))
q <- q+facet_grid(.~twistType, labeller = label_both)
q <- q+guides(colour = guide_legend(reverse = FALSE))
q <- q+guides(linetype = guide_legend(reverse = TRUE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.5)))    # bigger title text  

plot(q)
ggsave(pdffile, width = 8.04, height = 4.95, units = "in")
cat(paste("Plot saved to",pdffile,"\n"))

