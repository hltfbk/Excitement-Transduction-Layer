/* 
  SparseMatrix.cc
    Implementation of the SparseMatrix class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#include <iostream>
#include <cmath>
#include <assert.h>
#include <stdlib.h>

#include "SparseMatrix.h"


SparseMatrix::SparseMatrix(int row, int col, int nz, double *val, int *rowind, int *colptr) : Matrix(row, col)
{
  numRow = row;
  numCol = col;
  numVal  = nz;
  value = val;
  colPtr = colptr;
  rowIdx = rowind;
}


SparseMatrix::~SparseMatrix()
{
  if (norm != NULL)
    delete[] norm;
  if (L1_norm != NULL)
    delete[] L1_norm;
}


double SparseMatrix::operator()(int i, int j) const
{
  assert(i >= 0 && i < numRow && j >= 0 && j < numCol);
  for (int t = colPtr[j]; t < colPtr[j+1]; t++)
    if (rowIdx[t] == i) 
      return value[t];
  return 0.0;
}


double SparseMatrix::dot_mult(double *v, int i)
  /*compute the dot-product between the ith vector (in the sparse matrix) with vector v
    result is returned.
   */
{
  double result = 0.0;
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    result += value[j] * v[rowIdx[j]];
  return result;
}


void SparseMatrix::trans_mult(double *x, double *result) 
  /*compute the dot-product between every vector (in the sparse matrix) with vector v
    results are stored in array 'result'.
   */
{
  for (int i = 0; i < numCol; i++)
    result[i] = dot_mult(x, i);
}


double SparseMatrix::squared_dot_mult(double *v, int i)
  /*compute the dot-product between the ith vector (in the sparse matrix) with vector v
    result is returned.
   */
{
  double result = 0.0;
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    result += value[j] * v[rowIdx[j]];
  return result * result;
}


void SparseMatrix::squared_trans_mult(double *x, double *result) 
  /*compute the dot-product between every vector (in the sparse matrix) with vector v
    results are stored in array 'result'.
   */
{
  for (int i = 0; i < numCol; i++)
    result[i] = squared_dot_mult(x, i);
}


void SparseMatrix::A_trans_A(int flag, int * index, int *pointers, double **A_t_A, int & nz_counter)
/* computes A'A given doc_IDs in the same cluster; index[] contains doc_IDs for all docs, 
     pointers[] gives the range of doc_ID belonging to the same cluster;
     the resulting matrix is stored in dense format A_t_A( d by d matrix)
     The right SVs of A are the corresponding EVs of A'A 
     Notice that Gene expression matrix is usually n by d where n is #genes; this matrix
     format is different from that of document matrix. 
     In the main memory the matrix is stored in the format of document matrix.
  */
{
  int clustersize = pointers[1] - pointers[0]; 
  for (int i = 0; i < numRow; i++)
    for (int j = 0; j < numRow; j++)
      A_t_A[i][j] = 0;
  nz_counter = 0;
  if (flag > 0){
    for (int k = 0; k < clustersize; k++)
      for (int l = colPtr[index[k+pointers[0]]]; l < colPtr[index[k+pointers[0]]+1]; l++)
        for (int j = colPtr[index[k+pointers[0]]]; j < colPtr[index[k+pointers[0]]+1]; j++)
	  A_t_A[rowIdx[l]][rowIdx[j]] += value[l] * value[j];
  } else {
    for (int k = 0; k < clustersize; k++)
      for (int l = colPtr[k+pointers[0]]; l < colPtr[k+pointers[0]+1]; l++)
        for (int j = colPtr[k+pointers[0]]; j < colPtr[k+pointers[0]+1]; j++)
          A_t_A[rowIdx[l]][rowIdx[j]] += value[l] * value[j];
  }
  for (int i = 0; i < numRow; i++)
    for (int j = 0; j < numRow; j++)
      if (A_t_A[i][j] > 0)
        nz_counter++;
}


void SparseMatrix::dense_2_sparse(int* AtA_colptr, int *AtA_rowind, double *AtA_val, double **A_t_A)
{
  int k = 0;
  AtA_colptr[0] = 0;
  for (int j = 0; j < numRow; j++){
    int l = 0;
    for (int i = 0; i < numRow; i++)
      if (A_t_A[i][j] > 0){
        AtA_val[k] = A_t_A[i][j];
        AtA_rowind[k++] = i;
        l++;
      }
    AtA_colptr[j+1] = AtA_colptr[j] + l;
  }
}


