/* 
  DenseMatrix.cc
    Implementation of the DenseMatrix class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#include <iostream>
#include <fstream>
#include <cmath>
#include <assert.h>
#include <stdlib.h>

#include "DenseMatrix.h"


DenseMatrix::DenseMatrix(int row, int col, double **val) : Matrix (row, col)
  /* numRow, numCol, and val give the dimensionality and entries of matrix.
     norm[] may be used to store the L2 norm of each vector and
     L1_norm[] may be used to store the L1 norm of each vector
  */
{
  numRow = row;
  numCol = col;
  value = val;
}


DenseMatrix::~DenseMatrix ()
{
  if (norm != NULL)
    delete[] norm;
  if (L1_norm != NULL)
    delete[] L1_norm;
}


double DenseMatrix::dot_mult(double *x, int i) 
  /* this function computes the dot-product between the ith vector in the dense matrix
     and vector x; result is returned.
  */
{
  double result = 0.0;
  for (int j = 0; j < numRow; j++)
    result += value[j][i] * x[j];
  return result;
}


void DenseMatrix::trans_mult(double *x, double *result) 
  /* Suppose A is the dense matrix, this function computes A^T x;
     the results is stored in array result[]
  */
{
  for (int i = 0; i < numCol; i++)
    result[i] = dot_mult(x, i);
}


void DenseMatrix::squared_trans_mult(double *x, double *result) 
  /* Suppose A is the dense matrix, this function computes A^T x;
     the results is stored in array result[]
  */
{
  for (int i = 0; i < numCol; i++)
    result[i] = squared_dot_mult(x, i) ;
}


double DenseMatrix::squared_dot_mult(double *x, int i) 
  /* this function computes the dot-product between the ith vector in the dense matrix
     and vector x; result is returned.
  */
{
  double result=0.0;
  for (int j = 0; j< numRow; j++)
    result += value[j][i] * x[j];
  return result*result;
}


void DenseMatrix::A_trans_A(int flag, int * index, int *pointers, double ** A_t_A)
  /* computes A'A given doc_IDs in the same cluster; index[] contains doc_IDs for all docs, 
     pointers[] gives the range of doc_ID belonging to the same cluster;
     the resulting matrix is stored in A_t_A[][] ( d by d matrix)
     Notice that Gene expression matrix is usually n by d where n is #genes; this matrix
     format is different from that of document matrix. 
     In the main memory the matrix is stored in the format of document matrix.
  */
{
  int clustersize = pointers[1]-pointers[0];
  if (flag > 0){
    for (int i = 0; i < numRow; i++)
      for (int j = 0; j < numRow; j++){
        A_t_A[i][j] = 0;
        for (int k = 0; k < clustersize; k++){
	  int tempCol = index[k+pointers[0]];
          A_t_A[i][j] += value[i][tempCol] * value[j][tempCol];
	}
      }
  } else {
    for (int i = 0; i < numRow; i++)
      for (int j = 0; j < numRow; j++){
        A_t_A[i][j] = 0;
        for (int k = 0; k < clustersize; k++){
	  int tempCol = k + pointers[0];
          A_t_A[i][j] += value[i][tempCol] * value[j][tempCol];
	}
      }
  }
}


void DenseMatrix::right_dom_SV(int *cluster, int *cluster_size, int n_Clusters, double ** CV, double *cluster_quality, int flag)
{
  int *pointer = new int[n_Clusters+1], *index = new int[numCol], *range = new int[2];
  double **A_t_A;
  pointer[0] = 0;
  for (int i = 1; i < n_Clusters; i++)
    pointer[i] = pointer[i-1] + cluster_size[i-1];
  for (int i = 0; i < numCol; i++){
    int tempCluster = cluster[i];
    index[pointer[tempCluster]] = i;
    pointer[tempCluster]++;
  }
  pointer[0] = 0;
  for (int i = 1; i <= n_Clusters; i++)
    pointer[i] = pointer[i-1] + cluster_size[i-1];
  A_t_A =new double *[numRow];
  for (int i = 0; i < numRow; i++)
    A_t_A [i] = new double[numRow];
  if (flag < 0){
    for (int i = 0; i < n_Clusters; i++){
      range[0] = pointer[i];
      range[1] = pointer[i+1];
      A_trans_A(1, index, range, A_t_A); 
/*      
      for (int k=0; k< numRow; k++){
        for (int j=0; j< numRow; j++)
	  cout<<A_t_A[k][j]<<" ";
	cout<<endl;
	}
*/
      power_method(A_t_A, numRow, CV[i], CV[i], cluster_quality[i]);
   }
  } else if ((flag >= 0) && (flag < n_Clusters)){
    range[0] = pointer[flag];
    range[1] = pointer[flag+1];
    A_trans_A(1, index, range, A_t_A);
    power_method(A_t_A, numRow, CV[flag], CV[flag], cluster_quality[flag]);
  } else if ((flag >= n_Clusters) && (flag <2*n_Clusters)){
    range[0] = pointer[flag-n_Clusters];
    range[1] = pointer[flag-n_Clusters+1];
    A_trans_A(1, index, range, A_t_A);
    power_method(A_t_A, numRow, NULL, CV[flag-n_Clusters], cluster_quality[flag-n_Clusters]);
  }
  delete [] pointer;
  delete [] index;
  delete [] range;
  for (int i = 0; i < numRow; i++)
    delete [] A_t_A[i];
  delete [] A_t_A;
}


