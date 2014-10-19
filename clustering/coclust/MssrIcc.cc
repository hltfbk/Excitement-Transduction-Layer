/*
  MssrIcc.cc
    implementation of the coclustering algothm for the first problem, H = A - RR'ACC'
    in the paper, "Minimum Sum-Squared Residue Co-clustering of Gene Expression Data",
    with smoothing, local search, and variations of batch and local search update.
  cf. 	||A-Ahat||^2 	== ||A||^2 - ||R'*A*C||^2 
      			== ||A||^2 - sum_rc (Atilde_rc^2)/(rowCS[r]*colCS[c])	for unnormalized Atilde
			== ||A||^2 - sum_rc (Atilde_rc^2)*(rowCS[r]*colCS[c])	for normalized Atilde
  cf.   In batch update, we use a normalized compressed matrix.
        However, in local search (i.e., First Variation), we use an unnormalized compressed matrix 
	in order to directly add and subtract values. "isNormalized" flag is used for cheching normalization.

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#include <iostream>
#include <fstream>

#include "MatrixVector.h"
#include "MssrIcc.h"


MssrIcc::MssrIcc(Matrix *inputMatrix_CCS, Matrix *inputMatrix_CRS, commandLineArgument myCLA): Coclustering(inputMatrix_CCS, inputMatrix_CRS, myCLA)
{
//  cout << endl << "MssrIcc::MssrIcc()" << endl;
  if (isTakingReverse){
    isReversed = new bool[numRow];
    for (int r = 0; r < numRow; r++)
      isReversed[r] = false;
    memoryUsed += numRow * sizeof(bool);
  }
  squaredFNormA = myCCS->squaredFNorm();		// Sum_ij (A_ij)^2
  rowQuality4Compressed = new double[numRowCluster];		// rowQuality4Compressed[i] = sum_j (Acompressed_ij)^2/(rowCS[i]*colCS[j])
  colQuality4Compressed = new double[numColCluster];		// colQuality4Compressed[j] = sum_i (Acompressed_ij)^2/(rowCS[i]*colCS[j])
  isNormalizedCompressed = isNormalizedRowCentroid = isNormalizedColCentroid = false;
  memoryUsed += (numRowCluster + numColCluster) * sizeof(double);
  if (isComputingOneWayObjective){
    rowCentroid = new double*[numRowCluster];
    for (int rc = 0; rc < numRowCluster; rc++)
      rowCentroid[rc] = new double[numCol];
    colCentroid = new double*[numColCluster];
    for (int cc = 0; cc < numColCluster; cc++)
      colCentroid[cc] = new double[numRow];
    memoryUsed += (numRowCluster * numCol + numRow * numColCluster) * sizeof(double);
  }
}


MssrIcc::~MssrIcc()
{
  if (isTakingReverse)
    delete [] isReversed;
  delete [] rowQuality4Compressed;
  delete [] colQuality4Compressed;
  if (isComputingOneWayObjective){
    for (int rc = 0; rc < numRowCluster; rc++)
      delete [] rowCentroid[rc];
    delete [] rowCentroid;
    for (int cc = 0; cc < numColCluster; cc++)
      delete [] colCentroid[cc];
    delete [] colCentroid;
  }
//  cout << endl << "MssrIcc::~MssrIcc()" << endl;
}


void MssrIcc::doInitialization()
{
  chooseInitializationMethod();
  isEmptyRowClusterReported = isEmptyColClusterReported = false;
  computeRowClusterSize();
  computeColClusterSize();
  if (isTakingReverse)
    computeAcompressed(isReversed);
  else
    computeAcompressed();
  isNormalizedCompressed = false;
  computeObjectiveFunction4Unnormalized();
//  cout << "Initialization done..." << endl;
  if (isComputingOneWayObjective){
    computeRowCentroid();
    normalizeRowCentroid();
    computeObjectiveFunction4RowCluster();
    computeColCentroid();
    normalizeColCentroid();  
    computeObjectiveFunction4ColCluster();
  }
  checkDumpLevel4InitialObjectValue();
}


void MssrIcc::computeRowCentroid()
{
  myCRS->computeRowCentroid(numRowCluster, rowCL, rowCentroid);
  isNormalizedRowCentroid = false;
}


void MssrIcc::computeColCentroid()
{
  myCCS->computeColCentroid(numColCluster, colCL, colCentroid);
  isNormalizedColCentroid = false;
}


void MssrIcc::normalizeRowCentroid()
{
//  assert(!isNormalizedRowCentroid);
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int c = 0; c < numCol; c++)
        if (rowCentroid[rc][c] != 0)
          rowCentroid[rc][c] /= rowCS[rc];
  isNormalizedRowCentroid = true;
}


void MssrIcc::normalizeColCentroid()
{
//  assert(!isNormalizedColCentroid);
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int r = 0; r < numRow; r++)
        if (colCentroid[cc][r] != 0)
          colCentroid[cc][r] /= colCS[cc];
  isNormalizedColCentroid = true;
}


void MssrIcc::normalizeCompressedMatrix()
{
//  assert(!isNormalizedCompressed);
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int cc = 0; cc < numColCluster; cc++)
        if (colCS[cc] > 0)
          Acompressed[rc][cc] /= (rowCS[rc] * colCS[cc]);
  isNormalizedCompressed = true;
}


void MssrIcc::computeObjectiveFunction4Unnormalized()
{
  checkDumpLevel4Cocluster(dumpFile);
  objValue = squaredFNormA - computeQuality4CompressedUnnormalized();
}


void MssrIcc::computeObjectiveFunction4Normalized()
{
  checkDumpLevel4Cocluster(dumpFile);
  objValue = squaredFNormA - computeQuality4CompressedNormalized();
}


void MssrIcc::computeObjectiveFunction4Normalized(double **Acompressed)
{
  checkDumpLevel4Cocluster(dumpFile);
//  objValue = squaredFNormA - computeQuality4CompressedNormalized(tempRowCL, tempColCL);
  objValue = myCCS->computeObjectiveFunctionValue(rowCL, colCL, Acompressed);
}


void MssrIcc::computeObjectiveFunction4Normalized(double **Acompressed, bool *isReversed)
{
  checkDumpLevel4Cocluster(dumpFile);
//  objValue = squaredFNormA - computeQuality4CompressedNormalized(tempRowCL, tempColCL);
  objValue = myCCS->computeObjectiveFunctionValue(rowCL, colCL, Acompressed, isReversed);
}


void MssrIcc::computeObjectiveFunction4RowCluster()
{
  objValue4RowCluster = myCRS->computeObjectiveFunctionValue4RowCluster(rowCL, rowCentroid);
}


void MssrIcc::computeObjectiveFunction4ColCluster()
{
  objValue4ColCluster = myCCS->computeObjectiveFunctionValue4ColCluster(colCL, colCentroid);
}


void MssrIcc::computeRowQuality4Compressed2WayUnnormalized()
{
  double tempValue = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    rowQuality4Compressed[rc] = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int cc = 0; cc < numColCluster; cc++){
        tempValue = Acompressed[rc][cc];
        if (tempValue != 0 && colCS[cc] > 0)
	  rowQuality4Compressed[rc] += (tempValue * tempValue) / (rowCS[rc] * colCS[cc]);
      }
}


void MssrIcc::computeRowQuality4Compressed2WayNormalized()
{
  double tempValue = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    rowQuality4Compressed[rc] = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int cc = 0; cc < numColCluster; cc++){
        tempValue = Acompressed[rc][cc];
        if (tempValue != 0 && colCS[cc] > 0)
	  rowQuality4Compressed[rc] += (tempValue * tempValue) * (rowCS[rc] * colCS[cc]);
      }
}


void MssrIcc::computeRowQuality4Compressed1WayUnnormalized()
{
  double tempValue = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    rowQuality4Compressed[rc] = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int cc = 0; cc < numColCluster; cc++){
        tempValue = Acompressed[rc][cc];
        if (tempValue != 0 && colCS[cc] > 0)
          rowQuality4Compressed[rc] += (tempValue * tempValue) / (rowCS[rc] * rowCS[rc] * colCS[cc]);
      }
}


void MssrIcc::computeRowQuality4Compressed1WayNormalized()
{
  double tempValue = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    rowQuality4Compressed[rc] = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int cc = 0; cc < numColCluster; cc++){
        tempValue = Acompressed[rc][cc];
        if (tempValue != 0 && colCS[cc] > 0)
	  rowQuality4Compressed[rc] += (tempValue * tempValue) * colCS[cc];
      }
}


double MssrIcc::computeRowQuality4Compressed2WayUnnormalized(int rc)
{
  double tempValue = 0, temp = 0;
  if (rowCS[rc] > 0){
    for (int cc = 0; cc < numColCluster; cc++){
      tempValue = Acompressed[rc][cc];
      if (tempValue != 0 && colCS[cc] > 0)
        temp += (tempValue * tempValue) / (rowCS[rc] * colCS[cc]);
    }
  }
  return temp;
}


double MssrIcc::computeRowQuality4Compressed2WayNormalized(int rc)
{
  double tempValue = 0, temp = 0;
  if (rowCS[rc] > 0){
    for (int cc = 0; cc < numColCluster; cc++){
      tempValue = Acompressed[rc][cc];
      if (tempValue != 0 && colCS[cc] > 0)
        temp += (tempValue * tempValue) * (rowCS[rc] * colCS[cc]);
    }
  }
  return temp;
}


double MssrIcc::computeRowQuality4Compressed1WayUnnormalized(int rc)
{
  double tempValue = 0, temp = 0;
  if (rowCS[rc] > 0){
    for (int cc = 0; cc < numColCluster; cc++){
      tempValue = Acompressed[rc][cc];
      if (tempValue != 0 && colCS[cc] > 0)
        temp += (tempValue * tempValue) / (rowCS[rc] * rowCS[rc] * colCS[cc]);
    }
  }
  return temp;
}


double MssrIcc::computeRowQuality4Compressed1WayNormalized(int rc)
{
  double tempValue = 0, temp = 0;
  if (rowCS[rc] > 0){
    for (int cc = 0; cc < numColCluster; cc++){
      tempValue = Acompressed[rc][cc];
      if (tempValue != 0 && colCS[cc] > 0)
        temp += (tempValue * tempValue) * colCS[cc];
    }
  }
  return temp;
}


double MssrIcc::computeRowQuality4Compressed2WayUnnormalized(double *row2Way, int rowClusterSize)
{
  double tempValue = 0, temp = 0;
  if (rowClusterSize > 0){
    for (int cc = 0; cc < numColCluster; cc++){
      tempValue = row2Way[cc];
      if (tempValue != 0 && colCS[cc] > 0)
        temp += (tempValue * tempValue) / (rowClusterSize * colCS[cc]);
    }
  }
  return temp;
}


double MssrIcc::computeRowQuality4Compressed2WayNormalized(double *row2Way, int rowClusterSize)
{
  double tempValue = 0, temp = 0;
  if (rowClusterSize > 0){
    for (int cc = 0; cc < numColCluster; cc++){
      tempValue = row2Way[cc];
      if (tempValue != 0 && colCS[cc] > 0)
        temp += (tempValue * tempValue) * (rowClusterSize * colCS[cc]);
    }
  }
  return temp;
}


double MssrIcc::computeRowQuality4Compressed1WayUnnormalized(double *row1Way, int rowClusterSize)
{
  double tempValue = 0, temp = 0;
  if (rowClusterSize > 0){
    for (int cc = 0; cc < numColCluster; cc++){
      tempValue = row1Way[cc];
      if (tempValue != 0 && colCS[cc] > 0)
        temp += (tempValue * tempValue) / (rowClusterSize * rowClusterSize * colCS[cc]);
    }
  }
  return temp;
}


double MssrIcc::computeRowQuality4Compressed1WayNormalized(double *row1Way, int rowClusterSize)
{
  double tempValue = 0, temp = 0;
  if (rowClusterSize > 0){
    for (int cc = 0; cc < numColCluster; cc++){
      tempValue = row1Way[cc];
      if (tempValue != 0 && colCS[cc] > 0)
        temp += (tempValue * tempValue) * colCS[cc];
    }
  }
  return temp;
}


void MssrIcc::computeColQuality4Compressed2WayUnnormalized()
{
  double tempValue = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    colQuality4Compressed[cc] = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int rc = 0; rc < numRowCluster; rc++){
        tempValue = Acompressed[rc][cc];
        if (tempValue != 0 && rowCS[rc] > 0)
          colQuality4Compressed[cc] += (tempValue * tempValue) / (rowCS[rc] * colCS[cc]);
      }
}


void MssrIcc::computeColQuality4Compressed2WayNormalized()
{
  double tempValue = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    colQuality4Compressed[cc] = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int rc = 0; rc < numRowCluster; rc++){
        tempValue = Acompressed[rc][cc];
        if (tempValue != 0 && rowCS[rc] > 0)
          colQuality4Compressed[cc] += (tempValue * tempValue) * (rowCS[rc] * colCS[cc]);
      }
}


void MssrIcc::computeColQuality4Compressed1WayUnnormalized()
{
  double tempValue = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    colQuality4Compressed[cc] = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int rc = 0; rc < numRowCluster; rc++){
        tempValue = Acompressed[rc][cc];
        if (tempValue != 0 && rowCS[rc] > 0)
          colQuality4Compressed[cc] += (tempValue * tempValue) / (rowCS[rc] * colCS[cc] * colCS[cc]);
      }
}


void MssrIcc::computeColQuality4Compressed1WayNormalized()
{
  double tempValue = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    colQuality4Compressed[cc] = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int rc = 0; rc < numRowCluster; rc++){
        tempValue = Acompressed[rc][cc];
        if (tempValue != 0 && rowCS[rc] > 0)
          colQuality4Compressed[cc] += (tempValue * tempValue) * rowCS[rc];
      }
}


double MssrIcc::computeColQuality4Compressed2WayUnnormalized(int cc)
{
  double tempValue = 0, temp = 0;
  if (colCS[cc] > 0){
    for (int rc = 0; rc < numRowCluster; rc++){
      tempValue = Acompressed[rc][cc];
      if (tempValue != 0 && rowCS[rc] > 0)
        temp += (tempValue * tempValue) / (rowCS[rc] * colCS[cc]);
    }
  }
  return temp;
}


double MssrIcc::computeColQuality4Compressed2WayNormalized(int cc)
{
  double tempValue = 0, temp = 0;
  if (colCS[cc] > 0){
    for (int rc = 0; rc < numRowCluster; rc++){
      tempValue = Acompressed[rc][cc];
      if (tempValue != 0 && rowCS[rc] > 0)
        temp += (tempValue * tempValue) * (rowCS[rc] * colCS[cc]);
    }
  }
  return temp;
}


double MssrIcc::computeColQuality4Compressed1WayUnnormalized(int cc)
{
  double tempValue = 0, temp = 0;
  if (colCS[cc] > 0){
    for (int rc = 0; rc < numRowCluster; rc++){
      tempValue = Acompressed[rc][cc];
      if (tempValue != 0 && rowCS[rc] > 0)
        temp += (tempValue * tempValue) / (rowCS[rc] * colCS[cc] * colCS[cc]);
    }
  }
  return temp;
}


double MssrIcc::computeColQuality4Compressed1WayNormalized(int cc)
{
  double tempValue = 0, temp = 0;
  if (colCS[cc] > 0){
    for (int rc = 0; rc < numRowCluster; rc++){
      tempValue = Acompressed[rc][cc];
      if (tempValue != 0 && rowCS[rc] > 0)
        temp += (tempValue * tempValue) * rowCS[rc];
    }
  }
  return temp;
}


double MssrIcc::computeColQuality4Compressed2WayUnnormalized(double *col2Way, int colClusterSize)
{
  double tempValue = 0, temp = 0;
  if (colClusterSize > 0){
    for (int rc = 0; rc < numRowCluster; rc++){
      tempValue = col2Way[rc];
      if (tempValue != 0 && rowCS[rc] > 0)
        temp += (tempValue * tempValue) / (rowCS[rc] * colClusterSize);
    }
  }
  return temp;
}


double MssrIcc::computeColQuality4Compressed2WayNormalized(double *col2Way, int colClusterSize)
{
  double tempValue = 0, temp = 0;
  if (colClusterSize > 0){
    for (int rc = 0; rc < numRowCluster; rc++){
      tempValue = col2Way[rc];
      if (tempValue != 0 && rowCS[rc] > 0)
        temp += (tempValue * tempValue) * (rowCS[rc] * colClusterSize);
    }
  }
  return temp;
}


double MssrIcc::computeColQuality4Compressed1WayUnnormalized(double *col1Way, int colClusterSize)
{
  double tempValue = 0, temp = 0;
  if (colClusterSize > 0){
    for (int rc = 0; rc < numRowCluster; rc++){
      tempValue = col1Way[rc];
      if (tempValue != 0 && rowCS[rc] > 0)
        temp += (tempValue * tempValue) / (rowCS[rc] * colClusterSize * colClusterSize);
    }
  }
  return temp;
}


double MssrIcc::computeColQuality4Compressed1WayNormalized(double *col1Way, int colClusterSize)
{
  double tempValue = 0, temp = 0;
  if (colClusterSize > 0){
    for (int rc = 0; rc < numRowCluster; rc++){
      tempValue = col1Way[rc];
      if (tempValue != 0 && rowCS[rc] > 0)
        temp += (tempValue * tempValue) * rowCS[rc];
    }
  }
  return temp;
}


double MssrIcc::computeQuality4CompressedUnnormalized()
{
  double tempValue = 0, temp = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int cc = 0; cc < numColCluster; cc++){
        tempValue = Acompressed[rc][cc];
        if (tempValue != 0 && colCS[cc] > 0)
          temp += (tempValue * tempValue) / (rowCS[rc] * colCS[cc]);
      }
  return temp;
}


double MssrIcc::computeQuality4CompressedNormalized()
{
  double tempValue = 0, temp = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int cc = 0; cc < numColCluster; cc++){
        tempValue = Acompressed[rc][cc];
        if (tempValue != 0 && colCS[cc] > 0)
          temp += (tempValue * tempValue) * (rowCS[rc] * colCS[cc]);
      }
  return temp;
}

void MssrIcc::doRowFarthestInitialization()
{
  
}

void MssrIcc::doColFarthestInitialization()
{

}

void MssrIcc::doRowRandomPerturbInitialization()
{
}

void MssrIcc::doColRandomPerturbInitialization()
{
}

double MssrIcc::rowDistance(int r, int rc)
{
//  assert(isNormalizedCompressed);
  return myCRS->computeRowDistance(r, rc, colCL, Acompressed, rowQuality4Compressed[rc]);
}


double MssrIcc::rowDistance(int r, int rc, bool *isReversed)
{
//  assert(isNormalizedCompressed);
  return myCRS->computeRowDistance(r, rc, colCL, Acompressed, rowQuality4Compressed[rc], isReversed);
}


double MssrIcc::colDistance(int c, int cc)
{
//  assert(isNormalizedCompressed);
  return myCCS->computeColDistance(c, cc, rowCL, Acompressed, colQuality4Compressed[cc]);
}


double MssrIcc::colDistance(int c, int cc, bool *isReversed)
{
//  assert(isNormalizedCompressed);
  return myCCS->computeColDistance(c, cc, rowCL, Acompressed, colQuality4Compressed[cc], isReversed);
}


void MssrIcc::reassignRC()
{
  int rowClusterChange = 0;
  int tempRowCL, minCL;
  double tempDistance, minDistance;
  for (int r = 0; r < numRow; r++){
    tempRowCL = rowCL[r];
    minCL = tempRowCL;
    minDistance = MY_DBL_MAX;
    for (int rc = 0; rc < numRowCluster; rc++){
      if (rowCS[rc] > 0){
        tempDistance = rowDistance(r, rc); 
        updateVariable(minDistance, minCL, tempDistance, rc);
      }
    }
    if (minCL != tempRowCL)
      rowClusterChange++;
    rowCL[r] = minCL;
  }
  checkDumpLevel4NumOfChange("row(s)", rowClusterChange);
}


void MssrIcc::reassignRC(bool *isReversed)
{
  bool tempIsReversed = false;
  int rowClusterChange = 0;
  int tempRowCL, minCL;
  double tempDistance, minDistance;
  for (int r = 0; r < numRow; r++){
    tempRowCL = rowCL[r];
    minCL = tempRowCL;
    
    if (isReversed[r]){
      tempIsReversed = true;
      minDistance = MY_DBL_MAX;
      for (int rc = 0; rc < numRowCluster; rc++){
        if (rowCS[rc] > 0){
          tempDistance = rowDistance(r, rc, isReversed); 
	  updateVariable(minDistance, minCL, tempDistance, rc);
        }
      }
      isReversed[r] = false; 
      for (int rc = 0; rc < numRowCluster; rc++){
        if (rowCS[rc] > 0){
          tempDistance = rowDistance(r, rc); 
	  updateVariable(minDistance, minCL, tempIsReversed, tempDistance, rc, false);
        }
      } 

    } else {					// (i.e., isReversed[r] == false)

      tempIsReversed = false;
      minDistance = MY_DBL_MAX;
      for (int rc = 0; rc < numRowCluster; rc++)
        if (rowCS[rc] > 0){
          tempDistance = rowDistance(r, rc); 
	  updateVariable(minDistance, minCL, tempDistance, rc);
        } 
      isReversed[r] = true;
      for (int rc = 0; rc < numRowCluster; rc++){
       if (rowCS[rc] > 0){
          tempDistance = rowDistance(r, rc, isReversed); 
	  updateVariable(minDistance, minCL, tempIsReversed, tempDistance, rc, true);
        }
      }      
    
    }
    if (minCL != tempRowCL)
      rowClusterChange++;
    rowCL[r] = minCL;
    isReversed[r] = tempIsReversed;
  }
  checkDumpLevel4NumOfChange("row(s)", rowClusterChange);
  checkDumpLevel4ReversedRow();
}


void MssrIcc::reassignCC()
{
  int colClusterChange = 0;
  int tempColCL, minCL;
  double minDistance, tempDistance;
  for (int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    minCL = tempColCL;
    minDistance = MY_DBL_MAX;
    for (int cc = 0; cc < numColCluster; cc++){
      if (colCS[cc] > 0){
        tempDistance = colDistance(c, cc); 
	updateVariable(minDistance, minCL, tempDistance, cc);
      }  
    }
    if (minCL != tempColCL)
      colClusterChange++;
    colCL[c] = minCL;
  }
  checkDumpLevel4NumOfChange("col(s)", colClusterChange);
}


void MssrIcc::reassignCC(bool *isReversed)
{
  int colClusterChange = 0;
  int tempColCL, minCL;
  double tempDistance, minDistance;
  for (int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    minCL = tempColCL;
    minDistance = MY_DBL_MAX;
    for (int cc = 0; cc < numColCluster; cc++){
      if (colCS[cc] > 0){
        tempDistance = colDistance(c, cc, isReversed); 
	updateVariable(minDistance, minCL, tempDistance, cc);
      }  
    }
    if (minCL != tempColCL)
      colClusterChange++;
    colCL[c] = minCL;
  }
  checkDumpLevel4NumOfChange("col(s)", colClusterChange);
}


void MssrIcc::doBatchUpdate()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeAcompressed();
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();

  do {
    numIteration++;

    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;
    
    if (numRowCluster < numRow){
      computeRowQuality4Compressed1WayNormalized();
      reassignRC();
      computeRowClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized();

      checkDumpLevel4BatchUpdate("row", numIteration);
    }
    
    if (numColCluster < numCol){
      computeColQuality4Compressed1WayNormalized();
      reassignCC();
      computeColClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized();
    
      checkDumpLevel4BatchUpdate("col", numIteration);
    }
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIcc::doBatchUpdate(bool *isReversed)
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeAcompressed(isReversed);
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();

  do {
    numIteration++;
    
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;
    
    if (numRowCluster < numRow){
      computeRowQuality4Compressed1WayNormalized();
      reassignRC(isReversed);
      computeRowClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized();

      checkDumpLevel4BatchUpdate("row", numIteration);
    }
    
    if (numColCluster < numCol){
      computeColQuality4Compressed1WayNormalized();
      reassignCC(isReversed);
      computeColClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized();

      checkDumpLevel4BatchUpdate("col", numIteration);
    } 
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
  
  checkDumpLevel4BatchUpdate("end");
}


void MssrIcc::doBatchUpdate4VariationI()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed();
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();

  do {
    numIteration++;
    
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;
    computeRowQuality4Compressed1WayNormalized();
//-----------
    reassignRC();
    computeRowClusterSize();
//    computeObjectiveFunction4Normalized();		// Since Acompressed is not a real centroid, we should not use this.
    computeObjectiveFunction4Normalized(Acompressed);
    checkDumpLevel4BatchUpdate("row", numIteration);
//-----------
    computeColQuality4Compressed1WayNormalized();
    reassignCC();
    computeColClusterSize();
//    computeObjectiveFunction4Normalized();		// Since Acompressed is not a real centroid, we should not use this.
    computeObjectiveFunction4Normalized(Acompressed);
    checkDumpLevel4BatchUpdate("col", numIteration);
//-----------
    computeAcompressed();
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeObjectiveFunction4Normalized();		// Since Acompressed is a real centroid, we can use both functions.
//    computeObjectiveFunction4Normalized(Acompressed);
    checkDumpLevel4BatchUpdate("both", numIteration);
//------------
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIcc::doBatchUpdate4VariationII()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;
  double tempRowSmoothingFactor = rowSmoothingFactor, tempColSmoothingFactor = colSmoothingFactor;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed();
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  
  myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
  myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    oldObjValue = objValue;
    computeRowQuality4Compressed1WayNormalized();
//-----------
    reassignRC();
    computeRowClusterSize();
    computeAcompressed();
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("row", numIteration);
    tempRowSmoothingFactor *= myCRS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
//-----------
  numIteration = 0;
  tempRowSmoothingFactor = rowSmoothingFactor;
  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  do {
    numIteration++;
    myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
    oldObjValue = objValue;
    computeColQuality4Compressed1WayNormalized();
    reassignCC();
    computeColClusterSize();
    computeAcompressed();
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("col", numIteration);
    tempColSmoothingFactor *= myCCS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (colBatchUpdateThreshold * squaredFNormA));
  colSmoothingFactor = tempColSmoothingFactor;
  checkDumpLevel4BatchUpdate("end");
}

/*
// It doesn't guarantee the monotonic decrease of objective function values.
void MssrIcc::doBatchUpdate4VariationIII()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0, numRowIteration = 0, numColIteration = 0;
  int *beforeRowCL = new int[numRow];
  int *afterRowCL = new int[numRow];
  double oldObjValue;
  double tempRowSmoothingFactor = rowSmoothingFactor, tempColSmoothingFactor = colSmoothingFactor;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed();
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  do {
    numIteration++;
    for (int r = 0; r < numRow; r++)
      beforeRowCL[r] = rowCL[r];
    myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
    do {
      numRowIteration++;
      myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
      oldObjValue = objValue;
      computeRowQuality4Compressed1WayNormalized();
//-----------
      reassignRC();
      computeRowClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("row", numRowIteration);
      tempRowSmoothingFactor *= myCRS->getAnnealingFactor();
    } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
//-----------
    for (int r = 0; r < numRow; r++){
      afterRowCL[r] = rowCL[r];
      rowCL[r] = beforeRowCL[r];
    }
    computeRowClusterSize();
    computeAcompressed();
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    for (int r = 0; r < numRow; r++)
      rowCL[r] = afterRowCL[r];
    computeRowClusterSize();
    tempRowSmoothingFactor = rowSmoothingFactor;
    myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
//-----------
    do {
      numColIteration++;
      myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
      oldObjValue = objValue;
      computeColQuality4Compressed1WayNormalized();
      reassignCC();
      computeColClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized(Acompressed);
      checkDumpLevel4BatchUpdate("col", numColIteration);
      tempColSmoothingFactor *= myCCS->getAnnealingFactor();
    } while ((oldObjValue - objValue) > (colBatchUpdateThreshold * squaredFNormA));
//-----------
    oldObjValue = objValue;
    computeRowClusterSize();
    computeAcompressed();
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    oldObjValue = objValue;
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("both", numIteration);
//-----------
  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
 
  colSmoothingFactor = tempColSmoogthingFactor;
  delete [] beforeRowCL;
  delete [] afterRowCL;
  checkDumpLevel4BatchUpdate("end");
}
*/