void SparseMatrix::right_dom_SV(int *cluster, int *cluster_size, int n_Clusters, double ** CV, double *cluster_quality, int flag)
  /* 
     flag == -1, then update all the centroids and the cluster qualities; 
     0 <= flag < n_Clusters , then update the centroid and the cluster quality specified by 'flag';
     n_Clusters <= flag <2*n_Clusters, then update the cluster quality specified by 'flag-n_Clusters';
  */
{
  int *pointer = new int[n_Clusters+1], *index = new int[numCol], *range =new int[2], nz_counter;
  int *AtA_rowind = NULL, *AtA_colptr = NULL;
  double *AtA_val = NULL;
  double **A_t_A;
  A_t_A =new double *[numRow];
  for (int i = 0; i < numRow; i++)
    A_t_A[i] = new double[numRow];
  pointer[0] = 0;
  for (int i = 1; i < n_Clusters; i++)
    pointer[i] = pointer[i-1] + cluster_size[i-1];
  for (int i = 0; i < numCol; i++){
    index[pointer[cluster[i]]] = i;
    pointer[cluster[i]]++;
  }
  pointer[0] = 0;
  for (int i = 1; i <= n_Clusters; i++)
    pointer[i] = pointer[i-1] + cluster_size[i-1];
  if (flag <0){ // compute eigval and eigvec for all clusters
    for (int i = 0; i < n_Clusters; i++){
      range[0] = pointer[i];
      range[1] = pointer[i+1];
      A_trans_A(1, index, range, A_t_A, nz_counter);
      AtA_val = new double [nz_counter];
      AtA_rowind = new int [nz_counter];
      AtA_colptr = new int [numRow+1];
      dense_2_sparse(AtA_colptr, AtA_rowind, AtA_val, A_t_A);
 /*
      for (int l = 0; l < numRow; l++){
        for (int j = 0; j < numRow; j++)
          cout << A_t_A[l][j] << " ";
        cout<<endl;
      }
      for (int l = 0; l <= numRow; l++)
        cout << AtA_colptr[l] << " ";
      cout << endl;
      for (int l = 0; l < nz_counter; l++)
        cout << AtA_rowind[l] << " ";
      cout << endl;
      for (int l = 0; l < nz_counter; l++)
        cout << AtA_val[l] << " ";
      cout<<endl<<endl;
 */
      power_method(AtA_val, AtA_rowind, AtA_colptr, numRow, CV[i], CV[i], cluster_quality[i]);
/*
      for (int l = 0; l < numRow; l++)
        cout<<CV[i][l]<<" ";
      cout<<cluster_quality[i]<<endl;
*/	  
      delete [] AtA_val;
      delete [] AtA_rowind;
      delete [] AtA_colptr;
    }
     for (int i = 0; i < numRow; i++)
	delete [] A_t_A[i];
     delete [] A_t_A;
  } else if ((flag >= 0) && (flag < n_Clusters)){ //compute eigval and eigvec for cluster 'flag'
    range[0] = pointer[flag];
    range[1] = pointer[flag+1];
    A_trans_A(1, index, range, A_t_A, nz_counter);
    AtA_val = new double[nz_counter];
    AtA_rowind = new int[nz_counter];
    AtA_colptr = new int[numRow+1];
    dense_2_sparse(AtA_colptr, AtA_rowind, AtA_val, A_t_A);
    for (int i = 0; i < numRow; i++)
      delete [] A_t_A[i];
    delete [] A_t_A;
    power_method(AtA_val, AtA_rowind, AtA_colptr, numRow, CV[flag], CV[flag], cluster_quality[flag]);
    delete [] AtA_val;
    delete [] AtA_rowind;
    delete [] AtA_colptr;
  } else if ((flag >= n_Clusters) && (flag <= 2 * n_Clusters)){
    // compute eigval ONLY for cluster 'flag-n_Clusters', eigvec for  cluster 'flag-n_Clusters' no change
    range[0] = pointer[flag-n_Clusters];
    range[1] = pointer[flag-n_Clusters+1];
    A_trans_A(1, index, range, A_t_A, nz_counter);
    AtA_val = new double [nz_counter];
    AtA_rowind = new int [nz_counter];
    AtA_colptr = new int [numRow+1];
    dense_2_sparse(AtA_colptr, AtA_rowind, AtA_val, A_t_A);
    for (int i = 0; i < numRow; i++)
    delete [] A_t_A[i];
    delete [] A_t_A;
    power_method(AtA_val, AtA_rowind, AtA_colptr, numRow, NULL, CV[flag-n_Clusters], cluster_quality[flag-n_Clusters]);
    delete [] AtA_val;
    delete [] AtA_rowind;
    delete [] AtA_colptr;
  }
  delete [] pointer;
  delete [] index;
  delete [] range;
}


double SparseMatrix::euc_dis(double *v, int i, double norm_v)
  /* Given L2-norms of the vecs and v, norm[i] and norm_v,
     compute the squared Euc-dis between ith vec in the matrix and v,
     result is returned.
     Take advantage of (x-c)^T (x-c) = x^T x - 2 x^T c + c^T c
  */
{
  double result = 0.0;
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    result += value[j] * v[rowIdx[j]];
  result *= -2.0;
  result += norm[i]+norm_v;
  return result;
}


void SparseMatrix::euc_dis(double *x, double norm_x, double *result)
  /* Given L2-norms of the vecs and x, norm[i] and norm_x,
     compute the squared Euc-dis between each vec in the matrix with x,  
     results are stored in array 'result'. 
     Take advantage of (x-c)^T (x-c) = x^T x - 2 x^T c + c^T c
  */
{
  for (int i = 0; i < numCol; i++)
    result[i] = euc_dis(x, i, norm_x);
}


double SparseMatrix::Kullback_leibler(double *x, int i, int laplace, double L1norm_x)
  /* Given the L1_norms of vec[i] and x, (vec[i] need be normalized before function-call
     compute KL divergence between vec[i] in the matrix with x,
     result is returned. 
     Take advantage of KL(p, q) = \sum_i p_i log(p_i) - \sum_i p_i log(q_i) = norm[i] - \sum_i p_i log(q_i)
     the KL is in unit of nats NOT bits.
  */
{
  double result = 0.0, row_inv_alpha = smoothingFactor * L1norm_x / numRow, m_e = smoothingFactor * L1norm_x;
  if (priors[i] > 0){
    switch (laplace){
      case NO_SMOOTHING:
        for (int j = colPtr[i]; j < colPtr[i+1]; j++){
	  double tempValue = x[rowIdx[j]];
          if(tempValue > 0.0)
            result += value[j] * log(tempValue);
          else
            return MY_DBL_MAX; // if KL(vec[i], x) is inf. give it a huge number 1.0e20
        }
        result = norm[i] - result + log(L1norm_x);
	break;
      case UNIFORM_SMOOTHING:
        // this vector alpha is alpha (given by user) divided by |Y|,
        //row_inv_alpha is to make computation faster
        for (int j = colPtr[i]; j < colPtr[i+1]; j++)
           result += value[j] * log(x[rowIdx[j]] + row_inv_alpha);
        result = norm[i]-result+log((1+smoothingFactor)*L1norm_x);
	break;
      case MAXIMUM_ENTROPY_SMOOTHING:
        // this vector alpha is alpha (given by user) divided by |Y|,
        //row_inv_alpha is to make computation faster
        for (int j = colPtr[i]; j < colPtr[i+1]; j++)
          result += value[j] * log(x[rowIdx[j]] + m_e * p_x[rowIdx[j]]);
	result = norm[i]-result+log((1+smoothingFactor)*L1norm_x);
	break;
      case LAPLACE_SMOOTHING:			// not used in co-clustering...
        // this vector alpha is alpha (given by user) divided by |X|*|Y|,
        //row_alpha is its L1-norm
        for (int j = colPtr[i]; j < colPtr[i+1]; j++)
          result += (value[j]+row_inv_alpha) * log(x[rowIdx[j]]) ;
        result = norm[i]-result/(1+smoothingFactor);
        break;
      default:
        break;
    }
  }
  return result;
}


