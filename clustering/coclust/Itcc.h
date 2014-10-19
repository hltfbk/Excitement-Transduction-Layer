/*
  Itcc.h
    Header file for Itcc class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#if !defined(_ITCC_H_)
#define _ITCC_H_


#include <cmath>

#include "Coclustering.h"

class Itcc : public Coclustering
{

 protected:
  
bool isNormalizedRowCentroid;		// used only for computing one-way objective
bool isNormalizedColCentroid;		// used only for computing one-way objective

  double **pxhatyhat, **qYxhat, **qXyhat, *pX, *pY, *pxhat, *pyhat;
  double PlogP, mutualInfo;
  
  void computeRowCentroid();	// used for qYxhat
  void computeColCentroid();	// used for qXyhat

void computeRowCentroid4RowCluster();	// used only for computing one-way objective
void computeColCentroid4ColCluster();	// used only for computing one-way objective  

  void computeMarginal();
  void computeObjectiveFunction();
void computeObjectiveFunction4RowCluster();	// used only for computing one-way objective
void computeObjectiveFunction4ColCluster();	// used only for computing one-way objective

  double rowDistance(int r, int rc);
  double colDistance(int c, int cc);
  void reassignRC();
  void reassignCC();
  double rowClusterQuality(double *row, double rowP, double *colP);
  void rowClusterQuality(double *result);
  double colClusterQuality(double *row, double rowP, double *colP);
  void colClusterQuality(double *result);

  void recoverRowCL(int begin, int end, oneStep trace []);
  void recoverColCL(int begin, int end, oneStep trace []);

  virtual void doBatchUpdate();
  virtual void doBatchUpdate4VariationI();
  virtual void doBatchUpdate4VariationII();
  virtual void doBatchUpdate4VariationIII();
  virtual void doBatchUpdate4VariationIV();
//  virtual void doBatchUpdate4VariationV();
  void doRowLocalSearch(oneStep trace [], int step);
  void doColLocalSearch(oneStep trace [], int step);
  bool doRowLocalSearchChain();
  bool doColLocalSearchChain();
  virtual void doRowFarthestInitialization();
  virtual void doColFarthestInitialization();
  virtual void doRowRandomPerturbInitialization();
  virtual void doColRandomPerturbInitialization();

 public:
  Itcc(Matrix *inputCCS, Matrix *inputCRS, commandLineArgument myCLA);
  ~Itcc();
  
  virtual void doInitialization();
  virtual void doPingPong();
  
};


#endif //!defined(_ITCC_H)
  
