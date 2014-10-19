#
# Makefile of Co-clustering program
#
#    Copyright (c) 2005, 2006
#             by Hyuk Cho
#    Copyright (c) 2003, 2004
#    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
#               {hyukcho, yguan, suvrit}@cs.utexas.edu
#

programName     = cocluster
timeStamp 	= `date +_%m_%d_%y`

CXX		= g++
#CC		= /usr/bin/gcc
#CC		= gcc
#CFLAGS		= -Wall -g -O2
#CXXFLAGS	= -Wall -O2 -Wno-deprecated
#CXXFLAGS	= -Wall -O2
CXXFLAGS	= -pg

#LDFLAGS		=
LDFLAGS		= -pg
#INCLUDES	= -I/usr/include/g++
INCLUDES	=

SRCS = RandomGenerator.cc MatrixVector.cc Matrix.cc SparseMatrix.cc \
       DenseMatrix.cc ExternalValidity.cc Tools.cc Coclustering.cc \
       Cocluster.cc Itcc.cc MssrIcc.cc MssrIIcc.cc 
OBJS = RandomGenerator.o MatrixVector.o Matrix.o SparseMatrix.o \
       DenseMatrix.o ExternalValidity.o Tools.o Coclustering.o \
       Cocluster.o Itcc.o MssrIcc.o MssrIIcc.o 
AUX  = RandomGenerator.h MatrixVector.h Matrix.h SparseMatrix.h \
       DenseMatrix.h ExternalValidity.h Tools.h Coclustering.h \
       Itcc.h MssrIcc.h MssrIIcc.h Constants.h TimerUtility.h \
       README_CC_CURRENT Makefile 

.SUFFIXES: .c .cc .o

.cc.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) -c $<
#.c.o:
#	$(CC) $(CFLAGS) $(INCLUDES) -c $<
#.f.o:
#	$(F77) -c $<

$(programName)-$(OSTYPE): $(OBJS)
	$(CXX) -o $(programName)-$(OSTYPE) $(OBJS) $(LDFLAGS)

#$(programName)-`uname`: $(OBJS)
#	$(CXX) -o $(programName)-`uname` $(OBJS) $(LDFLAGS)

condor:
	condor_compile /lusr/gnu/bin/g++ -O2 -Wno-deprecated \
	-o condor_$(programName)-$(OSTYPE) \
        $(SRCS)

clean:
	rm -f $(OBJS)

cleanall:
	rm -f $(OBJS) $(programName)-$(OSTYPE) condor_$(programName)-$(OSTYPE) core

compress: 
	-rm -rf tmp.dir
	-mkdir tmp.dir
	-rm cocluster$(timeStamp).tar.gz
	-tar cvf ./tmp.dir/$(programName)$(timeStamp).tar $(SRCS) $(AUX)
	-tar tvf ./tmp.dir/$(programName)$(timeStamp).tar
	-gzip ./tmp.dir/$(programName)$(timeStamp).tar
	-cp ./tmp.dir/$(programName)$(timeStamp).tar.gz ./
	-rm -rf tmp.dir