void SparseMatrix::Kullback_leibler(double *x, double *result, int laplace, double L1norm_x)
  // Given the KL-norm of the vecs, norm[i] (already considered prior),
  //   compute KL divergence between each vec in the matrix with x,
  //   results are stored in array 'result'. 
  //   Take advantage of KL(p, q) = \sum_i p_i log(p_i) - \sum_i p_i log(q_i) = norm[i] - \sum_i p_i log(q_i)
  
{
  for (int i = 0; i < numCol; i++)
    result[i] = Kullback_leibler(x, i, laplace, L1norm_x);  
}


double SparseMatrix::Kullback_leibler(double *x, int i, int laplace)
  /* Given the L1_norms of vec[i] and x, (vec[i] and x need be normalized before function-call
     compute KL divergence between vec[i] in the matrix with x,
     result is returned. 
     Take advantage of KL(p, q) = \sum_i p_i log(p_i) - \sum_i p_i log(q_i) = norm[i] - \sum_i p_i log(q_i)
     the KL is in unit of nats NOT bits.
  */
{
  double result = 0.0, row_inv_alpha = smoothingFactor / numRow, m_e = smoothingFactor;
  if (priors[i] > 0){
    switch (laplace){
      case NO_SMOOTHING:
        for (int j = colPtr[i]; j < colPtr[i+1]; j++){
          if (x[rowIdx[j]] > 0.0)
            result += value[j] * log(x[rowIdx[j]]);
          else
            return MY_DBL_MAX; // if KL(vec[i], x) is inf. give it a huge number 1.0e20
        }
        result = norm[i] - result;
	break;
      case UNIFORM_SMOOTHING:
        // this vector alpha is alpha (given by user) divided by |Y|,
        //row_inv_alpha is to make computation faster
        for (int j = colPtr[i]; j < colPtr[i+1]; j++)
          result += value[j] * log(x[rowIdx[j]] + row_inv_alpha);
        result = norm[i]-result+log(1+smoothingFactor);
        break;
      case MAXIMUM_ENTROPY_SMOOTHING:
        // this vector alpha is alpha (given by user) divided by |Y|,
        //row_inv_alpha is to make computation faster
        for (int j = colPtr[i]; j < colPtr[i+1]; j++)
          result += value[j] * log(x[rowIdx[j]] + m_e * p_x[rowIdx[j]]);
        result = norm[i]-result+log(1+smoothingFactor);
        break;
      case LAPLACE_SMOOTHING:
        // this vector alpha is alpha (given by user) divided by |X|*|Y|,
        //row_alpha is its L1-norm
        for (int j = colPtr[i]; j < colPtr[i+1]; j++)
          result += (value[j] + row_inv_alpha) * log(x[rowIdx[j]]) ;
        result = norm[i]-result/(1+smoothingFactor);
        break;
      default:
        break;
    }
  }
  return result;
}


void SparseMatrix::Kullback_leibler(double *x, double *result, int laplace)
  // Given the KL-norm of the vecs, norm[i] (already considered prior),
  //   compute KL divergence between each vec in the matrix with x,
  //   results are stored in array 'result'. 
  //   Take advantage of KL(p, q) = \sum_i p_i log(p_i) - \sum_i p_i log(q_i) = norm[i] - \sum_i p_i log(q_i)
  
{
  for (int  i = 0; i < numCol; i++)
    result[i] = Kullback_leibler(x, i, laplace);  
}


double SparseMatrix::Jenson_Shannon(double *x, int i, double prior_x)
  /* Given the prior of vec[i],
     compute JS divergence between vec[i] in the data matrix with x,
     result in nats is returned. 
  */
{
  double result = 0.0, *p_bar, p1, p2;
  if ((priors[i] > 0) && (prior_x > 0)){
    p1 = priors[i] / (priors[i] + prior_x);
    p2 = prior_x / (priors[i] + prior_x);
    p_bar = new double[numRow];
    for (int j = 0; j < numRow; j++)
      p_bar[j] = p2 * x[j];
    for (int j = colPtr[i]; j < colPtr[i+1]; j++)
      p_bar[rowIdx[j]] += p1 * value[j];
    result = p1 * Kullback_leibler(p_bar, i, NO_SMOOTHING) + ::Kullback_leibler(x, p_bar, numRow) * p2; 
    delete [] p_bar;
  }
  return result; // the real JS value should be this result devided by L1_norm[i]+l1n_x
}


void SparseMatrix::Jenson_Shannon(double *x, double *result, double prior_x)
  /* Given the prior of vec[i] and x; vec[i] and x are all normalized
     compute JS divergence between all vec[i] in the data matrix with x,
     result in nats. 
  */
{  
  for (int i = 0; i < numCol; i++)
    result[i] = Jenson_Shannon(x, i, prior_x);
}


void SparseMatrix::computeNorm_2()
  /* compute the squared L-2 norms for each vec in the matrix
     first check if array 'norm' has been given memory space
   */
{
  if (norm == NULL){
    norm = new double[numCol];
    memoryUsed += numCol * sizeof(double);
  }
  for (int i = 0; i < numCol; i++){
    norm[i] = 0.0;
    for (int j = colPtr[i]; j < colPtr[i+1]; j++){
      double tempValue = value[j];
      norm[i] += tempValue * tempValue;
    }
  }
}


void SparseMatrix::computeNorm_1()
  /* compute the squared L-2 norms for each vec in the matrix
     first check if array 'norm' has been given memory space
   */
{
  if (L1_norm == NULL){
     L1_norm = new double[numCol];
     memoryUsed += numCol * sizeof(double);
  }
  for (int i = 0; i < numCol; i++){
    L1_norm[i] =0.0;
    for (int j = colPtr[i]; j < colPtr[i+1]; j++)
      L1_norm[i] += value[j] ;
  }
}


