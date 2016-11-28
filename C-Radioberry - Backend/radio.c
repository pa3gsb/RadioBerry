#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <math.h>

#include "wdsp.h" 

#include "radio.h"
#include "channel.h"
#include "mode.h"
#include "agc.h"


#define MIN(x,y) (x<y?x:y)
#define MAX(x,y) (x<y?y:x)
#define min(x,y) (x<y?x:y)
#define max(x,y) (x<y?y:x)
// analyzer
#define DETECTOR_MODE_PEAK 0
#define DETECTOR_MODE_ROSENFELL 1
#define DETECTOR_MODE_AVERAGE 2
#define DETECTOR_MODE_SAMPLE 3
#define AVERAGE_MODE_NONE 0
#define AVERAGE_MODE_RECURSIVE 1
#define AVERAGE_MODE_TIME_WINDOW 2
#define AVERAGE_MODE_LOG_RECURSIVE 3

static int display_detector_mode=DETECTOR_MODE_AVERAGE;
static int display_average_mode=AVERAGE_MODE_LOG_RECURSIVE;

static int spectrumWIDTH=800;
static int SPECTRUM_UPDATES_PER_SECOND=10;
static double display_average_time=120.0;
static int updates_per_second=10;
static int filterLow = -4000;
static int filterHigh = 4000;

static double agc_gain=80.0;
static double agc_slope=35.0;
static double agc_hang_threshold=0.0;


static int nr=0;
static int nr2=0;
static int anf=0;
static int snb=0;
static int nr_agc=0; 					// 0=pre AGC 1=post AGC
static int nr2_gain_method=2; 			// 0=Linear 1=Log 2=gamma
static int nr2_npe_method=0;			// 0=OSMS 1=MMSE
static int nr2_ae=1; 					// 0=disable 1=enable

static int buffer_size=1024;
static int fft_size=4096;
static int sample_rate=48000;
static int dspRate=48000;
static int outputRate=48000;

void createReceiveChannel() {

    fprintf(stderr,"OpenChannel %d buffer_size=%d fft_size=%d sample_rate=%d dspRate=%d outputRate=%d\n",
                CHANNEL_RX,
                buffer_size,
                fft_size,
                sample_rate,
                dspRate,
                outputRate);

    OpenChannel(CHANNEL_RX,
                buffer_size,
                fft_size,
                sample_rate,
                dspRate,
                outputRate,
                0, // receive
                0, // run
                0.010, 0.025, 0.0, 0.010, 0);
}

void initReceiveChannel() {
    setRXMode(modeAM);
    SetRXABandpassFreqs(CHANNEL_RX, (double)filterLow, (double)filterHigh);
	setFilter(filterLow, filterHigh);
    setAGCMode(AGC_MEDIUM);

    SetRXAAMDSBMode(CHANNEL_RX, 0);
    SetRXAShiftRun(CHANNEL_RX, 0);

    SetRXAEMNRPosition(CHANNEL_RX, nr_agc);
    SetRXAEMNRgainMethod(CHANNEL_RX, nr2_gain_method);
    SetRXAEMNRnpeMethod(CHANNEL_RX, nr2_npe_method);
    SetRXAEMNRRun(CHANNEL_RX, nr2);
    SetRXAEMNRaeRun(CHANNEL_RX, nr2_ae);

    SetRXAANRVals(CHANNEL_RX, 64, 16, 16e-4, 10e-7); // defaults
    SetRXAANRRun(CHANNEL_RX, nr);
    SetRXAANFRun(CHANNEL_RX, anf);
    SetRXASNBARun(CHANNEL_RX, snb);

    SetRXAPanelGain1(CHANNEL_RX, 0.9);//volume
	
	SetRXAShiftFreq(CHANNEL_RX, 0.0);
    SetRXAShiftRun(CHANNEL_RX, 0);
	
	SetChannelState(CHANNEL_RX,1,0);
}

void calculate_display_average() {
  double display_avb;
  int display_average;

  double t=0.001*display_average_time;
  display_avb = exp(-1.0 / ((double)updates_per_second * t));
  display_average = max(2, (int)min(60, (double)updates_per_second * t));
  SetDisplayAvBackmult(CHANNEL_RX, 0, display_avb);
  SetDisplayNumAverage(CHANNEL_RX, 0, display_average);
}

