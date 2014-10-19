/*
  TimerUtility.h
    Header file for the TimerUtility class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#if !defined(_TIMER_UTILITY_H_)
#define _TIMER_UTILITY_H_


#include <iostream>
#include <unistd.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/resource.h>

using namespace std;

class TimerUtil{
  private:
    long sec;
    long microsec;
    timeval startTime;  
    timeval stopTime;
    timeval usrStartTime, sysStartTime;

  public:
    // Note: constructor automatically sets start time
    TimerUtil() {
    setStartTime();
}

 
// Call this at the time you wish to start timing.
void setStartTime() 
{
  sec = microsec = 0;
  gettimeofday(&startTime, NULL);
  struct rusage r_usage;
  getrusage(RUSAGE_SELF, &r_usage);
  usrStartTime.tv_sec = r_usage.ru_utime.tv_sec;
  usrStartTime.tv_usec = r_usage.ru_utime.tv_usec;
  sysStartTime.tv_sec = r_usage.ru_stime.tv_sec;
  sysStartTime.tv_usec = r_usage.ru_stime.tv_usec;
}


// Call this at the time you wish to end timing.
void setStopTime() 
{
  gettimeofday(&stopTime, NULL);
  sec = stopTime.tv_sec - startTime.tv_sec;
  microsec = stopTime.tv_usec - startTime.tv_usec;
  if (microsec < 0) {
    microsec+=1000000;
    sec--;
  }
}  
  

void setStopTime(ostream &os, const char *notes) 
{
  struct rusage r_usage;
  getrusage(RUSAGE_SELF, &r_usage);
  long usec = r_usage.ru_utime.tv_sec - usrStartTime.tv_sec;
  long umsec = r_usage.ru_utime.tv_usec - usrStartTime.tv_usec;
  if (umsec < 0) {
    umsec += 1000000;
    usec--;
  }
  long ssec = r_usage.ru_stime.tv_sec - sysStartTime.tv_sec;
  long smsec = r_usage.ru_stime.tv_usec - sysStartTime.tv_usec;
  if (smsec < 0) {
    smsec += 1000000;
    ssec--;
  }
  setStopTime();
  os << notes << endl;
/*
  os << "CPU Usage: user = " << usec
     << " seconds " << umsec << " us, system = " << ssec 
     << " seconds " << smsec << " us" << endl;
  os << "ELAPSED Time: " << sec << " seconds "
     << microsec << " us" << endl;
*/
  os << "  user   = " << usec << " second(s) " << umsec << " ms" << endl;
  os << "  system = " << ssec << " second(s) " << smsec << " ms" << endl;
}


void setStopTime(ostream &os, const char *notes, const int iter) 
{
  struct rusage r_usage;
  getrusage(RUSAGE_SELF, &r_usage);
  long usec = r_usage.ru_utime.tv_sec - usrStartTime.tv_sec;
  long umsec = r_usage.ru_utime.tv_usec - usrStartTime.tv_usec;
  if (umsec < 0) {
    umsec += 1000000;
    usec--;
  }
  long ssec = r_usage.ru_stime.tv_sec - sysStartTime.tv_sec;
  long smsec = r_usage.ru_stime.tv_usec - sysStartTime.tv_usec;
  if (smsec < 0) {
    smsec += 1000000;
    ssec--;
  }
  setStopTime();
  os << notes << endl;
/*
  os << "CPU Usage: user = " << usec
     << " seconds " << umsec << " us, system = " << ssec 
     << " seconds " << smsec << " us" << endl;
  os << "Time per iteration = " << (usec+umsec/1e6) / iter << endl;
  os << "ELAPSED Time: " << sec << " seconds "
     << microsec << " us" << endl;
*/
  os << "  User     = " << usec << " second(s) " << umsec << " ms" << endl;
  os << "  System   = " << ssec << " second(s) " << smsec << " ms" << endl;
  os << "  Time/Run = " << (usec + umsec / 1e6) / iter << " second(s)" << endl;
}


void getTotalElapsedSec(ostream &os)
{
  struct rusage r_usage;
  getrusage(RUSAGE_SELF, &r_usage);
/*
  os << "CPU Usage: user = " << r_usage.ru_utime.tv_sec
     << " seconds " << r_usage.ru_utime.tv_usec << " us, system = " << r_usage.ru_stime.tv_sec
     << " seconds " << r_usage.ru_stime.tv_usec << " us" << endl;
*/
  os << " User   = " << r_usage.ru_utime.tv_sec << " second(s) " << r_usage.ru_utime.tv_usec << " ms" << endl;
  os << " system = " << r_usage.ru_stime.tv_sec << " second(s) " << r_usage.ru_stime.tv_usec << " ms" << endl;
}


// Returns the seconds between calls to setStartTime and setEndTime
long getElapsedSec() {return sec;}

  
// Returns any extra microseconds after taking to account elapsed seconds
long getElapsedMicrosec() {return microsec;}
   
};


#endif // !defined(_TIMER_UTILITY_H_)