void SparseMatrix::computeNorm_KL(int laplace)
  // the norm[i] is in unit of nats NOT bits
{
  double row_inv_alpha = smoothingFactor / numRow;
  if (norm == NULL){
    norm = new double[numCol];
    memoryUsed += numCol * sizeof(double);
  }
  Norm_sum = 0;
  switch (laplace){
    case NO_SMOOTHING:
    case UNIFORM_SMOOTHING:
      for (int i = 0; i < numCol; i++){
        norm[i] = 0.0;
        for (int j = colPtr[i]; j < colPtr[i+1]; j++){
	  double tempValue = value[j];
          norm[i] += tempValue * log(tempValue);
	}
        Norm_sum += norm[i] * priors[i];
      }
      break;
    case MAXIMUM_ENTROPY_SMOOTHING:
      for (int i = 0; i < numCol; i++){
        norm[i] = 0.0;
        for (int j = colPtr[i]; j < colPtr[i+1]; j++){
	  double tempValue = value[j];
          norm[i] += tempValue * log(tempValue);
	}
        Norm_sum +=norm[i] * priors[i];
      }
      if (p_x == NULL){
        p_x = new double[numRow];
	memoryUsed += numRow * sizeof(double);
      }
      for (int i = 0; i < numRow; i++)
        p_x[i] = 0;
      for (int i = 0; i < numCol; i++)
        for (int j = colPtr[i]; j < colPtr[i+1]; j++)
          p_x[rowIdx[j]] += priors[i] * value[j];
      break;
    case LAPLACE_SMOOTHING:
      for (int i = 0; i < numCol; i++){
        norm[i] =0.0;
        for (int j = colPtr[i]; j < colPtr[i+1]; j++){
	  double tempValue = value[j];
          norm[i] += (tempValue + row_inv_alpha) * log(tempValue + row_inv_alpha);
	}
        norm[i] += (numRow - (colPtr[i+1] - colPtr[i])) * row_inv_alpha * log(row_inv_alpha) ;
        norm[i] = norm[i] / (1+smoothingFactor) + log(1+smoothingFactor);
        Norm_sum += norm[i] * priors[i];
      }
    default:
      break;
  }
  Norm_sum /= log(2.0);
}


void SparseMatrix::normalize_mat_L2()
  /* compute the L_2 norms for each vec in the matrix and L_2-normalize them
     first check if array 'norm' has been given memory space
   */
{
  for (int i = 0; i < numCol; i++){
    double norm = 0.0;
    for (int j = colPtr[i]; j < colPtr[i+1]; j++){
      double tempValue = value[j];
      norm += tempValue * tempValue;
    }
    if( norm > 0.0){
      norm = sqrt(norm);
      for (int j = colPtr[i]; j < colPtr[i+1]; j++)
        value[j] /= norm;
    }
  }
}


void SparseMatrix::normalize_mat_L1()
  /* compute the L_1 norms for each vec in the matrix and L_1-normalize them
     first check if array 'L1_norm' has been given memory space
   */
{
  for (int i = 0; i < numCol; i++){
    double norm = 0.0;
    for (int j = colPtr[i]; j < colPtr[i+1]; j++)
      norm += fabs(value[j]);
    if(norm > 0)
      for (int j = colPtr[i]; j < colPtr[i+1]; j++)
         value[j] /= norm;
  }
}


void SparseMatrix::ith_add_CV(int i, double *CV)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    CV[rowIdx[j]] += value[j];
}


void SparseMatrix::ith_add_CV_prior(int i, double *CV)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    CV[rowIdx[j]] += priors[i] * value[j];
}


void SparseMatrix::CV_sub_ith(int i, double *CV)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    CV[rowIdx[j]] -= value[j];
}


void SparseMatrix::CV_sub_ith_prior(int i, double *CV)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    CV[rowIdx[j]] -= priors[i]*value[j];
}


double SparseMatrix::computeMutualInfo()
{
  double *rowSum= new double[numRow], MI = 0.0;
  for (int i = 0; i < numRow; i++)
    rowSum[i] = 0.0;
  for (int i = 0; i < numCol; i++)
    for (int j = colPtr[i]; j < colPtr[i+1]; j++)
      rowSum[rowIdx[j]] += value[j] * priors[i];
  for (int i = 0; i < numCol; i++){
    double temp = 0;
    for (int j = colPtr[i]; j < colPtr[i+1]; j++){
      double tempValue = value[j];
      temp += tempValue * log(tempValue / rowSum[rowIdx[j]]);
    }
    MI += temp * priors[i];
  }
  delete [] rowSum;
  return MI / log(2.0);
}


double SparseMatrix::exponential_kernel(double *v, int i, double norm_v, double sigma_squared)
  // this function computes the exponential kernel distance between i_th data with the centroid v
{
  double result = 0.0;
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    result += value[j] * v[rowIdx[j]];
  result *= -2.0;
  result += norm[i] + norm_v;
  return exp(result * 0.5 / sigma_squared);
}


void SparseMatrix::exponential_kernel(double *x, double norm_x, double *result, double sigma_squared)
  //this function computes the exponential kernel distance between all data with the centroid x
{
  for (int i = 0; i < numCol; i++)
    result[i] = exponential_kernel(x, i, norm_x, sigma_squared);
}


double SparseMatrix::i_j_dot_product(int i, int j)
//this function computes  dot product between vectors i and j
{
  double result = 0.0;
  if (i == j)
    for (int l = colPtr[i]; l < colPtr[i+1]; l++){
      double tempValue = value[l];
      result += tempValue * tempValue;
    }
  else
    for (int l = colPtr[i]; l < colPtr[i+1]; l++)
      for (int k = colPtr[j]; k < colPtr[j+1]; k++)
	if(rowIdx[l] == rowIdx[k])
	  result += value[l] * value[k];
  return result;
}


double SparseMatrix::squared_i_j_euc_dis(int i, int j)
{
  return (norm[i] - 2.0 * i_j_dot_product(i,j) + norm[j]);
}


