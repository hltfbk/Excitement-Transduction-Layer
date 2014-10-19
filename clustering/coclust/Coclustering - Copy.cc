/*
  Coclustering.cc
    Implementation of the super class of all co-cluster algorithms

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#include <iostream>
#include <fstream>
#include <algorithm>
#include <assert.h>
#include <time.h>
#include <stdlib.h>

#include "Coclustering.h"
#include "MatrixVector.h"

Coclustering::Coclustering(Matrix *inputCCS, Matrix *inputCRS, commandLineArgument &myCLA)
{
  assert(inputCCS != NULL && inputCRS != NULL);
  assert(myCLA.numRowCluster > 0 && myCLA.numRowCluster <= inputCCS->getNumRow());
  assert(myCLA.numColCluster > 0 && myCLA.numColCluster <= inputCCS->getNumCol());
 
  isShowingEachCluster = myCLA.showingEachCluster;
  isTakingReverse = myCLA.takingReverse;
  isHavingVariation = false;
  isComputingOneWayObjective = myCLA.computingOneWayObjective;

  dumpLevel = myCLA.dumpLevel;
  dumpAccessMode = myCLA.dumpAccessMode;
  batchUpdateType = myCLA.batchUpdateType;
  rowBatchUpdateThreshold = myCLA.rowBatchUpdateThreshold;
  colBatchUpdateThreshold = myCLA.colBatchUpdateThreshold;
  localSearchType = myCLA.localSearchType;
  rowLocalSearchThreshold = myCLA.rowLocalSearchThreshold;
  colLocalSearchThreshold = myCLA.colLocalSearchThreshold;
  rowLocalSearchLength = myCLA.rowLocalSearchLength;
  colLocalSearchLength = myCLA.colLocalSearchLength;
  if (rowLocalSearchLength == RESUME_LOCAL_SEARCH)
    isAvoidingEmptyRowCluster = true;
  else
    isAvoidingEmptyRowCluster = false;
  if (colLocalSearchLength == RESUME_LOCAL_SEARCH)
    isAvoidingEmptyColCluster = true;
  else
    isAvoidingEmptyColCluster = false;
  hasReadRowSeedingFile = false;
  hasReadColSeedingFile = false;
  
  numRowCluster = myCLA.numRowCluster;
  numColCluster = myCLA.numColCluster;
  smoothingType = myCLA.smoothingType;
  rowAnnealingFactor = myCLA.rowAnnealingFactor;		// not used...
  colAnnealingFactor = myCLA.colAnnealingFactor;		// not used...
  rowSmoothingFactor = myCLA.rowSmoothingFactor;		// not used...
  colSmoothingFactor = myCLA.colSmoothingFactor;		// not used...
  rowSmoothingFactor = myCLA.smoothingFactor;
  colSmoothingFactor = myCLA.smoothingFactor;
  perturbationMagnitude = myCLA.perturbationMagnitude;
  rowSeedingOffsetType = myCLA.rowSeedingOffsetType;
  colSeedingOffsetType = myCLA.colSeedingOffsetType;
  numRowSeedingSet = myCLA.numRowSeedingSet;
  numColSeedingSet = myCLA.numColSeedingSet;
  rowSeedingAccessMode = myCLA.rowSeedingAccessMode;
  colSeedingAccessMode = myCLA.colSeedingAccessMode;
  numRowClass = myCLA.numRowClass;
  numColClass = myCLA.numColClass;
  rowClassLabel = myCLA.rowClassLabel;
  colClassLabel = myCLA.colClassLabel;
  rowInitializationMethod = myCLA.rowInitializationMethod;
  colInitializationMethod = myCLA.colInitializationMethod;
//  rowSeedingFilename = myCLA.rowSeedingFilename;
//  colSeedingFilename = myCLA.colSeedingFilename;
//  coclusterFilename = myCLA.coclusterFilename;
  strcpy(rowSeedingFilename, myCLA.rowSeedingFilename);
  strcpy(colSeedingFilename, myCLA.colSeedingFilename);
  strcpy(coclusterFilename, myCLA.coclusterFilename);
  strcpy(objectiveFilename, myCLA.objectiveFilename);
  strcpy(dumpFilename, myCLA.dumpFilename);
  strcpy(statisticsFilename, myCLA.statisticsFilename);
  coclusterOffsetType = myCLA.coclusterOffsetType;
  coclusterLabelType = myCLA.coclusterLabelType;
  coclusterAccessMode = myCLA.coclusterAccessMode;
  objectiveAccessMode = myCLA.objectiveAccessMode;
  statisticsAccessMode = myCLA.statisticsAccessMode;
  
  numRow = inputCCS->getNumRow();
  numCol = inputCCS->getNumCol();

  myCCS = inputCCS;
  myCRS = inputCRS;
  myCRS->setSmoothingFactor(myCLA.smoothingType, myCLA.rowSmoothingFactor);
  myCCS->setSmoothingFactor(myCLA.smoothingType, myCLA.colSmoothingFactor);
  myCRS->setAnnealingFactor(myCLA.rowAnnealingFactor);
  myCCS->setAnnealingFactor(myCLA.colAnnealingFactor);

  isSilent = false;		// not used...
  isReversed = NULL;
  rowV = colV = 0;
  numIteration = 0;
  numEmptyRowCluster = 0;
  numEmptyColCluster = 0;
  numSingletonRowCluster = 0;
  numSingletonColCluster = 0;
  numReversedRow = 0;

  isRowMarked = new bool[numRow];
  isColMarked = new bool[numCol];
  memoryUsed = (numRow + numCol) * sizeof(bool);

  Acompressed = new double *[numRowCluster];
  for (int i = 0; i < numRowCluster; i++)
    Acompressed[i] = new double[numColCluster];
  memoryUsed = numRowCluster * numColCluster * sizeof(double);

  numRowPermutation = myCLA.numRowPermutation;
  numColPermutation = myCLA.numColPermutation;
  doRowCLVecInitialization(); 
  doColCLVecInitialization();
    
  rowCL = new int[numRow];
  colCL = new int[numCol];
  rowCS = new int[numRowCluster];
  colCS = new int[numColCluster];
  memoryUsed += (numRow + numCol + numRowCluster + numColCluster) * sizeof(int);

  if (isComputingOneWayObjective){
    twoNormOfEachRow = new double[numRow];
    twoNormOfEachCol = new double[numCol];
    memoryUsed += (numRow + numCol) * sizeof(double);
  }
  if (rowSeedingAccessMode != NO_OPEN_MODE && numRowSeedingSet > 1){
    if (rowSeedingAccessMode == BOTH_INPUT_MODE || rowSeedingAccessMode == ONE_INPUT_MODE)
      rowSeedingFile.open(rowSeedingFilename, ios::in);
    else
      rowSeedingFile.open(rowSeedingFilename, ios::app);
    if (!rowSeedingFile.is_open()){
      cout << "  !!! RowSeeding file open error: " << rowSeedingFilename << " !!!" << endl;
      exit(EXIT_FAILURE);
    }
  }
  if (colSeedingAccessMode != NO_OPEN_MODE && numColSeedingSet > 1){
    if (colSeedingAccessMode == ONE_INPUT_MODE )
      colSeedingFile.open(colSeedingFilename, ios::in);
    else
      colSeedingFile.open(colSeedingFilename, ios::app);
    if (!colSeedingFile.is_open()){
      cout << "  !!! ColSeeding file open error: " << colSeedingFilename << " !!!" << endl;
      exit(EXIT_FAILURE);
    }
  }
  if (coclusterAccessMode != NO_OPEN_MODE){
    if (coclusterAccessMode == OUTPUT_MODE)
      coclusterFile.open(coclusterFilename, ios::out);
    else
      coclusterFile.open(coclusterFilename, ios::app);
    if (!coclusterFile.is_open()){
      cout << "  !!! Cocluster file open error: " << coclusterFilename << " !!!" << endl;
      exit(EXIT_FAILURE);
    }
  }
  if (dumpAccessMode != NO_OPEN_MODE){
    if (dumpAccessMode == OUTPUT_MODE)
      dumpFile.open(dumpFilename, ios::out);
    else
      dumpFile.open(dumpFilename, ios::app);
    if (!dumpFile.is_open()){
      cout << "  !!! Dump file open error: " << dumpFilename << " !!!" << endl;
      exit(EXIT_FAILURE);
    }
  }
  if (objectiveAccessMode != NO_OPEN_MODE){
    if (objectiveAccessMode == OUTPUT_MODE)
      objectiveFile.open(objectiveFilename, ios::out);
    else
      objectiveFile.open(objectiveFilename, ios::app);
    if (!objectiveFile.is_open()){
      cout << "  !!! Objective file open error: " << objectiveFilename << " !!!" << endl;
      exit(EXIT_FAILURE);
    }
  }
  if (statisticsAccessMode != NO_OPEN_MODE){
    if (statisticsAccessMode == OUTPUT_MODE)
      statisticsFile.open(statisticsFilename, ios::out);
    else
      statisticsFile.open(statisticsFilename, ios::app);
    if (!statisticsFile.is_open()){
      cout << "  !!! Statistics file open error: " << statisticsFilename << " !!!" << endl;
      exit(EXIT_FAILURE);
    }
  }
  isEmptyRowClusterReported = isEmptyColClusterReported = false ;
  randNumGenerator.Set((unsigned)time(NULL));
}


Coclustering::~Coclustering()
{
  delete [] isRowMarked;
  delete [] isColMarked;
  delete [] rowCL;
  delete [] colCL;
  delete [] rowCS;
  delete [] colCS;
  for (int i = 0; i < numRowCluster; i++)
    delete [] Acompressed[i];
  delete [] Acompressed;
  if (isComputingOneWayObjective){
    delete [] twoNormOfEachRow;
    delete [] twoNormOfEachCol;
  }
  if ((rowSeedingAccessMode == BOTH_INPUT_MODE || rowSeedingAccessMode == ONE_INPUT_MODE) && numRowSeedingSet > 1)
    rowSeedingFile.close(); 
  if ((colSeedingAccessMode == ONE_INPUT_MODE) && numColSeedingSet > 1)
    colSeedingFile.close(); 
  if (coclusterAccessMode != NO_OPEN_MODE)
    coclusterFile.close(); 
  if (dumpAccessMode != NO_OPEN_MODE)
    dumpFile.close(); 
  if (objectiveAccessMode != NO_OPEN_MODE)
    objectiveFile.close();
  if (statisticsAccessMode != NO_OPEN_MODE)
    statisticsFile.close(); 
}


int Coclustering::getEmptyRC()
{
  return numEmptyRowCluster;
}


int Coclustering::getEmptyCC()
{
  return numEmptyColCluster;
}


int Coclustering::getSingletonRC()
{
  return numSingletonRowCluster;
}


int Coclustering::getSingletonCC()
{
  return numSingletonColCluster;
}


double Coclustering::getObjValue()
{
  return objValue;
}


double Coclustering::getObjValue4RowCluster()
{
  return objValue4RowCluster;
}


double Coclustering:: getObjValue4ColCluster()
{
  return objValue4ColCluster;
}


int Coclustering::getNumIteration()
{
  return numIteration;
}


int Coclustering::getNumReversedRow()
{
  return numReversedRow;
}


void Coclustering::chooseInitializationMethod()
{
  int tempRowClass, tempColClass;
  if (((rowInitializationMethod == SEEDING_INIT) && (colInitializationMethod == SEEDING_INIT))
       && (strcmp(rowSeedingFilename, colSeedingFilename) == 0)){
    if (!hasReadRowSeedingFile && (numRowSeedingSet == 1) && (numColSeedingSet == 1)){
      readLabel(rowSeedingFilename, numRow, numCol, rowCL, colCL, tempRowClass, tempColClass, rowSeedingOffsetType);   
      hasReadRowSeedingFile = true;
    }
    if ((numRowSeedingSet > 1) && (numColSeedingSet > 1)){
      readLabel(rowSeedingFile, numRow, numCol, rowCL, colCL, tempRowClass, tempColClass, rowSeedingOffsetType);
    }
  } else {
    if (numRowCluster == 1 || numRowCluster == numRow){
      doRowRandomInitializationModified();
    } else {
      switch (rowInitializationMethod){
        case RANDOM_INIT:
//          doRowRandomInitialization();
          doRowRandomInitializationModified();
          break;
        case RANDOM_PERTURB_INIT:
          doRowRandomPerturbInitialization();
          break;
        case FARTHEST_INIT:
          doRowFarthestInitialization();
          break;
        case SEEDING_INIT:
//          if (strcmp(rowSeedingFilename, EMPTY_STRING) != 0 && strcmp(colSeedingFilename, EMPTY_STRING) == 0)
          if (strcmp(rowSeedingFilename, EMPTY_STRING) != 0){
	    if (!hasReadRowSeedingFile && (numRowSeedingSet == 1)){
	      tempRowClass = readLabel(rowSeedingFilename, numRow, rowCL, rowSeedingOffsetType);        
              hasReadRowSeedingFile = true;
            }
	    if (numRowSeedingSet > 1){
	      readLabel(rowSeedingFile, numRow, rowCL, tempRowClass, rowSeedingOffsetType); 
            }
	  }
	  break;
	case PERMUTATION_INIT:
	  doRowPermutationInitialization();
	  break;     
        default:
          doRowRandomInitializationModified();
          break;
      }
    }
    if ((numColCluster == 1) || (numColCluster == numCol)){
      doColRandomInitializationModified();
    } else {
      switch (colInitializationMethod){
        case RANDOM_INIT:
//          doColRandomInitialization();
          doColRandomInitializationModified();
          break;
        case RANDOM_PERTURB_INIT:
          doColRandomPerturbInitialization();
          break;
        case FARTHEST_INIT:
          doColFarthestInitialization();
          break;
        case SEEDING_INIT:
//          if (strcmp(colSeedingFilename, EMPTY_STRING) != 0 && strcmp(rowSeedingFilename, EMPTY_STRING) == 0)
          if (strcmp(colSeedingFilename, EMPTY_STRING) != 0){
	    if (!hasReadColSeedingFile && (numColSeedingSet == 1)){
	      tempColClass = readLabel(colSeedingFilename, numCol, colCL, colSeedingOffsetType);
              hasReadColSeedingFile = true;
            }
	    if (numColSeedingSet > 1){
	      readLabel(colSeedingFile, numCol, colCL, tempColClass, colSeedingOffsetType);
	    }
          }
	  break;
	case PERMUTATION_INIT:
	  doColPermutationInitialization();
	  break;
        default:
          doColRandomInitializationModified();
          break;
      }
    }
  }
}


void Coclustering::doRowRandomInitialization()
{
  bool *mark = new bool[numRowCluster], isEnough = true;
  for (int i = 0; i < numRowCluster; i++)
    mark[i] = false;
  for (int i = 0; i < numRow; i++){
    rowCL[i] = randNumGenerator.GetUniformInt(numRowCluster);
    mark[rowCL[i]] = true;
  }
  for (int i = 0; i < numRowCluster; i++)
    if (mark[i] == false){
      isEnough = false;
      break;
    }
  if (isEnough == false)
    for (int j = 0; j < numRowCluster; j++)
      rowCL[j] = j;
  delete [] mark;
}


void Coclustering::doColRandomInitialization()
{
  bool *mark = new bool[numColCluster], isEnough = true;
  for (int i = 0; i < numColCluster; i++)
    mark[i] = false;
  for (int i = 0; i < numCol; i++){
    colCL[i] = randNumGenerator.GetUniformInt(numColCluster);
    mark[colCL[i]] = true;
  }
  for (int i = 0; i < numColCluster; i++)
    if (mark[i] == false){
      isEnough = false;
      break;
    }
  if (isEnough == false)
    for (int j = 0; j < numColCluster; j++)
      colCL[j] = j;
  delete [] mark;
}


void Coclustering::doRowRandomInitializationModified()
{
  if (numRowCluster == 1)
    for (int i = 0; i < numRow; i++)
      rowCL[i] = 0;
  else if (numRowCluster == numRow)
    doRowRandomInitializationDirect();
  else {
    for (int i = 0; i < numRow; i++)
      rowCL[i] = i % numRowCluster;
    for (int i = 0; i < numRow; i++){
      int j = randNumGenerator.GetUniformInt(numRow);
      if (i != j){
        int temp = rowCL[i];
        rowCL[i] = rowCL[j];
        rowCL[j] = temp;
      }
    }
  }
}


void Coclustering::doColRandomInitializationModified()
{
  if (numColCluster == 1)
    for (int i = 0; i < numCol; i++)
      colCL[i] = 0;
  else if (numColCluster == numCol)
    doColRandomInitializationDirect();
  else {
    for (int i = 0; i < numCol; i++)
      colCL[i] = i % numColCluster;
    for (int i = 0; i < numCol; i++){
      int j = randNumGenerator.GetUniformInt(numCol);
      if (i != j){
        int temp = colCL[i];
        colCL[i] = colCL[j];
        colCL[j] = temp;
      }
    }
  }
}


void Coclustering::doRowRandomInitializationDirect()
{
  for (int i = 0; i < numRow; i++)
    rowCL[i] = i;
}


void Coclustering::doColRandomInitializationDirect()
{
  for (int i = 0; i < numCol; i++)
    colCL[i] = i;
}


void Coclustering::doSeedingInitializationI(char * seedingFilename)
{
  int rn, cn, ID;
  std::ifstream gpfile(seedingFilename);
  if (gpfile.is_open()){
    cout << "  Reading cluster labels: " << seedingFilename << endl;
    for (int i = 0; i < numRowCluster; i++)
      for (int j = 0; j < numColCluster; j++){
        gpfile >> rn >> cn;
        for (int k = 0; k < rn; k++){
          gpfile >> ID;
          rowCL[ID] = i;
        }
        for (int k = 0; k < cn; k++){
          gpfile >> ID;
          colCL[ID] = j;
        }
      }
  } else {
    cout << "  !!! Seeding file open error: " << seedingFilename << " !!!" << endl << endl;
    exit(EXIT_FAILURE);
  }
/*
  { 
    cout << "Seeding file open error: " << seedingFilename << endl << ". So doing random initialization" << endl;
    randomInitial(numRowCluster, numRow, rowCL);
    randomInitial(numColCluster, numCol, colCL);
  }
*/
}


