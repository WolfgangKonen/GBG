#
# **** These are new results with MCTSWrapper[TCL4-EXP] from Aug-2022 & Sep-2022 & Jan-2023 ****
#
# **** see MCTSWrapperResults-2021-01-16.docx, Sec. 'Rubik's Cube'.
# **** The relevant CSV files are generated with 
#      MCTSWrapperAgentTest.rubiksCube2x2Test & .rubiksCube3x3Test
# 
library(ggplot2)
library(grid)
library(scales)       # for 'labels=percent' in ggplot
source("summarySE.R")

EE=50           # 20 or 50: eval epiLength
TWISTTYPE="HTM" # QTM or HTM
month="Jan23-"  # "Sep22-" or "Jan23-"
pdffile=paste0("Rubiks-both-cubes-ptwist-",month,TWISTTYPE,"-Mix.pdf")

filenames=c()
cubeWidthCol=c()

### the Sep-2022 results with cPUCT=1.0 ###
for (CUBEW in c(2,3)) {        # cube width, either 2 or 3
  cubeww =paste0( CUBEW,"x",CUBEW)  # e.g. "3x3"
  cubewww=paste0(cubeww,"x",CUBEW)  # e.g. "3x3x3"
  if (TWISTTYPE=="HTM") {
    path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_AT/csv/",sep=""); 
    fname <- switch(CUBEW
                    ," "
                    ,paste0("mRubiks2x2-MWrap[TCL4-p13-ET16]-",month,"p1-13-EE50.csv") # CUBEW=2, HTM
                    ,paste0("mRubiks3x3-MWrap[TCL4-p9-ET13]-",month,"p1-9-EE50.csv")   # CUBEW=3, HTM
    )
  } else {  # i.e. "QTM"
    path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_QT/csv/",sep=""); 
    fname <- switch(CUBEW
                    ," "
                    ,paste0("mRubiks2x2-MWrap[TCL4-p16-ET20]-",month,"p1-16-EE",EE,".csv")# CUBEW=2, QTM
                    ,paste0("mRubiks3x3-MWrap[TCL4-p13-ET16]-",month,"p1-13-EE50.csv")   # CUBEW=3, QTM
    )
  } 
  # other settings: maxDepth=50, c_puct=1.0, USESOFTMAX=true, USELASTMCTS=true, STICKER2 
  # agtFiles for HTM: 
  #   2x2: "multiTrain/TCL4-p13-ET16-3000k-60-7t_00.agt.zip"
  #   3x3: "multiTrain/TCL4-p9-ET13-3000k-120-7t_00.agt.zip"
  # agtFiles for QTM: 
  #   2x2: "multiTrain/TCL4-p16-ET20-3000k-60-7t_00.agt.zip" 
  #   3x3: "multiEval/TCL4-p13-ET16-3000k-120-7t_02.agt.zip"
  filename <- paste0(path,fname)
  filenames = c(filenames,filename)
  cubeWidthCol = c(cubeWidthCol,cubewww)
}

if (month!="Jan23-") {
  ### the Aug-2022 results with cPUCT=10.0 ###
  ### (only needed in context with Sep-2022 results, because Jan-2023 has cPUCT=10.0 
  ### already integrated)
  for (CUBEW in c(2,3)) {        # cube width, either 2 or 3
    cubeww =paste0( CUBEW,"x",CUBEW)  # e.g. "3x3"
    cubewww=paste0(cubeww,"x",CUBEW)  # e.g. "3x3x3"
    if (TWISTTYPE=="HTM") {
      path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_AT/csv/",sep=""); 
      fname <- switch(CUBEW
                      ," "
                      ,paste0("mRubiks2x2-MWrap[TCL4-p13-ET16]-Aug22-p1-13-EE",EE,".csv") # CUBEW=2, HTM
                      ,paste0("mRubiks3x3-MWrap[TCL4-p9-ET13]-Aug22-p1-9-EE",EE,".csv")   # CUBEW=3, HTM
      )
    } else {  # i.e. "QTM"
      path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_QT/csv/",sep=""); 
      fname <- switch(CUBEW
                      ," "
                      ,paste0("mRubiks2x2-MWrap[TCL4-p16-ET20]-Aug22-p1-16-EE",EE,".csv")# CUBEW=2, QTM
                      ,paste0("mRubiks3x3-MWrap[TCL4-p13-ET16]-Aug22-p1-13-EE50.csv")   # CUBEW=3, QTM
      )
    } 
    # other settings: maxDepth=50, c_puct=10.0, USESOFTMAX=true, USELASTMCTS=true, STICKER2 
    # agtFiles for HTM: 
    #   2x2: "multiTrain/TCL4-p13-ET16-3000k-60-7t_00.agt.zip"
    #   3x3: "multiTrain/TCL4-p9-ET13-3000k-120-7t_00.agt.zip"
    # agtFiles for QTM: 
    #   2x2: "multiTrain/TCL4-p16-ET20-3000k-60-7t_00.agt.zip" 
    #   3x3: "multiEval/TCL4-p13-ET16-3000k-120-7t_02.agt.zip"
    filename <- paste0(path,fname)
    filenames = c(filenames,filename)
    cubeWidthCol = c(cubeWidthCol,cubewww)
  }
}