void SparseMatrix::pearson_normalize()
  //this function shift each vector so that it has 0 mean of all its entries
  //then the vector gets L2 normalized.
{
  for (int i = 0; i < numCol; i++){
    double mean = 0.0;
    for (int j = colPtr[i]; j < colPtr[i+1]; j++)
      mean += value[j];
    mean /= numRow;
    for (int j = colPtr[i]; j < colPtr[i+1]; j++)
      value[j] -= mean;
  }
  normalize_mat_L2();
}


bool SparseMatrix::isHavingNegative()
{
  bool havingNegative = false;
  for (int c = 0; c < numCol && !havingNegative; c++)
    for (int k = colPtr[c]; k < colPtr[c+1] && !havingNegative; k++)
      if (value[k] < 0)
        havingNegative = true;
  return havingNegative;
}


double SparseMatrix::getPlogQ(double **pxhatyhat, int *rowCL, int *colCL, double *pXhat, double *pYhat)
{

  double PlogQ = 0;
  for (int i = 0; i < numCol; i++){
    int colID = colCL[i];
    for (int j = colPtr[i]; j < colPtr[i+1]; j++)
      PlogQ += value[j] * log(pxhatyhat[rowCL[rowIdx[j]]][colID] * pX[rowIdx[j]]* pY[i] / (pXhat[rowCL[rowIdx[j]]] * pYhat[colID]));
  }
  return PlogQ / log(2.0);
}


void SparseMatrix::preprocess()
{
  double totalSum = 0;
  PlogP = mutualInfo = 0;
  pX = new double[numRow];
  pY = new double[numCol];
  for (int i = 0; i < numRow; i++)
    pX[i] = 0;
  for (int j = 0; j < numCol; j++)
    pY[j] = 0;
  //cout<<numRow<<" "<<numCol<<endl;
  for (int i = 0; i < numCol; i++)
    for (int j = colPtr[i]; j < colPtr[i+1]; j++){
      totalSum += value[j];
      pY[i] += value[j];
      pX[rowIdx[j]] += value[j];
    }
  //cout<<"ts="<<totalSum<<endl;
  for (int i = 0; i < numCol; i++)
    for (int j = colPtr[i]; j < colPtr[i+1]; j++)
      value[j] /= totalSum;
  for (int i = 0; i < numRow; i++)
    pX[i] /= totalSum;
  for (int j = 0; j < numCol; j++)
    pY[j] /= totalSum;
  norm = new double[numCol];
  for (int i = 0; i < numCol; i++){
    norm[i] = 0;
    for (int j = colPtr[i]; j < colPtr[i+1]; j++){
      double tempValue = value[j];
      norm[i] += tempValue*log(tempValue);
      mutualInfo += tempValue*log(pX[rowIdx[j]] * pY[i]);
    }
    PlogP += norm[i];
  }
  mutualInfo = PlogP - mutualInfo;
}
	  	  

void SparseMatrix::condenseMatrix(int *rowCL, int *colCL, int numRowCluster, int numColCluster, double **cM)
{
  for (int i = 0; i < numRowCluster; i++)
    for (int j = 0; j < numColCluster; j++)
      cM[i][j] = 0;
  for (int i = 0; i < numCol; i++)
    for (int j = colPtr[i]; j < colPtr[i+1]; j++)
      cM[rowCL[rowIdx[j]]][colCL[i]] += value[j]; 
}


void SparseMatrix::condenseMatrix(int *rowCL, int *colCL, int numRowCluster, int numColCluster, double **cM, bool *isReversed)
{
  for (int i = 0; i < numRowCluster; i++)
    for (int j = 0; j < numColCluster; j++)
      cM[i][j] = 0;
  for (int i = 0; i < numCol; i++)
    for (int j = colPtr[i]; j < colPtr[i+1]; j++){
      int tempRowId = rowIdx[j];
      if (isReversed[tempRowId])
        cM[rowCL[tempRowId]][colCL[i]] += (-1) * value[j]; 
      else
        cM[rowCL[tempRowId]][colCL[i]] += value[j]; 
    }  
}


double SparseMatrix::Kullback_leibler(double *x, int i, int priorType, int clusterDimension)
// In fact, clusteringDimension is not used in the body...
{
  double result = 0.0, uni_alpha = smoothingFactor / numRow;
  switch (priorType){
    case NO_SMOOTHING:
      for (int j = colPtr[i]; j < colPtr[i+1]; j++){
        double tempValue = x[rowIdx[j]];
        if (tempValue > 0.0)
          result += value[j] * log(tempValue);
        else
          return MY_DBL_MAX; // if KL(vec[i], x) is inf. give it DBL_MAX
      }
      result = (norm[i] - result) / pY[i] - log(pY[i]);
      break;
    case UNIFORM_SMOOTHING:
      for (int j = colPtr[i]; j < colPtr[i+1]; j++)
        result += value[j] * log(x[rowIdx[j]] + uni_alpha);
      result = (norm[i] - result) / pY[i] - log(pY[i]) + log(1+smoothingFactor);
      break;
    case MAXIMUM_ENTROPY_SMOOTHING:
      for (int j = colPtr[i]; j < colPtr[i+1]; j++){
        int tempRowId = rowIdx[j];
        result += value[j] * log(x[tempRowId] + pX[tempRowId] * smoothingFactor) ;
      }
      result = (norm[i] - result)/ pY[i] - log(pY[i]) + log(1+smoothingFactor);
      break;
    default:
      break;
  }
//cout << "SMOOTHING_FACTOR = " << smoothingFactor << endl;  
  return result;
}


void SparseMatrix::subtractRow(double **x, int row, int i, int *colCL)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    x[row][colCL[rowIdx[j]]] -= value[j];
}


void SparseMatrix::subtractRow(double *x, int i, int *colCL)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    x[colCL[rowIdx[j]]] -= value[j];
}


void SparseMatrix::addRow(double **x, int row, int i, int *colCL)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    x[row][colCL[rowIdx[j]]] += value[j];
}


void SparseMatrix::addRow(double *x, int i, int *colCL)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    x[colCL[rowIdx[j]]] += value[j];
}


void SparseMatrix::subtractCol(double **x, int col, int i, int *rowCL)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    x[rowCL[rowIdx[j]]][col] -= value[j];
}