void Coclustering::doSeedingInitializationII(char * seedingFilename)
{
  std::ifstream gpfile(seedingFilename);
  if (gpfile.is_open()){
    cout << "  Reading cluster labels: " << seedingFilename << endl;
    for (int i = 0; i < numRow; i++)
      gpfile >> rowCL[i];
    for (int j = 0; j < numCol; j++)
      gpfile >> colCL[j];
  } else {
    cout << "  !!! Seeding file open error: " << seedingFilename << " !!!" << endl << endl;
    exit(EXIT_FAILURE);
  }
/*
  { 
    cout << "Seeding file open error: " << seedingFilename << endl << "So doing random initialization" << endl;
    randomInitial(numRowCluster, numRow, rowCL);
    randomInitial(numColCluster, numCol, colCL);
  }
*/
}

void Coclustering::doRowCLVecInitialization()
{
  if (numRowCluster == 1)
    for (int i = 0; i < numRow; i++)
      rowCLVec.push_back(0);
  else if (numRowCluster == numRow)
    for (int i = 0; i < numRow; i++)
      rowCLVec.push_back(i);
  else
    for (int i = 0; i < numRow; i++)
      rowCLVec.push_back(i % numRowCluster);
}


void Coclustering::doColCLVecInitialization()
{
  if (numColCluster == 1)
    for (int i = 0; i < numCol; i++)
      colCLVec.push_back(0);
  else if (numColCluster == numCol)
    for (int i = 0; i < numCol; i++)
      colCLVec.push_back(i);
  else
    for (int i = 0; i < numCol; i++)
      colCLVec.push_back(i % numColCluster);
}


