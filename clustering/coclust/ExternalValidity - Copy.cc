/*
  ExternalVality.cc
    Implementation of the ExternalValidity class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#include <iostream>
#include <fstream>
#include <algorithm>
#include <vector>
#include <cmath>
#include <stdio.h>
#include <assert.h>

#include "ExternalValidity.h"

using namespace std;

ExternalValidity::ExternalValidity(int nClass, int nCluster, int nPoint, int *classLbl, int *clusterLbl)
{
  numClass = nClass;
  numCluster = nCluster;
  numPoint = nPoint;
  classLabel = classLbl;
  clusterLabel = clusterLbl;
  clusterSize = new int [numCluster];
  classSize = new int [numClass];
  confusionMatrix = new int * [numCluster];
  for (int i = 0; i < numCluster; i++)
    confusionMatrix[i] = new int [numClass];
  for (int i = 0; i < numCluster; i++)
    clusterSize[i] = 0;
  for (int i = 0; i < numClass; i++)
    classSize[i] = 0;
  for (int i = 0; i < numPoint; i++){
    clusterSize[clusterLabel[i]]++;
    classSize[classLabel[i]]++;
  }
  for(int i = 0; i < numCluster; i++)
    for (int j = 0; j < numClass; j++)
      confusionMatrix[i][j] = 0;
  for (int i = 0; i < numPoint; i++)
    confusionMatrix[clusterLabel[i]][classLabel[i]]++;
  isSilent = false;		// not used...
  memoryUsed += (numCluster*numClass + numCluster + numClass) * sizeof(int);
}


ExternalValidity::~ExternalValidity()
{
  for (int j=0; j< numClass; j++)
    delete [] confusionMatrix[j];
  delete [] confusionMatrix;
  delete [] classSize;
  delete [] clusterSize;
}


void ExternalValidity::setSilent(bool s)	// not used...
{
  isSilent = s;
}


void ExternalValidity::printCM(ostream &os)
{
  if ((!os) == 0){
    os << "  Confusion Matrix" << endl;
    for (int i = 0; i < numCluster; i++){
//      os << endl << "\t";
      for (int j = 0; j < numClass; j++)
        os << "\t" << confusionMatrix[i][j];
      os << endl;
    }
    os << endl;
  }
}


void ExternalValidity::purity_Entropy_MutInfo(bool isShowingEachCluster, ostream &os1, ostream &os2, ostream &os3)
{
  int *sum_row, *sum_col;
  double sum, max, mut_info = 0.0, average_purity = 0.0, average_entropy = 0.0;
  sum_row = new int [numCluster];
  sum_col = new int [numClass];
  for(int i = 0;i < numCluster; i++){
    sum = 0.0;
    max = -1;
    for(int j = 0; j < numClass; j++){
      if (max < confusionMatrix[i][j])
        max = confusionMatrix[i][j];
      if (clusterSize[i] != 0 && confusionMatrix[i][j] != 0)
        sum += (double)confusionMatrix[i][j] / clusterSize[i] * log((double)clusterSize[i] / confusionMatrix[i][j]) / log((double)numClass);
    }
    if(clusterSize[i] != 0){  
      if (isShowingEachCluster){
        if ((!os1) == 0){
          os1 << "  Purity of cluster  " << i << "        = " << (double)max/clusterSize[i] << endl;
          os1 << "  Entropy of cluster " << i << "        = " << sum*log(2.0) << endl;
        }
        if ((!os2) == 0){
          os2 << "  Purity of cluster  " << i << "        = " << (double)max/clusterSize[i] << endl;
          os2 << "  Entropy of cluster " << i << "        = " << sum*log(2.0) << endl;
        }
        if ((!os3) == 0){
          os3 << "  Purity of cluster  " << i << "        = " << (double)max/clusterSize[i] << endl;
          os3 << "  Entropy of cluster " << i << "        = " << sum*log(2.0) << endl;
        }
      }
      average_purity += (double)max / clusterSize[i];
    } 
    average_entropy += sum;
  }
  if ((!os1) == 0){
    os1 << "  Average Purity of clusters  = " << average_purity / numCluster << endl;
    os1 << "  Average Entropy of clusters = " << average_entropy * log(2.0) / numCluster << endl;
  }
  if ((!os2) == 0){
    os2 << "  Average Purity of clusters  = " << average_purity / numCluster << endl;
    os2 << "  Average Entropy of clusters = " << average_entropy * log(2.0) / numCluster << endl;
  }
  if ((!os3) == 0){
    os3 << "  Average Purity of clusters  = " << average_purity / numCluster << endl;
    os3 << "  Average Entropy of clusters = " << average_entropy * log(2.0) / numCluster << endl;
  }
  for(int i = 0; i < numCluster; i++){
    sum_row[i] = 0;
    for(int k = 0; k < numClass; k++)
      sum_row[i] += confusionMatrix[i][k];
  }
  for(int k = 0; k < numClass; k++){
    sum_col[k] = 0;
    for(int i = 0; i < numCluster; i++)
      sum_col[k] += confusionMatrix[i][k];
  }
  for(int i = 0; i < numCluster; i++)
    for(int k = 0; k < numClass; k++)
      if (confusionMatrix[i][k] > 0)
        mut_info += confusionMatrix[i][k] * log(confusionMatrix[i][k] * numPoint * 1.0 / (sum_row[i] * sum_col[k]));
  mut_info /= numPoint;
  double hx = 0, hy = 0, min;
  for (int i = 0; i < numCluster; i++)
    if (sum_row[i] > 0)
      hx += sum_row[i] * log(sum_row[i] * 1.0);    
  hx = log(numPoint * 1.0) - hx / numPoint;
  for (int i = 0; i < numClass; i++)
    if (sum_col[i] > 0)
      hy += sum_col[i] * log(sum_col[i] * 1.0);
  hy = log(numPoint * 1.0) - hy / numPoint;
  //min = hx<hy?hx:hy;
  min = (hx + hy) / 2;
  if ((!os1) == 0)
    os1 << "  (Normalized) MI of clusters = " << mut_info / min << endl;
  if ((!os2) == 0)
    os2 << "  (Normalized) MI of clusters = " << mut_info / min << endl;
  if ((!os3) == 0)
    os3 << "  (Normalized) MI of clusters = " << mut_info / min << endl;
  delete [] sum_row;
  delete [] sum_col;
}


void ExternalValidity::F_measure(ostream &os1, ostream &os2, ostream &os3)
{
  double **Recall, **Precision, **F;
  double F_value; 
  Recall = new double *[numCluster];
  for (int i = 0; i < numCluster; i++)
    Recall[i] = new double[numClass];
  Precision = new double *[numCluster];
  for (int i = 0; i < numCluster; i++)
    Precision[i] = new double[numClass];
  F = new (double *)[numCluster];
  for (int i = 0; i < numCluster; i++)
    F[i] = new double[numClass];
  for (int i = 0; i < numCluster; i++)
    for (int j = 0; j < numClass; j++)
      Recall[i][j] = confusionMatrix[i][j] * 1.0 / classSize[j];
  for (int i = 0; i < numCluster; i++)
    for (int j = 0; j < numClass; j++)
      Precision[i][j] = confusionMatrix[i][j] * 1.0 / clusterSize[i];
  for (int i = 0; i < numCluster; i++)
    for (int j = 0; j < numClass; j++)
      F[i][j] = 2.0 * Recall[i][j] * Precision[i][j] / (Recall[i][j] + Precision[i][j]);
  F_value = 0.0;
  for (int j = 0; j < numClass; j++){
    double temp_max = 0.0;
    for (int i = 0; i < numCluster; i++)
      if (temp_max < F[i][j])
	temp_max = F[i][j];
    F_value += temp_max * classSize[j];
  }
  F_value /= numPoint;
  if ((!os1) == 0)
    os1 << "  F-measure value of clusters = " << F_value << endl;
  if ((!os2) == 0)
    os2 << "  F-measure value of clusters = " << F_value << endl;
  if ((!os3) == 0)
    os3 << "  F-measure value of clusters = " << F_value << endl;
  for(int i = 0; i < numCluster; i++){
    delete [] Recall[i];
    delete [] Precision[i];
    delete [] F[i];
  }
  delete [] F;
  delete [] Precision;
  delete [] Recall;
}


void ExternalValidity::micro_avg_precision_recall(double &p_t, double &r_t, ostream &os1, ostream &os2, ostream &os3)
  /* for the definition of micro-average precision/recall see paper "Unsupervised document classification
     using sequential information maximization" by N. Slonim, N. Friedman and N. Tishby */
{
  int *uni_label, *alpha, *beta, *gamma;
  uni_label = new int[numCluster];
  for (int i = 0; i < numCluster; i++){
    uni_label[i] = 0;
    double temp = confusionMatrix[i][0];
    for (int j = 1; j < numClass; j++)
      if (temp < confusionMatrix[i][j]){
        temp = confusionMatrix[i][j];
	uni_label[i] = j;
      }
  }
  alpha = new int[numClass];
  beta = new int[numClass];
  gamma = new int[numClass];
  for (int j = 0; j < numClass; j++){
    alpha[j] = 0;
    beta[j] = 0;
    gamma[j] = 0;
  }
  for (int i = 0; i < numPoint; i++)
    if (uni_label[clusterLabel[i]] == classLabel[i])
      alpha[classLabel[i]]++;
    else {
      beta[uni_label[clusterLabel[i]]]++;
      gamma[classLabel[i]]++;
    }
  double temp = 0, temp1 = 0;
  for (int j = 0; j < numClass; j++){
    temp += alpha[j];
    temp1 += beta[j];
  }
  temp1 += temp;
  p_t = temp * 1.0 / temp1;
  temp1 = 0;
  for (int j = 0; j < numClass; j++)
    temp1 += gamma[j];
  temp1 += temp;
  r_t = temp * 1.0 / temp1;
  if ((!os1) == 0){
    os1 << "  Micro-average Precision     = " << p_t << endl;
    os1 << "  Micro-average Recall        = " << r_t << endl << endl;
  }
  if ((!os2) == 0){
    os2 << "  Micro-average Precision     = " << p_t << endl;
    os2 << "  Micro-average Recall        = " << r_t << endl << endl;
  }
  if ((!os3) == 0){
    os3 << "  Micro-average Precision     = " << p_t << endl;
    os3 << "  Micro-average Recall        = " << r_t << endl << endl;
  }
  delete [] uni_label;
  delete [] alpha;
  delete [] beta;
  delete [] gamma;
}    


