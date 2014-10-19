/*
  Itcc.cc
    Implementation of the information theoretic co-cluster algorithm 
    with smoothing, local search, and variations of batch/local search update.

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#include <iostream>
#include <fstream>
#include <stdlib.h>

#include "Itcc.h"
#include "MatrixVector.h"


Itcc::Itcc(Matrix *inputCCS, Matrix *inputCRS, commandLineArgument myCLA): Coclustering(inputCCS, inputCRS, myCLA)
{
//  cout << endl << "Itcc::Itcc()" << endl;
  if (myCCS->isHavingNegative()){
    cout << "  Invalid matrix for ITCC. Matrix should be non-negative." << endl << endl;
    exit(EXIT_FAILURE);
  }
  PlogP = myCCS->getPlogP();
  mutualInfo = myCCS->getMutualInfo();
  pX = myCCS->getPX();
  pY = myCCS->getPY();
  qYxhat = new double *[numRowCluster];
  for (int rc = 0; rc < numRowCluster; rc++)
    qYxhat[rc] = new double[numCol];
  qXyhat = new double *[numColCluster];
  for (int cc = 0; cc < numColCluster; cc++)
    qXyhat[cc] = new double[numRow];
  pxhat = new double[numRowCluster];
  pyhat = new double[numColCluster];
  memoryUsed += (numRowCluster * numCol + numColCluster * numRow + numRowCluster + numColCluster) * sizeof(double);
}


Itcc::~Itcc()
{
  delete [] pxhat;
  delete [] pyhat;
  for (int cc = 0; cc < numColCluster; cc++)
    delete [] qXyhat[cc];
  delete [] qXyhat;
  for (int rc = 0; rc < numRowCluster; rc++)
    delete [] qYxhat[rc];
  delete [] qYxhat;
//  cout << endl << "Itcc::~Itcc()" << endl;
}


void Itcc::computeRowCentroid()
{
  for (int rc = 0; rc < numRowCluster; rc++)
    for (int c = 0; c < numCol; c++)
      qYxhat[rc][c] = 0;
  for (int r = 0; r < numRow; r++){
    int rc = rowCL[r];
    double pX_r_over_pxhat_rc = pX[r] / pxhat[rc];  // maybe we need to check pX[r] > 0
    for (int c = 0; c < numCol; c++){
      int cc = colCL[c];
      if (Acompressed[rc][cc] > 0)
	//qYxhat[rc][c] += Acompressed[rc][cc] * pX[r] / pxhat[rc] * pY[c] / pyhat[cc];
	qYxhat[rc][c] += Acompressed[rc][cc] * pX_r_over_pxhat_rc * pY[c] / pyhat[cc];

    }
  }
  for (int rc = 0; rc < numRowCluster; rc++)
    normalize_vec_1(qYxhat[rc], numCol);
  checkDumpLevel4Centroid(qYxhat, numRowCluster, numCol);
}


void Itcc::computeColCentroid()
{
  for (int cc = 0; cc < numColCluster; cc++)
    for (int r = 0; r < numRow; r++)
      qXyhat[cc][r] = 0;
  for (int c = 0; c < numCol; c++){
    int cc = colCL[c];
    double pY_c_over_pyhat_cc = pY[c] / pyhat[cc];  // maybe we need to check pY[c] > 0
    for (int r = 0; r < numRow; r++){
      int rc = rowCL[r];
      if (Acompressed[rc][cc] > 0)
        //qXyhat[cc][r] += Acompressed[rc][cc] * pX[r] / pxhat[rc] * pY[c] / pyhat[cc];
        qXyhat[cc][r] += Acompressed[rc][cc] * pX[r] / pxhat[rc] * pY_c_over_pyhat_cc ;
    }
  }
  for (int cc = 0; cc < numColCluster; cc++)
    normalize_vec_1(qXyhat[cc], numRow);
  checkDumpLevel4Centroid(qXyhat, numColCluster, numRow);
}



void Itcc::computeRowCentroid4RowCluster()
{
  myCRS->computeRowCentroid(numRowCluster, rowCL, qYxhat);
  isNormalizedRowCentroid = false;
}


void Itcc::computeColCentroid4ColCluster()
{
  myCCS->computeColCentroid(numColCluster, colCL, qXyhat);
  isNormalizedColCentroid = false;
}


void Itcc::computeMarginal()
{
  for (int rc = 0; rc < numRowCluster; rc++)
    pxhat[rc] = 0;
  for (int cc = 0; cc < numColCluster; cc++)
    pyhat[cc] = 0;
  for (int rc = 0; rc < numRowCluster; rc++)
    for (int cc = 0; cc < numColCluster; cc++)
      pxhat[rc] += Acompressed[rc][cc];
  for (int rc = 0; rc < numRowCluster; rc++)
    for (int cc = 0; cc < numColCluster; cc++)
      pyhat[cc] += Acompressed[rc][cc];
}


void Itcc::computeObjectiveFunction()
{
  checkDumpLevel4Cocluster(dumpFile);
  //objValue = PlogP - myCCS->getPlogQ(Acompressed, rowCL, colCL, pxhat, pyhat);
  objValue = mutualInfo - computeMutualInfo(Acompressed, numRowCluster, numColCluster, pxhat, pyhat);
}


void Itcc::computeObjectiveFunction4RowCluster()
{
  objValue4RowCluster = mutualInfo - computeMutualInfo(qYxhat, numRowCluster, numCol);
}


void Itcc::computeObjectiveFunction4ColCluster()
{
  objValue4ColCluster = mutualInfo - computeMutualInfo(qXyhat, numColCluster, numRow);
}


void Itcc::doRowFarthestInitialization()
{
  double maxDis = MY_DBL_MIN;
  double *tempVec = new double[numCol];
  double ** simMat;
  int maxInd = 0;
  bool *markPicked = new bool[numRow];
  for (int i = 0; i < numRow; i++)
    markPicked[i] = false;
  for (int i = 0; i < numCol; i++)
    tempVec[i] = 0;
  for (int i = 0; i < numRow; i++)
    myCRS->ith_add_CV(i, tempVec);
  for (int i = 0; i < numRow; i++){
//    double temp = myCRS->Kullback_leibler(tempVec, i, NO_SMOOTHING, ROW_DIMENSION);
//cout << "SMOOTHING_TYPE_4_ROW = " << smoothingType << endl;
    double temp = myCRS->Kullback_leibler(tempVec, i, smoothingType, ROW_DIMENSION);
    if (temp > maxDis){
      maxDis = temp;
      maxInd = i;
    }
  }
  markPicked[maxInd] = true;
  myCRS->ith_add_CV(maxInd, qYxhat[0]);
  normalize_vec_1(qYxhat[0], numCol);
  delete [] tempVec;
  simMat = new double *[numRowCluster];
  for (int i = 0; i < numRowCluster; i++)
    simMat[i] = new double[numRow];
  for (int i = 0; i < numRow; i++)
    simMat[0][i] = rowDistance(i, 0); 
  for (int i = 1; i < numRowCluster; i++){
    double temp;
    maxDis = MY_DBL_MIN;
    maxInd = 0;
    for(int j = 0; j < numRow; j++)
      if (!markPicked[j]){
        temp = 0;
        for (int k = 0; k < i; k++)
          temp += simMat[k][j];
          if (temp > maxDis){
            maxDis = temp;
            maxInd = j;
          }
      }
    markPicked[maxInd] = true;
    myCRS->ith_add_CV(maxInd, qYxhat[i]);
    normalize_vec_1(qYxhat[i], numCol);
    for(int j = 0; j < numRow; j++)
      simMat[i][j] = rowDistance(j, i);
  }
  for (int i = 0; i < numRow; i++)
    rowCL[i] = 0;
  for (int i = 0; i < numRowCluster; i++)
    rowCS[i] = 1;            // just to make sure cluster size is >0
  reassignRC();
  for (int i = 0; i < numRowCluster; i++)
    delete [] simMat[i];
  delete [] simMat;
  delete [] markPicked;
}


void Itcc::doColFarthestInitialization()
{
  double maxDis = MY_DBL_MIN;
  double *tempVec = new double[numRow];
  double **simMat;
  int maxInd = 0;
  bool *markPicked = new bool[numCol];
  for (int i = 0; i < numCol; i++)
    markPicked[i] = false;
  for (int i = 0; i < numRow; i++)
    tempVec[i] = 0;
  for (int i = 0; i < numCol; i++)
    myCCS->ith_add_CV(i, tempVec); 
  for (int i = 0; i < numCol; i++){
//    double temp = myCCS->Kullback_leibler(tempVec, i, NO_SMOOTHING, COL_DIMENSION);
//cout << "SMOOTHING_TYPE_4_COL = " << smoothingType << endl;
    double temp = myCCS->Kullback_leibler(tempVec, i, smoothingType, COL_DIMENSION);
    if (temp > maxDis){
      maxDis = temp;
      maxInd = i;
    }
  }
  markPicked[maxInd] = true;
  myCCS->ith_add_CV(maxInd, qXyhat[0]);
  normalize_vec_1(qXyhat[0], numRow);
  delete [] tempVec;
  simMat = new double *[numColCluster];
  for (int i = 0; i < numColCluster; i++)
    simMat[i] = new double [numCol];
  for (int i = 0; i < numCol; i++)
    simMat[0][i] = colDistance(i, 0);
  for (int i = 1; i < numColCluster; i++){
    double temp;
    maxDis = MY_DBL_MIN;
    maxInd = 0;
    for(int j = 0; j < numCol; j++)
      if (!markPicked[j]){
        temp = 0;
        for (int k = 0; k < i; k++)
          temp += simMat[k][j];
        if (temp > maxDis){
          maxDis = temp;
          maxInd = j;
        }
      }
    markPicked[maxInd] = true;
    myCCS->ith_add_CV(maxInd, qXyhat[i]);
    normalize_vec_1(qXyhat[i], numRow);
    for(int j = 0; j < numCol; j++)
      simMat[i][j] = colDistance(j, i);
  }
  for (int i = 0; i < numCol; i++)
    colCL[i] = 0;
  for (int i = 0; i < numColCluster; i++)
    colCS[i] = 1;              // just to make sure cluster size is >0
  reassignCC();
  for (int i = 0; i < numColCluster; i++)
    delete [] simMat[i];
  delete [] simMat;
  delete [] markPicked;
}

       
void Itcc::doRowRandomPerturbInitialization()
{
  double tempNorm;
  double *tempVec = new double[numCol];
  double *center = new double[numCol];
  for (int i=0; i<numCol; i++)
    tempVec[i] = randNumGenerator.GetUniform() - 0.5;
  normalize_vec_1(tempVec, numCol);
  tempNorm = perturbationMagnitude * randNumGenerator.GetUniform();
  for (int j = 0; j < numCol; j++)
    tempVec[j] *= tempNorm;
  for (int i = 0; i < numCol; i++)
    center[i] = 0;
  for (int i = 0; i < numRow; i++)
    myCRS->ith_add_CV(i, center);
  for (int i = 0; i < numRowCluster; i++){
    for(int j = 0; j < numCol; j++)
      qYxhat[i][j] = center[j] * fabs(tempVec[j]+1);
    normalize_vec_1(qYxhat[i], numCol);
  }
  for (int i = 0; i < numRow; i++)
    rowCL[i] = 0;
  for (int i = 0; i < numRowCluster; i++)
    rowCS[i] = 1;            // just to make sure cluster size is >0
  reassignRC();
  delete [] tempVec;
  delete [] center;
}


void Itcc::doColRandomPerturbInitialization()
{
  double tempNorm;
  double *tempVec = new double[numRow];
  double *center = new double[numRow];
  for (int i=0; i<numRow; i++)
    tempVec[i] = randNumGenerator.GetUniform() - 0.5;
  normalize_vec_1(tempVec, numRow);
  tempNorm = perturbationMagnitude * randNumGenerator.GetUniform();
  for (int j = 0; j < numRow; j++)
    tempVec[j] *= tempNorm;
  for (int i = 0; i < numRow; i++)
    center[i] = 0;
  for (int i = 0; i < numCol; i++)
    myCCS->ith_add_CV(i, center);
  for (int i = 0; i < numColCluster; i++){
    for(int j = 0; j < numRow; j++)
      qXyhat[i][j] = center[j] * fabs(tempVec[j]+1);
    normalize_vec_1(qXyhat[i], numRow);
  }
  for (int i = 0; i < numCol; i++)
    colCL[i] = 0;
  for (int i = 0; i < numColCluster; i++)
    colCS[i] = 1;            // just to make sure cluster size is >0
  reassignCC();
  delete [] tempVec;
  delete [] center;
}


double Itcc::rowDistance(int r, int rc)
{
  return myCRS->Kullback_leibler(qYxhat[rc], r, smoothingType, ROW_DIMENSION);
}


double Itcc::colDistance(int c, int cc)
{
  return myCCS->Kullback_leibler(qXyhat[cc], c, smoothingType, COL_DIMENSION);
}


void Itcc::reassignRC()
{
  int rowClusterChange = 0;
  for (int r = 0; r < numRow; r++){
    int tempRowCL = rowCL[r];
    int minCL = tempRowCL;
    double minDistance = MY_DBL_MAX;
    for (int rc = 0; rc < numRowCluster; rc++){
      if (rowCS[rc] > 0){
        double tempDistance = rowDistance(r, rc); 
	updateVariable(minDistance, minCL, tempDistance, rc);
      }
    }
    if (minCL != rowCL[r])
      rowClusterChange++;
    rowCL[r] = minCL;
  }
  checkDumpLevel4NumOfChange("row(s)", rowClusterChange);
}

	
void Itcc::reassignCC()
{
  int colClusterChange = 0;
  for (int c = 0; c < numCol; c++){
    int tempColCL = colCL[c];
    int minCL = tempColCL;
    double minDistance = MY_DBL_MAX;
    for (int cc = 0; cc < numColCluster; cc++){
      if (colCS[cc] > 0){
        double tempDistance = colDistance(c, cc); 
	updateVariable(minDistance, minCL, tempDistance, cc);
      }
    }
    if (minCL != colCL[c])
      colClusterChange++;
    colCL[c] = minCL;
  }
  checkDumpLevel4NumOfChange("col(s)", colClusterChange);
}    


void Itcc::doInitialization()
{  
  chooseInitializationMethod();
  if (isTakingReverse){
    cout << "  Row should not be reversed in ITCC." << endl << endl;
    exit(EXIT_FAILURE);
  }
  isEmptyRowClusterReported = isEmptyColClusterReported = false ;
  computeRowClusterSize();
  computeColClusterSize();
  if (isComputingOneWayObjective){
    computeRowCentroid4RowCluster();
//    normalizeRowCentroid();
    computeObjectiveFunction4RowCluster();
    computeColCentroid4ColCluster();
//    normalizeColCentroid();  
    computeObjectiveFunction4ColCluster();
  }
  computeAcompressed();
  computeMarginal();
  computeObjectiveFunction();
//  cout << "Initialization done..." << endl;
  checkDumpLevel4InitialObjectValue();
}


void Itcc::doBatchUpdate()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  do {
    numIteration++;
    
    oldObjValue = objValue;
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    if (numRowCluster < numRow){
      computeRowCentroid();
      reassignRC();
      computeAcompressed();
      computeMarginal();
      computeObjectiveFunction();

      checkDumpLevel4BatchUpdate("row", numIteration);
    }
    
    if (numColCluster < numCol){
      computeColCentroid();
      reassignCC();
      computeAcompressed();
      computeMarginal();
      computeObjectiveFunction();
    
      checkDumpLevel4BatchUpdate("col", numIteration);
    }
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * mutualInfo));

  checkDumpLevel4BatchUpdate("end");
}


void Itcc::doBatchUpdate4VariationI()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  int *beforeRowCL = new int[numRow];
  int *afterRowCL = new int[numRow];
//  int *beforeColCL = new int[numCol];
  double oldObjValue;

  do {
    numIteration++;
    
    oldObjValue = objValue;
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    for (int r = 0; r < numRow; r++)
      beforeRowCL[r] = rowCL[r];
    computeRowCentroid();
    reassignRC();
    for (int r = 0; r < numRow; r++)
      afterRowCL[r] = rowCL[r];
    computeAcompressed();
    computeMarginal();
    computeObjectiveFunction();
    checkDumpLevel4BatchUpdate("row", numIteration);
//------------
    for (int r = 0; r < numRow; r++)
      rowCL[r] = beforeRowCL[r];
    computeAcompressed();
    computeMarginal();
    for (int r = 0; r < numRow; r++)
      rowCL[r] = afterRowCL[r];
//------------
//    for (int c = 0; c < numCol; c++)
//      beforeColCL[c] = colCL[c];
    computeColCentroid();
    reassignCC();
    computeAcompressed();
    computeMarginal();
    computeObjectiveFunction();
    checkDumpLevel4BatchUpdate("col", numIteration);

    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * mutualInfo));

  delete [] beforeRowCL;
  delete [] afterRowCL;
//  delete [] beforeColCL;
  checkDumpLevel4BatchUpdate("end");
}


void Itcc::doBatchUpdate4VariationII()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;
  double tempRowSmoothingFactor = rowSmoothingFactor, tempColSmoothingFactor = colSmoothingFactor;
  
  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
    oldObjValue = objValue;
    computeRowCentroid();
    reassignRC();
    computeAcompressed();
    computeMarginal();
    computeObjectiveFunction();
    checkDumpLevel4BatchUpdate("row", numIteration);
    tempRowSmoothingFactor *= myCRS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * mutualInfo));

  numIteration = 0;  
  tempRowSmoothingFactor = rowSmoothingFactor;
  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  do {
    numIteration++;
    myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
    oldObjValue = objValue;
    computeColCentroid();
    reassignCC();
    computeAcompressed();
    computeMarginal();
    computeObjectiveFunction();
    checkDumpLevel4BatchUpdate("col", numIteration);
    tempColSmoothingFactor *= myCCS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (colBatchUpdateThreshold * mutualInfo));
  colSmoothingFactor = tempColSmoothingFactor;
  checkDumpLevel4BatchUpdate("end");
}

/*
// It doesn't guarantee the monotonic decrese of objective function values.
void Itcc::doBatchUpdate4VariationIII()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  int *beforeRowCL = new int[numRow];
  int *afterRowCL = new int[numRow];
  double oldObjValue;
  double tempRowSmoothingFactor = rowSmoothingFactor, tempColSmoothingFactor = colSmoothingFactor;
  
  for (int r = 0; r < numRow; r++)
    beforeRowCL[r] = rowCL[r];
  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
    oldObjValue = objValue;
//    for (int r = 0; r < numRow; r++)
//      beforeRowCL[r] = rowCL[r];
    computeRowCentroid();
    reassignRC();
    for (int r = 0; r < numRow; r++)
      afterRowCL[r] = rowCL[r];
    computeAcompressed();
    computeMarginal();
    computeObjectiveFunction();
    checkDumpLevel4BatchUpdate("row", numIteration);
    tempRowSmoothingFactor *= myCRS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * mutualInfo));
//------------
  for (int r = 0; r < numRow; r++)
    rowCL[r] = beforeRowCL[r];
  computeAcompressed();
  computeMarginal();
  for (int r = 0; r < numRow; r++)
    rowCL[r] = afterRowCL[r];
//------------
  numIteration = 0;  
  tempRowSmoothingFactor = rowSmoothingFactor;
  myCRS->setSmoothingFactor(smoothingType, tempRowSmoothingFactor);
  do {
    numIteration++;
    myCCS->setSmoothingFactor(smoothingType, tempColSmoothingFactor);
    oldObjValue = objValue;
    computeColCentroid();
    reassignCC();
    computeAcompressed();
    computeMarginal();
    computeObjectiveFunction();
    checkDumpLevel4BatchUpdate("col", numIteration);
    tempColSmoothingFactor *= myCCS->getAnnealingFactor();
  } while ((oldObjValue - objValue) > (colBatchUpdateThreshold * mutualInfo));
  colSmoothingFactor = tempColSmoothingFactor;
  delete [] beforeRowCL;
  delete [] afterRowCL;
  checkDumpLevel4BatchUpdate("end");
}
*/