void Coclustering::doRowPermutationInitialization()
{
  bool has_next_permutation = true;
  if ((numRowCluster != 1) && (numRowCluster != numRow))
    for (int i = 0; i < numRowPermutation && has_next_permutation; i++)
      has_next_permutation = next_permutation(rowCLVec.begin(), rowCLVec.end());
  assert(has_next_permutation);
  for (int i = 0; i < numRow; i++)
    rowCL[i] = rowCLVec[i];
}


void Coclustering::doColPermutationInitialization()
{
  bool  has_next_permutation = true;
  if ((numColCluster != 1) && (numColCluster != numCol))
    for (int i = 0; i < numColPermutation && has_next_permutation; i++)
      has_next_permutation = next_permutation(colCLVec.begin(), colCLVec.end());
  assert(has_next_permutation);
  for (int i = 0; i < numCol; i++)
    colCL[i] = colCLVec[i];
}


void Coclustering::checkHavingReversedRow()
{
  bool havingReversed = false;
  for (int r = 0; r < numRow; r++)
    if (rowCL[r] < 0){
      havingReversed = true;
      break;
    }
  if (havingReversed){
    if (!isTakingReverse){
      isTakingReverse = true;
      isReversed = new bool[numRow];
      memoryUsed += numRow * sizeof(bool);
    }  
    for (int r = 0; r < numRow; r++)
      if (rowCL[r] < 0){
        isReversed[r] = true;
        rowCL[r] *= -1;
      } else
        isReversed[r] = false;
  }
}    