void ExternalValidity::getAccuracy(double &accuracy, ostream &os1, ostream &os2, ostream &os3)
  /* This computes the general precision, sometimes called accuracy, which can be defined as:
     Accuracy = 1/T (sum_{i=1}^l (t_i)) * 100, 
     where T denotes the total number of points, 
           l denotes the number of clusters,
	   and t_i denotes the number of the points correctly clustered into a corresponding class i.
     Notice that each t_i is a diagonal element of the corresponding confusion matrix whose cluster labels 
     are permuted so that sum of diagonal elements is maximized.
  */ 
{
  assert(numClass == numCluster);
  vector<int> v;
  // create the data...
  for (int i = 0; i < numClass; i++)
    v.push_back(i);
  // permutate the data...
  int maxTrace = 0;
  do {
    int tempTrace = 0;
    for (int i = 0; i < numClass; i++)
      tempTrace += confusionMatrix[v[i]][i];
    if (tempTrace > maxTrace)
      maxTrace = tempTrace;
  } while (next_permutation(v.begin(), v.end()));
  int tempSum = 0;
  for (int i = 0; i < numClass; i++)
    for (int j = 0; j < numClass; j++)
      tempSum += confusionMatrix[i][j];
  accuracy = (double)maxTrace / (double)tempSum;
  if ((!os1) == 0)
    os1 << "  Accuracy (= Precision)      = " << accuracy << endl << endl;
  if ((!os2) == 0)
    os2 << "  Accuracy (= Precision)      = " << accuracy << endl << endl;
  if ((!os3) == 0)
    os3 << "  Accuracy (= Precision)      = " << accuracy << endl << endl;
}    