void SparseMatrix::subtractCol(double *x, int i, int *rowCL)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    x[rowCL[rowIdx[j]]] -= value[j];
}


void SparseMatrix::addCol(double **x, int col, int i, int *rowCL)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    x[rowCL[rowIdx[j]]][col] += value[j];
}


void SparseMatrix::addCol(double *x, int i, int *rowCL)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++)
    x[rowCL[rowIdx[j]]] += value[j];
}


double SparseMatrix::squaredFNorm()
  /* compute the squared Frobenius norm for the matrix.
   */
{
  double temp = 0;
  for (int c = 0; c < numCol; c++)
    for (int k = colPtr[c]; k < colPtr[c+1]; k++){
      double tempValue = value[k];
      temp += tempValue * tempValue;
    }
  return temp;
}


double SparseMatrix::squaredL2Norm4Row(int r)
  /* compute the squared L-2 norm for row vec i in the matrix.
   */
{
  double temp = 0;
  for (int k = colPtr[r]; k < colPtr[r+1]; k++){
    double tempValue = value[k];
    temp += tempValue * tempValue;
  }
  return temp;
}


double SparseMatrix::squaredL2Norm4Col(int c)
  /* compute the squared L-2 norm for column vec j in the matrix.
   */
{
  double temp = 0;
  for (int k = colPtr[c]; k < colPtr[c+1]; k++){
    double tempValue = value[k];
    temp += tempValue * tempValue;
  }
  return temp;
}


double SparseMatrix::computeRowDistance(int rowId, int rowCluster, int *colCL, double **cM, double rowQuality4Compressed)
{
  double temp = 0;
  for (int k = colPtr[rowId]; k < colPtr[rowId+1]; k++)
    temp += value[k] * cM[rowCluster][colCL[rowIdx[k]]];
  return (-2 * temp + rowQuality4Compressed);
}


double SparseMatrix::computeColDistance(int colId, int colCluster, int *rowCL, double **cM, double colQuality4Compressed)
{
  double temp = 0;
  for (int k = colPtr[colId]; k < colPtr[colId+1]; k++)
    temp += value[k] * cM[rowCL[rowIdx[k]]][colCluster];
  return (-2 * temp + colQuality4Compressed);
}


double SparseMatrix::computeRowDistance(int rowId, int rowCluster, int *colCL, double **cM, double rowQuality4Compressed, bool *isReversed)
{
  double temp = 0;
  if (isReversed[rowId])
    for (int k = colPtr[rowId]; k < colPtr[rowId+1]; k++)
      temp += (0 - value[k]) * cM[rowCluster][colCL[rowIdx[k]]];
  else
    for (int k = colPtr[rowId]; k < colPtr[rowId+1]; k++)
      temp += value[k] * cM[rowCluster][colCL[rowIdx[k]]];
  return (-2 * temp + rowQuality4Compressed);
}


double SparseMatrix::computeColDistance(int colId, int colCluster, int *rowCL, double **cM, double colQuality4Compressed, bool *isReversed)
{
  double temp = 0;
  for (int k = colPtr[colId]; k < colPtr[colId+1]; k++){
    int rowId = rowIdx[k];
    if (isReversed[rowId])
      temp += (0 - value[k]) * cM[rowCL[rowId]][colCluster];
    else
      temp += value[k] * cM[rowCL[rowId]][colCluster];
  }
  return (-2 * temp + colQuality4Compressed);
}


// MSSRIICC
void SparseMatrix::computeRowCentroid(int numRowCluster, int *rowCL, double **rowCentroid)
{
  for (int rc = 0; rc < numRowCluster; rc++)
    for (int c = 0; c < numRow; c++)		// numRow (in CRS) equals to numCol (in CCS).
      rowCentroid[rc][c] = 0;
  for (int r = 0; r < numCol; r++)		// numCol (in CRS) equals to numRow (in CCS).
    for (int k = colPtr[r]; k < colPtr[r+1]; k++)
      rowCentroid[rowCL[r]][rowIdx[k]] += value[k];
}


void SparseMatrix::computeRowCentroid(int numRowCluster, int *rowCL, double **rowCentroid, bool *isReversed)
{
  for (int rc = 0; rc < numRowCluster; rc++)
    for (int c = 0; c < numRow; c++)		// numRow (in CRS) equals to numCol (in CCS).
      rowCentroid[rc][c] = 0;
  for (int r = 0; r < numCol; r++)		// numCol (in CRS) equals to numRow (in CCS).
    if (isReversed[r])
      for (int k = colPtr[r]; k < colPtr[r+1]; k++)
        rowCentroid[rowCL[r]][rowIdx[k]] -= value[k];
    else
      for (int k = colPtr[r]; k < colPtr[r+1]; k++)
        rowCentroid[rowCL[r]][rowIdx[k]] += value[k];
}


void SparseMatrix::computeColCentroid(int numColCluster, int *colCL, double **colCentroid)
{
  for (int cc = 0; cc < numColCluster; cc++)
    for (int r = 0; r < numRow; r++)
      colCentroid[cc][r] = 0;
  for (int c = 0; c < numCol; c++)
    for (int k = colPtr[c]; k < colPtr[c+1]; k++)
      colCentroid[colCL[c]][rowIdx[k]] += value[k];
}


void SparseMatrix::computeColCentroid(int numColCluster, int *colCL, double **colCentroid, bool *isReversed)
{
  for (int cc = 0; cc < numColCluster; cc++)
    for (int r = 0; r < numRow; r++)
      colCentroid[cc][r] = 0;
  for (int c = 0; c < numCol; c++)
    for (int k = colPtr[c]; k < colPtr[c+1]; k++){
      int tempRowId = rowIdx[k];
      if (isReversed[tempRowId])
        colCentroid[colCL[c]][tempRowId] -= value[k]; 
      else
        colCentroid[colCL[c]][tempRowId] += value[k];
    }
}


