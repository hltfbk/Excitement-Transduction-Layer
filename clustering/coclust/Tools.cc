/*
  Tools.cc
    Implementation of the Tools class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/

  
#include <fstream>
#include <iostream>
#include <iomanip>
#include <cmath>
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <time.h>

#include "TimerUtility.h"
#include "Constants.h"
#include "Tools.h"

using namespace std;


void printUsage()
{
  printf("\n");
  printf("USAGE\n");
  printf("\t%s-'uname' [SWITCHES]\n", EXEC_PROGRAM);
}


void printDescription()
{
  printf("DESCRIPTION\n");
  printf("\tImplements three co-clustering algorithms (in C++):\n");
  printf("\tinformation-theoretic co-clustering and two types of\n");
  printf("\tminimum sum-squared residue co-clustering algorithms\n");
}


void printAuthor()
{
  printf("AUTHORS\n");
  printf("\tHyuk Cho\n");
  printf("\tCopyright (c) 2005, 2006\n");
  printf("\tHyuk Cho, Yuqiang Guan, and Suvrit Sra\n");
  printf("\t{hyukcho, yguan, suvrit}@cs.utexas.edu\n");
  printf("\tCopyright (c) 2003, 2004\n");
}



void printAlgorithmType()
{
  printf("REQUIRED SWITCHES\n");
  printf("\tAt least, the following switches are required: -A -C -I -R\n");
  printf("DESCRIPTION OF SWITCHES\n");
  printf("\t-A  algorithmType  (REQUIRED)\n");
  printf("\t    specifies type of coclustering algorithm\n");
  printf("\t      e -- euclidean coclustering algorithm (DEFAULT)\n");
  printf("\t      i -- information theoretic coclustering algorithm\n");
  printf("\t      r -- minimim squared residue coclustering algorithm\n");
}


void printColClusterNum()
{
  printf("\t-C  colClusterNum  (REQUIRED)\n");
  printf("\t    specifies number of column clusters (DEFAULT = %d)\n", DEFAULT_numColCluster);
}


void printDumpLevel()
{
  printf("\t-D  dumpLevel  [dumpAccessMode  dumpFilename]\n");
  printf("\t    specifies level of intermediate information dump\n"); 
  printf("\t    dumpLevel can be one of\n");
  printf("\t      0 -- minimal information (DEFAULT)\n");
  printf("\t      1 -- objective function value in Batch Update and minimal statistics\n");
  printf("\t      2 -- objective function value in local search and maximal statistics\n");
  printf("\t      3 -- maximal information in a specified file and minimal on stdout\n");
  printf("\t           dumpAccessMode is required after '-D 3' and one of\n");
  printf("\t             a -- append mode\n");
  printf("\t             o -- output mode\n");
  printf("\t           dumpFilename is required after '-D 3 a/o'\n");
}


void printShowingEachCluster()
{
  printf("\t-E  outputEachClsuter\n");
  printf("\t    specifies detail ouputs in external cluster validattion section\n");
  printf("\t    outputEachCluster can be one of\n");
  printf("\t      0 -- don't output a confusion matrix and each cluster's statistics (DEFAULT)\n");
  printf("\t      1 -- output them in external cluster validation section\n");
}


void printInputMatrixType()
{
  printf("\t-I  inputMatrixType  inputFormatType  inputFilename  (REQUIRED)\n");
  printf("\t    specifies details of input matrix file\n");
  printf("\t    inputMatrixType can be one of\n");
  printf("\t      d -- dense (i.e., rectangle/square) matrix\n");
  printf("\t           inputFormatType after '-I d' can be one of\n");
  printf("\t             s -- dimension and matrix stored separately\n");
  printf("\t             t -- both dimension and matrix stored together\n");
  printf("\t      s -- sparse matrix in CCS\n");
  printf("\t           inputFormatType after '-I s' can be one of\n");
  printf("\t             f -- tfn scaling\n");
  printf("\t             x -- txx scaling\n");
}


void printComputingOneWayObject()
{
  printf("\t-J  oneWay\n");
  printf("\t    specifies to get one-way row/column clustering objective function values\n");
  printf("\t    oneWay can be one of\n");
  printf("\t      0 -- don't compute one-way objective function value(s) (DEFAULT)\n");
  printf("\t      1 -- compute one-way objective function value(s)\n");
}


void printClassLabelSelection()
{
  printf("\t-K  classLabelSelection  classOffsetType  classLabelFilename\n");
  printf("\t    specifies details of class label file\n");
  printf("\t    classLabelSelection can be one of\n");
  printf("\t      b -- both row and column class labels\n");
  printf("\t      c -- column class labels\n");
  printf("\t      r -- row class labels\n");
  printf("\t    classOffsetType can be one of\n");
  printf("\t             0 -- class label index starts from 0\n");
  printf("\t             1 -- class label index starts from 1\n");
}


void printSmoothingType()
{
  printf("\t-M  smoothingType  [rowSmoothingMagnitude  colSmoothingMagnitude]\n");
  printf("\t    specifies details of smoothing techniques\n");
  printf("\t    smoothingType can be one of\n");
  printf("\t      a -- annealing of uniform smoothing (DEFAULT = %g)\n", DEFAULT_rowAnnealingFactor);
  printf("\t      h -- maximum entropy smoothing\n");
  printf("\t      n -- no smoothing (DEFAULT)\n");
  printf("\t      u -- uniform smoothing (DEFAULT = %g)\n", DEFAULT_rowSmoothingFactor);
  printf("\t           smoothingMagnitude is required for '-M a/h/u'\n");
}


void printRunNum()
{
  printf("\t-N  runNum\n");
  printf("\t    specifies number of runs to get averaged statistics (DEFAULT = %d)\n", DEFAULT_numRun);
}


void printOutputLabelType()
{
  printf("\t-O  outputFileType  [outputLabelType  outputOffsetType]  outputAccessMode  outputFilename\n");
  printf("\t    specifies details of cocluster/objective/statistics file information\n");
  printf("\t    outputFileType can be one of\n");
  printf("\t      c -- output co-clusters\n");
  printf("\t           outputLabelType is required after '-O c' and can be one of\n");
  printf("\t             b -- each co-cluster block consisting of three rows like\n");
  printf("\t                    #rows #columns (in 1st row)\n");
  printf("\t                    list of row #s in (1,1)-th co-cluster (in 2nd row)\n");
  printf("\t                    list of column #s in (1,2)-th column co-cluster (in 3rd row)\n");
  printf("\t                    and so on\n");
  printf("\t             s -- all co-clusters represented in a simple format of two rows like (DEFAULT)\n");
  printf("\t                    list of row cluster labels of all rows (in 1st row)\n");
  printf("\t                    list of column cluster labels of all columns (in 2nd row)\n");
  printf("\t           outputOffsetType is required after '-O c b' and outputLabelType and can be one of\n");
  printf("\t                    0 -- cluster label index starts from 0\n");
  printf("\t                    1 -- cluster label index starts from 1\n");
  printf("\t      o -- output objective function value(s)\n");
  printf("\t      s -- output satistical information\n");
  printf("\t    outputAccessMode can be one of\n");
  printf("\t                           a -- append mode, useful with '-N randomRunNum'\n");
  printf("\t                           o -- output mode\n");
}


void printRowClusterNum()
{
  printf("\t-R  rowClusterNum  (REQUIRED)\n");
  printf("\t    specifies number of row clusters (DEFAULT = %d)\n", DEFAULT_numRowCluster);
}


void printSeedingType()
{
  printf("\t-S  seedingType  seedingLabelSelection  [seedingPertValue\n"); 
  printf("\t                                         | (seedingOffsetType  numSeedingSet seedingFilename)\n"); 
  printf("\t                                         | (numRowPermutation | numColPermutation)]\n");
  printf("\t    specifies details of initial cluster assignment\n");
  printf("\t    seedingType can be one of\n");
  printf("\t      f -- farthest apart assignment\n");
  printf("\t      m -- permute an initial random cluster vector\n");
  printf("\t           numRowPermutation or/and numColPermutation is/are required after '-S m b/c/r'\n");
  printf("\t                    numRowPermutation  numColPermutation -- after '-S m b'\n"); 
  printf("\t                    numColPermutation -- after '-S m c'\n"); 
  printf("\t                    numRowPermutation -- after '-S m r'\n");
  printf("\t      p -- perturbates cluster centroids\n");
  printf("\t           seedingPertValue is required after '-S p'\n");
  printf("\t      r -- random assignment (DEFAULT)\n");
  printf("\t      s -- read cluster labels from a seeding file\n");        
  printf("\t           seedingOffsetType is required after '-S s b/c/r' and one of\n");
  printf("\t                    0 -- seeding label index starts from 0\n");
  printf("\t                    1 -- seeding label index starts from 1\n");
  printf("\t           numSeedingSet is required after '-S s b/c/r 0/1'\n");
  printf("\t                           n -- positive integer (>=1)\n");
  printf("\t           seedingFilename is required after '-S s b/c/r/ 0/1 n'\n");
  printf("\t    seedingLabelSelection can be one of\n");
  printf("\t             b -- both row and column cluster labels\n");
  printf("\t             c -- column cluster labels\n");
  printf("\t             r -- row cluster labels\n");
}


void printThresholdType()
{
  printf("\t-T  thresholdType  rowThreshold  colThreshold\n");
  printf("\t    specifies details of threshold for either batch update or local search\n");
  printf("\t    thresholdType can be one of\n");
  printf("\t      b -- batch update (default = +%g)\n", DEFAULT_rowBatchUpdateThreshold);
  printf("\t      l -- local search (default = %g)\n", DEFAULT_rowLocalSearchThreshold);
  printf("\t    Both rowThreshold and colThreshold are required.\n");
}


void printUpdateType()
{
  printf("\t-U  updateType  [updateSelection  |  (rowLocalSearchLength  colLocalSearchLength)]\n");
  printf("\t    specifies details of cluster-centroid-updating order in batch update or local search\n");
  printf("\t    updateType can be one of\n");
  printf("\t      b -- batch update\n");
  printf("\t           updateSelection is required after '-U b' and can be one of\n");
  printf("\t             0 -- single row and single column, respectively (DEFAULT)\n");
  printf("\t             1 -- single row and single column, in batch mode\n");
  printf("\t             2 -- single row or single column, flipping a pair coin\n");
  printf("\t             3 -- multiple run of either row or column, flipping a pair coin\n");
  printf("\t      l -- local search\n");
  printf("\t           rowLocalSearchLength (DEFAULT = %d) is required after 'l'\n", DEFAULT_rowLocalSearchLength);
  printf("\t           colLocalSearchLength (DEFAULT = %d) is required after 'l' and rowLocalSearchLength\n", DEFAULT_colLocalSearchLength);
  printf("\t           To avoid empty row/column cluster(s), use -1 for row/colLocalSearchLength.\n");
}


void printTakingReverse()
{
  printf("\t-X  anticorrelation\n");
  printf("\t    specifies to capture anti-correlated rows (by taking reverse of rows)\n");
  printf("\t    anticorrelation can be one of\n");
  printf("\t      0 -- don't capture anti-correlated rows (DEFAULT)\n");
  printf("\t      1 -- capture anti-correlated rows\n");
}


// show command-line parameters...
void printHelp()
{
  printUsage();
  printDescription();
  printAuthor();
  
  printAlgorithmType();  
  printColClusterNum();
  printDumpLevel();
  printShowingEachCluster();
  printInputMatrixType();  
  printComputingOneWayObject();
  printClassLabelSelection();
  printSmoothingType();
  printRunNum();
  printOutputLabelType();
  printRowClusterNum();          
  printSeedingType();
  printThresholdType();
  printUpdateType();
  printTakingReverse();

  printf("\n");
  exit(EXIT_SUCCESS);
}


// set default parameters...
void setCommandLine(commandLineArgument &myCLA)
{
 // default parameters 
  myCLA.numInvalidCLA			= 0;
  myCLA.algorithmType 			= DEFAULT_algorithmType;
  myCLA.numColCluster 			= DEFAULT_numColCluster;
  myCLA.dumpLevel 			= DEFAULT_dumpLevel;
  myCLA.showingEachCluster		= DEFAULT_showingEachCluster;
  myCLA.inputMatrixType 		= DEFAULT_inputMatrixType;
  myCLA.inputFormatType			= DEFAULT_inputFormatType;
  myCLA.computingOneWayObjective 	= DEFAULT_computingOneWayObjective;
  myCLA.externalValidityType		= DEFAULT_externalValidityType;
  myCLA.classOffsetType			= DEFAULT_classOffsetType;
  myCLA.numRowClass 			= DEFAULT_numRowClass;	// used in validation
  myCLA.numColClass 			= DEFAULT_numColClass;	// used in validation
  myCLA.smoothingType 			= DEFAULT_smoothingType;
  myCLA.rowSmoothingFactor 		= DEFAULT_rowSmoothingFactor;
  myCLA.colSmoothingFactor 		= DEFAULT_colSmoothingFactor;
  myCLA.numRun 				= DEFAULT_numRun;
  myCLA.coclusterOffsetType		= DEFAULT_coclusterOffsetType;
  myCLA.coclusterLabelType 		= DEFAULT_coclusterLabelType;
  myCLA.coclusterAccessMode 		= DEFAULT_coclusterAccessMode;
  myCLA.numRowCluster 			= DEFAULT_numRowCluster;
  myCLA.colInitializationMethod 	= DEFAULT_colInitializationMethod;
  myCLA.rowInitializationMethod		= DEFAULT_rowInitializationMethod;
  myCLA.rowSeedingOffsetType		= DEFAULT_rowSeedingOffsetType;
  myCLA.colSeedingOffsetType		= DEFAULT_colSeedingOffsetType;
  myCLA.numRowSeedingSet		= DEFAULT_numRowSeedingSet;
  myCLA.numColSeedingSet		= DEFAULT_numColSeedingSet;
  myCLA.rowSeedingAccessMode		= DEFAULT_rowSeedingAccessMode;
  myCLA.colSeedingAccessMode		= DEFAULT_colSeedingAccessMode;
  myCLA.perturbationMagnitude		= DEFAULT_perturbationMagnitude;
  myCLA.numRowPermutation		= DEFAULT_numRowPermutation;
  myCLA.numColPermutation		= DEFAULT_numColPermutation;
  myCLA.rowBatchUpdateThreshold 	= DEFAULT_rowBatchUpdateThreshold;
  myCLA.colBatchUpdateThreshold 	= DEFAULT_colBatchUpdateThreshold;
  myCLA.rowLocalSearchThreshold 	= DEFAULT_rowLocalSearchThreshold;
  myCLA.colLocalSearchThreshold 	= DEFAULT_colLocalSearchThreshold;
  myCLA.batchUpdateType 		= DEFAULT_batchUpdateType;
  myCLA.localSearchType			= DEFAULT_localSearchType;	// not used...
  myCLA.rowLocalSearchLength 		= DEFAULT_rowLocalSearchLength;
  myCLA.colLocalSearchLength 		= DEFAULT_colLocalSearchLength;    
  myCLA.takingReverse			= DEFAULT_takingReverse;
  myCLA.havingArgument			= DEFAULT_havingArgument;

  myCLA.emptyRowId			= NULL;	// not used...
  myCLA.emptyColId 			= NULL;
  myCLA.numEmptyRow			= 0;
  myCLA.numEmptyCol 			= 0;
  myCLA.rowClassLabel 			= NULL;		// used in validation
  myCLA.colClassLabel 			= NULL;		// used in validation

  // input and output files
  strncpy(myCLA.dumpFilename, EMPTY_STRING, FILENAME_LENGTH );
  strncpy(myCLA.inputFilename, EMPTY_STRING, FILENAME_LENGTH);
  strncpy(myCLA.bothClassFilename, EMPTY_STRING, FILENAME_LENGTH);
  strncpy(myCLA.rowClassFilename, EMPTY_STRING, FILENAME_LENGTH);
  strncpy(myCLA.colClassFilename, EMPTY_STRING, FILENAME_LENGTH);
  strncpy(myCLA.objectiveFilename, EMPTY_STRING, FILENAME_LENGTH);
  strncpy(myCLA.coclusterFilename, EMPTY_STRING, FILENAME_LENGTH);
  strncpy(myCLA.bothSeedingFilename, EMPTY_STRING, FILENAME_LENGTH);		// not used
  strncpy(myCLA.rowSeedingFilename, EMPTY_STRING, FILENAME_LENGTH);
  strncpy(myCLA.colSeedingFilename, EMPTY_STRING, FILENAME_LENGTH);
  strcpy(myCLA.scalingType, TXX_SCALING);		// used only for the matrix in CCS
}


// get type of co-clustering algorithm from command-line...
char **getAlgorithmType(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  switch (toupper((*(++argv))[0])){
    case MSSRCC_I_ALGORITHM:
      myCLA.algorithmType = MINIMUM_SUM_SQUARE_RESIDUE_I_CC;
      break;
    case ITCC_ALGORITHM:
      myCLA.algorithmType = INFORMATION_THEORETIC_CC;
      break;
    case MSSRCC_II_ALGORITHM:
      myCLA.algorithmType = MINIMUM_SUM_SQUARE_RESIDUE_II_CC;
      break;
    default:
      cout << "  !!! Invalid algorithm type: " << *argv << " !!!" << endl << endl;
      validCLA = false;
      break;
  }
  if (!validCLA){
    printAlgorithmType();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get # of column clusters from command-line...
char **getColClusterNum(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  if (*(argv+1) == NULL){
    cout << "  !!! Invalid # of column cluster(s): (# of column cluster(s) should be specified.) !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA && !isdigit((*(argv+1))[0])){
    cout << "  !!! Invalid # of column cluster(s): " << *(argv+1) << " !!!" << endl << endl;
    validCLA = false;
  }
  int tempNum = atoi(*(argv+1));
  if (tempNum <= 0){
    cout << "  !!! Invalid # of column cluster(s): (# of column cluster(s) should be a positive integer.): " << tempNum << " !!!" << endl;
    validCLA = false;
  }
  if (validCLA)
    myCLA.numColCluster = atoi(*(++argv));
  if (!validCLA){
    printColClusterNum();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get dump-level from command-line...
char **getDumpLevel(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  if (*(argv+1) == NULL){
    cout << "  !!! Invalid dump level: (dump level should be specified.) !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA && !isdigit((*(argv+1))[0])){
    cout << "  !!! Invalid dump level: " << *(argv+1) << " !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA){
    myCLA.dumpLevel = atoi(*(++argv));
    if ((myCLA.dumpLevel < MINIMUM_DUMP_LEVEL) || (myCLA.dumpLevel > MAXIMUM_DUMP_LEVEL)){
      cout << "  !!! Invalid dump level: " << myCLA.dumpLevel << " !!!" << endl << endl;
      validCLA = false;
    }
  }
  if (validCLA && (myCLA.dumpLevel == MAXIMUM_DUMP_LEVEL)){
    switch (toupper((*(++argv))[0])){
      case APPEND_OUT:
        myCLA.dumpAccessMode = APPEND_MODE;
	break;
      case OUTPUT_OUT:
        myCLA.dumpAccessMode = OUTPUT_MODE;
	break;
      default:
	cout << "  !!! Invalid dump file access mode: " << *argv << " !!!" << endl << endl;
        validCLA = false;
        break;
    }
    if (validCLA && (*(argv+1) == NULL)){
      cout << "  !!! Invalid dump filename: (dump filename should be specified.) !!!" << endl << endl;
      validCLA = false;
    }
    if (validCLA && (isdigit((*(argv+1))[0]) || ((*(argv+1))[0] == '-'))){
      cout << "  Invalid dump filename: " << *(argv+1) << endl << endl;
      validCLA = false;
    }
    if (validCLA)
      strcpy(myCLA.dumpFilename, *(++argv));
  }
  if (!validCLA){
    printOutputLabelType();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get information of showing a CM and each cluster's statistics...
char **getShowingEachCluster(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  if (*(argv+1) == NULL){
    cout << "  !!! Invalid parameter: (0 or 1 should be specified.) !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA && !isdigit((*(argv+1))[0])){
    cout << "  !!! Invalid parameter: " << *(argv+1) << " !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA){
    switch (atoi(*(++argv))){
      case ON:
        myCLA.showingEachCluster = true;
	break;
      case OFF:
        myCLA.showingEachCluster = false;
	break;
      default:
        cout << " !!! Invalid parameter: (0 or 1 should be specified.): " << myCLA.numRun << " !!!" << endl << endl;
        validCLA = false;
	break;
    }
  }
  if (!validCLA){
    printShowingEachCluster();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get input file information from command-line...
char **getInputFileInformation(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  switch (toupper((*(++argv))[0])){
    case DENSE_INPUT:
      myCLA.inputMatrixType = DENSE_MATRIX;
      switch (toupper((*(++argv))[0])){
	case SEPARATE_INPUT:
	  myCLA.inputFormatType = DIM_MATRIX_SEPARATE_FORMAT;
	  break;
        case TOGETHER_INPUT:
          myCLA.inputFormatType = DIM_MATRIX_TOGETHER_FORMAT;
	  break;
        default:
          cout << "  !!! Invalid dense matrix format type: " << *argv << " !!!" << endl << endl;
          validCLA = false;
	  break;
      }
      break;
    case SPARSE_INPUT:
      myCLA.inputMatrixType = SPARSE_MATRIX;
      switch (toupper((*(++argv))[0])){
	case TFN_INPUT:
	  strcpy(myCLA.scalingType, TFN_SCALING);	// In fact, it's not used.
	  myCLA.inputFormatType = TFN_FILE_FORMAT;
	  break;
        case TXX_INPUT:
          strcpy(myCLA.scalingType, TXX_SCALING);	// In fact, it's not used
	  myCLA.inputFormatType = TXX_FILE_FORMAT;
	  break;
        default:
          cout << "  !!! Invalid sparse matrix format type: " << *argv << " !!!" << endl << endl;
          validCLA = false;
	  break;
      }
      break;
    default:  
      cout << "  !!! Invalid input matrix argument(s): " << *argv << " !!!" << endl << endl;
      validCLA = false;
      break;
  }
  if (validCLA && ((*(++argv))[0] != '-')){
    sprintf(myCLA.inputFilename, "%s",*argv);
    if (strcmp(myCLA.coclusterFilename, "") == 0)
      extractFilename(*argv, myCLA.coclusterFilename);
    strcpy(myCLA.dumpFilename, myCLA.coclusterFilename);
    strcpy(myCLA.objectiveFilename, myCLA.coclusterFilename);
    myCLA.havingArgument = true;
  } else {
     cout << "  !!! Invalid input matrix argument(s): " << *argv << " !!!" << endl << endl;
     validCLA = false;
  }
  if (!validCLA){
    printInputMatrixType();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get information of computing one-way clustering objective function values from command-line...
char **getComputingOneWayObjective(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  if (*(argv+1) == NULL){
    cout << "  !!! Invalid parameter: (0 or 1 should be specified.) !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA && !isdigit((*(argv+1))[0])){
    cout << "  !!! Invalid parameter: " << *(argv+1) << " !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA){
    switch (atoi(*(++argv))){
      case ON:
        myCLA.computingOneWayObjective = true;
	break;
      case OFF:
        myCLA.computingOneWayObjective = false;
	break;
      default:
        cout << " !!! Invalid parameter: (0 or 1 should be specified.): " << myCLA.numRun << " !!!" << endl << endl;
        validCLA = false;
	break;
    }
  }
  if (!validCLA){
    printComputingOneWayObject();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get class (label) file information from command-line...	  
char **getClassFileInformation(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  switch (toupper((*(++argv))[0])){
    case BOTH_LABEL:
      myCLA.externalValidityType = BOTH_EXTERNAL_VALIDITY;
      break;
    case COL_LABEL:
      myCLA.externalValidityType = COL_EXTERNAL_VALIDITY;
      break;
    case ROW_LABEL:
      myCLA.externalValidityType = ROW_EXTERNAL_VALIDITY;
      break;
    default:
      cout << "  !!! Invalid class matrix selection: " << *argv << " !!!" << endl << endl;
      validCLA = false;
      break;
  }
  if (validCLA){
    switch ((*(++argv))[0]){
      case LABEL_FROM_0:
        myCLA.classOffsetType = START_FROM_0;
        break;
      case LABEL_FROM_1:
        myCLA.classOffsetType = START_FROM_1;
        break;
      default:
        cout << "  !!! Invalid class matrix offset type: " << *argv << " !!!" << endl << endl;
        validCLA = false;
        break;
    }
  }
  if (validCLA && (*(argv+1) == NULL)){
    cout << "  !!! Invalid class filename: (class filename should be specified.) !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA && (isdigit((*(argv+1))[0]) || ((*(argv+1))[0] == '-'))){
    cout << "  !!! Invalid class filename: " << *(argv+1) << " !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA){
    switch (myCLA.externalValidityType){
      case BOTH_EXTERNAL_VALIDITY:
        strcpy(myCLA.bothClassFilename, *(++argv));
        break;
      case COL_EXTERNAL_VALIDITY:
        strcpy(myCLA.colClassFilename, *(++argv));
        break;
      case ROW_EXTERNAL_VALIDITY:
        strcpy(myCLA.rowClassFilename, *(++argv));
        break;
      default:
        validCLA = false;
	break;
    }
  }      
  if (!validCLA){
    printClassLabelSelection();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


/*
// get length of local search from command-line...
char **getLocalSearchLength(int argc, char **argv, commandLineArgument &myCLA)
{
  myCLA.localSearchLength = atoi(*(++argv));
  return argv;
}
*/