static void initAnalyzer(int channel,int buffer_size) {
    int flp[] = {0};
    double KEEP_TIME = 0.1;
    int n_pixout=1;
    int spur_elimination_ffts = 1;
    int data_type = 1;
    int fft_size = 8192;
    int window_type = 4;
    double kaiser_pi = 14.0;
    int overlap = 2048;
    int clip = 0;
    int span_clip_l = 0;
    int span_clip_h = 0;
    int pixels=spectrumWIDTH;
    int stitches = 1;
    int avm = 0;
    double tau = 0.001 * 120.0;
    int MAX_AV_FRAMES = 60;
    int display_average = MAX(2, (int) MIN((double) MAX_AV_FRAMES, (double) SPECTRUM_UPDATES_PER_SECOND * tau));
    double avb = exp(-1.0 / (SPECTRUM_UPDATES_PER_SECOND * tau));
    int calibration_data_set = 0;
    double span_min_freq = 0.0;
    double span_max_freq = 0.0;

    int max_w = fft_size + (int) MIN(KEEP_TIME * (double) SPECTRUM_UPDATES_PER_SECOND, KEEP_TIME * (double) fft_size * (double) SPECTRUM_UPDATES_PER_SECOND);

    fprintf(stderr,"SetAnalyzer channel=%d buffer_size=%d\n",channel,buffer_size);

    SetAnalyzer(channel,
            n_pixout,
            spur_elimination_ffts, //number of LO frequencies = number of ffts used in elimination
            data_type, //0 for real input data (I only); 1 for complex input data (I & Q)
            flp, //vector with one elt for each LO frequency, 1 if high-side LO, 0 otherwise
            fft_size, //size of the fft, i.e., number of input samples
            buffer_size, //number of samples transferred for each OpenBuffer()/CloseBuffer()
            window_type, //integer specifying which window function to use
            kaiser_pi, //PiAlpha parameter for Kaiser window
            overlap, //number of samples each fft (other than the first) is to re-use from the previous
            clip, //number of fft output bins to be clipped from EACH side of each sub-span
            span_clip_l, //number of bins to clip from low end of entire span
            span_clip_h, //number of bins to clip from high end of entire span
            pixels, //number of pixel values to return.  may be either <= or > number of bins
            stitches, //number of sub-spans to concatenate to form a complete span
            calibration_data_set, //identifier of which set of calibration data to use
            span_min_freq, //frequency at first pixel value8192
            span_max_freq, //frequency at last pixel value
            max_w //max samples to hold in input ring buffers
    );
}

void createRXAnalyzer() {
    fprintf(stderr,"CreateAnalyzer %d\n",CHANNEL_RX);
    int success;
    XCreateAnalyzer(CHANNEL_RX, &success, 262144, 1, 1, "");
        if (success != 0) {
            fprintf(stderr, "XCreateAnalyzer %d failed: %d\n" ,CHANNEL_RX,success);
        }
    initAnalyzer(CHANNEL_RX,buffer_size);

    SetDisplayDetectorMode(CHANNEL_RX, 0, display_detector_mode);
    SetDisplayAverageMode(CHANNEL_RX, 0,  display_average_mode);
    
    calculate_display_average();
}

void init_radio() {
  char *res;
  char wisdom_directory[1024];
  char wisdom_file[1024];

  fprintf(stderr,"init radioberry \n");

  fprintf(stderr,"checking wisdom file exists \n");
  res=getcwd(wisdom_directory, sizeof(wisdom_directory));
  strcpy(&wisdom_directory[strlen(wisdom_directory)],"/");
  strcpy(wisdom_file,wisdom_directory);
  strcpy(&wisdom_file[strlen(wisdom_file)],"wdspWisdom");
  
   if(access(wisdom_file,F_OK)<0) {
		fprintf(stderr,"creating wsdp (fftw3) wisdom files..........\n");
		WDSPwisdom (wisdom_directory);
	} else fprintf(stderr,"wisdom files already exist \n");

	createReceiveChannel();
	createRXAnalyzer();	
	initReceiveChannel();
}

void setRXMode(int mode) {
  SetRXAMode(CHANNEL_RX, mode);
}

void setAGCGain(int gain) {
	agc_gain = gain;
}

void set_agc(int rx, int agc_mode) {
  SetRXAAGCMode(rx, agc_mode);
  SetRXAAGCSlope(rx,agc_slope);
  SetRXAAGCTop(rx,agc_gain);
  switch(agc_mode) {
    case AGC_OFF:
      break;
    case AGC_LONG:
      SetRXAAGCAttack(rx,2);
      SetRXAAGCHang(rx,2000);
      SetRXAAGCDecay(rx,2000);
      SetRXAAGCHangThreshold(rx,(int)agc_hang_threshold);
      break;
    case AGC_SLOW:
      SetRXAAGCAttack(rx,2);
      SetRXAAGCHang(rx,1000);
      SetRXAAGCDecay(rx,500);
      SetRXAAGCHangThreshold(rx,(int)agc_hang_threshold);
      break;
    case AGC_MEDIUM:
      SetRXAAGCAttack(rx,2);
      SetRXAAGCHang(rx,0);
      SetRXAAGCDecay(rx,250);
      SetRXAAGCHangThreshold(rx,100);
      break;
    case AGC_FAST:
      SetRXAAGCAttack(rx,2);
      SetRXAAGCHang(rx,0);
      SetRXAAGCDecay(rx,50);
      SetRXAAGCHangThreshold(rx,100);
      break;
  }
}

void setAGCMode(int agc_mode){
	set_agc(CHANNEL_RX, agc_mode);
}


//handling offset / shift TODO
/*
void wdsp_set_offset(long long offset) {
    if(offset==0) {
      SetRXAShiftFreq(receiver, (double)offset);
      SetRXAShiftRun(receiver, 0);
    } else {
      SetRXAShiftFreq(receiver, (double)offset);
      SetRXAShiftRun(receiver, 1);
    }

    setFilter(filterLow,filterHigh);
}
*/


void setFilter(int low,int high) {
	filterLow=low;
	filterHigh=high;
    
    //double fl=filterLow+ddsOffset;
    //double fh=filterHigh+ddsOffset;

    RXANBPSetFreqs(CHANNEL_RX,(double)filterLow,(double)filterHigh);
    //SetRXABandpassFreqs(receiver, fl,fh);
    SetRXASNBAOutputBandwidth(CHANNEL_RX, (double)filterLow, (double)filterHigh);

    //SetTXABandpassFreqs(CHANNEL_TX, fl,fh);
}

void stopRadio(){
	DestroyAnalyzer(CHANNEL_RX);
	SetChannelState(CHANNEL_RX,0,0);
	CloseChannel(CHANNEL_RX);
}

// end of radio.c