void MssrIcc::doBatchUpdate4VariationIII()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;
  computeAcompressed();
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();

  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;

    if (randNumGenerator.GetUniform() > SELECTION_PROBABILITY){
      computeRowQuality4Compressed1WayNormalized();
      reassignRC();
      computeRowClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("row", numIteration);
    } else {
      computeColQuality4Compressed1WayNormalized();
      reassignCC();
      computeColClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("col", numIteration);
    }    
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIcc::doBatchUpdate4VariationIV()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;
  computeAcompressed();
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();

  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);

    if (randNumGenerator.GetUniform() > SELECTION_PROBABILITY){
      do {
        oldObjValue = objValue;
        computeRowQuality4Compressed1WayNormalized();
        reassignRC();
        computeRowClusterSize();
        computeAcompressed();
        isNormalizedCompressed = false;
        normalizeCompressedMatrix();
        computeObjectiveFunction4Normalized();
        checkDumpLevel4BatchUpdate("row", numIteration);
      } while ((oldObjValue - objValue) > (MULTIPLE_FACTOR * rowBatchUpdateThreshold * squaredFNormA));
    } else {
      do {
        oldObjValue = objValue;
        computeColQuality4Compressed1WayNormalized();
        reassignCC();
        computeColClusterSize();
        computeAcompressed();
        isNormalizedCompressed = false;
        normalizeCompressedMatrix();
        computeObjectiveFunction4Normalized();
        checkDumpLevel4BatchUpdate("col", numIteration);
      } while ((oldObjValue - objValue) > (MULTIPLE_FACTOR * colBatchUpdateThreshold * squaredFNormA));
    }    
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIcc::doBatchUpdate4VariationI(bool *isReversed)
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed(isReversed);
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();

  do {
    numIteration++;
    
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;
    computeRowQuality4Compressed1WayNormalized();
//-----------
    reassignRC(isReversed);
    computeRowClusterSize();
//    computeObjectiveFunction4Normalized();		// Since Acompressed is not a real centroid, we should not use this.
    computeObjectiveFunction4Normalized(Acompressed, isReversed);
    checkDumpLevel4BatchUpdate("row", numIteration);
//-----------
    computeColQuality4Compressed1WayNormalized();
    reassignCC(isReversed);
    computeColClusterSize();
//    computeObjectiveFunction4Normalized();		// Since Acompressed is not a real centroid, we should not use this.
    computeObjectiveFunction4Normalized(Acompressed, isReversed);
    checkDumpLevel4BatchUpdate("col", numIteration);
//-----------
    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
//    computeObjectiveFunction4Normalized();		// Since Acompressed is a real centroid, we can use both functions.
    computeObjectiveFunction4Normalized(Acompressed, isReversed);
    checkDumpLevel4BatchUpdate("both", numIteration);
//------------
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIcc::doBatchUpdate4VariationII(bool *isReversed)
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;
  double tempRowSmoothingFactor = rowSmoothingFactor, tempColSmoothingFactor = colSmoothingFactor;
  
  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed(isReversed);
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  
  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
    oldObjValue = objValue;
    computeRowQuality4Compressed1WayNormalized();
//-----------
    reassignRC(isReversed);
    computeRowClusterSize();
    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("row", numIteration);
    tempRowSmoothingFactor *= myCRS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
//-----------
  numIteration = 0;
  tempRowSmoothingFactor = myCRS->getSmoothingFactor();
  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  do {
    numIteration++;
    myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
    oldObjValue = objValue;
    computeColQuality4Compressed1WayNormalized();
    reassignCC(isReversed);
    computeColClusterSize();
    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("col", numIteration);
    tempColSmoothingFactor *= myCCS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (colBatchUpdateThreshold * squaredFNormA));
  colSmoothingFactor = tempColSmoothingFactor;
  checkDumpLevel4BatchUpdate("end");
}


