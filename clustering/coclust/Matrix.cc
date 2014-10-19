/* 
  Matrix.cc
    Implementation of the Matrix class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#include <cmath>
#include <iostream>
#include <fstream>
#include <assert.h>
#include <stdlib.h>

#include "Matrix.h"


Matrix::Matrix(int r, int c)
{
  numRow = r; 
  numCol = c;
  memoryUsed = 0;
  smoothingFactor = 0;
  annealingFactor = 0;
  norm  = NULL;
  L1_sum = Norm_sum = constant = gain = PlogP = mutualInfo = 0;
  priors = L1_norm = p_x = pX = pY = NULL;
  Sim_Mat = NULL;
}


double Matrix::getMutualInfo() { return mutualInfo / log(2.0); }

double Matrix::getPlogP() {return PlogP / log(2.0); }

double* Matrix::getPX() {return pX; }

double* Matrix::getPY() {return pY; }

int Matrix::getNumRow() { return numRow; }

int Matrix::getNumCol() { return numCol; }

double Matrix::GetL1Sum() { return L1_sum; }

long Matrix::GetMemoryUsed() { return memoryUsed; }

double Matrix::GetNorm(int i) { return norm[i]; }

double Matrix::GetL1Norm(int i) { return L1_norm[i]; }

double Matrix::GetNormSum() { return Norm_sum; }

double Matrix::getSmoothingFactor(){ return smoothingFactor; }

double Matrix::getAnnealingFactor(){ return annealingFactor; }


void Matrix::setSmoothingFactor(int smoothingType, double sf)
{ 
  switch (smoothingType){
    case NO_SMOOTHING:
      smoothingFactor = 0;
      break;
    case UNIFORM_SMOOTHING:
    case MAXIMUM_ENTROPY_SMOOTHING:
      smoothingFactor= sf;
      break;
//    case LAPLACE_SMOOTHING:		// not used in co-clustering...
//      rowSmoothingFactor /= numRow;
//      colSmoothingFactor /= numCol;
      break; 
    default:
      break;
  }  
}


void Matrix::setAnnealingFactor(double af)
{
  annealingFactor = af;
}


double Matrix::get_dot_i_j(int i, int j)
{
  return Sim_Mat(i,j);
}