void Itcc::doBatchUpdate4VariationIII()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
    oldObjValue = objValue;
  
    if (randNumGenerator.GetUniform() > SELECTION_PROBABILITY){
      computeRowCentroid();
      reassignRC();
      computeAcompressed();
      computeMarginal();
      computeObjectiveFunction();
      checkDumpLevel4BatchUpdate("row", numIteration);
    } else {
      computeColCentroid();
      reassignCC();
      computeAcompressed();
      computeMarginal();
      computeObjectiveFunction();
      checkDumpLevel4BatchUpdate("col", numIteration);
    }
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * mutualInfo));

  checkDumpLevel4BatchUpdate("end");
}


void Itcc::doBatchUpdate4VariationIV()
{
  checkDumpLevel4BatchUpdate("begin");
  int numIteration = 0;
  double oldObjValue;

  do {
    numIteration++;
    myCRS->setSmoothingFactor(smoothingType, rowSmoothingFactor);
    myCCS->setSmoothingFactor(smoothingType, colSmoothingFactor);
  
    if (randNumGenerator.GetUniform() > SELECTION_PROBABILITY){
      do {
        oldObjValue = objValue;
        computeRowCentroid();
        reassignRC();
        computeAcompressed();
        computeMarginal();
        computeObjectiveFunction();
        checkDumpLevel4BatchUpdate("row", numIteration);
      } while ((oldObjValue - objValue) > (MULTIPLE_FACTOR * rowBatchUpdateThreshold * mutualInfo));
    } else {
      do {
        oldObjValue = objValue;
        computeColCentroid();
        reassignCC();
        computeAcompressed();
        computeMarginal();
        computeObjectiveFunction();
        checkDumpLevel4BatchUpdate("col", numIteration);
      } while ((oldObjValue - objValue) > (MULTIPLE_FACTOR * colBatchUpdateThreshold * mutualInfo));
    }
    rowSmoothingFactor *= myCRS->getAnnealingFactor();
    colSmoothingFactor *= myCCS->getAnnealingFactor();

  } while ((oldObjValue - objValue) > (rowBatchUpdateThreshold * mutualInfo));

  checkDumpLevel4BatchUpdate("end");
}