// get type of smoothing from command-line...
char **getSmoothingType(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  char tempSmoothingType = toupper((*(++argv))[0]);
  switch (tempSmoothingType){
    case ANNEALING_TYPE:
      break;
    case MAXIMUM_ENTROPY_TYPE:
      myCLA.smoothingType = MAXIMUM_ENTROPY_SMOOTHING;
      break;
    case NO_TYPE:
      myCLA.smoothingType = NO_SMOOTHING;
      break;
    case UNIFORM_TYPE:
      myCLA.smoothingType = UNIFORM_SMOOTHING;
      break;
    default:
      cout << "  !!! Invalid smoothing type: " << *argv << " !!!" << endl << endl;
      validCLA = false;
      break;
  }
  if (validCLA && (tempSmoothingType != NO_TYPE)){
    if (*(argv+1) == NULL){
      cout << "  !!! Invalid row smoothing magnitude: (row smoothing magnitude should be specified.) !!!" << endl << endl;
      validCLA = false;
    }
    if (validCLA && (!isdigit((*(argv+1))[0]) && ((*(argv+1))[0] != '.'))){
      cout << "  !!! Invalid row smoothing magnitude: " << *(argv+1) << " !!!" << endl << endl;
      validCLA = false;
    }
    if (validCLA && (*(argv+2) == NULL)){
      cout << "  !!! Invalid col smoothing magnitude: (col smoothing magnitude should be specified.) !!!" << endl << endl;
      validCLA = false;
    }
    if (validCLA && (!isdigit((*(argv+2))[0]) && ((*(argv+2))[0] != '.'))){
      cout << "  !!! Invalid col smoothing magnitude: " << *(argv+2) << " !!!" << endl << endl;
      validCLA = false;
    }
    if (validCLA){
      switch (tempSmoothingType){
        case ANNEALING_TYPE:
          myCLA.rowAnnealingFactor = atof(*(++argv));
          myCLA.colAnnealingFactor = atof(*(++argv));
	  break;
        case MAXIMUM_ENTROPY_TYPE:
        case UNIFORM_TYPE:
          myCLA.rowSmoothingFactor = atof(*(++argv));
          myCLA.colSmoothingFactor = atof(*(++argv));
          break;
        case NO_TYPE:
          break;
        default:
          cout << "  !!! Invalid smoothing parameter(s): " << *argv << " !!!" << endl << endl;
          validCLA = false;
        break;
      }
    }
  }  
  if (!validCLA){
    printSmoothingType();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get number of co-clustering running(s) from command-line...
char **getRunNum(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  if (*(argv+1) == NULL){
    cout << "  !!! Invalid # of run(s): (# of runs should be specified.) !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA && !isdigit((*(argv+1))[0])){
    cout << "  !!! Invalid # of run(s): " << *(argv+1) << " !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA){
    myCLA.numRun = atoi(*(++argv));
    if (myCLA.numRun <= 0){
      cout << " !!! Invalid # of run(s): " << myCLA.numRun << " !!!" << endl << endl;
      validCLA = false;
    }
  }
  if (!validCLA){
    printRunNum();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get output file information from command-line...
char **getOutputFileInformation(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  char tempOutputFileType = toupper((*(++argv))[0]);
  switch (tempOutputFileType){
    case OUTPUT_COCLUSTER_FILE:
      switch (toupper((*(++argv))[0])){
        case BLOCK_OUT:
          myCLA.coclusterLabelType = BLOCK_FORMAT;
          break;
        case SIMPLE_OUT:
          myCLA.coclusterLabelType = SIMPLE_FORMAT;
          break;
        default:
          cout << "  !!! Invalid cocluster label type: " << *argv << " !!!" << endl << endl;
          validCLA = false;
          break;
      } 
      switch ((*(++argv))[0]){
        case LABEL_FROM_0:
          myCLA.coclusterOffsetType = START_FROM_0;
          break;
        case LABEL_FROM_1:
          myCLA.coclusterOffsetType = START_FROM_1;
          break;
        default:
          cout << "  !!! Invalid cocluster label offset type: " << *argv << " !!!" << endl << endl;
          validCLA = false;
          break;
      }	      	      
      break;
    case OUTPUT_OBJECTIVE_FILE:
      break;
    case OUTPUT_STATISTICS_FILE:  
      break;
    default:
      cout << "  !!! Invalid output file type: " << *argv << " !!!" << endl << endl;
      validCLA = false;
      break;
  }
  int tempFileAccessMode = 0;
  if (validCLA){
    switch (toupper((*(++argv))[0])){
      case APPEND_OUT:
        tempFileAccessMode = APPEND_MODE;
        break;
      case OUTPUT_OUT:
        tempFileAccessMode = OUTPUT_MODE;
        break;
      default:
        cout << "  !!! Invalid file access mode: " << *argv << " !!!" << endl << endl;
        validCLA = false;
        break;
    }
    switch (tempOutputFileType){
      case OUTPUT_COCLUSTER_FILE:
        myCLA.coclusterAccessMode = tempFileAccessMode;
        break;
      case OUTPUT_OBJECTIVE_FILE:
        myCLA.objectiveAccessMode = tempFileAccessMode;
        break;
      case OUTPUT_STATISTICS_FILE:
        myCLA.statisticsAccessMode = tempFileAccessMode;
        break;
      default:
        validCLA = false;
        break;
    }
  }
  if (validCLA && (*(argv+1) == NULL)){
    cout << "  !!! Invalid output filename: (output filename should be specified.) !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA && (isdigit((*(argv+1))[0]) || ((*(argv+1))[0] == '-'))){
    cout << "  !!! Invalid output filename: " << *(argv+1) << " !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA){
    switch (tempOutputFileType){
      case OUTPUT_COCLUSTER_FILE:
        strcpy(myCLA.coclusterFilename, *(++argv));
        break;
      case OUTPUT_OBJECTIVE_FILE:
        strcpy(myCLA.objectiveFilename, *(++argv));
        break;
      case OUTPUT_STATISTICS_FILE:
        strcpy(myCLA.statisticsFilename, *(++argv));
        break;
      default:
        validCLA = false;
	break;
    }
  }
  if (!validCLA){
    printOutputLabelType();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get number of row clusters from command-line...
char **getRowClusterNum(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  if (*(argv+1) == NULL){
    cout << "  !!! Invalid # of row cluster(s): (# of row cluster(s) should be specified.) !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA && !isdigit((*(argv+1))[0])){
    cout << "  !!! Invalid # of row cluster(s): " << *(argv+1) << " !!!" << endl << endl;
    validCLA = false;
  }
  int tempNum = atoi(*(argv+1));
  if (tempNum <= 0){
    cout << "  !!! Invalid # of row clusters(s): (# of row clsuter(s) should be a positive integer.): " << tempNum << " !!!" << endl;
    validCLA = false;
  }
  if (validCLA)
    myCLA.numRowCluster = atoi(*(++argv));
  if (!validCLA){
    printRowClusterNum();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get seeding (i.e., initialization) information from command-line...
char **getSeedingInformation(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  int tempNum;
  switch (toupper((*(++argv))[0])){
    case FARTHEST_SEEDING:
      switch (toupper((*(++argv))[0])){
        case BOTH_LABEL:
	  myCLA.rowInitializationMethod = FARTHEST_INIT;
	  myCLA.colInitializationMethod = FARTHEST_INIT;
	  break;
        case COL_LABEL:
	  myCLA.colInitializationMethod = FARTHEST_INIT;
	  break;
        case ROW_LABEL:
	  myCLA.rowInitializationMethod = FARTHEST_INIT;
	  break;
        default:
	  cout << "  !!! Invalid farthest apart seeding label type: " << *argv << " !!!" << endl << endl;
          validCLA = false;
	  break;
      }
      break;
    case PERTURBATION_SEEDING:
      switch (toupper((*(++argv))[0])){
        case BOTH_LABEL:
	  myCLA.rowInitializationMethod = PERTURBATION_INIT;
	  myCLA.colInitializationMethod = PERTURBATION_INIT;
	  break;
        case COL_LABEL:
	  myCLA.colInitializationMethod = PERTURBATION_INIT;
	  break;
        case ROW_LABEL:
	  myCLA.rowInitializationMethod = PERTURBATION_INIT;
	  break;
        default:
	  cout << "  !!! Invalid perturbation seeding label type: " << *argv << " !!!" << endl << endl;
          validCLA = false;
	  break;
      }
      if (validCLA && (!isdigit((*(argv+1))[0]) && ((*(argv+2))[0] != '.'))){
        cout << "  !!! Invalid perturbation magnitude: " << *argv << " !!!" << endl << endl;
	validCLA = false;
      }
      if (validCLA)
        myCLA.perturbationMagnitude = atoi(*(++argv));
      break;
    case RANDOM_SEEDING:
      switch (toupper((*(++argv))[0])){
        case BOTH_LABEL:
	  myCLA.rowInitializationMethod = RANDOM_INIT;
	  myCLA.colInitializationMethod = RANDOM_INIT;
	  break;
        case COL_LABEL:
	  myCLA.colInitializationMethod = RANDOM_INIT;
	  break;
        case ROW_LABEL:
	  myCLA.rowInitializationMethod = RANDOM_INIT;
	  break;
        default:
	  cout << "  !!! Invalid random seeding label type: " << *argv << " !!!" << endl << endl;
          validCLA = false;
	  break;
      }
      break;
    case SEEDING_SEEDING:
      switch (toupper((*(++argv))[0])){
        case BOTH_LABEL:
          myCLA.rowInitializationMethod = SEEDING_INIT;
	  myCLA.colInitializationMethod = SEEDING_INIT;
	  break;
        case COL_LABEL:
	  myCLA.colInitializationMethod = SEEDING_INIT;
	  break;
        case ROW_LABEL:
	  myCLA.rowInitializationMethod = SEEDING_INIT;
	  break;
        default:
	  cout << "  !!! Invalid seeding file seeding label type: " << *argv << " !!!" << endl << endl;
          validCLA = false;
	  break;
      }
      if (validCLA){
        switch ((*(++argv))[0]){
          case LABEL_FROM_0:
	    switch (toupper((*(argv-1))[0])){
	      case BOTH_LABEL:
	        myCLA.rowSeedingOffsetType = START_FROM_0;
	        myCLA.colSeedingOffsetType = START_FROM_0;
	        break;
	      case COL_LABEL:
	        myCLA.colSeedingOffsetType = START_FROM_0;
	        break;
	      case ROW_LABEL:
	        myCLA.rowSeedingOffsetType = START_FROM_0;
	        break;
	      default:
	        break;
	    }
	    break;
          case LABEL_FROM_1:
	    switch (toupper((*(argv-1))[0])){
	      case BOTH_LABEL:
	        myCLA.rowSeedingOffsetType = START_FROM_1;
	        myCLA.colSeedingOffsetType = START_FROM_1;
	        break;
	      case COL_LABEL:
	        myCLA.colSeedingOffsetType = START_FROM_1;
	        break;
	      case ROW_LABEL:
	        myCLA.rowSeedingOffsetType = START_FROM_1;
	        break;
	      default:
	        break;
	    }
	    break;
          default:
            cout << "  !!! Invalid seeding file seeding offset type: " << *argv << " !!!" << endl << endl;
            validCLA = false;
	    break;
        }
      }
      if (validCLA){
        if (*(argv+1) == NULL){
          cout << "  !!! Invalid # of row/col seeding set(s): (# of row/col seeding set(s) should be specified.) !!!" << endl << endl;
          validCLA = false;
        }
        if (validCLA && !isdigit((*(argv+1))[0])){
          cout << "  !!! Invalid # of row/col seeding set(s): " << *(argv+1) << " !!!" << endl << endl;
          validCLA = false;
        }
        if (validCLA){
          tempNum = atoi(*(argv+1));
	  if (tempNum <= 0){
	    cout << "  !!! Invalid # of seeding set(s): (# of seeding set(s) should be a positive integer.): " << tempNum << " !!!" << endl;
	    validCLA = false;
	  } else {
	    switch (toupper((*(argv-1))[0])){
	      case BOTH_LABEL:
	        myCLA.numRowSeedingSet = atoi(*(++argv));
	        myCLA.numColSeedingSet = myCLA.numRowSeedingSet;
	        myCLA.rowSeedingAccessMode = BOTH_INPUT_MODE;
	        myCLA.colSeedingAccessMode = NO_OPEN_MODE;
	        break;
	      case COL_LABEL:
	        myCLA.numColSeedingSet = atoi(*(++argv));
	        myCLA.colSeedingAccessMode = ONE_INPUT_MODE;
	        break;
	      case ROW_LABEL:
	        myCLA.numRowSeedingSet = atoi(*(++argv));
	        myCLA.rowSeedingAccessMode = ONE_INPUT_MODE;
	        break;
	      default:
	        cout << "  !!! Invalid seeding file seeding label type: " << *(argv-1) << " !!!" << endl << endl;
                validCLA = false;
	        break;
	    }
	  }
        }      
      }	      	      
      if (validCLA && (*(argv+1) == NULL)){
        cout << "  !!! Invalid seeding filename: (seeding filename should be specified.) !!!" << endl << endl;
        validCLA = false;
      }
      if (validCLA && (isdigit((*(argv+1))[0]) || ((*(argv+1))[0] == '-'))){
        cout << "  !!! Invalid seeding filename: " << *(argv+1) << " !!!" << endl << endl;
        validCLA = false;
      }
      if (validCLA){
        switch (toupper((*(argv-2))[0])){
          case BOTH_LABEL:
            strcpy(myCLA.rowSeedingFilename, *(++argv));
	    strcpy(myCLA.colSeedingFilename, myCLA.rowSeedingFilename);
	    strcpy(myCLA.bothSeedingFilename, myCLA.rowSeedingFilename);
            break;
          case COL_LABEL:
            strcpy(myCLA.colSeedingFilename, *(++argv));
            break;
          case ROW_LABEL:
            strcpy(myCLA.rowSeedingFilename, *(++argv));
            break;
          default:
	    cout << "  !!! Invalid seeding file seeding label type: " << *(argv-2) << " !!!" << endl << endl;
	    validCLA = false;
            break;
        }
      }
      break;      
    case PERMUTATION_SEEDING:
      switch (toupper((*(++argv))[0])){
        case BOTH_LABEL:
          if (*(argv+1) == NULL){
            cout << "  !!! Invalid row permutation number: (row permutation number should be specified.) !!!" << endl << endl;
            validCLA = false;
          }
          if (validCLA && !isdigit((*(argv+1))[0])){
            cout << "  !!! Invalid row permutation number: " << *(argv+1) << " !!!" << endl << endl;
            validCLA = false;
          }
          tempNum = atoi(*(argv+1));
	  if (tempNum <= 0){
	    cout << "  !!! Invalid row permutation number: (row permutation number should be a positive integer.): " << tempNum << " !!!" << endl;
	    validCLA = false;
	  }
          if (validCLA && (*(argv+2) == NULL)){
            cout << "  !!! Invalid col permutation number: (col permutation number should be specified.) !!!" << endl << endl;
            validCLA = false;
          }
          if (validCLA && !isdigit((*(argv+2))[0])){
            cout << "  !!! Invalid col permutation number: " << *(argv+2) << " !!!" << endl << endl;
            validCLA = false;
          }
	  tempNum = atoi(*(argv+2));
	  if (tempNum <= 0){
	    cout << "  !!! Invalid col permutation number: (col permutation number should be a positive integer.): " << tempNum << " !!!" << endl;
	    validCLA = false;
	  }
          if (validCLA){
            myCLA.numRowPermutation = atoi(*(++argv));
            myCLA.numColPermutation = atoi(*(++argv));
	    myCLA.rowInitializationMethod = PERMUTATION_INIT;
	    myCLA.colInitializationMethod = PERMUTATION_INIT;
          }
	  break;
        case COL_LABEL:
          if (*(argv+1) == NULL){
            cout << "  !!! Invalid col permutation number: (col permutation number should be specified.) !!!" << endl << endl;
            validCLA = false;
          }
          if (validCLA && !isdigit((*(argv+1))[0])){
            cout << "  !!! Invalid col permutation number: " << *(argv+1) << " !!!" << endl << endl;
            validCLA = false;
          }
	  tempNum = atoi(*(argv+1));
	  if (tempNum <= 0){
	    cout << "  !!! Invalid col permutation number: (col permutation number should be a positive integer.): " << tempNum << " !!!" << endl;
	    validCLA = false;
	  }
          if (validCLA){
	    myCLA.numColPermutation = atoi(*(++argv));
	    myCLA.colInitializationMethod = PERMUTATION_INIT;
	  }
	  break;
        case ROW_LABEL:
          if (*(argv+1) == NULL){
            cout << "  !!! Invalid row permutation number: (row permutation number should be specified.) !!!" << endl << endl;
            validCLA = false;
          }
          if (validCLA && !isdigit((*(argv+1))[0])){
            cout << "  !!! Invalid row permutation number: " << *(argv+1) << " !!!" << endl << endl;
            validCLA = false;
          }
          tempNum = atoi(*(argv+1));
	  if (tempNum <= 0){
	    cout << "  !!! Invalid row permutation number: (row permutation number should be a positive integer.): " << tempNum << " !!!" << endl;
	    validCLA = false;
	  }
          if (validCLA){
	    myCLA.numRowPermutation = atoi(*(++argv));
	    myCLA.rowInitializationMethod = PERMUTATION_INIT;
	  }
	  break;
        default:
	  cout << "  !!! Invalid permutation seeding label type: " << *argv << " !!!" << endl << endl;
          validCLA = false;
	  break;
      }
      break;
    default:
      cout << "  !!! Invalid seeding paramater(s): " << *argv << " !!!" << endl << endl;
      validCLA = false;
      break;
  }
  if (!validCLA){
    printSeedingType();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get threshold value for batch update or local search from command-line... 
char **getThresholdInformation(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  char tempThresholdInfo = toupper((*(++argv))[0]);
  switch (tempThresholdInfo){
    case BATCH_UPDATE_STEP:
    case LOCAL_SEARCH_STEP:
      break;
    default:
      cout << "  !!! Invalid threshold type: " << *argv << " !!!" << endl << endl;
      validCLA = false;
  }
  if (validCLA){ 
    if (*(argv+1) == NULL){
      cout << "  !!! Invalid row threshold: (row local search threshold should be specified.) !!!" << endl << endl;
      validCLA = false;
    }
    if (validCLA && (!isdigit((*(argv+1))[0]) && ((*(argv+1))[0] != '.'))){
      cout << "  !!! Invalid row threshold: " << *(argv+1) << " !!!" << endl << endl;
      validCLA = false;
    }
    if (validCLA && (*(argv+2) == NULL)){
      cout << "  !!! Invalid col threshold: (col threshold should be specified.) !!!" << endl << endl;
      validCLA = false;
    }
    if (validCLA && (!isdigit((*(argv+2))[0]) && ((*(argv+2))[0] != '.'))){
      cout << "  !!! Invalid col threshold: " << *(argv+2) << " !!!" << endl << endl;
      validCLA = false;
    }
    if (validCLA){
      switch (tempThresholdInfo){
        case BATCH_UPDATE_STEP:
          myCLA.rowBatchUpdateThreshold = atof(*(++argv));
	  myCLA.colBatchUpdateThreshold = atof(*(++argv));
        break;
        case LOCAL_SEARCH_STEP:
          myCLA.rowLocalSearchThreshold = atof(*(++argv));
          myCLA.colLocalSearchThreshold = atof(*(++argv));
          break;
        default:
          cout << "  !!! Invalid threshold type: " << *argv << " !!!" << endl << endl;
          validCLA = false;
          break;
      }
    }
  }
  if (!validCLA){
    printThresholdType();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get type of batch update and local search from command-line...
char **getUpdateInformation(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  switch (toupper((*(++argv))[0])){
    case BATCH_UPDATE_STEP:
      if (*(argv+1) == NULL){
        cout << "  !!! Invalid batch update type: (batch update type should be specified.) !!!" << endl << endl;
        validCLA = false;
      }
      if (validCLA && (!isdigit((*(argv+1))[0]))){
        cout << "  !!! Invalid batch update type: " << *(argv+1) << " !!!" << endl << endl;
        validCLA = false;
      }
      if (validCLA){
        switch ((*(++argv))[0]){
          case SINGLE_UPDATE:
	    myCLA.batchUpdateType = SINGLE_RESPECTIVELY;
	    break;
	  case SINGLE_SINGLE_UPDATE:
	    myCLA.batchUpdateType = SINGLE_IN_BATCH;
	    break;
          case MULTIPLE_UPDATE:
	    myCLA.batchUpdateType = MULTIPLE_RESPECTIVELY;
	    break;
          case SINGLE_FLIP:
	    myCLA.batchUpdateType = SINGLE_BY_FLIP;
	    break;
	  case MULTIPLE_FLIP:
	    myCLA.batchUpdateType = MULTIPLE_BY_FLIP;
	    break;
          default:
            cout << "  !!! Invalid batch update type: " << *argv << " !!!" << endl << endl;
            validCLA = false;
	    break;
        }
      }
      break;
    case LOCAL_SEARCH_STEP:
/*
      switch (toupper((*(++argv))[0])){
	case 'B':
          myCLA.localSearchType = BOTH_ROW_AND_COL;
	  break;
	case 'C':
          myCLA.localSearchType = COL_ONLY;
	  break;
	case 'R':
          myCLA.localSearchType = ROW_ONLY;
	  break;
        default:
          cout << "  Invalid local search update type: " << *argv << endl << endl;
          validCLA = false;
 	  break;
      }
*/
      if (*(argv+1) == NULL){
        cout << "  !!! Invalid row local search length: (row local search length should be specified.) !!!" << endl << endl;
        validCLA = false;
      }
      if (validCLA && (!isdigit((*(argv+1))[0]) && (*(argv+1))[0] != '-')){
        cout << "  !!! Invalid row local search length: " << *(argv+1) << " !!!" << endl << endl;
        validCLA = false;
      }
      if (validCLA && (*(argv+2) == NULL)){
        cout << "  !!! Invalid col local search length: (col local search length should be specified.) !!!" << endl << endl;
        validCLA = false;
      }
      if (validCLA && (!isdigit((*(argv+2))[0]) && (*(argv+2))[0] != '-')){
        cout << "  !!! Invalid col local search length: " << *(argv+2) << " !!!" << endl << endl;
        validCLA = false;
      }
      if (validCLA){
        myCLA.rowLocalSearchLength = atoi(*(++argv));
        myCLA.colLocalSearchLength = atoi(*(++argv));
      }
      break;
    default:  
      cout << "  !!! Invalid update type: " << *argv << " !!!" << endl << endl;
      validCLA = false;
      break;
  }
  if (!validCLA){
    printUpdateType();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get information of taking reverse of rows from command-line...
char **getTakingReverse(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  if (*(argv+1) == NULL){
    cout << "  !!! Invalid parameter: (0 or 1 should be specified.) !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA && !isdigit((*(argv+1))[0])){
    cout << "  !!! Invalid parameter: " << *(argv+1) << " !!!" << endl << endl;
    validCLA = false;
  }
  if (validCLA){
    switch (atoi(*(++argv))){
      case ON:
        myCLA.takingReverse = true;
	break;
      case OFF:
        myCLA.takingReverse = false;
	break;
      default:
        cout << " !!! Invalid parameter: (0 or 1 should be specified.): " << myCLA.numRun << " !!!" << endl << endl;
        validCLA = false;
	break;
    }
  }
  if (!validCLA){
    printTakingReverse();
    myCLA.numInvalidCLA++;
  }
  return argv;
}


// get co-clustering parameter values from command-line...
void getCommandLine(int argc, char **argv, commandLineArgument &myCLA)
{
  bool validCLA = true;
  for (argv++; *argv != NULL; argv++){        
    if ((*argv)[0] == '-'){
      switch (toupper((*argv)[1])){
        case MY_ALGORITHM:		// -A  algorithmType
          argv = getAlgorithmType(argc, argv, myCLA);
	  break;
        case MY_NUM_COL_CLUSTER:	// -C  numColCluster
          argv = getColClusterNum(argc, argv, myCLA);
          break;
        case MY_DUMP_LEVEL:		// -D  dumpLevel
          argv = getDumpLevel(argc, argv, myCLA);
          break;
        case MY_SHOWING_EACH_CLUSTER:	// -E  dumpLevel
          argv = getShowingEachCluster(argc, argv, myCLA);
          break;
        case MY_HELP:
	  printHelp();
	  break;
	case MY_INPUT_FILE:		// -I  inputMatrixType  inputFormatType  inputFilename
          argv = getInputFileInformation(argc, argv, myCLA);
          break;
        case MY_CLASS_FILE:		// -K  classLabelSelection  classOffsetType  classLabelFilename
	  argv = getClassFileInformation(argc, argv, myCLA);
	  break;
        case MY_SMOOTHING:		// -M smoothingType
          argv = getSmoothingType(argc, argv, myCLA);
          break;
        case MY_NUM_RUN:		// -N  runNum
          argv = getRunNum(argc, argv, myCLA);
          break;
        case MY_OUTPUT_FILE:		// -O  outputLabelType  outputAccessMode  labelOffsetType  outputFilename
          argv = getOutputFileInformation(argc, argv, myCLA);
          break;
        case MY_NUM_ROW_CLUSTER:	// -R  numRowCluster
          argv = getRowClusterNum(argc, argv, myCLA);
          break;
        case MY_SEEDING:		// -S  seedingType  seedingLabelSelection [labelOffsetType seedingFilename]
          argv = getSeedingInformation(argc, argv, myCLA);
          break;
        case MY_THRESHOLD:		// -T thresholdType thresholdValue
          argv = getThresholdInformation(argc, argv, myCLA);
          break;
        case MY_UPDATE:			// -U  updateType  updateOrderSelection
          argv = getUpdateInformation(argc, argv, myCLA);
          break;
        case MY_COMPUTING_ONE_WAY:	// -W
          argv = getComputingOneWayObjective(argc, argv, myCLA);
           break;
        case MY_TAKING_REVERSE:		// -X
          argv = getTakingReverse(argc, argv, myCLA);
          break;
        default:
//          printHelp();
	  cout << "  !!! Invalid argument setting: " << *argv << " !!!" << endl << endl;
          validCLA = false;
          break;
      }
    } else {
      sprintf(myCLA.inputFilename, "%s",*argv);
      if (strcmp(myCLA.coclusterFilename, "") == 0)
        extractFilename(*argv, myCLA.coclusterFilename);
      strcpy(myCLA.dumpFilename, myCLA.coclusterFilename);
      strcpy(myCLA.objectiveFilename, myCLA.coclusterFilename);
      myCLA.havingArgument = true;
    }
  }
  if (!validCLA){
//    printHelp();
    myCLA.numInvalidCLA++;
  }
}


void extractFilename(char *path, char *name)
{
  int length = strlen(path);
  for(int i = length-1; i >= 0; i--)
    if ((path[i] == '/') || (path[i] == '\\')){
      i++;
      for (int j = i; j < length; j++)
        name[j-i] = path[j];
      break;
    } else if (i == 0){
      for (int j = i; j < length; j++)
        name[j-i]=path[j];
      break;
    }
}


void makeFilename(char *filename, char *suffix, commandLineArgument &myCLA)
{
  char buffer[DEFAULT_STRING_LENGTH];
  char rowInit = ' ';
  char colInit = ' ';
  switch (myCLA.rowInitializationMethod){
    case RANDOM_INIT:  		rowInit = 'r';	break;
    case RANDOM_PERTURB_INIT:	rowInit = 'p';	break;
    case FARTHEST_INIT:		rowInit = 'f';	break;
    case PERTURBATION_INIT:	rowInit = 'p';	break;
    case SEEDING_INIT:		rowInit = 's';  break;
    default:
      cout << "  !!! Invalid row initialization: " << myCLA.rowInitializationMethod << " !!!" << endl << endl;
      exit(EXIT_FAILURE);
      break;
  }
  switch (myCLA.colInitializationMethod){
    case RANDOM_INIT:  		colInit = 'r';	break;
    case RANDOM_PERTURB_INIT:	colInit = 'p';	break;
    case FARTHEST_INIT:		colInit = 'f';	break;
    case PERTURBATION_INIT:	colInit = 'p';	break;
    case SEEDING_INIT:		colInit = 's';  break;
    default:  
      cout << "  !!! Invalid column initialization: " << myCLA.colInitializationMethod << " !!!" << endl << endl;
      exit(EXIT_FAILURE);
      break;
  }
  sprintf(buffer, "%s_%c%c_%d_%d", suffix, rowInit, colInit, myCLA.numRowCluster, myCLA.numColCluster);
  strcat(filename, buffer); 
}


int *readMatrix(char *filename, denseStruct *mat, int &numEmptyCol, int formatType, int matrixType)
  //read in a dense matrix; but since either rows are to be clustered or columns are to be clustered,
  //we use 'matrixType' to identify that.
  //for dense matrix we assume there is NO empty vector.
{
  std::ifstream dimFile;
  std::ifstream inputFile;
  char tempFilename[FILENAME_LENGTH];
  char whole_line[DEFAULT_STRING_LENGTH];
  int *emptyColId;
  
  switch (formatType){
    case DIM_MATRIX_SEPARATE_FORMAT:
      sprintf(tempFilename, "%s%s", filename, "_dim");
      dimFile.open(tempFilename, ios::in);
      if (!dimFile.is_open()){
        cout << "  !!! Dimension file open error: " << tempFilename << " !!!" << endl;
        exit(EXIT_FAILURE);
      }
      dimFile >> mat->numRow >> mat->numCol;
      dimFile.getline(whole_line, DEFAULT_STRING_LENGTH, '\n');
      inputFile.open(filename, ios::in);
      if (!inputFile.is_open()){
        cout << "  !!! Input file open error: " << filename << " !!!" << endl;
        exit(EXIT_FAILURE);
      }
      break;
    case DIM_MATRIX_TOGETHER_FORMAT:
      inputFile.open(filename, ios::in);
      if (!inputFile.is_open()){
        cout << "  !!! Input file open error: " << filename << " !!!" << endl;
        exit(EXIT_FAILURE);
      }
      inputFile >> mat->numRow >> mat->numCol;
      inputFile.getline(whole_line, DEFAULT_STRING_LENGTH, '\n');
      break;
    default:
      cout << "  !!! Incorrect input format type: " << formatType << " !!!" << endl;
      exit(EXIT_FAILURE);
      break;
  }
                
  //cout<<mat->numRow<<" "<< mat->numCol<<endl;
  mat->numVal = mat->numRow * mat->numCol;
  mat->value = new double *[mat->numRow];
  for (int i = 0; i < mat->numRow; i++)
    mat->value[i]= new double[mat->numCol];
  memoryUsed += (mat->numRow * mat->numCol) * sizeof(double);
  
  cout << "  Reading matrix file ... " << filename << endl;
  switch (matrixType){
    case DENSE_MATRIX:
      numEmptyCol = 0;
      for (int i = 0; i < mat->numRow; i++)
        for (int j = 0; j < mat->numCol; j++)
          inputFile >> mat->value[i][j];
      break;
    case DENSE_MATRIX_TRANS:				// not used...
      numEmptyCol = 0;
      for (int i = 0; i < mat->numCol; i++)
        for (int j=0; j < mat->numRow; j++)
          inputFile >> mat->value[j][i];
      break;
    default:
      break;
  }
  dimFile.close();
  inputFile.close();
  emptyColId = new int[numEmptyCol+1];
  emptyColId[0] = mat->numCol;
  return  emptyColId;
}


int *readMatrix(char *fname, sparseStruct *mat, int &numEmptyCol, int formatType, char *scalingType)
// read in a sparse matrix from files into 'mat' and return #empty vectors and an array of empty vector IDs
{
  char filename[FILENAME_LENGTH];
  //clock_t start_clock, finish_clock;
  sprintf(filename, "%s%s", fname, "_dim");
  std::ifstream dimFile(filename);
  if (dimFile == 0)
    cout << "  !!! File open error: " << filename << " !!!" << endl << endl;
  sprintf(filename, "%s%s", fname, "_row_ccs");
  std::ifstream rowPtrFile(filename);
  if (rowPtrFile == 0)
    cout << "  !!! File open error: " << filename << " !!!" << endl << endl;
  sprintf(filename, "%s%s", fname, "_col_ccs");
  std::ifstream colIdxFile(filename);
  if (colIdxFile == 0)
    cout << "  !!! File open error: " << filename << " !!!" << endl << endl;
  sprintf(filename, "%s%s", fname, "_");
  if (formatType == 0)
    sprintf(filename, "%s%s", filename, TXX_SCALING);
  else
    sprintf(filename, "%s%s", filename, TFN_SCALING);  
  sprintf(filename, "%s%s", filename, "_nz");
  std::ifstream valFile(filename);
  if (valFile == 0)
      cout << "  !!! File open error: " << filename << " !!!" << endl;
  if (dimFile == 0 || rowPtrFile == 0 || colIdxFile == 0 || valFile == 0){
    cout << "  !!! Matrix file " << fname << "_* has missing file(s) !!!" << endl;
    exit(EXIT_FAILURE);
  }
  //data.width(MAX_DESC_STR_LENGTH);
  //data >> mat->descString;
  //data >> mat->numCol >> mat->numRow >> mat->numVal;
  //start_clock = clock();
  cout << "  Reading dimension file ... " << endl;
  dimFile >> mat->numRow >> mat->numCol >> mat->numVal;
  dimFile.close();
  mat->colPtr = new int[mat->numCol+1];
  mat->rowIdx = new int[mat->numVal];
  mat->value = new double[mat->numVal];
  //space used for storing the sparse matrix is as follows
  memoryUsed += (mat->numCol + 1 + mat->numVal) * sizeof(int) + mat->numVal * sizeof(double);
  //it is necessary to handle empty vectors separately
  int pre = -1;
  int *tempEmptyColId = new int[mat->numCol], *emptyColId;
  numEmptyCol = 0;
  cout << "  Reading column pointer file ... " << endl;
  for (int i = 0; i < mat->numCol+1; i++){
    colIdxFile >> (mat->colPtr)[i];
    if ((mat->colPtr)[i] == pre){
      tempEmptyColId[numEmptyCol] = i - 1;
      numEmptyCol++;
    }
    pre = (mat->colPtr)[i];
  }
  colIdxFile.close();
  emptyColId = new int[numEmptyCol+1];
  for(int i = 0; i < numEmptyCol; i++)
    emptyColId[i] = tempEmptyColId[i];
  emptyColId[numEmptyCol] = mat->numCol;
  delete [] tempEmptyColId;
  cout << "  Reading row index file ... " << endl;
  for (int i = 0; i < mat->numVal; i++)
    rowPtrFile >> (mat->rowIdx)[i];
  rowPtrFile.close();
  cout << "  Reading non-zero value file ... " << endl;
  for (int i = 0; i < mat->numVal; i++)
    valFile >> (mat->value)[i];
  valFile.close();
  cout << endl;
  //finish_clock = clock();
  //cout << "Reading file time: " << (finish_clock - start_clock) / 1e6 << " seconds." << endl;
  if (numEmptyCol == 0)
    cout << "  !!! No empty col found !!!" << endl;
  else {
    cout << "  !!! " << numEmptyCol << " empty col(s) found !!!" << endl;
    for(int i = 0; i < numEmptyCol; i++)
      cout << emptyColId[i] << " ";
    cout << endl;
  }
  return  emptyColId;
}


/*
void myReadMatrix(commandLineArgument myCLA, sparseStruct &sparseCCS, sparseStruct &sparseCRS, denseStruct &denseMat)
{
  switch (myCLA.inputMatrixType){
    case DENSE_MATRIX:
    case DENSE_MATRIX_TRANS:	// not used...
      myCLA.emptyColId = readMatrix(myCLA.inputFilename, &denseMat, myCLA.numEmptyCol, myCLA.inputFormatType, myCLA.inputMatrixType);
      assert(((myCLA.numRowCluster <= denseMat.numRow) && (myCLA.numColCluster <= denseMat.numCol)) && (myCLA.numRowCluster != denseMat.numRow || myCLA.numColCluster != denseMat.numCol));
      assert((myCLA.rowLocalSearchLength < denseMat.numRow) && (myCLA.colLocalSearchLength < denseMat.numCol));
      memoryUsed += denseMat.numRow * denseMat.numCol * sizeof(double);
      break;
    case SPARSE_MATRIX:
    default:
      myCLA.emptyColId = readMatrix(myCLA.inputFilename, &sparseCCS, myCLA.numEmptyCol, myCLA.inputFormatType, myCLA.scalingType);
      assert(((myCLA.numRowCluster <= sparseCCS.numRow) && (myCLA.numColCluster <= sparseCCS.numCol)) && (myCLA.numRowCluster != sparseCCS.numRow || myCLA.numColCluster != sparseCCS.numCol));
      assert((myCLA.rowLocalSearchLength < sparseCCS.numRow) && (myCLA.colLocalSearchLength < sparseCCS.numCol));
      sparseCRS.colPtr = new int[sparseCCS.numRow+1];
      sparseCRS.rowIdx = new int[sparseCCS.numVal];
      sparseCRS.value = new double[sparseCCS.numVal];
      sparseCRS.numCol = sparseCCS.numRow;
      sparseCRS.numRow = sparseCCS.numCol;
      sparseCRS.numVal = sparseCCS.numVal;
      convertSparse2Sparse(sparseCCS.numCol, sparseCCS.numRow, sparseCCS.numVal, sparseCCS.colPtr, sparseCCS.rowIdx, sparseCCS.value, sparseCRS.colPtr, sparseCRS.rowIdx, sparseCRS.value); 
 //     checkConvertSparse2Sparse(sparseCCS.numCol, sparseCCS.numRow, sparseCCS.numVal, sparseCCS.colPtr, sparseCCS.rowIdx, sparseCCS.value, sparseCRS.colPtr, sparseCRS.rowIdx, sparseCRS.value); 
      memoryUsed += (sparseCCS.numCol + sparseCCS.numRow + 2 * sparseCCS.numVal) * sizeof(int) + 2 * sparseCCS.numVal * sizeof(double);
      break;
  }
}
*/


/*
void myPreprocessMatrix(commandLineArgument myCLA, sparseStruct &sparseCCS, sparseStruct &sparseCRS, denseStruct &denseMat, Matrix *myCCS, Matrix *myCRS)
{
  switch (myCLA.inputMatrixType){
    case DENSE_MATRIX:
      myCCS = new DenseMatrix(denseMat.numRow, denseMat.numCol, denseMat.value); 
      myCRS = myCCS;
      if (myCLA.algorithmType == INFORMATION_THEORETIC_CC)
        myCCS->preprocess();
      break;
    case SPARSE_MATRIX:
      myCCS = new SparseMatrix(sparseCCS.numRow, sparseCCS.numCol, sparseCCS.numVal, sparseCCS.value, sparseCCS.rowIdx, sparseCCS.colPtr);
      if (myCLA.algorithmType == INFORMATION_THEORETIC_CC)
        myCCS->preprocess();
      myCRS = new SparseMatrix(sparseCRS.numRow, sparseCRS.numCol, sparseCRS.numVal, sparseCRS.value, sparseCRS.rowIdx, sparseCRS.colPtr); 
      if (myCLA.algorithmType == INFORMATION_THEORETIC_CC)
        myCRS->preprocess();
      break;
    default:
      break;
  }  
}
*/


/*
void myConstructCoclustering(commandLineArgument myCLA, Matrix *myCCS, Matrix *myCRS, Coclustering *myCC)
{
  switch (myCLA.algorithmType){
    case INFORMATION_THEORETIC_CC:
      myCC = new Itcc(myCCS, myCRS, myCLA);
      myCC->classPrefix = ITCC_CLASS;
      break;
    case MINIMUM_SUM_SQUARE_RESIDUE_I_CC:
      myCC = new MssrIcc(myCCS, myCRS, myCLA);
      myCC->classPrefix = MSSRICC_CLASS;
      break;
    case MINIMUM_SUM_SQUARE_RESIDUE_II_CC:
      myCC = new MssrIIcc(myCCS, myCRS, myCLA);
      myCC->classPrefix = MSSRIICC_CLASS;
      break;
    default:
      myCC = new Itcc(myCCS, myCRS, myCLA);
      myCC->classPrefix = ITCC_CLASS;
      break;
  }
}
*/


// Read column class/cluster labels from the file whose first row contains num, 
// and each of the other line contains a label (i.e., one label in a row).
int readLabel(char *filename, int numData, int *label, int classOffsetType)
{
  char whole_line[DEFAULT_STRING_LENGTH];
  int maxLabel = 0, num, numClass = 0;
//  cout << "  Reading class label file ... " << filename << endl;
  std::ifstream labelFile(filename);

  if (!labelFile.is_open()){
    if (strcmp(filename, "") != 0){
      cout << "  !!! File open error: " << filename << " !!!" << endl;
      exit(EXIT_FAILURE);
    }
  }
  labelFile >> num;
  labelFile.getline(whole_line, DEFAULT_STRING_LENGTH, '\n');	// skip all the other columns in the first line
  if (num != numData){
    cout << "  !!! Incorrect number of class labels in: " << filename << " !!!" << endl;
    exit(EXIT_FAILURE);
  }
//  if (!isSilent)	// not used...
    cout << "  Reading class label file ... " << filename << endl;
  for (int i = 0; i < num; i++){
    labelFile >> label[i];
    labelFile.getline(whole_line, DEFAULT_STRING_LENGTH, '\n');
    if (label[i] > maxLabel)
      maxLabel = label[i];
  }
  labelFile.close();
  if (classOffsetType == START_FROM_0) 
    numClass = maxLabel + 1;
  else {
    numClass = maxLabel;
    for (int i = 0; i < num; i++)
      label[i]--;
  }
  return numClass;
}


// Read row or column class/cluster labels from the file, 
// where each line contains either row or column labels.
// So, if the file consists of many lines, # of lines == # of label sets.
void readLabel(char *filename, int numLabel, int *label, int &numClass, int classOffsetType)
{
//  cout << "  Reading class label file ... " << filename << endl;
  std::ifstream labelFile(filename);
  if (!labelFile.is_open()){
    if (strcmp(filename, "") != 0)
      cout << "  File open error: " << filename << endl;
    exit(EXIT_FAILURE);
  }
  readLabel(labelFile, numLabel, label, numClass, classOffsetType);

  labelFile.close();
}


// Read row or column class/cluster labels from the file stream, 
// where each line contains either row or column labels, 
// So, it the file consists of many lines, # of lines == # of label sets.
//(i.e., it may contains several sets of labels.)
void readLabel(istream &labelFile, int numLabel, int *label, int &numClass, int classOffsetType)
{
//  cout << "  Reading class label file ... " << filename << endl;
//  if (!isSilent)		// not used...
    cout << "  Reading class label file ... " << endl;  
  int maxLabel = 0;
  for (int i = 0; i < numLabel; i++){
    labelFile >> label[i];
    if (label[i] > maxLabel)
      maxLabel = label[i];
  }
  if (classOffsetType == START_FROM_0) 
    numClass = maxLabel + 1;
  else {
    numClass = maxLabel;
    for (int i = 0; i < numLabel; i++)
      label[i]--;
  }
}


// Read row and column class/cluster labels from the file consisting of two lines, 
// first line for row labels and second line for col labels.
void readLabel(char *filename, int numRow, int numCol, int *rowLabel, int *colLabel, int &numRowClass, int &numColClass, int classOffsetType)
{
//  cout << "  Reading class label file ... " << filename << endl;
  std::ifstream labelFile(filename);
  if (!labelFile.is_open()){
    if (strcmp(filename, "") != 0)
      cout << "  File open error: " << filename << endl;
    exit(EXIT_FAILURE);
  }
  readLabel(labelFile, numRow, numCol, rowLabel, colLabel, numRowClass, numColClass, classOffsetType);
// Otherwise, the following two lines are equivalent to the line above.
//  readLabel(labelFile, numRow, rowLabel, numRowClass, classOffsetType);
//  readLabel(labelFile, numCol, colLabel, numColClass, classOffsetType);
  labelFile.close();
}


// Read row and column class/cluster labels from the file stream, 
// where the first line for row labels and second line for col labels, 
// and so on. (i.e., it may contains several sets of labels.)
void readLabel(istream &labelFile, int numRow, int numCol, int *rowLabel, int *colLabel, int &numRowClass, int &numColClass, int classOffsetType)
{
//  cout << "  Reading class label file ... " << filename << endl;
//  if (!isSilent)		// not used...
    cout << "  Reading class label file ... " << endl;  
  int maxLabel = 0;
  for (int i = 0; i < numRow; i++){
    labelFile >> rowLabel[i];
    if (rowLabel[i] > maxLabel)
      maxLabel = rowLabel[i];
  }
  if (classOffsetType == START_FROM_0) 
    numRowClass = maxLabel + 1;
  else {
    numRowClass = maxLabel;
    for (int i = 0; i < numRow; i++)
      rowLabel[i]--;
  }
  maxLabel = 0;
  for (int i = 0; i < numCol; i++){
    labelFile >> colLabel[i];
    if (colLabel[i] > maxLabel)
      maxLabel = colLabel[i];
  }
  if (classOffsetType == START_FROM_0) 
    numColClass = maxLabel + 1;  
  else {
    numColClass = maxLabel;
    for (int i = 0; i < numCol; i++)
      colLabel[i]--;
  }
}



/*
 * Description:  
 * Get rank by sorting an array ra[1..n] into ascending numerical order using the Heapsort algorithm. 
 *   arrin[] is the input array containing the values
 *   indx[] is the output containing the rank of each element of arrin[]
 *   n is the length of arrin[]
 *   The input quantities N and ARRIN are not changed. 
 * Based on "Numerical Recipes in C" p. 247
 */
void getRank( int arrin[], int indx[], int *n )
{
  int ir;
  int l;
  /* initialize the index aray with consecutive integers.
   */
  for (l = 0; l < (*n); l++) indx[l] = l;
  if ((*n) < 2) return;
  /* The index L will be decremented from its initial value down to 1
   * during the "hiring" (heap creation) phase. Once it reaches 1, 
   * the index IR will be decremented from its initial value down to 1
   * during the "retirement-and-promotion" (heap selection) phase.
   */
  l = (*n) / 2;
  ir = (*n) - 1;
  while (1) {
    int i, j;
    int indxt;
    int q;
    if (l > 0) {                      /* Still in hiring phase */
      indxt = indx[--l];
      q = arrin[indxt];
    } else {                          /* In retirement-and-promotion phase */
      indxt = indx[ir];              /* Clear a space at end of array */
      q = arrin[indxt];
      indx[ir--] = indx[0];          /* Retire the top of the heap into it */
      if (ir == 0) {                 /* Done with the last promotion */
        indx[0] = indxt;            /* The least competent worker of all */
        return;
      }
    }
    /* Whether we are in the hiring phase or promotion phase, 
    * we here set up to sift down element q to its proper level.
    */
    i = l;
    j = l + l + 1;
    while (j <= ir) {
      /* Compare to the better underling
      */
      if (j < ir && arrin[indx[j]] < arrin[indx[j+1]]) j++;
      if (q < arrin[indx[j]]) {      /* Demote q */
        indx[i] = indx[j];
        i = j;
        j = j + j + 1;
      } else {                       /* This is q's level */
        j = ir + 1;                 /* Set j to terminate the sift-down */
      }
    }
    indx[i] = indxt;                  /* Put q into its slot */
  }
}


void convertSparse2Sparse(int numPtr, int numIdx, int numVal, int *fromPtr, int *fromIdx, double *fromVal, int *toPtr, int *toIdx, double *toVal)
{ 
  int *tempToIdx = new int[numVal];
  int *tempRank = new int[numVal];
  int *tempCount = new int[numIdx];

  getRank(fromIdx, tempRank, &numVal);  
  
  int tempIdx = 0;
  for (int i = 0; i < numPtr; i++)
    for (int j = 0; j < (fromPtr[i+1] - fromPtr[i]); j++)
      tempToIdx[tempIdx++] = i;
  for (int i = 0; i < numIdx; i++)
    tempCount[i] = 0;
  for (int i = 0; i < numVal; i++){
    toIdx[i] = tempToIdx[tempRank[i]]; 
    toVal[i] = fromVal[tempRank[i]];
    tempCount[fromIdx[i]]++;
  }
  toPtr[0] = 0;
  for (int i = 0; i < numIdx; i++)
    toPtr[i+1] = toPtr[i] + tempCount[i];

  delete [] tempToIdx;
  delete [] tempRank;
  delete [] tempCount;
}


void checkConvertSparse2Sparse(int numPtr, int numIdx, int numVal, int *fromPtr, int *fromIdx, double *fromVal, int *toPtr, int *toIdx, double *toVal)
{ 
  double *tempFromValue = new double[numIdx];
  double *tempToValue = new double[numIdx];
  for (int i = 0; i < numPtr; i++){
    for (int j = 0; j < numIdx; j++){
      tempFromValue[j] = 0;
      tempToValue[j] = 0;
    }
    for (int j = fromPtr[i]; j < fromPtr[i+1]; j++)
      tempFromValue[fromIdx[j]] = fromVal[j];
    for (int m = 0; m < numIdx; m++)
      for (int n = toPtr[m]; n < toPtr[m+1]; n++)
        if (toIdx[n] == i)
	  tempToValue[m] = toVal[n];
    for (int j = 0; j < numIdx; j++)
      if (tempFromValue[j] != tempToValue[j]){
        cout << "  !! Incorrect conversion from sparse to sparse !!!" << endl;
	exit(EXIT_FAILURE);
      }
  }
  cout << "  !!! Correct conversion from sparse to sparse !!!" << endl;  
  delete [] tempFromValue;
  delete [] tempToValue;
}


double computeMutualInfo(double **mat, int numRow, int numCol, double *marginX, double *marginY)
  //matrix is joint probability
{
  double MI = 0;

  for(int i = 0; i < numRow; i++)
    for (int j = 0; j < numCol; j++){
      double tempValue = mat[i][j];
      if (tempValue > 0)
	MI += tempValue * log(tempValue / (marginX[i] * marginY[j]));
    }
  return MI / log(2.0);
}


double computeMutualInfo(double **mat, int numRowCluster, int numColCluster)
{
  double *rowSum = new double[numRowCluster], MI = 0.0;
  for (int i = 0; i < numRowCluster; i++){
    rowSum[i] = 0.0;
    for (int j = 0; j < numColCluster; j++)
      rowSum[i] += mat[i][j];
  }
  for (int i = 0; i < numRowCluster; i++){
    double temp = 0;
    for (int j = 0; j < numColCluster; j++){
      double tempValue = mat[i][j];
      if (tempValue > 0.0)
        temp += tempValue * log(tempValue / rowSum[i]);
    }
    MI += temp;
  }
  delete [] rowSum;
  return MI / log(2.0);
}


void getStatistics(int *value, int num, double &average, double &variance, double &stdDev)
{
  average = 0; 
  variance = 0;
  for (int i = 0; i < num; i++)
    average += value[i];
  average /= num;
  if (num == 1)
    variance = 0;
  else {
    for (int i = 0; i < num; i++){
      double tempValue = value[i] - average;
      variance += tempValue * tempValue;
    }
    variance /= (num - 1);
  }
  stdDev = sqrt(variance);
}


void getStatistics(double *value, int num, double &average, double &variance, double &stdDev)
{
  average = 0; 
  variance = 0;
  for (int i = 0; i < num; i++)
    average += value[i];
  average /= num;
  if (num == 1)
    variance = 0;
  else {
    for (int i = 0; i < num; i++){
      double tempValue = value[i] - average;
      variance += tempValue * tempValue;
    }
    variance /= (num - 1);
  }
  stdDev = sqrt(variance);
}


void getMinMax(int *value, int num, int &min, int &max)
{
  min = MY_INT_MAX;
  for (int i = 0; i < num; i++)
    if (value[i] < min)
      min = value[i];
  max = min;
  for (int i = 0; i < num; i++)
    if (value[i] > max)
      max = value[i];
}


void getMinMax(double *value, int num, double &min, double &max)
{
  min = MY_DBL_MAX;
  for (int i = 0; i < num; i++)
    if (value[i] < min)
      min = value[i];
  max = min;
  for (int i = 0; i < num; i++)
    if (value[i] > max)
      max = value[i];
}


void outputStatistics(commandLineArgument &myCLA, char *classPrefix, int *value, char *msg, ostream &dumpFile, ostream &statisticsFile)
{
  int minValue = 0, maxValue = 0;
  double average = 0, variance = 0, stdDev = 0;
  getStatistics(value, myCLA.numRun, average, variance, stdDev);
  cout << classPrefix << msg << average << " with Variance(" << variance << ") and Std(" << stdDev << ")" << endl;
  if (myCLA.dumpLevel == MAXIMUM_DUMP_LEVEL)
    dumpFile << classPrefix << msg << average << " with Variance(" << variance << ") and Std(" << stdDev << ")" << endl;
  if (myCLA.statisticsAccessMode != NO_OPEN_MODE)
    statisticsFile << classPrefix << msg << average << " with Variance(" << variance << ") and Std(" << stdDev << ")" << endl;
  if (myCLA.numRun > 1){
    getMinMax(value, myCLA.numRun, minValue, maxValue);
    cout << setfill(' ') << setw(strlen(classPrefix)+52) << " ranging from (" << minValue << ") to (" << maxValue << ")" << endl;
    if (myCLA.dumpLevel == MAXIMUM_DUMP_LEVEL)
      dumpFile << setfill(' ') << setw(strlen(classPrefix)+52) << " ranging from (" << minValue << ") to (" << maxValue << ")" << endl;
    if (myCLA.statisticsAccessMode != NO_OPEN_MODE)
      statisticsFile << setfill(' ') << setw(strlen(classPrefix)+52) << " ranging from (" << minValue << ") to (" << maxValue << ")" << endl;
  }
}


void outputStatistics(commandLineArgument &myCLA, char *classPrefix, double *value, char *msg, ostream &dumpFile, ostream &statisticsFile)
{
  double minValue = 0, maxValue = 0;
  double average = 0, variance = 0, stdDev = 0;
  getStatistics(value, myCLA.numRun, average, variance, stdDev);
  cout << classPrefix << msg << average << " with Variance(" << variance << ") and Std(" << stdDev << ")" << endl;
  if (myCLA.dumpLevel >= MAXIMUM_DUMP_LEVEL)
    dumpFile << classPrefix << msg << average << " with Variance(" << variance << ") and Std(" << stdDev << ")" << endl;
  if (myCLA.statisticsAccessMode != NO_OPEN_MODE)
    statisticsFile << classPrefix << msg << average << " with Variance(" << variance << ") and Std(" << stdDev << ")" << endl;
  if (myCLA.numRun > 1){
    getMinMax(value, myCLA.numRun, minValue, maxValue);
    cout << setfill(' ') << setw(strlen(classPrefix)+52) << " ranging from (" << minValue << ") to (" << maxValue << ")" << endl;
    if (myCLA.dumpLevel >= MAXIMUM_DUMP_LEVEL)
      dumpFile << setfill(' ') << setw(strlen(classPrefix)+52) << " ranging from (" << minValue << ") to (" << maxValue << ")" << endl;
    if (myCLA.statisticsAccessMode != NO_OPEN_MODE)
      statisticsFile << setfill(' ') << setw(strlen(classPrefix)+52) << " ranging from (" << minValue << ") to (" << maxValue << ")" << endl;
  }
}



void outputRunTime(TimerUtil &runTime, commandLineArgument &myCLA, char *classPrefix, ostream &dumpFile, ostream &statisticsFile)
{
  cout << classPrefix;
  runTime.setStopTime(cout, "CPU Usage: ", myCLA.numRun);
  cout << endl << classPrefix << "Memory used: " << memoryUsed << " byte(s)" << endl << endl;
  if (myCLA.dumpLevel == MAXIMUM_DUMP_LEVEL){
    dumpFile << classPrefix;
    runTime.setStopTime(dumpFile, "CPU Usage: ", myCLA.numRun);
    dumpFile << endl << classPrefix << "Memory used: " << memoryUsed << " byte(s)" << endl << endl;
  }
  if (myCLA.statisticsAccessMode != NO_OPEN_MODE){
    statisticsFile << classPrefix;
    runTime.setStopTime(statisticsFile, "CPU Usage: ", myCLA.numRun);
    statisticsFile << endl << classPrefix << "Memory used: " << memoryUsed << " byte(s)" << endl << endl;
  }
}


void outputConstructor(commandLineArgument &myCLA, char *classPrefix, ostream &dumpFile, ostream &statisticsFile)
{
  char tempString[strlen(classPrefix)];
  strcpy(tempString, classPrefix);
  strcpy(tempString + strlen(classPrefix)-2, "()");
  cout << endl << endl << classPrefix << tempString << endl;
  if (myCLA.dumpLevel == MAXIMUM_DUMP_LEVEL)
    dumpFile << endl << endl << classPrefix << tempString << endl;
  if (myCLA.statisticsAccessMode != NO_OPEN_MODE)
    statisticsFile << endl << endl << classPrefix << tempString << endl;
}


void outputDeconstructor(commandLineArgument &myCLA, char *classPrefix, ostream &dumpFile, ostream &statisticsFile)
{
  char tempString[strlen(classPrefix)];
  strcpy(tempString, classPrefix);
  strcpy(tempString + strlen(classPrefix)-2, "()");
  cout << classPrefix << "~" << tempString << endl << endl << endl;
  if (myCLA.dumpLevel == MAXIMUM_DUMP_LEVEL)
    dumpFile << classPrefix << "~" << tempString << endl << endl << endl;
  if (myCLA.statisticsAccessMode != NO_OPEN_MODE)
    statisticsFile << classPrefix << "~" << tempString << endl << endl << endl;
}


void writeCoefMatrix(Matrix *matrix, int n, char *outFilename, char *suffix, int matrixType, char *prefix)
  //write out the similarity matrix between each pair of vectors in a file containing an n by n matrix
{
  char browserFilePostfix[FILENAME_LENGTH], inFilename[FILENAME_LENGTH];  
  char line[1024];
  sprintf(inFilename, prefix); 
  sprintf(browserFilePostfix, "%s_correlation_matrix", outFilename);
  if (matrixType == SPARSE_MATRIX){
    strcat(outFilename, "_");
    strcat(outFilename, suffix);
  }
  //strcat(outFilename, browserFilePostfix);
  strcat(inFilename, "_docs");
  std::ofstream outFile(browserFilePostfix);
//  if (!outFile.is_open()){
//    cout << "File open error: " << browserFilePostfix;
//    exit(EXIT_FAILURE);
//  }
  std::ifstream inFile(inFilename);
//  if (!inFile.is_open()){
//    cout << "File open error: " << inFilename;
//    exit(EXIT_FAILURE);
//  }
  outFile << n << endl;
  char ch;
  int j;
  for (int i = 0; i < n; i++){
    if (inFile.is_open()){
      inFile >> j;
      inFile >> ch;
      inFile >> line; //path of the file; reuse string
      outFile << line << " ";
    }
    for(int j = 0; j <= i; j++)
      outFile << matrix->get_dot_i_j(i,j) << " ";
    outFile << endl;      
  }
  outFile.close();
  if (inFile.is_open())
    inFile.close();
}


void writeCluster(int *cluster, int n, int finalClusterNum, char *outFilename, char *suffix, int matrixType, char *prefix)
{
  char line[DEFAULT_STRING_LENGTH * 4];
  char browserFilePostfix[FILENAME_LENGTH], inFilename[FILENAME_LENGTH];  
  sprintf(inFilename, prefix); 
  sprintf(browserFilePostfix, "_doctoclus.%d", finalClusterNum);
  if (matrixType == SPARSE_MATRIX){
    strcat(outFilename, "_");
    strcat(outFilename, suffix);
  }
  strcat(outFilename, browserFilePostfix);
  strcat(inFilename, "_docs");
  std::ofstream outFile(outFilename);
//  if (!outFile.is_open()){
//    cout << "File open error: " << outFilename << endl;
//    exit(EXIT_FAILURE);
//  }
  std::ifstream inFile(inFilename);
//  if (!inFile.is_open()){
//    cout << "File open error: " << inFilename << endl;
//    exit(EXIT_FAILURE);
//  }
  char ch;
  int j;
  outFile << n << endl;
  for (int i = 0; i < n; i++){
    outFile << cluster[i];
    if (inFile.is_open()){
      inFile >> j;
      inFile >> ch;
      inFile >> line; //path of the file; reuse string
      outFile << "\t" << line << endl;
    } else
      outFile << endl;
  }
  outFile.close();
  if (inFile.is_open())
    inFile.close();
}


void outputCommandLineArgument(int argc, char **argv, ostream &os)
{
  if ((!os) == 0){
    for ( ; *argv != NULL; argv++)       
      os << *argv << " ";
    os << endl << endl;
  }
}

