/*
  DenseMatrix.h
    Header file for the DenseMatrix class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#if !defined(_DENSE_MATRIX_H_)
#define _DENSE_MATRIX_H_


//#include "Constants.h"
#include "MatrixVector.h"
#include "Matrix.h"

class DenseMatrix : public Matrix
{
  protected:
    int numRow;
    int numCol;
    double **value;
    double *xnorm;
    double *ynorm;
  
    void A_trans_A(int flag, int * index, int *pointers, double ** A_t_A);

  public:
    DenseMatrix ( int row, int col, double ** val);
    ~DenseMatrix();
    inline double &val(int i, int j) { return value[i][j]; }
    virtual void trans_mult(double *x, double *result);
    virtual void squared_trans_mult(double *x, double *result);
    virtual double dot_mult(double *x, int i); 
    virtual double squared_dot_mult(double *x, int i); 
    virtual void right_dom_SV(int *cluster, int *cluster_size, int n_Clusters, double ** CV, double *cluster_quality, int flag);
    virtual void euc_dis(double *x, double norm_x, double *result);
    virtual double euc_dis(double *x, int i, double norm_x);
    virtual void Kullback_leibler(double *x, double *result,int laplace);
    virtual double Kullback_leibler(double *x, int i, int laplace);
    virtual void Kullback_leibler(double *x, double *result,int laplace, double l1norm_X);
    virtual double Kullback_leibler(double *x, int i, int laplace, double l1norm_X);
    virtual double Jenson_Shannon(double *x, int i, double l1n_x);
    virtual void Jenson_Shannon(double *x, double *result, double prior_x);
    virtual void computeNorm_KL(int l);
    virtual void computeNorm_2();
    virtual void computeNorm_1();
    virtual void normalize_mat_L2();
    virtual void normalize_mat_L1();
    virtual void ith_add_CV(int i, double *CV);
    virtual void CV_sub_ith(int i, double *CV);
    virtual void CV_sub_ith_prior(int i, double *CV);
    virtual void ith_add_CV_prior(int i, double *CV);
    virtual double computeMutualInfo();
    virtual double exponential_kernel(double *v, int i, double norm_v, double sigma_squared);
    virtual void exponential_kernel(double *x, double norm_x, double *result, double sigma_squared);
    virtual double i_j_dot_product(int i, int j);
    virtual double squared_i_j_euc_dis(int i, int j);
    virtual void pearson_normalize();
/*
    void dmatvec(int m, int n, double **a, double *x, double *y);
    void dmatvecat(int m, int n, double **a, double *x, double *y);
    void dqrbasis(double **q);
    double dvec_l2normsq(int dim, double *v);
    void dvec_l2normalize(int dim, double *v);
*/

    //itcc
    virtual bool isHavingNegative();
    virtual double getPlogQ(double **pxhatyhat, int *rowCL, int *colCL, double *pXhat, double *pYhat);
    virtual void preprocess();
    virtual void condenseMatrix(int *rowCL, int *colCL, int numRC, int numCC, double **cM);
    virtual void condenseMatrix(int *rowCL, int *colCL, int numRC, int numCC, double **cM, bool *inInversed);
    virtual double Kullback_leibler(double *x, int i, int priorType, int clusterDimension);
    virtual void addRow(double *x, int i, int *colCL);
    virtual void addRow(double **x, int row, int i, int *colCL);
    virtual void addCol(double *x, int i, int *rowCL);
    virtual void addCol(double **x, int col, int i, int *rowCL);
    virtual void subtractRow(double *x, int i, int *colCL);
    virtual void subtractRow(double **x, int row, int i, int *colCL);
    virtual void subtractCol(double *x, int i, int *rowCL);
    virtual void subtractCol(double **x, int col, int i, int *rowCL);

    //mssrIcc
virtual double computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM);    
virtual double computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, bool *isInversed);    
virtual double computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, double **rowCentroid, double **colCentroid);    
virtual double computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, double **rowCentroid, double **colCentroid, bool *isInversed);    
virtual double computeObjectiveFunctionValue4RowCluster(int *rowCL, double **rowCentroid);
virtual double computeObjectiveFunctionValue4ColCluster(int *colCL, double **colCentroid);
    virtual double squaredFNorm();
    virtual double squaredL2Norm4Row(int r);
    virtual double squaredL2Norm4Col(int c);
    virtual double computeRowDistance(int rowId, int rowCluster, int *colCL, double **cM, double rowQuality4Compressed);
    virtual double computeColDistance(int colId, int colCluster, int *rowCL, double **cM, double colQuality4Compressed);
virtual double computeRowDistance(int rowId, int rowCluster, int *rowCL, int *colCL, double **cM);
virtual double computeColDistance(int colId, int colCluster, int *rowCL, int *colCL, double **cM);
    virtual double computeRowDistance(int rowId, int rowCluster, int *colCL, double **cM, double rowQuality4Compressed, bool *isInversed);
    virtual double computeColDistance(int colId, int colCluster, int *rowCL, double **cM, double colQuality4Compressed, bool *isInversed);

    //mssrIIcc
    virtual void computeRowCentroid(int numRC, int *rowCL, double **rowCentroid);
    virtual void computeRowCentroid(int numRC, int *rowCL, double **rowCentroid, bool *isInversed);
    virtual void computeColCentroid(int numCC, int *colCL, double **colCentroid);
    virtual void computeColCentroid(int numCC, int *colCL, double **colCentroid, bool *isInversed);
    virtual void computeRowAP(int rowId, double **colCentroid, int *colCL, double *rowAP);
    virtual void computeRowAP(int rowId, double **colCentroid, int *colCL, double *rowAP, bool *isInversed);
    virtual void computeColAP(int colId, double **rowCentroid, int *rowCL, double *colAP);
    virtual void computeColAP(int colId, double **rowCentroid, int *rowCL, double *colAP, bool *isInversed);
    virtual void addRow(double *x, int r);
    virtual void addRow(double *x, int r, bool *isInversed);
    virtual void addCol(double *x, int c); 
    virtual void addCol(double *x, int c, bool *isInversed); 
    virtual void addCol(double *x, int j, int *rowCL, bool *isInversed);
    virtual void addCol(double **x, int col, int j, int *rowCL, bool *isInversed);
    virtual void subtractRow(double *x, int r);
    virtual void subtractRow(double *x, int r, bool *isInversed);
    virtual void subtractCol(double *x, int c); 
    virtual void subtractCol(double *x, int c, bool *isInversed); 
    virtual void subtractCol(double *x, int j, int *rowCL, bool *isInversed);
    virtual void subtractCol(double **x, int col, int j, int *rowCL, bool *isInversed);
};


#endif // !defined(_DENSE_MATRIX_H_)