/*
// It doesn't guarantee the monotonic decrease of objective function values.
void MssrIcc::doBatchUpdate4VariationIII(bool *isReversed)
{
  checkDumpLevel4BatchUpdate("begin");
  bool *beforeIsReversed = new bool[numRow];
  bool *afterIsReversed = new bool[numRow];
  int numIteration = 0, numRowIteration = 0, numColIteration = 0;
  int *beforeRowCL = new int[numRow];
  int *afterRowCL = new int[numRow];
  double oldObjValue;
  double tempRowSmoothingFactor = rowSmoothingFactor, tempColSmoothingFactor = colSmoothingFactor;
  
  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed(isReversed);
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  do {
    numIteration++;
    for (int r = 0; r < numRow; r++){
      beforeIsReversed[r] = isReversed[r];
      beforeRowCL[r] = rowCL[r];
    }
    myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
    do {
      numRowIteration++;
      myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
      oldObjValue = objValue;
      computeRowQuality4Compressed1WayNormalized();
//-----------
      reassignRC(isReversed);
      computeRowClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("row", numRowIteration);
      tempRowSmoothingFactor *= myCRS->getAnnealingFactor();
    } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
//-----------
    for (int r = 0; r < numRow; r++){
      afterIsReversed[r] = isReversed[r];
      isReversed[r] = beforeIsReversed[r];
      afterRowCL[r] = rowCL[r];
      rowCL[r] = beforeRowCL[r];
    }
    computeRowClusterSize();
    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    for (int r = 0; r < numRow; r++){
      isReversed[r] = afterIsReversed[r];
      rowCL[r] = afterRowCL[r];
    }
    computeRowClusterSize();
    tempRowSmoothingFactor = rowSmoothingFactor;
    myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
//-----------
    do {
      numColIteration++;
      myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
      oldObjValue = objValue;
      computeColQuality4Compressed1WayNormalized();
      reassignCC(isReversed);
      computeColClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized(Acompressed, isReversed);
      checkDumpLevel4BatchUpdate("col", numColIteration);
      tempColSmoothingFactor *= myCCS->getAnnealingFactor();
    } while ((oldObjValue - objValue) > (colBatchUpdateThreshold * squaredFNormA));
//-----------
    oldObjValue = objValue;
    computeRowClusterSize();
    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("both", numIteration);
//-----------
  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
  colSmoothingFactor = tempColSmoothingFactor;
  delete [] beforeIsReversed;
  delete [] afterIsReversed;
  delete [] beforeRowCL;
  delete [] afterRowCL;
  checkDumpLevel4BatchUpdate("end");
}
*/