double DenseMatrix::euc_dis(double *x, int i, double norm_x)
  /* Given squared L2-norms of the vecs and v, norm[i] and norm_v,
     compute the Euc-dis between ith vec in the matrix and v,
     result is returned.
     Used (x-c)^T (x-c) = x^T x - 2 x^T c + c^T c
  */
{
  double result=0.0;
  for (int j = 0; j < numRow; j++)
    result += x[j] * value[j][i];
  result *= -2.0;
  result += norm[i] + norm_x;
  return result;
}


void DenseMatrix::euc_dis(double *x, double norm_x, double *result)
  /* Given squared L2-norms of the vecs and x, norm[i] and norm_x,
     compute the Euc-dis between each vec in the matrix with x,  
     results are stored in array 'result'. 
     Since the matrix is dense, not taking advantage of 
     (x-c)^T (x-c) = x^T x - 2 x^T c + c^T c
     but the abstract class definition needs the parameter of 'norm_x'
  */
{
  for (int i = 0; i < numCol; i++)
    result[i] = euc_dis(x, i, norm_x);
}


void DenseMatrix::Kullback_leibler(double *x, double *result, int laplace)
{
  for (int i=0; i<numCol; i++)
    result [i] = Kullback_leibler(x, i, laplace);
}


double DenseMatrix::Kullback_leibler(double *x, int i, int laplace)
  // Given the KL-norm of vec[i], norm[i], (already considered prior)
  //   compute KL divergence between vec[i] in the matrix with x,
  //   result is returned. 'sum_log' is sum of logs of x[j]
  //   Take advantage of KL(p, q) = 
  //  \sum_i p_i log(p_i) - \sum_i p_i log(q_i) = norm[i] - \sum_i p_i log(q_i)
  // KL norm is in unit of nats NOT bits
  
{
  double result = 0.0, row_inv_alpha = smoothingFactor / numRow, m_e = smoothingFactor;
  if (priors[i] > 0){
    switch (laplace){
      case NO_SMOOTHING:
        for (int j = 0; j < numRow; j++){
          if(x[j] > 0.0)
            result += value[j][i] * log(x[j]);
          else if (value[j][i] > 0.0)
	    return MY_DBL_MAX; // if KL(vec[i], x) is inf. give it a huge number 1.0e20
        }
        result = norm[i]-result;
	break;
      case UNIFORM_SMOOTHING:
        for (int j = 0; j < numRow; j++)
          result += value[j][i] * log(x[j] + row_inv_alpha);
        result = norm[i] - result + log(1 + smoothingFactor);
	break;
      case MAXIMUM_ENTROPY_SMOOTHING:
        for (int j = 0; j < numRow; j++)
          result += value[j][i] * log(x[j] + m_e * p_x[j]);
	result = norm[i] - result + log(1 + smoothingFactor);
	break;
      case LAPLACE_SMOOTHING:
        for (int j = 0; j < numRow; j++)
          result += (value[j][i] + row_inv_alpha) * log(x[j]);
	result = norm[i] - result / (1 + smoothingFactor);
	break;
      default:
        break;
    }
  }
  return result;
}


void DenseMatrix::Kullback_leibler(double *x, double *result, int laplace, double L1norm_x)
{
  for (int i = 0; i < numCol; i++)
    result[i] = Kullback_leibler(x, i, laplace, L1norm_x);
}


double DenseMatrix::Kullback_leibler(double *x, int i, int laplace, double L1norm_x)
  // Given the KL-norm of vec[i], norm[i], (already considered prior)
  //   compute KL divergence between vec[i] in the matrix with x,
  //   result is returned. 'sum_log' is sum of logs of x[j]
  //   Take advantage of KL(p, q) = 
  //  \sum_i p_i log(p_i) - \sum_i p_i log(q_i) = norm[i] - \sum_i p_i log(q_i)
  // KL norm is in unit of nats NOT bits
{
  double result = 0.0, row_inv_alpha = smoothingFactor * L1norm_x / numRow , m_e = smoothingFactor * L1norm_x;
  if (priors[i] > 0){
    switch (laplace){
      case NO_SMOOTHING:
        for (int j = 0; j < numRow; j++){
          if (x[j] > 0.0)
            result += value[j][i] * log(x[j]);
          else if (value[j][i] > 0.0)
            return MY_DBL_MAX; // if KL(vec[i], x) is inf. give it a huge number 1.0e20
	}
        result = norm[i] - result + log(L1norm_x);
        break;
      case UNIFORM_SMOOTHING:
        for (int j = 0; j < numRow; j++)
          result += value[j][i] * log(x[j] + row_inv_alpha);
        result = norm[i] - result + log((1+smoothingFactor) * L1norm_x);
        break;
      case MAXIMUM_ENTROPY_SMOOTHING:
        for (int j = 0; j < numRow; j++)
          result += value[j][i] * log(x[j] + m_e * p_x[j]);
        result = norm[i] - result + log((1+smoothingFactor) * L1norm_x);
        break; 
      case LAPLACE_SMOOTHING:
        for (int j = 0; j < numRow; j++)
          result += (value[j][i] + row_inv_alpha) * log(x[j]);
        result = norm[i] - result / (1+smoothingFactor);
        break;
      default:
        break;
    }
  }
  return result;
}


