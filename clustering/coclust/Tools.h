/*
  Tools.h

   Copyright (c) 2005, 2006
              by Hyuk Cho
   Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu

*/


#if !defined(_TOOLS_H_)
#define _TOOLS_H_

#include "Matrix.h"
#include "Coclustering.h"
#include "TimerUtility.h"

#define MAX_DESC_STR_LENGTH	9

extern long memory_consume;

struct sparseStruct
{
  char descString[MAX_DESC_STR_LENGTH];	// description string
  int numRow;
  int numCol;
  int numVal;
  int *colPtr;
  int *rowIdx;
  double *value;
};


//struct for a dense matrix
struct denseStruct
{
  int numRow;
  int numCol;
  int numVal;
  double **value;
};

void printUsage();			// USAGE:
void printDescription();		// DESCRIPTION:
void printAuthor();			// AUTHORS:
void printAlgorithmType();		// -A
void printColClusterNum();		// -C
void printDumpLevel();			// -D
void printShowingEachCluster();		// -E
void printHelp();			// -H
void printInputMatrixType();		// -I
void printComputingOneWayObject();	// -J
void printClassLabelSelection();	// -K
void printSmoothingType();		// -M
void printRunNum();			// -N
void printOutputLabelType();		// -O
void printRowClusterNum();		// -R
void printSeedingType();		// -S
void printThresholdType();		// -T
void printUpdateType();			// -U
void printTakingReverse();		// -X

// set default command-line arguments
void setCommandLine(commandLineArgument &myCLA);

// get command-line arguments
char **getAlgorithmType(int argc, char **argv, commandLineArgument &myCLA);
char **getColClusterNum(int argc, char **argv, commandLineArgument &myCLA);
char **getDumpLevel(int argc, char **argv, commandLineArgument &myCLA);
char **getShowingEachCluster(int argc, char **argv, commandLineArgument &myCLA);
char **getInputFileInformation(int argc, char **argv, commandLineArgument &myCLA);
char **getComputingOneWayObjective(int argc, char **argv, commandLineArgument &myCLA);
char **getClassFileInformation(int argc, char **argv, commandLineArgument &myCLA);
char **getLocalSearchLength(int argc, char **argv, commandLineArgument &myCLA);
char **getSmoothingType(int argc, char **argv, commandLineArgument &myCLA);
char **getRunNum(int argc, char **argv, commandLineArgument &myCLA);
char **getOutputFileInformation(int argc, char **argv, commandLineArgument &myCLA);
char **getRowClusterNum(int argc, char **argv, commandLineArgument &myCLA);
char **getSeedingInformation(int argc, char **argv, commandLineArgument &myCLA);
char **getThresholdInformation(int argc, char **argv, commandLineArgument &myCLA);
char **getUpdateInformation(int argc, char **argv, commandLineArgument &myCLA);
char **getTakingReverse(int argc, char **argv, commandLineArgument &myCLA);
void getCommandLine(int argc, char **argv, commandLineArgument &myCLA);

void extractFilename(char *path, char *fname);
void makeFilename(char *filename, char *suffix, commandLineArgument &myCLA);
int *readMatrix(char *fname, denseStruct *mat, int &numEmptyCol, int formatType, int matrixType);
int *readMatrix(char *fname, sparseStruct *mat, int &numEmptyCol, int formatType, char *scalingType);
//void myReadMatrix(commandLineArgument myCLA, sparseStruct &sparseCCS, sparseStruct &sparseCRS, denseStruct &denseMat);
//void myPreprocessMatrix(commandLineArgument myCLA, sparseStruct &sparseCCS, sparseStruct &sparseCRS, denseStruct &denseMat, Matrix *myCCS, Matrix *myCRS);
//void myConstructCoclustering(commandLineArgument myCLA, Matrix *myCCS, Matrix *myCRS, Coclustering *myCC);
int readLabel(char *fname, int num, int *label, int classOffsetType);
void readLabel(char *filename, int numLabel, int *label, int &numClass, int classOffsetType);
void readLabel(istream &labelFile, int numLabel, int *Label, int &numClass, int classOffsetType);
void readLabel(char *filename, int numRow, int numCol, int *rowLabel, int *colLabel, int &numRowClass, int &numColClass, int classOffsetType);
void readLabel(istream &labelFile, int numRow, int numCol, int *rowLabel, int *colLabel, int &numRowClass, int &numColClass, int classOffsetType);
void getRank(int arrin[], int indx[], int *n );
void convertSparse2Sparse(int numPtr, int numIdx, int numVal, int *fromPtr, int *fromIdx, double *fromVal, int *toPtr, int *toIdx, double *toVal);
void checkConvertSparse2Sparse(int numPtr, int numIdx, int numVal, int *fromPtr, int *fromIdx, double *fromVal, int *toPtr, int *toIdx, double *toVal);
double computeMutualInfo(double **matrix, int numRow, int numCol, double *marginX, double *marginY);
double computeMutualInfo(double **matrix, int numRC, int numCC);
void getStatistics(int *val, int num, double &average, double &variance, double &stdDev);
void getStatistics(double *val, int num, double &average, double &variance, double &stdDev);
void getMinMax(int *value, int num, int &min, int &max);
void getMinMax(double *value, int num, double &min, double &max);
void outputStatistics(commandLineArgument &myCLA, char *classPrefix, int *value, char *msg, ostream &dumpFile, ostream &statisticsFile);
void outputStatistics(commandLineArgument &myCLA, char *classPrefix, double *value, char *msg, ostream &dumpFile, ostream &statisticsFile);
void outputRunTime(TimerUtil &runTime, commandLineArgument &myCLA, char *classPrefix, ostream &dumpFile, ostream &statisticsFile);
void outputConstructor(commandLineArgument &myCLA, char *classPrefix, ostream &dumpFile, ostream &statisticsFile);
void outputDeconstructor(commandLineArgument &myCLA, char *classPrefix, ostream &dumpFile, ostream &statisticsFile);
void writeCoefMatrix(Matrix *matrix, int n, char *outFilename, char *suffix, int format, char *prefix);
void writeCluster(int *cluster, int n, int m, char *fname, char *suffix, int format, char *prefix);
void outputCommandLineArgument(int argc, char **argv, ostream &os);

#endif // !defined(_TOOLS_H_)