void MssrIcc::doBatchUpdate4VariationIII(bool *isReversed)
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;
  computeAcompressed(isReversed);
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();

  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;

    if (randNumGenerator.GetUniform() > SELECTION_PROBABILITY){
      computeRowQuality4Compressed1WayNormalized();
      reassignRC(isReversed);
      computeRowClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("row", numIteration);
    } else {
      computeColQuality4Compressed1WayNormalized();
      reassignCC(isReversed);
      computeColClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("col", numIteration);
    }    
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIcc::doBatchUpdate4VariationIV(bool *isReversed)
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;
  computeAcompressed(isReversed);
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();

  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);

    if (randNumGenerator.GetUniform() > SELECTION_PROBABILITY){
      do {
        oldObjValue = objValue;
        computeRowQuality4Compressed1WayNormalized();
        reassignRC(isReversed);
        computeRowClusterSize();
        computeAcompressed(isReversed);
        isNormalizedCompressed = false;
        normalizeCompressedMatrix();
        computeObjectiveFunction4Normalized();
        checkDumpLevel4BatchUpdate("row", numIteration);
      } while ((oldObjValue - objValue) > (MULTIPLE_FACTOR * rowBatchUpdateThreshold * squaredFNormA));
    } else {
      do {
        oldObjValue = objValue;
	computeColQuality4Compressed1WayNormalized();
        reassignCC(isReversed);
        computeColClusterSize();
        computeAcompressed(isReversed);
        isNormalizedCompressed = false;
        normalizeCompressedMatrix();
        computeObjectiveFunction4Normalized();
        checkDumpLevel4BatchUpdate("col", numIteration);
      } while ((oldObjValue - objValue) > (MULTIPLE_FACTOR * colBatchUpdateThreshold * squaredFNormA));
    }    
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIcc::recoverRowCL(int begin, int end, oneStep trace [])
{
  for(int r = begin; r < end; r++)
    if (trace[r].toCluster != trace[r].fromCluster) {
//        myCRS->subtractRow(Acompressed, trace[r].toCluster, trace[r].id, colCL);
        rowCS[trace[r].toCluster]--;
//        myCRS->addRow(Acompressed, trace[r].fromCluster, trace[r].id, colCL);
        rowCS[trace[r].fromCluster]++;
        rowCL[trace[r].id] = trace[r].fromCluster;
    }
}