double Itcc::rowClusterQuality(double *row, double rowP, double *colP)
{
  double rcq = 0;
  for(int cc = 0; cc < numColCluster; cc++)
    if (row[cc] > 0)
      rcq += row[cc] * log(row[cc] / (rowP * colP[cc]));
  return rcq / log(2.0);
}


double Itcc::colClusterQuality(double *col, double colP, double *rowP)
{
  double ccq = 0;
  for(int rc = 0; rc < numRowCluster; rc++)
    if (col[rc] > 0)
      ccq += col[rc] * log(col[rc] / (colP * rowP[rc]));
  return ccq / log(2.0);
}


void Itcc::rowClusterQuality(double *result)
{
  for(int rc = 0; rc < numRowCluster; rc++)
    result[rc] = 0;
  for(int rc = 0; rc < numRowCluster; rc++)
    for(int cc = 0; cc < numColCluster; cc++)
      if (Acompressed[rc][cc] > 0)
	result[rc] += Acompressed[rc][cc] * log(Acompressed[rc][cc] / (pxhat[rc] * pyhat[cc])) / log(2.0);
}


void Itcc::colClusterQuality(double *result)
{
  for(int cc = 0; cc < numColCluster; cc++)
    result[cc] = 0;
  for(int cc = 0; cc < numColCluster; cc++)
    for(int rc = 0; rc < numRowCluster; rc++)
      if (Acompressed[rc][cc] > 0)
	result[cc] += Acompressed[rc][cc] * log(Acompressed[rc][cc] / (pxhat[rc] * pyhat[cc])) / log(2.0);
}


