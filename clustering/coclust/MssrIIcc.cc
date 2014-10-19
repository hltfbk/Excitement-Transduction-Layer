/*
  MssrIIcc.cc
    Implementation of the coclustering algothm for the second problem, H = A - RR'A - ACC' + RR'ACC'
    in the paper, "Minimum Sum-Squared Residue Co-clustering of Gene Expression Data",
    with smoothing, local search, and variations of batch and local search update.
  cf. 	||A-Ahat||^2 	== ||A||^2 - ||R'A||^2 - ||AC||^2 + ||R'*A*C||^2 
                           where  (||R'A||^2)_rc = sum(sum(rowCentroid .* rowCentroid * rowCS[rc]))
                                  (||AC||^2)_cc = sum(sum(colCentroid .* colCentroid * colCS[cc]))
                                  (||R'AC||^2)_rc_cc = sum(sum(Acompressed_rc_cc .* Acompressed_rc_cc * rowCS[rc] * colCS[cc]))
                              or  (||R'A||^2)_rc = sum(sum(rowCentroid .* rowCentroid / rowCS[rc]))
                                  (||AC||^2)_cc = sum(sum(colCentroid .* colCentroid / colCS[cc]))
                                  (||R'AC||^2)_rc_cc = sum(sum((Acompressed_rc_cc .* Acompressed_rc_cc) / (rowCS[rc] * colCS[cc])))
  cf.   In batch update, we use a normalized compressed matrix.
        However, in local search (i.e., First Variation), we use an unnormalized compressed matrix 
	in order to directly add and subtract values. 
	"isNormalizedCompressed", "isNormalizedRowCentroid", and "isNormalizedColCentroid" are used for checking the normalization.	
 
    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#include <iostream>
#include <fstream>

#include "MatrixVector.h"
#include "MssrIIcc.h"


MssrIIcc::MssrIIcc(Matrix *inputMatrix_CCS, Matrix *inputMatrix_CRS, commandLineArgument myCLA): Coclustering(inputMatrix_CCS, inputMatrix_CRS, myCLA)
{
//  cout << endl << "MssrIIcc::MssrIIcc()" << endl;
  if (isTakingReverse){
    isReversed = new bool[numRow];
    for (int r = 0; r < numRow; r++)
      isReversed[r] = false;
    memoryUsed += numRow * sizeof(bool);
  }
  squaredFNormA = myCCS->squaredFNorm();		// Sum_ij (A_ij)^2
  rowQuality4Compressed = new double[numRowCluster];		// rowQuality4Compressed[i] = sum_j (Acompressed_ij)^2/(rowCS[i]*colCS[j])
  colQuality4Compressed = new double[numColCluster];		// colQuality4Compressed[j] = sum_i (Acompressed_ij)^2/(rowCS[i]*colCS[j])
  memoryUsed += (numColCluster + numRowCluster) * sizeof(double);
  isNormalizedCompressed = false;
  isNormalizedRowCentroid = false;
  isNormalizedColCentroid = false;
  rowCentroid = new double*[numRowCluster];
  for (int rc = 0; rc < numRowCluster; rc++)
    rowCentroid[rc] = new double[numCol];
  colCentroid = new double*[numColCluster];
  for (int cc = 0; cc < numColCluster; cc++)
    colCentroid[cc] = new double[numRow];
  memoryUsed += (numRowCluster * numCol + numRow * numColCluster) * sizeof(double);
  rowAR = NULL;
  colAC = NULL;
  rowAP = new double[numCol];
  colAP = new double[numRow];
  memoryUsed += (numCol + numRow) * sizeof(double);
  rowQuality4Centroid = new double[numRowCluster];
  colQuality4Centroid = new double[numColCluster];
  memoryUsed += (numRowCluster + numColCluster) * sizeof(double);
}

MssrIIcc::~MssrIIcc()
{
  if (isTakingReverse)
    delete [] isReversed;
  delete [] rowQuality4Compressed;
  delete [] colQuality4Compressed;
  for (int rc = 0; rc < numRowCluster; rc++)
    delete [] rowCentroid[rc];
  delete [] rowCentroid;
  for (int cc = 0; cc < numColCluster; cc++)
    delete [] colCentroid[cc];
  delete [] colCentroid;
  delete [] rowAP;
  delete [] colAP;  
  delete [] rowQuality4Centroid;
  delete [] colQuality4Centroid;
//  cout << endl << "MssrIIcc::~MssrIIcc()" << endl;
}

void MssrIIcc::doInitialization()
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
  normalizeCompressedMatrix();
  if (isTakingReverse)
    computeRowCentroid(isReversed);
  else
    computeRowCentroid();
  normalizeRowCentroid();
  if (isTakingReverse)
    computeColCentroid(isReversed);
  else
    computeColCentroid();
  normalizeColCentroid();
  computeObjectiveFunction4Normalized();
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


void MssrIIcc::computeRowCentroid()
{
  myCRS->computeRowCentroid(numRowCluster, rowCL, rowCentroid);
  isNormalizedRowCentroid = false;
}
 
 
void MssrIIcc::computeRowCentroid(bool *isReversed)
{
  myCRS->computeRowCentroid(numRowCluster, rowCL, rowCentroid, isReversed);
  isNormalizedRowCentroid = false;
}
 
 
void MssrIIcc::computeColCentroid()
{
  myCCS->computeColCentroid(numColCluster, colCL, colCentroid);
  isNormalizedColCentroid = false;
}


void MssrIIcc::computeColCentroid(bool *isReversed)
{
  myCCS->computeColCentroid(numColCluster, colCL, colCentroid, isReversed);
  isNormalizedColCentroid = false;
}
 
 
void MssrIIcc::normalizeRowCentroid()
{
//  assert(!isNormalizedRowCentroid);
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int c = 0; c < numCol; c++)
        if (rowCentroid[rc][c] != 0)
          rowCentroid[rc][c] /= rowCS[rc];
  isNormalizedRowCentroid = true;
}


void MssrIIcc::normalizeColCentroid()
{
//  assert(!isNormalizedColCentroid);
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int r = 0; r < numRow; r++)
        if (colCentroid[cc][r] != 0)
          colCentroid[cc][r] /= colCS[cc];
  isNormalizedColCentroid = true;
}


void MssrIIcc::normalizeCompressedMatrix()
{
//  assert(!isNormalizedCompressed);
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int cc = 0; cc < numColCluster; cc++)
        if (Acompressed[rc][cc] != 0 && colCS[cc] > 0)
          Acompressed[rc][cc] /= rowCS[rc] * colCS[cc];
  isNormalizedCompressed = true;
}


void MssrIIcc::computeRowAR()
{
//  assert(isNormalizedCompressed);
//  assert(isNormalizedRowCentroid);
  for (int rc = 0; rc < numRowCluster; rc++)
    for (int c = 0; c < numCol; c++)
      rowAR[rc][c] = rowCentroid[rc][c] - Acompressed[rc][colCL[c]];
}


void MssrIIcc::computeColAC()
{
//  assert(isNormalizedCompressed);
//  assert(isNormalizedColCentroid);
  for (int cc = 0; cc < numColCluster; cc++)
    for (int r = 0; r < numRow; r++)
      colAC[cc][r] = colCentroid[cc][r] - Acompressed[rowCL[r]][cc];
}


void MssrIIcc::computeQuality4RowAR()
{
  // rowAR contains mAR = mR'A - mR'ACC'.
  double tempValue = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    rowQuality4Compressed[rc] = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int c = 0; c < numCol; c++){
        tempValue = rowAR[rc][c];
        if (tempValue != 0)
          rowQuality4Compressed[rc] += tempValue * tempValue;
      }	
}


void MssrIIcc::computeQuality4ColAC()
{
  // colAC contains mAC = mAC - mRR'AC.
  double tempValue = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    colQuality4Compressed[cc] = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int r = 0; r < numRow; r++){
        tempValue = colAC[cc][r];
        if (tempValue != 0)
          colQuality4Compressed[cc] += tempValue * tempValue;
      }
}


double MssrIIcc::computeQuality4RowCentroidUnnormalized()
{
  double tempValue = 0, temp = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int c = 0; c < numCol; c++){
        tempValue = rowCentroid[rc][c];
        if (tempValue != 0)
          temp += (tempValue * tempValue) / rowCS[rc];
      }
  return temp;
}


double MssrIIcc::computeQuality4RowCentroidNormalized()
{
  double tempValue = 0, temp = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int c = 0; c < numCol; c++){
        tempValue = rowCentroid[rc][c];
        if (tempValue != 0)
	  temp += (tempValue * tempValue) * rowCS[rc];
      }
  return temp;
}


double MssrIIcc::computeQuality4ColCentroidUnnormalized()
{
  double tempValue = 0, temp = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int r = 0; r < numRow; r++){
        tempValue = colCentroid[cc][r];
        if (tempValue != 0)
          temp += (tempValue * tempValue) / colCS[cc];
      }
  return temp;
}


double MssrIIcc::computeQuality4ColCentroidNormalized()
{
  double tempValue = 0, temp = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int r = 0; r < numRow; r++){
        tempValue = colCentroid[cc][r];
        if (tempValue != 0)
          temp += (tempValue * tempValue) * colCS[cc];
      }
  return temp;
}


void MssrIIcc::computeRowQuality4CentroidUnnormalized()
{
  double tempValue = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    rowQuality4Centroid[rc] = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int c = 0; c < numCol; c++){
        tempValue = rowCentroid[rc][c];
        if (tempValue != 0)
          rowQuality4Centroid[rc] += (tempValue * tempValue) / rowCS[rc];
      }
}


void MssrIIcc::computeRowQuality4CentroidNormalized()
{
  double tempValue = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    rowQuality4Centroid[rc] = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    if (rowCS[rc] > 0)
      for (int c = 0; c < numCol; c++){
        tempValue = rowCentroid[rc][c];
        if (tempValue != 0)
          rowQuality4Centroid[rc] += (tempValue * tempValue) * rowCS[rc];
      }
}


double MssrIIcc::computeRowQuality4CentroidUnnormalized(int rc)
{
  double tempValue = 0, temp = 0;
  if (rowCS[rc] > 0){
    for (int c = 0; c < numCol; c++){
      tempValue = rowCentroid[rc][c];
      if (tempValue != 0)
        temp += (tempValue * tempValue) / rowCS[rc];
    }
  }
  return temp;
}


double MssrIIcc::computeRowQuality4CentroidNormalized(int rc)
{
  double tempValue = 0, temp = 0;
  if (rowCS[rc] > 0){
    for (int c = 0; c < numCol; c++){
      tempValue = rowCentroid[rc][c];
      if (tempValue != 0)
        temp += (tempValue * tempValue) * rowCS[rc];
    }
  }
  return temp;
}



double MssrIIcc::computeRowQuality4CentroidUnnormalized(double *row1Way, int rowClusterSize)
{
  double tempValue = 0, temp = 0;
  if (rowClusterSize > 0){
    for (int c = 0; c < numCol; c++){
      tempValue = row1Way[c];
      if (tempValue != 0)
        temp += (tempValue * tempValue) / rowClusterSize;
    }
  }
  return temp;
}


double MssrIIcc::computeRowQuality4CentroidNormalized(double *row1Way, int rowClusterSize)
{
  double tempValue = 0, temp = 0;
  if (rowClusterSize > 0){
    for (int c = 0; c < numCol; c++){
      tempValue = row1Way[c];
      if (tempValue != 0)
        temp += (tempValue * tempValue) * rowClusterSize;
    }
  }
  return temp;
}


void MssrIIcc::computeColQuality4CentroidUnnormalized()
{
  double tempValue = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    colQuality4Centroid[cc] = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int r = 0; r < numRow; r++){
        tempValue = colCentroid[cc][r];
        if (tempValue != 0)
          colQuality4Centroid[cc] += (tempValue * tempValue) / colCS[cc];
      }
}


void MssrIIcc::computeColQuality4CentroidNormalized()
{
  double tempValue = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    colQuality4Centroid[cc] = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    if (colCS[cc] > 0)
      for (int r = 0; r < numRow; r++){
        tempValue = colCentroid[cc][r];
        if (tempValue != 0)
          colQuality4Centroid[cc] += (tempValue * tempValue) * colCS[cc];
      }
}


double MssrIIcc::computeColQuality4CentroidUnnormalized(int cc)
{
  double tempValue = 0, temp = 0;
  if (colCS[cc] > 0){
    for (int r = 0; r < numRow; r++){
      tempValue = colCentroid[cc][r];
      if (tempValue != 0)
        temp += (tempValue * tempValue) / colCS[cc];
    }
  }
  return temp;
}


double MssrIIcc::computeColQuality4CentroidNormalized(int cc)
{
  double tempValue = 0, temp = 0;
  if (colCS[cc] > 0){
    for (int r = 0; r < numRow; r++){
      tempValue = colCentroid[cc][r];
      if (tempValue != 0)
        temp += (tempValue * tempValue) * colCS[cc];
    }
  }
  return temp;
}


double MssrIIcc::computeColQuality4CentroidUnnormalized(double *col1Way, int colClusterSize)
{
  double tempValue = 0, temp = 0;
  if (colClusterSize > 0){
    for (int r = 0; r < numRow; r++){
      tempValue = col1Way[r];
      if (tempValue != 0)
        temp += (tempValue * tempValue) / colClusterSize;
    }
  }
  return temp;
}


double MssrIIcc::computeColQuality4CentroidNormalized(double *col1Way, int colClusterSize)
{
  double tempValue = 0, temp = 0;
  if (colClusterSize > 0){
    for (int r = 0; r < numRow; r++){
      tempValue = col1Way[r];
      if (tempValue != 0)
        temp += (tempValue * tempValue) * colClusterSize;
    }
  }
  return temp;
}


void MssrIIcc::computeRowAP(int r)
{
//  assert(isNormalizedColCentroid);
  myCRS->computeRowAP(r, colCentroid, colCL, rowAP);
}

void MssrIIcc::computeRowAP(int r, bool *isReversed)
{
//  assert(isNormalizedColCentroid);
  myCRS->computeRowAP(r, colCentroid, colCL, rowAP, isReversed);
}


void MssrIIcc::computeColAP(int c)
{
//  assert(isNormalizedRowCentroid);
  myCCS->computeColAP(c, rowCentroid, rowCL, colAP);
}


void MssrIIcc::computeColAP(int c, bool *isReversed)
{
//  assert(isNormalizedRowCentroid);
  myCCS->computeColAP(c, rowCentroid, rowCL, colAP, isReversed);
}


void MssrIIcc::computeObjectiveFunction4Unnormalized()
{
  checkDumpLevel4Cocluster(dumpFile);
  objValue = squaredFNormA - computeQuality4RowCentroidUnnormalized() - computeQuality4ColCentroidUnnormalized() + computeQuality4CompressedUnnormalized();
}


void MssrIIcc::computeObjectiveFunction4Normalized()
{
  checkDumpLevel4Cocluster(dumpFile);
  objValue = squaredFNormA - computeQuality4RowCentroidNormalized() - computeQuality4ColCentroidNormalized() + computeQuality4CompressedNormalized();
}


void MssrIIcc::computeObjectiveFunction4Normalized(double **Acompressed)
{
  checkDumpLevel4Cocluster(dumpFile);
  objValue = myCCS->computeObjectiveFunctionValue(rowCL, colCL, Acompressed, rowCentroid, colCentroid);
}


void MssrIIcc::computeObjectiveFunction4Normalized(double **Acompressed, bool *isReversed)
{
  checkDumpLevel4Cocluster(dumpFile);
  objValue = myCCS->computeObjectiveFunctionValue(rowCL, colCL, Acompressed, rowCentroid, colCentroid, isReversed);
}


void MssrIIcc::computeObjectiveFunction4RowCluster()
{
  // It should be implemented... Currently it's left as it is. ==> Done...
  objValue4RowCluster = myCRS->computeObjectiveFunctionValue4RowCluster(rowCL, rowCentroid);
}


void MssrIIcc::computeObjectiveFunction4ColCluster()
{
  objValue4ColCluster = myCCS->computeObjectiveFunctionValue4ColCluster(colCL, colCentroid);
}


void MssrIIcc::computeRowQuality4Compressed2WayUnnormalized()
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


void MssrIIcc::computeRowQuality4Compressed2WayNormalized()
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


double MssrIIcc::computeRowQuality4Compressed2WayUnnormalized(int rc)
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


double MssrIIcc::computeRowQuality4Compressed2WayNormalized(int rc)
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


double MssrIIcc::computeRowQuality4Compressed2WayUnnormalized(double *row2Way, int rowClusterSize)
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


double MssrIIcc::computeRowQuality4Compressed2WayNormalized(double *row2Way, int rowClusterSize)
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


void MssrIIcc::computeColQuality4Compressed2WayUnnormalized()
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


void MssrIIcc::computeColQuality4Compressed2WayNormalized()
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


double MssrIIcc::computeColQuality4Compressed2WayUnnormalized(int cc)
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


double MssrIIcc::computeColQuality4Compressed2WayNormalized(int cc)
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


double MssrIIcc::computeColQuality4Compressed2WayUnnormalized(double *col2Way, int colClusterSize)
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


double MssrIIcc::computeColQuality4Compressed2WayNormalized(double *col2Way, int colClusterSize)
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


double MssrIIcc::computeQuality4CompressedUnnormalized()
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


double MssrIIcc::computeQuality4CompressedNormalized()
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

void MssrIIcc::doRowFarthestInitialization()
{

}

void MssrIIcc::doColFarthestInitialization()
{

}

void MssrIIcc::doRowRandomPerturbInitialization()
{
}

void MssrIIcc::doColRandomPerturbInitialization()
{
}

double MssrIIcc::rowDistance(int r, int rc)
{
  // rowAR contains mR'A - mR'ACC'
//  assert(isNormalizedCompressed);
//  assert(isNormalizedRowCentroid);
//  assert(isNormalizedColCentroid);
  double temp = 0;
  for (int c = 0; c < numCol; c++)
    temp += rowAP[c] * rowAR[rc][c];
  return (-2 * temp + rowQuality4Compressed[rc]);
}


double MssrIIcc::colDistance(int c, int cc)
{
  // colAC contains nAC - nRR'AC.
//  assert(isNormalizedCompressed);
//  assert(isNormalizedRowCentroid);
//  assert(isNormalizedColCentroid);
  double temp = 0;
  for (int r = 0; r < numRow; r++)
    temp += colAP[r] * colAC[cc][r];
  return (-2 * temp + colQuality4Compressed[cc]);
}


void MssrIIcc::reassignRC()
{
  int rowClusterChange = 0;
  int tempRowCL, minCL;
  double tempDistance, minDistance;
  if (rowAR == NULL){
    rowAR = new double*[numRowCluster];
    for (int rc = 0; rc < numRowCluster; rc++)
      rowAR[rc] = new double[numCol];
  }
  computeRowCentroid();			// rowCentroid, in R^(numRowCluster * numCol)
  normalizeRowCentroid();
  computeColCentroid();			// colCentroid, in R^(numRow * numColCluster)
  normalizeColCentroid();
  computeRowAR();			// rowAR = mA^R = mR'A - mR'ACC', in R^(numRowCluster * numCol)
  computeQuality4RowAR();		// rowQuality4Compressed[r] = sum_j(mA^R_rj)^2 = sum_j(m_r^(-1/2) * A^R_rj)^2 

  for (int r = 0; r < numRow; r++){
    tempRowCL = rowCL[r];
    minCL = tempRowCL;
    computeRowAP(r);			// rowAP_i = (A - ACC')_i, in R^(1*numCol)
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
  if (rowAR != NULL){
    for (int rc = 0; rc < numRowCluster; rc++)
      delete [] rowAR[rc];
    delete [] rowAR;
    rowAR = NULL;
  }
  checkDumpLevel4NumOfChange("row(s)", rowClusterChange);
}

	
void MssrIIcc::reassignRC(bool *isReversed)
{
  bool tempIsReversed;
  int rowClusterChange = 0;
  int tempRowCL, minCL;
  double tempDistance, minDistance;

  if (rowAR == NULL){
    rowAR = new double*[numRowCluster];
    for (int rc = 0; rc < numRowCluster; rc++)
      rowAR[rc] = new double[numCol];
  }
  computeRowCentroid(isReversed);	// rowCentroid, in R^(numRowCluster * numCol)
  normalizeRowCentroid();
  computeColCentroid(isReversed);	// colCentroid, in R^(numRow * numColCluster)
  normalizeColCentroid();
  computeRowAR();			// rowAR = mA^R = mR'A - mR'ACC', in R^(numRowCluster * numCol)
  computeQuality4RowAR();		// rowQuality4Compressed[r] = sum_j(mA^R_rj)^2 = sum_j(m_r^(-1/2) * A^R_rj)^2 

  for (int r = 0; r < numRow; r++){
    tempRowCL = rowCL[r];
    minCL = tempRowCL;

    if (isReversed[r]){

      tempIsReversed = true;
      computeRowAP(r, isReversed);	// rowAP_i = (A - ACC')_i, in R^(1*numCol)
      minDistance = MY_DBL_MAX;
      for (int rc = 0; rc < numRowCluster; rc++)
        if (rowCS[rc] > 0){
          tempDistance = rowDistance(r, rc); 
	  updateVariable(minDistance, minCL, tempDistance, rc);
        }
      isReversed[r] = false; 
      computeRowAP(r);			// rowAP_i = (A - ACC')_i, in R^(1*numCol)
      for (int rc = 0; rc < numRowCluster; rc++)
        if (rowCS[rc] > 0){
          tempDistance = rowDistance(r, rc); 
	  updateVariable(minDistance, minCL, tempIsReversed, tempDistance, rc, false);
        }

    } else {				// (i.e., isReversed[r] == false)

      tempIsReversed = false;
      computeRowAP(r);			// rowAP_i = (A - ACC')_i, in R^(1*numCol)
      minDistance = MY_DBL_MAX;
      for (int rc = 0; rc < numRowCluster; rc++)
        if (rowCS[rc] > 0){
          tempDistance = rowDistance(r, rc); 
          updateVariable(minDistance, minCL, tempDistance, rc);
        }
      isReversed[r] = true;
      computeRowAP(r, isReversed);	// rowAP_i = (A - ACC')_i, in R^(1*numCol)
      for (int rc = 0; rc < numRowCluster; rc++)
        if (rowCS[rc] > 0){
	  tempDistance = rowDistance(r, rc);
          updateVariable(minDistance, minCL, tempIsReversed, tempDistance, rc, true);
        }

    }
    if (minCL != tempRowCL)
      rowClusterChange++;
    rowCL[r] = minCL;
    isReversed[r] = tempIsReversed;
  }
  if (rowAR != NULL){
    for (int rc = 0; rc < numRowCluster; rc++)
      delete [] rowAR[rc];
    delete [] rowAR;
    rowAR = NULL;
  }
  checkDumpLevel4NumOfChange("row(s)", rowClusterChange);
  checkDumpLevel4ReversedRow();
/*

cout << "I am here..." << endl;
computeObjectiveFunction4Normalized(Acompressed, isReversed);		// not working!!!
checkDumpLevel4BatchUpdate("row", numIteration);
*/
}