void MssrIcc::recoverRowCL(int begin, int end, oneStep trace [], bool *isReversed)
{
  for(int r = begin; r < end; r++)
    if (isReversed[r])
      if (trace[r].toCluster != trace[r].fromCluster) {
//        myCRS->addRow(Acompressed, trace[r].toCluster, trace[r].id, colCL);
        rowCS[trace[r].toCluster]--;
//        myCRS->subtractRow(Acompressed, trace[r].fromCluster, trace[r].id, colCL);
        rowCS[trace[r].fromCluster]++;
        rowCL[trace[r].id] = trace[r].fromCluster;
      }
    else
      if (trace[r].toCluster != trace[r].fromCluster) {
//        myCRS->subtractRow(Acompressed, trace[r].toCluster, trace[r].id, colCL);
        rowCS[trace[r].toCluster]--;
//        myCRS->addRow(Acompressed, trace[r].fromCluster, trace[r].id, colCL);
        rowCS[trace[r].fromCluster]++;
        rowCL[trace[r].id] = trace[r].fromCluster;
      }
}


void MssrIcc::recoverColCL(int begin, int end, oneStep trace [])
{
  for(int c = begin; c < end; c++)
    if (trace[c].toCluster != trace[c].fromCluster) {
//      myCCS->subtractCol(Acompressed, trace[c].toCluster, trace[c].id, rowCL);
      colCS[trace[c].toCluster]--;
//      myCCS->addCol(Acompressed, trace[c].fromCluster, trace[c].id, rowCL);
      colCS[trace[c].fromCluster]++;
      colCL[trace[c].id] = trace[c].fromCluster;
    }
}


void MssrIcc::recoverColCL(int begin, int end, oneStep trace [], bool *isReversed)
{
  for(int c = begin; c < end; c++)
    if (trace[c].toCluster != trace[c].fromCluster) {
//      myCCS->subtractCol(Acompressed, trace[c].toCluster, trace[c].id, rowCL, isReversed);
      colCS[trace[c].toCluster]--;
//      myCCS->addCol(Acompressed, trace[c].fromCluster, trace[c].id, rowCL, isReversed);
      colCS[trace[c].fromCluster]++;
      colCL[trace[c].id] = trace[c].fromCluster;
    }
}


