/*
  Cocluster.cc
    the main file for co-clustering programs 
    
    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#include <fstream>
#include <cmath>
#include <iomanip>
#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "MatrixVector.h"
#include "TimerUtility.h"
#include "Tools.h"
#include "Itcc.h"
#include "MssrIcc.h"
#include "MssrIIcc.h"

using namespace std;

long memoryUsed = 0l;


int main(int argc, char **argv)
{
  commandLineArgument myCLA;  
//------------------------------------------------------------------------------
  setCommandLine(myCLA);		// set default command line arguments  
  getCommandLine(argc, argv, myCLA);	// get command line arguments
//------------------------------------------------------------------------------
  if (!myCLA.havingArgument)
    printHelp();
  if (myCLA.numInvalidCLA != 0)
    exit(EXIT_FAILURE);
  if (myCLA.algorithmType == INFORMATION_THEORETIC_CC && myCLA.takingReverse){
    cout << "  Taking reverse is invalid for ITCC..." << endl << endl;
    exit(EXIT_FAILURE);
  }
  if (myCLA.takingReverse && myCLA.coclusterOffsetType == START_FROM_0){
    cout << "  Taking reverse and output labels starting from 0 are not valid..." << endl << endl;
    exit(EXIT_FAILURE);
  }
//  strcat(myCLA.dumpFilename, DUMP_FILENAME_SUFFIX);
//  strcat(myCLA.objectiveFilename, OBJECTIVE_FILENAME_SUFFIX);
//  strcat(myCLA.coclusterFilename, COCLUSTER_FILENAME_SUFFIX);
//  makeFilename(myCLA.objectiveFilename, OBJECTIVE_FILENAME_SUFFIX, myCLA);
//  makeFilename(myCLA.coclusterFilename, COCLUSTER_FILENAME_SUFFIX, myCLA);

//------------------------------------------------------------------------------
  sparseStruct sparseCCS;
  sparseStruct sparseCRS;
  denseStruct denseMat;
  TimerUtil runTime;

  switch (myCLA.inputMatrixType){
    case DENSE_MATRIX:
    case DENSE_MATRIX_TRANS:	// not used...
      myCLA.emptyColId = readMatrix(myCLA.inputFilename, &denseMat, myCLA.numEmptyCol, myCLA.inputFormatType, myCLA.inputMatrixType);
      assert(((myCLA.numRowCluster <= denseMat.numRow) && (myCLA.numColCluster <= denseMat.numCol)) && (myCLA.numRowCluster != denseMat.numRow || myCLA.numColCluster != denseMat.numCol));
      assert((myCLA.rowLocalSearchLength < denseMat.numRow) && (myCLA.colLocalSearchLength < denseMat.numCol));
      memoryUsed += denseMat.numRow * denseMat.numCol * sizeof(double);
      break;
    case SPARSE_MATRIX:
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
    default:
      break;
  }
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
  Matrix *myCCS = NULL;
  Matrix *myCRS = NULL;
  Coclustering *myCC = NULL;
  
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
//------------------------------------------------------------------------------  

//------------------------------------------------------------------------------  
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
//------------------------------------------------------------------------------  

//------------------------------------------------------------------------------  
  if (myCLA.externalValidityType == ROW_EXTERNAL_VALIDITY){
    myCLA.rowClassLabel = new int[myCCS->getNumRow()];
    myCLA.numRowClass = readLabel(myCLA.rowClassFilename, myCCS->getNumRow(), myCLA.rowClassLabel, myCLA.classOffsetType);
  }
  if (myCLA.externalValidityType == COL_EXTERNAL_VALIDITY){ 
    myCLA.colClassLabel = new int[myCCS->getNumCol()];
    myCLA.numColClass = readLabel(myCLA.colClassFilename, myCCS->getNumCol(), myCLA.colClassLabel, myCLA.classOffsetType);
  }
  if (myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
    myCLA.rowClassLabel = new int[myCCS->getNumRow()];
    myCLA.colClassLabel = new int[myCCS->getNumRow()];
    readLabel(myCLA.bothClassFilename, myCCS->getNumRow(), myCCS->getNumCol(), myCLA.rowClassLabel, myCLA.colClassLabel, myCLA.numRowClass, myCLA.numColClass, myCLA.classOffsetType);   
  }
//------------------------------------------------------------------------------
  
//------------------------------------------------------------------------------    
  outputConstructor(myCLA, myCC->classPrefix, myCC->dumpFile, myCC->statisticsFile);
  if (myCLA.numRun >= 1){
    int *numPingPong = new int[myCLA.numRun];
    int *numReversedRow = NULL;
    double *initialObjectVal4RC = NULL, *initialObjectVal4CC = NULL;
    double *finalObjectVal4RC = NULL, *finalObjectVal4CC = NULL;
    double *numEmptyRC = new double[myCLA.numRun];
    double *numEmptyCC = new double[myCLA.numRun];
    double *numSingletonRC = new double[myCLA.numRun];
    double *numSingletonCC = new double[myCLA.numRun];
    double *initialObjectVal = new double[myCLA.numRun];
    double *finalObjectVal = new double[myCLA.numRun];
    double *initialRowPrecision = NULL, *initialColPrecision = NULL;		// micro-averaged precision
    double *finalRowPrecision = NULL, *finalColPrecision = NULL;		// micro-averaged precision
    double *initialRowAccuracy = NULL, *initialColAccuracy = NULL;
    double *finalRowAccuracy = NULL, *finalColAccuracy = NULL;
    if (myCLA.externalValidityType == ROW_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
      initialRowPrecision = new double[myCLA.numRun];
      finalRowPrecision = new double[myCLA.numRun];
      initialRowAccuracy = new double[myCLA.numRun];
      finalRowAccuracy = new double[myCLA.numRun];
    }
    if (myCLA.externalValidityType == COL_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
      initialColPrecision = new double[myCLA.numRun];
      finalColPrecision = new double[myCLA.numRun];
      initialColAccuracy = new double[myCLA.numRun];
      finalColAccuracy = new double[myCLA.numRun];
    }
    if (myCLA.takingReverse)
      numReversedRow = new int[myCLA.numRun];
    if (myCLA.computingOneWayObjective){
      initialObjectVal4RC = new double[myCLA.numRun];
      initialObjectVal4CC = new double[myCLA.numRun];
      finalObjectVal4RC = new double[myCLA.numRun];
      finalObjectVal4CC = new double[myCLA.numRun];
    }
    for (int i = 0; i < myCLA.numRun; i++){
      cout << endl << myCC->classPrefix << "beginRandomRun(): numRun(" << i+1 << ")" << endl << endl;;
      if (myCLA.dumpLevel == MAXIMUM_DUMP_LEVEL)
        myCC->dumpFile << endl << myCC->classPrefix << "beginRandomRun(): numRun(" << i+1 << ")" << endl << endl;
      if (myCLA.statisticsAccessMode != NO_OPEN_MODE)
        myCC->statisticsFile << endl << myCC->classPrefix << "beginRandomRun(): numRun(" << i+1 << ")" << endl << endl;
      myCC->setRowSmoothingFactor(myCLA.rowSmoothingFactor);
      myCC->setColSmoothingFactor(myCLA.colSmoothingFactor);
      myCC->doInitialization();
      initialObjectVal[i] = myCC->getObjValue();
      if (myCLA.computingOneWayObjective){
        initialObjectVal4RC[i] = myCC->getObjValue4RowCluster();
	initialObjectVal4CC[i] = myCC->getObjValue4ColCluster();
      }
      if (myCLA.externalValidityType == ROW_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){ 
        myCC->validateRowCluster(myCLA.numRowClass, myCLA.rowClassLabel);
        initialRowPrecision[i] = myCC->getRowPrecision();
        initialRowAccuracy[i] = myCC->getRowAccuracy();
      }
      if (myCLA.externalValidityType == COL_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
        myCC->validateColCluster(myCLA.numColClass, myCLA.colClassLabel);
        initialColPrecision[i] = myCC->getColPrecision();
        initialColAccuracy[i] = myCC->getColAccuracy();
      }
      myCC->doPingPong();
      numEmptyRC[i] = myCC->getEmptyRC();
      numEmptyCC[i] = myCC->getEmptyCC();
      numSingletonRC[i] = myCC->getSingletonRC();
      numSingletonCC[i] = myCC->getSingletonCC();
      finalObjectVal[i] = myCC->getObjValue();
      if (myCLA.computingOneWayObjective){
        finalObjectVal4RC[i] = myCC->getObjValue4RowCluster();
	finalObjectVal4CC[i] = myCC->getObjValue4ColCluster();
      }
      numPingPong[i] = myCC->getNumIteration();
      if (myCLA.externalValidityType == ROW_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){ 
        myCC->validateRowCluster(myCLA.numRowClass, myCLA.rowClassLabel);
        finalRowPrecision[i] = myCC->getRowPrecision();
        finalRowAccuracy[i] = myCC->getRowAccuracy();
      }
      if (myCLA.externalValidityType == COL_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
        myCC->validateColCluster(myCLA.numColClass, myCLA.colClassLabel);
        finalColPrecision[i] = myCC->getColPrecision();
        finalColAccuracy[i] = myCC->getColAccuracy();
      }
      if (myCLA.takingReverse)
        numReversedRow[i] = myCC->getNumReversedRow();
      cout << myCC->classPrefix << "endRandomRun(): numRun(" << i+1 << ")" << endl << endl;
      if (myCLA.dumpLevel == MAXIMUM_DUMP_LEVEL)
        myCC->dumpFile << myCC->classPrefix << "endRandomRun(): numRun(" << i+1 << ")" << endl << endl;
      if (myCLA.statisticsAccessMode != NO_OPEN_MODE)
        myCC->statisticsFile << myCC->classPrefix << "endRandomRun(): numRun(" << i+1 << ")" << endl  << endl;
    }
//    cout << endl << "  ### " << myCLA.numRun << " random run(s) done ###" << endl << endl;
//    if (myCLA.dumpLevel == MAXIMUM_DUMP_LEVEL)
//      myCC->dumpFile << endl << "  ### " << myCLA.numRun << " random run(s) done ###" << endl << endl;
//    if (myCLA.statisticsAccessMode != NO_OPEN_MODE)
//      myCC->statisticsFile << endl << "  ### " << myCLA.numRun << " random run(s) done ###" << endl << endl;

    outputStatistics(myCLA, myCC->classPrefix, numPingPong, "Average # of PingPong Iteration     = ", myCC->dumpFile, myCC->statisticsFile);
    outputStatistics(myCLA, myCC->classPrefix, numEmptyRC,  "Average # of Empty Row Clusters     = ", myCC->dumpFile, myCC->statisticsFile);
    outputStatistics(myCLA, myCC->classPrefix, numEmptyCC,  "Average # of Empty Col Clusters     = ", myCC->dumpFile, myCC->statisticsFile);
    outputStatistics(myCLA, myCC->classPrefix, numSingletonRC, "Average # of Singleton Row Clusters = ", myCC->dumpFile, myCC->statisticsFile);
    outputStatistics(myCLA, myCC->classPrefix, numSingletonCC, "Average # of Singleton Col Clusters = ", myCC->dumpFile, myCC->statisticsFile);
    if (myCLA.takingReverse){
      outputStatistics(myCLA, myCC->classPrefix, numReversedRow,"Average # of Reversed Row(s)        = ", myCC->dumpFile, myCC->statisticsFile);
      delete [] numReversedRow;
    } 
    outputStatistics(myCLA, myCC->classPrefix, initialObjectVal, "Average Initial Objective Value     = ", myCC->dumpFile, myCC->statisticsFile);
    outputStatistics(myCLA, myCC->classPrefix, finalObjectVal,   "Average Final Objective Value       = ", myCC->dumpFile, myCC->statisticsFile);
    if (myCLA.computingOneWayObjective){
      outputStatistics(myCLA, myCC->classPrefix, initialObjectVal4RC, "Average Initial Obj. of Row Cluster = ", myCC->dumpFile, myCC->statisticsFile);
      outputStatistics(myCLA, myCC->classPrefix, finalObjectVal4RC,   "Average Final Obj. of Row Cluster   = ", myCC->dumpFile, myCC->statisticsFile);
      outputStatistics(myCLA, myCC->classPrefix, initialObjectVal4CC, "Average Initial Obj. of Col Cluster = ", myCC->dumpFile, myCC->statisticsFile);
      outputStatistics(myCLA, myCC->classPrefix, finalObjectVal4CC,   "Average Final Obj. of Col Cluster   = ", myCC->dumpFile, myCC->statisticsFile);
      delete [] initialObjectVal4RC;
      delete [] initialObjectVal4CC;
      delete [] finalObjectVal4RC;
      delete [] finalObjectVal4CC;
    }      
    delete [] numPingPong;
    delete [] numEmptyRC;
    delete [] numEmptyCC;
    delete [] numSingletonRC;
    delete [] numSingletonCC;
    delete [] initialObjectVal;
    delete [] finalObjectVal;
    if (myCLA.externalValidityType == ROW_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
      outputStatistics(myCLA, myCC->classPrefix, initialRowPrecision, "Average Initial Row Precision Value = ", myCC->dumpFile, myCC->statisticsFile);
      delete [] initialRowPrecision;
    }
    if (myCLA.externalValidityType == ROW_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
      outputStatistics(myCLA, myCC->classPrefix, finalRowPrecision, "Average Final Row Precision Value   = ", myCC->dumpFile, myCC->statisticsFile);
      delete [] finalRowPrecision;
    }
    if (myCLA.externalValidityType == COL_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
      outputStatistics(myCLA, myCC->classPrefix, initialColPrecision, "Average Initial Col Precision Value = ", myCC->dumpFile, myCC->statisticsFile);
      delete [] initialColPrecision;
    }
    if (myCLA.externalValidityType == COL_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
      outputStatistics(myCLA, myCC->classPrefix, finalColPrecision, "Average Final Col Precision Value   = ", myCC->dumpFile, myCC->statisticsFile);
      delete [] finalColPrecision;
    }
    if (myCLA.externalValidityType == ROW_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
      outputStatistics(myCLA, myCC->classPrefix, initialRowAccuracy,  "Average Initial Row Accuracy Value  = ", myCC->dumpFile, myCC->statisticsFile);
      delete [] initialRowAccuracy;
    }
    if (myCLA.externalValidityType == ROW_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
      outputStatistics(myCLA, myCC->classPrefix, finalRowAccuracy,  "Average Final Row Accuracy Value    = ", myCC->dumpFile, myCC->statisticsFile);
      delete [] finalRowAccuracy;
    }
    if (myCLA.externalValidityType == COL_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
      outputStatistics(myCLA, myCC->classPrefix, initialColAccuracy,  "Average Initial Col Accuracy Value  = ", myCC->dumpFile, myCC->statisticsFile);
      delete [] initialColAccuracy;
    }
    if (myCLA.externalValidityType == COL_EXTERNAL_VALIDITY || myCLA.externalValidityType == BOTH_EXTERNAL_VALIDITY){
      outputStatistics(myCLA, myCC->classPrefix, finalColAccuracy,  "Average Final Col Accuracy Value    = ", myCC->dumpFile, myCC->statisticsFile);
      delete [] finalColAccuracy;
    }
//------------------------------------------------------------------------------
    if (myCLA.coclusterAccessMode != NO_OPEN_MODE)
      if (myCLA.numRun == 1 || (myCLA.numRun > 1 && myCLA.coclusterAccessMode == APPEND_MODE))
        myCC->writeCocluster();
//------------------------------------------------------------------------------    
  }
  outputRunTime(runTime, myCLA, myCC->classPrefix, myCC->dumpFile, myCC->statisticsFile);
  outputDeconstructor(myCLA, myCC->classPrefix, myCC->dumpFile, myCC->statisticsFile);
  outputCommandLineArgument(argc, argv, cout);
  outputCommandLineArgument(argc, argv, myCC->dumpFile);
  outputCommandLineArgument(argc, argv, myCC->statisticsFile);
}