double DenseMatrix::Jenson_Shannon(double *x, int i, double prior_x)
  // Given the prior of vec[i]
  //   compute KL divergence between vec[i] in the matrix with x,
  //   result in nats is returned. 
  
{
  double result = 0.0, *p_bar, p1, p2, tempPriorsI = priors[i]; 
  if ((tempPriorsI > 0) && (prior_x > 0)){
    p1 = tempPriorsI / (tempPriorsI + prior_x);
    p2 = prior_x / (tempPriorsI + prior_x);
    p_bar = new double[numRow];
    for (int j = 0; j < numRow; j++)
      p_bar[j] = p2 * x[j] + p1 * value[j][i];
    result = p1 * Kullback_leibler(p_bar, i, NO_SMOOTHING) + ::Kullback_leibler(x, p_bar, numRow) * p2; 
    //result /= L1_norm[i] + l1n_x;
    delete [] p_bar;
  }
  return result; // the real JS value should be this result devided by L1_norm[i]+l1n_x
}


void DenseMatrix::Jenson_Shannon(double *x, double *result, double prior_x)
{
  for (int i = 0; i < numCol; i++)
    result[i] = Jenson_Shannon(x, i, prior_x);
}


void DenseMatrix::computeNorm_2()
  /* compute the squared L2 norms of each vector in the dense matrix
   */
{
  if (norm == NULL){
    norm = new double[numCol];
    memoryUsed += numCol * sizeof(double);
  }
  for (int i = 0; i < numCol; i++){
    norm[i] = 0.0;
    for (int j = 0; j < numRow; j++){
      double tempValue = value[j][i];
      norm[i] += tempValue * tempValue;
    }
  }
}


void DenseMatrix::computeNorm_1()
 /* compute the L1 norms of each vector in the dense matrix
   */
{
  if (L1_norm == NULL){
    L1_norm = new double[numCol];
    memoryUsed += numCol * sizeof(double);
  }
  for (int i = 0; i < numCol; i++){
    L1_norm[i] = 0.0;
    for (int j = 0; j < numRow; j++)
      L1_norm[i] += fabs(value[j][i]);
    L1_sum += L1_norm [i];
  }
}


void DenseMatrix::computeNorm_KL(int laplace)
  /* compute the KL norms of each vector p_i in the dense matrix
     i.e. \sum_x p_i(x) \log p_i(x)
     the norm[i] is in unit of nats NOT bits
   */
{
  double row_inv_alpha = smoothingFactor / numRow;
  Norm_sum = 0;
  if (norm == NULL){
    norm = new double[numCol];
    memoryUsed += numCol * sizeof(double);
  }
  switch (laplace){
    case NO_SMOOTHING:
    case UNIFORM_SMOOTHING:
      for (int i = 0; i < numCol; i++){
        norm[i] = 0.0;
        for (int j = 0; j < numRow; j++){
	  double tempValue = value[j][i];
          if (tempValue > 0.0)
	    norm[i] += tempValue * log(tempValue);
	}
        Norm_sum += norm[i] * priors[i];
      }
      break;
    case MAXIMUM_ENTROPY_SMOOTHING:
      for (int i = 0; i < numCol; i++){
        norm[i] = 0.0;
        for (int j = 0; j < numRow; j++){
	  double tempValue = value[j][i];
          if (tempValue > 0.0)
            norm[i] += tempValue * log(tempValue);
	}
	Norm_sum += norm[i] * priors[i];
      }
      if (p_x == NULL){
        p_x = new double [numRow];
        memoryUsed += numRow * sizeof(double);
      }
      for (int j = 0; j < numRow; j++){
         p_x[j] =0;
         for (int i = 0; i < numCol; i++)
           p_x[j] += priors[i] * value[j][i];
      }
      break;
    case LAPLACE_SMOOTHING:
      for (int i = 0; i < numCol; i++){
        norm[i] = 0.0;
        for (int j = 0; j < numRow; j++){
	  double tempValue = value[j][i];
          norm[i] += (tempValue + row_inv_alpha) * log(tempValue + row_inv_alpha);
          norm[i] = norm[i] / (1+smoothingFactor) + log(1+smoothingFactor);
          Norm_sum += norm[i] * priors[i];
        }
      }
      break;
    default:
      break;
  }
  Norm_sum /= log(2.0);
}


void DenseMatrix::normalize_mat_L2()
  /* L2 normalize each vector in the dense matrix to have L2 norm 1
   */
{
  for (int i = 0; i < numCol; i++){
    double norm = 0.0;
    for (int j = 0; j < numRow; j++){
      double tempValue = value[j][i];
      norm += tempValue * tempValue;
    }
    if (norm > 0.0){
      norm = sqrt(norm);
      for (int j = 0; j < numRow; j++)
        value[j][i] /= norm;
    }
  }
}


void DenseMatrix::normalize_mat_L1()
  /* L1 normalize each vector in the dense matrix to have L1 norm 1
   */
{
  for (int i = 0; i < numCol; i++){
    double norm = 0.0;
    for (int j = 0; j < numRow; j++)
      norm += fabs(value[j][i]);
    if (norm > 0)
      for (int j = 0; j < numRow; j++)
        value[j][i] /= norm;
  }
}