void MssrIIcc::reassignCC()
{
  int colClusterChange = 0;
  int tempColCL, minCL;
  double tempDistance, minDistance;
  if (colAC == NULL){
    colAC = new double*[numColCluster];
    for (int cc = 0; cc < numColCluster; cc++)
      colAC[cc] = new double[numRow];
  }
  computeRowCentroid();			// rowCentroid, in R^(numRowCluster * numCol)
  normalizeRowCentroid();
  computeColCentroid();			// colCentroid, in R^(numRow * numColCluster)
  normalizeColCentroid();
  computeColAC();			// colAC = nA^C = nAC - nRR'AC, in R^(numRow * numColCluster)
  computeQuality4ColAC();		// colQuality4Compressed[c] = sum_i(nA^C_ic)^2 = sum_i(n_c^(-1/2) * A^C_ic)^2

  for (int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    minCL = tempColCL;
    computeColAP(c);			// colAP_j = (A - RR'A)_j, in R^(1*numRow)
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
  if (colAC != NULL){
    for (int cc = 0; cc < numColCluster; cc++)
      delete [] colAC[cc];
    delete [] colAC;
    colAC = NULL;
  }
  checkDumpLevel4NumOfChange("col(s)", colClusterChange);
}
  

void MssrIIcc::reassignCC4Variation()
{
  int colClusterChange = 0;
  int tempColCL, minCL;
  double tempDistance, minDistance;
  if (colAC == NULL){
    colAC = new double*[numColCluster];
    for (int cc = 0; cc < numColCluster; cc++)
      colAC[cc] = new double[numRow];
  }
// The following two lines are commented out, to make use of the previous rowCentroid!!!
//  computeRowCentroid();			// rowCentroid, in R^(numRowCluster * numCol)
//  normalizeRowCentroid();
  computeColCentroid();			// colCentroid, in R^(numRow * numColCluster)
  normalizeColCentroid();
  computeColAC();			// colAC = nA^C = nAC - nRR'AC, in R^(numRow * numColCluster)
  computeQuality4ColAC();		// colQuality4Compressed[c] = sum_i(nA^C_ic)^2 = sum_i(n_c^(-1/2) * A^C_ic)^2

  for (int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    minCL = tempColCL;
    computeColAP(c);			// colAP_j = (A - RR'A)_j, in R^(1*numRow)
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
  if (colAC != NULL){
    for (int cc = 0; cc < numColCluster; cc++)
      delete [] colAC[cc];
    delete [] colAC;
    colAC = NULL;
  }
  checkDumpLevel4NumOfChange("col(s)", colClusterChange);
}


void MssrIIcc::reassignCC(bool *isReversed)
{
  int colClusterChange = 0;
  int tempColCL, minCL;
  double tempDistance, minDistance;
  if (colAC == NULL){
    colAC = new double*[numColCluster];
    for (int cc = 0; cc < numColCluster; cc++)
      colAC[cc] = new double[numRow];
  }
  computeRowCentroid(isReversed);	// rowCentroid, in R^(numRowCluster * numCol)
  normalizeRowCentroid();
  computeColCentroid(isReversed);	// colCentroid, in R^(numRow * numColCluster)
  normalizeColCentroid();
  computeColAC();			// colAC = nA^C = nAC - nRR'AC, in R^(numRow * numColCluster)
  computeQuality4ColAC();		// colQuality4Compressed[c] = sum_i(nA^C_ic)^2 = sum_i(n_c^(-1/2) * A^C_ic)^2

  for (int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    minCL = tempColCL;
    computeColAP(c, isReversed);	// colAP_j = (A - RR'A)_j, in R^(1*numRow)
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
  if (colAC != NULL){
    for (int cc = 0; cc < numColCluster; cc++)
      delete [] colAC[cc];
    delete [] colAC;
    colAC = NULL;
  }
  checkDumpLevel4NumOfChange("col(s)", colClusterChange);
}


void MssrIIcc::reassignCC4Variation(bool *isReversed)
{
  int colClusterChange = 0;
  int tempColCL, minCL;
  double tempDistance, minDistance;
  if (colAC == NULL){
    colAC = new double*[numColCluster];
    for (int cc = 0; cc < numColCluster; cc++)
      colAC[cc] = new double[numRow];
  }
//  computeRowCentroid(isReversed);	// rowCentroid, in R^(numRowCluster * numCol)
//  normalizeRowCentroid();
  computeColCentroid(isReversed);	// colCentroid, in R^(numRow * numColCluster)
  normalizeColCentroid();
  computeColAC();			// colAC = nA^C = nAC - nRR'AC, in R^(numRow * numColCluster)
  computeQuality4ColAC();		// colQuality4Compressed[c] = sum_i(nA^C_ic)^2 = sum_i(n_c^(-1/2) * A^C_ic)^2

  for (int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    minCL = tempColCL;
    computeColAP(c, isReversed);	// colAP_j = (A - RR'A)_j, in R^(1*numRow)
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
  if (colAC != NULL){
    for (int cc = 0; cc < numColCluster; cc++)
      delete [] colAC[cc];
    delete [] colAC;
    colAC = NULL;
  }
  checkDumpLevel4NumOfChange("col(s)", colClusterChange);
}


void MssrIIcc::doBatchUpdate()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed();
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  computeRowCentroid();
  normalizeRowCentroid();
  computeColCentroid();
  normalizeColCentroid();

  do {
    numIteration++;

    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;
    
    if (numRowCluster < numRow){
      reassignRC();
      computeRowClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeRowCentroid();
      normalizeRowCentroid();
      computeObjectiveFunction4Normalized();

      checkDumpLevel4BatchUpdate("row", numIteration);
    }
    if (numColCluster < numCol){
      reassignCC();
      computeColClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeColCentroid();
      normalizeColCentroid();
      computeObjectiveFunction4Normalized();

      checkDumpLevel4BatchUpdate("col", numIteration);
    }
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIIcc::doBatchUpdate4VariationI()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed();
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  computeRowCentroid();
  normalizeRowCentroid();
  computeColCentroid();
  normalizeColCentroid();

  do {
    numIteration++;

    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;
    reassignRC();
    computeRowClusterSize();
//    computeAcompressed();
//    isNormalizedCompressed = false;
//    normalizeCompressedMatrix();
//    computeRowCentroid();
//    normalizeRowCentroid();
    computeObjectiveFunction4Normalized(Acompressed);
    checkDumpLevel4BatchUpdate("row", numIteration);
//------------
    reassignCC4Variation();
    computeColClusterSize();
//    computeAcompressed();
//    isNormalizedCompressed = false;
//    normalizeCompressedMatrix();
//    computeColCentroid();
//    normalizeColCentroid();
    computeObjectiveFunction4Normalized(Acompressed);
    checkDumpLevel4BatchUpdate("col", numIteration);
//------------
    computeAcompressed();
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeRowCentroid();
    normalizeRowCentroid();
    computeColCentroid();
    normalizeColCentroid();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("both", numIteration);
//------------
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIIcc::doBatchUpdate4VariationII()
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
  computeRowCentroid();
  normalizeRowCentroid();
  computeColCentroid();
  normalizeColCentroid();

  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
    oldObjValue = objValue;
    reassignRC();
    computeRowClusterSize();
    computeAcompressed();
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeRowCentroid();
    normalizeRowCentroid();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("row", numIteration);
    tempRowSmoothingFactor *= myCRS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
//------------
  numIteration = 0;
  tempRowSmoothingFactor = myCRS->getSmoothingFactor();
  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  do {
    numIteration++;
    myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
    oldObjValue = objValue;
    reassignCC();
    computeColClusterSize();
    computeAcompressed();
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeColCentroid();
    normalizeColCentroid();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("col", numIteration);
    tempColSmoothingFactor *= myCCS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (colBatchUpdateThreshold * squaredFNormA));
  colSmoothingFactor = tempColSmoothingFactor;
  checkDumpLevel4BatchUpdate("end");
}

/*
// It doesn't guarantee the monotonic decrease of objective function values.
void MssrIIcc::doBatchUpdate4VariationIII()
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
  computeRowCentroid();
  normalizeRowCentroid();
  computeColCentroid();
  normalizeColCentroid();
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
//-----------
      reassignRC();
      computeRowClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeRowCentroid();
      normalizeRowCentroid();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("row", numRowIteration);
      tempRowSmoothingFactor *= myCRS->getAnnealingFactor();
    } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
//------------
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
//      reassignCC4Variation();			// Both are ok because Acompressed is based on old rowCL.
      reassignCC();
      computeColClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeColCentroid();
      normalizeColCentroid();
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
  colSmoothingFactor = tempColSmoothingFactor;
  delete [] beforeRowCL;
  delete [] afterRowCL;
  checkDumpLevel4BatchUpdate("end");
}
*/


void MssrIIcc::doBatchUpdate4VariationIII()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed();
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  computeRowCentroid();
  normalizeRowCentroid();
  computeColCentroid();
  normalizeColCentroid();

  do {
    numIteration++;

    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;
    
    if (randNumGenerator.GetUniform() > SELECTION_PROBABILITY){
      reassignRC();
      computeRowClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeRowCentroid();
      normalizeRowCentroid();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("row", numIteration);
    } else {
      reassignCC();
      computeColClusterSize();
      computeAcompressed();
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeColCentroid();
      normalizeColCentroid();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("col", numIteration);
    }
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIIcc::doBatchUpdate4VariationIV()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed();
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  computeRowCentroid();
  normalizeRowCentroid();
  computeColCentroid();
  normalizeColCentroid();

  myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
  myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
  do {
    numIteration++;

    if (randNumGenerator.GetUniform() > SELECTION_PROBABILITY){
      myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
      do {
        oldObjValue = objValue;
        reassignRC();
        computeRowClusterSize();
        computeAcompressed();
        isNormalizedCompressed = false;
        normalizeCompressedMatrix();
        computeRowCentroid();
        normalizeRowCentroid();
        computeObjectiveFunction4Normalized();
        checkDumpLevel4BatchUpdate("row", numIteration);
      } while ((oldObjValue - objValue) > (MULTIPLE_FACTOR * rowBatchUpdateThreshold * squaredFNormA));
      rowSmoothingFactor *= myCRS->getAnnealingFactor();
    } else {
      myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
      do {
        oldObjValue = objValue;
        reassignCC();
        computeColClusterSize();
        computeAcompressed();
        isNormalizedCompressed = false;
        normalizeCompressedMatrix();
        computeColCentroid();
        normalizeColCentroid();
        computeObjectiveFunction4Normalized();
        checkDumpLevel4BatchUpdate("col", numIteration);
      } while ((oldObjValue - objValue) > (MULTIPLE_FACTOR * colBatchUpdateThreshold * squaredFNormA));
      colSmoothingFactor *= myCCS->getAnnealingFactor();
    }

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIIcc::doBatchUpdate(bool *isReversed)
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed(isReversed);
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  computeRowCentroid(isReversed);
  normalizeRowCentroid();
  computeColCentroid(isReversed);
  normalizeColCentroid();

  do {
    numIteration++;

    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;

    if (numRowCluster < numRow){
      reassignRC(isReversed);
      computeRowClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeRowCentroid(isReversed);
      normalizeRowCentroid();
      computeColCentroid(isReversed);
      normalizeColCentroid();
      computeObjectiveFunction4Normalized();

      checkDumpLevel4BatchUpdate("row", numIteration);
    }
    
    if (numColCluster < numCol){
      reassignCC(isReversed);
      computeColClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeRowCentroid(isReversed);
      normalizeRowCentroid();
      computeColCentroid(isReversed);
      normalizeColCentroid();
      computeObjectiveFunction4Normalized();

      checkDumpLevel4BatchUpdate("col", numIteration);
    }
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIIcc::doBatchUpdate4VariationI(bool *isReversed)
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed(isReversed);
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  computeRowCentroid(isReversed);
  normalizeRowCentroid();
  computeColCentroid(isReversed);
  normalizeColCentroid();

  do {
    numIteration++;

    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;
    reassignRC(isReversed);
    computeRowClusterSize();
						// Without this comment, it works...
    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeRowCentroid(isReversed);
    normalizeRowCentroid();
    computeColCentroid(isReversed);
    normalizeColCentroid();

//    computeObjectiveFunction4Normalized();				// not working!!!
//    checkDumpLevel4BatchUpdate("row", numIteration);
//    computeObjectiveFunction4Normalized(Acompressed);				// not working!!!
//    checkDumpLevel4BatchUpdate("row", numIteration);
    computeObjectiveFunction4Normalized(Acompressed, isReversed);		// not working!!!
    checkDumpLevel4BatchUpdate("row", numIteration);
    
    reassignCC4Variation(isReversed);
//    reassignCC(isReversed);
    computeColClusterSize();
/*
    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeRowCentroid(isReversed);
    normalizeRowCentroid();
    computeColCentroid(isReversed);
    normalizeColCentroid();
*/
//    computeObjectiveFunction4Normalized();			// not working!!!
    computeObjectiveFunction4Normalized(Acompressed, isReversed);
    checkDumpLevel4BatchUpdate("col", numIteration);

    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeRowCentroid(isReversed);
    normalizeRowCentroid();
    computeColCentroid(isReversed);
    normalizeColCentroid();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("both", numIteration);

    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIIcc::doBatchUpdate4VariationII(bool *isReversed)
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
  computeRowCentroid(isReversed);
  normalizeRowCentroid();
  computeColCentroid(isReversed);
  normalizeColCentroid();

  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
    oldObjValue = objValue;
    reassignRC(isReversed);
    computeRowClusterSize();
    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeRowCentroid(isReversed);
    normalizeRowCentroid();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("row", numIteration);
    tempRowSmoothingFactor *= myCRS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
//------------
  numIteration = 0;
  tempRowSmoothingFactor = rowSmoothingFactor;
  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  do {
    numIteration++;
    myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
    oldObjValue = objValue;
    reassignCC(isReversed);
    computeColClusterSize();
    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
    computeColCentroid(isReversed);
    normalizeColCentroid();
    computeObjectiveFunction4Normalized();
    checkDumpLevel4BatchUpdate("col", numIteration);
    tempColSmoothingFactor *= myCCS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (colBatchUpdateThreshold * squaredFNormA));
  colSmoothingFactor = tempColSmoothingFactor;
  checkDumpLevel4BatchUpdate("end");
}

/*
// It doesn't guarantee the monotonic decrease of objective function values.
void MssrIIcc::doBatchUpdate4VariationIII(bool *isReversed)
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
  computeRowCentroid(isReversed);
  normalizeRowCentroid();
  computeColCentroid(isReversed);
  normalizeColCentroid();
 
  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
  do{
    numIteration++;
    for (int r = 0; r < numRow; r++){
      beforeIsReversed[r] = isReversed[r];
      beforeRowCL[r] = rowCL[r];
    }
    do {
      numRowIteration++;
      myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
      oldObjValue = objValue;
      reassignRC(isReversed);
      computeRowClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeRowCentroid(isReversed);
      normalizeRowCentroid();
      computeColCentroid(isReversed);
      normalizeColCentroid();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("row", numRowIteration);
      tempRowSmoothingFactor *= myCRS->getAnnealingFactor();
    } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));
//------------

//    for (int r = 0; r < numRow; r++){
//      afterIsReversed[r] = isReversed[r];
//      isReversed[r] = beforeIsReversed[r];
//      afterRowCL[r] = rowCL[r];
//      rowCL[r] = beforeRowCL[r];
//    }
//    computeRowClusterSize();
//    computeAcompressed(isReversed);
//    isNormalizedCompressed = false;
//    normalizeCompressedMatrix();
//    computeRowCentroid(isReversed);
//    normalizeRowCentroid();
//    computeColCentroid(isReversed);
//    normalizeColCentroid();
//    for (int r = 0; r < numRow; r++){
//      isReversed[r] = afterIsReversed[r];
//      rowCL[r] = afterRowCL[r];
//    }
//    computeRowClusterSize();

    numColIteration = 0;
    tempRowSmoothingFactor = rowSmoothingFactor;
    myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
//-----------
    do {
      numColIteration++;
      myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
      oldObjValue = objValue;
      reassignCC(isReversed);
      computeColClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeColCentroid(isReversed);
      normalizeColCentroid();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("col", numColIteration);
      tempColSmoothingFactor *= myCCS->getAnnealingFactor();
    } while ((oldObjValue - objValue) > (colBatchUpdateThreshold * squaredFNormA));
//-----------
    oldObjValue = objValue;
    computeRowClusterSize();
    computeAcompressed(isReversed);
    isNormalizedCompressed = false;
    normalizeCompressedMatrix();
//    computeRowCentroid(isReversed);
//    normalizeRowCentroid();
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


void MssrIIcc::doBatchUpdate4VariationIII(bool *isReversed)
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed(isReversed);
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  computeRowCentroid(isReversed);
  normalizeRowCentroid();
  computeColCentroid(isReversed);
  normalizeColCentroid();

  do {
    numIteration++;

    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;
    
    if (randNumGenerator.GetUniform() > SELECTION_PROBABILITY){
      reassignRC(isReversed);
      computeRowClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeRowCentroid(isReversed);
      normalizeRowCentroid();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("row", numIteration);
    } else {
      reassignCC(isReversed);
      computeColClusterSize();
      computeAcompressed(isReversed);
      isNormalizedCompressed = false;
      normalizeCompressedMatrix();
      computeColCentroid(isReversed);
      normalizeColCentroid();
      computeObjectiveFunction4Normalized();
      checkDumpLevel4BatchUpdate("col", numIteration);
    }
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIIcc::doBatchUpdate4VariationIV(bool *isReversed)
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  computeRowClusterSize();
  computeColClusterSize();
  computeAcompressed(isReversed);
  isNormalizedCompressed = false;
  normalizeCompressedMatrix();
  computeRowCentroid(isReversed);
  normalizeRowCentroid();
  computeColCentroid(isReversed);
  normalizeColCentroid();

  do {
    numIteration++;

    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    
    if (randNumGenerator.GetUniform() > SELECTION_PROBABILITY){
      do {
        oldObjValue = objValue;
        reassignRC(isReversed);
        computeRowClusterSize();
        computeAcompressed(isReversed);
        isNormalizedCompressed = false;
        normalizeCompressedMatrix();
        computeRowCentroid(isReversed);
        normalizeRowCentroid();
        computeObjectiveFunction4Normalized();
        checkDumpLevel4BatchUpdate("row", numIteration);
      } while ((oldObjValue - objValue) > (MULTIPLE_FACTOR * rowBatchUpdateThreshold * squaredFNormA));
    } else {
      do {
        oldObjValue = objValue;
        reassignCC(isReversed);
        computeColClusterSize();
        computeAcompressed(isReversed);
        isNormalizedCompressed = false;
        normalizeCompressedMatrix();
        computeColCentroid(isReversed);
        normalizeColCentroid();
        computeObjectiveFunction4Normalized();
        checkDumpLevel4BatchUpdate("col", numIteration);
      } while ((oldObjValue - objValue) > (MULTIPLE_FACTOR * colBatchUpdateThreshold * squaredFNormA));
    }
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * squaredFNormA));

  checkDumpLevel4BatchUpdate("end");
}


void MssrIIcc::recoverRowCL(int begin, int end, oneStep trace [])
{
  for(int r = begin; r < end; r++)
    if (trace[r].toCluster != trace[r].fromCluster) {
//      myCRS->subtractRow(Acompressed, trace[r].toCluster, trace[r].id, colCL);
      rowCS[trace[r].toCluster]--;
//      myCRS->addRow(Acompressed, trace[r].fromCluster, trace[r].id, colCL);
      rowCS[trace[r].fromCluster]++;
      rowCL[trace[r].id] = trace[r].fromCluster;
    }
}


void MssrIIcc::recoverRowCL(int begin, int end, oneStep trace [], bool *isReversed)
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


void MssrIIcc::recoverColCL(int begin, int end, oneStep trace [])
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


void MssrIIcc::recoverColCL(int begin, int end, oneStep trace [], bool *isReversed)
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


void MssrIIcc::doRowLocalSearch(oneStep trace [], int step)
{
  int fromRow = 0, tempCluster, toCluster, tempRowCL;
  double delta1, delta2, minDelta = MY_DBL_MAX, minDelta2;
  double *centroidRow = new double [numCol], *compressedRow = new double [numColCluster];
  trace[step].id = 0;
  trace[step].fromCluster = rowCL[0];
  trace[step].toCluster = toCluster = rowCL[0];
  trace[step].change = 0;

  for (int r = 0; r < numRow; r++){
    tempRowCL = rowCL[r];
    if (rowCS[tempRowCL] > 1 && !isRowMarked[r]){
     tempCluster = tempRowCL;
     minDelta2 = MY_DBL_MAX;
     for (int c = 0; c < numCol; c++)
        centroidRow[c] = rowCentroid[tempRowCL][c];
      myCRS->subtractRow(centroidRow, r);
      for (int cc = 0; cc < numColCluster; cc++)
        compressedRow[cc] = Acompressed[tempRowCL][cc];
      myCRS->subtractRow(compressedRow, r, colCL);
      delta1 = rowQuality4Centroid[tempRowCL] - computeRowQuality4CentroidUnnormalized(centroidRow, rowCS[tempRowCL]-1);
      delta1 += computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[tempRowCL]-1) - rowQuality4Compressed[tempRowCL]; 
      for (int rc = 0; rc < tempRowCL; rc++){
        for (int c = 0; c < numCol; c++)
          centroidRow[c] = rowCentroid[rc][c];
        myCRS->addRow(centroidRow, r);
        for(int cc = 0; cc < numColCluster; cc++)
          compressedRow[cc] = Acompressed[rc][cc];
        myCRS->addRow(compressedRow, r, colCL);
        delta2 = rowQuality4Centroid[rc] - computeRowQuality4CentroidUnnormalized(centroidRow, rowCS[rc]+1);
        delta2 += computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1) - rowQuality4Compressed[rc];
        updateVariable(minDelta2, tempCluster, delta2, rc);
      }
      for (int rc = tempRowCL+1; rc < numRowCluster; rc++){
        for (int c = 0; c < numCol; c++)
          centroidRow[c] = rowCentroid[rc][c];
        myCRS->addRow(centroidRow, r);
        for(int cc = 0; cc < numColCluster; cc++)
          compressedRow[cc] = Acompressed[rc][cc];
        myCRS->addRow(compressedRow, r, colCL);
        delta2 = rowQuality4Centroid[rc] - computeRowQuality4CentroidUnnormalized(centroidRow, rowCS[rc]+1);
        delta2 += computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1) - rowQuality4Compressed[rc];
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
  myCRS->subtractRow(rowCentroid[rowCL[fromRow]], fromRow); 
  rowQuality4Centroid[rowCL[fromRow]] = computeRowQuality4CentroidUnnormalized(rowCL[fromRow]);
  myCRS->addRow(rowCentroid[toCluster], fromRow); 
  rowQuality4Centroid[toCluster] = computeRowQuality4CentroidUnnormalized(toCluster);

  rowCL[fromRow] = toCluster;
  delete [] centroidRow;
  delete [] compressedRow;
  checkDumpLevel4Cocluster(dumpFile);
}


void MssrIIcc::doRowLocalSearch(oneStep trace [], int step, bool *isReversed)
{
  bool tempIsReversed = false;
  int fromRow = 0, tempCluster, toCluster, tempRowCL;
  double delta1, delta2, minDelta = MY_DBL_MAX, minDelta2;
  double *centroidRow = new double [numCol], *compressedRow = new double [numColCluster];
  trace[step].id = 0;
  trace[step].fromCluster = rowCL[0];
  trace[step].toCluster = toCluster = rowCL[0];
  trace[step].change = 0;

  for (int r = 0; r < numRow; r++){
    tempRowCL = rowCL[r];
    if (rowCS[tempRowCL] > 1 && !isRowMarked[r]){
      tempCluster = tempRowCL;
      minDelta2 = MY_DBL_MAX;

      if (isReversed[r]){

        for (int c = 0; c < numCol; c++)
          centroidRow[c] = rowCentroid[tempRowCL][c];
        myCRS->addRow(centroidRow, r);
        for (int cc = 0; cc < numColCluster; cc++)
          compressedRow[cc] = Acompressed[tempRowCL][cc];
        myCRS->addRow(compressedRow, r, colCL);
        delta1 = rowQuality4Centroid[tempRowCL] - computeRowQuality4CentroidUnnormalized(centroidRow, rowCS[tempRowCL]-1);
        delta1 += computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[tempRowCL]-1) - rowQuality4Compressed[tempRowCL]; 
        for (int rc = 0; rc < tempRowCL; rc++){
          for (int c = 0; c < numCol; c++)
            centroidRow[c] = rowCentroid[rc][c];
          myCRS->subtractRow(centroidRow, r);
          for(int cc = 0; cc < numColCluster; cc++)
            compressedRow[cc] = Acompressed[rc][cc];
          myCRS->subtractRow(compressedRow, r, colCL);
          delta2 = rowQuality4Centroid[rc] - computeRowQuality4CentroidUnnormalized(centroidRow, rowCS[rc]+1);
          delta2 += computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1) - rowQuality4Compressed[rc];
          updateVariable(minDelta2, tempCluster, delta2, rc);
        }
        for (int rc = tempRowCL+1; rc < numRowCluster; rc++){
          for (int c = 0; c < numCol; c++)
            centroidRow[c] = rowCentroid[rc][c];
          myCRS->subtractRow(centroidRow, r);
          for(int cc = 0; cc < numColCluster; cc++)
            compressedRow[cc] = Acompressed[rc][cc];
          myCRS->subtractRow(compressedRow, r, colCL);
          delta2 = rowQuality4Centroid[rc] - computeRowQuality4CentroidUnnormalized(centroidRow, rowCS[rc]+1);
          delta2 += computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1) - rowQuality4Compressed[rc];
          updateVariable(minDelta2, tempCluster, delta2, rc);
        }
        if ((delta1 + minDelta2) < minDelta){
	  fromRow = r;
	  toCluster = tempCluster;
          minDelta = delta1 + minDelta2;
	  tempIsReversed = true;
        }
     
      } else {					// (i.e., isReversed[r] == false)
      
        for (int c = 0; c < numCol; c++)
          centroidRow[c] = rowCentroid[tempRowCL][c];
        myCRS->subtractRow(centroidRow, r);
        for (int cc = 0; cc < numColCluster; cc++)
          compressedRow[cc] = Acompressed[tempRowCL][cc];
        myCRS->subtractRow(compressedRow, r, colCL);
        delta1 = rowQuality4Centroid[tempRowCL] - computeRowQuality4CentroidUnnormalized(centroidRow, rowCS[tempRowCL]-1);
        delta1 += computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[tempRowCL]-1) - rowQuality4Compressed[tempRowCL]; 
        for (int rc = 0; rc < tempRowCL; rc++){
          for (int c = 0; c < numCol; c++)
            centroidRow[c] = rowCentroid[rc][c];
          myCRS->addRow(centroidRow, r);
          for(int cc = 0; cc < numColCluster; cc++)
            compressedRow[cc] = Acompressed[rc][cc];
          myCRS->addRow(compressedRow, r, colCL);
          delta2 = rowQuality4Centroid[rc] - computeRowQuality4CentroidUnnormalized(centroidRow, rowCS[rc]+1);
	  delta2 += computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1) - rowQuality4Compressed[rc];
          updateVariable(minDelta2, tempCluster, delta2, rc);
        }
        for (int rc = tempRowCL+1; rc < numRowCluster; rc++){
          for (int c = 0; c < numCol; c++)
            centroidRow[c] = rowCentroid[rc][c];
          myCRS->addRow(centroidRow, r);
          for(int cc = 0; cc < numColCluster; cc++)
            compressedRow[cc] = Acompressed[rc][cc];
          myCRS->addRow(compressedRow, r, colCL);
          delta2 = rowQuality4Centroid[rc] - computeRowQuality4CentroidUnnormalized(centroidRow, rowCS[rc]+1);
	  delta2 += computeRowQuality4Compressed2WayUnnormalized(compressedRow, rowCS[rc]+1) - rowQuality4Compressed[rc];
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
  
  if (!tempIsReversed){
    myCRS->subtractRow(Acompressed, rowCL[fromRow], fromRow, colCL);
    rowQuality4Compressed[rowCL[fromRow]] = computeRowQuality4Compressed2WayUnnormalized(rowCL[fromRow]);
    myCRS->addRow(Acompressed, toCluster, fromRow, colCL);
    rowQuality4Compressed[toCluster] = computeRowQuality4Compressed2WayUnnormalized(toCluster);
    myCRS->subtractRow(rowCentroid[rowCL[fromRow]], fromRow); 
    rowQuality4Centroid[rowCL[fromRow]] = computeRowQuality4CentroidUnnormalized(rowCL[fromRow]);
    myCRS->addRow(rowCentroid[toCluster], fromRow); 
    rowQuality4Centroid[toCluster] = computeRowQuality4CentroidUnnormalized(toCluster);
  } else {
    myCRS->addRow(Acompressed, rowCL[fromRow], fromRow, colCL);
    rowQuality4Compressed[rowCL[fromRow]] = computeRowQuality4Compressed2WayUnnormalized(rowCL[fromRow]);
    myCRS->subtractRow(Acompressed, toCluster, fromRow, colCL);
    rowQuality4Compressed[toCluster] = computeRowQuality4Compressed2WayUnnormalized(toCluster);
    myCRS->addRow(rowCentroid[rowCL[fromRow]], fromRow); 
    rowQuality4Centroid[rowCL[fromRow]] = computeRowQuality4CentroidUnnormalized(rowCL[fromRow]);
    myCRS->subtractRow(rowCentroid[toCluster], fromRow); 
    rowQuality4Centroid[toCluster] = computeRowQuality4CentroidUnnormalized(toCluster);
  }
  rowCL[fromRow] = toCluster;
  delete [] centroidRow;
  delete [] compressedRow;
  checkDumpLevel4Cocluster(dumpFile);
}


void MssrIIcc::doColLocalSearch(oneStep trace [], int step)
{
  int fromCol = 0, tempCluster, toCluster, tempColCL;
  double delta1, delta2, minDelta = MY_DBL_MAX, minDelta2 = MY_DBL_MAX;
  double *centroidCol = new double [numRow], *compressedCol = new double [numRowCluster];
  trace[step].id = 0;
  trace[step].fromCluster = colCL[0];
  trace[step].toCluster = toCluster = colCL[0];
  trace[step].change = 0;

  for(int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    if (colCS[tempColCL] > 1  && !isColMarked[c]){
      tempCluster = tempColCL;
      minDelta2 = MY_DBL_MAX;
      for (int r = 0; r < numRow; r++)
        centroidCol[r] = colCentroid[tempColCL][r];
      myCCS->subtractCol(centroidCol, c);
      for(int rc = 0; rc < numRowCluster; rc++)
	compressedCol[rc] = Acompressed[rc][tempColCL];
      myCCS->subtractCol(compressedCol, c, rowCL);
      delta1 = colQuality4Centroid[tempColCL] - computeColQuality4CentroidUnnormalized(centroidCol, colCS[tempColCL]-1);
      delta1 += computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[tempColCL]-1) - colQuality4Compressed[tempColCL]; 
      for (int cc = 0; cc < tempColCL; cc++){
        for (int r = 0; r < numRow; r++)
          centroidCol[r] = colCentroid[cc][r];
        myCCS->addCol(centroidCol, c);
        for(int rc = 0; rc < numRowCluster; rc++)
          compressedCol[rc] = Acompressed[rc][cc];
        myCCS->addCol(compressedCol, c, rowCL);
        delta2 = colQuality4Centroid[cc] - computeColQuality4CentroidUnnormalized(centroidCol, colCS[cc]+1);
        delta2 += computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[cc]+1) - colQuality4Compressed[cc];
        updateVariable(minDelta2, tempCluster, delta2, cc);
      }
      for (int cc = tempColCL+1; cc < numColCluster; cc++){
        for (int r = 0; r < numRow; r++)
          centroidCol[r] = colCentroid[cc][r];
        myCCS->addCol(centroidCol, c);
        for(int rc = 0; rc < numRowCluster; rc++)
          compressedCol[rc] = Acompressed[rc][cc];
        myCCS->addCol(compressedCol, c, rowCL);
        delta2 = colQuality4Centroid[cc] - computeColQuality4CentroidUnnormalized(centroidCol, colCS[cc]+1);
        delta2 += computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[cc]+1) - colQuality4Compressed[cc];
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
  myCCS->subtractCol(colCentroid[colCL[fromCol]], fromCol); 
  colQuality4Centroid[colCL[fromCol]] = computeColQuality4CentroidUnnormalized(colCL[fromCol]);
  myCCS->addCol(colCentroid[toCluster], fromCol); 
  colQuality4Centroid[toCluster] = computeColQuality4CentroidUnnormalized(toCluster);

  colCL[fromCol] = toCluster;
  delete [] centroidCol;
  delete [] compressedCol;
  checkDumpLevel4Cocluster(dumpFile);
}


void MssrIIcc::doColLocalSearch(oneStep trace [], int step, bool *isReversed)
{
  int fromCol = 0, tempCluster, toCluster, tempColCL;
  double delta1, delta2, minDelta = MY_DBL_MAX, minDelta2 = MY_DBL_MAX;
  double *centroidCol = new double [numRow], *compressedCol = new double [numRowCluster];
  trace[step].id = 0;
  trace[step].fromCluster = colCL[0];
  trace[step].toCluster = toCluster = colCL[0];
  trace[step].change = 0;

  for(int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    if (colCS[tempColCL] > 1  && !isColMarked[c]){
      tempCluster = tempColCL;
      minDelta2 = MY_DBL_MAX;
      for (int r = 0; r < numRow; r++)
        centroidCol[r] = colCentroid[tempColCL][r];
      myCCS->subtractCol(centroidCol, c, isReversed);
      for(int rc = 0; rc < numRowCluster; rc++)
	compressedCol[rc] = Acompressed[rc][tempColCL];
      myCCS->subtractCol(compressedCol, c, rowCL, isReversed);
      delta1 = colQuality4Centroid[tempColCL] - computeColQuality4CentroidUnnormalized(centroidCol, colCS[tempColCL]-1);
      delta1 += computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[tempColCL]-1) - colQuality4Compressed[tempColCL]; 
      for (int cc = 0; cc < tempColCL; cc++){
        for (int r = 0; r < numRow; r++)
          centroidCol[r] = colCentroid[cc][r];
        myCCS->addCol(centroidCol, c, isReversed);
        for(int rc = 0; rc < numRowCluster; rc++)
          compressedCol[rc] = Acompressed[rc][cc];
        myCCS->addCol(compressedCol, c, rowCL, isReversed);
        delta2 = colQuality4Centroid[cc] - computeColQuality4CentroidUnnormalized(centroidCol, colCS[cc]+1);
        delta2 += computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[cc]+1) - colQuality4Compressed[cc];
        updateVariable(minDelta2, tempCluster, delta2, cc);
      }
      for (int cc = tempColCL+1; cc < numColCluster; cc++){
        for (int r = 0; r < numRow; r++)
          centroidCol[r] = colCentroid[cc][r];
        myCCS->addCol(centroidCol, c, isReversed);
        for(int rc = 0; rc < numRowCluster; rc++)
          compressedCol[rc] = Acompressed[rc][cc];
        myCCS->addCol(compressedCol, c, rowCL, isReversed);
        delta2 = colQuality4Centroid[cc] - computeColQuality4CentroidUnnormalized(centroidCol, colCS[cc]+1);
        delta2 += computeColQuality4Compressed2WayUnnormalized(compressedCol, colCS[cc]+1) - colQuality4Compressed[cc];
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
  myCCS->subtractCol(colCentroid[colCL[fromCol]], fromCol, isReversed); 
  colQuality4Centroid[colCL[fromCol]] = computeColQuality4CentroidUnnormalized(colCL[fromCol]);
  myCCS->addCol(colCentroid[toCluster], fromCol, isReversed); 
  colQuality4Centroid[toCluster] = computeColQuality4CentroidUnnormalized(toCluster);

  colCL[fromCol] = toCluster;
  delete [] centroidCol;
  delete [] compressedCol;
  checkDumpLevel4Cocluster(dumpFile);
}



bool MssrIIcc::doRowLocalSearchChain()
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

     
bool MssrIIcc::doColLocalSearchChain()
{
  checkDumpLevel4LocalSearch("beginCol");
  bool isHelpful = false;
  int minIndex;
  double *totalChange = new double[colLocalSearchLength], minChange;
  oneStep *trace = new oneStep[colLocalSearchLength];
 
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


void MssrIIcc::doPingPong()
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
	case SINGLE_IN_BATCH:					// NEED TO DOUBLE-CHECK!!!
          doBatchUpdate4VariationI(isReversed);
//          cout << "  Invalid BatchUpdate Variation Type: " << batchUpdateType << endl << endl;
//          exit(EXIT_FAILURE);	
	  break;
	case MULTIPLE_RESPECTIVELY:
          doBatchUpdate4VariationII(isReversed);
	  break;
	case SINGLE_BY_FLIP:					// NEED TO IMPLEMENT!!!
          doBatchUpdate4VariationIII(isReversed);
//          cout << "  Invalid BatchUpdate Variation Type: " << batchUpdateType << endl << endl;
//          exit(EXIT_FAILURE);	
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
        computeRowCentroid(isReversed);
        isNormalizedRowCentroid = false;
        computeRowQuality4CentroidUnnormalized();  
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
        computeColCentroid(isReversed);  
        isNormalizedColCentroid = false;
        computeColQuality4CentroidUnnormalized();  
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
//        computeRowClusterSize();
        rowLocalSearchLength = getEmptyRC();
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
        computeRowCentroid();
        isNormalizedRowCentroid = false;
        computeRowQuality4CentroidUnnormalized();  
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
        computeColCentroid();  
        isNormalizedColCentroid = false;
        computeColQuality4CentroidUnnormalized();  
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
