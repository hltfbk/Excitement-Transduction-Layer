/*
  Constants.h
    Header file for the Constants

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#if !defined(_CONSTANTS_H_)
#define _CONSTANTS_H_

#define VERSION_NUMBER				"1.1"

#define EXEC_PROGRAM                            "cocluster"

#define FLOAT_PRECISION         		1.19209290e-07

#define MY_INT_MAX 				32767
#define MY_INT_MIN				-32767

#define MY_LONG_MAX				2147483647
#define MY_LONG_MIN				-2147483647

#define	MY_FLT_DIG				6			/* digits of precision of a "float" */
#define	MY_FLT_MAX				3.402823466E+38F  	/* max decimal value of a "float" */
#define	MY_FLT_MIN				1.175494351E-38F  	/* min decimal value of a "float" */

#define	MY_DBL_DIG				15			/* digits of precision of a "double" */
#define	MY_DBL_MAX				1.7976931348623157E+308	/* max decimal value of a *double" */
#define	MY_DBL_MIN				2.2250738585072014E-308	/* min decimal value of a "double" */

#define DEFAULT_STRING_LENGTH			1024
#define SCALE_SURFIX_LENGTH     		DEFAULT_STRING_LENGTH
#define FILENAME_LENGTH      			DEFAULT_STRING_LENGTH

#define OFF					0
#define ON					1

#define ROW_DIMENSION				1	// for kullback-leibler, but not used in the function 
#define COL_DIMENSION				2	// for kullback-leibler, but not used in the function

#define ITCC_CLASS				"Itcc::"
#define MSSRICC_CLASS				"MssrIcc::"
#define MSSRIICC_CLASS				"MssrIIcc::"
#define ROW_CENTROID				"rowCentroid:"
#define COL_CENTROID				"colCentroid:"
#define COCLUSTER_FILENAME_SUFFIX		"_cocluster"
#define DUMP_FILENAME_SUFFIX			"_dump"
#define OBJECTIVE_FILENAME_SUFFIX		"_objective"
#define STATISTICS_FILENAME_SUFFIX		"_statistics"
#define EMPTY_STRING				""


#define MY_ALGORITHM				'A'
#define MY_NUM_COL_CLUSTER			'C'
#define MY_DUMP_LEVEL				'D'
#define MY_SHOWING_EACH_CLUSTER			'E'
#define MY_HELP					'H'
#define MY_INPUT_FILE				'I'
#define MY_COMPUTING_ONE_WAY			'J'
#define MY_CLASS_FILE				'K'
#define MY_SMOOTHING				'M'
#define MY_NUM_RUN				'N'
#define MY_OUTPUT_FILE				'O'
#define MY_NUM_ROW_CLUSTER			'R'
#define MY_SEEDING				'S'
#define MY_THRESHOLD				'T'
#define MY_UPDATE				'U'
#define MY_TAKING_REVERSE			'X'


// for "-A"
#define MSSRCC_I_ALGORITHM			'E'
#define MSSRCC_II_ALGORITHM			'R'
#define ITCC_ALGORITHM				'I'
#define INFORMATION_THEORETIC_CC		0	// myCLA.algorithmType	(DEFAULT)
#define MINIMUM_SUM_SQUARE_RESIDUE_I_CC		1	// myCLA.algorithmType
#define MINIMUM_SUM_SQUARE_RESIDUE_II_CC	2	// myCLA.algorithmType

// for "-D"
#define MINIMUM_DUMP_LEVEL			0
#define BATCH_UPDATE_DUMP_LEVEL			1
#define LOCAL_SEARCH_DUMP_LEVEL			2
#define MAXIMUM_DUMP_LEVEL			3
#define APPEND_OUT				'A'	// Also used for "-O"
#define OUTPUT_OUT				'O'	// Also used for "-O"

// for "-I"
#define DENSE_INPUT				'D'
#define SPARSE_INPUT				'S'
#define SEPARATE_INPUT				'S'
#define TOGETHER_INPUT				'T'
#define TFN_INPUT				'F'
#define TXX_INPUT				'X'
#define TFN_SCALING				"tfn"
#define TXX_SCALING				"txx"	
#define SPARSE_MATRIX           		0	// myCLA.inputMatrixType
#define DENSE_MATRIX            		1	// myCLA.inputMatrixType (DEFAULT)
#define DENSE_MATRIX_TRANS      		2	// myCLA.inputMatrixType (not used)
#define DIM_MATRIX_SEPARATE_FORMAT		0	// myCLA.inputFormatType (for DENSE_INPUT) (DEFAULT)
#define DIM_MATRIX_TOGETHER_FORMAT		1	// myCLA.inputFormatType (for DENSE_INPUT)
#define	TXX_FILE_FORMAT				0	// myCLA.inputFormatType (for SPARSE_INPUT) (DEFAULT)
#define TFN_FILE_FORMAT				1	// myCLA.inputFormatType (for SPARSE_INPUT)