/*
double SparseMatrix::computeRowDistance(int rowId, int *colCL, double *, double *mAR, double rowQuality4Compressed) 
{
  double temp = 0;
  for (int j = colPtr[rowId]; j < colPtr[rowId+1]; j++){
    int tempColId = rowIdx[j];
    temp += (value[j] - colCentroid[colCL[tempColId]]) * mAR[tempColId];
  }
  return (-2 * temp + rowQuality4Compressed);
}


double SparseMatrix::computeColDistance(int colId, int *rowCL, double *rowCentroid, double *nAC, double colQuality4Compressed)
{
  double temp = 0;
  for (int j = colPtr[colId]; j < colPtr[colId+1]; j++){
    int tempRowId = rowIdx[j];
    temp += (value[j] - rowCentroid[tempRowId]) * nAC[tempRowId];
  }
  return (-2 * temp + colQuality4Compressed);
}
*/


double SparseMatrix::computeRowDistance(int rowId, int rowCluster, int *rowCL, int *colCL, double **cM)
{
  double temp = 0;
  double *tempDistance = new double[numRow];		// numRow (in CRS) equals to numCol (in CCS).
  for (int c = 0; c < numRow; c++)
    tempDistance[c] = 0;
  for (int k = colPtr[rowId]; k < colPtr[rowId+1]; k++)
    tempDistance[rowIdx[k]] = value[k];
  for (int c = 0; c < numRow; c++){
    double tempValue = tempDistance[c] - cM[rowCluster][colCL[c]];
    temp += tempValue * tempValue;
  }  
  delete [] tempDistance;
  return temp;
}


double SparseMatrix::computeColDistance(int colId, int colCluster, int *rowCL, int *colCL, double **cM)
{
  double temp = 0;
  double *tempDistance = new double[numRow];
  for (int r = 0; r < numRow; r++)
    tempDistance[r] = 0;
  for (int k = colPtr[colId]; k < colPtr[colId+1]; k++)
    tempDistance[rowIdx[k]] = value[k];
  for (int r = 0; r < numRow; r++){
    double tempValue = tempDistance[r] - cM[rowCL[r]][colCluster];
    temp += tempValue * tempValue;
  }
  delete [] tempDistance;
  return temp;
}


void SparseMatrix::computeRowAP(int rowId, double **colCentroid, int *colCL, double *rowAP)
{
  for (int c = 0; c < numRow; c++)		// numRow (in CRS) equals to numCol (in CCS).
    rowAP[c] = 0;
  for (int k = colPtr[rowId]; k < colPtr[rowId+1]; k++)
    rowAP[rowIdx[k]] = value[k];
  for (int c = 0; c < numRow; c++)		// numRow (in CRS) equals to numCol (in CCS).
    rowAP[c] -= colCentroid[colCL[c]][rowId];
}


void SparseMatrix::computeRowAP(int rowId, double **colCentroid, int *colCL, double *rowAP, bool *isReversed)
{
  for (int c = 0; c < numRow; c++)		// numRow (in CRS) equals to numCol (in CCS).
    rowAP[c] = 0;
  if (isReversed[rowId])
    for (int k = colPtr[rowId]; k < colPtr[rowId+1]; k++)
      rowAP[rowIdx[k]] -= value[k];
  else
    for (int k = colPtr[rowId]; k < colPtr[rowId+1]; k++)
      rowAP[rowIdx[k]] = value[k];
  for (int c = 0; c < numRow; c++)		// numRow (in CRS) equals to numCol (in CCS).
    rowAP[c] -= colCentroid[colCL[c]][rowId];
}


void SparseMatrix::computeColAP(int colId, double **rowCentroid, int *rowCL, double *colAP)
{
  for (int r = 0; r < numRow; r++)
    colAP[r] = 0;
  for (int k = colPtr[colId]; k < colPtr[colId+1]; k++)
    colAP[rowIdx[k]] = value[k];
  for (int r = 0; r < numRow; r++)
    colAP[r] -= rowCentroid[rowCL[r]][colId];
}


void SparseMatrix::computeColAP(int colId, double **rowCentroid, int *rowCL, double *colAP, bool *isReversed)
{
  for (int r = 0; r < numRow; r++)
    colAP[r] = 0;
  for (int k = colPtr[colId]; k < colPtr[colId+1]; k++){
    int rowId = rowIdx[k];
    if (isReversed[rowId])
      colAP[rowId] -= value[k];
    else
      colAP[rowId] = value[k];
  }
  for (int r = 0; r < numRow; r++)
    colAP[r] -= rowCentroid[rowCL[r]][colId];
}


void SparseMatrix::subtractRow(double *x, int r)
{
  for (int k = colPtr[r]; k < colPtr[r+1]; k++)
    x[rowIdx[k]] -= value[k];
}


void SparseMatrix::subtractCol(double *x, int c)
{
  for (int k = colPtr[c]; k < colPtr[c+1]; k++)
    x[rowIdx[k]] -= value[k];
}


void SparseMatrix::addRow(double *x, int r)
{
  for (int k = colPtr[r]; k < colPtr[r+1]; k++)
    x[rowIdx[k]] += value[k];
}


void SparseMatrix::addCol(double *x, int c)
{
  for (int k = colPtr[c]; k < colPtr[c+1]; k++)
    x[rowIdx[k]] += value[k];
}


// for reversed rows
void SparseMatrix::subtractRow(double *x, int r, bool *isReversed)
{
  if (isReversed[r])
    for (int k = colPtr[r]; k < colPtr[r+1]; k++)
      x[rowIdx[k]] += value[k]; 
  else
    for (int k = colPtr[r]; k < colPtr[r+1]; k++)
      x[rowIdx[k]] -= value[k]; 
}


void SparseMatrix::subtractCol(double *x, int c, bool *isReversed)
{
  for (int k = colPtr[c]; k < colPtr[c+1]; k++){
    int rowId = rowIdx[k];
    if (isReversed[rowId])
      x[rowId] += value[k];
    else
      x[rowId] -= value[k];
  }  
}


void SparseMatrix::addRow(double *x, int r, bool *isReversed)
{
  if (isReversed[r])
    for (int k = colPtr[r]; k < colPtr[r+1]; k++)
      x[rowIdx[k]] -= value[k];
  else
    for (int k = colPtr[r]; k < colPtr[r+1]; k++)
      x[rowIdx[k]] += value[k];
}


