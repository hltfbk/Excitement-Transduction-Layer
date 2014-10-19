/*
  MatrixVector.h
    Subroutines to handle matrix and vector

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#if !defined(_MATRIX_VECTOR_H_)
#define _MATRIX_VECTOR_H_


#include "Matrix.h"

//#include "RandomGenerator.h"
// document encoding 
#define NORM_TERM_FREQ			1 // normalized term frequency
#define NORM_TERM_FREQ_INV_DOC_FREQ	2 // normalized term frequency-inverse
					  // document frequency
#define Power_Method_Epsilon            0.0001
/*

// Encode documents using the specified encoding scheme.
// The elements of the input matrix contains the number of occurrences of a word
// in a document.
void encode_mat(SparseMatrix *mat, int scheme = NORM_TERM_FREQ);
void encode_mat(DenseMatrix *mat, int scheme = NORM_TERM_FREQ);
void dmatvec(int m, int n, double **a, double *x, double *y);
void dmatvecat(int m, int n, double **a, double *x, double *y);
void dqrbasis( int m, int n, double **a, double **q , double *work);
double dvec_l2normsq( int dim, double *v );
void dvec_l2normalize( int dim, double *v );
void dvec_scale( double alpha, int n, double *v );
*/

void average_vec(double vec[], int n, int num);
double norm_2 (double vec[], int n);
double norm_1(double *x, int n);
double KL_norm (double vec[], int n);
double euclidian_distance(double *v1, double *v2, int n);
double Kullback_leibler(double *x, double *y, int n);
double normalize_vec(double *vec, int n);
double normalize_vec_1(double *vec, int n);
double dot_mult(double *v1, double *v2, int n);
void Ax (double **A, double *x, int dim1, int dim2, double *result);
void Ax(double *vals, int *rowinds, int *colptrs, int dim1, int dim2, double *x, double *result);
double x_dot_y(double *x, double *y, int dim1);
void power_method(double **A_t_A, int dim, double * CV, double *init, double & Lamda);
void power_method(double *vals, int *rowinds, int *colptrs, int n_col, double * CV, double *init, double & Lamda);
double mutual_info(double ** matrix, int r, int c);
double mutual_info(double ** matrix, int r, int c, double*);


#endif // !defined(_MATRIX_VECTOR_H_)