// for "-K"
#define BOTH_LABEL				'B'
#define COL_LABEL				'C'
#define ROW_LABEL				'R'
#define LABEL_FROM_0				'0'
#define LABEL_FROM_1				'1'
#define START_FROM_0				0	// myCLA.classOffsetType, myCLA.outputOffsetType
#define START_FROM_1				1	// myCLA.classOffsetType, myCLA.outputOffsetType (DEFAULT)
#define NO_EXTERNAL_VALIDITY			0	// myCLA.externalValidityType (DEFAULT)
#define ROW_EXTERNAL_VALIDITY			1	// myCLA.externalValidityType
#define COL_EXTERNAL_VALIDITY			2	// myCLA.externalValidityType
#define BOTH_EXTERNAL_VALIDITY			3	// myCLA.externalValidityType

// for "-M"
#define ANNEALING_TYPE				'A'
#define MAXIMUM_ENTROPY_TYPE			'H'
#define LAPLACE_TYPE				'L'
#define MAGNITUDE_TYPE				'M'
#define NO_TYPE					'N'
#define UNIFORM_TYPE				'U'
#define SMOOTHING_FACTOR        		0.0
#define ANNEALING_FACTOR			1.0	// (DEFAULT)
#define NO_SMOOTHING            		0	// myCLA.smoothingType (DEFAULT)
#define UNIFORM_SMOOTHING       		1	// myCLA.smoothingType
#define MAXIMUM_ENTROPY_SMOOTHING    		2	// myCLA.smoothingType
#define LAPLACE_SMOOTHING       		3	// myCLA.smoothingType
#define MAGNITUDE_SMOOTHING			4	// myCLA.smoothingType

// for "-O"
#define OUTPUT_COCLUSTER_FILE			'C'
#define OUTPUT_OBJECTIVE_FILE			'O'
#define OUTPUT_STATISTICS_FILE			'S'
#define BLOCK_OUT				'B'
#define SIMPLE_OUT				'S'
#define BLOCK_FORMAT            		0	// myCLA.outLabelType
#define SIMPLE_FORMAT           		1	// myCLA.outLabelType (DEFAULT)
#define NO_OPEN_MODE				0	// (DEFAULT)
#define APPEND_MODE				1	// myCLA.outputAccessMode
#define OUTPUT_MODE				2	// myCLA.outputAccessMode (DEFAULT)

// for "-S"
#define FARTHEST_SEEDING			'F'
#define PERTURBATION_SEEDING			'P'
#define RANDOM_SEEDING				'R'
#define SEEDING_SEEDING				'S'
#define PERMUTATION_SEEDING			'M'
#define PERTURBATION_MAGNITUDE			0.1
#define RANDOM_INIT				0	// myCLA.{row|col}InitializationMethod (DEFAULT)
#define RANDOM_PERTURB_INIT 			1	// myCLA.{row|col}InitializationMethod
#define FARTHEST_INIT       			2	// myCLA.{row|col}InitializationMethod
#define PERTURBATION_INIT			3	// myCLA.{row|col}InitializationMethod
#define SEEDING_INIT				4	// myCLA.{row|col}InitializationMethod
#define PERMUTATION_INIT			5	// myCLA.{row|col}InitializationMethod
#define ONE_INPUT_MODE				3	// myCLA.{row|col}SeedingAccessMode
#define BOTH_INPUT_MODE				4	// myCLA.{row|col}SeedingAccessMode

const char initialMethod[][DEFAULT_STRING_LENGTH] 
  = {"Random", 
     "Random perturbation",
     "Farthest apart", 
     "Perturbation",
     "Seeding file",
     "Permutation", 
     "Seeding file (two-line format)",			// not used
     "Seeding file (block format)"			// not used
    };