void Coclustering::updateVariable(double &minDistance, int &minCL, double tempDistance, int tempCL)
{
  if (tempDistance < minDistance){
    minDistance = tempDistance;
    minCL = tempCL;
  }
}


void Coclustering::updateVariable(double &minDistance, int &minCL, bool &tempIsReversed, double tempDistance, int tempCL, bool trueOrFalse)
{
  if (tempDistance < minDistance){
    minDistance = tempDistance;
    minCL = tempCL;
    tempIsReversed = trueOrFalse;
  }
}


void Coclustering::computeNumReversedRow()
{
  numReversedRow = 0;
  for (int r = 0; r < numRow; r++)
    if (isReversed[r])
      numReversedRow++;
}


void Coclustering::adjustClusterLabel(int value)
{
  for (int r = 0; r < numRow; r++)
    rowCL[r] += value;
  for (int c = 0; c < numCol; c++)
    colCL[c] += value;
}  


void Coclustering::computeRowClusterSize()
{
  for (int i = 0; i < numRowCluster; i++)
    rowCS[i] = 0;
  for (int i = 0; i < numRow; i++)
    rowCS[rowCL[i]]++;
  numEmptyRowCluster = 0;
  numSingletonRowCluster = 0;
  for (int i = 0; i < numRowCluster; i++){
    if (rowCS[i] == 0)
      numEmptyRowCluster++;			// count # of empty row cluster(s)
    else if (rowCS[i] == 1)
      numSingletonRowCluster++;			// count # of singleton row cluster(s)
  }
  if (numEmptyRowCluster > 0){
    switch (dumpLevel){
      case MINIMUM_DUMP_LEVEL:
      case BATCH_UPDATE_DUMP_LEVEL:
        break;
      case LOCAL_SEARCH_DUMP_LEVEL:
        cout << "  ### " << numEmptyRowCluster << " empty row cluster(s) ###" << endl;
        break;
      case MAXIMUM_DUMP_LEVEL:
        dumpFile << "  ### " << numEmptyRowCluster << " empty row cluster(s) ###" << endl;
        break;
    }
    if (statisticsAccessMode != NO_OPEN_MODE)
        statisticsFile << "  ### " << numEmptyRowCluster << " empty row cluster(s) ###" << endl;    
    isEmptyRowClusterReported = true;
  } else if (isEmptyRowClusterReported){
    switch (dumpLevel){
      case MINIMUM_DUMP_LEVEL:
      case BATCH_UPDATE_DUMP_LEVEL:
        break;
      case LOCAL_SEARCH_DUMP_LEVEL:
        cout << "  !!! Fixing empty row cluster(s) !!!" << endl;
        break;
      case MAXIMUM_DUMP_LEVEL:
        dumpFile << "  !!! Fixing empty row cluster(s) !!!" << endl;
        break;
    }
    if (statisticsAccessMode != NO_OPEN_MODE)
      statisticsFile << "  !!! Fixing empty row cluster(s) !!!" << endl;
    isEmptyRowClusterReported = false;
  }
  if (numSingletonRowCluster > 0){
    switch (dumpLevel){
      case MINIMUM_DUMP_LEVEL:
      case BATCH_UPDATE_DUMP_LEVEL:
        break;
      case LOCAL_SEARCH_DUMP_LEVEL:
        cout << "  ### " << numSingletonRowCluster << " singleton row cluster(s) ###" << endl;
        break;
      case MAXIMUM_DUMP_LEVEL:
        dumpFile << "  ### " << numSingletonRowCluster << " singleton row cluster(s) ###" << endl;
        break;
    }
    if (statisticsAccessMode != NO_OPEN_MODE)
      statisticsFile << "  ### " << numSingletonRowCluster << " singleton row cluster(s) ###" << endl;
  }
}