void Itcc::recoverRowCL(int begin, int end, oneStep trace [])
{
  for(int r = begin; r < end; r++)
    if (trace[r].toCluster != trace[r].fromCluster){
      myCRS->subtractRow(Acompressed, trace[r].toCluster, trace[r].id, colCL);
      pxhat[trace[r].toCluster] -= pX[trace[r].id];
      myCRS->addRow(Acompressed, trace[r].fromCluster, trace[r].id, colCL);
      pxhat[trace[r].fromCluster] += pX[trace[r].id];
      rowCL[trace[r].id] = trace[r].fromCluster;
    }
}


void Itcc::recoverColCL(int begin, int end, oneStep trace [])
{
  for(int c = begin; c < end; c++)
    if (trace[c].toCluster != trace[c].fromCluster){
      myCCS->subtractCol(Acompressed, trace[c].toCluster, trace[c].id, rowCL);
      pyhat[trace[c].toCluster] -= pY[trace[c].id];
      myCCS->addCol(Acompressed, trace[c].fromCluster, trace[c].id, rowCL);
      pyhat[trace[c].fromCluster] += pY[trace[c].id];
      colCL[trace[c].id] = trace[c].fromCluster;
    }
}


void Itcc::doRowLocalSearch(oneStep trace [], int step)
{
  int fromRow = 0, tempCluster, toCluster, tempRowCL;
  double rowP, delta1, delta2, minDelta = MY_DBL_MAX, minDelta2 = MY_DBL_MAX;
  double *rowClusterQ = new double[numRowCluster], *temppxhat = new double[numColCluster];
  trace[step].id = 0;
  trace[step].fromCluster = rowCL[0];
  trace[step].toCluster = toCluster = rowCL[0];
  trace[step].change = 0;
  rowClusterQuality(rowClusterQ);
  for(int r = 0; r < numRow; r++){
    tempRowCL = rowCL[r];
    tempCluster = tempRowCL;
    minDelta2 = MY_DBL_MAX;
    if (rowCS[tempRowCL] > 1 && !isRowMarked[r]){
      for(int cc = 0; cc < numColCluster; cc++)
        temppxhat[cc] = Acompressed[tempRowCL][cc];
      myCRS->subtractRow(temppxhat, r, colCL);
      rowP = pxhat[tempRowCL] - pX[r];
      delta1 = rowClusterQ[tempRowCL] - rowClusterQuality(temppxhat, rowP, pyhat);
      for (int rc = 0; rc < tempRowCL; rc++){
	for(int cc = 0; cc < numColCluster; cc++)
	  temppxhat[cc] = Acompressed[rc][cc];
	myCRS->addRow(temppxhat, r, colCL);
        rowP = pxhat[rc] + pX[r];
        delta2 = rowClusterQ[rc] - rowClusterQuality(temppxhat, rowP, pyhat);
        updateVariable(minDelta2, tempCluster, delta2, rc);
      }
      for (int rc = tempRowCL+1; rc < numRowCluster; rc++){
	for(int cc = 0; cc < numColCluster; cc++)
	  temppxhat[cc] = Acompressed[rc][cc];
	myCRS->addRow(temppxhat, r, colCL);
        rowP = pxhat[rc] + pX[r];
        delta2 = rowClusterQ[rc] - rowClusterQuality(temppxhat, rowP, pyhat);
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
  pxhat[rowCL[fromRow]] -= pX[fromRow];
  myCRS->addRow(Acompressed, toCluster, fromRow, colCL);
  pxhat[toCluster] += pX[fromRow];

  rowCL[fromRow] = toCluster;
  delete [] rowClusterQ;
  delete [] temppxhat;
  checkDumpLevel4Cocluster(dumpFile);
}


void Itcc::doColLocalSearch(oneStep trace [], int step)
{
  int fromCol = 0, tempCluster, toCluster, tempColCL;
  double colP, delta1, delta2, minDelta = MY_DBL_MAX, minDelta2 = MY_DBL_MAX;
  double *colClusterQ = new double[numColCluster], *temppyhat = new double[numRowCluster];
  trace[step].id = 0;
  trace[step].fromCluster = colCL[0];
  trace[step].toCluster = toCluster = colCL[0];
  trace[step].change = 0;
  colClusterQuality(colClusterQ);
  for (int c = 0; c < numCol; c++){
    tempColCL = colCL[c];
    tempCluster = tempColCL;
    minDelta2 = MY_DBL_MAX;
    if (colCS[tempColCL] > 1  && !isColMarked[c]){
      for (int rc = 0; rc < numRowCluster; rc++)
        temppyhat[rc] = Acompressed[rc][tempColCL];
      myCCS->subtractCol(temppyhat, c, rowCL);
      colP = pyhat[tempColCL] - pY[c];
      delta1 = colClusterQ[tempColCL] - colClusterQuality(temppyhat, colP, pxhat);
      for (int cc = 0; cc < tempColCL; cc++){
        for (int rc = 0; rc < numRowCluster; rc++)
	  temppyhat[rc] = Acompressed[rc][cc];
	myCCS->addCol(temppyhat, c, rowCL);
	colP = pyhat[cc] + pY[c];
        delta2 = colClusterQ[cc] - colClusterQuality(temppyhat, colP, pxhat);
        updateVariable(minDelta2, tempCluster, delta2, cc);
      }
      for (int cc = tempColCL+1; cc < numColCluster; cc++){
        for (int rc = 0; rc < numRowCluster; rc++)
	  temppyhat[rc] = Acompressed[rc][cc];
	myCCS->addCol(temppyhat, c, rowCL);
	colP = pyhat[cc] + pY[c];
        delta2 = colClusterQ[cc] - colClusterQuality(temppyhat, colP, pxhat);
        updateVariable(minDelta2, tempCluster, delta2, cc);
      }
      if ((delta1 + minDelta2) < minDelta){
        minDelta = delta1 + minDelta2;
	toCluster = tempCluster;
	fromCol = c;
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
  pyhat[colCL[fromCol]] -= pY[fromCol];
  myCCS->addCol(Acompressed, toCluster, fromCol, rowCL);
  pyhat[toCluster] += pY[fromCol];

  colCL[fromCol] = toCluster;
  delete [] colClusterQ;
  delete [] temppyhat;
  checkDumpLevel4Cocluster(dumpFile);
}


bool Itcc::doRowLocalSearchChain()
{
  checkDumpLevel4LocalSearch("beginRow");
  bool isHelpful = false;
  int minIndex;
  double *totalChange = new double [rowLocalSearchLength], minChange;
  oneStep *trace = new oneStep[rowLocalSearchLength];
  
  for (int i = 0; i < rowLocalSearchLength; i++){
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
  if(totalChange[minIndex] > rowLocalSearchThreshold * mutualInfo){
    checkDumpLevel4NumOfChain("row", 0, NULL);
    recoverRowCL(0, rowLocalSearchLength, trace);
    isHelpful = false;
  } else {
    checkDumpLevel4NumOfChain("row", minIndex, totalChange);
    recoverRowCL(minIndex+1, rowLocalSearchLength, trace);
    isHelpful = true;
  }
  delete [] totalChange;
  delete [] trace;
  checkDumpLevel4Cocluster(dumpFile);
  checkDumpLevel4LocalSearch("endRow");
  return isHelpful;
}
     

bool Itcc::doColLocalSearchChain()
{
  checkDumpLevel4LocalSearch("beginCol");
  bool isHelpful = false;
  int minIndex;
  double *totalChange = new double[colLocalSearchLength], minChange;
  oneStep *trace = new oneStep[colLocalSearchLength];
  
  for (int i = 0; i < colLocalSearchLength; i++){
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
  if(totalChange[minIndex] > colLocalSearchThreshold * mutualInfo){
    checkDumpLevel4NumOfChain("col", 0, NULL);
    recoverColCL(0, colLocalSearchLength, trace);
    isHelpful = false;
  } else {
    checkDumpLevel4NumOfChain("col", minIndex, totalChange);
    recoverColCL(minIndex+1, colLocalSearchLength, trace);
    isHelpful = true;
  }
  delete [] totalChange;
  delete [] trace;
  checkDumpLevel4Cocluster(dumpFile);
  checkDumpLevel4LocalSearch("endCol");
  return isHelpful;
}


void Itcc::doPingPong()
{
  bool isRowLocalSearchHelpful = false, isColLocalSearchHelpful = false;
  numIteration = 0;
  do {
    isRowLocalSearchHelpful = false;
    isColLocalSearchHelpful = false;
    numIteration++;
    
    checkDumpLevel4PingPong("begin", numIteration);
    
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

    computeRowClusterSize();
    computeColClusterSize();
    
    //-----------------------------------------
    // To avoid empty row cluster(s)...
    if (isAvoidingEmptyRowCluster){
      computeRowClusterSize();
//      rowLocalSearchLength = getEmptyRC();
      if (getEmptyRC() > 0)
        rowLocalSearchLength = DEFAULT_ROW_LOCAL_SEARCH_LENGTH;
      else
        rowLocalSearchLength = 0;
    }
    //-----------------------------------------       
    if (rowLocalSearchLength > 0){
      clearMark4Row();
      isRowLocalSearchHelpful = doRowLocalSearchChain();
    }  
    
    //-----------------------------------------
    // To avoid empty col cluster(s)...
    if (isAvoidingEmptyColCluster){
      computeColClusterSize();
//      colLocalSearchLength = getEmptyCC();
      if (getEmptyCC() > 0)
        colLocalSearchLength = DEFAULT_COL_LOCAL_SEARCH_LENGTH;
      else
        colLocalSearchLength = 0;
    }
    //-----------------------------------------    
    if (colLocalSearchLength > 0){
      clearMark4Col();
      isColLocalSearchHelpful = doColLocalSearchChain();
    }
    checkDumpLevel4PingPong("end", numIteration);

  } while ((isRowLocalSearchHelpful || isColLocalSearchHelpful) & (numIteration <= DEFAULT_MAX_PINGPONG_ITERATION));

  if (isComputingOneWayObjective){
    computeRowCentroid4RowCluster();
//    normalizeRowCentroid();
    computeObjectiveFunction4RowCluster();
    computeColCentroid4ColCluster();
//    normalizeColCentroid();  
    computeObjectiveFunction4ColCluster();
  }
  checkDumpLevel4FinalObjectValue();
  switch (dumpLevel){
    case MINIMUM_DUMP_LEVEL:
      break;
    case BATCH_UPDATE_DUMP_LEVEL:
    case LOCAL_SEARCH_DUMP_LEVEL:
      checkDumpLevel4Coclustering(cout, numIteration, mutualInfo);
    case MAXIMUM_DUMP_LEVEL:
      checkDumpLevel4Coclustering(dumpFile, numIteration, mutualInfo);
  }
  if (statisticsAccessMode != NO_OPEN_MODE)
    checkDumpLevel4Coclustering(statisticsFile, numIteration, mutualInfo);
//  cout << endl << "Itcc::~Itcc()" << endl;
}