// for "-T"
#define BATCH_UPDATE_THRESHOLD  		0.001		// (DEFAULT)
#define LOCAL_SEARCH_THRESHOLD  		-0.000001	// for both row and col (DEFAULT)
#define BATCH_UPDATE_STEP			'B'
#define LOCAL_SEARCH_STEP			'L'


// for "-U"
#define LOCAL_SEARCH_LENGTH			0	// (DEFAULT)
#define SINGLE_UPDATE				'0'
#define SINGLE_SINGLE_UPDATE			'1'
#define MULTIPLE_UPDATE				'2'
#define SINGLE_FLIP				'3'
#define MULTIPLE_FLIP				'4'
#define SELECTION_PROBABILITY			0.5
#define MULTIPLE_FACTOR				2.0
#define SINGLE_RESPECTIVELY			0	// myCLA.batchUpdateType
#define SINGLE_IN_BATCH				1	// myCLA.batchUpdateType
#define MULTIPLE_RESPECTIVELY			2	// myCLA.batchUpdateType
#define SINGLE_BY_FLIP				3	// myCLA.batchUpdateType
#define MULTIPLE_BY_FLIP			4	// myCLA.batchUpdateType
#define BOTH_ROW_AND_COL			0	// myCLA.localSearchType (not used)
#define COL_ONLY				1	// myCLA.localSearchType (not used)
#define ROW_ONLY				2	// myCLA.localSearchType (not used)
#define RESUME_LOCAL_SEARCH			-1	// localSearch resumes when empty cluster(s) exist(s)
							// In fact, local search is controlled, based on 
							//   both rowLocalSearchLength and colLocalSearchLength
#define DEFAULT_ROW_LOCAL_SEARCH_LENGTH		20	// used to avoid row empty clusters
#define DEFAULT_COL_LOCAL_SEARCH_LENGTH		20	// used to avoid col empty clusters
#define DEFAULT_MAX_PINGPONG_ITERATION		50	// used to avoid infinite pingpong iteration


struct commandLineArgument {
  int numInvalidCLA;				// used to keep track of # of invalid command-line-arguments
  int algorithmType;				// -A  
  int numColCluster;				// -C
  int dumpLevel;				// -D
  int dumpAccessMode;				// -D
  bool showingEachCluster;			// -E
  int inputMatrixType;				// -I
  int inputFormatType;				// -I
  bool computingOneWayObjective;		// -J
  int externalValidityType;			// -K
  int classOffsetType;				// -K
  int numRowClass;				// used for validateRowCluster() and related with -K
  int numColClass;				// used for validateColCluster() and related with -K
  int smoothingType;				// -M
  double smoothingFactor;			// -M		// not used...	
  double rowAnnealingFactor;			// -M
  double colAnnealingFactor;			// -M
  double rowSmoothingFactor;			// -M
  double colSmoothingFactor;			// -M
  int numRun;					// -N
  int coclusterOffsetType;			// -O
  int coclusterLabelType;			// -O
  int coclusterAccessMode;			// -O
  int objectiveAccessMode;			// -O
  int statisticsAccessMode;			// -0
  int numRowCluster;				// -R
  int rowInitializationMethod;			// -S
  int colInitializationMethod;			// -S
  int rowSeedingOffsetType;			// -S
  int colSeedingOffsetType;			// -S
  int numRowSeedingSet;				// -S s b/c/r 0/1
  int numColSeedingSet;				// -S s b/c/r 0/1
  int rowSeedingAccessMode;			// -S s b/c/r 0/1
  int colSeedingAccessMode;			// -S s b/c/r 0/1
  int numRowPermutation;			// -S m
  int numColPermutation;			// -S m
  double perturbationMagnitude;			// -S
  double rowBatchUpdateThreshold;		// -T b
  double colBatchUpdateThreshold;		// -T b
  double rowLocalSearchThreshold;		// -T l
  double colLocalSearchThreshold;		// -T l
  int batchUpdateType;				// -U b
  int localSearchType;				// -U l		// not used...
  int rowLocalSearchLength;			// -U l 
  int colLocalSearchLength;			// -U l 
  bool takingReverse;				// -X
  int numEmptyRow;				// not used...
  int numEmptyCol;				// to keep # of empty column(s) in input matrix
  int *emptyRowId;				// not used...
  int *emptyColId;				// only needed for checking empty column(s) in VSM in CCS 
  int *rowClassLabel;				// to keep row class label
  int *colClassLabel;				// to keep column class label
  bool havingArgument; 				// Is input matrix specified or not?
  
