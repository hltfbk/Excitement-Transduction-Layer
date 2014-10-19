/*
  Matrix.h
    Header file for the Matrix class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#if !defined(_MATRIX_H_)
#define _MATRIX_H_


#include "Constants.h"

#define Sim_Mat(i,j) (i>=j? Sim_Mat[i][j]:Sim_Mat[j][i])

//typedef double *VECTOR_double;

extern long memoryUsed;

class Matrix
{
  protected:
    int numRow, numCol, kernel, degree;
    long memory_used;
    double smoothingFactor;
    double annealingFactor;
    double L1_sum, Norm_sum, constant, gain, PlogP, mutualInfo;
    double *norm, *L1_norm, *priors, *p_x, *pX, *pY;
    double **Sim_Mat;

  public:
    double getMutualInfo();
    double getPlogP();
    int	getNumRow(); 
    int	getNumCol();
    double GetL1Norm(int i);
    double GetNorm(int i);
    double GetL1Sum();
    double GetNormSum();
    long GetMemoryUsed();
    void setSmoothingFactor(int smoothingType, double smooghingFactor);
    void setAnnealingFactor(double annealingFactor);
    double getSmoothingFactor();
    double getAnnealingFactor();
    double *getPX();
    double *getPY();
    Matrix(int r, int c);
    virtual ~Matrix() {};
    virtual void trans_mult(double *x, double *result) = 0;
    virtual void squared_trans_mult(double *x, double *result) = 0; 
    virtual double dot_mult(double *v, int i) = 0;
    virtual double squared_dot_mult(double *v, int i) = 0;
    virtual void right_dom_SV(int *cluster, int *cluster_size, int n_Clusters, double ** CV, double *cluster_quality, int flag) = 0;
    //virtual void A_trans_A(int flag, int * index, int *pointers, double ** A_t_A) = 0;
    //virtual void euc_dis(double *x, double *result) = 0;
    virtual void euc_dis(double *x, double norm_x, double *result) = 0;
    //virtual double euc_dis(double *v, int i) = 0;
    virtual double euc_dis(double *v, int i, double norm_v) = 0;
    virtual void Kullback_leibler(double *x, double *result, int laplace) = 0;
    virtual double Kullback_leibler(double *x, int i, int laplace) = 0;
    virtual void Kullback_leibler(double *x, double *result, int laplace, double l1norm_X) = 0;
    virtual double Kullback_leibler(double *x, int i, int laplace, double l1norm_X) = 0;
    virtual double Jenson_Shannon(double *x, int i, double l1n_x) = 0;
    virtual void Jenson_Shannon(double *x, double *result, double prior_x) = 0;
    virtual void computeNorm_2() = 0;
    virtual void computeNorm_1() = 0 ;
    virtual void computeNorm_KL(int l) = 0;
    virtual void normalize_mat_L2() = 0;
    virtual void normalize_mat_L1() = 0;
    virtual void pearson_normalize() = 0;
    virtual void ith_add_CV(int i, double *CV) = 0;
    virtual void CV_sub_ith(int i, double *CV) = 0;
    virtual void CV_sub_ith_prior(int i, double *CV) = 0;
    virtual void ith_add_CV_prior(int i, double *CV) = 0;
    virtual double computeMutualInfo() = 0;
    virtual double exponential_kernel(double *v, int i, double norm_v, double sigma_squared) = 0;
    virtual void exponential_kernel(double *x, double norm_x, double *result, double sigma_squared) = 0;
  
    //void  polynomial_kernel(int *cluster, int *cluster_size, int n_Clusters, double **result, double c, int d, int flag);
    virtual double i_j_dot_product(int i, int j) = 0;
    virtual double squared_i_j_euc_dis(int i, int j) = 0;
    double get_dot_i_j(int i, int j);

    //itcc
    virtual bool isHavingNegative() = 0;
    virtual double getPlogQ(double **pxhatyhat, int *rowCL, int *colCL, double *pXhat, double *pYhat) = 0;
    virtual void preprocess() = 0;
    virtual void condenseMatrix(int *rowCL, int *colCL, int numRC, int numCC, double **cM) = 0;
    virtual void condenseMatrix(int *rowCL, int *colCL, int numRC, int numCC, double **cM, bool *isInversed) = 0;
    virtual double Kullback_leibler(double *x, int i, int priorType, int clusterDimension) = 0;
    virtual void addRow(double *x, int i, int *colCL) = 0;
    virtual void addRow(double **x, int row, int i, int *colCL) = 0;
    virtual void addCol(double *x, int i, int *rowCL) = 0;
    virtual void addCol(double **x, int col, int i, int *rowCL) = 0;
    virtual void subtractRow(double *x, int i, int *colCL) = 0;
    virtual void subtractRow(double **x, int row, int i, int *colCL) = 0;
    virtual void subtractCol(double *x, int i, int *rowCL) = 0;
    virtual void subtractCol(double **x, int col, int i, int *rowCL) = 0;

    // mssrIcc
virtual double computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM) = 0;    
virtual double computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, bool *isInversed) = 0;    
virtual double computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, double **rowCentroid, double **colCentroid) = 0;    
virtual double computeObjectiveFunctionValue(int *rowCL, int *colCL, double **cM, double **rowCentroid, double **colCentroid, bool *isInversed) = 0;    
virtual double computeObjectiveFunctionValue4RowCluster(int *rowCL, double **rowCentroid) = 0;
virtual double computeObjectiveFunctionValue4ColCluster(int *colCL, double **colCentroid) = 0;
    virtual double squaredFNorm() = 0;
    virtual double squaredL2Norm4Row(int i) = 0;  
    virtual double squaredL2Norm4Col(int j) = 0;  
    virtual double computeRowDistance(int rowId, int clusterLabel, int *colCL, double **cM, double rowQuality4Compressed) = 0;
    virtual double computeColDistance(int colId, int clusterLabel, int *rowCL, double **cM, double colQuality4Compressed) = 0;
virtual double computeRowDistance(int rowId, int rowCluster, int *rowCL, int *colCL, double **cM) = 0;
virtual double computeColDistance(int colId, int colCluster, int *rowCL, int *colCL, double **cM) = 0;
    virtual double computeRowDistance(int rowId, int clusterLabel, int *colCL, double **cM, double rowQuality4Compressed, bool *isInversed) = 0;
    virtual double computeColDistance(int colId, int clusterLabel, int *rowCL, double **cM, double colQuality4Compressed, bool *isInversed) = 0;

    // mssrIIcc
    virtual void computeRowCentroid(int numRC, int *rowCL, double **rowCentroid) = 0;
    virtual void computeRowCentroid(int numRC, int *rowCL, double **rowCentroid, bool *isInversed) = 0;
    virtual void computeColCentroid(int numCC, int *colCL, double **colCentroid) = 0;
    virtual void computeColCentroid(int numCC, int *colCL, double **colCentroid, bool *isInversed) = 0;
    virtual void computeRowAP(int rowId, double **colCentroid, int *colCL, double *rowAP) = 0;
    virtual void computeRowAP(int rowId, double **colCentroid, int *colCL, double *rowAP, bool *isInversed) = 0;
    virtual void computeColAP(int colId, double **rowCentroid, int *rowCL, double *colAP) = 0;
    virtual void computeColAP(int colId, double **rowCentroid, int *rowCL, double *colAP, bool *isInversed) = 0;
    virtual void addRow(double *x, int i) = 0;
    virtual void addRow(double *x, int i, bool *isInversed) = 0;
    virtual void addCol(double *x, int i) = 0;
    virtual void addCol(double *x, int i, bool *isInversed) = 0;
    virtual void addCol(double *x, int i, int *rowCL, bool *isInversed) =0;
    virtual void addCol(double **x, int col, int i, int *rowCL, bool *isInversed) =0;
    virtual void subtractRow(double *x, int i) = 0;
    virtual void subtractRow(double *x, int i, bool *isInversed) = 0;
    virtual void subtractCol(double *x, int i) = 0;
    virtual void subtractCol(double *x, int i, bool *isInversed) = 0;
    virtual void subtractCol(double *x, int i, int *rowCL, bool *isInversed) =0;
    virtual void subtractCol(double **x, int col, int i, int *rowCL, bool *isInversed) =0;
};


#endif // !defined(_MATRIX_H_)