void MssrIcc::doRowLocalSearch(oneStep trace [], int step)
{
  int fromRow = 0, tempCluster, toCluster, tempRowCL;
  double delta1, delta2, minDelta = MY_DBL_MAX, minDelta2 = MY_DBL_MAX;
  double *compressedRow = new double [numColCluster];
  trace[step].id = 0;
  trace[step].fromCluster = rowCL[0];
  trace[step].toCluster = toCluster = rowCL[0];
  trace[step].change = 0;
/*
computeAcompressed();
isNormalizedCompressed = false;
computeRowQuality4Compressed2WayUnnormalized();
*/
  for (int r = 0; r < numRow; r++){
    tempRowCL = rowCL[r];
    if (rowCS[tempRowCL] > 1 && !isRowMarked[r]){
      tempCluster = tempRowCL;
      minDelta2 = MY_DBL_MAX;
      for (int cc = 0; cc < numColCluster; cc++)
        compressedRow[cc] = Acompressed[tempRowCL][cc];
      myCRS->subtractRow(compressedRow, r, colCL);
      delta1 = rowQuality4Compressed[tempRowCL] - computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[tempRowCL]-1);
      for (int rc = 0; rc < tempRowCL; rc++){
	for(int cc = 0; cc < numColCluster; cc++)
	  compressedRow[cc] = Acompressed[rc][cc];
	myCRS->addRow(compressedRow, r, colCL);
	delta2 = rowQuality4Compressed[rc] - computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1);
	updateVariable(minDelta2, tempCluster, delta2, rc);
      }
      for (int rc = tempRowCL+1; rc < numRowCluster; rc++){
	for(int cc = 0; cc < numColCluster; cc++)
	  compressedRow[cc] = Acompressed[rc][cc];
	myCRS->addRow(compressedRow, r, colCL);
	delta2 = rowQuality4Compressed[rc] - computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1);
        updateVariable(minDelta2, tempCluster, delta2, rc);
      }
      if ((delta1 + minDelta2) < minDelta){
	fromRow = r;
	toCluster = tempCluster;
        minDelta = delta1 + minDelta2;
      }
      checkDumpLevel4DeltaTrace("row", r, tempCluster, delta1, minDelta2);
    }
  }
  isRowMarked[fromRow] = true;
  trace[step].id = fromRow;
  trace[step].fromCluster = rowCL[fromRow];
  trace[step].toCluster = toCluster;
  trace[step].change = minDelta;
  rowCS[rowCL[fromRow]]--;
  rowCS[toCluster]++;

  myCRS->subtractRow(Acompressed, rowCL[fromRow], fromRow, colCL);
  rowQuality4Compressed[rowCL[fromRow]] = computeRowQuality4Compressed2WayUnnormalized(rowCL[fromRow]);
  myCRS->addRow(Acompressed, toCluster, fromRow, colCL);
  rowQuality4Compressed[toCluster] = computeRowQuality4Compressed2WayUnnormalized(toCluster);

  rowCL[fromRow] = toCluster;
  delete [] compressedRow;
  checkDumpLevel4Cocluster(dumpFile);
}


void MssrIcc::doRowLocalSearch(oneStep trace [], int step, bool *isReversed)
{
  bool tempIsReversed = false;
  int fromRow = 0, tempCluster, toCluster, tempRowCL;
  double delta1, delta2, minDelta = MY_DBL_MAX, minDelta2 = MY_DBL_MAX;
  double *compressedRow = new double [numColCluster];
  trace[step].id = 0;
  trace[step].fromCluster = rowCL[0];
  trace[step].toCluster = toCluster = rowCL[0];
  trace[step].change = 0;
/*
computeAcompressed(isReversed);
isNormalizedCompressed = false;
computeRowQuality4Compressed2WayUnnormalized();
*/
  for (int r = 0; r < numRow; r++){
    tempRowCL = rowCL[r];
    if (rowCS[tempRowCL] > 1 && !isRowMarked[r]){
      tempCluster = tempRowCL;
      minDelta2 = MY_DBL_MAX;

      if (isReversed[r]){

        for (int cc = 0; cc < numColCluster; cc++)
          compressedRow[cc] = Acompressed[tempRowCL][cc];
        myCRS->addRow(compressedRow, r, colCL);
        delta1 = rowQuality4Compressed[tempRowCL] - computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[tempRowCL]-1);
        for (int rc = 0; rc < tempRowCL; rc++){
	  for(int cc = 0; cc < numColCluster; cc++)
	    compressedRow[cc] = Acompressed[rc][cc];
	  myCRS->subtractRow(compressedRow, r, colCL);
	  delta2 = rowQuality4Compressed[rc] - computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1);
          updateVariable(minDelta2, tempCluster, delta2, rc);
        }
        for (int rc = tempRowCL+1; rc < numRowCluster; rc++){
	  for(int cc = 0; cc < numColCluster; cc++)
	    compressedRow[cc] = Acompressed[rc][cc];
	  myCRS->subtractRow(compressedRow, r, colCL);
	  delta2 = rowQuality4Compressed[rc] - computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1);
          updateVariable(minDelta2, tempCluster, delta2, rc);
        }
        if ((delta1 + minDelta2) < minDelta){
	  fromRow = r;
	  toCluster = tempCluster;
          minDelta = delta1 + minDelta2;
          tempIsReversed = true;
        }
 
      } else {				// (i.e., isReversed[r] == false)

        for (int cc = 0; cc < numColCluster; cc++)
          compressedRow[cc] = Acompressed[tempRowCL][cc];
        myCRS->subtractRow(compressedRow, r, colCL);
        delta1 = rowQuality4Compressed[tempRowCL] - computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[tempRowCL]-1);
        for (int rc = 0; rc < tempRowCL; rc++){
	  for(int cc = 0; cc < numColCluster; cc++)
	    compressedRow[cc] = Acompressed[rc][cc];
	  myCRS->addRow(compressedRow, r, colCL);
	  delta2 = rowQuality4Compressed[rc] - computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1);
          updateVariable(minDelta2, tempCluster, delta2, rc);
        }
        for (int rc = tempRowCL+1; rc < numRowCluster; rc++){
	  for(int cc = 0; cc < numColCluster; cc++)
	    compressedRow[cc] = Acompressed[rc][cc];
	  myCRS->addRow(compressedRow, r, colCL);
	  delta2 = rowQuality4Compressed[rc] - computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1);
          updateVariable(minDelta2, tempCluster, delta2, rc);
        }
        if ((delta1 + minDelta2) < minDelta){
	  fromRow = r;
	  toCluster = tempCluster;
          minDelta = delta1 + minDelta2;
          tempIsReversed = false;
        }
      }
      checkDumpLevel4DeltaTrace("row", r, tempCluster, delta1, minDelta2);
    }
  }
  isRowMarked[fromRow] = true;
  trace[step].id = fromRow;
  trace[step].fromCluster = rowCL[fromRow];
  trace[step].toCluster = toCluster;
  trace[step].change = minDelta;
  rowCS[rowCL[fromRow]]--;
  rowCS[toCluster]++;
  isReversed[fromRow] = tempIsReversed;

  if (tempIsReversed){
    myCRS->addRow(Acompressed, rowCL[fromRow], fromRow, colCL);
    rowQuality4Compressed[rowCL[fromRow]] = computeRowQuality4Compressed2WayUnnormalized(rowCL[fromRow]);
    myCRS->subtractRow(Acompressed, toCluster, fromRow, colCL);
    rowQuality4Compressed[toCluster] = computeRowQuality4Compressed2WayUnnormalized(toCluster);
  } else {
    myCRS->subtractRow(Acompressed, rowCL[fromRow], fromRow, colCL);
    rowQuality4Compressed[rowCL[fromRow]] = computeRowQuality4Compressed2WayUnnormalized(rowCL[fromRow]);
    myCRS->addRow(Acompressed, toCluster, fromRow, colCL);
    rowQuality4Compressed[toCluster] = computeRowQuality4Compressed2WayUnnormalized(toCluster);
  }
  
  rowCL[fromRow] = toCluster;
  delete [] compressedRow;
  checkDumpLevel4Cocluster(dumpFile);
}