void Coclustering::computeColClusterSize()
{
  for (int i = 0; i < numColCluster; i++)
    colCS[i] = 0;
  for (int i = 0; i < numCol; i++)
    colCS[colCL[i]]++;
  numEmptyColCluster = 0;
  numSingletonColCluster = 0;
  for (int i = 0; i < numColCluster; i++){
    if (colCS[i] == 0)
      numEmptyColCluster++;			// count # of empty column cluster(s)
    else if (colCS[i] == 1)
      numSingletonColCluster++;			// count # of singleton row cluster(s)
  }
  if (numEmptyColCluster > 0){
    switch (dumpLevel){
      case MINIMUM_DUMP_LEVEL:
      case BATCH_UPDATE_DUMP_LEVEL:
        break;
      case LOCAL_SEARCH_DUMP_LEVEL:
        cout << "  ### " << numEmptyColCluster << " empty col cluster(s) ###" << endl;
        break;
      case MAXIMUM_DUMP_LEVEL:
        dumpFile << "  ### " << numEmptyColCluster << " empty col cluster(s) ###" << endl;
        break;
    }
    if (statisticsAccessMode != NO_OPEN_MODE)
      statisticsFile << endl << "  ### " << numEmptyColCluster << " empty col cluster(s) ###" << endl;
    isEmptyColClusterReported = true;
  } else if (isEmptyColClusterReported){
    switch (dumpLevel){
      case MINIMUM_DUMP_LEVEL:
      case BATCH_UPDATE_DUMP_LEVEL:
        break;
      case LOCAL_SEARCH_DUMP_LEVEL:
        cout << "  !!! Fixing empty col cluster(s) !!!" << endl;
        break;
      case MAXIMUM_DUMP_LEVEL:
        dumpFile << "  !!! Fixing empty col cluster(s) !!!" << endl;
        break;
    }
    if (statisticsAccessMode != NO_OPEN_MODE)
      statisticsFile << "  !!! Fixing empty col cluster(s) !!!" << endl;
    isEmptyColClusterReported = false;
  }
  if (numSingletonColCluster > 0){
    switch (dumpLevel){
      case MINIMUM_DUMP_LEVEL:
      case BATCH_UPDATE_DUMP_LEVEL:
        break;
      case LOCAL_SEARCH_DUMP_LEVEL:
        cout << "  ### " << numSingletonColCluster << " singleton col cluster(s) ###" << endl;
        break;
      case MAXIMUM_DUMP_LEVEL:
        dumpFile << "  ### " << numSingletonColCluster << " singleton col cluster(s) ###" << endl;
        break;
    }
    if (statisticsAccessMode != NO_OPEN_MODE)
      statisticsFile << "  ### " << numSingletonColCluster << " singleton col cluster(s) ###" << endl;
  }
}


void Coclustering::removeEmptyCluster()
{
  int *rowClusterNewLabel = new int[numRowCluster], *colClusterNewLabel = new int[numColCluster];
  int tmp_label = 0;
  if (numEmptyRowCluster > 0){
    for (int i = 0; i < numRowCluster; i++){
      if (rowCS[i] > 0){
        rowClusterNewLabel[i]= tmp_label;
        tmp_label++;
      }
    }
    for (int i = 0; i < numRow; i++)
      rowCL[i] = rowClusterNewLabel[rowCL[i]];
  }
  tmp_label = 0;
  if (numEmptyColCluster > 0){
    for (int i = 0; i < numColCluster; i++){
      if (colCS[i] > 0){
        colClusterNewLabel[i] = tmp_label;
        tmp_label++;
      }
    }
    for (int i = 0; i < numCol; i++)
      colCL[i] = colClusterNewLabel[colCL[i]];
  }
  delete [] rowClusterNewLabel;
  delete [] colClusterNewLabel;
}


void Coclustering::writeCocluster()
{
  removeEmptyCluster();
  computeRowClusterSize();
  computeColClusterSize();
  if (coclusterLabelType == BLOCK_FORMAT){
    int *rowChecker = new int[numRow];
    int *colChecker = new int[numCol];
    int *rowBin = new int[numRowCluster];
    int *colBin = new int[numColCluster];
    rowBin[0] = 0;
    for (int i = 1; i < numRowCluster; i++)
      rowBin[i] = rowBin[i-1] + rowCS[i-1];
    colBin[0] = 0;
    for (int j = 1; j < numColCluster; j++)
      colBin[j] = colBin[j-1] + colCS[j-1];
    for (int i = 0; i < numRow; i++){
      rowChecker[rowBin[rowCL[i]]] = i;
      rowBin[rowCL[i]]++;
    }
    for (int i = 0; i < numCol; i++){
      colChecker[colBin[colCL[i]]] = i;
      colBin[colCL[i]]++;
    }
    int rowIndex = 0, colIndex = 0;
    for (int i = 0; i < numRowCluster; i++){
      colIndex = 0;
      if (rowCS[i] > 0){
	for (int j = 0; j < numColCluster; j++){
	  if (colCS[j] > 0){
	    coclusterFile << rowCS[i] << " " << colCS[j] << endl;
	    for (int k = rowIndex; k < rowIndex+rowCS[i]; k++){
	      if (isTakingReverse){
		if (isReversed[rowChecker[k]])
		  coclusterFile << "-";
        	coclusterFile << (rowChecker[k]+1) << " ";
              } else if (coclusterOffsetType == START_FROM_0){
        	coclusterFile << rowChecker[k] << " ";
	      } else if (coclusterOffsetType == START_FROM_1){
        	coclusterFile << (rowChecker[k]+1) << " ";
              }
            }
            coclusterFile << endl;
	    for (int k = colIndex; k < colIndex+colCS[j]; k++){
	      if (coclusterOffsetType == START_FROM_0)
        	coclusterFile << colChecker[k] << " ";
              else if (coclusterOffsetType == START_FROM_1)
        	coclusterFile << (colChecker[k]+1) << " ";
	    }
	    coclusterFile << endl;
	  }
	  colIndex += colCS[j];
	}
      }
      rowIndex += rowCS[i];
    }
    delete [] rowChecker;
    delete [] colChecker;
    delete [] rowBin;
    delete [] colBin;
  } else {
//	removeEmptyCluster();
    for (int i = 0; i < numRow; i++){
      if (isTakingReverse){
	if (isReversed[i])
	  coclusterFile << "-";
        coclusterFile << (rowCL[i]+1) << " ";
      } else if (coclusterOffsetType == START_FROM_0){
        coclusterFile << rowCL[i] << " ";
      } else 
	coclusterFile << (rowCL[i]+1) << " ";
    }
    coclusterFile << endl;
    for (int j = 0; j < numCol; j++){
      if (coclusterOffsetType == START_FROM_0)
	coclusterFile << colCL[j] << " ";
      else
        coclusterFile << (colCL[j]+1) << " ";
    }
    coclusterFile << endl;
  }
}


void Coclustering::setSilent(bool s)	// not used...
{
  isSilent = s;
}


void Coclustering::setRowSmoothingFactor(double p)
{
  rowSmoothingFactor = p;
}


void Coclustering::setColSmoothingFactor(double p)
{
  colSmoothingFactor = p;
}


void Coclustering::computeAcompressed()
{
  myCCS->condenseMatrix(rowCL, colCL, numRowCluster, numColCluster, Acompressed);
}


void Coclustering::computeAcompressed(bool *isReversed)
{
  myCCS->condenseMatrix(rowCL, colCL, numRowCluster, numColCluster, Acompressed, isReversed);
}