void DenseMatrix::ith_add_CV(int i, double *CV)
{
  for (int j = 0; j < numRow; j++)
    CV[j] += value[j][i];
}


void DenseMatrix::ith_add_CV_prior(int i, double *CV)
{
  for (int j = 0; j < numRow; j++)
    CV[j] += priors[i] * value[j][i];
}


void DenseMatrix::CV_sub_ith(int i, double *CV)
{
  for (int j = 0; j < numRow; j++)
    CV[j] -= value[j][i];
}


void DenseMatrix::CV_sub_ith_prior(int i, double *CV)
{
  for (int j = 0; j < numRow; j++)
    CV[j] -= priors[i] * value[j][i];
}


double DenseMatrix::computeMutualInfo()
{
  double *rowSum = new double[numRow], MI = 0.0;
  for (int i = 0; i < numRow; i++){
    rowSum[i] = 0.0;
    for (int j = 0; j < numCol; j++)
      rowSum[i] += value[i][j] * priors[i];
  }
  for (int i = 0; i < numRow; i++){
    double temp = 0;
    for (int j = 0; j < numCol; j++){
      double tempValue = value[i][j];
      if (tempValue > 0.0)
        temp += tempValue * log(tempValue / rowSum[i]);
    }
    MI += temp * priors[i];
  }
  delete [] rowSum;
  return MI / log(2.0);
}


double DenseMatrix::exponential_kernel(double *x, int i, double norm_x, double sigma_squared)
  // this function computes the exponential kernel distance between i_th data with the centroid v
{
  double result = 0.0;
  for (int j = 0; j < numRow; j++)
    result += x[j] * value[j][i];
  result *= -2.0;
  result += norm[i] + norm_x;
  return exp(result * 0.5 / sigma_squared);
}


void DenseMatrix::exponential_kernel(double *x, double norm_x, double *result, double sigma_squared)
  //this function computes the exponential kernel distance between all data with the centroid x
{
  for (int i = 0; i < numCol; i++)
    result[i] = exponential_kernel(x, i, norm_x, sigma_squared);
}


double DenseMatrix::i_j_dot_product(int i, int j)
//this function computes  dot product between vectors i and j
{
  double result = 0;
  if (i == j)
    for (int l = 0; l < numRow; l++){
      double tempValue = value[l][i];
      result += tempValue * tempValue;
    }
  else
    for (int l = 0; l < numRow; l++)
      result += value[l][i] * value[l][j];
  return result;
}


double DenseMatrix::squared_i_j_euc_dis(int i, int j)
  //computes squared Euc_dis between vec i and vec j
{
  return (norm[i] - 2.0 * i_j_dot_product(i,j) + norm[j]);
}


void  DenseMatrix::pearson_normalize()
//this function shift each vector so that it has 0 mean of all its entries
  //then the vector gets L2 normalized.
{
  for (int i = 0; i < numCol; i++){
    double mean = 0;
    for (int j = 0; j < numRow; j++)
      mean += value[j][i];
    mean /= numRow;
    for (int j = 0; j < numRow; j++)
      value[j][i] -= mean;
  }
/*
  for (int i = 0; i < numCol; i++) {
    for (int j = 0; j < numRow; j++)
      cout << value[j][i] << " ";
    cout << endl;
  }
*/
  normalize_mat_L2();
/*
  for (int i = 0; i < numCol; i++){
    for (j = 0; j < numRow; j++)
      cout << value[j][i] << " ";
    cout << endl;
  }
*/
}


bool DenseMatrix::isHavingNegative()
{
  bool havingNegative = false;
  for (int r = 0; r < numRow && !havingNegative; r++)
    for (int c = 0; c < numCol && !havingNegative; c++)
      if (value[r][c] < 0)
        havingNegative = true;
  return havingNegative;
}


double DenseMatrix::getPlogQ(double **pxhatyhat, int *rowCL, int *colCL, double *pXhat, double *pYhat)
{
  double PlogQ = 0;
  for (int i = 0; i < numRow; i++)
    for (int j = 0; j < numCol; j++){
      double tempValue = value[i][j];
      if (tempValue > 0.0){
        int tempRowCLI = rowCL[i];
	int tempColCLJ = colCL[j];
        PlogQ += tempValue * log(pxhatyhat[tempRowCLI][tempColCLJ] * pX[i] * pY[j] / (pXhat[tempRowCLI] * pYhat[tempColCLJ]));
      }
    }
  return PlogQ / log(2.0);
}


void DenseMatrix::preprocess()
{
  double totalSum = 0;
  PlogP = mutualInfo = 0;
  pX = new double[numRow];
  pY = new double[numCol];
  for (int i = 0; i < numRow; i++)
    pX[i] = 0;
  for (int j = 0; j < numCol; j++)
    pY[j] = 0;
  for (int i = 0; i < numRow; i++)
    for (int j = 0; j < numCol; j++){
      double tempValue = value[i][j];
      totalSum += tempValue;
      pX[i] += tempValue;
      pY[j] += tempValue;
    }
  for (int i = 0; i < numRow; i++)
    for (int j = 0; j < numCol; j++)
      value[i][j] /= totalSum;
  for (int i = 0; i < numRow; i++)
    pX[i] /= totalSum;
  for (int j = 0; j < numCol; j++)
    pY[j] /= totalSum;
  xnorm = new double[numRow];
  ynorm = new double[numCol];
  for (int i = 0; i < numRow; i++)
    xnorm[i] = 0;
  for (int j = 0; j < numCol; j++)
    ynorm[j] = 0;
  for (int i = 0; i < numRow; i++){
    for (int j = 0; j < numCol; j++){
      double tempValue = value[i][j];
      if(tempValue > 0){
        double temp = tempValue * log(tempValue);
        xnorm[i] += temp;
        ynorm[j] += temp;
        mutualInfo += tempValue * log(pX[i] * pY[j]);
      }
    }
    PlogP += xnorm[i];
  }
  mutualInfo = PlogP - mutualInfo;
}