void MssrIcc::doColLocalSearch(oneStep trace [], int step)
{
  int fromCol = 0, tempCluster, toCluster, tempColCL;
  double delta1, delta2, minDelta = MY_DBL_MAX, minDelta2 = MY_DBL_MAX;
  double *compressedCol = new double [numRowCluster];
  trace[step].id = 0;
  trace[step].fromCluster = colCL[0];
  trace[step].toCluster = toCluster = colCL[0];
  trace[step].change = 0;
/*
computeAcompressed();
isNormalizedCompressed = false;
computeColQuality4Compressed2WayUnnormalized();
*/
  for(int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    if (colCS[tempColCL] > 1  && !isColMarked[c]){
      tempCluster = tempColCL;
      minDelta2 = MY_DBL_MAX;
      for(int rc = 0; rc < numRowCluster; rc++)
	compressedCol[rc] = Acompressed[rc][tempColCL];
      myCCS->subtractCol(compressedCol, c, rowCL);
      delta1 = colQuality4Compressed[tempColCL] - computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[tempColCL]-1);
      for (int cc = 0; cc < tempColCL; cc++){
        for(int rc = 0; rc < numRowCluster; rc++)
          compressedCol[rc] = Acompressed[rc][cc];
        myCCS->addCol(compressedCol, c, rowCL);
        delta2 = colQuality4Compressed[cc] - computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[cc]+1);
        updateVariable(minDelta2, tempCluster, delta2, cc);
      }
      for (int cc = tempColCL+1; cc < numColCluster; cc++){
        for(int rc = 0; rc < numRowCluster; rc++)
          compressedCol[rc] = Acompressed[rc][cc];
        myCCS->addCol(compressedCol, c, rowCL);
        delta2 = colQuality4Compressed[cc] - computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[cc]+1);
        updateVariable(minDelta2, tempCluster, delta2, cc);
      }
      if ((delta1 + minDelta2) < minDelta){
	fromCol = c;
	toCluster = tempCluster;
        minDelta = delta1 + minDelta2;
      }
      checkDumpLevel4DeltaTrace("col", c, tempCluster, delta1, minDelta2);
    }
  }
  isColMarked[fromCol] = true;
  trace[step].id = fromCol;
  trace[step].fromCluster = colCL[fromCol];
  trace[step].toCluster = toCluster;
  trace[step].change = minDelta;
  colCS[colCL[fromCol]]--;
  colCS[toCluster]++;

  myCCS->subtractCol(Acompressed, colCL[fromCol], fromCol, rowCL);
  colQuality4Compressed[colCL[fromCol]] = computeColQuality4Compressed2WayUnnormalized(colCL[fromCol]);
  myCCS->addCol(Acompressed, toCluster, fromCol, rowCL);
  colQuality4Compressed[toCluster] = computeColQuality4Compressed2WayUnnormalized(toCluster);

  colCL[fromCol] = toCluster;
  delete [] compressedCol;
  checkDumpLevel4Cocluster(dumpFile);
}



void MssrIcc::doColLocalSearch(oneStep trace [], int step, bool *isReversed)
{
  int fromCol = 0, tempCluster, toCluster, tempColCL;
  double delta1, delta2, minDelta = MY_DBL_MAX, minDelta2 = MY_DBL_MAX;
  double *compressedCol = new double [numRowCluster];
  trace[step].id = 0;
  trace[step].fromCluster = colCL[0];
  trace[step].toCluster = toCluster = colCL[0];
  trace[step].change = 0;
/*
computeAcompressed(isReversed);
isNormalizedCompressed = false;
computeColQuality4Compressed2WayUnnormalized();
*/
  for(int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    if (colCS[tempColCL] > 1  && !isColMarked[c]){
      tempCluster = tempColCL;
      minDelta2 = MY_DBL_MAX;
      for(int rc = 0; rc < numRowCluster; rc++)
	compressedCol[rc] = Acompressed[rc][tempColCL];
      myCCS->subtractCol(compressedCol, c, rowCL, isReversed);
      delta1 = colQuality4Compressed[tempColCL] - computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[tempColCL]-1);
      for (int cc = 0; cc < tempColCL; cc++){
        for(int rc = 0; rc < numRowCluster; rc++)
          compressedCol[rc] = Acompressed[rc][cc];
        myCCS->addCol(compressedCol, c, rowCL, isReversed);
        delta2 = colQuality4Compressed[cc] - computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[cc]+1);
        updateVariable(minDelta2, tempCluster, delta2, cc);
      }
      for (int cc = tempColCL+1; cc < numColCluster; cc++){
        for(int rc = 0; rc < numRowCluster; rc++)
          compressedCol[rc] = Acompressed[rc][cc];
        myCCS->addCol(compressedCol, c, rowCL, isReversed);
        delta2 = colQuality4Compressed[cc] - computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[cc]+1);
        updateVariable(minDelta2, tempCluster, delta2, cc);
      }
      if ((delta1 + minDelta2) < minDelta){
	fromCol = c;
	toCluster = tempCluster;
        minDelta = delta1 + minDelta2;
      }
      checkDumpLevel4DeltaTrace("col", c, tempCluster, delta1, minDelta2);
    }
  }
  isColMarked[fromCol] = true;
  trace[step].id = fromCol;
  trace[step].fromCluster = colCL[fromCol];
  trace[step].toCluster = toCluster;
  trace[step].change = minDelta;
  colCS[colCL[fromCol]]--;
  colCS[toCluster]++;

  myCCS->subtractCol(Acompressed, colCL[fromCol], fromCol, rowCL, isReversed);
  colQuality4Compressed[colCL[fromCol]] = computeColQuality4Compressed2WayUnnormalized(colCL[fromCol]);
  myCCS->addCol(Acompressed, toCluster, fromCol, rowCL, isReversed);
  colQuality4Compressed[toCluster] = computeColQuality4Compressed2WayUnnormalized(toCluster);

  colCL[fromCol] = toCluster;
  delete [] compressedCol;
  checkDumpLevel4Cocluster(dumpFile);
}


