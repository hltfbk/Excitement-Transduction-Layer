/*
  MssrIcc.h
    Header file for mssrIcc class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#if !defined(_MSSRICC_H_)
#define _MSSRICC_H_


#include <cmath>

#include "Coclustering.h"

class MssrIcc : public Coclustering
{
  protected:
    bool isNormalizedCompressed;
    bool isNormalizedRowCentroid;		// used only for computing one-way objective
    bool isNormalizedColCentroid;		// used only for computing one-way objective

    double *rowQuality4Compressed, *colQuality4Compressed;
    double **rowCentroid, **colCentroid;	// used only for computing one-way objective
    double squaredFNormA;
   
    void normalizeCompressedMatrix();
    void computeRowQuality4Compressed2WayUnnormalized();
    void computeRowQuality4Compressed2WayNormalized();
    void computeRowQuality4Compressed1WayUnnormalized();
    void computeRowQuality4Compressed1WayNormalized();
    double computeRowQuality4Compressed2WayUnnormalized(int rc);
    double computeRowQuality4Compressed2WayNormalized(int rc);
    double computeRowQuality4Compressed1WayUnnormalized(int rc);
    double computeRowQuality4Compressed1WayNormalized(int rc);
    double computeRowQuality4Compressed2WayUnnormalized(double *row2Way, int rowClusterSize);
    double computeRowQuality4Compressed2WayNormalized(double *row2Way, int rowClusterSize);
    double computeRowQuality4Compressed1WayUnnormalized(double *row1Way, int rowClusterSize);
    double computeRowQuality4Compressed1WayNormalized(double *row1Way, int rowClusterSize);
    void computeColQuality4Compressed2WayUnnormalized();
    void computeColQuality4Compressed2WayNormalized();
    void computeColQuality4Compressed1WayUnnormalized();
    void computeColQuality4Compressed1WayNormalized();
    double computeColQuality4Compressed2WayUnnormalized(int cc);
    double computeColQuality4Compressed2WayNormalized(int cc);
    double computeColQuality4Compressed1WayUnnormalized(int cc);
    double computeColQuality4Compressed1WayNormalized(int cc);
    double computeColQuality4Compressed2WayUnnormalized(double *col2Way, int colClusterSize);
    double computeColQuality4Compressed2WayNormalized(double *col2Way, int colClusterSize);
    double computeColQuality4Compressed1WayUnnormalized(double *col1Way, int colClusterSize);
    double computeColQuality4Compressed1WayNormalized(double *col1Way, int colClusterSize);
    double computeQuality4CompressedUnnormalized();
    double computeQuality4CompressedNormalized();
    void computeObjectiveFunction4Unnormalized();			// for updated Acompressed
    void computeObjectiveFunction4Normalized();
    void computeObjectiveFunction4Normalized(double **Acompressed);	// for unupdated Acompressed
    void computeObjectiveFunction4Normalized(double **Acompressed, bool *isReversed); // for unupdated Acompressed & reversed
    void computeObjectiveFunction4RowCluster();
    void computeObjectiveFunction4ColCluster();

    double rowDistance(int r, int rc);
    double rowDistance(int r, int rc, bool *isReversed);
    double colDistance(int c, int cc);
    double colDistance(int c, int cc, bool *isReversed);
    void reassignRC();
    void reassignRC(bool *isReversed);
    void reassignCC();
    void reassignCC(bool *isReversed);
    void recoverRowCL(int begin, int end, oneStep trace []);
    void recoverRowCL(int begin, int end, oneStep trace [], bool *isReversed);
    void recoverColCL(int begin, int end, oneStep trace []);
    void recoverColCL(int begin, int end, oneStep trace [], bool *isReversed);
    
    virtual void doBatchUpdate();
    virtual void doBatchUpdate(bool *isReversed);
    virtual void doBatchUpdate4VariationI();	// for variation I of batch update (Govaert's algorithm)
    								//   (i.e., single row/col batch update without updating Acompressed) 
    virtual void doBatchUpdate4VariationII();	// for variation II of batch update
    								//   (i.e., multiple row/col batch update with updating Acompressed) 
    virtual void doBatchUpdate4VariationIII();	// for variation III of batch update
    								//   (i.e., multiple row/col batch update without updating Acompressed) 
    virtual void doBatchUpdate4VariationIV();	// for variation IV of batch update
    								//   (i.e., toss a coin to select either row/col batch update with updating Acompressed) 
//    virtual void doBatchUpdate4VariationV();	// for variation IV of batch update
//    								//   (i.e., toss a coin to select either row/col batch update with updating Acompressed) 
    virtual void doBatchUpdate4VariationI(bool *isReversed);
    virtual void doBatchUpdate4VariationII(bool *isReversed);
    virtual void doBatchUpdate4VariationIII(bool *isReversed);
    virtual void doBatchUpdate4VariationIV(bool *isReversed);
//    virtual void doBatchUpdate4VariationV(bool *isReversed);

    void doRowLocalSearch(oneStep trace [], int step);
    void doRowLocalSearch(oneStep trace [], int step, bool *isReversed);
    void doColLocalSearch(oneStep trace [], int step);
    void doColLocalSearch(oneStep trace [], int step, bool *isReversed);
    bool doRowLocalSearchChain();
    bool doColLocalSearchChain();
    virtual void doRowFarthestInitialization();
    virtual void doColFarthestInitialization();
    virtual void doRowRandomPerturbInitialization();
    virtual void doColRandomPerturbInitialization();

    void computeRowCentroid();		// used only for computing one-way objective for non-reversed rows
    void computeColCentroid();		// used only for computing one-way objective for non-reversed rows
    void normalizeRowCentroid();	// used only for computing one-way objective for non-reversed rows
    void normalizeColCentroid();	// used only for computing one-way objective for non-reversed rows

  public:
    MssrIcc(Matrix *inputCCS, Matrix *inputCRS, commandLineArgument myCLA);
    ~MssrIcc();
    virtual void doInitialization();
    virtual void doPingPong();
};


#endif // !defined(_MSSRICC_H_)
  