void DenseMatrix::condenseMatrix(int *rowCL, int *colCL, int numRowCluster, int numColCluster, double **cM)
{
  for (int i = 0; i < numRowCluster; i++)
    for (int j = 0; j < numColCluster; j++)
      cM[i][j] = 0;
  for (int i = 0; i < numRow; i++)
    for (int j = 0; j < numCol; j++)
      cM[rowCL[i]][colCL[j]] += value[i][j];
}


void DenseMatrix::condenseMatrix(int *rowCL, int *colCL, int numRowCluster, int numColCluster, double **cM, bool *isReversed)
{
  int rowId = 0;
  for (int i = 0; i < numRowCluster; i++)
    for (int j = 0; j < numColCluster; j++)
      cM[i][j] = 0;
  for (int i = 0; i < numRow; i++){
    rowId = rowCL[i];
    if (isReversed[i])
      for (int j = 0; j < numCol; j++)
        cM[rowId][colCL[j]] += (0 - value[i][j]);
    else
      for (int j = 0; j < numCol; j++)
        cM[rowId][colCL[j]] += value[i][j];
  }    
}


double DenseMatrix::Kullback_leibler(double *x, int i, int priorType, int clusterDimension)
{
  double result = 0.0, uni_alpha;
  if (clusterDimension == COL_DIMENSION){
    switch (priorType){
      case NO_SMOOTHING:
        for (int j = 0; j < numRow; j++){
          if(x[j] > 0)
            result += value[j][i] * log(x[j]);
          else if (value[j][i] >0)
            return MY_DBL_MAX; // if KL(vec[i], x) is inf. give it a huge number 1.0e20
        }
        result = (ynorm[i] - result)/ pY[i] - log(pY[i]);
	break;
      case UNIFORM_SMOOTHING:
        uni_alpha = smoothingFactor / numRow;
        for (int j = 0; j < numRow; j++)
          result += value[j][i] * log(x[j] + uni_alpha) ;
        result = (ynorm[i] - result) / pY[i] - log(pY[i]) + log(1 + smoothingFactor);
        break;
      case MAXIMUM_ENTROPY_SMOOTHING:
        for (int j = 0; j < numRow; j++)
          result += value[j][i] * log(x[j] + pX[j] * smoothingFactor);
        result = (ynorm[i] - result)/ pY[i] - log(pY[i]) + log(1 + smoothingFactor);
        break;
      default:
        break;
    }
  } else {
    switch (priorType){
      case NO_SMOOTHING:
        for (int j = 0; j < numCol; j++)
          if (value[i][j] > 0){
            if(x[j] > 0)
              result += value[i][j] * log(x[j]);
            else 
              return MY_DBL_MAX; // if KL(vec[i], x) is inf. give it DBL_MAX.
	  }
        result = (xnorm[i] - result) / pX[i] - log(pX[i]);
        break;
      case UNIFORM_SMOOTHING:
        uni_alpha = smoothingFactor / numCol;
        for (int j = 0; j < numCol; j++)
          result += value[i][j] * log(x[j] + uni_alpha) ;
	result = (xnorm[i] - result) / pX[i] - log(pX[i]) + log(1 + smoothingFactor);
        break;
      case MAXIMUM_ENTROPY_SMOOTHING:
        for (int j = 0; j < numCol; j++)
          result += value[i][j] * log(x[j] + pY[j] * smoothingFactor) ;
        result = (xnorm[i] - result) / pX[i] - log(pX[i]) + log(1 + smoothingFactor);
	break;
      default:
        break;
    }
  }
//cout << result << endl;
  return result;
}


void DenseMatrix::subtractRow(double **x, int row, int i, int *colCL)
{
  for (int j = 0; j < numCol; j++)
    x[row][colCL[j]] -= value[i][j];
}


void DenseMatrix::addRow(double **x, int row, int i, int *colCL)
{
  for (int j = 0; j < numCol; j++)
    x[row][colCL[j]] += value[i][j];
}


void DenseMatrix::subtractCol(double **x, int col, int j, int *rowCL)
{
  for (int i = 0; i < numRow; i++)
    x[rowCL[i]][col] -= value[i][j];
}


void DenseMatrix::addCol(double **x, int col, int j, int *rowCL)
{
  for (int i = 0; i < numRow; i++)
    x[rowCL[i]][col] += value[i][j];
}


void DenseMatrix::subtractRow(double *x, int i, int *colCL)
{
  for (int j = 0; j < numCol; j++)
    x[colCL[j]] -= value[i][j];
}


void DenseMatrix::addRow(double *x, int i, int *colCL)
{
  for (int j = 0; j < numCol; j++)
    x[colCL[j]] += value[i][j];
}


