/*
  MatrixVector.cc
    Implementation of the MatrixVector class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#include <iostream>
#include <cmath>

#include "MatrixVector.h"


/* 
//The procedure dqrbasis outputs an orthogonal basis spanned by the rows
//  of matrix a (using the QR Factorization of a ).  
void dqrbasis( int m, int n, double **a, double **q , double *work)
{
  for(int i = 0; i < m; i++){
    dmatvec(i, n, q, a[i], work);
    dmatvecat(i, n, q, work, q[i]);
    for(int j = 0; j < n; j++)
      q[i][j] = a[i][j] - q[i][j];
    dvec_l2normalize(n, q[i]);
  }
}


// Does y = A * x, A is mxn, and y & x are mx1 and nx1 vectors respectively  
void dmatvec(int m, int n, double **a, double *x, double *y)
{
  for(int i = 0; i < m; i++){
    double yi = 0.0;
    for(int j = 0; j < n; j++)
      yi += a[i][j] * x[j];
    y[i] = yi;
  }
}


// Does y = A' * x, A is mxn, and y & x are nx1 and mx1 vectors respectively  
void dmatvecat(int m, int n, double **a, double *x, double *y)
{
  for(int i = 0; i < n; i++){
    double yi = 0.0;
    for(int j = 0; j < m; j++)
      yi += a[j][i] * x[j];
    y[i] = yi;
  }
}


// The function dvec_l2normsq computes the square of the Euclidean length 
// (2-norm) of the double precision vector v 
double dvec_l2normsq( int dim, double *v )
{
  double length = 0.0;
  for(int i = 0; i < dim; i++){
    double tmp = *v++;
    length += tmp * tmp;
  }
  return length;
}


// The function dvec_l2normalize normalizes the double precision vector v to have 2-norm equal to 1 
void dvec_l2normalize( int dim, double *v )
{
  double nrm = sqrt(dvec_l2normsq(dim, v));
  if (nrm != 0)
    dvec_scale(1.0 / nrm, dim, v);
}


void dvec_scale( double alpha, int n, double *v )
{
  for(int i = 0; i < n; i++){
    *v++ = *v * alpha;
  }
}
*/


double normalize_vec(double vec[], int n)
{
  double norm = 0.0;
  for (int i = 0; i < n; i++){
    double tempValue = vec[i];
    norm += tempValue * tempValue;
  }
  if (norm > 0){
    norm = sqrt(norm);
    for (int i = 0; i < n; i++)
      vec[i] /= norm;
  }
  return norm;
}


double normalize_vec_1(double vec[], int n)
{
  double norm = 0.0;
  for (int i = 0; i < n; i++)
    norm += fabs(vec[i]);
  if (norm > 0) 
    for (int i = 0; i < n; i++)
	vec[i] /= norm;
  return norm;
}


void average_vec(double vec[], int n, int num)
{
  for (int i = 0; i < n; i++)
    vec[i] /= num;
} 


double Kullback_leibler(double *x, double *y, int n)
  // in nats NOT in bits
{
  double result = 0.0;
  for (int i = 0; i < n; i++){
    double tempX = x[i];
    if (tempX > 0.0){
      double tempY = y[i];
      if (tempY > 0.0)
	result += tempX * log(tempX / tempY);
      else
	return MY_DBL_MAX;
    }
  }	
  return result;
}


double euclidian_distance(double *v1, double *v2, int n)
{
  double result = 0.0;
  for (int j = 0; j < n; j++){
    double tempValue = v1[j] - v2[j];
    result += tempValue * tempValue;
  }
  return sqrt(result);
}


double norm_1(double *x, int n)
{
  double result =0.0;
  for (int i = 0; i < n; i++)
    result += fabs(x[i]);
  return result;
}


double dot_mult(double *v1, double *v2, int n)
{
  double result = 0.0;
  for (int j = 0; j < n; j++)
    result += v1[j] * v2[j];
  return result;
}


double norm_2(double vec[], int n)
  //compute squared L2 norm of vec
{
  double norm = 0.0;
  for (int i = 0; i < n; i++){
    double tempValue = vec[i];
    norm += tempValue * tempValue;
  }
  return norm; 
}


double KL_norm (double vec[], int n)
{
  double norm = 0.0;
  for (int i = 0; i < n; i++){
    double tempValue = vec[i];
    if (tempValue > 0.0)
      norm += tempValue * log(tempValue) / log(2.0);
  }
  return norm;
}


void Ax(double *vals, int *rowinds, int *colptrs, int dim1, int dim2, double *x, double *result)
  /* compute Ax for a sparse matrix A and a dense vector x
     suppose A is (dim1 by dim2) matrix and x is (dim2 by 1) vector */ 
{
  for (int i = 0; i < dim1; i++)
    result[i] = 0.0;
  for (int i = 0; i < dim2; i++)
    for (int j = colptrs[i]; j < colptrs[i+1]; j++)
      result[rowinds[j]] += vals[j] * x[i];
}