void Coclustering::validateRowCluster(int numRowClass, int *rowClassLabel)
{
  ExternalValidity ev(numRowClass, numRowCluster, numRow, rowClassLabel, rowCL);
/*
//  if (dumpLevel > MINIMUM_DUMP_LEVEL){
    cout << endl << "  ### External Row Cluster Validation ###" << endl << endl;
    if (isShowingEachCluster)
      ev.printCM(cout);
    ev.purity_Entropy_MutInfo(cout, isShowingEachCluster);
    ev.F_measure(cout);
    ev.micro_avg_precision_recall(rowPrecision, rowRecall, cout);
//  }
  if (statisticsAccessMode != NO_OPEN_MODE){
    statisticsFile << endl << "  ### External Row Cluster Validation ###" << endl << endl;
    if (isShowingEachCluster)
      ev.printCM(statisticsFile);
    ev.purity_Entropy_MutInfo(statisticsFile, isShowingEachCluster);
    ev.F_measure(statisticsFile);
    ev.micro_avg_precision_recall(rowPrecision, rowRecall, statisticsFile);
  }
  if (dumpAccessMode != NO_OPEN_MODE){
    dumpFile << endl << "  ### External Row Cluster Validation ###" << endl << endl;
    if (isShowingEachCluster)
      ev.printCM(dumpFile);
    ev.purity_Entropy_MutInfo(dumpFile, isShowingEachCluster);
    ev.F_measure(dumpFile);
    ev.micro_avg_precision_recall(rowPrecision, rowRecall, dumpFile);
  }
*/
  if (dumpLevel >= MINIMUM_DUMP_LEVEL)		// Don't need to check, but...
    cout << endl << "  ### External Row Cluster Validation ###" << endl << endl;
  if (dumpAccessMode != NO_OPEN_MODE)
    dumpFile << endl << "  ### External Row Cluster Validation ###" << endl << endl;
  if (statisticsAccessMode != NO_OPEN_MODE)
    statisticsFile << endl << "  ### External Row Cluster Validation ###" << endl << endl;
  if (isShowingEachCluster){
      ev.printCM(cout);
      ev.printCM(dumpFile);
      ev.printCM(statisticsFile);
  }
  ev.purity_Entropy_MutInfo(isShowingEachCluster, cout, dumpFile, statisticsFile);
  ev.F_measure(cout, dumpFile, statisticsFile);
  ev.micro_avg_precision_recall(rowPrecision, rowRecall, cout, dumpFile, statisticsFile);
  ev.getAccuracy(rowAccuracy, cout, dumpFile, statisticsFile);
}

void Coclustering::validateColCluster(int numColClass, int *colClassLabel)
{
  ExternalValidity ev(numColClass, numColCluster, numCol, colClassLabel, colCL);
/*
//  if (dumpLevel > MINIMUM_DUMP_LEVEL){
    cout << endl << "  ### External Column Cluster Validation ###" << endl << endl;
    if (isShowingEachCluster)
      ev.printCM(cout);
    ev.purity_Entropy_MutInfo(cout, isShowingEachCluster);
    ev.F_measure(cout);
    ev.micro_avg_precision_recall(rowPrecision, rowRecall, cout);
//  }
  if (statisticsAccessMode != NO_OPEN_MODE){
    statisticsFile << endl << "  ### External Column Cluster Validation ###" << endl << endl;
    if (isShowingEachCluster)
      ev.printCM(statisticsFile);
    ev.purity_Entropy_MutInfo(statisticsFile, isShowingEachCluster);
    ev.F_measure(statisticsFile);
    ev.micro_avg_precision_recall(rowPrecision, rowRecall, statisticsFile);
  }
  if (dumpAccessMode != NO_OPEN_MODE){
    dumpFile << endl << "  ### External Column Cluster Validation ###" << endl << endl;
    if (isShowingEachCluster)
      ev.printCM(dumpFile);
    ev.purity_Entropy_MutInfo(dumpFile, isShowingEachCluster);
    ev.F_measure(dumpFile);
    ev.micro_avg_precision_recall(rowPrecision, rowRecall, dumpFile);
  }
*/
  if (dumpLevel >= MINIMUM_DUMP_LEVEL)		// Don't need to check, but...
    cout << endl << "  ### External Column Cluster Validation ###" << endl << endl;
  if (dumpAccessMode != NO_OPEN_MODE)
    dumpFile << endl << "  ### External Column Cluster Validation ###" << endl << endl;
  if (statisticsAccessMode != NO_OPEN_MODE)
    statisticsFile << endl << "  ### External Column Cluster Validation ###" << endl << endl;
  if (isShowingEachCluster){
      ev.printCM(cout);
      ev.printCM(dumpFile);
      ev.printCM(statisticsFile);
  }
  ev.purity_Entropy_MutInfo(isShowingEachCluster, cout, dumpFile, statisticsFile);
  ev.F_measure(cout, dumpFile, statisticsFile);
  ev.micro_avg_precision_recall(colPrecision, colRecall, cout, dumpFile, statisticsFile);
  ev.getAccuracy(colAccuracy, cout, dumpFile, statisticsFile);
}

double Coclustering::getRowPrecision()
{
  return rowPrecision;
}

double Coclustering::getRowRecall()
{
  return rowRecall;
}

double Coclustering::getRowAccuracy()
{
  return rowAccuracy;
}

double Coclustering::getColPrecision()
{
  return colPrecision;
}

double Coclustering::getColRecall()
{
  return colRecall;
}

double Coclustering::getColAccuracy()
{
  return colAccuracy;
}

void Coclustering::clearMark4Row()
{
  for (int i = 0; i < numRow; i++)
    isRowMarked[i] = false;
}


void Coclustering::clearMark4Col()
{
  for (int i = 0; i < numCol; i++)
    isColMarked[i] = false;
}


void Coclustering::checkDumpLevel4Cocluster(ostream &os)
{ 
  if (dumpLevel == MAXIMUM_DUMP_LEVEL){
    os << "  Row cluster labels:" << endl;
    os << "  ";
    for (int r = 0; r < numRow; r++)
      os << rowCL[r] << " ";
    os << endl << "  Col cluster labels:" << endl;
    os << "  ";
    for (int c = 0; c < numCol; c++)
      os << colCL[c] << " ";
    os << endl << "  Compressed matrix:" << endl;
    for (int rc = 0; rc < numRowCluster; rc++){
      os << "  ";
      for (int cc = 0; cc < numColCluster; cc++)
        os << Acompressed[rc][cc] << " ";
      os << endl;
    }
  }
}