void DenseMatrix::subtractCol(double *x, int j, int *rowCL)
{
  for (int i = 0; i < numRow; i++)
    x[rowCL[i]] -= value[i][j];
}


void DenseMatrix::addCol(double *x, int j, int *rowCL)
{
  for (int i = 0; i < numRow; i++)
    x[rowCL[i]] += value[i][j];
}


/*
// Does y = A * x, A is mxn, and y & x are mx1 and nx1 vectors
//   respectively  
void DenseMatrix::dmatvec(int m, int n, double **a, double *x, double *y)
{
  double yi;
  for(int i = 0;i < m; i++){
    yi = 0.0;
    for(int j = 0;j < n; j++)
      yi += a[i][j] * x[j];
    y[i] = yi;
  }
}


// Does y = A' * x, A is mxn, and y & x are nx1 and mx1 vectors
//   respectively  
void DenseMatrix::dmatvecat(int m, int n, double **a, double *x, double *y)
{
  double yi;
  for(int i = 0;i < n; i++){
    yi = 0.0;
    for(int j = 0;j < m; j++)
      yi += a[j][i] * x[j];
    y[i] = yi;
  }
}


// The procedure dqrbasis outputs an orthogonal basis spanned by the rows
//   of matrix a (using the QR Factorization of a ).  
void DenseMatrix::dqrbasis(double **q)
{
  double *work = new double[numRow];
  for(int i = 0; i < numRow;i++){
    dmatvec(i, numCol, q, value[i], work);
    dmatvecat(i, numCol, q, work, q[i]);
    for(int j = 0; j < numCol; j++)
      q[i][j] = value[i][j] - q[i][j];
    dvec_l2normalize(numCol, q[i]);
  }
}


// The function dvec_l2normsq computes the square of the
//  Euclidean length (2-norm) of the double precision vector v 
double DenseMatrix::dvec_l2normsq( int dim, double *v )
{
  double length = 0.0, tmp;
  for(int i = 0; i < dim; i++){
    tmp = *v++;
    length += tmp*tmp;
  }
  return length;
}


// The function dvec_l2normalize normalizes the double precision
//   vector v to have 2-norm equal to 1 
void DenseMatrix::dvec_l2normalize( int dim, double *v )
{
  double nrm;
  nrm = sqrt(dvec_l2normsq(dim, v));
  //if(nrm != 0) 
  //  dvec_scale(1.0 / nrm, dim, v);
}
*/


double DenseMatrix::squaredFNorm()
  /* compute the squared Frobenius norm of the dense matrix.
   */
{
  double temp = 0;
  for (int r = 0; r < numRow; r++)
    for (int c = 0; c < numCol; c++){
      double tempValue = value[r][c];
      temp += tempValue * tempValue;
    }
  return temp;
}


double DenseMatrix::squaredL2Norm4Row(int r)
  /* compute the squared L2 norm of row i in a matrix
   */
{
  double temp = 0;
  for (int c = 0; c < numCol; c++){
    double tempValue = value[r][c];
    temp += tempValue * tempValue;
  }
  return temp;
}


double DenseMatrix::squaredL2Norm4Col(int c)
  /* compute the squared L2 norm of column j in a matrix
   */
{
  double temp = 0;
  for (int r = 0; r < numRow; r++){
    double tempValue = value[r][c];
    temp += tempValue * tempValue;
  }
  return temp;
}


double DenseMatrix::computeRowDistance(int rowId, int rowCluster, int *colCL, double **cM, double rowQuality4Compressed)
{
  double temp = 0;
  for (int c = 0; c < numCol; c++)
    temp += value[rowId][c] * cM[rowCluster][colCL[c]];
  return (-2 * temp + rowQuality4Compressed);  
}


double DenseMatrix::computeColDistance(int colId, int colCluster, int *rowCL, double **cM, double colQuality4Compressed)
{
  double temp = 0;
  for (int r = 0; r < numRow; r++)
    temp += value[r][colId] * cM[rowCL[r]][colCluster];
  return (-2 * temp + colQuality4Compressed);  
}


double DenseMatrix::computeRowDistance(int rowId, int rowCluster, int *colCL, double **cM, double rowQuality4Compressed, bool *isReversed)
{
  double temp = 0;
  if (isReversed[rowId])
    for (int c = 0; c < numCol; c++)
      temp += (0 - value[rowId][c]) * cM[rowCluster][colCL[c]];
  else
    for (int c = 0; c < numCol; c++)
      temp += value[rowId][c] * cM[rowCluster][colCL[c]];
  return (-2 * temp + rowQuality4Compressed);  
}


double DenseMatrix::computeColDistance(int colId, int colCluster, int *rowCL, double **cM, double colQuality4Compressed, bool *isReversed)
{
  double temp = 0;
  for (int r = 0; r < numRow; r++){
    if (isReversed[r])
      temp += (0 - value[r][colId]) * cM[rowCL[r]][colCluster];
    else
      temp += value[r][colId] * cM[rowCL[r]][colCluster];
  }
  return (-2 * temp + colQuality4Compressed);  
}


//MSSRIICC
void DenseMatrix::computeRowCentroid(int numRowCluster, int *rowCL, double **rowCentroid)
{
  for (int rc = 0; rc < numRowCluster; rc++)
    for (int c = 0; c < numCol; c++)
      rowCentroid[rc][c] = 0;
  for (int r = 0; r < numRow; r++)
    for (int c = 0; c < numCol; c++)
      rowCentroid[rowCL[r]][c] += value[r][c];
}


