#
# Unify N .csv files containing multiTrain results with the same settings
# but different runs. 
# E.g. if there is one file with runs 0,..,2 and another one with runs 
# 0,...,6, unify them in one new file with run numbers 0,...,8
# 
path <- "../../agents/ConnectFour/csv/"; 

filenames=c( "multiTrainLambda.csv"
            ,"multiTrainLambda2.csv"
            #"multiTrain_TCL-EXP-NT3-al25-lam000-750k-epsfin0-V6m.csv"
            #,"multiTrain_TCL-EXP-NT3-al25-lam000-750k-epsfin0-V6m-2.csv"
           )
outfile = "multiTrain_TCL-EXP-NT3-al37-lamSweep-750k-epsfin0-V12m.csv"
  
filename <- paste0(path,filenames[1])
header0 <- readLines(con<-file(filename),n=2)
close(con)
dfUnify <- read.csv(file=filename, dec=".",skip=2)
for (k in 2:length(filenames)) {
  filename <- paste0(path,filenames[k])
  header <- readLines(con<-file(filename),n=2)
  close(con)
  df <- read.csv(file=filename, dec=".",skip=2)
  
  if (header0[1]!=header[1]) 
    stop(paste("1st header line is different:",filename,"\n",header0[1],"\n",header[1]))
  if (header0[2]!=header[2]) 
    stop(paste("2nd header line is different:",filename,"\n",header0[2],"\n",header[2]))
  
  df$run <- df$run + max(unique(dfUnify$run)+1)
  
  dfUnify <- rbind(dfUnify,df)
}  

outname <- paste0(path,outfile)
outdf <- paste0(path,outfile,".df")
writeLines(header0,con<-file(outname))
close(con)
write.csv(dfUnify,file=outdf,row.names=FALSE)  
file.append(outname,outdf)        # write.csv cannot append
unlink(outdf)                     # delete temp file outdf  

cat(paste("Unified result written to \n   ",outname,"\n"))