void Coclustering::checkDumpLevel4InitialObjectValue()
{
  switch (dumpLevel){
    case MINIMUM_DUMP_LEVEL:
    case MAXIMUM_DUMP_LEVEL:
      break;
    case BATCH_UPDATE_DUMP_LEVEL:
    case LOCAL_SEARCH_DUMP_LEVEL:
      cout << classPrefix << "Initial Objective Function Value = " << objValue << endl;
      break;
  }
  if (objectiveAccessMode != NO_OPEN_MODE)
    objectiveFile << "0 " << objValue << endl;
  if (isComputingOneWayObjective){
    switch (dumpLevel){
      case MINIMUM_DUMP_LEVEL:
        break;
      case BATCH_UPDATE_DUMP_LEVEL:
      case LOCAL_SEARCH_DUMP_LEVEL:
        cout << classPrefix << "Initial Obj.Func. of one-way Row = " << objValue4RowCluster << endl;
        cout << classPrefix << "Initial Obj.Func. of one-way Col = " << objValue4ColCluster << endl;
        break;
      case MAXIMUM_DUMP_LEVEL:
        dumpFile << classPrefix << "Initial Obj.Func. of one-way Row = " << objValue4RowCluster << endl;
        dumpFile << classPrefix << "Initial Obj.Func. of one-way Col = " << objValue4ColCluster << endl;
        break;
    }
    if (statisticsAccessMode != NO_OPEN_MODE){
      statisticsFile << classPrefix << "Initial Obj.Func. of one-way Row = " << objValue4RowCluster << endl;
      statisticsFile << classPrefix << "Initial Obj.Func. of one-way Col = " << objValue4ColCluster << endl;
    } 
  }    
}


void Coclustering::checkDumpLevel4FinalObjectValue()
{
  switch (dumpLevel){
    case MINIMUM_DUMP_LEVEL:
    case MAXIMUM_DUMP_LEVEL:
      break;
    case BATCH_UPDATE_DUMP_LEVEL:
    case LOCAL_SEARCH_DUMP_LEVEL:
      cout << classPrefix << "Final Objective Function Value   = " << objValue << endl;
      break;
  }
}


void Coclustering::checkDumpLevel4Centroid(double **centroid, int row, int col)
{
  if (dumpLevel == MAXIMUM_DUMP_LEVEL){
    char *title = NULL;
    if (row == numRowCluster && col == numCol){
      title = ROW_CENTROID;
    } else if (row == numColCluster && col == numRow){
      title = COL_CENTROID;
    } else {
      cout << "  Invalid argument in checkDumpLevel4Centroid(): " << row << " " << col << endl;
      dumpFile << "  Invalid argument in checkDumpLevel4Centroid(): " << row << " " << col << endl;
      exit(EXIT_FAILURE);
    }
    dumpFile << endl << title << endl;
    for (int r = 0; r < row; r++){
      dumpFile << "  ";
      for (int c = 0; c < col; c++)
	dumpFile << centroid[r][c] << " ";
      dumpFile << endl;
    }
    dumpFile << endl;
  }
}


void Coclustering::checkDumpLevel4NumOfChange(char *token, int numChange)
{
  switch (dumpLevel){
    case MINIMUM_DUMP_LEVEL:
    case BATCH_UPDATE_DUMP_LEVEL:
      break;
    case LOCAL_SEARCH_DUMP_LEVEL:
      cout << "  ### " << numChange << " " << token << " changing cluster label(s) ###" << endl;
      break;
    case MAXIMUM_DUMP_LEVEL:
      dumpFile << "  ### " << numChange << " " << token << " changing cluster label(s) ###" << endl;
      break;
  }
}


void Coclustering::checkDumpLevel4ReversedRow()
{
  if (isTakingReverse){
    computeNumReversedRow();
    switch (dumpLevel){
      case MINIMUM_DUMP_LEVEL:
      case BATCH_UPDATE_DUMP_LEVEL:
        break;
      case LOCAL_SEARCH_DUMP_LEVEL:
        cout << "  ### " << numReversedRow << " reversed row(s) ###" << endl;
        break;
      case MAXIMUM_DUMP_LEVEL:
        for (int i = 0; i < numRow; i++){
          if (isReversed[i])
            dumpFile << "-";
          dumpFile << i << ", ";
        }
        dumpFile << endl;
        dumpFile << "  ### " << numReversedRow << " reversed row(s) ###" << endl;
        break;
    }
    if (statisticsAccessMode != NO_OPEN_MODE)
      statisticsFile << "  ### " << numReversedRow << " reversed row(s) ###" << endl;
  }
}


void Coclustering::checkDumpLevel4BatchUpdate(char *token)
{
  switch (dumpLevel){
    case MINIMUM_DUMP_LEVEL:
      break;
    case BATCH_UPDATE_DUMP_LEVEL:
    case LOCAL_SEARCH_DUMP_LEVEL:
      cout << classPrefix << token << "BatchUpdate()" << endl;
      break;
    case MAXIMUM_DUMP_LEVEL:
      dumpFile << classPrefix << token << "BatchUpdate()" << endl;
      break;
  }
}


void Coclustering::checkDumpLevel4BatchUpdate(char *token, int num)
{
  if (objectiveAccessMode != NO_OPEN_MODE){
    char *objValueType = NULL;
    if (strcmp(token, "row") == 0)
      objValueType = "1 ";
    else if (strcmp(token, "col") == 0)
      objValueType = "2 ";
    else if (strcmp(token, "both") == 0)			// for Govaert's algorithm
      objValueType = "2 ";
    else {
      cout << "  Invalid argument in checkDumpLevel4BatchUpdate(): " << token << endl;
      if (dumpLevel == MAXIMUM_DUMP_LEVEL)
        dumpFile << "  Invalid argument in checkDumpLevel4BatchUpdate(): " << token << endl;
      exit(EXIT_FAILURE);
    }
    objectiveFile << objValueType << objValue << endl;
  }
  switch (dumpLevel){
    case MINIMUM_DUMP_LEVEL:
      break;
    case BATCH_UPDATE_DUMP_LEVEL:
    case LOCAL_SEARCH_DUMP_LEVEL:
      cout << classPrefix << token << "BatchUpdate[" << num << "] = " << objValue << endl;
      break;
    case MAXIMUM_DUMP_LEVEL:
      dumpFile << classPrefix << token << "BatchUpdate[" << num << "] = " << objValue << endl;
      break;
  }
}


void Coclustering::checkDumpLevel4DeltaTrace(char *token, int id, int toCluster, double delta, double minDelta)
{
  if (dumpLevel == MAXIMUM_DUMP_LEVEL){
   dumpFile << "  " << token << "[" << id << "](";
   if (strcmp(token, "row") == 0)
     dumpFile << rowCL[id];
   else if (strcmp(token, "col") == 0)
     dumpFile << colCL[id];
   else {
     cout << "  Invalid argument in checkDumpLevel4DeltaTrace(): " << token << endl;
     dumpFile << "  Invalid argument in checkDumpLevel4DeltaTrace(): " << token << endl;
     exit(EXIT_FAILURE);
   }
   dumpFile << "=>" << toCluster << "): " << delta << " + " << minDelta << " = " << (delta+minDelta) << endl;
  }
}