void DenseMatrix::computeRowCentroid(int numRowCluster, int *rowCL, double **rowCentroid, bool *isReversed)
{
  for (int rc = 0; rc < numRowCluster; rc++)
    for (int c = 0; c < numCol; c++)
      rowCentroid[rc][c] = 0;
  for (int r = 0; r < numRow; r++)
    if (isReversed[r])
      for (int c = 0; c < numCol; c++)
        rowCentroid[rowCL[r]][c] -= value[r][c];
    else
      for (int c = 0; c < numCol; c++)
        rowCentroid[rowCL[r]][c] += value[r][c];
}


void DenseMatrix::computeColCentroid(int numColCluster, int *colCL, double **colCentroid)
{
  for (int cc = 0; cc < numColCluster; cc++)
    for (int r = 0; r < numRow; r++)
      colCentroid[cc][r] = 0;
  for (int r = 0; r < numRow; r++)
    for (int c = 0; c < numCol; c++)
      colCentroid[colCL[c]][r] += value[r][c];
}


void DenseMatrix::computeColCentroid(int numColCluster, int *colCL, double **colCentroid, bool *isReversed)
{
  for (int cc = 0; cc < numColCluster; cc++)
    for (int r = 0; r < numRow; r++)
      colCentroid[cc][r] = 0;
  for (int r = 0; r < numRow; r++)
    if (isReversed[r])
      for (int c = 0; c < numCol; c++)
        colCentroid[colCL[c]][r] -= value[r][c];
    else
      for (int c = 0; c < numCol; c++)
        colCentroid[colCL[c]][r] += value[r][c];
}


/*
double DenseMatrix::computeRowDistance(int rowId, int *colCL, double *colCentroid, double *mAR, double rowQuality4Compressed) 
{
  double temp = 0;
  for (int j = 0; j < numCol; j++)
    temp += (value[rowId][j] - colCentroid[colCL[j]]) * mAR[j];
  return (-2 * temp + rowQuality4Compressed);  
}


double DenseMatrix::computeColDistance(int colId, int *rowCL, double *rowCentroid, double *nAC, double colQuality4Compressed)
{
  double temp = 0;
  for (int i = 0; i < numRow; i++)
    temp += (value[i][colId] - rowCentroid[rowCL[i]]) * nAC[i];
  return (-2 * temp + colQuality4Compressed);  
}
*/


double DenseMatrix::computeRowDistance(int rowId, int rowCluster, int *rowCL, int *colCL, double **cM)
{
  double temp = 0;
  for (int c = 0; c < numCol; c++){
    double tempValue = value[rowId][c] - cM[rowCluster][colCL[c]];
    temp += tempValue * tempValue;
  }
  return temp;
}


double DenseMatrix::computeColDistance(int colId, int colCluster, int *rowCL, int *colCL, double **cM)
{
  double temp = 0;
  for (int r = 0; r < numRow; r++){
    double tempValue = value[r][colId] - cM[colCluster][rowCL[r]];
    temp += tempValue * tempValue;
  }
  return temp;
}


void DenseMatrix::computeRowAP(int rowId, double **colCentroid, int *colCL, double *rowAP)
{
  for (int c = 0; c < numCol; c++)
    rowAP[c] = value[rowId][c];
  for (int c = 0; c < numCol; c++)
    rowAP[c] -= colCentroid[colCL[c]][rowId];
}



void DenseMatrix::computeRowAP(int rowId, double **colCentroid, int *colCL, double *rowAP, bool *isReversed)
{
  if (isReversed[rowId]){
    for (int c = 0; c < numCol; c++)
      rowAP[c] = 0;
    for (int c = 0; c < numCol; c++)
      rowAP[c] -= value[rowId][c];
  } else 
    for (int c = 0; c < numCol; c++)
      rowAP[c] = value[rowId][c];
  for (int c = 0; c < numCol; c++)
    rowAP[c] -= colCentroid[colCL[c]][rowId];
}


void DenseMatrix::computeColAP(int colId, double **rowCentroid, int *rowCL, double *colAP)
{
  for (int r = 0; r < numRow; r++)
    colAP[r] = value[r][colId];
  for (int r = 0; r < numRow; r++)
    colAP[r] -= rowCentroid[rowCL[r]][colId];
}


void DenseMatrix::computeColAP(int colId, double **rowCentroid, int *rowCL, double *colAP, bool *isReversed)
{
  for (int r = 0; r < numRow; r++)
    colAP[r] = 0;
  for (int r = 0; r < numRow; r++)
    if (isReversed[r])
      colAP[r] -= value[r][colId];  
    else
      colAP[r] = value[r][colId];
  for (int r = 0; r < numRow; r++)
    colAP[r] -= rowCentroid[rowCL[r]][colId];
}


void DenseMatrix::subtractRow(double *x, int r)
{
  for (int c = 0; c < numCol; c++)
    x[c] -= value[r][c];
}


void DenseMatrix::subtractCol(double *x, int c)
{
  for (int r = 0; r < numRow; r++)
    x[r] -= value[r][c];
}