void Ax(double **A, double *x, int dim1, int dim2, double *result)
  //for dense matrix, A is a matrix and x is a vector
{
  for (int i = 0; i < dim1; i++){
    result[i] = 0;
    for (int j = 0; j < dim2; j++)
      result[i] += A[i][j] * x[j];
  }
}


double x_dot_y(double *x, double *y, int dim1)
{
  double dot = 0;
  for (int i = 0; i < dim1; i++)
    dot += x[i] * y[i];
  return dot;
}


void power_method(double **A, int dim, double * CV, double *init, double & Lamda)
  // for dense square matrix A of dim by dim, initialized with vector init
{
  //RandomGeneratorMT19937 randNumGenerator;
  double *x=new double[dim], *y=new double[dim], norm, *temp=new double[dim], *old_x=new double[dim], dis=0;
  for (int i = 0; i < dim; i++)
    x[i] = old_x[i] = init[i];
  do {
    Ax(A, x, dim, dim, y);
    norm = sqrt(norm_2(y, dim));
    for (int i=0; i<dim; i++)
      x[i] = y[i] / norm;
    Ax(A, x, dim, dim, temp);
    Lamda = x_dot_y(x, temp, dim);
    dis = euclidian_distance(x, old_x, dim);
    for (int i = 0; i < dim; i++)
      old_x[i] = x[i];
    //cout <<dis<<" ";
   } while (dis  > Power_Method_Epsilon);
  if (CV != NULL)
    for (int i = 0; i < dim; i++)
      CV[i] = x[i];
  //cout <<"Powermethod is done"<<endl;
}


void power_method(double *vals, int *rowinds, int *colptrs, int dim, double * CV, double *init, double & Lamda)
  // power_method works for square matrix only; so we have only 1 value for dimension

{
  //RandomGeneratorMT19937 randNumGenerator;
  double *x=new double[dim], *y=new double[dim], norm, *temp=new double[dim], *old_x=new double[dim], dis=0;
  //randNumGenerator.Set((unsigned)time(NULL));
  for (int i = 0; i < dim; i++)
    x[i] = old_x[i] = init[i];
  do {
    Ax(vals, rowinds, colptrs, dim, dim, x, y);
    norm = sqrt(norm_2(y, dim));
    for (int i = 0; i < dim; i++)
      x[i] = y[i] / norm;
    Ax(vals, rowinds, colptrs, dim, dim, x, temp);
    Lamda = x_dot_y(x, temp, dim);
    dis = euclidian_distance(x, old_x, dim);
    for (int i = 0; i < dim; i++)
      old_x[i] = x[i];
      //cout <<dis<<" ";
  } while (dis > Power_Method_Epsilon);
  if (CV != NULL)
    for (int i = 0; i < dim; i++)
      CV[i] = x[i];
  //cout <<"Powermethod is done"<<endl;
}


double mutual_info(double ** matrix, int r, int c)
  // compute mutual information for a dense matrix of size r by c
{
  double sum = 0.0, mi = 0.0, *margin_c, *margin_r;
  margin_c = new double [c];
  margin_r = new double [r];
  for (int i = 0; i < r; i++)
    margin_r[i] = 0.0;
  for (int j = 0; j < c; j++)
    margin_c[j] = 0.0;
  for (int i = 0; i < r; i++)
    for (int j = 0; j < c; j++){
      margin_r[i] += matrix[i][j];
      margin_c[j] += matrix[i][j];
    }
  for (int i = 0; i < r; i++)
    sum += margin_r[i];
  for (int i = 0; i < r; i++)
    for (int j = 0; j < c; j++){
      double tempValue = matrix[i][j];
      if (tempValue > 0)
	mi += tempValue * log(tempValue / (margin_r[i] * margin_c[j]));
    }	
  mi = mi / sum + log(sum);
  mi /= log(2.0);
  delete [] margin_c;
  delete [] margin_r;
  return mi;
}


double mutual_info(double ** matrix, int r, int c, double *prior)
  // compute mutual information for a dense matrix of size r by c
  // each row is L1-normalized
{
  double mi = 0.0, *margin_c = new double[c];
  for (int i = 0; i < c; i++)
    margin_c[i] = 0.0;
  for (int i = 0; i < r; i++)
    for (int j = 0; j < c; j++)
      margin_c[j] += matrix[i][j] * prior[i];
  for (int i = 0; i < r; i++){
    double temp = 0;
    for (int j = 0; j < c; j++){
      double tempValue = matrix[i][j];
      if (tempValue > 0)
	  temp += tempValue * log(tempValue / margin_c[j]);
    }
    mi += temp * prior[i];
  }
  mi /= log(2.0);
  delete [] margin_c;
  return mi;
}