wfac = 1;
errWidth=0.5;
#titleStr = paste("RubiksCube ",cubeww," with MCTSWrap(TCL), SoftMax",sep="");
titleStr = substr(fname,12,nchar(fname))
evalStr = "percentage solved"
Ylimits=c(-0.01,1.01); 

# files generated via MCTSWrapperAgentTest.rubiksCube2x2Test or rubiksCube3x3Test:

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename = filenames[k]
  df <- read.csv(file=filename, dec=".",sep=",",skip=2)
  if (length(df)==1) df <- read.csv(file=filename, dec=".",sep=";",skip=2)
  df$run <- as.factor(df$run)
  print(unique(df$run))
  df <- df[,setdiff(names(df),c("EE","dEdax", "user1","user2"))]
  df <- df[df$EPS==1e-8,]

  dfBoth <- rbind(dfBoth,cbind(df,cubeWidth=cubeWidthCol[k]))
}

# select the iterMWrap levels:
dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(0,100,800),]

# select the better c_puct case for every curve (manual tuning)
dfMix <- data.frame()
dfMix <- rbind(dfMix,dfBoth[dfBoth$iterMWrap==0 & dfBoth$c_puct==1,])
if (TWISTTYPE=="HTM") {
  dfMix <- rbind(dfMix,dfBoth[dfBoth$iterMWrap==800 & dfBoth$c_puct==1,])
  dfMix <- rbind(dfMix,dfBoth[dfBoth$iterMWrap==100 & dfBoth$cubeWidth=="2x2x2" & dfBoth$c_puct==1,])
  dfMix <- rbind(dfMix,dfBoth[dfBoth$iterMWrap==100 & dfBoth$cubeWidth=="3x3x3" & dfBoth$c_puct==10,])
} else {  # i.e. "QTM"
  dfMix <- rbind(dfMix,dfBoth[dfBoth$iterMWrap==100 & dfBoth$c_puct==1,])
  dfMix <- rbind(dfMix,dfBoth[dfBoth$iterMWrap==800 & dfBoth$cubeWidth=="2x2x2" & dfBoth$c_puct==10,])
  dfMix <- rbind(dfMix,dfBoth[dfBoth$iterMWrap==800 & dfBoth$cubeWidth=="3x3x3" & dfBoth$c_puct==1,])
}

# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfMix, measurevar="winrate", groupvars=c("iterMWrap","cubeWidth","pTwist"))
#tgc$pTwist <- as.factor(tgc$pTwist)
tgc$cubeWidth <- as.factor(tgc$cubeWidth)
tgc$iterMWrap <- as.factor(tgc$iterMWrap)

# The errorbars may overlap, so use position_dodge to move them horizontally
#pd <- position_dodge(1000/wfac) # move them 1000/wfac to the left and right


q <- ggplot(tgc,aes(x=pTwist,y=winrate,color=iterMWrap,shape=iterMWrap,linetype=cubeWidth))
#q <- q+labs(title=titleStr)
q <- q+geom_errorbar(aes(ymin=winrate-se, ymax=winrate+se), width=errWidth) #, position=pd)
#q <- q+geom_line(position=pd,size=1.0) + geom_point(position=pd,size=2.0) 
q <- q+geom_line(size=1.0) + geom_point(size=3.0)
q <- q+scale_y_continuous(limits=Ylimits, labels=percent) 
q <- q+ylab(evalStr)
q <- q+scale_x_continuous(limits=c(1,ifelse(TWISTTYPE=="HTM",13,16)), breaks=c(1,3,5,7,9,11,13,15)) 
q <- q+xlab("scrambling twists") +
     annotate("text",x=ifelse(TWISTTYPE=="HTM",11,14),y=0.05,label=TWISTTYPE, size=5)
q <- q+guides(colour = guide_legend(reverse = FALSE))
q <- q+guides(linetype = guide_legend(reverse = FALSE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.5)))    # bigger title text  


plot(q)
ggsave(pdffile, width = 8.04, height = 4.95, units = "in")
cat(paste("Plot saved to",pdffile,"\n"))