void DenseMatrix::addRow(double *x, int r)
{
  for (int c = 0; c < numCol; c++)
    x[c] += value[r][c];
}


void DenseMatrix::addCol(double *x, int c)
{
  for (int r = 0; r < numRow; r++)
    x[r] += value[r][c];

}


// for reversed rows

void DenseMatrix::subtractRow(double *x, int r, bool *isReversed)
{
  if (isReversed[r])
    for (int c = 0; c < numCol; c++)
      x[c] += value[r][c];
  else
    for (int c = 0; c < numCol; c++)
      x[c] -= value[r][c];
}


void DenseMatrix::subtractCol(double *x, int c, bool *isReversed)
{
  for (int r = 0; r < numRow; r++)
    if (isReversed[r])
      x[r] += value[r][c];
    else
      x[r] -= value[r][c];
}


void DenseMatrix::addRow(double *x, int r, bool *isReversed)
{
  if (isReversed[r])
    for (int c = 0; c < numCol; c++)
      x[c] -= value[r][c];
  else
    for (int c = 0; c < numCol; c++)
      x[c] += value[r][c];
}


void DenseMatrix::addCol(double *x, int c, bool *isReversed)
{
  for (int r = 0; r < numRow; r++)
    if (isReversed[r])
      x[r] -= value[r][c];
    else
      x[r] += value[r][c];
}


void DenseMatrix::subtractCol(double **x, int col, int j, int *rowCL, bool *isReversed)
{
  for (int i = 0; i < numRow; i++)
    if (isReversed[i])
      x[rowCL[i]][col] += value[i][j];
    else
      x[rowCL[i]][col] -= value[i][j];
}


void DenseMatrix::subtractCol(double *x, int j, int *rowCL, bool *isReversed)
{
  for (int i = 0; i < numRow; i++)
    if (isReversed[i])
      x[rowCL[i]] += value[i][j];
    else
      x[rowCL[i]] -= value[i][j];
}


void DenseMatrix::addCol(double **x, int col, int j, int *rowCL, bool *isReversed)
{
  for (int i = 0; i < numRow; i++)
    if (isReversed[i])
      x[rowCL[i]][col] -= value[i][j];
    else
      x[rowCL[i]][col] += value[i][j];
}


void DenseMatrix::addCol(double *x, int j, int *rowCL, bool *isReversed)
{
  for (int i = 0; i < numRow; i++)
    if (isReversed[i])
      x[rowCL[i]] -= value[i][j];
    else
      x[rowCL[i]] += value[i][j];
}


double DenseMatrix::computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM)
{
  double temp = 0;
  for (int r = 0; r < numRow; r++)
    for (int c = 0; c < numCol; c++){
      double tempValue = value[r][c] - cM[rowCL[r]][colCL[c]];
      temp += tempValue * tempValue;
    }
  return temp;
}  


double DenseMatrix::computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, bool *isReversed)
{
  double temp = 0;
  for (int r = 0; r < numRow; r++){
    if (isReversed[r])
      for (int c = 0; c < numCol; c++){
        double tempValue = (0 - value[r][c]) - cM[rowCL[r]][colCL[c]];
        temp += tempValue * tempValue;
      }
    else
      for (int c = 0; c < numCol; c++){
        double tempValue = value[r][c] - cM[rowCL[r]][colCL[c]];
        temp += tempValue * tempValue;
      }
  }
  return temp;
}  


double DenseMatrix::computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, double **rowCentroid, double **colCentroid)
{
  double temp = 0;
  for (int r = 0; r < numRow; r++)
    for (int c = 0; c < numCol; c++){
      double tempValue = value[r][c] - rowCentroid[rowCL[r]][c] - colCentroid[colCL[c]][r] + cM[rowCL[r]][colCL[c]];
      temp += tempValue * tempValue;
    }
  return temp;
}  


double DenseMatrix::computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, double **rowCentroid, double **colCentroid, bool *isReversed)
{
  double temp = 0;
  for (int r = 0; r < numRow; r++){
    if (isReversed[r])
      for (int c = 0; c < numCol; c++){
        double tempValue = (0 - value[r][c]) - rowCentroid[rowCL[r]][c] - colCentroid[colCL[c]][r] + cM[rowCL[r]][colCL[c]];
        temp += tempValue * tempValue;
      }
    else
      for (int c = 0; c < numCol; c++){
        double tempValue = value[r][c] - rowCentroid[rowCL[r]][c] - colCentroid[colCL[c]][r] + cM[rowCL[r]][colCL[c]];
        temp += tempValue * tempValue;
      }
  }
  return temp;
}  


double DenseMatrix::computeObjectiveFunctionValue4RowCluster(int *rowCL, double **rowCentroid)
{
  double temp = 0, tempValue = 0;
  for (int r = 0; r < numRow; r++)
    for (int c = 0; c < numCol; c++){
      tempValue = value[r][c] - rowCentroid[rowCL[r]][c];
      temp += tempValue * tempValue;
    }
  return temp;
}


double DenseMatrix::computeObjectiveFunctionValue4ColCluster(int *colCL, double **colCentroid)
{
  double temp = 0, tempValue = 0;
  for (int r = 0; r < numRow; r++)
    for (int c = 0; c < numCol; c++){
      tempValue = value[r][c] - colCentroid[colCL[c]][r];
      temp += tempValue * tempValue;
    }
  return temp;
}
