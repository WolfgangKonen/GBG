#
# **** These are new results with MCTSWrapper[TCL4-EXP] from Jan 2021 ****
#
# **** see MCTSWrapperResults-2021-01-16.docx, Sec. 'Rubik's Cube'.
# **** The relevant CSV files are generate with 
#      MCTSWrapperAgentTest.rubiksCube2x2Test & .rubiksCube3x3Test
# 
library(ggplot2)
library(grid)
library(scales)       # for 'labels=percent' in ggplot
source("summarySE.R")

PLOTALLLINES=F  # if =T: make a plot for each filename, with one line for each run
EE=20           # 20 or 50: eval epiLength
TWISTTYPE="QTM" # QTM or HTM

filenames=c()
for (CUBEW in c(2,3)) {        # cube width, either 2 or 3
  cubeww =paste0( CUBEW,"x",CUBEW)  # e.g. "3x3"
  cubewww=paste0(cubeww,"x",CUBEW)  # e.g. "3x3x3"
  if (TWISTTYPE=="HTM") {
    path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_AT/csv/",sep=""); 
    fname <- switch(CUBEW
                    ," "
                    ,paste0("mRubiks2x2-MWrap[TCL4-p13]-SoftMax-p1-13-EE",EE,".csv") # CUBEW=2, HTM
                    ,paste0("mRubiks3x3-MWrap[TCL4-p9]-SoftMax-p1-9-EE",EE,".csv")   # CUBEW=3, HTM
                    #,paste0("mRubiks3x3-MWrap[TCL4-p20]-SoftMax-p1-9-EE",EE,".csv") # CUBEW=3
    )
  } else {  # i.e. "QTM"
    path <- paste("../../agents/RubiksCube/",cubewww,"_STICKER2_QT/csv/",sep=""); 
    fname <- switch(CUBEW
                    ," "
                    #,paste0("mRubiks2x2-MWrap[TCL4-p16]-QTM-p1-16-EE",EE,".csv")  # CUBEW=2, QTM
                    ,paste0("mRubiks2x2-MWrap[TCL4-p16-lam05]-QTM-p1-16-EE",EE,".csv")# CUBEW=2, QTM
                    ,paste0("mRubiks3x3-MWrap[TCL4-p13]-QTM-p1-13-EE",EE,".csv")   # CUBEW=3, QTM
    )
  } 
  # -noSoftMax/-SoftMax: with USESOFTMAX=false/true in ConfigWrapper.
  # -noLast: with USELASTMCTS=false in ConfigWrapper.
  # other settings: maxDepth=50, c_puct=1.0, STICKER2 
  # agtFiles for HTM: 
  #   2x2: "TCL4-p13-3000k-60-7t.agt.zip"
  #   3x3: "TCL4-p9-2000k-120-7t.agt.zip"
  # agtFiles for QTM: 
  #   2x2: "TCL4-p16-3000k-60-7t.agt.zip" or "TCL4-p16-3000k-60-7t-lam05.agt.zip"
  #   3x3: "TCL4-p13-3000k-120-7t.agt.zip"
  filename <- paste0(path,fname)
  filenames = c(filenames,filename)
}

wfac = 1;
errWidth=0.5;
#titleStr = paste("RubiksCube ",cubeww," with MCTSWrap(TCL), SoftMax",sep="");
titleStr = substr(fname,12,nchar(fname))
evalStr = "percentage solved"
Ylimits=c(-0.01,1.01); 
#Xlimits=c(0,100000); # c(400,6100) (-/+100 to grab position-dodge-moved points)

# files generated via MCTSWrapperAgentTest.rubiksCube2x2Test or rubiksCube3x3Test:

  
dfBoth = data.frame()
for (k in 1:length(filenames)) {
  filename = filenames[k]
  df <- read.csv(file=filename, dec=".",sep=",",skip=2)
  df$run <- as.factor(df$run)
  print(unique(df$run))
  df <- df[,setdiff(names(df),c("EE","dEdax", "user1","user2"))]
  df <- df[df$EPS==1e-8,]
  #browser()

  if (PLOTALLLINES) {
    q <- ggplot(df,aes(x=iterMWrap,y=winrate))
    q <- q+geom_line(aes(colour=run),size=1.0) + 
      scale_y_continuous(limits=c(0,1)) #+
      #facet_grid(.~THETA, labeller = label_both)
    plot(q)
  }
  
  # add column 'cubeWidth' which is either "2x2" or "3x3"
  cubeWidthCol = switch(k
                         ,"2x2x2"
                         ,"3x3x3"
  )
  #
  dfBoth <- rbind(dfBoth,cbind(df,cubeWidth=cubeWidthCol))
}

# add column 'agent' which is "TCL-base" for iterMWrap==0 and "TCL-wrap" else
# and copy the "TCL-base" data points and set their iterMWrap=1000 
# --> horizontal line for "TCL-base"
#
dfBoth <- cbind(dfBoth,agent="TCL-wrap",stringsAsFactors=FALSE)
dfBoth$agent[dfBoth$iterMWrap==0] <- "TCL-base"
df1000 <- dfBoth[dfBoth$agent=="TCL-base",]
df1000$iterMWrap <- 1000
#dfBoth <- rbind(dfBoth,df1000)

dfBoth <- dfBoth[dfBoth$iterMWrap %in% c(0,100,500,1000),]

# summarySE is a very useful script from www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)
# It summarizes a dataset, by grouping measurevar according to groupvars and calculating
# its mean, sd, se (standard dev of the mean), ci (conf.interval) and count N.
tgc <- summarySE(dfBoth, measurevar="winrate", groupvars=c("iterMWrap","cubeWidth","pTwist"))
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
q <- q+scale_x_continuous(limits=c(1,16), breaks=c(1,3,5,7,9,11,13,15)) 
q <- q+xlab("scrambling twists") +
     annotate("text",x=14.0,y=0.05,label=TWISTTYPE, size=5)
q <- q+guides(colour = guide_legend(reverse = FALSE))
q <- q+guides(linetype = guide_legend(reverse = FALSE))
q <- q+theme(axis.title = element_text(size = rel(1.5)))    # bigger axis labels 
q <- q+theme(axis.text = element_text(size = rel(1.5)))     # bigger tick mark text  
q <- q+theme(legend.text = element_text(size = rel(1.2)))   # bigger legend text  
q <- q+theme(plot.title = element_text(size = rel(1.5)))    # bigger title text  


plot(q)
pdffile=paste0("Rubiks-both-cubes-ptwist-",TWISTTYPE,".pdf")
ggsave(pdffile, width = 8.04, height = 4.95, units = "in")
cat(paste("Plot saved to",pdffile,"\n"))

