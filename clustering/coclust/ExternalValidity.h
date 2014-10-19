/*
  ExternalValidity.h
    Header file for the ExternalValidity class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#if !defined(_EXTERNAL_VALIDITY_H_)
#define _EXTERNAL_VALIDITY_H_

using namespace std;

extern long memoryUsed;

class ExternalValidity
{
  protected:
    int **confusionMatrix, *classSize, *clusterSize;
    int numClass, numCluster, numPoint;
    int *classLabel, *clusterLabel;
    bool isSilent;		// not used...

  public:
    ExternalValidity(int nClass, int nCluster, int nPoint, int *classLbl, int *clusterLbl);
    ~ExternalValidity();
    void setSilent(bool s);	// not used...
    void printCM(ostream &os);
    void purity_Entropy_MutInfo(bool isShowingEachCluster, ostream &os1, ostream &os2, ostream &os3);
    void F_measure(ostream &os1, ostream &os2, ostream &os3);
    void micro_avg_precision_recall(double &p_t, double &r_t, ostream &os1, ostream &os2, ostream &os3);
    void getAccuracy(double &accuracy, ostream &os1, ostream &os2, ostream &os3);
};

  
#endif //!defined(_EXTERNAL_VALIDITY_H_)