void Coclustering::checkDumpLevel4LocalSearch(char *token)
{
  switch (dumpLevel){
    case MINIMUM_DUMP_LEVEL:
      break;
    case BATCH_UPDATE_DUMP_LEVEL:
    case LOCAL_SEARCH_DUMP_LEVEL:
      cout << classPrefix << token << "LocalSearch()" << endl;
      break;
    case MAXIMUM_DUMP_LEVEL:
      dumpFile << classPrefix << token << "LocalSearch()" << endl;
      break;
  }
}


void Coclustering::checkDumpLevel4LocalSearch(char *token, int id, int from, int to, double change)
{
  switch (dumpLevel){
    case MINIMUM_DUMP_LEVEL:
      break;
    case LOCAL_SEARCH_DUMP_LEVEL:
      cout << classPrefix << token << "LocalSearch[" << id << "](" << from << "=>" << to << ") = " << change << endl;
      break;
    case MAXIMUM_DUMP_LEVEL:
      dumpFile << classPrefix << token << "LocalSearch[" << id << "](" << from << "=>" << to << ") = " << change << endl;
      break;
  }
}


void Coclustering::checkDumpLevel4NumOfChain(char *token, int num, double *totalChange)
{
  if (num == 0){
    switch (dumpLevel){
      case MINIMUM_DUMP_LEVEL:
      case BATCH_UPDATE_DUMP_LEVEL:
        break;
      case LOCAL_SEARCH_DUMP_LEVEL:
        cout << "  !!! No " << token << " chain taken !!!" << endl;
        break;
      case MAXIMUM_DUMP_LEVEL:
        dumpFile << "  !!! No " << token << " chain taken !!!" << endl;
        break;
    }
  } else {
    char *objValueType = NULL;
    if (strcmp(token, "row") == 0)
      objValueType = "3 ";
    else if (strcmp(token, "col") == 0)
      objValueType = "4 ";
    else {
      switch (dumpLevel){
        case MINIMUM_DUMP_LEVEL:
        case BATCH_UPDATE_DUMP_LEVEL:
	  break;
        case LOCAL_SEARCH_DUMP_LEVEL:
          cout << "  Invalid argument in checkDumpLevel4NumOfChain(): " << token << endl;
          break;
        case MAXIMUM_DUMP_LEVEL:
          dumpFile << "  Invalid argument in checkDumpLevel4NumOfChain(): " << token << endl;
          break;
      }
      exit(EXIT_FAILURE);
    }
    if (objectiveAccessMode != NO_OPEN_MODE)
      for (int i = 0; i < num+1; i++)
        objectiveFile << objValueType << (objValue + totalChange[i]) << endl;
    objValue += totalChange[num];
    switch (dumpLevel){
      case MINIMUM_DUMP_LEVEL:
      case BATCH_UPDATE_DUMP_LEVEL:
        break;
      case LOCAL_SEARCH_DUMP_LEVEL:
        cout << "  ### " << (num+1) << " " << token << " chain(s) taken ###" << endl;
        break;
      case MAXIMUM_DUMP_LEVEL:
        dumpFile << "  ### " << (num+1) << " " << token << " chain(s) taken ###" << endl;
        break;
    }
  }  
}
  
  
void Coclustering::checkDumpLevel4PingPong(char *token, int num)
{
  switch (dumpLevel){
    case MINIMUM_DUMP_LEVEL:
      break;
      case BATCH_UPDATE_DUMP_LEVEL:
      case LOCAL_SEARCH_DUMP_LEVEL:
      cout << classPrefix << token << "PingPoing(): numIteration(" << num << ")" << endl;
      if (strcmp(token, "end") == 0)
        cout << endl;
      break;
    case MAXIMUM_DUMP_LEVEL:
      dumpFile << classPrefix << token << "PingPong(): numIteration(" << num << ")" << endl;
      if (strcmp(token, "end") == 0)
        dumpFile << endl;
      break;
    default:
      break;
  }
} 


void Coclustering::checkDumpLevel4Coclustering(ostream &os, int num, double value)
{
  os << endl;
  os << classPrefix << "Initialization Method     = " << initialMethod[rowInitializationMethod] << " & " << initialMethod[colInitializationMethod] << endl;
  os << classPrefix << "Variation of Batch Update = " << batchUpdateType << endl;
  if (isTakingReverse)
    os << classPrefix << "# of Reversed Row(s)      = " << numReversedRow << endl;
  os << classPrefix << "# of PingPong Iteration   = " << num << endl;
  if (strcmp(classPrefix, ITCC_CLASS) == 0){
    os << classPrefix << "Mutual Information(MI) of input Matrix      = " << value << endl;
    os << classPrefix << "Final Objective Function Value (Loss in MI) = " << objValue << endl;
  } else if (strcmp(classPrefix, MSSRICC_CLASS) == 0){
    os << classPrefix << "Squared Frobenius Norm of input Matrix            = " << value << endl;
    os << classPrefix << "Final Objective Function Value (=||A-RR'ACC'||^2) = " << objValue << endl;
  } else if (strcmp(classPrefix, MSSRIICC_CLASS) == 0){
    os << classPrefix << "Squared Frobenius Norm of input Matrix                    = " << value << endl;
    os << classPrefix << "Final Objective Function Value (=||A-RR'A-ACC'+RR'ACC'||) = " << objValue << endl;
  } else {
    os << "Invalid algorithm type in checkDumpLevel4Coclustering(): " << classPrefix << endl;
    exit(EXIT_FAILURE);
  }
  if (isComputingOneWayObjective){
    os << classPrefix << "Final Obj.Func.Value of One-way Row Cluster  = " << objValue4RowCluster << endl;
    os << classPrefix << "Final Obj.Func.Value of One-way Col Cluster  = " << objValue4ColCluster << endl;
  } 
  if (rowInitializationMethod == SEEDING_INIT)
    os << classPrefix << "Row seeding file          = " << rowSeedingFilename << endl;
  if (colInitializationMethod == SEEDING_INIT)
    os << classPrefix << "Column seeding file       = " << colSeedingFilename << endl;
  if (coclusterAccessMode != NO_OPEN_MODE)
    os << classPrefix << "Cocluster file            = " << coclusterFilename << endl;
  if (dumpLevel == MAXIMUM_DUMP_LEVEL)
    os << classPrefix << "Dump file                 = " << dumpFilename << endl;
  if (objectiveAccessMode != NO_OPEN_MODE)
    os << classPrefix << "Objective file            = " << objectiveFilename << endl;
  if (statisticsAccessMode != NO_OPEN_MODE)
    os << classPrefix << "Statistics file           = " << statisticsFilename << endl;
//  os << endl;
}