  char dumpFilename[FILENAME_LENGTH];		// -D
  char inputFilename[FILENAME_LENGTH];		// -I
  char bothClassFilename[FILENAME_LENGTH];	// -K
  char rowClassFilename[FILENAME_LENGTH];	// -K
  char colClassFilename[FILENAME_LENGTH];	// -K
  char coclusterFilename[FILENAME_LENGTH];	// -O
  char statisticsFilename[FILENAME_LENGTH];	// -0
  char objectiveFilename[FILENAME_LENGTH];	// -0
  char bothSeedingFilename[FILENAME_LENGTH];	// -S		// not used...
  char rowSeedingFilename[FILENAME_LENGTH];	// -S
  char colSeedingFilename[FILENAME_LENGTH];	// -S
  char scalingType[SCALE_SURFIX_LENGTH];	// for input matrix in CCS, but not used here
                                                // inputFormatType handles this.
};  


#define DEFAULT_algorithmType			MINIMUM_SUM_SQUARE_RESIDUE_I_CC
#define DEFAULT_numColCluster			1
#define DEFAULT_dumpLevel			MINIMUM_DUMP_LEVEL
#define DEFAULT_dumpAccessMode			NO_OPEN_MODE
#define DEFAULT_showingEachCluster		false
#define DEFAULT_inputMatrixType			DENSE_MATRIX
#define DEFAULT_inputFormatType			DIM_MATRIX_SEPARATE_FORMAT
#define DEFAULT_computingOneWayObjective	false
#define DEFAULT_externalValidityType		NO_EXTERNAL_VALIDITY
#define DEFAULT_classOffsetType			START_FROM_1
#define DEFAULT_numRowClass			0
#define DEFAULT_numColClass			0
#define DEFAULT_smoothingType			NO_SMOOTHING
#define DEFAULT_rowAnnealingFactor		ANNEALING_FACTOR
#define DEFAULT_colAnnealingFactor		ANNEALING_FACTOR
#define DEFAULT_rowSmoothingFactor		SMOOTHING_FACTOR
#define DEFAULT_colSmoothingFactor		SMOOTHING_FACTOR
#define DEFAULT_numRun				1
#define DEFAULT_coclusterOffsetType		START_FROM_1
#define DEFAULT_coclusterLabelType		SIMPLE_FORMAT
#define DEFAULT_coclusterAccessMode		NO_OPEN_MODE
#define DEFAULT_objectiveAccessMode		NO_OPEN_MODE
#define DEFAULT_statisticsAccessMode		NO_OPEN_MODE
#define DEFAULT_numRowCluster			1
#define DEFAULT_rowInitializationMethod		RANDOM_INIT
#define DEFAULT_colInitializationMethod		RANDOM_INIT
#define DEFAULT_rowSeedingOffsetType		START_FROM_1
#define DEFAULT_colSeedingOffsetType		START_FROM_1
#define DEFAULT_numRowSeedingSet		1			// for -S s b/c/r 0/1
#define DEFAULT_numColSeedingSet		1			// for -S s b/c/r 0/1
#define DEFAULT_rowSeedingAccessMode		NO_OPEN_MODE
#define DEFAULT_colSeedingAccessMode		NO_OPEN_MODE
#define DEFAULT_perturbationMagnitude		PERTURBATION_MAGNITUDE
#define DEFAULT_numRowPermutation		1			// for -S m
#define DEFAULT_numColPermutation		1			// for -S m
#define DEFAULT_rowBatchUpdateThreshold		BATCH_UPDATE_THRESHOLD
#define DEFAULT_colBatchUpdateThreshold		BATCH_UPDATE_THRESHOLD
#define DEFAULT_rowLocalSearchThreshold		LOCAL_SEARCH_THRESHOLD
#define DEFAULT_colLocalSearchThreshold		LOCAL_SEARCH_THRESHOLD
#define DEFAULT_batchUpdateType			SINGLE_RESPECTIVELY
#define DEFAULT_localSearchType			BOTH_ROW_AND_COL	// not used
#define DEFAULT_rowLocalSearchLength		0
#define DEFAULT_colLocalSearchLength		0			
#define DEFAULT_takingReverse			false
#define DEFAULT_havingArgument			false

#endif // !defined(_CONSTANTS_H_)