void SparseMatrix::addCol(double *x, int c, bool *isReversed)
{
  for (int k = colPtr[c]; k < colPtr[c+1]; k++){
    int rowId = rowIdx[k];
    if (isReversed[rowId])
      x[rowId] -= value[k];
    else
      x[rowId] += value[k];
  }
}


void SparseMatrix::subtractCol(double **x, int col, int i, int *rowCL, bool *isReversed)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++){
    int rowId = rowIdx[j];
    if (isReversed[rowId])
      x[rowCL[rowId]][col] += value[j];
    else
      x[rowCL[rowId]][col] -= value[j];
  }
}


void SparseMatrix::subtractCol(double *x, int i, int *rowCL, bool *isReversed)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++){
    int rowId = rowIdx[j];
    if (isReversed[rowId])
      x[rowCL[rowId]] += value[j];
    else
      x[rowCL[rowId]] -= value[j];
  }
}


void SparseMatrix::addCol(double **x, int col, int i, int *rowCL, bool *isReversed)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++){
    int rowId = rowIdx[j];
    if (isReversed[rowId])
      x[rowCL[rowId]][col] -= value[j];
    else
      x[rowCL[rowId]][col] += value[j];
  }
}


void SparseMatrix::addCol(double *x, int i, int *rowCL, bool *isReversed)
{
  for (int j = colPtr[i]; j < colPtr[i+1]; j++){
    int rowId = rowIdx[j];
    if (isReversed[rowId])
      x[rowCL[rowId]] -= value[j];
    else
      x[rowCL[rowId]] += value[j];
  }
}


double SparseMatrix::computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM)
{
  double temp = 0;
  double *tempDifference = new double[numRow];
  for (int c = 0; c < numCol; c++){
    for (int r = 0; r < numRow; r++)
      tempDifference[r] = 0;
    for (int k = colPtr[c]; k < colPtr[c+1]; k++)
      tempDifference[rowIdx[k]] = value[k];
    for (int r = 0; r < numRow; r++){
      double tempValue = tempDifference[r] - cM[rowCL[r]][colCL[c]];
      temp += tempValue * tempValue;
    }
  }
  delete [] tempDifference;
  return temp;
}  


double SparseMatrix::computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, bool *isReversed)
{
  double temp = 0;
  double *tempDifference = new double[numRow];
  for (int c = 0; c < numCol; c++){
    for (int r = 0; r < numRow; r++)
      tempDifference[r] = 0;
    for (int k = colPtr[c]; k < colPtr[c+1]; k++){
      int tempRowId = rowIdx[k];
      if (isReversed[tempRowId])
        tempDifference[tempRowId] = (0 - value[k]);
      else
        tempDifference[tempRowId] = value[k];
    }
    for (int r = 0; r < numRow; r++){
      double tempValue = tempDifference[r] - cM[rowCL[r]][colCL[c]];
      temp += tempValue * tempValue;
    }
  }
  delete [] tempDifference;
  return temp;
}  


double SparseMatrix::computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, double **rowCentroid, double **colCentroid)
{
  double temp = 0;
  double *tempDifference = new double[numRow];
  for (int c = 0; c < numCol; c++){
    for (int r = 0; r < numRow; r++)
      tempDifference[r] = 0;
    for (int k = colPtr[c]; k < colPtr[c+1]; k++)
      tempDifference[rowIdx[k]] = value[k];
    for (int r = 0; r < numRow; r++){
      double tempValue = tempDifference[r] - rowCentroid[rowCL[r]][c] - colCentroid[colCL[c]][r] + cM[rowCL[r]][colCL[c]];
      temp += tempValue * tempValue;
    }
  }
  delete [] tempDifference;
  return temp;
}  


double SparseMatrix::computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, double **rowCentroid, double **colCentroid, bool *isReversed)
{
  double temp = 0;
  double *tempDifference = new double[numRow];
  for (int c = 0; c < numCol; c++){
    for (int r = 0; r < numRow; r++)
      tempDifference[r] = 0;
    for (int k = colPtr[c]; k < colPtr[c+1]; k++){
      int tempRowId = rowIdx[k];
      if (isReversed[tempRowId])
        tempDifference[tempRowId] = (0 - value[k]);
      else
        tempDifference[tempRowId] = value[k];
    }
    for (int r = 0; r < numRow; r++){
      double tempValue = tempDifference[r] - rowCentroid[rowCL[r]][c] - colCentroid[colCL[c]][r] + cM[rowCL[r]][colCL[c]];
      temp += tempValue * tempValue;
    }
  }
  delete [] tempDifference;
  return temp;
}  


double SparseMatrix::computeObjectiveFunctionValue4RowCluster(int *rowCL, double **rowCentroid)
{
  double temp = 0, tempValue = 0;
  double *tempRow = new double[numRow];
  for (int r = 0; r < numCol; r++){	// numRow (in CRS) equals to numCol (in CCS).
    for (int c = 0; c < numRow; c++)
      tempRow[c] = 0;
    for (int k = colPtr[r]; k < colPtr[r+1]; k++)
      tempRow[rowIdx[k]] = value[k];
    for (int c = 0; c < numRow; c++){
      tempValue = tempRow[c] - rowCentroid[rowCL[r]][c];
      temp += tempValue * tempValue;
    }
  }
  delete [] tempRow;
  return temp;
}


double SparseMatrix::computeObjectiveFunctionValue4ColCluster(int *colCL, double **colCentroid)
{
  double temp = 0, tempValue = 0;
  double *tempCol = new double[numRow];
  for (int c = 0; c < numCol; c++){
    for (int r = 0; r < numRow; r++)
      tempCol[r] = 0;
    for (int k = colPtr[c]; k < colPtr[c+1]; k++)
      tempCol[rowIdx[k]] = value[k];
    for (int r = 0; r < numRow; r++){
      tempValue = tempCol[r] - colCentroid[colCL[c]][r];
      temp += tempValue * tempValue;
    }
  }
  delete [] tempCol;
  return temp;
}