bool MssrIcc::doRowLocalSearchChain()
{
  checkDumpLevel4LocalSearch("beginRow");
  bool isHelpful = false;
  int minIndex;
  double *totalChange = new double [rowLocalSearchLength], minChange;
  oneStep *trace = new oneStep[rowLocalSearchLength];

  for (int i = 0; i < rowLocalSearchLength; i++){
    if (isTakingReverse)
      doRowLocalSearch(trace, i, isReversed);
    else
      doRowLocalSearch(trace, i);
    checkDumpLevel4LocalSearch("row", trace[i].id, trace[i].fromCluster, trace[i].toCluster, trace[i].change);
  }
  totalChange[0] = trace[0].change;
  minChange = trace[0].change;
  minIndex = 0;
  for(int i = 1; i < rowLocalSearchLength; i++)
    totalChange[i] = totalChange[i-1] + trace[i].change;
  for(int i = 0; i < rowLocalSearchLength; i++)
//    if (totalChange[i] <= minChange){
    if (totalChange[i] < minChange){
      minChange = totalChange[i];
      minIndex = i;
    }
  if (totalChange[minIndex] > (rowLocalSearchThreshold * squaredFNormA)){
    checkDumpLevel4NumOfChain("row", 0, NULL);
    if (isTakingReverse)
      recoverRowCL(0, rowLocalSearchLength, trace, isReversed);
    else
      recoverRowCL(0, rowLocalSearchLength, trace);
    isHelpful = false;
  } else {		//   i.e. if(totalChange[minIndex] <= (rowLocalSearchThreshold * squaredFNormA))
    checkDumpLevel4NumOfChain("row", minIndex, totalChange);
    if (isTakingReverse)
      recoverRowCL(minIndex+1, rowLocalSearchLength, trace, isReversed);
    else
      recoverRowCL(minIndex+1, rowLocalSearchLength, trace);
    isHelpful = true;
  }
  delete [] totalChange;
  delete [] trace;
  checkDumpLevel4Cocluster(dumpFile);
  checkDumpLevel4LocalSearch("endRow");
  return isHelpful;
}

     
bool MssrIcc::doColLocalSearchChain()
{
  checkDumpLevel4LocalSearch("beginCol");
  bool isHelpful = false;
  int minIndex;
  double *totalChange = new double [colLocalSearchLength], minChange;
  oneStep *trace = new oneStep [colLocalSearchLength];

  for (int i = 0; i < colLocalSearchLength; i++){
    if (isTakingReverse)
      doColLocalSearch(trace, i, isReversed);
    else
      doColLocalSearch(trace, i);
    checkDumpLevel4LocalSearch("col", trace[i].id, trace[i].fromCluster, trace[i].toCluster, trace[i].change);
  }
  totalChange[0] = trace[0].change;
  minChange = trace[0].change;
  minIndex = 0;
  for(int i = 1; i < colLocalSearchLength; i++)
    totalChange[i] = totalChange[i-1] + trace[i].change;
  for(int i = 0; i < colLocalSearchLength; i++)
//    if (totalChange[i] <= minChange){
    if (totalChange[i] < minChange){
      minChange = totalChange[i];
      minIndex = i;
    }
  if (totalChange[minIndex] > (colLocalSearchThreshold * squaredFNormA)){
    checkDumpLevel4NumOfChain("col", 0, NULL);
    if (isTakingReverse)
      recoverColCL(0, colLocalSearchLength, trace, isReversed);
    else
      recoverColCL(0, colLocalSearchLength, trace);
    isHelpful = false;
  } else { 		//  i.e., if( totalChange[minIndex] <= (colLocalSearchThreshold * sqrt(squaredFNormA)))
    checkDumpLevel4NumOfChain("col", minIndex, totalChange);
    if (isTakingReverse)
      recoverColCL(minIndex+1, colLocalSearchLength, trace, isReversed);
    else
      recoverColCL(minIndex+1, colLocalSearchLength, trace);
    isHelpful = true;
  }
  delete [] totalChange;
  delete [] trace;
  checkDumpLevel4Cocluster(dumpFile);
  checkDumpLevel4LocalSearch("endCol");
  return isHelpful;
}


void MssrIcc::doPingPong()
{
  bool isRowLocalSearchHelpful = false, isColLocalSearchHelpful = false;
  numIteration = 0;
  do {
    isRowLocalSearchHelpful = false;
    isColLocalSearchHelpful = false;
    numIteration++;
    
    checkDumpLevel4PingPong("begin", numIteration);

    if (isTakingReverse){

      switch (batchUpdateType){
        case SINGLE_RESPECTIVELY:
          doBatchUpdate(isReversed);
          break;
        case SINGLE_IN_BATCH:
          doBatchUpdate4VariationI(isReversed);
          break;
        case MULTIPLE_RESPECTIVELY:
          doBatchUpdate4VariationII(isReversed);
          break;
        case SINGLE_BY_FLIP:
          doBatchUpdate4VariationIII(isReversed);
          break;
        case MULTIPLE_BY_FLIP:
          doBatchUpdate4VariationIV(isReversed);
          break;
	default:
	  break;
      }

      //-----------------------------------------
      // To avoid empty row cluster(s)...
      if (isAvoidingEmptyRowCluster){
        computeRowClusterSize();
//        rowLocalSearchLength = getEmptyRC();
        if (getEmptyRC() > 0)
          rowLocalSearchLength = DEFAULT_ROW_LOCAL_SEARCH_LENGTH;
        else
          rowLocalSearchLength = 0;
      }
      //-----------------------------------------       
      if (rowLocalSearchLength > 0){
        clearMark4Row();
        computeAcompressed(isReversed);
        isNormalizedCompressed = false;
        computeRowQuality4Compressed2WayUnnormalized();
        isRowLocalSearchHelpful = doRowLocalSearchChain();
      }
      //-----------------------------------------
      // To avoid empty col cluster(s)...
      if (isAvoidingEmptyColCluster){
        computeColClusterSize();
//        colLocalSearchLength = getEmptyCC();
        if (getEmptyCC() > 0)
          colLocalSearchLength = DEFAULT_COL_LOCAL_SEARCH_LENGTH;
        else
          colLocalSearchLength = 0;
      }
      //-----------------------------------------
      if (colLocalSearchLength > 0){
        clearMark4Col();
        computeAcompressed(isReversed);
        isNormalizedCompressed = false;
        computeColQuality4Compressed2WayUnnormalized();
        isColLocalSearchHelpful = doColLocalSearchChain();
      }

    } else {

      switch (batchUpdateType){
        case SINGLE_RESPECTIVELY:
          doBatchUpdate();
          break;
        case SINGLE_IN_BATCH:
          doBatchUpdate4VariationI();
          break;
        case MULTIPLE_RESPECTIVELY:
          doBatchUpdate4VariationII();
          break;
        case SINGLE_BY_FLIP:
          doBatchUpdate4VariationIII();
          break;
        case MULTIPLE_BY_FLIP:
          doBatchUpdate4VariationIV();
          break;
	default:
	  break;
      }

      //-----------------------------------------
      // To avoid empty row cluster(s)...
      if (isAvoidingEmptyRowCluster){
        computeRowClusterSize();
//        rowLocalSearchLength = getEmptyRC();
        if (getEmptyRC() > 0)
          rowLocalSearchLength = DEFAULT_ROW_LOCAL_SEARCH_LENGTH;
        else
          rowLocalSearchLength = 0;
      }
      //-----------------------------------------       
      if (rowLocalSearchLength > 0){
        clearMark4Row();
	computeAcompressed();
        isNormalizedCompressed = false;
        computeRowQuality4Compressed2WayUnnormalized();
        isRowLocalSearchHelpful = doRowLocalSearchChain();
      }
      //-----------------------------------------
      // To avoid empty col cluster(s)...
      if (isAvoidingEmptyColCluster){
        computeColClusterSize();
//        colLocalSearchLength = getEmptyCC();
        if (getEmptyCC() > 0)
          colLocalSearchLength = DEFAULT_COL_LOCAL_SEARCH_LENGTH;
        else
          colLocalSearchLength = 0;
      }
      //-----------------------------------------
      if (colLocalSearchLength > 0){
        clearMark4Col();
        computeAcompressed();
        isNormalizedCompressed = false;
        computeColQuality4Compressed2WayUnnormalized();
        isColLocalSearchHelpful = doColLocalSearchChain();
      }
    }
    checkDumpLevel4PingPong("end", numIteration);

  } while ((isRowLocalSearchHelpful || isColLocalSearchHelpful) && (numIteration <= DEFAULT_MAX_PINGPONG_ITERATION));

  if (isComputingOneWayObjective){
    computeRowCentroid();
    normalizeRowCentroid();
    computeObjectiveFunction4RowCluster();
    computeColCentroid();
    normalizeColCentroid();  
    computeObjectiveFunction4ColCluster();
  }
  checkDumpLevel4FinalObjectValue();
  switch (dumpLevel){
    case MINIMUM_DUMP_LEVEL:
      break;
    case BATCH_UPDATE_DUMP_LEVEL:
    case LOCAL_SEARCH_DUMP_LEVEL:
      checkDumpLevel4Coclustering(cout, numIteration, squaredFNormA);
    case MAXIMUM_DUMP_LEVEL:
      checkDumpLevel4Coclustering(dumpFile, numIteration, squaredFNormA);
  }
  if (statisticsAccessMode != NO_OPEN_MODE)
    checkDumpLevel4Coclustering(statisticsFile, numIteration, squaredFNormA);
//  cout << endl << "MssrIIcc::~MssrIIcc()" << endl;
}
